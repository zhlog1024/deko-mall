package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费模板表单对象
 */
@Data
@Schema(description = "运费模板表单对象")
public class FreightTemplateForm {

    @Schema(description = "供应商ID")
    private Long supplierId;
    @Schema(description = "国家区域ID")
    private Long countryId;
    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", required = true)
    private String templateName;
    @NotNull(message = "计费方式不能为空")
    @Schema(description = "计费方式(BY_WEIGHT-按重量,BY_PIECE-按件数)", required = true)
    private String chargeType;
    @NotNull(message = "是否包邮不能为空")
    @Schema(description = "是否包邮(0-否,1-是)", required = true)
    private Integer isFreeShipping;
    @Schema(description = "是否默认(0-否,1-是)")
    private Integer isDefault;
    @Valid
    @Schema(description = "运费规则列表", required = true)
    private List<FreightRuleForm> rules;
    @Data
    @Schema(description = "运费规则表单对象")
    public static class FreightRuleForm {

        @Schema(description = "主键ID")
        private Long id;
        @NotBlank(message = "区域编码不能为空")
        @Schema(description = "区域编码集合,多个以逗号分隔", required = true)
        private String regionCodes;
        @NotBlank(message = "区域名称不能为空")
        @Schema(description = "区域名称集合,多个以逗号分隔", required = true)
        private String regionNames;
        @NotNull(message = "首件/首重不能为空")
        @Schema(description = "首件数量(个)或首重(kg*100)", required = true)
        private Integer firstItem;
        @NotNull(message = "首费不能为空")
        @Schema(description = "首费(元)", required = true)
        private BigDecimal firstFee;

        @NotNull(message = "续件/续重不能为空")
        @Schema(description = "续件数量(个)或续重(kg*100)", required = true)
        private Integer continueItem;
        @NotNull(message = "续费不能为空")
        @Schema(description = "续费(元)", required = true)
        private BigDecimal continueFee;

        @Schema(description = "是否为默认规则(0-否,1-是)")
        private Integer isDefault;
    }
}
