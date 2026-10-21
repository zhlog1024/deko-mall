package com.deko.mall.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 国家区号 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryCodeVO {

    /** ISO 3166-1 alpha-2 */
    private String isoCode;

    /** 中文名 */
    private String nameZh;

    /** 英文名 */
    private String nameEn;

    /** 电话区号(如+86) */
    private String phoneCode;

    /** 手机号长度(多值逗号分隔,如11或9,10) */
    private String phoneLength;

    /** 国旗emoji */
    private String flagEmoji;
}
