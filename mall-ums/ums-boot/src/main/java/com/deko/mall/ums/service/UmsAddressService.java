package com.deko.mall.ums.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.ums.dto.MemberAddressDTO;
import com.deko.mall.ums.model.entity.UmsAddress;
import com.deko.mall.ums.model.form.AddressForm;

import java.util.List;

/**
 * 会员地址业务接口
 */
public interface UmsAddressService extends IService<UmsAddress> {

    /**
     * 新增地址
     */
    boolean addAddress(AddressForm addressForm);

    /**
     * 修改地址
     */
    boolean updateAddress(AddressForm addressForm);

    /**
     * 获取当前登录会员的地址列表
     */
    List<MemberAddressDTO> listCurrentMemberAddresses();

    /**
     * 逻辑删除用户地址
     */
    boolean deletedByIds(List<String> asList);
}
