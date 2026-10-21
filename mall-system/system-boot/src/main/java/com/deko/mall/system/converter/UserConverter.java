package com.deko.mall.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.system.model.bo.UserBO;
import com.deko.mall.system.model.bo.UserFormBO;
import com.deko.mall.system.model.bo.UserProfileBO;
import com.deko.mall.system.model.entity.SysUser;
import com.deko.mall.system.model.form.UserForm;
import com.deko.mall.system.model.vo.UserImportVO;
import com.deko.mall.system.model.vo.UserInfoVO;
import com.deko.mall.system.model.vo.UserPageVO;
import com.deko.mall.system.model.vo.UserProfileVO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 用户对象转换器
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mappings({
            @Mapping(target = "genderLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getGender(), com.deko.mall.common.enums.GenderEnum.class))"),
            @Mapping(target = "systemTypeLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getSystemType(), com.deko.mall.common.enums.SystemTypeEnum.class))")
    })
    UserPageVO bo2Vo(UserBO bo);

    Page<UserPageVO> bo2Vo(Page<UserBO> bo);

    UserForm bo2Form(UserFormBO bo);

    UserForm entity2Form(SysUser entity);

    @InheritInverseConfiguration(name = "entity2Form")
    SysUser form2Entity(UserForm entity);

    @Mappings({
            @Mapping(target = "userId", source = "id")
    })
    UserInfoVO entity2UserInfoVo(SysUser entity);

    SysUser importVo2Entity(UserImportVO vo);

    @Mappings({
            @Mapping(target = "genderLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(bo.getGender(), com.deko.mall.common.enums.GenderEnum.class))")
    })
    UserProfileVO userProfileBo2Vo(UserProfileBO bo);
}
