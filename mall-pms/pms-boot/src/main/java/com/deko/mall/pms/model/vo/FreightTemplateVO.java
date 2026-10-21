package com.deko.mall.pms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运费模板视图对象
 */
@Data
@Schema(description = "运费模板视图对象")
public class FreightTemplateVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "供应商ID")
    private Long supplierId;
    @Schema(description = "国家区域ID")
    private Long countryId;
    @Schema(description = "模板名称")
    private String templateName;
    @Schema(description = "计费方式(0-按件数,1-按重量)")
    private String chargeType;
    @Schema(description = "计费方式描述")
    private String chargeTypeLabel;
    @Schema(description = "是否包邮(0-否,1-是)")
    private Integer isFreeShipping;
    @Schema(description = "是否包邮描述")
    private String isFreeShippingLabel;
    @Schema(description = "是否默认(0-否,1-是)")
    private Integer isDefault;
    @Schema(description = "是否默认描述")
    private String isDefaultLabel;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "运费规则列表")
    private List<FreightRuleVO> rules;
}
