package com.deko.mall.sms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @desc: 优惠券分页查询对象
 */
@Schema(description = "优惠券分页查询对象")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponPageQuery extends BasePageQuery {

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "优惠券码")
    private String code;
}
