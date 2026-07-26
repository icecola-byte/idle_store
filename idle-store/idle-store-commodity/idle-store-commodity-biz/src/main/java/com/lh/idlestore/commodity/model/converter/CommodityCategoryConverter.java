package com.lh.idlestore.commodity.model.converter;

import com.lh.idlestore.commodity.infrastructure.cache.dto.CommodityCategoryCacheDTO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryRespVO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryTreeRespVO;
import com.lh.idlestore.commodity.repository.dataobject.CommodityCategoryDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommodityCategoryConverter {

    public CommodityCategoryCacheDTO toCacheDTO(CommodityCategoryDO categoryDO) {
        CommodityCategoryCacheDTO cacheDTO = new CommodityCategoryCacheDTO();
        BeanUtils.copyProperties(categoryDO, cacheDTO);
        return cacheDTO;
    }

    public List<CommodityCategoryCacheDTO> toCacheDTOList(
            List<CommodityCategoryDO> categories) {

        return categories.stream()
                .map(this::toCacheDTO)
                .toList();
    }

    public CommodityCategoryRespVO toResponse(
            CommodityCategoryCacheDTO cacheDTO) {

        CommodityCategoryRespVO response = new CommodityCategoryRespVO();
        BeanUtils.copyProperties(cacheDTO, response);
        return response;
    }

    public CommodityCategoryTreeRespVO toTreeResponse(
            CommodityCategoryCacheDTO cacheDTO) {

        CommodityCategoryTreeRespVO response =
                new CommodityCategoryTreeRespVO();

        BeanUtils.copyProperties(cacheDTO, response);
        response.setChildren(List.of());

        return response;
    }
}
