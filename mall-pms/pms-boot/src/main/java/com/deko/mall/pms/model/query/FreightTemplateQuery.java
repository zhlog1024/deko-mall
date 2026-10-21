package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运费模板查询对象
 */
@Data
@Schema(description = "运费模板查询对象")
@EqualsAndHashCode(callSuper = true)
public class FreightTemplateQuery extends BasePageQuery {

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "计费方式(0-按件数,1-按重量)")
    private String chargeType;

    @Schema(description = "是否包邮(0-否,1-是)")
    private Integer isFreeShipping;

    @Schema(description = "是否默认(0-否,1-是)")
    private Integer isDefault;

    @Schema(description = "供应商ID")
    private Long supplierId;
}
