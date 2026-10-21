package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商分页查询对象
 */
@Data
@Schema(description = "供应商分页查询对象")
@EqualsAndHashCode(callSuper = true)
public class SupplierPageQuery extends BasePageQuery {

    @Schema(description = "关键词: 登录账号|供应商名称|供应商编码|联系电话")
    private String keywords;


    @Schema(description = "审核状态(0-待审核,1-审核通过,2-审核拒绝)")
    private Integer auditStatus;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;
}
