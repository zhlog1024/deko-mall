package com.deko.mall.system.converter;

import com.deko.mall.system.model.entity.SysMenu;
import com.deko.mall.system.model.form.MenuForm;
import com.deko.mall.system.model.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 菜单对象转换器
 */
@Mapper(componentModel = "spring")
public interface MenuConverter {


    @Mappings({
            @Mapping(target = "systemTypeLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(entity.getSystemType(), com.deko.mall.common.enums.SystemTypeEnum.class))")
    })
    MenuVO entity2Vo(SysMenu entity);

    MenuForm entity2Form(SysMenu entity);

    SysMenu form2Entity(MenuForm menuForm);

}