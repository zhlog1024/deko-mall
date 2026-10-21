package com.deko.mall.pms.converter;

import com.deko.mall.pms.model.entity.PmsSpuAttribute;
import com.deko.mall.pms.model.form.PmsSpuAttributeForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 商品属性对象转换器
 */
@Mapper(componentModel = "spring")
public interface SpuAttributeConverter {

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    PmsSpuAttribute form2Entity(PmsSpuAttributeForm form);

}
