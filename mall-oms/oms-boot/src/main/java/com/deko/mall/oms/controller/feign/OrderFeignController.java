package com.deko.mall.oms.controller.feign;

import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.api.OrderFeignClient;
import com.deko.mall.oms.constant.OrderConstants;
import com.deko.mall.oms.converter.OrderConverter;
import com.deko.mall.oms.enums.OrderSourceEnum;
import com.deko.mall.oms.model.dto.OrderDTO;
import com.deko.mall.oms.model.dto.OrderItemDTO;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.model.vo.OrderSubmitResultVO;
import com.deko.mall.oms.service.order.OrderFacadeService;
import com.deko.mall.oms.service.order.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务Feign接口实现
 * 供其他微服务调用订单服务的数据查询和确认功能
 */
@Slf4j
@RestController
@RequestMapping("/feign-api/orders")
@RequiredArgsConstructor
public class OrderFeignController implements OrderFeignClient {

    private final OrderFacadeService orderFacadeService;
    private final OrderItemService orderItemService;
    private final OrderConverter orderConverter;

    /**
     * 根据订单编号获取订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @Override
    @GetMapping("/no/{orderNo}")
    public Result<OrderDTO> getOrderByOrderNo(@PathVariable("orderNo") String orderNo) {
        log.info("【Feign调用】根据订单编号获取订单详情：{}", orderNo);
        try {
            OmsOrder order = orderFacadeService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.failed("订单不存在");
            }
            OrderDTO orderDTO = orderConverter.toOrderDTO(order);
            // 获取订单项
            List<OmsOrderItem> orderItems = orderItemService.lambdaQuery()
                    .eq(OmsOrderItem::getOrderId, order.getId())
                    .list();
            if (orderItems != null && !orderItems.isEmpty()) {
                List<OrderItemDTO> orderItemDTOs = orderConverter.toOrderItemDTOList(orderItems);
                orderDTO.setOrderItems(orderItemDTOs);
            }
            return Result.success(orderDTO);
        } catch (Exception e) {
            log.error("【Feign调用】根据订单编号获取订单详情异常", e);
            return Result.failed("获取订单详情失败：" + e.getMessage());
        }
    }

    /**
     * 根据会员ID获取订单列表
     *
     * @param memberId 会员ID
     * @return 订单列表
     */
    @Override
    @GetMapping("/member/{memberId}")
    public Result<List<OrderDTO>> getOrdersByMemberId(@PathVariable("memberId") Long memberId) {
        log.info("【Feign调用】根据会员ID获取订单列表：{}", memberId);
        try {
            List<OmsOrder> orders = orderFacadeService.lambdaQuery()
                    .eq(OmsOrder::getMemberId, memberId)
                    .list();

            if (orders.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            // 使用converter转换为DTO对象
            List<OrderDTO> orderDTOList = orderConverter.toOrderDTOList(orders);

            // 为每个订单加载订单项
            for (int i = 0; i < orders.size(); i++) {
                OmsOrder order = orders.get(i);
                OrderDTO orderDTO = orderDTOList.get(i);

                List<OmsOrderItem> orderItems = orderItemService.lambdaQuery()
                        .eq(OmsOrderItem::getOrderId, order.getId())
                        .list();
                if (orderItems != null && !orderItems.isEmpty()) {
                    List<OrderItemDTO> orderItemDTOs = orderConverter.toOrderItemDTOList(orderItems);
                    orderDTO.setOrderItems(orderItemDTOs);
                }
            }

            return Result.success(orderDTOList);
        } catch (Exception e) {
            log.error("【Feign调用】根据会员ID获取订单列表异常", e);
            return Result.failed("获取订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据支付单号查询订单列表。
     *
     * @param paymentNo 支付单号
     * @return 订单列表
     */
    @Override
    @GetMapping("/payment/{paymentNo}")
    public Result<List<OrderDTO>> getOrdersByPaymentNo(@PathVariable("paymentNo") String paymentNo) {
        log.info("【Feign调用】根据支付单号获取订单列表：{}", paymentNo);
        try {
            List<OmsOrder> orders = orderFacadeService.lambdaQuery()
                    .eq(OmsOrder::getPaymentNo, paymentNo)
                    .list();
            if (orders.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            List<OrderDTO> orderDTOList = orderConverter.toOrderDTOList(orders);
            for (int i = 0; i < orders.size(); i++) {
                OmsOrder order = orders.get(i);
                OrderDTO orderDTO = orderDTOList.get(i);
                List<OmsOrderItem> orderItems = orderItemService.lambdaQuery()
                        .eq(OmsOrderItem::getOrderId, order.getId())
                        .list();
                if (orderItems != null && !orderItems.isEmpty()) {
                    orderDTO.setOrderItems(orderConverter.toOrderItemDTOList(orderItems));
                }
            }
            return Result.success(orderDTOList);
        } catch (Exception e) {
            log.error("【Feign调用】根据支付单号获取订单列表异常", e);
            return Result.failed("获取订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单编号获取订单项列表
     *
     * @param orderNo 订单编号
     * @return 订单项列表
     */
    @Override
    @GetMapping("/no/{orderNo}/items")
    public Result<List<OrderItemDTO>> getOrderItemsByOrderNo(@PathVariable("orderNo") String orderNo) {
        log.info("【Feign调用】根据订单编号获取订单项列表：{}", orderNo);
        try {
            // 先查询订单
            OmsOrder order = orderFacadeService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.failed("订单不存在");
            }

            // 查询订单项
            List<OmsOrderItem> orderItems = orderItemService.lambdaQuery()
                    .eq(OmsOrderItem::getOrderId, order.getId())
                    .list();

            if (orderItems.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            // 使用converter转换为DTO对象
            List<OrderItemDTO> orderItemDTOList = orderConverter.toOrderItemDTOList(orderItems);

            return Result.success(orderItemDTOList);
        } catch (Exception e) {
            log.error("【Feign调用】根据订单编号获取订单项列表异常", e);
            return Result.failed("获取订单项列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单编号更新订单状态
     *
     * @param orderNo 订单编号
     * @param status  订单状态
     * @return 更新结果
     */
    @Override
    @PutMapping("/no/{orderNo}/status")
    public Result<Boolean> updateOrderStatusByOrderNo(@PathVariable("orderNo") String orderNo, @RequestParam("status") String status) {
        log.info("【Feign调用】根据订单编号更新订单状态：订单编号={}, 状态={}", orderNo, status);
        try {
            OmsOrder order = orderFacadeService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.failed("订单不存在");
            }

            // 设置订单状态并更新
            order.setStatus(Integer.parseInt(status));
            boolean result = orderFacadeService.updateById(order);

            return Result.success(result);
        } catch (Exception e) {
            log.error("【Feign调用】根据订单编号更新订单状态异常", e);
            return Result.failed("更新订单状态失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单编号确认订单完成
     *
     * @param orderNo 订单编号
     * @return 确认结果
     */
    @Override
    @PutMapping("/no/{orderNo}/complete")
    public Result<Boolean> completeOrderByOrderNo(@PathVariable("orderNo") String orderNo) {
        log.info("【Feign调用】根据订单编号确认订单完成：{}", orderNo);
        try {
            boolean result = orderFacadeService.confirmOrder(orderNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("【Feign调用】根据订单编号确认订单完成异常", e);
            return Result.failed("确认订单完成失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单编号确认订单发货
     *
     * @param orderNo         订单编号
     * @param deliveryCode    物流单号
     * @param deliveryCompany 物流公司
     * @return 发货结果
     */
    @Override
    @PutMapping("/no/{orderNo}/deliver")
    public Result<Boolean> deliverOrderByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam("deliveryCode") String deliveryCode,
            @RequestParam("deliveryCompany") String deliveryCompany
    ) {
        log.info("【Feign调用】根据订单编号确认订单发货：订单编号={}, 物流单号={}, 物流公司={}", orderNo, deliveryCode, deliveryCompany);
        try {
            boolean result = orderFacadeService.deliverOrder(orderNo, deliveryCompany, deliveryCode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("【Feign调用】根据订单编号确认订单发货异常", e);
            return Result.failed("确认订单发货失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单编号取消订单
     *
     * @param orderNo 订单编号
     * @param reason  取消原因
     * @return 取消结果
     */
    @Override
    @PutMapping("/no/{orderNo}/cancel")
    public Result<Boolean> cancelOrderByOrderNo(@PathVariable("orderNo") String orderNo, @RequestParam("reason") String reason) {
        log.info("【Feign调用】根据订单编号取消订单：订单编号={}, 取消原因={}", orderNo, reason);
        try {
            OmsOrder order = orderFacadeService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.failed("订单不存在");
            }

            // 记录取消原因
            order.setRemark(reason);
            orderFacadeService.updateById(order);

            boolean result = orderFacadeService.cancelOrder(orderNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("【Feign调用】根据订单编号取消订单异常", e);
            return Result.failed("取消订单失败：" + e.getMessage());
        }
    }

}
