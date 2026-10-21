package com.deko.mall.ums.convert;

import com.deko.mall.ums.dto.MemberAddressDTO;
import com.deko.mall.ums.model.entity.UmsAddress;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 会员地址对象转换器
 */
@Mapper(componentModel = "spring")
public interface AddressConvert {

    List<MemberAddressDTO> entity2Dto(List<UmsAddress> entities);
}
