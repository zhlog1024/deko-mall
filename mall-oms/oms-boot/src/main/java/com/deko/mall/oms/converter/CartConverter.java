package com.deko.mall.oms.converter;

import com.deko.mall.oms.model.dto.CartItemDto;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 购物车对象转化器
 */
@Mapper(componentModel = "spring")
public interface CartConverter {

    @Mappings({
            @Mapping(target = "skuId", source = "id"),
            @Mapping(target = "price", source = "retailPrice"),
    })
    CartItemDto sku2CartItem(SkuInfoDTO skuInfo);

}