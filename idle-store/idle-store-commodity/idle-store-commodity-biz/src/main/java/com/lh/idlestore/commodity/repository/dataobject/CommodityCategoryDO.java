package com.lh.idlestore.commodity.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lh.idlestore.commodity.enums.CategoryStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品分类表数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_commodity_category")
public class CommodityCategoryDO {

    @TableId(type = IdType.AUTO)
    private Long categoryId;

    private Long parentId;

    private String categoryName;

    private String iconUrl;

    private Integer sortOrder;

    private CategoryStatusEnum status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Boolean isDeleted;
}
