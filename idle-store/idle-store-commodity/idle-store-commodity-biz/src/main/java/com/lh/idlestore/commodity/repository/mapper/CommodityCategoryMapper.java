package com.lh.idlestore.commodity.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.commodity.repository.dataobject.CommodityCategoryDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类持久化 Mapper。
 */
public interface CommodityCategoryMapper extends BaseMapper<CommodityCategoryDO> {

    List<CommodityCategoryDO> queryEnabledCommodityCategories();

    List<Long> findSubtreeIds(@Param("categoryId") Long categoryId);

    int logicalDeleteByIds(@Param("categoryIds") List<Long> categoryIds);

    List<Long> selectIconFileIdsByIds(@Param("subtreeIds") List<Long> subtreeIds);

}
