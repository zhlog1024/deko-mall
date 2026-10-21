package com.deko.mall.oms.model.form;

import com.deko.mall.oms.enums.OrderSourceEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单提交表单对象
 */
@Data
@ToString
public class OrderSubmitForm {

    @Schema(description = "订单确认页面签发的令牌(防止重复提交)")
    @NotBlank(message = "订单令牌不能为空")
    private String orderToken;

    @Schema(description = "订单来源")
    @NotNull(message = "订单来源不能为空")
    private OrderSourceEnum orderSource;

    @Schema(description = "订单商品明细")
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItem> orderItems;

    @Schema(description = "应付金额(单位：元)")
    @NotNull(message = "应付金额不能为空")
    private BigDecimal paymentAmount;

    @Schema(description = "收获地址")
    @NotNull(message = "收货地址不能为空")
    private ShippingAddress shippingAddress;

    @Schema(description = "订单备注")
    @Size(max = 500, message = "订单备注长度不能超过500")
    private String remark;

    /**
     * 分享用户ID
     */
    @Schema(description = "分享用户ID")
    private Long shareUserId;

    @Schema(description = "使用的积分数量")
    private BigDecimal usePointsAmount;

    @Schema(description = "积分抵扣金额(单位：元)")
    private BigDecimal pointsDeductionAmount;

    /**
     * 订单类型
     */
    @Schema(description = "订单类型：NORMAL-普通订单")
    private String orderType;

    /**
     * 购买渠道编码
     */
    @Schema(description = "购买渠道编码：NORMAL-普通购买")
    private String channelCode;

    private String orderScene;

    @Schema(description = "收获地址")
    @Data
    public static class ShippingAddress {

        private Long id;

        @Schema(description = "收货人姓名")
        private String consigneeName;

        @Schema(description = "收货人手机号")
        private String consigneeMobile;

        @Schema(description = "省份")
        private String province;

        @Schema(description = "城市")
        private String city;

        @Schema(description = "区域")
        private String district;

        @Schema(description = "详细地址")
        private String detailAddress;
    }

    @Schema(description = "订单商品")
    @Data
    public static class OrderItem {

        @Schema(description = "SKU ID")
        private Long skuId;

        @Schema(description = "供应商ID")
        private Long supplierId;

        @Schema(description = "商品数量")
        private Integer quantity;

        @Schema(description = "商品单价(单位：元)")
        private BigDecimal price;

        @Schema(description = "运费模板ID")
        private Long freightTemplateId;

        @Schema(description = "禁售区域ID")
        private Long forbiddenAreaId;

        @Schema(description = "运费金额(单位：元)")
        private BigDecimal freightAmount = BigDecimal.ZERO;

        @Schema(description = "SKU 编号")
        private String skuSn;

        @Schema(description = "SKU 名称")
        private String skuName;

        @Schema(description = "商品图片URL")
        private String picUrl;

        @Schema(description = "商品零售价格(单位:元)")
        private BigDecimal retailPrice;
        @Schema(description = "商品供货价格(单位:元)")
        private BigDecimal supplyPrice;

        @Schema(description = "商品名称")
        private String spuName;
    }

}
