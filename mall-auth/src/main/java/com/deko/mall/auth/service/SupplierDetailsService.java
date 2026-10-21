package com.deko.mall.auth.service;

import com.deko.mall.auth.config.AuthResultCode;
import com.deko.mall.auth.model.SysUserDetails;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.api.SupplierFeignClient;
import com.deko.mall.pms.model.dto.SupplierDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 商城会员用户认证服务
 */
@Service
@RequiredArgsConstructor
public class SupplierDetailsService {

    private final SupplierFeignClient supplierFeignClient;

    public UserDetails loadSupplierByUserId(SysUserDetails userDetails, Long userId) {
        Result<SupplierDTO> supplierDTOResult = supplierFeignClient.getSupplierByUserId(userId);
        SupplierDTO supplier = supplierDTOResult.getData();
        if (supplier == null) {
            throw new BizException(AuthResultCode.SUPPLIER_NOT_FOUND);
        }
        userDetails.setSupplierId(supplier.getId());
        return userDetails;
    }


}
