package com.deko.mall.pms.converter;

import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.vo.PmsSpuRenderVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SPU数据转换器
 */
@Component
public class PmsSpuDataConverter {

    /**
     * 转换为VO列表
     *
     * @param spuList SPU列表
     * @return VO列表
     */
    public List<PmsSpuRenderVO> convertToProductVOList(List<PmsSpuDTO> spuList) {
        return spuList.stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为单个VO
     *
     * @param spu SPU DTO
     * @return 渲染VO
     */
    public PmsSpuRenderVO convertToProductVO(PmsSpuDTO spu) {
        PmsSpuRenderVO vo = convertBaseInfo(spu);
        processProductStatus(vo);
        return vo;
    }

    /**
     * 转换基础信息
     */
    private PmsSpuRenderVO convertBaseInfo(PmsSpuDTO spu) {
        PmsSpuRenderVO vo = new PmsSpuRenderVO();
        vo.setSpuId(spu.getId());
        vo.setTitle(spu.getName());
        vo.setImageUrl(spu.getPicUrl());

        // 价格信息
        BigDecimal price = spu.getPrice() != null ? spu.getPrice() : BigDecimal.ZERO;
        BigDecimal originalPrice = spu.getOriginalPrice() != null ? spu.getOriginalPrice() : price;
        vo.setPrice(price);
        vo.setOriginalPrice(originalPrice);

        // 销量和库存
        vo.setSales(spu.getSales() != null ? spu.getSales() : 0);
        Integer totalStock = spu.getTotalStock();
        vo.setStock(totalStock != null ? totalStock : 0);
        vo.setHasStock(totalStock != null && totalStock > 0);

        // 上架状态
        vo.setIsOnSale(spu.getStatus() != null && spu.getStatus() == 1);

        return vo;
    }

    /**
     * 处理商品状态
     */
    private void processProductStatus(PmsSpuRenderVO vo) {
        // 如果没有库存，视为无效商品
        if (vo.getStock() != null && vo.getStock() <= 0) {
            vo.setHasStock(false);
        }
    }
}

