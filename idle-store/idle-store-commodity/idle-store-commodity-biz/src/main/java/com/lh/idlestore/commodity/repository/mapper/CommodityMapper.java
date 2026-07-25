package com.lh.idlestore.commodity.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.commodity.repository.dataobject.CommodityDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommodityMapper extends BaseMapper<CommodityDO> {

    Long countActiveCommoditiesByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
}
