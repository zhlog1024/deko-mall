package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Schema(description = "运费规则视图对象")
public class FreightRuleVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "区域编码集合,多个以逗号分隔")
    private String regionCodes;

    @Schema(description = "区域名称集合,多个以逗号分隔")
    private String regionNames;

    @Schema(description = "首件数量(个)或首重(kg*100)")
    private Integer firstItem;


    @Schema(description = "首费(元)")
    private java.math.BigDecimal firstFee;

    @NotNull(message = "续件/续重不能为空")
    @Schema(description = "续件数量(个)或续重(kg*100)")
    private Integer continueItem;

    @NotNull(message = "续费不能为空")
    @Schema(description = "续费(元)")
    private java.math.BigDecimal continueFee;

    @Schema(description = "是否为默认规则(0-否,1-是)")
    private Integer isDefault;
}
