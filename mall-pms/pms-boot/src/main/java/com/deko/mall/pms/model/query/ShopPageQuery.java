package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺分页查询对象
 */
@Schema(description = "店铺分页查询对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopPageQuery extends BasePageQuery {

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "关键字(店铺名称模糊匹配)")
    private String keywords;

    @Schema(description = "归属类型(SUPPLIER-供应商, MEMBER-会员)")
    private String ownerType;

    @Schema(description = "店铺状态(0-关闭,1-开启)")
    private Integer status;
}
