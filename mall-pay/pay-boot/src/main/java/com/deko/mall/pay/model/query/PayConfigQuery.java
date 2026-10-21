package com.deko.mall.pay.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单分页查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付配置分页查询对象")
@Data
public class PayConfigQuery extends BasePageQuery {

    private String payChannel;

    private Boolean enabled;

}
