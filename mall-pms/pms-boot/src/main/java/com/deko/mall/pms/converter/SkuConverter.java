package com.deko.mall.pms.converter;

import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.model.form.PmsSkuForm;
import com.deko.mall.pms.model.vo.PmsSkuVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 商品对象转换器
 */
@Mapper(componentModel = "spring")
public interface SkuConverter {

    SkuInfoDTO entity2SkuInfoDto(PmsSku entity);

    PmsSkuVO entity2VO(PmsSku sku);

    PmsSku form2Entity(PmsSkuForm sku);

    List<PmsSku> form2ListEntity(List<PmsSkuForm> skuList);

    List<PmsSkuVO> entity2VOList(List<PmsSku> skuList);
}
