package com.deko.mall.oms.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderOfflineDelivery;
import com.deko.mall.oms.model.form.BatchDeliverForm;
import com.deko.mall.oms.model.form.OrderAfterSaleForm;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.model.form.OfflineDeliveryForm;
import com.deko.mall.oms.model.form.ReturnLogisticsForm;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.query.OrderPageQuery;
import com.deko.mall.oms.model.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * 订单业务服务接口
 * 提供订单全生命周期的管理功能，包括：
 * 1. 订单创建流程（确认、提交）
 * 2. 订单支付流程（支付、退款回调）
 * 3. 订单状态管理（取消、关闭、完成）
 * 4. 订单查询服务
 */
public interface OrderFacadeService extends IService<OmsOrder> {

    /**
     * 订单确认
     * 进入订单创建页面的预处理，有两个入口：
     * 1. 立即购买：需要指定商品SKU和数量
     * 2. 购物车结算：从购物车中获取选中的商品
     *
     * @param skuId       商品SKU ID，立即购买时必填
     * @param count       购买数量，立即购买时必填
     * @param channelCode 渠道编码（可选）
     * @return 订单确认页面所需数据，包括：商品信息、收货地址、防重Token等
     */
    OrderConfirmVO confirmOrder(Long skuId, Integer count, String channelCode, Long addressId);

    /**
     * 提交订单
     * 创建新的订单记录，包括：
     * 1. 生成订单信息
     * 2. 扣减商品库存
     * 3. 清理购物车（购物车结算时）
     *
     * @param orderSubmitForm 订单提交表单，包含收货地址、支付方式等信息
     * @return 订单提交结果，包含订单号、是否需要支付、应付金额等信息
     */
    OrderSubmitResultVO submitOrder(OrderSubmitForm orderSubmitForm);

    /**
     * 删除订单
     * 仅支持删除已完成或已取消的订单
     *
     * @param orderNo 订单编号
     * @return 删除结果
     */
    boolean deleteOrder(String orderNo);

    /**
     * 订单分页查询
     * 支持按订单状态、时间等条件筛选
     *
     * @param queryParams 查询参数
     * @return 分页订单列表
     */
    IPage<OrderPageVO> getOrderPage(OrderPageQuery queryParams);

    /**
     * 取消订单
     * 仅支持取消未支付的订单，会释放已锁定的库存
     *
     * @param orderNo 订单编号
     * @return 取消结果
     */
    boolean cancelOrder(String orderNo);


    /**
     * 获取订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情VO
     */
    OrderPageVO getOrderPageVO(String orderNo);

    /**
     * 根据订单编号获取订单信息
     *
     * @param orderNo 订单编号
     * @return 订单实体
     */
    OmsOrder getOrderByOrderNo(String orderNo);

    /**
     * 处理支付回调
     * 处理支付平台的异步通知，更新订单状态
     *
     * @param order 订单信息
     */
    void paymentResult(OmsOrder order);

    /**
     * 处理退款回调
     * 处理支付平台的退款结果通知，更新订单状态
     *
     * @param order 订单信息
     */
    void refundCallbackOrder(OmsOrder order);

    /**
     * 确认收货
     * 用户确认收到商品，将订单状态更新为已完成
     *
     * @param orderNo 订单编号
     * @return 操作结果
     */
    boolean confirmOrder(String orderNo);

    /**
     * 确认收货（订单级/运单级双路径）
     *
     * @param orderNo    订单号
     * @param trackingNo 物流单号（非空=仅确认该发货单关联商品项；空=整单确认）
     * @return 是否确认成功
     */
    boolean confirmOrder(String orderNo, String trackingNo);

    /**
     * 后台订单分页列表
     *
     * @param queryParams {@link OrderPageQuery}
     */
    IPage<OmsOrderPageVO> getAdminOrderPage(OrderPageQuery queryParams);


    OmsOrderPriceVO getOrderPriceSum(OrderPageQuery queryParams);

    boolean deliverOrder(@NotNull(message = "订单编号不能为空") String orderNo, @NotBlank(message = "物流公司不能为空") String deliveryCode, @NotBlank(message = "物流单号不能为空") String deliverySn);

