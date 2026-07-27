package com.lh.idlestore.commodity.service.impl;

import com.lh.framework.common.exception.BizException;
import com.lh.idlestore.commodity.enums.CategoryStatusEnum;
import com.lh.idlestore.commodity.enums.CommodityResponseCodeEnum;
import com.lh.idlestore.commodity.repository.dataobject.CommodityCategoryDO;
import com.lh.idlestore.commodity.repository.mapper.CommodityCategoryMapper;
import com.lh.idlestore.commodity.service.CommodityCategoryBindingGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.lh.idlestore.commodity.constant.CommodityCategoryConstants.ROOT_ID;

/**
 * 基于 MySQL 行锁的分类绑定校验。
 */
@Service
@RequiredArgsConstructor
public class CommodityCategoryBindingGuardImpl implements CommodityCategoryBindingGuard {

    private final CommodityCategoryMapper commodityCategoryMapper;

    /**
     * MANDATORY 强制调用方把校验和绑定写操作放进同一事务。
     * 共享锁会在外层事务提交或回滚时统一释放。
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void checkCategoryCanBind(Long categoryId) {
        List<CommodityCategoryDO> categoryPath =
                commodityCategoryMapper.selectPathForShare(categoryId);

        boolean reachesRoot = categoryPath.stream()
                .anyMatch(category -> ROOT_ID.equals(category.getParentId()));
        boolean allEnabled = categoryPath.stream()
                .allMatch(category -> category.getStatus() == CategoryStatusEnum.ENABLED
                        && !Boolean.TRUE.equals(category.getIsDeleted()));

        // 路径不完整意味着分类不存在、父节点被物理删除或数据已断链。
        if (categoryPath.isEmpty() || !reachesRoot || !allEnabled) {
            throw new BizException(CommodityResponseCodeEnum.CATEGORY_NOT_AVAILABLE);
        }
    }
}
