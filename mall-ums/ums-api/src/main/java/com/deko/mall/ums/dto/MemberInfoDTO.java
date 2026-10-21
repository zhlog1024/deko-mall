package com.deko.mall.ums.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员传输对象。
 */
@Data
public class MemberInfoDTO {

    private String nickName;

    private String mobile;

    private String avatarUrl;

    private Long userId;

    private String roleCode;

    /**
     * 用户可用的支付抵扣金额(元)
     */
    private BigDecimal paymentDiscountAmount;

}
