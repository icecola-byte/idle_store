package com.lh.idlestore.commodity.infrastructure.cache.dto;

import lombok.Data;

@Data
public class CommodityCategoryCacheDTO {

    private Long categoryId;

    private Long parentId;

    private String categoryName;

    private String iconUrl;

    private Integer sortOrder;

    private Integer version;
}
