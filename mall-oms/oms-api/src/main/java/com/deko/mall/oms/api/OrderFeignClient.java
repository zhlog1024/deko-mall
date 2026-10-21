package com.deko.mall.oms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.oms.model.dto.OrderDTO;
import com.deko.mall.oms.model.dto.OrderItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 订单服务Feign客户端接口
 */
@FeignClient(value = "mall-oms", contextId = "order")
public interface OrderFeignClient {

    /**
     * 根据订单编号获取订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @GetMapping("/feign-api/orders/no/{orderNo}")
    Result<OrderDTO> getOrderByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 根据会员ID获取订单列表
     *
     * @param memberId 会员ID
     * @return 订单列表
     */
    @GetMapping("/feign-api/orders/member/{memberId}")
    Result<List<OrderDTO>> getOrdersByMemberId(@PathVariable("memberId") Long memberId);

    /**
     * 根据支付单号查询订单列表。
     *
     * @param paymentNo 支付单号
     * @return 订单列表
     */
    @GetMapping("/feign-api/orders/payment/{paymentNo}")
    Result<List<OrderDTO>> getOrdersByPaymentNo(@PathVariable("paymentNo") String paymentNo);

    /**
     * 根据订单编号获取订单项列表
     *
     * @param orderNo 订单编号
     * @return 订单项列表
     */
    @GetMapping("/feign-api/orders/no/{orderNo}/items")
    Result<List<OrderItemDTO>> getOrderItemsByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 根据订单编号更新订单状态
     *
     * @param orderNo 订单编号
     * @param status  订单状态
     * @return 更新结果
     */
    @PutMapping("/feign-api/orders/no/{orderNo}/status")
    Result<Boolean> updateOrderStatusByOrderNo(@PathVariable("orderNo") String orderNo, @RequestParam("status") String status);

    /**
     * 根据订单编号确认订单完成
     *
     * @param orderNo 订单编号
     * @return 确认结果
     */
    @PutMapping("/feign-api/orders/no/{orderNo}/complete")
    Result<Boolean> completeOrderByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 根据订单编号确认订单发货
     *
     * @param orderNo         订单编号
     * @param deliveryCode    物流单号
     * @param deliveryCompany 物流公司
     * @return 发货结果
     */
    @PutMapping("/feign-api/orders/no/{orderNo}/deliver")
    Result<Boolean> deliverOrderByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam("deliveryCode") String deliveryCode,
            @RequestParam("deliveryCompany") String deliveryCompany
    );

    /**
     * 根据订单编号取消订单
     *
     * @param orderNo 订单编号
     * @param reason  取消原因
     * @return 取消结果
     */
    @PutMapping("/feign-api/orders/no/{orderNo}/cancel")
    Result<Boolean> cancelOrderByOrderNo(@PathVariable("orderNo") String orderNo, @RequestParam("reason") String reason);
}
