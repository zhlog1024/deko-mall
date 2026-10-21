package com.deko.mall.ums.convert;

import com.deko.mall.ums.dto.MemberAuthDTO;
import com.deko.mall.ums.dto.MemberInfoDTO;
import com.deko.mall.ums.dto.MemberRegisterDTO;
import com.deko.mall.ums.model.entity.UmsMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 会员对象转换器
 */
@Mapper(componentModel = "spring")
public interface MemberConvert {
    @Mappings({
            @Mapping(target = "username", source = "openid")
    })
    MemberAuthDTO entity2OpenidAuthDTO(UmsMember entity);

    @Mappings({
            @Mapping(target = "username", source = "mobile")
    })
    MemberAuthDTO entity2MobileAuthDTO(UmsMember entity);


    UmsMember dto2Entity(MemberRegisterDTO memberRegisterDTO);

    MemberInfoDTO entity2InfoDto(UmsMember user);
}
