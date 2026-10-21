package com.deko.mall.ums.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WxAuthDTO {

    private String code;

    private String encryptedData;


    private String iv;

}
