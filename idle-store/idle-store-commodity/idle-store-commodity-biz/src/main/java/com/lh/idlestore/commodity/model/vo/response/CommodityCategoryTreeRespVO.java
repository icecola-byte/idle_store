package com.lh.idlestore.commodity.model.vo.response;

import lombok.Data;

import java.util.List;

@Data
public class CommodityCategoryTreeRespVO {

    private Long categoryId;

    private String categoryName;

    private String iconUrl;

    private List<CommodityCategoryTreeRespVO> children;
}
