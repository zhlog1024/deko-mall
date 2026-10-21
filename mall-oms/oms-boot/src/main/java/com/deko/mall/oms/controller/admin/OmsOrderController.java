package com.deko.mall.oms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.oms.model.form.BatchDeliverForm;
import com.deko.mall.oms.model.form.OfflineDeliveryForm;
import com.deko.mall.oms.model.form.OrderDeliveryForm;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.query.OrderPageQuery;
import com.deko.mall.oms.model.vo.AfterSalePageVO;
import com.deko.mall.oms.model.vo.BatchDeliverResultVO;
import com.deko.mall.oms.model.vo.OfflineDeliveryDetailVO;
import com.deko.mall.oms.model.vo.OmsOrderDetailVO;
import com.deko.mall.oms.model.vo.OmsOrderPageVO;
import com.deko.mall.oms.model.vo.OmsOrderPriceVO;
import com.deko.mall.oms.service.order.OrderFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Admin-订单控制层
 */
@Tag(name = "Admin-订单管理")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OmsOrderController {

    private final OrderFacadeService orderFacadeService;

    @Operation(summary = "订单分页列表")
    @GetMapping
    public PageResult<OmsOrderPageVO> getOrderPage(OrderPageQuery queryParams) {
        IPage<OmsOrderPageVO> page = orderFacadeService.getAdminOrderPage(queryParams);
        return PageResult.success(page);
    }

    @Operation(summary = "导出订单数据")
    @GetMapping("/export")
    public void exportOrders(OrderPageQuery queryParams, HttpServletResponse response) throws IOException {
        orderFacadeService.exportOrders(queryParams, response);
    }


    @Operation(summary = "订单金额")
    @GetMapping("/price/sum")
    public Result<OmsOrderPriceVO> getOrderPriceSum(OrderPageQuery queryParams) {
        return Result.success(orderFacadeService.getOrderPriceSum(queryParams));
    }


    @Operation(summary = "订单发货")
    @PostMapping("/delivery")
    public Result<Boolean> deliverOrder(@Validated @RequestBody OrderDeliveryForm deliveryVO) {
        boolean result = orderFacadeService.deliverOrder(
                deliveryVO.getOrderNo(),
                deliveryVO.getDeliveryCode(),
                deliveryVO.getDeliverySn()
        );
        return Result.success(result);
    }

    @Operation(summary = "批量发货")
    @PostMapping("/batch-delivery")
    public Result<BatchDeliverResultVO> batchDeliver(@Validated @RequestBody BatchDeliverForm form) {
        BatchDeliverResultVO result = orderFacadeService.batchDeliverOrders(form);
        return Result.success(result);
    }


    @Operation(summary = "售后列表", description = "获取所有售后申请列表")
    @GetMapping("/aftersales")
    public PageResult<AfterSalePageVO> getAfterSaleList(AfterSalePageQuery queryParams) {
        Page<AfterSalePageVO> page = orderFacadeService.getAfterSalePage(queryParams);
        return PageResult.success(page);
    }

    @Operation(summary = "售后详情", description = "获取售后申请详情")
    @GetMapping("/aftersales/{id}")
    public Result<AfterSalePageVO> getAfterSaleDetail(
            @Parameter(description = "售后ID") @PathVariable Long id) {
        AfterSalePageVO detail = orderFacadeService.getAfterSaleDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "审核售后", description = "审核用户的售后申请")
    @PostMapping("/aftersales/{id}/audit")
    public Result<Boolean> auditAfterSale(
            @Parameter(description = "售后ID") @PathVariable Long id,
            @Parameter(description = "审核结果，true为通过，false为拒绝") @RequestParam Boolean approve,
            @Parameter(description = "审核备注") @RequestParam(required = false) String remark) {

        String adminUsername = SecurityUtils.getUsername();
        orderFacadeService.auditAfterSale(id, approve, remark, adminUsername);
        return Result.success(true);
    }

    @Operation(summary = "收货确认", description = "确认收到退货或拒绝收货")
    @PostMapping("/aftersales/{id}/receive")
    public Result<Boolean> confirmReceive(
            @Parameter(description = "售后ID") @PathVariable Long id,
            @Parameter(description = "确认结果，true为确认收货，false为拒绝收货") @RequestParam(defaultValue = "true") Boolean approved,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {

        String adminUsername = SecurityUtils.getUsername();
        boolean result = orderFacadeService.confirmReceipt(id, adminUsername, remark, approved);
        return Result.judge(result);
    }

    @Operation(summary = "执行退款", description = "执行退款操作")
    @PostMapping("/aftersales/{id}/refund")
    public Result<Boolean> executeAfterSale(
            @Parameter(description = "售后ID") @PathVariable Long id) {
        boolean result = orderFacadeService.processAfterSaleRefund(id);
        return Result.judge(result);
    }


    @Operation(summary = "导出售后单数据", description = "导出售后单列表数据")
    @GetMapping("/aftersales/export")
    public void exportAfterSales(AfterSalePageQuery queryParams, HttpServletResponse response) throws IOException {
        orderFacadeService.exportAfterSales(queryParams, response);
    }

    @Operation(summary = "订单详情", description = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OmsOrderDetailVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        OmsOrderDetailVO detail = orderFacadeService.getOrderDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "线下发货", description = "运营提交线下发货")
    @PostMapping("/offline-delivery")
    public Result<Void> submitOfflineDelivery(@Validated @RequestBody OfflineDeliveryForm form) {
        orderFacadeService.submitOfflineDelivery(form);
        return Result.success();
    }

    @Operation(summary = "线下发货详情", description = "获取线下发货申请详情")
    @GetMapping("/{id}/offline-delivery")
    public Result<OfflineDeliveryDetailVO> getOfflineDeliveryDetail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        OfflineDeliveryDetailVO detail = orderFacadeService.getOfflineDeliveryDetail(id);
        return Result.success(detail);
    }

}
