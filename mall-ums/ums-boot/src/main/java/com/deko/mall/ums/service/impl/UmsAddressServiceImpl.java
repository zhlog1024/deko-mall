package com.deko.mall.ums.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.constant.GlobalConstants;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.ums.dto.MemberAddressDTO;
import com.deko.mall.ums.mapper.UmsAddressMapper;
import com.deko.mall.ums.model.entity.UmsAddress;
import com.deko.mall.ums.model.form.AddressForm;
import com.deko.mall.ums.service.UmsAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 会员地址业务实现类
 */
@Service
public class UmsAddressServiceImpl extends ServiceImpl<UmsAddressMapper, UmsAddress> implements UmsAddressService {

    /**
     * 新增地址
     */
    @Override
    @Transactional
    public boolean addAddress(AddressForm addressForm) {
        Long memberId = SecurityUtils.getMemberId();

        UmsAddress umsAddress = new UmsAddress();
        BeanUtil.copyProperties(addressForm, umsAddress);
        umsAddress.setCompleteAddress(buildCompleteAddress(umsAddress));
        umsAddress.setMemberId(memberId);
        boolean result = this.save(umsAddress);
        if (result) {
            // 修改其他默认地址为非默认
            if (GlobalConstants.STATUS_YES.equals(addressForm.getDefaulted())) {
                this.update(new LambdaUpdateWrapper<UmsAddress>()
                        .eq(UmsAddress::getMemberId, memberId)
                        .eq(UmsAddress::getDefaulted, 1)
                        .ne(UmsAddress::getId, umsAddress.getId())
                        .set(UmsAddress::getDefaulted, 0)
                );
            }
        }
        return result;
    }

    /**
     * 修改地址
     */
    @Override
    public boolean updateAddress(AddressForm addressForm) {
        Long memberId = SecurityUtils.getMemberId();

        UmsAddress umsAddress = new UmsAddress();
        // 先复制表单属性，再生成完整地址和更新时间
        BeanUtil.copyProperties(addressForm, umsAddress);
        umsAddress.setCompleteAddress(buildCompleteAddress(umsAddress));
        umsAddress.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(umsAddress);

        if (result) {
            // 修改其他默认地址为非默认
            if (GlobalConstants.STATUS_YES.equals(addressForm.getDefaulted())) {
                this.update(new LambdaUpdateWrapper<UmsAddress>()
                        .eq(UmsAddress::getMemberId, memberId)
                        .eq(UmsAddress::getDefaulted, 1)
                        .ne(UmsAddress::getId, umsAddress.getId())
                        .set(UmsAddress::getDefaulted, 0)
                );
            }
        }
        return result;
    }

    /**
     * 获取当前登录会员的地址列表
     */
    @Override
    public List<MemberAddressDTO> listCurrentMemberAddresses() {
        Long memberId = SecurityUtils.getMemberId();
        List<UmsAddress> umsAddressList = this.list(new LambdaQueryWrapper<UmsAddress>()
                .eq(UmsAddress::getMemberId, memberId)
                .orderByDesc(UmsAddress::getDefaulted) // 默认地址排在首位
        );
        List<MemberAddressDTO> memberAddressList = Optional.ofNullable(umsAddressList).orElse(new ArrayList<>()).stream()
                .map(umsAddress -> {
                    MemberAddressDTO memberAddressDTO = new MemberAddressDTO();
                    BeanUtil.copyProperties(umsAddress, memberAddressDTO);
                    return memberAddressDTO;
                }).collect(Collectors.toList());
        return memberAddressList;
    }

    private String buildCompleteAddress(UmsAddress addr) {
        StringBuilder sb = new StringBuilder();
        // 非中国时加上国家名
        if (addr.getCountryName() != null && !"中国".equals(addr.getCountryName()) && !"China".equals(addr.getCountryName())) {
            sb.append(addr.getCountryName());
        }
        if (addr.getProvinceName() != null) sb.append(addr.getProvinceName());
        if (addr.getCityName() != null) sb.append(addr.getCityName());
        if (addr.getAreaName() != null) sb.append(addr.getAreaName());
        if (addr.getDetailAddress() != null) sb.append(addr.getDetailAddress());
        return sb.toString();
    }

    @Override
    public boolean deletedByIds(List<String> asList) {
        List<Long> collect = asList.stream().map(Long::valueOf).collect(Collectors.toList());
        if (this.removeByIds(collect)) {
            return true;
        }
        // 把 collect 中的值以id批量修改数据为删除状态，修改时间等
        boolean update = this.update(new LambdaUpdateWrapper<UmsAddress>()
                .in(UmsAddress::getId, collect)
                .set(UmsAddress::getDeleted, 1)
                .set(UmsAddress::getUpdateTime, LocalDateTime.now())
        );

        return update;
    }
}
