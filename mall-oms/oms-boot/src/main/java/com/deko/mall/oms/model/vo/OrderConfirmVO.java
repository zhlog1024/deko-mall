package com.deko.mall.oms.model.vo;

import com.deko.mall.ums.dto.MemberAddressDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * 订单确认响应对象
 */
@Schema(description = "订单确认响应对象")
@Data
public class OrderConfirmVO {

    /**
     * 订单防重提交令牌
     */
    @Schema(description = "订单token，提交订单时需要传回")
    private String orderToken;

    /**
     * 订单商品（已拆分，按店铺+商品类型分组）
     */
    @Schema(description = "默认收货地址（优先默认地址，否则第一个）")
    private MemberAddressDTO defaultAddress;

    @Schema(description = "拆分后的子订单列表")
    private List<SubOrder> orders;

    /**
     * 所有子订单应付总额（单位：元）
     */
    @Schema(description = "所有子订单应付总额(单位：元)")
    private BigDecimal totalPaymentAmount = BigDecimal.ZERO;

    /**
     * 子订单数量
     */
    @Schema(description = "子订单数量")
    private Integer subOrderCount = 0;


    /**
     * 子订单
     */
    @Data
    @Schema(description = "子订单（按店铺分组）")
    public static class SubOrder {

        @Schema(description = "店铺ID")
        private Long shopId;

        @Schema(description = "店铺名称")
        private String shopName;

        @Schema(description = "商品类型（NORMAL）")
        private String spuType;

        @Schema(description = "订单商品列表")
        private List<OrderItemVO> orderItems;

        @Schema(description = "运费金额(单位：元)")
        private BigDecimal freightAmount = BigDecimal.ZERO;

        @Schema(description = "最终需支付金额（单位：元）")
        private BigDecimal paymentAmount = BigDecimal.ZERO;

        @Schema(description = "抵扣到商品上的积分金额（单位：元）")
        private BigDecimal productDeductionAmount = BigDecimal.ZERO;

        @Schema(description = "抵扣到运费上的积分金额（单位：元）")
        private BigDecimal freightDeductionAmount = BigDecimal.ZERO;

        @Schema(description = "积分抵扣金额（单位：元）")
        private BigDecimal pointsDeductionAmount = BigDecimal.ZERO;





        @Schema(description = "是否包含禁售区域商品")
        private Boolean hasForbiddenAreaItems = false;

        @Schema(description = "禁售商品名称列表")
        private List<String> forbiddenItemNames = new ArrayList<>();
    }
}
