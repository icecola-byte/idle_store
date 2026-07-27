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

    int logicalDeleteByIds(@Param("categoryIds") List<Long> categoryIds);

    /**
     * 锁住指定分类到虚拟根节点的整条路径。
     * 商品发布、修改商品分类、新增子分类时使用。
     * 必须在事务内调用，否则数据库行锁会在语句结束后立即释放。
     */
    List<CommodityCategoryDO> selectPathForShare(@Param("categoryId") Long categoryId);

    /**
     * 锁住一个分类行。分类更新、删除分类树的根节点时使用。
     */
    CommodityCategoryDO selectByIdForUpdate(@Param("categoryId") Long categoryId);

    /**
     * 锁住某个分类及其全部子孙分类，结果按分类 ID 排序以降低死锁概率。
     * 删除分类树时使用。
     */
    List<CommodityCategoryDO> selectSubtreeForUpdate(@Param("categoryId") Long categoryId);
}
