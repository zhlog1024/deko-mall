package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 禁售区域查询对象
 */
@Data
@Schema(description = "禁售区域查询对象")
@EqualsAndHashCode(callSuper = true)
public class ForbiddenAreaQuery extends BasePageQuery {

    @Schema(description = "区域名称")
    private String regionNames;
    @Schema(description = "区域编码")
    private String regionCodes;

    @Schema(description = "供应商ID")
    private Long supplierId;
}
