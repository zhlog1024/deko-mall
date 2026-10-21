package com.deko.mall.pms.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运费规则数据传输对象
 */
@Data
public class FreightRuleDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 区域编码集合,多个以逗号分隔
     */
    private String regionCodes;

    /**
     * 区域名称集合,多个以逗号分隔
     */
    private String regionNames;

    /**
     * 首件数量(个)或首重(kg*100)
     */
    private Integer firstItem;

    /**
     * 首费(单位：元)
     */
    private BigDecimal firstFee;

    /**
     * 续件数量(个)或续重(kg*100)
     */
    private Integer continueItem;

    /**
     * 续费(单位：元)
     */
    private BigDecimal continueFee;

    /**
     * 是否为默认规则(0-否,1-是)
     */
    private Integer isDefault;
}
