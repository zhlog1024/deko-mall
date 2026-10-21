package com.deko.mall.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.system.model.entity.SysRole;
import com.deko.mall.system.model.form.RoleForm;
import com.deko.mall.system.model.vo.RolePageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 角色对象转换器
 */
@Mapper(componentModel = "spring")
public interface RoleConverter {


    Page<RolePageVO> entity2Page(Page<SysRole> page);


    @Mappings({
            @Mapping(target = "systemTypeLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(role.getSystemType(), com.deko.mall.common.enums.SystemTypeEnum.class))")
    })
    RolePageVO entity2Vo(SysRole role);

    @Mappings({
            @Mapping(target = "value", source = "id"),
            @Mapping(target = "label", source = "name")
    })
    Option entity2Option(SysRole role);


    List<Option> entities2Options(List<SysRole> roles);

    SysRole form2Entity(RoleForm roleForm);

    RoleForm entity2Form(SysRole entity);
}