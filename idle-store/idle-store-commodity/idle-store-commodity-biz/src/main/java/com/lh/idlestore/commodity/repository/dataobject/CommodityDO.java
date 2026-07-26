package com.lh.idlestore.commodity.repository.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.lh.idlestore.commodity.enums.CommodityStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "t_commodity", autoResultMap = true)
public class CommodityDO {

    @TableId(type = IdType.INPUT)
    private Long commodityId;

    private Long categoryId;

    private Long sellerId;

    private String communityId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> imageFileIds;

    private BigDecimal price;

    private Integer totalQuantity;

    private Integer soldQuantity;

    private CommodityStatusEnum status;

    private Integer logisticsMode;

    private String description;

    private Boolean drainagePlan;

    private String commodityName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Boolean isDeleted;

    @Version
    private Integer version;
}
