package com.lh.idlestore.commodity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.lh.framework.common.exception.BizException;
import com.lh.framework.common.util.JsonUtils;
import com.lh.idlestore.commodity.constant.CommodityConstants;
import com.lh.idlestore.commodity.enums.CommodityOutboxStatusEnum;
import com.lh.idlestore.commodity.enums.CommodityResponseCodeEnum;
import com.lh.idlestore.commodity.infrastructure.cache.dto.CommodityCategoryCacheDTO;
import com.lh.idlestore.commodity.infrastructure.cache.local.CommodityCategoryLocalCache;
import com.lh.idlestore.commodity.infrastructure.cache.redis.CommodityCategoryRedisCache;
import com.lh.idlestore.commodity.model.converter.CommodityCategoryConverter;
import com.lh.idlestore.commodity.model.event.CommodityAggregateTypeEnum;
import com.lh.idlestore.commodity.model.event.CommodityCategoryCacheInvalidatedEvent;
import com.lh.idlestore.commodity.model.event.CommodityEventTypeEnum;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryResponse;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryTreeResponse;
import com.lh.idlestore.commodity.remote.DistributedIdGeneratorRemoteService;
import com.lh.idlestore.commodity.repository.dataobject.CommodityCategoryDO;
import com.lh.idlestore.commodity.repository.dataobject.CommodityOutboxDO;
import com.lh.idlestore.commodity.repository.mapper.CommodityCategoryMapper;
import com.lh.idlestore.commodity.repository.mapper.CommodityMapper;
import com.lh.idlestore.commodity.repository.mapper.CommodityOutboxMapper;
import com.lh.idlestore.commodity.service.CommodityCategoryService;
import com.lh.idlestore.distributed.id.generator.constant.DistributedIdGeneratorApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.lh.idlestore.commodity.constant.CommodityCategoryConstants.ROOT_ID;

@Service
@RequiredArgsConstructor
public class CommodityCategoryServiceImpl implements CommodityCategoryService {

    private final CommodityCategoryMapper commodityCategoryMapper;

    private final CommodityMapper commodityMapper;

    private final CommodityOutboxMapper commodityOutboxMapper;

    private final CommodityCategoryLocalCache commodityCategoryLocalCache;

    private final CommodityCategoryRedisCache commodityCategoryRedisCache;

    private final CommodityCategoryConverter commodityCategoryConverter;

    private final DistributedIdGeneratorRemoteService idGenerator;


    @Override
    public List<CommodityCategoryTreeResponse> getCommodityCategoryTree() {
        List<CommodityCategoryCacheDTO> categories = getCategoriesFromCacheOrDb();
        return buildCategoryTree(categories);
    }

    @Override
    public List<CommodityCategoryResponse> getChildrenByParentId(Long parentId) {
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategoryTree(Long categoryId) {
        // 不能为根
        if (ROOT_ID.equals(categoryId)) {
            throw new BizException(CommodityResponseCodeEnum.ROOT_CATEGORY_CANNOT_DELETE);
        }

        // 判断分类是否存在，同时递归查询子分类
        List<Long> subtreeIds = commodityCategoryMapper.findSubtreeIds(categoryId);
        if (CollUtil.isEmpty(subtreeIds)) {
            throw new BizException(CommodityResponseCodeEnum.CATEGORY_NOT_FOUND);
        }

        // 分类存在，查看该分类下是否有商品
        long commodityCount = commodityMapper.countActiveCommoditiesByCategoryIds(subtreeIds);
        if (commodityCount > 0) {
            throw new BizException(CommodityResponseCodeEnum.CATEGORY_CONTAINS_COMMODITY);
        }
        // 删除商品分类 ID
        commodityCategoryMapper.logicalDeleteByIds(subtreeIds);
        Long eventId = idGenerator.nextId(CommodityConstants.OUTBOX_EVENT_ID_KEY);
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
                .nextRetryTime(now)
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

    private List<CommodityCategoryTreeResponse> buildCategoryTree(
            List<CommodityCategoryCacheDTO> categories) {
        Map<Long, List<CommodityCategoryCacheDTO>> categoryMap =
                categories.stream()
                        .collect(Collectors.groupingBy(
                                CommodityCategoryCacheDTO::getParentId
                        ));

        return buildCategoryTree(ROOT_ID, categoryMap);
    }

    private List<CommodityCategoryTreeResponse> buildCategoryTree(
            Long parentId,
            Map<Long, List<CommodityCategoryCacheDTO>> categoryMap) {

        return categoryMap.getOrDefault(parentId, List.of())
                .stream()
                .map(category -> {
                    CommodityCategoryTreeResponse response =
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
