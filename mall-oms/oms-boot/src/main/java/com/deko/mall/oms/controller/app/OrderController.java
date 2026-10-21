package com.deko.mall.oms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pay.api.client.PayFeignClient;
import com.deko.mall.pay.api.dto.PaymentRequest;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.oms.model.form.OrderAfterSaleForm;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.model.form.ReturnLogisticsForm;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.query.OrderPageQuery;
import com.deko.mall.oms.model.vo.AfterSalePageVO;
import com.deko.mall.oms.model.vo.OrderConfirmVO;
import com.deko.mall.oms.model.vo.OrderPageVO;
import com.deko.mall.oms.model.vo.OrderSubmitResultVO;
import com.deko.mall.oms.service.order.OrderFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * APP端订单管理控制器
 * 提供移动端用户订单相关的接口，包括：
 * 1. 订单查询和管理（列表、详情、状态查询）
 * 2. 订单流程控制（确认、提交、取消、删除）
 * 3. 订单库存管理
 */
@Slf4j
@Tag(name = "APP-订单接口", description = "提供APP端订单管理相关接口")
@RestController
@RequestMapping("/app-api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacadeService orderFacadeService;
    private final PayFeignClient payFeignClient;

    @Operation(summary = "订单分页列表", description = "获取当前用户的订单列表，支持分页和条件查询")
    @GetMapping
    public PageResult<OrderPageVO> getOrderPage(
            @Parameter(description = "查询参数，支持按订单状态、时间等条件筛选") OrderPageQuery queryParams) {
        queryParams.setMemberId(SecurityUtils.getMemberId());
        IPage<OrderPageVO> result = orderFacadeService.getOrderPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "订单详情信息", description = "根据订单编号获取订单详细信息")
    @GetMapping("/info/{orderNo}")
    public Result<OrderPageVO> getOrderInfo(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        OrderPageVO result = orderFacadeService.getOrderPageVO(orderNo);
        return Result.success(result);
    }

    @Operation(summary = "订单状态查询", description = "获取指定订单的当前状态")
    @GetMapping("/info-status/{orderNo}")
    public Result<Integer> getOrderStatus(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        OrderPageVO result = orderFacadeService.getOrderPageVO(orderNo);
        if (null == result) {
            return Result.failed();
        }
        return Result.success(result.getStatus());
    }

    @Operation(summary = "订单确认", description = "进入订单确认页面有两个入口：1.立即购买；2.购物车结算")
    @PostMapping("/confirm")
    public Result<OrderConfirmVO> confirmOrder(
            @Parameter(description = "商品SKU ID，立即购买时必填") @RequestParam(required = false) Long skuId,
            @Parameter(description = "购买数量，立即购买时必填") @RequestParam(required = false) Integer count,
            @Parameter(description = "渠道编码（可选）") @RequestParam(required = false) String channelCode,
            @Parameter(description = "收货地址ID") @RequestParam(required = false) Long addressId) {
        OrderConfirmVO result = orderFacadeService.confirmOrder(skuId, count, channelCode, addressId);
        return Result.success(result);
    }

    @Operation(summary = "订单提交", description = "提交订单信息，创建新的订单")
    @PostMapping("/submit")
    public Result<OrderSubmitResultVO> submitOrder(
            @Parameter(description = "订单提交表单，包含收货地址、支付方式等信息")
            @Validated @RequestBody OrderSubmitForm submitForm) {
        OrderSubmitResultVO result = orderFacadeService.submitOrder(submitForm);

        if (!result.getNeedPay()) {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentNo(result.getPaymentNo());
            paymentRequest.setPayChannel(PayChannelEnum.FULL_DEDUCTION.getValue());
            // 支付单已在 submitOrder 中由 pay service 创建，这里只发起全额抵扣
            payFeignClient.pay(paymentRequest);
        }

        return Result.success(result);
    }

    @Operation(summary = "订单删除", description = "删除指定订单，仅支持已完成或已取消的订单")
    @DeleteMapping("/{orderNo}")
    public Result<Boolean> deleteOrder(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        boolean deleted = orderFacadeService.deleteOrder(orderNo);
        return Result.judge(deleted);
    }

    @Operation(summary = "快速退款（已关闭）")
    @PostMapping("/{orderNo}/quick-refund")
    public Result<Boolean> quickRefund(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        return Result.failed("快速退款功能已关闭，请走正常售后流程");
    }

    @Operation(summary = "订单取消", description = "取消未支付的订单")
    @PostMapping("/cancel/{orderNo}")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable String orderNo) {
        boolean cancel = orderFacadeService.cancelOrder(orderNo);
        return Result.judge(cancel);
    }

    @Operation(summary = "确认收货", description = "用户确认收到商品，订单交易完成")
    @PostMapping("/{orderNo}/confirm-receipt")
    public Result confirmReceipt(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        boolean result = orderFacadeService.confirmOrder(orderNo);
        return Result.judge(result);
    }

    @Operation(summary = "申请售后", description = "用户提交订单售后申请，支持退款以及退货退款")
    @PostMapping("/aftersale")
    public Result<Long> applyAfterSale(@Validated @RequestBody OrderAfterSaleForm form) {
        log.info("用户申请订单退款，订单号：{}", form.getOrderNo());
        Long refundId = orderFacadeService.applyAfterSale(form);
        return Result.success(refundId);
    }

    @Operation(summary = "售后列表", description = "获取当前用户的售后申请列表")
    @GetMapping("/aftersale")
    public PageResult<AfterSalePageVO> getAfterSaleList(AfterSalePageQuery query) {
        // 设置当前用户ID
        query.setMemberId(SecurityUtils.getMemberId());
        Page<AfterSalePageVO> page = orderFacadeService.getAfterSalePage(query);
        return PageResult.success(page);
    }

    @Operation(summary = "售后详情", description = "获取售后申请详情")
    @GetMapping("/aftersale/{id}")
    public Result<AfterSalePageVO> getAfterSaleDetail(
            @Parameter(description = "售后ID") @PathVariable Long id) {
        AfterSalePageVO detail = orderFacadeService.getAfterSaleDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "取消售后", description = "取消售后申请")
    @PostMapping("/aftersale/{id}/cancel")
    public Result<Boolean> cancelAfterSale(
            @Parameter(description = "售后ID") @PathVariable Long id) {
        boolean result = orderFacadeService.cancelAfterSale(id);
        return Result.judge(result);
    }

    @Operation(summary = "填写退货物流", description = "退货售后场景下，用户填写退货物流信息")
    @PostMapping("/aftersale/delivery")
    public Result<Boolean> fillAfterSaleLogistics(@Validated @RequestBody ReturnLogisticsForm form) {
        log.info("用户提交退货物流信息: [售后ID: {}, 物流公司: {}, 物流公司编码: {}, 物流单号: {}]",
                form.getAfterSaleId(), form.getCarrierName(), form.getCarrierCode(), form.getTrackingNo());

        boolean result = orderFacadeService.returnGoods(form);
        return Result.judge(result);
    }

    @Operation(summary = "重算支付金额", description = "取消部分订单后重新计算支付单的有效金额")
    @PostMapping("/payment/{paymentNo}/recalculate")
    public Result<String> recalculatePayment(
            @Parameter(description = "支付单号") @PathVariable String paymentNo) {
        String newPaymentNo = orderFacadeService.recalculatePayment(paymentNo);
        return Result.success(newPaymentNo);
    }

    @Operation(summary = "为订单创建支付单", description = "为单个待付款订单创建独立支付单，返回新支付单号")
    @PostMapping("/{orderNo}/create-payment")
    public Result<String> createPayment(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        String paymentNo = orderFacadeService.createPaymentForOrder(orderNo);
        return Result.success(paymentNo);
    }
}
