package com.deko.mall.ums.service;

import com.deko.mall.ums.dto.AddressParseReq;
import com.deko.mall.ums.dto.AddressParseRes;

/**
 * 智能地址识别服务
 */
public interface AddressParseService {

    /**
     * 解析原始地址文本，提取姓名、手机号、省市区、详细地址
     */
    AddressParseRes parse(AddressParseReq req);
}
