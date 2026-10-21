package com.deko.mall.pms.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.entity.PmsFreightTemplate;
import com.deko.mall.pms.model.vo.FreightTemplateVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 商品对象转换器
 */
@Mapper(componentModel = "spring")
public interface FreightTemplateConverter {


    @Mappings({
            @Mapping(target = "isDefaultLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(entity.getIsDefault(), com.deko.mall.pms.enums.YesNoEnum.class))"),
            @Mapping(target = "chargeTypeLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(entity.getChargeType(), com.deko.mall.pms.enums.ChargeTypeEnum.class))"),
            @Mapping(target = "isFreeShippingLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(entity.getIsFreeShipping(), com.deko.mall.pms.enums.YesNoEnum.class))")
    })
    FreightTemplateVO entity2VO(PmsFreightTemplate entity);

    Page<FreightTemplateVO> entity2Page(Page<PmsFreightTemplate> page);


}
