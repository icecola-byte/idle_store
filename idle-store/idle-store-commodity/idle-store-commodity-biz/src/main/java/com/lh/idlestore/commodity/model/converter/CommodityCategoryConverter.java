package com.lh.idlestore.commodity.model.converter;

import com.lh.idlestore.commodity.infrastructure.cache.dto.CommodityCategoryCacheDTO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryRespVO;
import com.lh.idlestore.commodity.model.vo.response.CommodityCategoryTreeRespVO;
import com.lh.idlestore.commodity.remote.OssRemoteService;
import com.lh.idlestore.commodity.repository.dataobject.CommodityCategoryDO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommodityCategoryConverter {

    private final OssRemoteService ossRemoteService;

    public CommodityCategoryCacheDTO toCacheDTO(CommodityCategoryDO categoryDO) {
        CommodityCategoryCacheDTO cacheDTO = new CommodityCategoryCacheDTO();
        BeanUtils.copyProperties(categoryDO, cacheDTO);
        if (categoryDO.getIconFileId() != null) {
            String iconUrl = ossRemoteService.getAccessUrls(List.of(categoryDO.getIconFileId())).get(categoryDO.getIconFileId());
            cacheDTO.setIconUrl(iconUrl);
        }

        return cacheDTO;
    }

    public List<CommodityCategoryCacheDTO> toCacheDTOList(
            List<CommodityCategoryDO> categories) {
        List<Long> iconFileIds = categories.stream().map(CommodityCategoryDO::getIconFileId).toList();
        Map<Long, String> accessUrls = ossRemoteService.getAccessUrls(iconFileIds);

        return categories.stream()
                .map(commodityCategoryDO -> {
                    CommodityCategoryCacheDTO cacheDTO = new CommodityCategoryCacheDTO();
                    BeanUtils.copyProperties(commodityCategoryDO, cacheDTO);
                    cacheDTO.setIconUrl(accessUrls.get(commodityCategoryDO.getIconFileId()));
                    return cacheDTO;
                }).toList();
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
