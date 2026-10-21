package com.deko.mall.pms.converter;

import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.vo.PmsSpuVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品VO转换器
 */
@Component
public class PmsSpuConverter {

    /**
     * DTO转VO
     *
     * @param dto DTO
     * @return VO
     */
    public PmsSpuVO dto2VO(PmsSpuDTO dto) {
        if (dto == null) {
            return null;
        }
        PmsSpuVO vo = new PmsSpuVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        vo.setCategoryId(dto.getCategoryId());
        vo.setBrandId(dto.getBrandId());
        vo.setPrice(dto.getPrice());
        vo.setSales(dto.getSales());
        vo.setPicUrl(dto.getPicUrl());
        vo.setAlbum(dto.getAlbum());
        vo.setUnit(dto.getUnit());
        vo.setDescription(dto.getDescription());
        vo.setDetail(dto.getDetail());
        vo.setSpuType(dto.getSpuType());
        vo.setSpuTypeDesc(dto.getSpuTypeDesc());
        vo.setStatus(dto.getStatus());
        return vo;
    }

    /**
     * DTO列表转VO列表
     *
     * @param dtoList DTO列表
     * @return VO列表
     */
    public List<PmsSpuVO> dto2VOList(List<PmsSpuDTO> dtoList) {
        return dtoList.stream()
                .map(this::dto2VO)
                .collect(Collectors.toList());
    }
}

