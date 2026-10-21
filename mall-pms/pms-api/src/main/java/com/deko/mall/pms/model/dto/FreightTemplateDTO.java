package com.deko.mall.pms.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运费模板数据传输对象
 */
@Data
public class FreightTemplateDTO {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 供应商ID
     */
    private Long supplierId;
    /** 国家区域ID */
    private Long countryId;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 计费方式(0-按件数,1-按重量)
     */
    private String chargeType;
    /**
     * 是否包邮(0-否,1-是)
     */
    private Integer isFreeShipping;
    /**
     * 是否默认(0-否,1-是)
     */
    private Integer isDefault;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 运费规则列表
     */
    private List<FreightRuleDTO> rules;
}