    /**
     * 订单发货（多供应商订单按供应商各自发货）
     *
     * @param orderNo      订单号
     * @param supplierId   发货供应商ID
     * @param deliveryCode 物流公司编码
     * @param deliverySn   物流单号
     * @return 是否发货成功
     */
    boolean deliverOrder(@NotNull(message = "订单编号不能为空") String orderNo, Long supplierId, @NotBlank(message = "物流公司不能为空") String deliveryCode, @NotBlank(message = "物流单号不能为空") String deliverySn);

    /**
     * 批量发货
     *
     * @param form 批量发货表单
     * @return 批量发货结果
     */
    BatchDeliverResultVO batchDeliverOrders(BatchDeliverForm form);


    /**
     * 发起订单售后
     */
    Long applyAfterSale(OrderAfterSaleForm form);

    /**
     * 获取售后列表
     *
     * @param query 查询条件
     * @return 售后分页列表
     */
    Page<AfterSalePageVO> getAfterSalePage(AfterSalePageQuery query);

    /**
     * 获取售后详情
     *
     * @param id 售后ID
     * @return 售后详情
     */
    AfterSalePageVO getAfterSaleDetail(Long id);

    /**
     * 取消售后
     *
     * @param id 售后ID
     * @return 操作结果
     */
    boolean cancelAfterSale(Long id);

    /**
     * 填写退货物流信息
     */
    boolean returnGoods(ReturnLogisticsForm returnLogisticsForm);

    /**
     * 审核售后申请
     *
     * @param id       售后申请ID
     * @param approve  是否通过
     * @param remark   审核备注
     * @param handleBy 处理人
     */
    void auditAfterSale(Long id, Boolean approve, String remark, String handleBy);

    /**
     * 确认收货
     *
     * @param id       售后ID
     * @param handleBy 处理人
     * @param remark   备注
     * @return 操作结果
     */
    boolean confirmReceipt(Long id, String handleBy, String remark, Boolean approved);

    /**
     * 导出订单数据
     *
     * @param queryParams 查询参数
     * @param response    响应对象
     */
    void exportOrders(OrderPageQuery queryParams, HttpServletResponse response) throws IOException;

    /**
     * 处理退款回调
     * 统一处理支付平台的退款结果通知，内部根据退款类型分发到不同的处理流程
     *
     * @param refundResultDTO 退款回调数据
     * @return 处理结果
     */
    boolean handleRefundCallback(RefundResultDTO refundResultDTO);

    /**
     * 导出售后单数据
     *
     * @param queryParams 查询参数
     * @param response    响应对象
     * @throws IOException IO异常
     */
    void exportAfterSales(AfterSalePageQuery queryParams, HttpServletResponse response) throws IOException;

    /**
     * 获取售后单统计数据
     *
     * @param queryParams 查询参数
     * @return 售后单统计信息
     */
    AfterSaleStatisticsVO getAfterSaleStatistics(AfterSalePageQuery queryParams);


    /**
     * 订单快速退款
     * 订单状态必须为待付款，且未支付
     * 退款金额必须小于订单金额
     *
     * @param orderNo 订单编号
     * @return 退款结果
     */
    boolean processQuickRefund(String orderNo);




    /**
     * 售后退款
     */
    boolean processAfterSaleRefund(Long afterSaleId);

    /**
     * 获取订单详情（管理端/商家端）
     *
     * @param orderId 订单ID
     * @return 订单详情VO
     */
    OmsOrderDetailVO getOrderDetail(Long orderId);

    /**
     * 运营提交线下发货（系统自动审核通过）
     *
     * @param form 线下发货表单
     */
    void submitOfflineDelivery(OfflineDeliveryForm form);

    /**
     * 获取线下发货申请详情
     *
     * @param orderId 订单ID
     * @return 线下发货申请详情
     */
    OfflineDeliveryDetailVO getOfflineDeliveryDetail(Long orderId);

    /**
     * 重算支付金额（取消部分订单后更新支付单金额）
     */
    String recalculatePayment(String paymentNo);

    /**
     * 为单个订单创建独立支付单（用于待付款列表重付场景）
     */
    String createPaymentForOrder(String orderNo);

}
