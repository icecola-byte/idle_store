package com.lh.idlestore.commodity.service;

import com.lh.idlestore.commodity.model.vo.request.UpdateCommodityCategoryReqVO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryRespVO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryTreeRespVO;

import java.util.List;

public interface CommodityCategoryService {

    /**
     * 查询完整的商品类别分类树
     * @return 商品分类树(已排序)
     */
    List<CommodityCategoryTreeRespVO> getCommodityCategoryTree();

    /**
     * 查询某个分类下的类别
     * @param parentId 该分类 Id
     * @return
     */
    List<CommodityCategoryRespVO> getChildrenByParentId(Long parentId);

    /**
     * 删除某个商品分类(子分类也会被删除)
     * 不可以是 ROOT_ID
     * @param categoryId 分类 Id
     */
    void deleteCategoryTree(Long categoryId);

    void updateCategoryById(UpdateCommodityCategoryReqVO categoryReqVO);
}
