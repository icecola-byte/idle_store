package com.lh.idlestore.commodity.controller;

import com.lh.framework.biz.operationlog.annotation.ApiOperationLog;
import com.lh.framework.common.response.Response;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryResponse;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryTreeResponse;
import com.lh.idlestore.commodity.service.CommodityCategoryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commodity/categories")
@Validated
@RequiredArgsConstructor
public class CommodityCategoryController {

    private final CommodityCategoryService commodityCategoryService;

    /**
     * 查询指定父分类下的子分类。
     */
    @GetMapping("/{parentId}/children")
    @ApiOperationLog("查询商品分类子类")
    public Response<List<CommodityCategoryResponse>> listChildrenByParentId(@PathVariable("parentId") Long parentId) {
        List<CommodityCategoryResponse> result = commodityCategoryService.getChildrenByParentId(parentId);

        return Response.success(result);
    }


    /**
     * 查询商品分类
     */
    @GetMapping("/tree")
    public Response<List<CommodityCategoryTreeResponse>> listTree() {
        List<CommodityCategoryTreeResponse> result = commodityCategoryService.getCommodityCategoryTree();

        return Response.success(result);
    }

    /**
     * 删除商品分类
     */
    @DeleteMapping("/{parentId}/children")
    @ApiOperationLog("删除某商品分类")
    public Response<Void> deleteCategoryTree(
            @PathVariable("parentId")
            @Positive(message = "商品分类 ID 必须大于 0")
            Long parentId) {
        commodityCategoryService.deleteCategoryTree(parentId);

        return Response.success();
    }
}
