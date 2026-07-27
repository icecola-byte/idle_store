package com.lh.idlestore.commodity.service.impl;

import com.google.common.base.Preconditions;
import com.lh.framework.common.exception.BizException;
import com.lh.framework.common.util.JsonUtils;
import com.lh.framework.web.enums.CommonResponseCodeEnum;
import com.lh.idlestore.commodity.enums.CategoryStatusEnum;
import com.lh.idlestore.commodity.infrastructure.cache.config.CommodityCategoryCacheProperties;
import com.lh.idlestore.commodity.infrastructure.outbox.constant.CommodityOutboxConstants;
import com.lh.idlestore.commodity.infrastructure.outbox.enums.CommodityAggregateTypeEnum;
import com.lh.idlestore.commodity.infrastructure.outbox.enums.CommodityEventTypeEnum;
import com.lh.idlestore.commodity.infrastructure.outbox.enums.CommodityOutboxStatusEnum;
import com.lh.idlestore.commodity.enums.CommodityResponseCodeEnum;
import com.lh.idlestore.commodity.infrastructure.cache.dto.CommodityCategoryCacheDTO;
import com.lh.idlestore.commodity.infrastructure.cache.local.CommodityCategoryLocalCache;
import com.lh.idlestore.commodity.infrastructure.cache.redis.CommodityCategoryRedisCache;
import com.lh.idlestore.commodity.model.converter.CommodityCategoryConverter;
import com.lh.idlestore.commodity.model.vo.request.InsertCommodityCategoryReqVO;
import com.lh.idlestore.commodity.model.vo.request.UpdateCommodityCategoryReqVO;
import com.lh.idlestore.commodity.mq.event.CommodityCategoryCacheInvalidatedEvent;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryRespVO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryTreeRespVO;
import com.lh.idlestore.commodity.mq.event.CommodityCategoryIconFileDeleteRequestEvent;
import com.lh.idlestore.commodity.remote.DistributedIdGeneratorRemoteService;
import com.lh.idlestore.commodity.remote.OssRemoteService;
import com.lh.idlestore.commodity.repository.dataobject.CommodityCategoryDO;
import com.lh.idlestore.commodity.repository.dataobject.CommodityOutboxDO;
import com.lh.idlestore.commodity.repository.mapper.CommodityCategoryMapper;
import com.lh.idlestore.commodity.repository.mapper.CommodityMapper;
import com.lh.idlestore.commodity.repository.mapper.CommodityOutboxMapper;
import com.lh.idlestore.commodity.service.CommodityCategoryService;
import com.lh.idlestore.commodity.service.CommodityCategoryBindingGuard;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.lh.idlestore.commodity.constant.CommodityCategoryConstants.ROOT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommodityCategoryServiceImpl implements CommodityCategoryService {

    private final CommodityCategoryMapper commodityCategoryMapper;

    private final CommodityMapper commodityMapper;

    private final CommodityOutboxMapper commodityOutboxMapper;

    private final CommodityCategoryLocalCache commodityCategoryLocalCache;

    private final CommodityCategoryRedisCache commodityCategoryRedisCache;

    private final CommodityCategoryConverter commodityCategoryConverter;

    private final DistributedIdGeneratorRemoteService idGenerator;

    private final CommodityCategoryCacheProperties commodityCategoryCacheProperties;

    private final OssRemoteService ossRemoteService;

    private final TransactionTemplate transactionTemplate;

    private final CommodityCategoryBindingGuard commodityCategoryBindingGuard;


    @Override
    public List<CommodityCategoryTreeRespVO> getCommodityCategoryTree() {
        List<CommodityCategoryCacheDTO> categories = getCategoriesFromCacheOrDb();
        return buildCategoryTree(categories);
    }

    @Override
    public List<CommodityCategoryRespVO> getChildrenByParentId(Long parentId) {
        if (parentId == null) {
            return List.of();
        }

        List<CommodityCategoryCacheDTO> categories = getCategoriesFromCacheOrDb();

        checkParentCategoryExists(parentId, categories);

        return categories.stream()
                .filter(category -> Objects.equals(
                        category.getParentId(),
                        parentId
                ))
                .map(commodityCategoryConverter::toResponse)
                .toList();
    }

    @Override
    public void deleteCategoryTree(Long categoryId) {
        // 不能为根
        if (ROOT_ID.equals(categoryId)) {
            throw new BizException(CommodityResponseCodeEnum.ROOT_CATEGORY_CANNOT_DELETE);
        }

        transactionTemplate.executeWithoutResult(status -> {
            // 先锁删除根节点，阻止新子分类或商品绑定操作进入该子树。
            CommodityCategoryDO rootCategory =
                    commodityCategoryMapper.selectByIdForUpdate(categoryId);
            if (rootCategory == null) {
                throw new BizException(CommodityResponseCodeEnum.CATEGORY_NOT_FOUND);
            }

            // 再锁整棵子树；读图标、查商品、逻辑删除都基于这份锁定快照进行。
            List<CommodityCategoryDO> subtree =
                    commodityCategoryMapper.selectSubtreeForUpdate(categoryId);
            List<Long> subtreeIds = subtree.stream()
                    .map(CommodityCategoryDO::getCategoryId)
                    .toList();

            long commodityCount = commodityMapper.countActiveCommoditiesByCategoryIds(subtreeIds);
            if (commodityCount > 0) {
                throw new BizException(CommodityResponseCodeEnum.CATEGORY_CONTAINS_COMMODITY);
            }

            List<Long> iconFileIds = subtree.stream()
                    .map(CommodityCategoryDO::getIconFileId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            commodityCategoryMapper.logicalDeleteByIds(subtreeIds);

            LocalDateTime now = LocalDateTime.now();
            appendCacheInvalidationOutbox(categoryId, now);
            appendCacheInvalidationOutbox(categoryId,
                    now.plus(commodityCategoryCacheProperties.getDelayedInvalidationDelay()));
            // 一文件一事件，失败时可由消息系统独立重试。
            iconFileIds.forEach(iconFileId -> appendIconFileDeleteOutbox(categoryId, iconFileId));
        });
    }

    @Override
    public void updateCategoryById(UpdateCommodityCategoryReqVO categoryReqVO) {
        Long categoryId = categoryReqVO.getCategoryId();
        Long newIconFileId = null;
        try {
            MultipartFile iconFile = categoryReqVO.getIconFile();
            if (iconFile != null && !iconFile.isEmpty()) {
                newIconFileId = ossRemoteService.uploadFile(iconFile).fileId();
            }

            Long uploadedIconFileId = newIconFileId;
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                // 排他锁覆盖“读取旧值 + 更新 + Outbox”，避免与删除树、状态变更交叉。
                CommodityCategoryDO commodityCategoryDO =
                        commodityCategoryMapper.selectByIdForUpdate(categoryId);
                if (commodityCategoryDO == null) {
                    throw new BizException(CommodityResponseCodeEnum.CATEGORY_NOT_FOUND);
                }

                boolean needUpdate = false;
                boolean categoryInformationChanged = false;
                CommodityCategoryDO updateCommodityCategoryDO = new CommodityCategoryDO();
                updateCommodityCategoryDO.setCategoryId(categoryId);

                String categoryName = categoryReqVO.getCategoryName();
                if (Objects.nonNull(categoryName)
                        && !Objects.equals(categoryName, commodityCategoryDO.getCategoryName())) {
                    Preconditions.checkArgument(StringUtils.isNotBlank(categoryName),
                            CommonResponseCodeEnum.PARAM_NOT_VALID.getErrorMessage());
                    updateCommodityCategoryDO.setCategoryName(categoryName);
                    needUpdate = true;
                    categoryInformationChanged = true;
                }

                Integer sortOrder = categoryReqVO.getSortOrder();
                if (Objects.nonNull(sortOrder)
                        && !Objects.equals(sortOrder, commodityCategoryDO.getSortOrder())) {
                    Preconditions.checkArgument(sortOrder >= 0,
                            CommonResponseCodeEnum.PARAM_NOT_VALID.getErrorMessage());
                    updateCommodityCategoryDO.setSortOrder(sortOrder);
                    needUpdate = true;
                }

                Integer status = categoryReqVO.getStatus();
                if (Objects.nonNull(status)
                        && !Objects.equals(status, commodityCategoryDO.getStatus().getCode())) {
                    Preconditions.checkArgument(CategoryStatusEnum.isValid(status),
                            CommonResponseCodeEnum.PARAM_NOT_VALID.getErrorMessage());
                    // 只更新父分类本身；子分类保留各自原有状态。
                    updateCommodityCategoryDO.setStatus(CategoryStatusEnum.fromCode(status));
                    needUpdate = true;
                    categoryInformationChanged = true;
                }

                if (uploadedIconFileId != null) {
                    updateCommodityCategoryDO.setIconFileId(uploadedIconFileId);
                    needUpdate = true;
                }
                if (!needUpdate) {
                    return;
                }

                updateCommodityCategoryDO.setUpdateTime(LocalDateTime.now());
                // 排序、图标不影响选择哪个分类，不更新版本号
                if (categoryInformationChanged) {
                    updateCommodityCategoryDO.setVersion(commodityCategoryDO.getVersion() + 1);
                }
                int updatedRows = commodityCategoryMapper.updateById(updateCommodityCategoryDO);
                if (updatedRows != 1) {
                    throw new BizException(CommodityResponseCodeEnum.CATEGORY_NOT_FOUND);
                }

                LocalDateTime now = LocalDateTime.now();
                appendCacheInvalidationOutbox(categoryId, now);
                appendCacheInvalidationOutbox(categoryId, now.plus(commodityCategoryCacheProperties.getDelayedInvalidationDelay()));

                if (uploadedIconFileId != null && commodityCategoryDO.getIconFileId() != null) {
                    appendIconFileDeleteOutbox(categoryId, commodityCategoryDO.getIconFileId());
                }
            });
        } catch (DuplicateKeyException e) {
            appendNewIconCleanupOutbox(categoryId, newIconFileId);
            throw new BizException(CommodityResponseCodeEnum.CATEGORY_NAME_ALREADY_EXISTS);
        } catch (RuntimeException e) {
            appendNewIconCleanupOutbox(categoryId, newIconFileId);
            throw e;
        }
    }

    @Override
    public void insertCategory(InsertCommodityCategoryReqVO categoryReqVO) {
        Long iconFileId = null;
        try {
            MultipartFile iconFile = categoryReqVO.getIconFile();
            if (iconFile != null && !iconFile.isEmpty()) {
                iconFileId = ossRemoteService.uploadFile(categoryReqVO.getIconFile()).fileId();
            }

            final Long newIconFileId = iconFileId;
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                Long parentId = categoryReqVO.getParentId();
                if (!ROOT_ID.equals(parentId)) {
                    // 锁住父分类到根的路径，避免在删除/禁用期间插入子分类。
                    commodityCategoryBindingGuard.checkCategoryCanBind(parentId); // --> 这个操作必须写到事务里面
                }
                // 唯一键约束
                CommodityCategoryDO newCommodityCategory = CommodityCategoryDO.builder()
                        .parentId(parentId)
                        .categoryName(categoryReqVO.getCategoryName())
                        .sortOrder(categoryReqVO.getSortOrder() == null ? 0 : categoryReqVO.getSortOrder())
                        .iconFileId(newIconFileId)
                        .status(CategoryStatusEnum.ENABLED)
                        .version(1)
                        .build();
                commodityCategoryMapper.insert(newCommodityCategory);
                LocalDateTime now = LocalDateTime.now();
                appendCacheInvalidationOutbox(newCommodityCategory.getCategoryId(), now);
                appendCacheInvalidationOutbox(newCommodityCategory.getCategoryId(), now.plus(commodityCategoryCacheProperties.getDelayedInvalidationDelay()));
            });
        // 唯一键约束异常
        } catch (DuplicateKeyException e) {
            if (Objects.nonNull(iconFileId)) {
                appendNewIconCleanupOutbox(categoryReqVO.getParentId(), iconFileId);
            }
            throw new BizException(CommodityResponseCodeEnum.CATEGORY_NAME_ALREADY_EXISTS);
        } catch (RuntimeException e) {
            if (Objects.nonNull(iconFileId)) {
                appendNewIconCleanupOutbox(categoryReqVO.getParentId(), iconFileId);
            }
            throw e;
        }

    }

    private void appendNewIconCleanupOutbox(
            Long categoryId,
            Long newIconFileId) {

        if (newIconFileId == null) {
            return;
        }

        try {
            appendIconFileDeleteOutbox(categoryId, newIconFileId);
        } catch (Exception cleanupException) {
            log.error(
                    "分类更新失败后写入新图标删除任务失败，fileId={}",
                    newIconFileId,
                    cleanupException
            );
        }
    }

    /**
     * 写入分类图标文件删除请求
     * @param categoryId 商品分类 ID
     * @param fileId IconFileId
     */
    private void appendIconFileDeleteOutbox(Long categoryId, Long fileId) {
        Long eventId = idGenerator.nextId(CommodityOutboxConstants.OUTBOX_EVENT_ID_KEY);
        LocalDateTime now = LocalDateTime.now();
        commodityOutboxMapper.insert(CommodityOutboxDO.builder()
                .eventId(eventId)
                .aggregateType(CommodityAggregateTypeEnum.CATEGORY)
                .aggregateId(categoryId)
                .eventType(CommodityEventTypeEnum.CATEGORY_ICON_FILE_DELETE_REQUEST)
                .eventStatus(CommodityOutboxStatusEnum.PENDING)
                .retryCount(0)
                .eventPayload(JsonUtils.toJsonString(
                        CommodityCategoryIconFileDeleteRequestEvent.builder()
                                .eventId(eventId)
                                .fileId(fileId)
                                .build()
                ))
                .nextRetryTime(now) // 首次发送时间
                .createTime(now)
                .updateTime(now)
                .build());
    }

    private void appendCacheInvalidationOutbox(Long categoryId, LocalDateTime sendTime) {
        Long eventId = idGenerator.nextId(CommodityOutboxConstants.OUTBOX_EVENT_ID_KEY);
        LocalDateTime now = LocalDateTime.now();
        commodityOutboxMapper.insert(CommodityOutboxDO.builder()
                .eventId(eventId)
                .aggregateType(CommodityAggregateTypeEnum.CATEGORY)
                .aggregateId(categoryId)
                .eventType(CommodityEventTypeEnum.CATEGORY_CACHE_INVALIDATED)
                .eventStatus(CommodityOutboxStatusEnum.PENDING)
                .retryCount(0)
                .eventPayload(JsonUtils.toJsonString(
                        CommodityCategoryCacheInvalidatedEvent.builder()
                                .eventId(eventId)
                                .build()
                ))
                .nextRetryTime(sendTime) // 首次发送时间
                .createTime(now)
                .updateTime(now)
                .build());
    }


    /**
     * 从本地缓存、Redis 或 DB 获取启用分类平铺列表。
     *
     * Redis 返回 null 表示未命中；
     * 返回空 List 表示命中空数据，不再查 DB。
     */
    private List<CommodityCategoryCacheDTO> getCategoriesFromCacheOrDb() {
        return commodityCategoryLocalCache.getCategories(
                this::getCategoriesFromRedisOrDb
        );
    }

    private List<CommodityCategoryCacheDTO> getCategoriesFromRedisOrDb() {
        List<CommodityCategoryCacheDTO> cachedCategories =
                commodityCategoryRedisCache.getCategories();

        if (cachedCategories != null) {
            return cachedCategories;
        }

        List<CommodityCategoryDO> categoryDOList =
                commodityCategoryMapper.queryEnabledCommodityCategories();

        List<CommodityCategoryCacheDTO> categories =
                commodityCategoryConverter.toCacheDTOList(categoryDOList);

        commodityCategoryRedisCache.setCategories(categories);

        return categories;
    }

    private void checkParentCategoryExists(
            Long parentId,
            List<CommodityCategoryCacheDTO> categories) {
        // ROOT_ID 是虚拟根节点，始终合法
        if (ROOT_ID.equals(parentId)) {
            return;
        }

        boolean exists = categories.stream()
                .anyMatch(category -> Objects.equals(
                        category.getCategoryId(),
                        parentId
                ));

        if (!exists) {
            throw new BizException(CommodityResponseCodeEnum.CATEGORY_NOT_FOUND);
        }
    }

    private List<CommodityCategoryTreeRespVO> buildCategoryTree(
            List<CommodityCategoryCacheDTO> categories) {
        Map<Long, List<CommodityCategoryCacheDTO>> categoryMap =
                categories.stream()
                        .collect(Collectors.groupingBy(
                                CommodityCategoryCacheDTO::getParentId
                        ));

        return buildCategoryTree(ROOT_ID, categoryMap);
    }

    private List<CommodityCategoryTreeRespVO> buildCategoryTree(
            Long parentId,
            Map<Long, List<CommodityCategoryCacheDTO>> categoryMap) {

        return categoryMap.getOrDefault(parentId, List.of())
                .stream()
                .map(category -> {
                    CommodityCategoryTreeRespVO response =
                            commodityCategoryConverter.toTreeResponse(category);

                    response.setChildren(buildCategoryTree(
                            category.getCategoryId(),
                            categoryMap
                    ));

                    return response;
                })
                .toList();
    }

}
