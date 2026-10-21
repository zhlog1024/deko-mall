package com.deko.mall.oms.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.common.rabbitmq.service.MessageProcessService;
import com.deko.mall.common.rabbitmq.service.MessageSendService;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.utils.IdGenerator;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.config.OmsResultCode;
import com.deko.mall.oms.annotation.OrderLog;
import com.deko.mall.oms.constant.OrderConstants;
import com.deko.mall.oms.constant.OrderRoleConstants;
import com.deko.mall.oms.converter.OrderConverter;
import com.deko.mall.oms.converter.OrderItemConverter;
import com.deko.mall.oms.enums.*;
import com.deko.mall.oms.mapper.OrderAfterSaleMapper;
import com.deko.mall.oms.mapper.OrderMapper;
import com.deko.mall.oms.mapper.order.OmsOrderDeliveryItemMapper;
import com.deko.mall.oms.mapper.order.OmsOrderOfflineDeliveryMapper;
import com.deko.mall.oms.model.bo.OrderBO;
import com.deko.mall.oms.model.dto.*;
import com.deko.mall.oms.model.entity.order.*;
import com.deko.mall.oms.model.form.BatchDeliverForm;
import com.deko.mall.oms.model.form.OrderAfterSaleForm;
import com.deko.mall.oms.model.form.OrderAfterSaleItemForm;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.model.form.OfflineDeliveryForm;
import com.deko.mall.oms.model.form.ReturnLogisticsForm;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.query.OrderPageQuery;
import com.deko.mall.oms.model.vo.*;
import com.deko.mall.oms.service.logistics.LogisticsInfoService;
import com.deko.mall.oms.service.order.*;
import com.deko.mall.oms.service.order.deduction.*;
import com.deko.mall.oms.service.order.factory.OrderStrategyFactory;
import com.deko.mall.oms.service.order.statemachine.OrderItemStateMachine;
import com.deko.mall.oms.service.order.statemachine.OrderStateMachine;
import com.deko.mall.oms.service.order.statemachine.OrderStatusAggregator;
import com.deko.mall.oms.service.order.strategy.OrderProcessStrategy;
import com.deko.mall.pay.api.client.PayFeignClient;
import com.deko.mall.pay.api.dto.PaymentRequest;
import com.deko.mall.pay.api.dto.RefundRequestDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.model.vo.OfflinePayDetailVO;
import com.deko.mall.pms.api.ForbiddenAreaFeignClient;
import com.deko.mall.pms.api.FreightFeignClient;
import com.deko.mall.pms.api.SkuFeignClient;
import com.deko.mall.pms.api.SkuStockFeignClient;
import com.deko.mall.pms.model.dto.AtpChangeDTO;
import com.deko.mall.pms.enums.SpuTypeEnum;
import com.deko.mall.pms.model.dto.*;
import com.deko.mall.ums.api.MemberAssetsOperateFeignClient;
import com.deko.mall.ums.api.MemberFeignClient;
import com.deko.mall.ums.dto.*;
import com.deko.mall.ums.enums.AssetCodeEnum;
import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.*;
import java.util.stream.Collectors;
import com.deko.mall.oms.model.dto.OrderOfflineDeliverDTO;

/**
 * 订单业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacadeServiceImpl extends ServiceImpl<OrderMapper, OmsOrder> implements OrderFacadeService {

    private final CartService cartService;
    private final OrderItemService orderItemService;

    private final StringRedisTemplate redisTemplate;
    private final MemberFeignClient memberFeignClient;
    private final MemberAssetsOperateFeignClient memberAssetsOperateFeignClient;
    private final OrderAfterSaleService orderAfterSaleService;
    private final SkuFeignClient skuFeignClient;

    /**
     * 供应商直发订单通过 PMS 占用、扣减可承诺量。
     */
    private final SkuStockFeignClient skuStockFeignClient;

    private final FreightFeignClient freightFeignClient;
    private final ForbiddenAreaFeignClient forbiddenAreaFeignClient;
    private final IdGenerator idGenerator;
    private final OrderConverter orderConverter;
    private final OrderStrategyFactory orderStrategyFactory;
    private final OrderAddressService orderAddressService;
    private final OrderStateMachine orderStateMachine;
    private final OrderItemStateMachine orderItemStateMachine;
    private final OrderStatusAggregator orderStatusAggregator;
    private final OrderDeliveryService orderDeliveryService;
    private final LogisticsInfoService logisticsInfoService;
    private final OmsOrderOfflineDeliveryMapper offlineDeliveryMapper;
    private final OmsOrderDeliveryItemMapper omsOrderDeliveryItemMapper;
    private final OrderAfterSaleItemService orderAfterSaleItemService;
    private final OrderAfterSaleMapper orderAfterSaleMapper;
    private final MessageSendService messageSendService;
    private final MessageProcessService messageProcessService;
    private final OrderRoleService orderRoleService;
    private final PayFeignClient payFeignClient;
    private final OrderItemConverter orderItemConverter;
    private final PointsStep pointsStep;

    /**
     * 获取订单分页列表
     *
     * @param queryParams 查询参数
     * @return 订单分页列表
     */
    @Override
    public IPage<OrderPageVO> getOrderPage(OrderPageQuery queryParams) {
        Page<OrderBO> boPage = this.baseMapper.getOrderPage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams);

        // 转换为VO对象
        IPage<OrderPageVO> orderPageVOIPage = orderConverter.toVoPageForApp(boPage);

        // 补充 canComment、canApplyAfterSale、线下支付审核状态
        if (orderPageVOIPage.getRecords() != null) {
            orderPageVOIPage.getRecords().forEach(vo -> {
                // canComment: 已完成 + 存在未评价商品项
                boolean canComment = OrderStatusEnum.COMPLETED.getValue().equals(vo.getStatus())
                        && vo.getOrderItems() != null
                        && vo.getOrderItems().stream().anyMatch(item -> item.getIsCommented() == null || item.getIsCommented() == 0);
                vo.setCanComment(canComment);

                // canApplyAfterSale: 复用已有逻辑
                OmsOrder temp = new OmsOrder();
                temp.setOrderType(vo.getOrderType());
                temp.setStatus(vo.getStatus());
                vo.setCanApplyAfterSale(canApplyAfterSale(temp));
                setAvailableAfterSaleTypes(vo);

                // 线下支付审核状态（仅 UNPAID 订单查询，payChannel 审核通过前为 null）
                if (OrderStatusEnum.UNPAID.getValue().equals(vo.getStatus())
                        && vo.getPaymentNo() != null) {
                    try {
                        Result<OfflinePayDetailVO> offlineResult =
                                payFeignClient.getOfflinePayDetail(vo.getPaymentNo());
                        if (Result.isSuccess(offlineResult) && offlineResult.getData() != null
                                && offlineResult.getData().getAuditStatus() != null) {
                            vo.setOfflinePayAuditStatus(offlineResult.getData().getAuditStatus());
                            vo.setOfflinePayAuditRemark(offlineResult.getData().getAuditRemark());
                            vo.setPayChannel(PayChannelEnum.OFFLINE_PAY.getValue());
                        }
                    } catch (Exception e) {
                        log.debug("列表查询线下支付状态失败: paymentNo={}", vo.getPaymentNo());
                    }
                }
            });
        }

        return orderPageVOIPage;
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @return 是否取消成功
     */
    @Override
    @Transactional
    @OrderLog(detail = "取消订单")
    public boolean cancelOrder(String orderNo) {
        // 根据订单id 查出订单
        OmsOrder order = getOrderByOrderNo(orderNo);
        // 订单状态流转
        if (!orderStateMachine.transform(order, OrderStatusEnum.CANCELED)) {
            throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_CANCEL);
        }
        // 更新订单状态
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.CANCELED.getValue());
        this.updateById(order);

        // 商品项同步置为已取消（订单级真值同步到承载单元）
        orderItemService.lambdaUpdate()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getDeleted, 0)
                .set(OmsOrderItem::getStatus, OrderItemStatusEnum.CANCELED.getValue())
                .update();

        // 解锁库存（下单锁 subOrderNo，取消解锁同一 key）
        atpUnlock(order.getOrderNo(), atpItemsOf(order.getOrderNo()));

        // 退回已扣减的积分
        try {
            if (order.getUsePointsAmount() != null && order.getUsePointsAmount().compareTo(BigDecimal.ZERO) > 0) {
                AssetOperateRequest cancelRequest = new AssetOperateRequest();
                cancelRequest.setMemberId(order.getMemberId());
                cancelRequest.setAssetCode(AssetCodeEnum.POINT.getCode());
                cancelRequest.setNum(order.getUsePointsAmount());
                cancelRequest.setOperateType(AssetOperateSceneEnum.ORDER_CANCEL_REFUND);
                cancelRequest.setBizNo(order.getOrderNo());
                cancelRequest.setRemark("订单取消退回积分");

                Result<Boolean> cancelResult = memberAssetsOperateFeignClient.operateAsset(cancelRequest);
                if (!Result.isSuccess(cancelResult) || !cancelResult.getData()) {
                    log.error("订单取消退回积分失败: memberId={}, orderNo={}, amount={}",
                            order.getMemberId(), order.getOrderNo(), order.getUsePointsAmount());
                } else {
                    log.info("订单取消退回积分成功: memberId={}, orderNo={}, amount={}",
                            order.getMemberId(), order.getOrderNo(), order.getUsePointsAmount());
                }
            }

        } catch (Exception e) {
            log.error("订单取消退回资产异常: orderNo={}", order.getOrderNo(), e);
            // 不抛出异常，避免影响订单取消流程
        }

        // 关闭关联的支付单（防止线下支付待审核记录残留）
        try {
            if (order.getPaymentNo() != null) {
                payFeignClient.closePaymentByPaymentNo(order.getPaymentNo());
                log.info("订单取消，已关闭关联支付单, orderNo={}, paymentNo={}", orderNo, order.getPaymentNo());
            }
        } catch (Exception e) {
            log.error("订单取消关闭支付单失败: orderNo={}", orderNo, e);
            // 不抛出异常，避免影响订单取消流程
        }

        return true;
    }


    /**
     * 获取订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @Override
    public OrderPageVO getOrderPageVO(String orderNo) {
        // 根据订单编号 查出有效的订单信息
        OmsOrder order = this.getOne(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getOrderNo, orderNo)
                .eq(OmsOrder::getDeleted, 0)
        );

        if (order == null) {
            return null;
        }

        //根据orderId查询出OmsOrderItem
        List<OmsOrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OmsOrderItem>()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getDeleted, 0)
        );
        order.setOrderItems(orderItems);

        // 转换为VO对象
        OrderPageVO orderPageVO = orderConverter.toVoApp(order);

        // 设置支付方式标签（MapStruct 无法自动映射 payChannel → payChannelLabel）
        orderPageVO.setPayChannelLabel(IBaseEnum.getLabelByValue(order.getPayChannel(), PayChannelEnum.class));

        // 设置是否可申请售后标志
        orderPageVO.setCanApplyAfterSale(canApplyAfterSale(order));
        setAvailableAfterSaleTypes(orderPageVO);

        // 快速退款已全面关闭
        orderPageVO.setCanQuickRefund(false);

        // 设置是否可评论（已完成的订单，且存在未评论的商品项）
        boolean canComment = OrderStatusEnum.COMPLETED.getValue().equals(order.getStatus()) &&
                orderItems.stream().anyMatch(item -> item.getIsCommented() == null || item.getIsCommented() == 0);
        orderPageVO.setCanComment(canComment);

        // 查询线下支付凭证信息
        if (order.getPaymentNo() != null) {
            try {
                Result<OfflinePayDetailVO> offlineResult =
                        payFeignClient.getOfflinePayDetail(order.getPaymentNo());
                if (Result.isSuccess(offlineResult) && offlineResult.getData() != null
                        && offlineResult.getData().getAuditStatus() != null) {
                    orderPageVO.setOfflinePayAuditStatus(offlineResult.getData().getAuditStatus());
                    orderPageVO.setOfflinePayAuditRemark(offlineResult.getData().getAuditRemark());
                    orderPageVO.setPayChannel(PayChannelEnum.OFFLINE_PAY.getValue());
                }
            } catch (Exception e) {
                log.warn("查询线下支付凭证信息失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }

        // 添加收货人信息
        OmsOrderAddress orderAddress = orderAddressService.findByOrderId(order.getId(), ReceiveAddressTypeEnum.USER_RECEIVE);
        if (orderAddress != null) {
            orderPageVO.setReceiverName(orderAddress.getUserName());
            orderPageVO.setReceiverPhone(orderAddress.getUserPhone());
            orderPageVO.setReceiverAddress(orderAddress.getAddress());
            orderPageVO.setReceiverProvince(orderAddress.getProvinceName());
            orderPageVO.setReceiverCity(orderAddress.getCityName());
            orderPageVO.setReceiverDistrict(orderAddress.getAreaName());
            orderPageVO.setReceiverZipCode(orderAddress.getZipCode());
        }

        // 添加物流信息 - 从订单模块内部获取物流数据
        OmsOrderDelivery orderDelivery = orderDeliveryService.getOrderDelivery(order.getId());
        if (orderDelivery != null) {
            orderPageVO.setCarrierName(orderDelivery.getCarrierName());
            orderPageVO.setTrackingNo(orderDelivery.getTrackingNo());
            orderPageVO.setDeliveryType(orderDelivery.getDeliveryType());
            // 如果是线下发货，获取线下发货备注
            if (orderDelivery.getDeliveryType() != null && orderDelivery.getDeliveryType() == 2) {
                OmsOrderOfflineDelivery offlineDelivery = offlineDeliveryMapper.selectOne(
                        new LambdaQueryWrapper<OmsOrderOfflineDelivery>()
                                .eq(OmsOrderOfflineDelivery::getOrderId, order.getId())
                                .orderByDesc(OmsOrderOfflineDelivery::getCreateTime)
                                .last("LIMIT 1")
                );
                if (offlineDelivery != null) {
                    orderPageVO.setOfflineRemark(offlineDelivery.getOfflineRemark());
                    orderPageVO.setOfflineProof(offlineDelivery.getOfflineProof());
                }
            }
        }
        // 补充积分抵扣信息
        orderPageVO.setUsePointsAmount(Optional.ofNullable(order.getUsePointsAmount()).orElse(BigDecimal.ZERO));
        orderPageVO.setPointsDeductionAmount(Optional.ofNullable(order.getPointsDeductionAmount()).orElse(BigDecimal.ZERO));

        return orderPageVO;
    }

    /**
     * 根据订单编号获取订单
     *
     * @param orderNo 订单编号
     * @return 订单实体
     */
    @Override
    public OmsOrder getOrderByOrderNo(String orderNo) {
        return this.getOne(new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getOrderNo, orderNo));
    }

    /**
     * 判断订单是否可以申请售后
     * 业务规则：
     * 1. 只有普通订单可以申请售后
     * 2. 只有已发货的订单可以申请售后
     * 3. 已支付的订单且存在可售后商品项时才能申请售后
     * 4. 订单状态不能是待付款(0)、已取消(4)、售后中(5)、已退款(6)等状态
     *
     * @return 是否可以申请售后
     */
    private Boolean canApplyAfterSale(OmsOrder order) {
        // 1. 只有普通订单可以申请售后
        String orderType = order.getOrderType();
        if (!OrderTypeEnum.NORMAL.getValue().equals(orderType)) {
            return false;
        }

        // 2. 只有存在"可售后商品项"的订单可以申请售后（行级校验，多供应商订单按商品行）
        long refundableItemCount = orderItemService.lambdaQuery()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getDeleted, 0)
                .ne(OmsOrderItem::getRefundStatus, 2)
                .count();
        if (refundableItemCount == 0) {
            return false;
        }

        // 3. 订单状态检查：排除待付款、待发货、已取消、售后中、已退款 等状态
        // 已发货(DELIVERED)和已完成(COMPLETED)的订单可以申请售后
        if (order.getStatus() == null ||
                order.getStatus().equals(OrderStatusEnum.UNPAID.getValue()) ||
                order.getStatus().equals(OrderStatusEnum.CANCELED.getValue()) ||
                order.getStatus().equals(OrderStatusEnum.REFUNDING.getValue()) ||
                order.getStatus().equals(OrderStatusEnum.REFUNDED.getValue())) {
            return false;
        }

        return true;
    }

    /**
     * 设置可用的售后类型列表
     * <p>未发货（待发货状态）仅可选"仅退款"，已发货可选"仅退款"和"退货退款"</p>
     *
     * @param vo 订单视图对象（需已设置 canApplyAfterSale 和 status）
     */
    private void setAvailableAfterSaleTypes(OrderPageVO vo) {
        if (Boolean.TRUE.equals(vo.getCanApplyAfterSale())) {
            if (OrderStatusEnum.PAID.getValue().equals(vo.getStatus())) {
                vo.setAvailableAfterSaleTypes(List.of(AfterSaleTypeEnum.REFUND_ONLY.getValue()));
            } else {
                vo.setAvailableAfterSaleTypes(List.of(
                        AfterSaleTypeEnum.REFUND_ONLY.getValue(),
                        AfterSaleTypeEnum.RETURN_REFUND.getValue()));
            }
        }
    }

    /**
     * 订单支付成功回调
     *
     * @param order 订单实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "订单支付成功")
    public void paymentResult(OmsOrder order) {
        OmsOrder orderInfo = this.getOrderByOrderNo(order.getOrderNo());
        // 1. 更新订单状态
        if (!orderStateMachine.transform(orderInfo, OrderStatusEnum.PAID)) {
            throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_PAY);
        }
        orderInfo.setStatus(OrderStatusEnum.PAID.getValue());


        //判断订单是否需要发货，如果无需发货，订单直接变成已发货
        if (!orderInfo.getNeedDelivery()) {
            if (!orderStateMachine.transform(orderInfo, OrderStatusEnum.DELIVERED)) {
                throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_SHIP);
            }
            orderInfo.setStatus(OrderStatusEnum.DELIVERED.getValue());
            orderInfo.setDeliveryTime(LocalDateTime.now());
        }

        // 设置支付时间：优先使用传入的支付时间，否则使用当前时间
        if (order.getPaymentTime() != null) {
            orderInfo.setPaymentTime(order.getPaymentTime());
        } else {
            orderInfo.setPaymentTime(LocalDateTime.now());
        }
        orderInfo.setPayChannel(order.getPayChannel());
        orderInfo.setMchId(order.getMchId());
        orderInfo.setPaymentChannelNo(order.getPaymentChannelNo());
        orderInfo.setChannelTradeNo(order.getChannelTradeNo());
        orderInfo.setChannelUserId(order.getChannelUserId());
        this.updateById(orderInfo);


        // 2. 扣减库存
        atpDeduct(orderInfo.getOrderNo(), atpItemsOf(orderInfo.getOrderNo()));
    }

    /**
     * 订单退款成功回调
     *
     * @param order 订单实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "订单退款成功")
    public void refundCallbackOrder(OmsOrder order) {
        OmsOrder orderInfo = getOrderByOrderNo(order.getOrderNo());
        // 1. 更新订单状态
        if (!orderStateMachine.transform(orderInfo, OrderStatusEnum.REFUNDED)) {
            throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_REFUND);
        }
        orderInfo.setStatus(OrderStatusEnum.REFUNDED.getValue());
        orderInfo.setRefundTime(LocalDateTime.now());
        this.updateById(orderInfo);
        // 2. 库存 / 货权回退
        atpUnlock(order.getOrderNo(), atpItemsOf(order.getOrderNo()));
    }

    /**
     * 订单确认 → 进入创建订单页面
     * <p>
     * 获取购买商品明细、用户默认收货地址、防重提交唯一token
     * 进入订单创建页面有两个入口，1：立即购买；2：购物车结算
     *
     * @param skuId       商品ID(直接购买传值)
     * @param count       商品数量
     * @param channelCode 渠道编码（可选）
     * @return 订单确认信息
     */

    @Override
    public OrderConfirmVO confirmOrder(Long skuId, Integer count, String channelCode, Long addressId) {
        // 1. 参数校验
        if (skuId != null && count <= 0) {
            throw new BizException(OmsResultCode.QUANTITY_MUST_BE_POSITIVE);
        }

        Long memberId = SecurityUtils.getMemberId();

        // 2. 生成订单Token
        String orderToken = idGenerator.generateOrderNo();
        redisTemplate.opsForValue().set(OrderConstants.ORDER_TOKEN_PREFIX + orderToken, orderToken);


        // 3. 组装订单商品明细
        List<OrderItemDTO> orderItems = getOrderItems(skuId, memberId, count);
        if (orderItems.isEmpty()) {
            if (skuId != null) {
                throw new BizException(OmsResultCode.ORDER_GOODS_NOT_EXIST);
            } else {
                throw new BizException(OmsResultCode.CART_NO_SELECTED_GOODS);
            }
        }

        // 5. 获取用户收货地址
        Result<List<MemberAddressDTO>> getMemberAddressResult = memberFeignClient.listMemberAddresses(memberId);
        List<MemberAddressDTO> addresses = Result.isSuccess(getMemberAddressResult) ? getMemberAddressResult.getData() : Collections.emptyList();

        // 6. 确定目标地址
        MemberAddressDTO calcAddress = null;
        if (!addresses.isEmpty()) {
            calcAddress = addressId != null ? findAddressById(addresses, addressId) : addresses.get(0);
        }

        // 7. 按 (shopId, spuType) 分组（店铺=履约单元，供应商只参与结算不参与拆单）
        Map<String, List<OrderItemDTO>> grouped = orderItems.stream()
                .collect(Collectors.groupingBy(item ->
                        (item.getShopId() != null ? item.getShopId() : 0) + "_" + item.getSpuType()));

        List<OrderConfirmVO.SubOrder> subOrders = new ArrayList<>();
        BigDecimal totalPayment = BigDecimal.ZERO;

        for (Map.Entry<String, List<OrderItemDTO>> entry : grouped.entrySet()) {
            List<OrderItemDTO> groupItems = entry.getValue();
            OrderItemDTO first = groupItems.get(0);
            String groupSpuType = first.getSpuType();

            // 组装 SubOrder（用户侧只暴露店铺信息，供应商为内部供应链维度）
            OrderConfirmVO.SubOrder subOrder = new OrderConfirmVO.SubOrder();
            subOrder.setShopId(first.getShopId());
            subOrder.setShopName(first.getShopName());
            subOrder.setSpuType(groupSpuType);
            subOrder.setOrderItems(orderItemConverter.dto2ListVO(groupItems));

            // 运费
            BigDecimal freightAmount = BigDecimal.ZERO;
            if (calcAddress != null) {
                freightAmount = calculateFreightForConfirm(groupItems, calcAddress);
                // 禁售校验
                Map<Long, SkuInfoDTO> skuMap = buildSkuMapFromOrderItems(groupItems);
                List<String> forbiddenItems = checkForbiddenAreaForConfirm(calcAddress, groupItems, skuMap);
                if (!forbiddenItems.isEmpty()) {
                    subOrder.setHasForbiddenAreaItems(true);
                    subOrder.setForbiddenItemNames(forbiddenItems);
                }
            }
            subOrder.setFreightAmount(freightAmount);

            // 商品总金额
            BigDecimal totalAmount = groupItems.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 抵扣预览
            Map<Long, SkuInfoDTO> deductionSkuMap = buildSkuMapFromOrderItems(groupItems);
            OrderTypeEnum previewOrderType = OrderTypeEnum.NORMAL;

            OrderSubmitForm tempForm = new OrderSubmitForm();

            StepContext ctx = StepContext.builder()
                    .memberId(memberId)
                    .orderType(previewOrderType)
                    .skuMap(deductionSkuMap)
                    .submitForm(tempForm)
                    .order(null)
                    .orderToken(orderToken)
                    .totalProductAmount(totalAmount)
                    .freightAmount(freightAmount)
                    .build();

            // 抵扣链：仅保留通用能力（积分）
            DeductionChain chain = DeductionChain.of(pointsStep);
            BigDecimal paymentAmount = chain.calculate(ctx);

            subOrder.setProductDeductionAmount(ctx.getTotalProductDeduction().setScale(2, RoundingMode.CEILING));
            subOrder.setFreightDeductionAmount(ctx.getTotalFreightDeduction().setScale(2, RoundingMode.CEILING));
            subOrder.setPaymentAmount(paymentAmount.setScale(2, RoundingMode.CEILING));

            StepResult pointsResult = ctx.getLine(AssetCodeEnum.POINT);
            subOrder.setPointsDeductionAmount(
                    pointsResult.getProductDeduction().add(pointsResult.getFreightDeduction())
                            .setScale(2, RoundingMode.CEILING));

            subOrders.add(subOrder);
            totalPayment = totalPayment.add(paymentAmount);
        }

        // 8. 组装返回
        OrderConfirmVO vo = new OrderConfirmVO();
        vo.setOrderToken(orderToken);
        if (calcAddress != null) {
            vo.setDefaultAddress(calcAddress);
        }
        vo.setOrders(subOrders);
        vo.setTotalPaymentAmount(totalPayment.setScale(2, RoundingMode.CEILING));
        vo.setSubOrderCount(subOrders.size());
        if (calcAddress != null) {
            vo.setDefaultAddress(calcAddress);
        }

        log.info("Order confirm response: subOrders={}, totalPayment={}", subOrders.size(), vo.getTotalPaymentAmount());
        return vo;
    }

    private String getSupplierName(Long supplierId) {
        if (supplierId == null) return "默认供应商";
        return "供应商(ID:" + supplierId + ")";
    }

    private MemberAddressDTO findAddressById(List<MemberAddressDTO> addresses, Long addressId) {
        if (addressId == null || addresses == null) return null;
        return addresses.stream()
                .filter(a -> addressId.equals(a.getId()))
                .findFirst().orElse(null);
    }

    /**
     * 从订单项中批量查SKU，避免重复查询
     */
    private Map<Long, SkuInfoDTO> buildSkuMapFromOrderItems(List<OrderItemDTO> orderItems) {
        Map<Long, SkuInfoDTO> skuMap = new HashMap<>();
        List<Long> skuIds = orderItems.stream().map(OrderItemDTO::getSkuId).distinct().collect(Collectors.toList());
        if (!skuIds.isEmpty()) {
            Result<List<SkuInfoDTO>> skuInfoListResult = skuFeignClient.getSkuInfoList(skuIds);
            if (Result.isSuccess(skuInfoListResult) && skuInfoListResult.getData() != null) {
                skuInfoListResult.getData().forEach(skuInfo -> skuMap.put(skuInfo.getId(), skuInfo));
            }
        }
        return skuMap;
    }

    /**
     * 订单提交
     *
     * @param submitForm 订单提交表单
     * @return 订单提交结果，包含订单号、是否需要支付、应付金额等信息
     */
    @Override
    @Transactional
    @OrderLog(detail = "订单提交")
    public OrderSubmitResultVO submitOrder(OrderSubmitForm submitForm) {
        log.info("订单提交参数:{}", JSONUtil.toJsonStr(submitForm));

        // 1. 验证表单 & 防重复提交
        validateSubmitForm(submitForm);
        checkDuplicateSubmit(submitForm.getOrderToken());

        // 2. 获取商品信息
        List<OrderSubmitForm.OrderItem> allItems = submitForm.getOrderItems();
        Map<Long, SkuInfoDTO> skuMap = getSkuInfoMap(allItems);

        // 3. 按 (shopId, spuType) 分组，每组独立创建子订单
        //    （店铺=履约单元，供应商只参与结算不参与拆单，未来非供应商直营/一店多供应商均按店铺拆单）
        String orderToken = submitForm.getOrderToken();
        List<String> orderNos = new ArrayList<>();
        List<String> lockedOrderNos = new ArrayList<>();
        // 已占用 ATP 的明细（子单号 → 明细）：失败补偿要按它释放 —— 此时订单行尚未落库，读 DB 读不到
        Map<String, List<AtpChangeDTO>> lockedAtp = new LinkedHashMap<>();
        // 已完成资产抵扣的子订单：登记点在 strategy.calculateAmount 成功返回之后。
        // 失败时依据子订单记录的实际扣减额逐单退还。
        List<OmsOrder> deductionCompletedOrders = new ArrayList<>();
        BigDecimal totalPayment = BigDecimal.ZERO;
        Boolean needPay = false;

        Map<String, List<OrderSubmitForm.OrderItem>> grouped = allItems.stream()
                .collect(Collectors.groupingBy(item -> {
                    SkuInfoDTO sku = skuMap.get(item.getSkuId());
                    if (sku == null) {
                        throw new BizException(OmsResultCode.SKU_NOT_FOUND, item.getSkuId());
                    }
                    return (sku.getShopId() != null ? sku.getShopId() : 0)
                            + "_" + (sku.getSpuType() != null ? sku.getSpuType() : "NORMAL");
                }));

        try {
            for (Map.Entry<String, List<OrderSubmitForm.OrderItem>> entry : grouped.entrySet()) {
                List<OrderSubmitForm.OrderItem> groupItems = entry.getValue();
                SkuInfoDTO firstSku = skuMap.get(groupItems.get(0).getSkuId());
                String spuType = firstSku.getSpuType() != null ? firstSku.getSpuType() : "NORMAL";

                // 生成子订单号
                String subOrderNo = grouped.size() == 1 ? orderToken : idGenerator.generateOrderNo();

                // 锁定本子订单的库存（key = subOrderNo，与 deductStock 对应）
                List<AtpChangeDTO> lockedItems = lockSkuStock(subOrderNo, groupItems, groupSkuMap(groupItems, skuMap));
                lockedOrderNos.add(subOrderNo);
                lockedAtp.put(subOrderNo, lockedItems);
                log.info("锁定库存成功，子订单: {}", subOrderNo);

                // 子订单的 OrderSubmitForm（继承父级 token + address）
                OrderSubmitForm subForm = new OrderSubmitForm();
                subForm.setOrderToken(orderToken);
                subForm.setOrderItems(groupItems);
                subForm.setPaymentAmount(submitForm.getPaymentAmount());
                subForm.setShippingAddress(submitForm.getShippingAddress());
                subForm.setRemark(submitForm.getRemark());
                subForm.setOrderSource(submitForm.getOrderSource());
                subForm.setShareUserId(submitForm.getShareUserId());
                subForm.setChannelCode(submitForm.getChannelCode());
                subForm.setOrderScene(submitForm.getOrderScene());

                // 验证所有 SKU 都能查到
                for (OrderSubmitForm.OrderItem item : allItems) {
                    if (skuMap.get(item.getSkuId()) == null) {
                        throw new BizException(OmsResultCode.SKU_NOT_FOUND, item.getSkuId());
                    }
                }

                // 获取策略
                Map<Long, SkuInfoDTO> groupSkuMap = new HashMap<>();
                for (OrderSubmitForm.OrderItem item : groupItems) {
                    SkuInfoDTO sku = skuMap.get(item.getSkuId());
                    if (sku != null) {
                        groupSkuMap.put(item.getSkuId(), sku);
                    }
                }

                OrderProcessStrategy strategy = orderStrategyFactory.getStrategy(subForm, groupSkuMap);
                strategy.preProcess(subForm, groupSkuMap);
                strategy.validateOrder(subForm, groupSkuMap);

                // 创建子订单
                OmsOrder order = createOrder(subForm, groupItems, groupSkuMap);
                order.setOrderNo(subOrderNo);  // 子订单号覆盖父 token，避免唯一键冲突
                // 店铺信息快照（用户侧按店铺维度展示/售后）
                order.setShopId(firstSku.getShopId());
                order.setShopName(firstSku.getShopName());
                order.setOrderScene(subForm.getOrderScene());
                order.setNeedDelivery(firstSku.getNeedDelivery() != null ? firstSku.getNeedDelivery() : Boolean.TRUE);
                this.save(order);

                MemberAddressDTO memberAddress = this.saveUserOrderAddress(order, submitForm);

                // 运费 & 禁售
                this.calculateAndSaveFreight(order, groupItems, memberAddress);
                this.checkForbiddenArea(order, memberAddress, groupItems, groupSkuMap);

                // 计算金额（策略定价 + 抵扣链）
                strategy.calculateAmount(order, subForm, groupSkuMap);
                // 抵扣链内部是**真实扣减**（chain.lock → ums operateAsset，非预占）；成功返回即登记，
                // 失败时按子订单的实际扣减额退还积分。
                deductionCompletedOrders.add(order);
                log.info("子订单[{}]使用[{}]策略计算金额完成", order.getOrderNo(), strategy.getClass().getSimpleName());

                // 订单项 & 角色
                createOrderItems(order, groupItems, groupSkuMap);
                saveOrderRole(order);
                strategy.postProcess(order, subForm, groupSkuMap);
                this.updateById(order);

                orderNos.add(order.getOrderNo());
                totalPayment = totalPayment.add(order.getPaymentAmount());
                if (order.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
                    needPay = true;
                }
            }

            // 4. 清空购物车
            cartService.removeCheckedItem();

            // 5. 创建支付单（pay service 生成 paymentNo 并返回）
            PaymentRequest payReq = new PaymentRequest();
            payReq.setPayAmount(totalPayment.setScale(2, RoundingMode.CEILING));
            payReq.setOrderNos(String.join(",", orderNos));
            payReq.setMemberId(SecurityUtils.getMemberId());
            Result<String> payResult = payFeignClient.createPayment(payReq);
            if (!Result.isSuccess(payResult)) {
                throw new BizException(OmsResultCode.PAY_CREATE_FAILED);
            }
            String paymentNo = payResult.getData();

            // 6. 回写 paymentNo 到所有子订单
            for (String orderNo : orderNos) {
                OmsOrder o = getOrderByOrderNo(orderNo);
                o.setPaymentNo(paymentNo);
                this.updateById(o);
            }

            log.info("订单提交成功: token={}, paymentNo={}, subOrders={}, orderNos={}, total={}",
                    orderToken, paymentNo, orderNos.size(), orderNos, totalPayment);

            return OrderSubmitResultVO.builder()
                    .orderNo(paymentNo)
                    .paymentNo(paymentNo)
                    .needPay(needPay)
                    .paymentAmount(totalPayment.setScale(2, RoundingMode.CEILING))
                    .subOrderCount(orderNos.size())
                    .build();
        } catch (Exception e) {
            // 按已成功占用的 ATP 明细释放库存，不依赖订单是否落库。
            for (Map.Entry<String, List<AtpChangeDTO>> locked : lockedAtp.entrySet()) {
                try { atpUnlock(locked.getKey(), locked.getValue()); }
                catch (Exception ex) { log.error("释放 ATP 失败: {}", locked.getKey(), ex); }
            }
            // 按已完成抵扣的子订单退还实际扣减额。
            refundDeductedAssetsOnFailure(submitForm, deductionCompletedOrders, orderToken);
            log.error("订单提交失败", e);
            throw new BizException(OmsResultCode.ORDER_SUBMIT_FAILED, (Object) e.getMessage());
        }
    }

    private Map<Long, SkuInfoDTO> groupSkuMap(List<OrderSubmitForm.OrderItem> items,
                                               Map<Long, SkuInfoDTO> fullSkuMap) {
        Map<Long, SkuInfoDTO> result = new HashMap<>();
        for (OrderSubmitForm.OrderItem item : items) {
            SkuInfoDTO sku = fullSkuMap.get(item.getSkuId());
            if (sku != null) result.put(item.getSkuId(), sku);
        }
        return result;
    }

    /**
     * 退还已完成抵扣的子订单积分。
     *
     * 资产扣减通过远程事务提交，不随 OMS 本地事务回滚，需逐单补偿。
     */
    void refundDeductedAssetsOnFailure(OrderSubmitForm submitForm,
                                      List<OmsOrder> deductionCompletedOrders,
                                      String orderToken) {
        if (deductionCompletedOrders.isEmpty()) {
            rollbackDeductedAssets(submitForm, null, orderToken);
            return;
        }
        for (OmsOrder completedOrder : deductionCompletedOrders) {
            rollbackDeductedAssets(submitForm, completedOrder, completedOrder.getOrderNo());
        }
    }

    private void rollbackDeductedAssets(OrderSubmitForm submitForm, OmsOrder order, String orderNo) {
        Long memberId = order != null ? order.getMemberId() : SecurityUtils.getMemberId();

        // 退还积分
        if (order != null && order.getUsePointsAmount() != null
                && order.getUsePointsAmount().compareTo(BigDecimal.ZERO) > 0) {
            tryRefundAsset(memberId, AssetCodeEnum.POINT,
                    order.getUsePointsAmount(), orderNo, "订单提交失败退还积分");
        }
    }

    private void tryRefundAsset(Long memberId, AssetCodeEnum assetCode, BigDecimal amount,
                                String bizNo, String remark) {
        try {
            AssetOperateRequest refundRequest = new AssetOperateRequest();
            refundRequest.setMemberId(memberId);
            refundRequest.setAssetCode(assetCode.getCode());
            refundRequest.setNum(amount);
            refundRequest.setOperateType(AssetOperateSceneEnum.ORDER_CANCEL_REFUND);
            refundRequest.setBizNo(bizNo);
            refundRequest.setRemark(remark);

            Result<Boolean> result = memberAssetsOperateFeignClient.operateAsset(refundRequest);
            if (Result.isSuccess(result) && result.getData()) {
                log.info("订单提交失败退还资产成功: memberId={}, assetCode={}, amount={}, bizNo={}",
                        memberId, assetCode.getCode(), amount, bizNo);
            } else {
                log.error("订单提交失败退还资产失败: memberId={}, assetCode={}, amount={}, bizNo={}",
                        memberId, assetCode.getCode(), amount, bizNo);
            }
        } catch (Exception ex) {
            log.error("订单提交失败退还资产异常: memberId={}, assetCode={}, amount={}, bizNo={}",
                    memberId, assetCode.getCode(), amount, bizNo, ex);
        }
    }

    private void saveOrderRole(OmsOrder order) {
        List<OmsOrderRole> orderRoleList = new ArrayList<>();
        // 下单人角色快照存储
        if (order.getMemberId() != null) {
            Result<MemberInfoDTO> buyerInfoResult = memberFeignClient.info(order.getMemberId());
            if (Result.isSuccess(buyerInfoResult) && buyerInfoResult.getData() != null) {
                orderRoleList.add(OmsOrderRole.builder()
                        .memberId(order.getMemberId())
                        .orderId(order.getId())
                        .orderNo(order.getOrderNo())
                        .roleType(OrderRoleConstants.RoleType.BUYER)
                        .roleCode(buyerInfoResult.getData().getRoleCode())
                        .build());
            }

        }

        // 分享人角色快照（会员不存在/已清理时跳过，不影响下单）
        if (order.getShareUserId() != null) {
            try {
                Result<MemberInfoDTO> sharerRoleResult = memberFeignClient.info(order.getShareUserId());
                if (Result.isSuccess(sharerRoleResult) && sharerRoleResult.getData() != null) {
                    orderRoleList.add(OmsOrderRole.builder()
                            .memberId(order.getShareUserId())
                            .orderId(order.getId())
                            .orderNo(order.getOrderNo())
                            .roleType(OrderRoleConstants.RoleType.SHARER)
                            .roleCode(sharerRoleResult.getData().getRoleCode())
                            .build());
                } else {
                    log.warn("分享人无效，跳过角色快照: orderNo={}, shareUserId={}",
                            order.getOrderNo(), order.getShareUserId());
                }
            } catch (Exception ex) {
                log.warn("查询分享人失败，跳过角色快照: orderNo={}, shareUserId={}, err={}",
                        order.getOrderNo(), order.getShareUserId(), ex.getMessage());
            }
        }

        if (!orderRoleList.isEmpty()) {
            orderRoleService.saveBatch(orderRoleList);
        }
    }

    private OmsOrder createOrder(OrderSubmitForm submitForm, List<OrderSubmitForm.OrderItem> orderItems, Map<Long, SkuInfoDTO> skuMap) {
        OmsOrder order = new OmsOrder();
        order.setOrderNo(submitForm.getOrderToken());
        order.setStatus(OrderStatusEnum.UNPAID.getValue());
        order.setMemberId(SecurityUtils.getMemberId());
        // 下单人手机号快照（冗余存储）
        // 优先从 JWT 获取（零网络开销）；旧 token 无此 claim 时降级 Feign 兜底
        String buyerPhone = SecurityUtils.getMemberPhone();
        if (buyerPhone == null) {
            try {
                Result<MemberInfoDTO> infoResult = memberFeignClient.info(order.getMemberId());
                if (Result.isSuccess(infoResult) && infoResult.getData() != null) {
                    buyerPhone = infoResult.getData().getMobile();
                }
            } catch (Exception e) {
                log.warn("Feign兜底获取手机号失败, memberId={}", order.getMemberId(), e);
            }
        }
        order.setBuyerPhone(buyerPhone);
        order.setShareUserId(submitForm.getShareUserId());
        order.setSource(submitForm.getOrderSource().getValue());
        order.setRemark(submitForm.getRemark());

        // 只计算基础数据，不做订单类型判断和特殊价格处理
        calculateBaseAmount(order, orderItems, skuMap);


        return order;
    }

    /**
     * 计算订单基础金额和数量信息
     * 注意：此方法只计算基础数量和零售价/供货价统计，不做订单类型判断和特殊价格处理
     * 订单类型判断和特殊价格处理由各策略类的calculateAmount方法负责
     */
    private void calculateBaseAmount(OmsOrder order, List<OrderSubmitForm.OrderItem> orderItems, Map<Long, SkuInfoDTO> skuMap) {
        int totalQuantity = 0;
        BigDecimal totalRetailAmount = BigDecimal.ZERO;
        BigDecimal totalSupplyAmount = BigDecimal.ZERO;

        for (OrderSubmitForm.OrderItem item : orderItems) {
            SkuInfoDTO skuInfo = skuMap.get(item.getSkuId());

            // 只计算基础数据，不处理价格选择
            totalRetailAmount = totalRetailAmount.add(skuInfo.getRetailPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            BigDecimal supplyPrice = skuInfo.getSupplyPrice() != null ? skuInfo.getSupplyPrice() : BigDecimal.ZERO;
            totalSupplyAmount = totalSupplyAmount.add(supplyPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalQuantity += item.getQuantity();
        }

        order.setTotalQuantity(totalQuantity);
        order.setTotalRetailPrice(totalRetailAmount);
        order.setTotalSupplyPrice(totalSupplyAmount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
    }

    private void validateSubmitForm(OrderSubmitForm submitForm) {
        Assert.notEmpty(submitForm.getOrderItems(), "订单商品不能为空");
        Assert.notNull(submitForm.getShippingAddress(), "收货地址不能为空");
        Assert.notNull(submitForm.getOrderSource(), "订单来源不能为空");
    }

    private void checkDuplicateSubmit(String orderToken) {
        String lockAcquireScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long lockAcquired = this.redisTemplate.execute(
                new DefaultRedisScript<>(lockAcquireScript, Long.class),
                Collections.singletonList(OrderConstants.ORDER_TOKEN_PREFIX + orderToken),
                orderToken
        );
        Assert.isTrue(lockAcquired != null && lockAcquired.equals(1L), "订单重复提交，请刷新页面后重试");
    }

    /**
     * 获取SKU信息映射，并确保OrderItem中包含必要的快照信息
     *
     * @param orderItems 订单商品项列表
     * @return SKU信息映射 (SkuId -> SkuInfoDTO)
     */


    private Map<Long, SkuInfoDTO> getSkuInfoMap(List<OrderSubmitForm.OrderItem> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) {
            return Collections.emptyMap();
        }

        // 获取所有SKU ID
        List<Long> skuIds = orderItems.stream()
                .map(OrderSubmitForm.OrderItem::getSkuId)
                .collect(Collectors.toList());

        // 调用商品服务获取SKU信息列表
        Result<List<SkuInfoDTO>> skuListResult = skuFeignClient.getSkuInfoList(skuIds);
        List<SkuInfoDTO> skuList = skuListResult.getData();
        Assert.notEmpty(skuList, "商品信息不存在");

        // 创建SKU信息映射
        Map<Long, SkuInfoDTO> skuMap = skuList.stream()
                .collect(Collectors.toMap(SkuInfoDTO::getId, sku -> sku));

        // 为订单商品项设置快照信息：运费模板ID、禁售区域ID、供应商ID
        for (OrderSubmitForm.OrderItem item : orderItems) {
            Long skuId = item.getSkuId();
            SkuInfoDTO skuInfo = skuMap.get(skuId);

            if (skuInfo != null) {
                // 记录运费模板ID
                if (item.getFreightTemplateId() == null && skuInfo.getFreightTemplateId() != null) {
                    log.debug("为商品{}设置运费模板ID: {}", skuId, skuInfo.getFreightTemplateId());
                    item.setFreightTemplateId(skuInfo.getFreightTemplateId());
                }

                // 记录禁售区域ID
                if (item.getForbiddenAreaId() == null && skuInfo.getForbiddenAreaId() != null) {
                    log.debug("为商品{}设置禁售区域ID: {}", skuId, skuInfo.getForbiddenAreaId());
                    item.setForbiddenAreaId(skuInfo.getForbiddenAreaId());
                }

                // 记录供应商ID
                if (item.getSupplierId() == null && skuInfo.getSupplierId() != null) {
                    log.debug("为商品{}设置供应商ID: {}", skuId, skuInfo.getSupplierId());
                    item.setSupplierId(skuInfo.getSupplierId());
                }

                // 填充快照价格（前端不传，后端根据 SKU 信息补全）
                if (item.getPrice() == null) {
                    item.setPrice(skuInfo.getRetailPrice());
                }
            }
        }

        return skuMap;
    }

    /**
     * 占用本单的 ATP（零售只占可承诺量：实物不进云仓、由供应商直发）
     * <p>
     * 整批一次调用 —— pms 侧一个本地事务，避免逐 SKU 半途失败留下"部分占用"（跨服务无法回滚）。
     *
     * @return 已占用的明细（补偿时按明细逐条释放，订单未落库时读不到订单行）
     */
    private List<AtpChangeDTO> lockSkuStock(String orderToken, List<OrderSubmitForm.OrderItem> orderItems,
                                            Map<Long, SkuInfoDTO> skuMap) {
        List<AtpChangeDTO> atpItems = orderItems.stream()
                .map(item -> {
                    SkuInfoDTO skuInfo = skuMap.get(item.getSkuId());
                    Assert.notNull(skuInfo, String.format("商品(%s)已下架或删除", item.getSkuName()));

                    AtpChangeDTO atp = new AtpChangeDTO();
                    atp.setSkuId(item.getSkuId());
                    atp.setSpuId(skuInfo.getSpuId());
                    atp.setQuantity(item.getQuantity());
                    return atp;
                })
                .collect(Collectors.toList());

        Result<Boolean> lockResult = skuStockFeignClient.lock(orderToken, atpItems);
        Assert.isTrue(Result.isSuccess(lockResult) && Boolean.TRUE.equals(lockResult.getData()),
                "占用商品可售量失败");
        return atpItems;
    }

    private void createOrderItems(OmsOrder order, List<OrderSubmitForm.OrderItem> orderItems, Map<Long, SkuInfoDTO> skuMap) {
        List<OmsOrderItem> orderItemEntities = orderItems.stream()
                .map(item -> {
                    SkuInfoDTO skuInfo = skuMap.get(item.getSkuId());
                    return buildOrderItem(order.getId(), item, skuInfo);
                })
                .collect(Collectors.toList());

        // 初始化状态 + 行级抵扣分摊 + 行实付（商品项为履约/资金承载单元）
        initItemAndDistributeDeductions(order, orderItemEntities);

        orderItemService.saveBatch(orderItemEntities);
    }

    /**
     * 行级金额分摊与状态初始化
     * <p>
     * 分摊规则（与订单级金额守恒）：
     * - 各类订单级抵扣按行商品金额占比分摊，最后一行兜底尾差（Σ行 = 订单级）
     * - 行实付 = 行商品额 + 行运费 - Σ行抵扣（clamp >= 0）
     * - 商品金额合计为 0（如全额抵扣订单）时按行数均分兜底
     */
    private void initItemAndDistributeDeductions(OmsOrder order, List<OmsOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OmsOrderItem item : items) {
            item.setStatus(OrderItemStatusEnum.PENDING_SHIP.getValue());
            item.setRefundStatus(0); // 0-未退款（item 私有语义，pay 枚举无 NORMAL）
            item.setRefundQuantity(0);
            item.setRefundAmount(BigDecimal.ZERO);
        }

        BigDecimal totalProduct = items.stream()
                .map(item -> nvl(item.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean equalSplit = totalProduct.compareTo(BigDecimal.ZERO) <= 0;

        BigDecimal coupon = nvl(order.getCouponAmount());
        BigDecimal points = nvl(order.getPointsDeductionAmount());

        BigDecimal remainCoupon = coupon;
        BigDecimal remainPoints = points;

        int size = items.size();
        for (int i = 0; i < size; i++) {
            OmsOrderItem item = items.get(i);
            boolean last = i == size - 1;
            BigDecimal ratio = equalSplit
                    ? BigDecimal.ONE.divide(BigDecimal.valueOf(size), 10, RoundingMode.DOWN)
                    : nvl(item.getTotalAmount()).divide(totalProduct, 10, RoundingMode.DOWN);

            BigDecimal itemCoupon;
            BigDecimal itemPoints;
            if (last) {
                // 尾差兜底：剩余抵扣全部归最后一行，保证 Σ行 = 订单级
                itemCoupon = remainCoupon;
                itemPoints = remainPoints;
            } else {
                itemCoupon = coupon.multiply(ratio).setScale(2, RoundingMode.DOWN);
                itemPoints = points.multiply(ratio).setScale(2, RoundingMode.DOWN);
                remainCoupon = remainCoupon.subtract(itemCoupon);
                remainPoints = remainPoints.subtract(itemPoints);
            }

            item.setCouponDeductionAmount(itemCoupon);
            item.setPointsDeductionAmount(itemPoints);

            // 行实付 = 商品额 + 行运费 - Σ行抵扣（clamp >= 0，防抵扣含运费部分时行级为负）
            BigDecimal deductions = itemCoupon.add(itemPoints);
            BigDecimal payment = nvl(item.getTotalAmount())
                    .add(nvl(item.getFreightAmount()))
                    .subtract(deductions);
            item.setPaymentAmount(payment.compareTo(BigDecimal.ZERO) > 0 ? payment.setScale(2, RoundingMode.DOWN) : BigDecimal.ZERO);
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * 构建订单商品行对象，包含运费信息、禁售区域和供应商等快照信息
     *
     * @param orderId 订单ID
     * @param item    订单项信息
     * @param skuInfo SKU信息
     * @return 订单商品行实体
     */
    private OmsOrderItem buildOrderItem(Long orderId, OrderSubmitForm.OrderItem item, SkuInfoDTO skuInfo) {
        OmsOrderItem orderItem = new OmsOrderItem();
        // 设置基本信息
        orderItem.setOrderId(orderId);
        orderItem.setSpuId(skuInfo.getSpuId());
        orderItem.setSkuId(skuInfo.getId());
        orderItem.setSkuSn(skuInfo.getSkuSn());
        orderItem.setPrice(item.getPrice());
        orderItem.setSpuName(skuInfo.getSpuName());
        orderItem.setSkuName(skuInfo.getSkuName());
        orderItem.setPicUrl(skuInfo.getPicUrl());
        orderItem.setRetailPrice(skuInfo.getRetailPrice());
        orderItem.setSupplyPrice(skuInfo.getSupplyPrice());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setTotalAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        // 设置运费模板信息（快照）
        orderItem.setFreightTemplateId(item.getFreightTemplateId());
        orderItem.setFreightAmount(item.getFreightAmount());

        // 设置禁售区域信息（快照）
        orderItem.setForbiddenAreaId(item.getForbiddenAreaId());

        // 设置供应商信息（快照）
        // 优先使用OrderItem中的供应商ID，如果为空则从SkuInfo获取
        if (item.getSupplierId() != null) {
            orderItem.setSupplierId(item.getSupplierId());
        } else if (skuInfo.getSupplierId() != null) {
            orderItem.setSupplierId(skuInfo.getSupplierId());
        }
        orderItem.setSupplierName(skuInfo.getSupplierName());

        // 设置店铺信息（快照，用户侧按店铺维度展示/售后）
        orderItem.setShopId(skuInfo.getShopId());
        orderItem.setShopName(skuInfo.getShopName());

        return orderItem;
    }

    /** 释放 ATP 占用（bizNo = 订单号/子单号）；幂等由 pms 侧条件更新兜底 */
    private void atpUnlock(String bizNo, List<AtpChangeDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Result<Boolean> result = skuStockFeignClient.unlock(bizNo, items);
        if (!Result.isSuccess(result)) {
            log.error("释放 ATP 失败：bizNo={}, items={}", bizNo, items.size());
        }
    }

    /** 成交扣减 ATP（含销量累加） */
    private void atpDeduct(String bizNo, List<AtpChangeDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Result<Boolean> result = skuStockFeignClient.deduct(bizNo, items);
        if (!Result.isSuccess(result)) {
            throw new BizException("扣减商品可售量失败：" + bizNo);
        }
    }

    /** 订单行 → ATP 变动明细（零售只动 ATP；订单行是 oms 自己的真值，不依赖库存侧的锁表） */
    private List<AtpChangeDTO> atpItemsOf(String orderNo) {
        OmsOrder order = getOrderByOrderNo(orderNo);
        if (order == null) {
            return List.of();
        }
        List<OmsOrderItem> items = orderItemService.list(new LambdaQueryWrapper<OmsOrderItem>()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getDeleted, 0));
        return items.stream().map(item -> {
            AtpChangeDTO atp = new AtpChangeDTO();
            atp.setSkuId(item.getSkuId());
            atp.setSpuId(item.getSpuId());
            atp.setQuantity(item.getQuantity());
            return atp;
        }).collect(Collectors.toList());
    }

    /**
     * 删除订单
     *
     * @param orderNo 订单编号
     * @return 是否删除成功
     */
    @Override
    @Transactional
    @OrderLog(detail = "删除订单")
    public boolean deleteOrder(String orderNo) {
        Long memberId = SecurityUtils.getMemberId();
        OmsOrder order = getOrderByOrderNo(orderNo);
        Assert.isTrue(null != order && memberId.equals(order.getMemberId()), "订单记录不存在");
        // 检查订单是否可以删除
        Assert.isTrue(OrderStatusEnum.canDelete(order.getStatus()), "当前订单状态不可删除");
        return this.removeById(order.getId());
    }

    private List<OrderItemDTO> getOrderItems(Long skuId, Long memberId, Integer count) {
        List<OrderItemDTO> orderItems;
        if (skuId != null) {  // 直接购买
            orderItems = new ArrayList<>();
            Result<SkuInfoDTO> skuInfoDTOResult = skuFeignClient.getSkuInfo(skuId);
            if (!Result.isSuccess(skuInfoDTOResult) || skuInfoDTOResult.getData() == null) {
                throw new BizException(OmsResultCode.GOODS_NOT_EXIST_OR_OFF_SHELF, (Object) ("skuId=" + skuId));
            }
            SkuInfoDTO skuInfoDTO = skuInfoDTOResult.getData();
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            orderItemDTO.setSkuId(skuId);
            BeanUtil.copyProperties(skuInfoDTO, orderItemDTO);
            orderItemDTO.setSkuId(skuInfoDTO.getId());
            orderItemDTO.setQuantity(count); // 直接购买商品的数量为1
            orderItemDTO.setProductAmount(skuInfoDTO.getRetailPrice().multiply(BigDecimal.valueOf(count)));

            // 商品销售单价
            orderItemDTO.setPrice(skuInfoDTO.getRetailPrice());
            orderItems.add(orderItemDTO);
        } else { // 购物车结算
            List<CartItemDto> cartItems = cartService.listCartItems(memberId);
            List<Long> skuIds = cartItems.stream().map(CartItemDto::getSkuId).toList();
            Result<List<SkuInfoDTO>> skuInfoListResult = skuFeignClient.getSkuInfoList(skuIds);
            List<SkuInfoDTO> skuInfoDTO = skuInfoListResult.getData();

            // 遍历 cartItems 把对象中的 属性 从 skuInfoDTO 填充
            cartItems.forEach(cartItem -> {
                SkuInfoDTO skuInfo = skuInfoDTO.stream().filter(s -> s.getId().equals(cartItem.getSkuId())).findFirst().orElse(null);
                if (null != skuInfo) {
                    cartItem.setSkuSn(skuInfo.getSkuSn());
                    cartItem.setSkuName(skuInfo.getSkuName());
                    cartItem.setPicUrl(skuInfo.getPicUrl());
                    cartItem.setSpuName(skuInfo.getSpuName());
                    cartItem.setStock(skuInfo.getStock());
                    cartItem.setPrice(skuInfo.getRetailPrice());
                    // 填充供应商ID、店铺信息和商品类型（用于拆单分组/展示）
                    cartItem.setSupplierId(skuInfo.getSupplierId());
                    cartItem.setShopId(skuInfo.getShopId());
                    cartItem.setShopName(skuInfo.getShopName());
                    cartItem.setSpuType(skuInfo.getSpuType());
                    cartItem.setFreightTemplateId(skuInfo.getFreightTemplateId());
                }
            });
            orderItems = cartItems.stream()
                    .filter(CartItemDto::getChecked)
                    .map(cartItem -> {
                        OrderItemDTO orderItemDTO = new OrderItemDTO();
                        BeanUtil.copyProperties(cartItem, orderItemDTO);
                        orderItemDTO.setQuantity(cartItem.getCount());
                        return orderItemDTO;
                    }).collect(Collectors.toList());
        }
        return orderItems;
    }

    /**
     * 保存订单收货地址信息
     *
     * @param order      订单信息
     * @param submitForm 订单提交表单
     * @return 收货地址信息
     */
    private MemberAddressDTO saveUserOrderAddress(OmsOrder order, OrderSubmitForm submitForm) {
        Result<List<MemberAddressDTO>> listResult = memberFeignClient.listMemberAddresses(order.getMemberId());
        Assert.isTrue(Result.isSuccess(listResult), "获取会员收货地址失败");

        List<MemberAddressDTO> addressList = listResult.getData();
        Long addressId = submitForm.getShippingAddress().getId();

        MemberAddressDTO memberAddress = addressList.stream()
                .filter(item -> item.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new BizException(OmsResultCode.ORDER_RECEIVE_ADDRESS_NOT_EXIST));


        OmsOrderAddress orderAddress = OmsOrderAddress.builder()
                .orderNo(order.getOrderNo())
                .orderId(order.getId())
                .receiveAddressId(memberAddress.getId())
                .receiveAddressType(ReceiveAddressTypeEnum.USER_RECEIVE)
                .userId(order.getMemberId())
                .userName(memberAddress.getConsigneeName())
                .userPhone(memberAddress.getConsigneeMobile())
                .areaId(memberAddress.getAreaId())
                .areaName(memberAddress.getAreaName())
                .cityId(memberAddress.getCityId())
                .cityName(memberAddress.getCityName())
                .provinceId(memberAddress.getProvinceId())
                .provinceName(memberAddress.getProvinceName())
                .address(memberAddress.getDetailAddress())
                .completeAddress(memberAddress.getCompleteAddress())
                .build();

        orderAddressService.save(orderAddress);

        return memberAddress;
    }

    /**
     * 检查订单商品是否在禁售区域内
     *
     * @param order         订单信息
     * @param memberAddress 会员地址
     * @param orderItems    订单商品列表
     * @param skuMap        SKU信息映射
     */
    private void checkForbiddenArea(OmsOrder order, MemberAddressDTO memberAddress, List<OrderSubmitForm.OrderItem> orderItems, Map<Long, SkuInfoDTO> skuMap) {
        // 仅当省份ID存在且有商品时才检查
        if (memberAddress.getProvinceId() == null || orderItems.isEmpty()) {
            return;
        }

        // 构建区域编码
        // 从省市区ID构建区域编码，用于后续检查
        String regionCode = buildRegionCode(memberAddress.getProvinceId(), memberAddress.getCityId(), memberAddress.getAreaId());
        if (regionCode == null) {
            log.warn("无法构建有效的区域编码，跳过禁售区域检查");
            return;
        }

        // 收集所有商品的禁售区域ID
        List<Long> forbiddenAreaIds = new ArrayList<>();
        Map<Long, List<SkuInfoDTO>> forbiddenAreaSkusMap = new HashMap<>(); // 禁售区域ID -> SKU列表

        // 第一步：收集所有的禁售区域ID和相关商品
        for (OrderSubmitForm.OrderItem item : orderItems) {
            Long skuId = item.getSkuId();
            SkuInfoDTO skuInfo = skuMap.get(skuId);
            if (skuInfo != null && skuInfo.getForbiddenAreaId() != null) {
                Long forbiddenAreaId = skuInfo.getForbiddenAreaId();
                // 添加到禁售区域ID列表
                if (!forbiddenAreaIds.contains(forbiddenAreaId)) {
                    forbiddenAreaIds.add(forbiddenAreaId);
                }

                // 将SKU添加到对应禁售区域的商品列表中
                forbiddenAreaSkusMap.computeIfAbsent(forbiddenAreaId, k -> new ArrayList<>()).add(skuInfo);
            }
        }

        if (forbiddenAreaIds.isEmpty()) {
            log.info("订单中没有关联禁售区域的商品，跳过禁售区域检查");
            return;
        }


        // 调用禁售区域服务，检查当前区域是否在禁售区域列表中
        Result<List<Long>> result = forbiddenAreaFeignClient.checkRegionInForbiddenAreas(regionCode, forbiddenAreaIds);

        if (!Result.isSuccess(result)) {
            log.error("调用禁售区域服务失败: {}", result.getMsg());
            // 服务调用失败时，为了安全，默认允许下单，由后续人工审核把关
            return;
        }

        List<Long> matchedForbiddenAreaIds = result.getData();
        if (matchedForbiddenAreaIds != null && !matchedForbiddenAreaIds.isEmpty()) {
            // 查找被禁售的商品名称，用于提示
            List<String> forbiddenProducts = new ArrayList<>();

            // 从匹配的禁售区域中获取相关商品
            for (Long forbiddenAreaId : matchedForbiddenAreaIds) {
                List<SkuInfoDTO> skuList = forbiddenAreaSkusMap.get(forbiddenAreaId);
                if (skuList != null) {
                    for (SkuInfoDTO skuInfo : skuList) {
                        forbiddenProducts.add(skuInfo.getSkuName());
                    }
                }
            }

            // 如果有被禁售的商品，抛出业务异常
            if (!forbiddenProducts.isEmpty()) {
                String productNames = String.join(", ", forbiddenProducts);
                throw new BizException(OmsResultCode.ORDER_REGION_NOT_SUPPORT, (Object) productNames);
            }
        }

        // 完成检查后更新订单相关信息
        this.updateById(order);
    }

    /**
     * 构建区域编码
     * 格式: 省ID_市ID_区ID，如果某级ID为空则忽略
     *
     * @param provinceId 省ID
     * @param cityId     市ID
     * @param areaId     区/县ID
     * @return 区域编码
     */
    private String buildRegionCode(Integer provinceId, Integer cityId, Integer areaId) {
        if (provinceId == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(provinceId);

        if (cityId != null) {
            sb.append(",").append(cityId);

            if (areaId != null) {
                sb.append(",").append(areaId);
            }
        }

        return sb.toString();
    }

    /**
     * 计算订单运费并分配到订单商品
     *
     * @param order         订单信息
     * @param orderItems    订单商品列表
     * @param memberAddress 收货地址
     */
    private void calculateAndSaveFreight(OmsOrder order, List<OrderSubmitForm.OrderItem> orderItems, MemberAddressDTO memberAddress) {
        if (orderItems == null || orderItems.isEmpty()) {
            order.setFreightAmount(BigDecimal.ZERO);
            this.updateById(order);
            return;
        }

        // 从订单商品中获取freightTemplateId并按运费模板分组
        // 注意：优先使用OrderSubmitForm.OrderItem中的freightTemplateId字段
        Map<Long, List<OrderSubmitForm.OrderItem>> templateItemsMap = new HashMap<>();

        for (OrderSubmitForm.OrderItem item : orderItems) {
            Long templateId = item.getFreightTemplateId();
            if (templateId != null) {
                // 将商品添加到对应模板的集合中
                templateItemsMap.computeIfAbsent(templateId, k -> new ArrayList<>()).add(item);
            }
        }

        // 获取所有用到的运费模板ID
        List<Long> templateIds = new ArrayList<>(templateItemsMap.keySet());
        if (templateIds.isEmpty()) {
            log.warn("订单商品未关联运费模板，使用默认运费计算");
            // 使用默认运费逻辑
            calculateDefaultFreight(order, orderItems, memberAddress);
            return;
        }

        // 总运费
        BigDecimal totalFreight = BigDecimal.ZERO;

        log.info("开始计算运费，涉及 {} 个不同的运费模板", templateIds.size());

        // 查询运费模板信息
        Result<List<FreightTemplateDTO>> templateResult = freightFeignClient.getFreightTemplatesByIds(templateIds);
        if (Result.isSuccess(templateResult) && templateResult.getData() != null) {
            Map<Long, FreightTemplateDTO> templateMap = templateResult.getData().stream()
                    .collect(Collectors.toMap(FreightTemplateDTO::getId, template -> template));

            // 准备批量计算运费的请求参数
            Map<Long, FreightCalcParam> batchCalcParamMap = new HashMap<>();

            // 按模板准备运费计算参数
            for (Map.Entry<Long, List<OrderSubmitForm.OrderItem>> entry : templateItemsMap.entrySet()) {
                Long templateId = entry.getKey();
                List<OrderSubmitForm.OrderItem> items = entry.getValue();

                FreightTemplateDTO template = templateMap.get(templateId);
                if (template == null) {
                    log.warn("运费模板不存在，ID: {}", templateId);
                    continue;
                }

                // 判断是否包邮
                if (Integer.valueOf(1).equals(template.getIsFreeShipping())) {
                    // 设置该模板下所有商品运费为0
                    items.forEach(item -> item.setFreightAmount(BigDecimal.ZERO));
                    continue; // 包邮模板不需要计算运费
                }

                // 计算该模板下的商品总数量、总金额
                int totalQuantity = items.stream().mapToInt(OrderSubmitForm.OrderItem::getQuantity).sum();
                BigDecimal totalAmount = items.stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 准备运费计算参数
                FreightCalcParam calcParam = new FreightCalcParam();
                calcParam.setTemplateId(templateId);
                calcParam.setProvinceCode(memberAddress.getProvinceId() != null ? memberAddress.getProvinceId().toString() : null);
                calcParam.setCityCode(memberAddress.getCityId() != null ? memberAddress.getCityId().toString() : null);
                calcParam.setDistrictCode(memberAddress.getAreaId() != null ? memberAddress.getAreaId().toString() : null);
                calcParam.setTotalQuantity(totalQuantity);
                calcParam.setTotalAmount(totalAmount);

                // 将参数添加到批量计算映射中
                batchCalcParamMap.put(templateId, calcParam);
            }

            // 如果有需要计算运费的模板
            if (!batchCalcParamMap.isEmpty()) {
                // 调用批量运费计算接口
                Result<Map<Long, BigDecimal>> batchFreightResult = freightFeignClient.batchCalculateFreight(batchCalcParamMap);

                if (Result.isSuccess(batchFreightResult) && batchFreightResult.getData() != null) {
                    Map<Long, BigDecimal> freightMap = batchFreightResult.getData();
                    log.info("批量运费计算结果: {}", freightMap);

                    // 处理每个模板的运费计算结果
                    for (Map.Entry<Long, BigDecimal> freightEntry : freightMap.entrySet()) {
                        Long templateId = freightEntry.getKey();
                        BigDecimal templateFreight = freightEntry.getValue();
                        List<OrderSubmitForm.OrderItem> items = templateItemsMap.get(templateId);

                        if (items == null) {
                            continue;
                        }

                        // 累加到总运费
                        totalFreight = totalFreight.add(templateFreight);

                        // 获取该模板下商品的总金额，用于按比例分配运费
                        BigDecimal itemsTotalAmount = items.stream()
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // 按比例分配运费到各个商品
                        if (templateFreight.compareTo(BigDecimal.ZERO) > 0) {
                            distributeFreightToTemplateItems(items, templateFreight, itemsTotalAmount);
                        } else {
                            // 如果运费为0，设置所有商品运费为0
                            items.forEach(item -> item.setFreightAmount(BigDecimal.ZERO));
                        }
                    }
                } else {
                    log.warn("批量计算运费失败: {}", batchFreightResult.getMsg());
                    // 批量计算失败时，回退到单个计算
                    calculateFreightIndividually(templateItemsMap, templateMap, memberAddress);
                }
            }
        } else {
            // 运费模板查询失败，使用默认运费计算
            log.warn("运费模板查询失败，使用默认运费计算");
            calculateDefaultFreight(order, orderItems, memberAddress);
            return;
        }

        // 更新订单总运费
        order.setFreightAmount(totalFreight);
        this.updateById(order);
    }

    /**
     * 分配运费到同一模板下的商品
     *
     * @param items           商品项
     * @param templateFreight 总运费
     * @param totalAmount     总金额
     */
    private void distributeFreightToTemplateItems(List<OrderSubmitForm.OrderItem> items, BigDecimal templateFreight, BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0 || templateFreight.compareTo(BigDecimal.ZERO) <= 0) {
            items.forEach(item -> item.setFreightAmount(BigDecimal.ZERO));
            return;
        }

        BigDecimal distributedFreight = BigDecimal.ZERO;
        for (int i = 0; i < items.size(); i++) {
            OrderSubmitForm.OrderItem item = items.get(i);
            BigDecimal itemValue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            // 最后一个商品获得剩余的运费金额，避免分配误差
            if (i == items.size() - 1) {
                item.setFreightAmount(templateFreight.subtract(distributedFreight));
            } else {
                // 按价值比例分配运费
                BigDecimal itemFreight = itemValue.multiply(templateFreight).divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP);
                item.setFreightAmount(itemFreight);
                distributedFreight = distributedFreight.add(itemFreight);
            }
        }
    }

    /**
     * 当批量计算运费失败时，回退到通过单个运费模板计算运费
     *
     * @param templateItemsMap 按运费模板分组的商品列表
     * @param templateMap      运费模板ID与模板对象的映射
     * @param memberAddress    收货地址
     * @return 总运费金额
     */
    private BigDecimal calculateFreightIndividually(
            Map<Long, List<OrderSubmitForm.OrderItem>> templateItemsMap,
            Map<Long, FreightTemplateDTO> templateMap,
            MemberAddressDTO memberAddress) {

        log.info("批量运费计算失败，回退到单个运费计算模式");
        BigDecimal totalFreight = BigDecimal.ZERO;

        // 逐个模板计算运费
        for (Map.Entry<Long, List<OrderSubmitForm.OrderItem>> entry : templateItemsMap.entrySet()) {
            Long templateId = entry.getKey();
            List<OrderSubmitForm.OrderItem> items = entry.getValue();

            FreightTemplateDTO template = templateMap.get(templateId);
            if (template == null) {
                log.warn("运费模板不存在，ID: {}", templateId);
                continue;
            }

            // 已经判断过是否包邮的模板已经在外层处理完毕，这里只需要计算非包邮的部分

            // 计算该模板下的商品总数量、总金额
            int totalQuantity = items.stream().mapToInt(OrderSubmitForm.OrderItem::getQuantity).sum();
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 调用运费计算服务
            FreightCalcParam calcParam = new FreightCalcParam();
            calcParam.setTemplateId(templateId);
            calcParam.setProvinceCode(memberAddress.getProvinceId() != null ? memberAddress.getProvinceId().toString() : null);
            calcParam.setCityCode(memberAddress.getCityId() != null ? memberAddress.getCityId().toString() : null);
            calcParam.setDistrictCode(memberAddress.getAreaId() != null ? memberAddress.getAreaId().toString() : null);
            calcParam.setTotalQuantity(totalQuantity);
            calcParam.setTotalAmount(totalAmount);

            try {
                Result<BigDecimal> freightResult = freightFeignClient.calculateFreight(calcParam);
                BigDecimal templateFreight = Result.isSuccess(freightResult) ? freightResult.getData() : BigDecimal.ZERO;

                // 累加到总运费
                totalFreight = totalFreight.add(templateFreight);

                // 按比例分配运费到各个商品
                if (templateFreight.compareTo(BigDecimal.ZERO) > 0) {
                    distributeFreightToTemplateItems(items, templateFreight, totalAmount);
                } else {
                    // 如果运费为0，设置所有商品运费为0
                    items.forEach(item -> item.setFreightAmount(BigDecimal.ZERO));
                }
            } catch (Exception e) {
                log.error("计算模板 {} 的运费时出错: {}", templateId, e.getMessage());
                // 单个模板计算失败不影响其他模板的计算
                items.forEach(item -> item.setFreightAmount(BigDecimal.ZERO));
            }
        }

        return totalFreight;
    }

    /**
     * 默认运费计算方法（当运费模板不可用时使用）
     *
     * @param order         订单信息
     * @param orderItems    订单商品列表
     * @param memberAddress 收货地址
     */
    private void calculateDefaultFreight(OmsOrder order, List<OrderSubmitForm.OrderItem> orderItems, MemberAddressDTO memberAddress) {
        // 计算商品总价值
        BigDecimal totalValue = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 从系统配置中获取默认运费和免运费阈值（而不是硬编码）
        BigDecimal baseFreight = BigDecimal.valueOf(1000); // 默认10元运费
        BigDecimal freeShippingThreshold = BigDecimal.valueOf(9900); // 默认99元免运费

        // 判断是否满足包邮条件
        boolean isFreeShipping = totalValue.compareTo(freeShippingThreshold) >= 0;

        // 最终运费金额
        BigDecimal finalFreight = isFreeShipping ? BigDecimal.ZERO : baseFreight;

        // 分配运费到商品
        if (finalFreight.compareTo(BigDecimal.ZERO) > 0) {
            distributeFreightToItems(orderItems, finalFreight, totalValue);
        } else {
            // 如果包邮，设置所有商品运费为0
            for (OrderSubmitForm.OrderItem item : orderItems) {
                item.setFreightAmount(BigDecimal.ZERO);
            }
        }

        // 更新订单总运费
        order.setFreightAmount(finalFreight);
        this.updateById(order);
    }

    /**
     * 将总运费按商品价格比例分配到每个商品
     *
     * @param items        商品项
     * @param totalFreight 总运费
     * @param totalAmount  总金额
     */
    private void distributeFreightToItems(List<OrderSubmitForm.OrderItem> items, BigDecimal totalFreight, BigDecimal totalAmount) {
        if (totalFreight.compareTo(BigDecimal.ZERO) == 0 || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            items.forEach(item -> item.setFreightAmount(BigDecimal.ZERO));
            return;
        }

        BigDecimal remainingFreight = totalFreight;

        // 按价格比例分配运费到每个商品
        for (int i = 0; i < items.size() - 1; i++) {
            OrderSubmitForm.OrderItem item = items.get(i);
            BigDecimal itemValue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            // 最后一个商品获得剩余的运费金额，避免分配误差
            if (i == items.size() - 1) {
                item.setFreightAmount(remainingFreight);
            } else {
                // 按价值比例分配运费
                BigDecimal itemFreight = itemValue.multiply(totalFreight).divide(totalAmount);
                item.setFreightAmount(itemFreight);
                remainingFreight = remainingFreight.subtract(itemFreight);
            }
        }
    }

    /**
     * 订单确认收货
     *
     * @param orderNo 订单编号
     * @return 是否确认收货成功
     */
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "订单确认收货")
    @Override
    public boolean confirmOrder(String orderNo) {
        return confirmOrder(orderNo, null);
    }

    @Override
    public boolean confirmOrder(String orderNo, String trackingNo) {
        // 1. 获取订单信息
        OmsOrder order = this.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(OmsResultCode.ORDER_NOT_FOUND);
        }

        // 2. 校验订单状态（幂等：非待收货状态直接返回）
        if (!OrderStatusEnum.DELIVERED.getValue().equals(order.getStatus())) {
            log.info("订单状态不可确认收货（幂等/状态不符）: orderNo={}, status={}", orderNo, order.getStatus());
            return false;
        }

        // 3. 定位本次确认收货的商品项：
        //    trackingNo 非空 → 该发货单关联商品项（物流签收路径）
        //    trackingNo 为空 → 订单全部发货单关联商品项（手动/自动整单确认路径）
        List<Long> deliveryIds;
        if (trackingNo != null) {
            OmsOrderDelivery delivery = orderDeliveryService.getOrderDeliveryByTrackingNo(trackingNo);
            deliveryIds = delivery != null ? List.of(delivery.getId()) : List.of();
        } else {
            deliveryIds = orderDeliveryService.lambdaQuery()
                    .eq(OmsOrderDelivery::getOrderId, order.getId())
                    .list().stream().map(OmsOrderDelivery::getId).collect(Collectors.toList());
        }

        List<OmsOrderItem> itemsToReceive = new ArrayList<>();
        if (!deliveryIds.isEmpty()) {
            List<Long> orderItemIds = omsOrderDeliveryItemMapper.selectList(
                            new LambdaQueryWrapper<OmsOrderDeliveryItem>()
                                    .in(OmsOrderDeliveryItem::getDeliveryId, deliveryIds))
                    .stream().map(OmsOrderDeliveryItem::getOrderItemId).distinct().collect(Collectors.toList());
            if (!orderItemIds.isEmpty()) {
                itemsToReceive = orderItemService.listByIds(orderItemIds);
            }
        }

        // 4. 商品项状态 → 已收货（2→3 守卫；无需发货订单（虚拟商品）允许 1→3）
        boolean needDelivery = order.getNeedDelivery() != null && order.getNeedDelivery();
        for (OmsOrderItem item : itemsToReceive) {
            boolean transformed;
            if (!needDelivery && OrderItemStatusEnum.PENDING_SHIP.getValue().equals(item.getStatus())) {
                // 虚拟商品无履约阶段：未发货直接确认收货
                item.setStatus(OrderItemStatusEnum.RECEIVED.getValue());
                transformed = true;
            } else {
                transformed = orderItemStateMachine.transform(item, OrderItemStatusEnum.RECEIVED);
            }
            if (transformed) {
                orderItemService.updateById(item);
            }
        }

        // 5. 物流状态更新
        if (needDelivery) {
            orderDeliveryService.updateDeliveryStatus(order.getId(), DeliveryStatusEnum.COMPLETE.getValue());
        }

        // 6. 订单状态由商品项聚合派生
        orderStatusAggregator.refreshOrderStatus(order.getId());
        log.info("订单确认收货完成: orderNo={}, trackingNo={}", orderNo, trackingNo);
        return true;
    }


    /**
     * 订单发货
     *
     * @param orderNo      订单编号
     * @param deliveryCode 物流公司编码
     * @param deliverySn   物流单号
     * @return 是否发货成功
     */
    @Override
    @Transactional
    @OrderLog(detail = "订单发货")
    public boolean deliverOrder(String orderNo, String deliveryCode, String deliverySn) {
        // 兼容调用（无供应商上下文）：取本订单商品项的供应商（单供应商订单语义）
        OmsOrder order = this.lambdaQuery()
                .eq(OmsOrder::getOrderNo, orderNo)
                .one();
        Long supplierId = null;
        if (order != null) {
            OmsOrderItem firstItem = orderItemService.lambdaQuery()
                    .eq(OmsOrderItem::getOrderId, order.getId())
                    .eq(OmsOrderItem::getDeleted, 0)
                    .last("LIMIT 1")
                    .one();
            supplierId = firstItem != null ? firstItem.getSupplierId() : null;
        }
        return deliverOrder(orderNo, supplierId, deliveryCode, deliverySn);
    }

    @Override
    @Transactional
    @OrderLog(detail = "订单发货")
    public boolean deliverOrder(String orderNo, Long supplierId, String deliveryCode, String deliverySn) {
        // 获取订单信息
        OmsOrder order = this.lambdaQuery()
                .eq(OmsOrder::getOrderNo, orderNo)
                .one();

        // 本供应商商品项：全量发货约束（全部待发货才可发货）
        List<OmsOrderItem> supplierItems = orderItemService.lambdaQuery()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getSupplierId, supplierId)
                .eq(OmsOrderItem::getDeleted, 0)
                .list();
        if (supplierItems.isEmpty()) {
            throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_SHIP);
        }
        for (OmsOrderItem item : supplierItems) {
            if (!OrderItemStatusEnum.PENDING_SHIP.getValue().equals(item.getStatus())) {
                log.warn("商品项非待发货状态，禁止发货: itemId={}, status={}", item.getId(), item.getStatus());
                throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_SHIP);
            }
        }

        // 保存发货单（按 (orderId, supplierId) 校验该供应商未发货）
        orderDeliveryService.saveOrderDelivery(order.getId(), supplierId, deliveryCode, deliverySn);
        OmsOrderDelivery delivery = orderDeliveryService.getOrderDeliveryBySupplier(order.getId(), supplierId);

        // 写发货单商品明细 + 商品项状态 → 已发货（同事务）
        for (OmsOrderItem item : supplierItems) {
            OmsOrderDeliveryItem deliveryItem = new OmsOrderDeliveryItem();
            deliveryItem.setDeliveryId(delivery.getId());
            deliveryItem.setOrderItemId(item.getId());
            deliveryItem.setQuantity(item.getQuantity());
            omsOrderDeliveryItemMapper.insert(deliveryItem);

            if (orderItemStateMachine.transform(item, OrderItemStatusEnum.SHIPPED)) {
                orderItemService.updateById(item);
            }
        }

        order.setDeliveryTime(LocalDateTime.now());
        this.updateById(order);

        // 订单状态由商品项聚合派生
        orderStatusAggregator.refreshOrderStatus(order.getId());

        //查询订单收货地址
        OmsOrderAddress orderAddress = orderAddressService.findByOrderId(order.getId(), ReceiveAddressTypeEnum.USER_RECEIVE);

        OrderPageVO orderPage = getOrderPageVO(orderNo);
        String skuName = orderPage.getOrderItems().get(0).getSkuName();

        // 通过支付服务获取渠道支付信息
        String paymentChannelNo = order.getPaymentChannelNo();

        LogisticsDTO logisticsDTO = LogisticsDTO.builder()
                .bizNo(orderNo)
                .logisticsType(LogisticsTypeEnum.DELIVERY.getValue())
                .trackingNo(deliverySn)
                .carrierCode(deliveryCode)
                .phone(orderAddress.getUserPhone())
                .spuName(skuName)
                .openid(order.getChannelUserId())
                .paymentChannelNo(paymentChannelNo)
                .payChannel(order.getPayChannel())
                .channelTradeNo(order.getChannelTradeNo())
                .build();

        // 异步调用物流订阅，不阻塞发货流程
        subscribeLogisticsAsync(logisticsDTO);

        return true;
    }

    /**
     * 异步订阅物流轨迹
     *
     * @param logisticsDTO 物流信息
     */
    @Async("logisticsAsyncExecutor")
    public void subscribeLogisticsAsync(LogisticsDTO logisticsDTO) {
        try {
            logisticsInfoService.subscribeLogistics(logisticsDTO);
        } catch (Exception e) {
            log.error("异步物流订阅失败，订单号：{}，物流单号：{}",
                    logisticsDTO.getBizNo(), logisticsDTO.getTrackingNo(), e);
        }
    }

    /**
     * 批量发货
     *
     * @param form 批量发货表单
     * @return 批量发货结果
     */
    @Override
    @Transactional
    @OrderLog(detail = "批量发货")
    public BatchDeliverResultVO batchDeliverOrders(BatchDeliverForm form) {
        log.info("批量发货开始，数量：{}", form.getItems().size());

        List<String> successOrderNos = new ArrayList<>();
        List<BatchDeliverResultVO.FailItem> failItems = new ArrayList<>();

        for (BatchDeliverForm.DeliverItem item : form.getItems()) {
            try {
                // 单个订单发货
                boolean success = deliverOrder(item.getOrderNo(), item.getDeliveryCode(), item.getDeliverySn());
                if (success) {
                    successOrderNos.add(item.getOrderNo());
                    log.info("批量发货成功：orderNo={}", item.getOrderNo());
                } else {
                    failItems.add(BatchDeliverResultVO.FailItem.builder()
                            .orderNo(item.getOrderNo())
                            .reason("发货失败")
                            .build());
                }
            } catch (BizException e) {
                failItems.add(BatchDeliverResultVO.FailItem.builder()
                        .orderNo(item.getOrderNo())
                        .reason(e.getMessage())
                        .build());
                log.warn("批量发货失败：orderNo={}，原因：{}", item.getOrderNo(), e.getMessage());
            } catch (Exception e) {
                failItems.add(BatchDeliverResultVO.FailItem.builder()
                        .orderNo(item.getOrderNo())
                        .reason("系统异常：" + e.getMessage())
                        .build());
                log.error("批量发货异常：orderNo={}", item.getOrderNo(), e);
            }
        }

        log.info("批量发货完成，成功：{}，失败：{}", successOrderNos.size(), failItems.size());

        return BatchDeliverResultVO.builder()
                .totalCount(form.getItems().size())
                .successCount(successOrderNos.size())
                .failCount(failItems.size())
                .successOrderNos(successOrderNos)
                .failItems(failItems)
                .build();
    }

    /**
     * 运营提交线下发货（系统自动审核通过）
     *
     * @param form 线下发货表单
     */
    @Override
    @Transactional
    @OrderLog(detail = "线下发货")
    public void submitOfflineDelivery(OfflineDeliveryForm form) {
        log.info("线下发货开始，orderId={}", form.getOrderId());

        // 获取订单信息
        OmsOrder order = this.getById(form.getOrderId());
        if (order == null) {
            throw new BizException(OmsResultCode.ORDER_NOT_FOUND);
        }

        // 校验订单状态是否为待发货
        if (!OrderStatusEnum.PAID.getValue().equals(order.getStatus())) {
            throw new BizException(OmsResultCode.ORDER_STATUS_NOT_PENDING_SHIP);
        }

        // 创建线下发货申请记录
        OmsOrderOfflineDelivery offlineDelivery = OmsOrderOfflineDelivery.builder()
                .orderId(form.getOrderId())
                .offlineProof(form.getOfflineProof())
                .offlineRemark(form.getOfflineRemark())
                .carrierCode(form.getCarrierCode())
                .carrierName(form.getCarrierName())
                .trackingNo(form.getTrackingNo())
                .applyTime(LocalDateTime.now())
                .auditStatus(1)  // 自动通过
                .auditor("SYSTEM")
                .auditTime(LocalDateTime.now())
                .auditRemark("系统自动审核")
                .build();
        offlineDeliveryMapper.insert(offlineDelivery);

        // 本订单商品项全量发货校验 + 状态流转（admin 代发：整单发货语义）
        List<OmsOrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getDeleted, 0)
                .list();
        for (OmsOrderItem item : orderItems) {
            if (!OrderItemStatusEnum.PENDING_SHIP.getValue().equals(item.getStatus())) {
                throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_SHIP);
            }
        }

        Long orderSupplierId = orderItems.stream()
                .map(OmsOrderItem::getSupplierId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        // 保存发货信息到物流表（线下发货按商品项供应商归属）
        OrderOfflineDeliverDTO deliverDTO = new OrderOfflineDeliverDTO();
        deliverDTO.setOrderId(form.getOrderId());
        deliverDTO.setSupplierId(orderSupplierId);
        deliverDTO.setDeliveryCode(form.getCarrierCode());
        deliverDTO.setDeliverySn(form.getTrackingNo());
        deliverDTO.setCarrierName(form.getCarrierName());
        orderDeliveryService.saveOrderOfflineDeliver(deliverDTO);
        OmsOrderDelivery delivery = orderDeliveryService.getOrderDeliveryBySupplier(order.getId(), orderSupplierId);

        // 写发货单商品明细 + 商品项状态 → 已发货（同事务）
        for (OmsOrderItem item : orderItems) {
            OmsOrderDeliveryItem deliveryItem = new OmsOrderDeliveryItem();
            deliveryItem.setDeliveryId(delivery.getId());
            deliveryItem.setOrderItemId(item.getId());
            deliveryItem.setQuantity(item.getQuantity());
            omsOrderDeliveryItemMapper.insert(deliveryItem);

            if (orderItemStateMachine.transform(item, OrderItemStatusEnum.SHIPPED)) {
                orderItemService.updateById(item);
            }
        }

        order.setDeliveryTime(LocalDateTime.now());
        this.updateById(order);

        // 订单状态由商品项聚合派生
        orderStatusAggregator.refreshOrderStatus(order.getId());

        log.info("线下发货完成，orderId={}", form.getOrderId());
    }

    /**
     * 获取线下发货申请详情
     *
     * @param orderId 订单ID
     * @return 线下发货申请详情
     */
    @Override
    public OfflineDeliveryDetailVO getOfflineDeliveryDetail(Long orderId) {
        OmsOrderOfflineDelivery offlineDelivery = offlineDeliveryMapper.selectOne(
                new LambdaQueryWrapper<OmsOrderOfflineDelivery>()
                        .eq(OmsOrderOfflineDelivery::getOrderId, orderId)
                        .orderByDesc(OmsOrderOfflineDelivery::getCreateTime)
                        .last("LIMIT 1")
        );

        if (offlineDelivery == null) {
            return null;
        }

        OmsOrder order = this.getById(orderId);

        OfflineDeliveryDetailVO vo = new OfflineDeliveryDetailVO();
        vo.setId(offlineDelivery.getId());
        vo.setOrderId(offlineDelivery.getOrderId());
        vo.setOrderNo(order != null ? order.getOrderNo() : null);
        vo.setOfflineProof(offlineDelivery.getOfflineProof());
        vo.setOfflineRemark(offlineDelivery.getOfflineRemark());
        vo.setCarrierCode(offlineDelivery.getCarrierCode());
        vo.setCarrierName(offlineDelivery.getCarrierName());
        vo.setTrackingNo(offlineDelivery.getTrackingNo());
        vo.setApplyTime(offlineDelivery.getApplyTime());
        vo.setAuditStatus(offlineDelivery.getAuditStatus());
        vo.setAuditStatusLabel(offlineDelivery.getAuditStatus() != null && offlineDelivery.getAuditStatus() == 1 ? "已通过" : "未知");
        vo.setAuditor(offlineDelivery.getAuditor());
        vo.setAuditTime(offlineDelivery.getAuditTime());
        vo.setAuditRemark(offlineDelivery.getAuditRemark());

        return vo;
    }

    @Override
    public OmsOrderPriceVO getOrderPriceSum(OrderPageQuery queryParams) {
        return baseMapper.getOrderPriceSum(queryParams);
    }

    @Override
    public OmsOrderDetailVO getOrderDetail(Long orderId) {
        // 查询订单基本信息
        OmsOrder order = this.getById(orderId);
        if (order == null) {
            throw new BizException(OmsResultCode.ORDER_NOT_FOUND);
        }

        // 构建订单详情VO
        OmsOrderDetailVO detailVO = new OmsOrderDetailVO();
        detailVO.setId(order.getId());
        detailVO.setOrderNo(order.getOrderNo());
        detailVO.setPaymentNo(order.getPaymentNo());
        detailVO.setOrderType(order.getOrderType());
        detailVO.setOrderTypeLabel(IBaseEnum.getLabelByValue(order.getOrderType(), OrderTypeEnum.class));
        detailVO.setTotalAmount(order.getTotalAmount());
        detailVO.setTotalRetailPrice(order.getTotalRetailPrice());
        detailVO.setFreightAmount(order.getFreightAmount());
        detailVO.setCouponAmount(order.getCouponAmount());
        detailVO.setPaymentAmount(order.getPaymentAmount());
        detailVO.setPayChannel(order.getPayChannel());
        detailVO.setPayChannelLabel(IBaseEnum.getLabelByValue(order.getPayChannel(), PayChannelEnum.class));
        detailVO.setStatus(order.getStatus());
        detailVO.setStatusLabel(IBaseEnum.getLabelByValue(order.getStatus(), OrderStatusEnum.class));
        detailVO.setTotalQuantity(order.getTotalQuantity());
        detailVO.setCreateTime(order.getCreateTime());
        detailVO.setPaymentTime(order.getPaymentTime());
        detailVO.setSourceLabel(IBaseEnum.getLabelByValue(order.getSource(), OrderSourceEnum.class));
        detailVO.setRemark(order.getRemark());
        detailVO.setMemberId(order.getMemberId());
        detailVO.setShopId(order.getShopId());
        detailVO.setShopName(order.getShopName());
        detailVO.setNeedDelivery(order.getNeedDelivery());
        detailVO.setUsePointsAmount(order.getUsePointsAmount());
        detailVO.setPointsDeductionAmount(order.getPointsDeductionAmount());
        detailVO.setPaymentDiscountAmount(order.getPaymentDiscountAmount());
        detailVO.setReceiveTime(order.getReceiveTime());

        // 查询线下支付凭证信息
        if (order.getPaymentNo() != null) {
            try {
                Result<OfflinePayDetailVO> offlineResult =
                        payFeignClient.getOfflinePayDetail(order.getPaymentNo());
                if (Result.isSuccess(offlineResult) && offlineResult.getData() != null
                        && offlineResult.getData().getAuditStatus() != null) {
                    detailVO.setOfflinePayAuditStatus(offlineResult.getData().getAuditStatus());
                    detailVO.setPayChannel(PayChannelEnum.OFFLINE_PAY.getValue());
                    detailVO.setOfflinePayAuditRemark(offlineResult.getData().getAuditRemark());
                    detailVO.setOfflinePayAuditTime(offlineResult.getData().getAuditTime());
                    detailVO.setOfflinePayAuditor(offlineResult.getData().getAuditor());
                    String voucherJson = offlineResult.getData().getVoucherUrls();
                    if (voucherJson != null && !voucherJson.isEmpty()) {
                        try {
                            detailVO.setOfflinePayVouchers(
                                    cn.hutool.json.JSONUtil.toList(voucherJson, String.class));
                        } catch (Exception e) {
                            log.warn("解析线下支付凭证URL失败: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("查询线下支付凭证信息失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }

        // 查询收货地址信息
        OmsOrderAddress orderAddress = orderAddressService.findByOrderId(order.getId(), ReceiveAddressTypeEnum.USER_RECEIVE);
        if (orderAddress != null) {
            detailVO.setReceiverName(orderAddress.getUserName());
            detailVO.setReceiverPhone(orderAddress.getUserPhone());
            detailVO.setReceiverProvince(orderAddress.getProvinceName());
            detailVO.setReceiverCity(orderAddress.getCityName());
            detailVO.setReceiverDistrict(orderAddress.getAreaName());
            detailVO.setReceiverAddress(orderAddress.getAddress());
            detailVO.setReceiverCompleteAddress(orderAddress.getProvinceName() +
                    orderAddress.getCityName() +
                    orderAddress.getAreaName() +
                    orderAddress.getAddress());
        }

        // 查询物流信息
        OmsOrderDelivery orderDelivery = orderDeliveryService.getOrderDelivery(order.getId());
        if (orderDelivery != null) {
            detailVO.setTrackingNo(orderDelivery.getTrackingNo());
            detailVO.setCarrierName(orderDelivery.getCarrierName());
            detailVO.setDeliveryTime(orderDelivery.getDeliveryTime());
            detailVO.setDeliveryType(orderDelivery.getDeliveryType());
            // 如果是线下发货，获取线下发货备注
            if (orderDelivery.getDeliveryType() != null && orderDelivery.getDeliveryType() == 2) {
                OmsOrderOfflineDelivery offlineDelivery = offlineDeliveryMapper.selectOne(
                        new LambdaQueryWrapper<OmsOrderOfflineDelivery>()
                                .eq(OmsOrderOfflineDelivery::getOrderId, order.getId())
                                .orderByDesc(OmsOrderOfflineDelivery::getCreateTime)
                                .last("LIMIT 1")
                );
                if (offlineDelivery != null) {
                    detailVO.setOfflineRemark(offlineDelivery.getOfflineRemark());
                }
            }
        }

        // 查询下单人信息
        if (order.getMemberId() != null) {
            try {
                Result<MemberInfoDTO> memberResult = memberFeignClient.info(order.getMemberId());
                if (Result.isSuccess(memberResult) && memberResult.getData() != null) {
                    detailVO.setBuyerPhone(memberResult.getData().getMobile());
                }
            } catch (Exception e) {
                log.warn("获取会员信息失败: {}", e.getMessage());
            }
        }

        // 查询订单商品列表
        List<OmsOrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OmsOrderItem>()
                .eq(OmsOrderItem::getOrderId, order.getId()));
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OmsOrderDetailVO.OrderItemVO> itemVOList = new ArrayList<>();
            for (OmsOrderItem item : orderItems) {
                OmsOrderDetailVO.OrderItemVO itemVO = new OmsOrderDetailVO.OrderItemVO();
                itemVO.setId(item.getId());
                itemVO.setShopId(item.getShopId());
                itemVO.setShopName(item.getShopName());
                itemVO.setSpuId(item.getSpuId());
                itemVO.setSpuName(item.getSpuName());
                itemVO.setSkuId(item.getSkuId());
                itemVO.setSkuSn(item.getSkuSn());
                itemVO.setSkuName(item.getSkuName());
                itemVO.setPicUrl(item.getPicUrl());
                itemVO.setPrice(item.getPrice());
                itemVO.setRetailPrice(item.getRetailPrice());
                itemVO.setSupplyPrice(item.getSupplyPrice());
                itemVO.setQuantity(item.getQuantity());
                itemVO.setTotalAmount(item.getTotalAmount());
                itemVO.setFreightAmount(item.getFreightAmount());
                itemVO.setStatus(item.getStatus());
                itemVO.setRefundStatus(item.getRefundStatus());
                itemVO.setRefundQuantity(item.getRefundQuantity());
                itemVO.setRefundAmount(item.getRefundAmount());
                itemVOList.add(itemVO);
            }

            detailVO.setOrderItems(itemVOList);
            // 订单级退款总额由商品行聚合（订单表已无 refund_amount 字段）
            BigDecimal orderRefundAmount = orderItems.stream()
                    .map(item -> nvl(item.getRefundAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detailVO.setRefundAmount(orderRefundAmount);

            // 计算供货总价
            BigDecimal totalSupplyPrice = itemVOList.stream()
                    .map(item -> {
                        BigDecimal supplyPrice = item.getSupplyPrice() != null ? item.getSupplyPrice() : BigDecimal.ZERO;
                        Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                        return supplyPrice.multiply(BigDecimal.valueOf(quantity));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detailVO.setTotalSupplyPrice(totalSupplyPrice);
        }

        return detailVO;
    }


    /**
     * 导出订单数据
     *
     * @param queryParams 查询参数
     * @param response    HTTP响应
     */
    @Override
    public void exportOrders(OrderPageQuery queryParams, HttpServletResponse response) throws IOException {
        try {
            log.info("开始导出订单数据，查询参数：{}", queryParams);

            // 1. 查询订单数据
            List<OrderBO> orderList = this.getBaseMapper().listOrderPages(queryParams);
            if (orderList == null || orderList.isEmpty()) {
                throw new BizException(OmsResultCode.NO_EXPORTABLE_ORDER_DATA);
            }

            // 2. 转换为导出VO
            List<OmsOrderSupplierExportVO> supplierExportList = new ArrayList<>();
            List<OmsOrderExportVO> adminExportList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            // 判断是否是商家端导出
            boolean isSupplierExport = SecurityUtils.isSupplier();

            for (OrderBO orderBO : orderList) {
                // 每个订单的商品分别导出一行
                if (orderBO.getOrderItems() != null) {
                    for (OrderBO.OrderItem item : orderBO.getOrderItems()) {
                        // 根据导出类型创建不同的VO
                        if (isSupplierExport) {
                            // 商家端导出
                            OmsOrderSupplierExportVO exportVO = new OmsOrderSupplierExportVO();
                            exportVO.setOrderNo(orderBO.getOrderNo());
                            exportVO.setOrderTypeLabel(IBaseEnum.getLabelByValue(orderBO.getOrderType(), OrderTypeEnum.class));
                            exportVO.setStatusDesc(IBaseEnum.getLabelByValue(orderBO.getStatus(), OrderStatusEnum.class));
                            exportVO.setSpuId(item.getSpuId());
                            exportVO.setSkuId(item.getSkuId());
                            exportVO.setSupplyPrice(item.getSupplyPrice());
                            exportVO.setRetailPrice(item.getRetailPrice());
                            exportVO.setSkuName(item.getSkuName());
                            exportVO.setQuantity(item.getQuantity());
                            exportVO.setOrderItemTotalAmount(item.getTotalAmount());
                            exportVO.setFreightAmount(orderBO.getFreightAmount());
                            // 收件人信息
                            exportVO.setReceiverName(orderBO.getReceiverName());
                            exportVO.setReceiverPhone(orderBO.getReceiverPhone());
                            exportVO.setReceiverProvince(orderBO.getReceiverProvince());
                            exportVO.setReceiverCity(orderBO.getReceiverCity());
                            exportVO.setReceiverDistrict(orderBO.getReceiverDistrict());
                            exportVO.setReceiverCompleteAddress(orderBO.getReceiverCompleteAddress());
                            // 物流信息
                            exportVO.setCarrierName(orderBO.getCarrierName());
                            exportVO.setTrackingNo(orderBO.getTrackingNo());
                            exportVO.setPaymentChannelNo(orderBO.getPaymentChannelNo());
                            exportVO.setChannelTradeNo(orderBO.getChannelTradeNo());
                            // 时间
                            if (orderBO.getCreateTime() != null) {
                                exportVO.setCreateTime(orderBO.getCreateTime().format(formatter));
                            }
                            if (orderBO.getPaymentTime() != null) {
                                exportVO.setPaymentTime(orderBO.getPaymentTime().format(formatter));
                            }
                            if (orderBO.getDeliveryTime() != null) {
                                exportVO.setDeliveryTime(orderBO.getDeliveryTime().format(formatter));
                            }
                            supplierExportList.add(exportVO);
                        } else {
                            // 运营后台导出
                            OmsOrderExportVO exportVO = new OmsOrderExportVO();
                            exportVO.setOrderNo(orderBO.getOrderNo());
                            exportVO.setOrderTypeLabel(IBaseEnum.getLabelByValue(orderBO.getOrderType(), OrderTypeEnum.class));
                            exportVO.setTotalAmount(orderBO.getTotalAmount());
                            exportVO.setFreightAmount(orderBO.getFreightAmount());
                            exportVO.setStatusDesc(IBaseEnum.getLabelByValue(orderBO.getStatus(), OrderStatusEnum.class));
                            exportVO.setPayChannel(IBaseEnum.getLabelByValue(orderBO.getPayChannel(), PayChannelEnum.class));
                            exportVO.setSupplierId(item.getSupplierId() != null ? item.getSupplierId().toString() : null);
                            exportVO.setOrderItemId(item.getId());
                            exportVO.setSpuId(item.getSpuId());
                            exportVO.setSkuId(item.getSkuId());
                            exportVO.setSupplyPrice(item.getSupplyPrice());
                            exportVO.setRetailPrice(item.getRetailPrice());
                            exportVO.setSkuName(item.getSkuName());
                            exportVO.setQuantity(item.getQuantity());
                            exportVO.setPrice(item.getPrice());
                            exportVO.setOrderItemTotalAmount(item.getTotalAmount());
                            exportVO.setRemark(orderBO.getRemark());
                            // 收件人信息
                            exportVO.setReceiverName(orderBO.getReceiverName());
                            exportVO.setReceiverPhone(orderBO.getReceiverPhone());
                            exportVO.setReceiverProvince(orderBO.getReceiverProvince());
                            exportVO.setReceiverCity(orderBO.getReceiverCity());
                            exportVO.setReceiverDistrict(orderBO.getReceiverDistrict());
                            exportVO.setReceiverCompleteAddress(orderBO.getReceiverCompleteAddress());
                            // 下单人信息
                            exportVO.setBuyerPhone(orderBO.getBuyerPhone());
                            // 物流信息
                            exportVO.setCarrierName(orderBO.getCarrierName());
                            exportVO.setTrackingNo(orderBO.getTrackingNo());
                            exportVO.setPaymentChannelNo(orderBO.getPaymentChannelNo());
                            exportVO.setChannelTradeNo(orderBO.getChannelTradeNo());
                            // 抵扣信息
                            exportVO.setPointsDeductionAmount(orderBO.getPointsDeductionAmount());
                            // 时间
                            if (orderBO.getCreateTime() != null) {
                                exportVO.setCreateTime(orderBO.getCreateTime().format(formatter));
                            }
                            if (orderBO.getPaymentTime() != null) {
                                exportVO.setPaymentTime(orderBO.getPaymentTime().format(formatter));
                            }
                            if (orderBO.getDeliveryTime() != null) {
                                exportVO.setDeliveryTime(orderBO.getDeliveryTime().format(formatter));
                            }
                            if (orderBO.getReceiveTime() != null) {
                                exportVO.setReceiveTime(orderBO.getReceiveTime().format(formatter));
                            }
                            adminExportList.add(exportVO);
                        }
                    }
                }
            }

            // 设置响应头
            String fileName = isSupplierExport ? "商家订单列表.xlsx" : "订单列表.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20"); // 替换空格为 %20

            // 兼容新旧浏览器
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");

            // 根据导出类型使用不同的VO类
            if (isSupplierExport) {
                EasyExcel.write(response.getOutputStream(), OmsOrderSupplierExportVO.class)
                        .sheet("商家订单列表")
                        .doWrite(supplierExportList);
            } else {
                EasyExcel.write(response.getOutputStream(), OmsOrderExportVO.class)
                        .sheet("订单列表")
                        .doWrite(adminExportList);
            }

        } catch (Exception e) {
            log.error("订单导出失败", e);
            throw new BizException(OmsResultCode.ORDER_EXPORT_FAILED, (Object) e.getMessage());
        }
    }

    /**
     * Admin-订单分页列表
     *
     * @param queryParams {@link OrderPageQuery}
     * @return {@link OmsOrderPageVO}
     */
    @Override
    public IPage<OmsOrderPageVO> getAdminOrderPage(OrderPageQuery queryParams) {
        Page<OrderBO> boPage = this.baseMapper.getOrderPage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams);
        Page<OmsOrderPageVO> voPage = orderConverter.toVoPage(boPage);

        voPage.getRecords().stream().forEach(orderPageVO -> {
            if (orderPageVO.getNeedDelivery()) {
                OmsOrderDelivery orderDelivery = orderDeliveryService.getOrderDelivery(orderPageVO.getId());
                if (orderDelivery != null) {
                    orderPageVO.setTrackingNo(orderDelivery.getTrackingNo());
                    orderPageVO.setCarrierName(orderDelivery.getCarrierName());
                    orderPageVO.setDeliveryType(orderDelivery.getDeliveryType());
                    // 如果是线下发货，获取线下发货备注
                    if (orderDelivery.getDeliveryType() != null && orderDelivery.getDeliveryType() == 2) {
                        OmsOrderOfflineDelivery offlineDelivery = offlineDeliveryMapper.selectOne(
                                new LambdaQueryWrapper<OmsOrderOfflineDelivery>()
                                        .eq(OmsOrderOfflineDelivery::getOrderId, orderPageVO.getId())
                                        .orderByDesc(OmsOrderOfflineDelivery::getCreateTime)
                                        .last("LIMIT 1")
                        );
                        if (offlineDelivery != null) {
                            orderPageVO.setOfflineRemark(offlineDelivery.getOfflineRemark());
                        }
                    }
                }
            }

        });
        return voPage;

    }

    /**
     * 处理退款回调
     * 统一处理支付平台的退款结果通知，内部根据退款类型分发到不同的处理流程
     * 已移除对支付记录的处理，只关注订单状态管理
     *
     * @param refundResultDTO 退款回调数据
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefundCallback(RefundResultDTO refundResultDTO) {
        log.info("开始处理退款回调，退款单号: {}, 订单号: {}",
                refundResultDTO.getRefundNo(), refundResultDTO.getOrderNo());

        try {
            // 统一走售后退款处理逻辑
            return handleAfterSaleRefund(refundResultDTO);
        } catch (Exception e) {
            log.error("处理退款回调异常，退款单号: {}", refundResultDTO.getRefundNo(), e);
            return false;
        }
    }

    /**
     * 处理售后退款
     * 内部方法，处理售后退款的结果
     * 包括更新售后单状态和相应订单状态
     *
     * @param refundResultDTO 退款回调数据
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAfterSaleRefund(RefundResultDTO refundResultDTO) {
        log.info("开始处理售后退款，退款单号: {}, 订单号: {}",
                refundResultDTO.getRefundNo(), refundResultDTO.getOrderNo());

        // 本次退款涉及的商品项
        List<OmsOrderAfterSaleItem> refundAfterSaleItems = new ArrayList<>();

        try {
            // 1.1 通过售后单号查询售后单信息
            LambdaQueryWrapper<OmsOrderAfterSale> afterSaleByNoWrapper = new LambdaQueryWrapper<>();
            afterSaleByNoWrapper.eq(OmsOrderAfterSale::getAfterSaleNo, refundResultDTO.getApplyNo());
            OmsOrderAfterSale afterSale = orderAfterSaleService.getOne(afterSaleByNoWrapper);
            log.info("通过售后单号查询售后单, 售后单号: {}", refundResultDTO.getApplyNo());
            if (afterSale != null) {
                log.info("处理售后单商品退款状态，售后单ID: {}, 订单号: {}", afterSale.getId(), afterSale.getOrderNo());

                // 获取售后单包含的商品明细
                List<OmsOrderAfterSaleItem> afterSaleItems = orderAfterSaleItemService.getByAfterSaleId(afterSale.getId());
                refundAfterSaleItems = afterSaleItems;

                if (afterSaleItems != null && !afterSaleItems.isEmpty()) {
                    log.info("售后单包含 {} 个商品项目", afterSaleItems.size());

                    // 仅更新售后单中包含的订单商品行
                    for (OmsOrderAfterSaleItem afterSaleItem : afterSaleItems) {
                        Long orderItemId = afterSaleItem.getOrderItemId();
                        if (orderItemId != null) {
                            boolean updated = orderItemService.updateItemFullyRefunded(orderItemId);
                            if (updated) {
                                log.info("售后退款成功，更新订单商品退款状态：afterSaleId={}, orderItemId={}, skuName={}, 退款金额={}元",
                                        afterSale.getId(), orderItemId, afterSaleItem.getSkuName(), afterSaleItem.getTotalAmount());
                            } else {
                                log.warn("更新订单商品退款状态失败: orderItemId={}, skuName={}",
                                        orderItemId, afterSaleItem.getSkuName());
                            }
                            // 商品项履约状态：全额退款→5；部分退款→按售后前快照恢复
                            updateItemStatusAfterRefund(orderItemId, afterSaleItem.getPreStatus());
                        } else {
                            log.warn("售后商品项缺失orderItemId关联: afterSaleItemId={}", afterSaleItem.getId());
                        }
                    }
                } else {
                    log.warn("售后单无商品项目信息: afterSaleId={}, orderNo={}", afterSale.getId(), afterSale.getOrderNo());
                }
            } else {
                log.warn("未找到对应的售后单信息，退款单号: {}", refundResultDTO.getRefundNo());
            }

            // 2. 获取订单信息
            OmsOrder order = this.getOrderByOrderNo(refundResultDTO.getOrderNo());
            if (order == null) {
                log.error("订单不存在，无法更新订单状态，订单号: {}", refundResultDTO.getOrderNo());
                return true; // 售后处理成功，订单不存在不影响结果
            }

            // 3. 聚合刷新订单状态（全部商品项已退款 → 订单 REFUNDED；部分退款 → 恢复履约态）
            orderStatusAggregator.refreshOrderStatus(order.getId());
            OmsOrder refreshedOrder = this.getById(order.getId());
            boolean allRefunded = refreshedOrder != null
                    && OrderStatusEnum.REFUNDED.getValue().equals(refreshedOrder.getStatus());
            if (allRefunded) {
                // 订单已全额退款，补充退款时间
                refreshedOrder.setRefundTime(LocalDateTime.now());
                this.updateById(refreshedOrder);
                log.info("售后退款完成，所有商品已退款，更新订单状态为已退款，订单号: {}", order.getOrderNo());

                // 订单全额退款后退还已扣积分。
                try {
                    // 退回积分（已扣减 → 可用）
                    if (order.getUsePointsAmount() != null && order.getUsePointsAmount().compareTo(BigDecimal.ZERO) > 0) {
                        AssetOperateRequest refundRequest = new AssetOperateRequest();
                        refundRequest.setMemberId(order.getMemberId());
                        refundRequest.setAssetCode(AssetCodeEnum.POINT.getCode());
                        refundRequest.setNum(order.getUsePointsAmount());
                        refundRequest.setOperateType(AssetOperateSceneEnum.ORDER_REFUND_REFUND);
                        refundRequest.setBizNo(refundResultDTO.getRefundNo());
                        refundRequest.setRemark("售后退款退回积分");

                        Result<Boolean> refundResult = memberAssetsOperateFeignClient.operateAsset(refundRequest);
                        if (!Result.isSuccess(refundResult) || !refundResult.getData()) {
                            log.error("积分退回失败，退款单号: {}, 订单号: {}, 数量: {}",
                                    refundResultDTO.getRefundNo(), order.getOrderNo(), order.getUsePointsAmount());
                        } else {
                            log.info("积分退回成功: memberId={}, orderNo={}, refundNo={}, amount={}",
                                    order.getMemberId(), order.getOrderNo(), refundResultDTO.getRefundNo(), order.getUsePointsAmount());
                        }
                    }
                } catch (Exception e) {
                    log.error("积分退回处理异常，退款单号: {}, 订单号: {}, 错误: {}",
                            refundResultDTO.getRefundNo(), order.getOrderNo(), e.getMessage());
                }
            } else {
                log.info("售后退款完成，部分商品已退款，订单号: {}, 退款金额: {}元",
                        order.getOrderNo(), refundResultDTO.getRefundAmount());
            }
            afterSale.setRefundSuccessTime(LocalDateTime.now());
            afterSale.setStatus(AfterSaleStatusEnum.REFUND_SUCCESS.getValue());
            orderAfterSaleService.updateById(afterSale);

            return true;
        } catch (Exception e) {            log.error("处理售后退款异常，退款单号: {}", refundResultDTO.getRefundNo(), e);
            throw new RuntimeException("处理售后退款异常", e);
        }
    }

    /**
     * 订单确认时计算运费
     *
     * @param orderItems 订单商品列表
     * @param address    会员收货地址
     * @return 运费金额(单位：元)
     */
    private BigDecimal calculateFreightForConfirm(List<OrderItemDTO> orderItems, MemberAddressDTO address) {
        if (orderItems == null || orderItems.isEmpty() || address == null) {
            return BigDecimal.ZERO;
        }

        try {
            // 按运费模板ID分组商品
            Map<Long, List<OrderItemDTO>> templateItemsMap = orderItems.stream()
                    .filter(item -> item.getFreightTemplateId() != null)
                    .collect(Collectors.groupingBy(OrderItemDTO::getFreightTemplateId));

            // 如果没有运费模板，使用默认运费
            if (templateItemsMap.isEmpty()) {
                log.warn("订单商品未关联运费模板，使用默认运费计算");
                return calculateDefaultFreight(orderItems);
            }

            // 总运费
            BigDecimal totalFreight = BigDecimal.ZERO;

            // 获取所有运费模板ID
            List<Long> templateIds = new ArrayList<>(templateItemsMap.keySet());
            log.info("开始计算运费，涉及 {} 个不同的运费模板", templateIds.size());

            // 查询运费模板信息
            Result<List<FreightTemplateDTO>> templateResult = freightFeignClient.getFreightTemplatesByIds(templateIds);
            if (!Result.isSuccess(templateResult) || templateResult.getData() == null) {
                log.warn("获取运费模板信息失败，使用默认运费计算");
                return calculateDefaultFreight(orderItems);
            }

            // 模板ID -> 模板信息映射
            Map<Long, FreightTemplateDTO> templateMap = templateResult.getData().stream()
                    .collect(Collectors.toMap(FreightTemplateDTO::getId, template -> template));

            // 准备批量计算运费的请求参数
            Map<Long, FreightCalcParam> batchCalcParamMap = new HashMap<>();

            // 按模板准备运费计算参数
            for (Map.Entry<Long, List<OrderItemDTO>> entry : templateItemsMap.entrySet()) {
                Long templateId = entry.getKey();
                List<OrderItemDTO> items = entry.getValue();

                FreightTemplateDTO template = templateMap.get(templateId);
                if (template == null) {
                    log.warn("运费模板不存在，ID: {}", templateId);
                    continue;
                }

                // 判断是否包邮
                if (Integer.valueOf(1).equals(template.getIsFreeShipping())) {
                    // 包邮模板不需要计算运费
                    continue;
                }

                // 计算该模板下的商品总数量、总金额
                int totalQuantity = items.stream().mapToInt(OrderItemDTO::getQuantity).sum();
                BigDecimal totalAmount = items.stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 准备运费计算参数
                FreightCalcParam calcParam = new FreightCalcParam();
                calcParam.setTemplateId(templateId);
                calcParam.setProvinceCode(address.getProvinceId() != null ? address.getProvinceId().toString() : null);
                calcParam.setCityCode(address.getCityId() != null ? address.getCityId().toString() : null);
                calcParam.setDistrictCode(address.getAreaId() != null ? address.getAreaId().toString() : null);
                calcParam.setTotalQuantity(totalQuantity);
                calcParam.setTotalAmount(totalAmount);

                // 将参数添加到批量计算映射中
                batchCalcParamMap.put(templateId, calcParam);
            }

            // 如果有需要计算运费的模板
            if (!batchCalcParamMap.isEmpty()) {
                // 调用批量运费计算接口
                Result<Map<Long, BigDecimal>> batchFreightResult = freightFeignClient.batchCalculateFreight(batchCalcParamMap);

                if (Result.isSuccess(batchFreightResult) && batchFreightResult.getData() != null) {
                    Map<Long, BigDecimal> freightMap = batchFreightResult.getData();
                    log.info("批量运费计算结果: {}", freightMap);

                    // 累加各模板运费
                    for (BigDecimal freight : freightMap.values()) {
                        totalFreight = totalFreight.add(freight);
                    }
                } else {
                    log.warn("调用批量运费计算接口失败，使用默认运费计算");
                    return calculateDefaultFreight(orderItems);
                }
            }

            return totalFreight;

        } catch (Exception e) {
            log.error("计算运费异常", e);
            // 出现异常时使用默认运费计算
            return calculateDefaultFreight(orderItems);
        }
    }

    /**
     * 计算默认运费（当运费模板不可用时使用）
     *
     * @param orderItems 订单商品列表
     * @return 默认运费金额(单位：元)
     */
    private BigDecimal calculateDefaultFreight(List<OrderItemDTO> orderItems) {
        // 计算商品总价值
        BigDecimal totalValue = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 从系统配置中获取默认运费和免运费阈值
        BigDecimal baseFreight = BigDecimal.valueOf(1000); // 默认10元运费
        BigDecimal freeShippingThreshold = BigDecimal.valueOf(9900); // 默认99元免运费

        // 判断是否满足包邮条件
        return totalValue.compareTo(freeShippingThreshold) >= 0 ? BigDecimal.ZERO : baseFreight;
    }

    /**
     * 订单确认时检查商品是否在禁售区域内
     *
     * @param address    会员地址
     * @param orderItems 订单商品列表
     * @param skuMap     SKU信息映射
     * @return 禁售商品名称列表，如果没有则返回空列表
     */
    private List<String> checkForbiddenAreaForConfirm(MemberAddressDTO address, List<OrderItemDTO> orderItems, Map<Long, SkuInfoDTO> skuMap) {
        List<String> forbiddenProducts = new ArrayList<>();

        // 必要参数检查
        if (address == null || orderItems == null || orderItems.isEmpty() || skuMap == null || skuMap.isEmpty()) {
            return forbiddenProducts;
        }

        try {
            // 构建区域编码
            String regionCode = (address.getProvinceId() != null ? address.getProvinceId() : "") + "," +
                    (address.getCityId() != null ? address.getCityId() : "") + "," +
                    (address.getAreaId() != null ? address.getAreaId() : "");

            // 收集所有商品的禁售区域ID
            Map<Long, List<Long>> forbiddenAreaSkusMap = new HashMap<>(); // 禁售区域ID -> SKU ID列表
            List<Long> forbiddenAreaIds = new ArrayList<>();

            // 收集所有的禁售区域ID和相关商品
            for (OrderItemDTO item : orderItems) {
                Long skuId = item.getSkuId();
                SkuInfoDTO skuInfo = skuMap.get(skuId);
                if (skuInfo != null && skuInfo.getForbiddenAreaId() != null) {
                    Long forbiddenAreaId = skuInfo.getForbiddenAreaId();
                    // 添加到禁售区域ID列表
                    if (!forbiddenAreaIds.contains(forbiddenAreaId)) {
                        forbiddenAreaIds.add(forbiddenAreaId);
                    }

                    // 将SKU添加到对应禁售区域的商品列表中
                    forbiddenAreaSkusMap.computeIfAbsent(forbiddenAreaId, k -> new ArrayList<>()).add(skuId);
                }
            }

            if (forbiddenAreaIds.isEmpty()) {
                log.info("订单中没有关联禁售区域的商品，跳过禁售区域检查");
                return forbiddenProducts;
            }

            // 调用禁售区域服务，检查当前区域是否在禁售区域列表中
            Result<List<Long>> result = forbiddenAreaFeignClient.checkRegionInForbiddenAreas(regionCode, forbiddenAreaIds);

            if (!Result.isSuccess(result)) {
                log.error("调用禁售区域服务失败: {}", result.getMsg());
                return forbiddenProducts;
            }

            List<Long> matchedForbiddenAreaIds = result.getData();
            if (matchedForbiddenAreaIds != null && !matchedForbiddenAreaIds.isEmpty()) {
                // 从匹配的禁售区域中获取相关商品
                for (Long forbiddenAreaId : matchedForbiddenAreaIds) {
                    List<Long> skuIds = forbiddenAreaSkusMap.get(forbiddenAreaId);
                    if (skuIds != null) {
                        for (Long skuId : skuIds) {
                            SkuInfoDTO skuInfo = skuMap.get(skuId);
                            if (skuInfo != null) {
                                forbiddenProducts.add(skuInfo.getSkuName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查禁售区域异常", e);
        }

        return forbiddenProducts;
    }

    /**
     * 发起订单售后
     * 门面方法，负责校验表单参数、记录日志及消息通知
     *
     * @param form 售后申请表单
     * @return 售后申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "发起订单售后申请", type = "APPLY_AFTERSALE", bizNo = "#form.orderNo")
    public Long applyAfterSale(OrderAfterSaleForm form) {
        log.info("发起订单售后申请，订单号：{}", form.getOrderNo());

        // 1. 校验参数
        Assert.notNull(form, "售后申请表单不能为空");
        Assert.notBlank(form.getOrderNo(), "订单号不能为空");
        Assert.notNull(form.getAfterSaleType(), "售后类型不能为空");

        // 2. 验证订单状态
        OmsOrder order = this.getOrderByOrderNo(form.getOrderNo());
        if (order == null) {
            throw new BizException(OmsResultCode.ORDER_NOT_FOUND);
        }

        Integer orderStatus = order.getStatus();
        if (orderStatus < OrderStatusEnum.PAID.getValue() || orderStatus > OrderStatusEnum.COMPLETED.getValue()) {
            throw new BizException(OmsResultCode.AFTER_SALE_ORDER_STATUS_NOT_SATISFIED);
        }

        // 3. 校验售后商品：一售后单 = 一订单商品行（售后单维度绑定商品行，与用户端单选一致）
        if (form.getOrderItems() == null || form.getOrderItems().isEmpty()) {
            throw new BizException(OmsResultCode.AFTER_SALE_GOODS_CANNOT_EMPTY);
        }
        if (form.getOrderItems().size() != 1) {
            throw new BizException(OmsResultCode.AFTER_SALE_GOODS_CANNOT_EMPTY);
        }

        // 3.1 校验是否重复申请（同一商品项已有进行中的售后）
        List<Long> orderItemIds = form.getOrderItems().stream()
                .map(OrderAfterSaleItemForm::getOrderItemId)
                .collect(Collectors.toList());
        List<OmsOrderAfterSale> activeAfterSales = orderAfterSaleService.getBaseMapper().selectList(
                new LambdaQueryWrapper<OmsOrderAfterSale>()
                        .eq(OmsOrderAfterSale::getOrderId, order.getId())
                        .in(OmsOrderAfterSale::getStatus, List.of(
                                AfterSaleStatusEnum.WAIT_AUDIT.getValue(),
                                AfterSaleStatusEnum.WAIT_RETURN.getValue(),
                                AfterSaleStatusEnum.RETURNING.getValue(),
                                AfterSaleStatusEnum.WAIT_RECEIVE.getValue(),
                                AfterSaleStatusEnum.WAIT_REFUND.getValue(),
                                AfterSaleStatusEnum.REFUNDING.getValue())));
        if (!activeAfterSales.isEmpty()) {
            List<Long> activeAfterSaleIds = activeAfterSales.stream()
                    .map(OmsOrderAfterSale::getId).collect(Collectors.toList());
            long duplicateCount = orderAfterSaleItemService.getBaseMapper().selectList(
                    new LambdaQueryWrapper<OmsOrderAfterSaleItem>()
                            .in(OmsOrderAfterSaleItem::getOrderItemId, orderItemIds)
                            .in(OmsOrderAfterSaleItem::getAfterSaleId, activeAfterSaleIds)
            ).size();
            if (duplicateCount > 0) {
                throw new BizException(OmsResultCode.AFTER_SALE_ALREADY_IN_PROGRESS);
            }
        }

        // 3.1 校验退款金额：行级退款上限（Σ涉及商品项可退 = 行实付 - 行已退）
        if (form.getRefundAmount() == null || form.getRefundAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(OmsResultCode.REFUND_AMOUNT_CANNOT_BE_NEGATIVE);
        }
        if (form.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            List<OmsOrderItem> involvedItems = orderItemService.listByIds(orderItemIds);
            BigDecimal available = involvedItems.stream()
                    .map(i -> nvl(i.getPaymentAmount()).subtract(nvl(i.getRefundAmount())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (form.getRefundAmount().compareTo(available) > 0) {
                throw new BizException(OmsResultCode.REFUND_AMOUNT_EXCEEDS_PAID);
            }
        }

        // 4. 委托给领域服务处理具体业务逻辑，并传递order对象
        Long afterSaleId = orderAfterSaleService.applyAfterSale(form, order);

        // 5. 如果售后单创建成功，聚合刷新订单状态（涉及 item 已由领域服务置为售后中）
        if (afterSaleId != null) {
            orderStatusAggregator.refreshOrderStatus(order.getId());
            log.info("订单售后申请完成，订单号：{}", order.getOrderNo());
        }

        return afterSaleId;
    }

    /**
     * 获取订单售后列表
     *
     * @param query 查询条件
     * @return 售后分页列表
     */
    @Override
    public Page<AfterSalePageVO> getAfterSalePage(AfterSalePageQuery query) {
        return orderAfterSaleService.getAfterSalePage(query);
    }

    /**
     * 获取订单售后详情
     *
     * @param id 售后ID
     * @return 售后详情
     */
    @Override
    public AfterSalePageVO getAfterSaleDetail(Long id) {
        return orderAfterSaleService.getAfterSaleDetail(id);
    }

    /**
     * 取消订单售后
     * 门面方法，负责状态校验、事务控制和消息通知
     *
     * @param id 售后ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "取消售后申请", type = "CANCEL_AFTERSALE", bizNo = "#id")
    public boolean cancelAfterSale(Long id) {
        log.info("取消售后申请，售后ID:{}", id);

        // 1. 参数校验
        Assert.notNull(id, "售后ID不能为空");

        // 2. 获取售后单信息
        OmsOrderAfterSale afterSale = orderAfterSaleService.getById(id);
        if (afterSale == null) {
            log.error("售后单不存在，ID：{}", id);
            throw new BizException(OmsResultCode.AFTER_SALE_ORDER_NOT_EXIST);
        }

        // 3. 校验售后单状态（仅待审核和待退货可取消）
        Integer status = afterSale.getStatus();
        if (status != AfterSaleStatusEnum.WAIT_AUDIT.getValue()
                && status != AfterSaleStatusEnum.WAIT_RETURN.getValue()) {
            log.warn("当前售后状态不支持取消，ID：{}，状态：{}", id, status);
            throw new BizException(OmsResultCode.AFTER_SALE_STATUS_CANNOT_CANCEL);
        }

        // 4. 委托给领域服务处理具体业务逻辑
        boolean result = orderAfterSaleService.cancelAfterSale(id);


        // 5. 检查订单是否还有其他在途售后单，无则按 pre_status 恢复商品项 + 聚合订单状态
        if (result) {
            OmsOrder order = getOrderByOrderNo(afterSale.getOrderNo());
            tryRestoreOrderStatus(order);
        }
        return result;
    }

    /**
     * 填写退货物流信息
     * 门面方法，负责事务控制和状态流转管理
     *
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "提交退货物流信息", type = "RETURN_GOODS", bizNo = "#id")
    public boolean returnGoods(ReturnLogisticsForm returnLogisticsForm) {

        Long afterSaleId = returnLogisticsForm.getAfterSaleId();
        // 1. 获取售后单信息
        OmsOrderAfterSale afterSale = orderAfterSaleService.getById(afterSaleId);
        if (afterSale == null) {
            log.error("售后单不存在，ID：{}", returnLogisticsForm.getAfterSaleId());
            throw new BizException(OmsResultCode.AFTER_SALE_ORDER_NOT_EXIST);
        }

        // 2. 校验售后单状态
        if (!AfterSaleStatusEnum.WAIT_RETURN.getValue().equals(afterSale.getStatus())) {
            log.warn("当前售后状态不支持填写物流，ID：{}，状态：{}", afterSaleId, afterSale.getStatus());
            throw new BizException(OmsResultCode.AFTER_SALE_STATUS_CANNOT_FILL_LOGISTICS);
        }

        // 获取关联订单信息
        OmsOrder order = this.getById(afterSale.getOrderId());
        if (order == null) {
            log.error("关联订单不存在，订单ID：{}", afterSale.getOrderId());
            throw new BizException(OmsResultCode.ORDER_ASSOCIATED_NOT_EXIST);
        }

        // 4. 委托给领域服务处理具体业务逻辑，并传递order对象
        boolean result = orderAfterSaleService.returnGoods(returnLogisticsForm);

        // 5. 处理结果并记录日志

        if (result) {
            // 查询商户收货地址
            OmsOrderAddress merchantAddress = orderAddressService.findByOrderId(order.getId(), ReceiveAddressTypeEnum.MERCHANT_RECEIVE);
            String merchantPhone = merchantAddress != null ? merchantAddress.getUserPhone() : "";

            // 构建物流DTO并订阅物流跟踪
            try {
                LogisticsDTO logisticsDTO = LogisticsDTO.builder()
                        .bizNo(afterSale.getAfterSaleNo())
                        .trackingNo(returnLogisticsForm.getTrackingNo())
                        .carrierCode(returnLogisticsForm.getCarrierCode())
                        .phone(merchantPhone)
                        .spuName("itemName")
                        .logisticsType(LogisticsTypeEnum.RETURN.getValue())
                        .build();

                boolean subscribed = logisticsInfoService.subscribeReturnLogistics(logisticsDTO);
                if (!subscribed) {
                    log.warn("售后退货物流订阅失败，售后单ID: {}", afterSaleId);
                }
            } catch (Exception e) {
                log.error("物流订阅异常", e);
                // 物流订阅失败不影响售后流程继续
            }
        }

        return result;
    }

    /**
     * 审核售后申请
     * 在OrderService层管理售后审核逻辑和订单状态变更
     *
     * @param id       售后申请ID
     * @param approve  是否通过
     * @param remark   审核备注
     * @param handleBy 处理人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OrderLog(detail = "售后单审核", type = "AUDIT_AFTERSALE", bizNo = "#form.orderNo")
    public void auditAfterSale(Long id, Boolean approve, String remark, String handleBy) {
        log.info("审核售后申请，ID:{}，审核结果:{}，处理人:{}", id, approve, handleBy);

        // 1. 获取售后申请
        OmsOrderAfterSale afterSale = orderAfterSaleService.getById(id);
        if (afterSale == null) {
            throw new BizException(OmsResultCode.AFTER_SALE_NOT_EXIST);
        }

        // 2. 检查售后状态
        if (!AfterSaleStatusEnum.WAIT_AUDIT.getValue().equals(afterSale.getStatus())) {
            throw new BizException(OmsResultCode.AFTER_SALE_STATUS_CANNOT_AUDIT);
        }

        // 3. 获取关联订单
        OmsOrder order = this.getById(afterSale.getOrderId());
        if (order == null) {
            throw new BizException(OmsResultCode.ORDER_NOT_FOUND);
        }

        // 4. 更新售后状态
        if (approve) {
            // 审核通过
            if (afterSale.getAfterSaleType() == AfterSaleTypeEnum.REFUND_ONLY.getValue()) {
                afterSale.setStatus(AfterSaleStatusEnum.WAIT_REFUND.getValue());
            } else {
                afterSale.setStatus(AfterSaleStatusEnum.WAIT_RETURN.getValue());
            }
        } else {
            // 审核拒绝
            afterSale.setStatus(AfterSaleStatusEnum.AUDIT_REJECTED.getValue());
        }

        // 5. 更新售后记录
        afterSale.setHandleTime(LocalDateTime.now());
        afterSale.setHandleBy(handleBy);
        afterSale.setHandleNote(remark);
        boolean result = orderAfterSaleService.updateById(afterSale);

        // 5. 检查订单是否还有其他在途售后单
        if (result) {
            tryRestoreOrderStatus(order);
        }

    }

    /**
     * 确认收货
     * 在OrderService层实现收货确认逻辑
     *
     * @param id       售后ID
     * @param handleBy 处理人
     * @param remark   备注
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceipt(Long id, String handleBy, String remark, Boolean approved) {
        log.info("收货确认，售后ID:{}，处理人:{}，结果:{}", id, handleBy, approved);

        // 1. 获取售后申请
        OmsOrderAfterSale afterSale = orderAfterSaleService.getById(id);
        if (afterSale == null) {
            log.error("售后申请不存在，ID：{}", id);
            return false;
        }

        // 2. 检查售后状态
        if (afterSale.getStatus() != AfterSaleStatusEnum.RETURNING.getValue() &&
                afterSale.getStatus() != AfterSaleStatusEnum.WAIT_RECEIVE.getValue()) {
            log.warn("当前售后状态不支持收货确认，ID：{}，状态：{}", afterSale.getId(), afterSale.getStatus());
            return false;
        }

        // 3. 获取关联订单
        OmsOrder order = this.getById(afterSale.getOrderId());
        if (order == null) {
            log.error("关联订单不存在，订单ID：{}", afterSale.getOrderId());
            return false;
        }

        // 4. 更新售后状态
        if (Boolean.FALSE.equals(approved)) {
            // 拒绝收货 → 已拒绝
            afterSale.setStatus(AfterSaleStatusEnum.AUDIT_REJECTED.getValue());
            afterSale.setHandleNote(remark);
        } else {
            // 确认收货 → 待退款
            afterSale.setStatus(AfterSaleStatusEnum.WAIT_REFUND.getValue());
            afterSale.setReceiveTime(LocalDateTime.now());
        }
        afterSale.setHandleBy(handleBy);

        boolean result = orderAfterSaleService.updateById(afterSale);
        if (!result) {
            return false;
        }

        // 5. 拒绝收货时，恢复订单状态
        if (Boolean.FALSE.equals(approved)) {
            tryRestoreOrderStatus(order);
        }

        return true;
    }

    /**
     * 尝试恢复订单状态（售后全部处理完后，将涉及商品项按 pre_status 恢复，订单状态由聚合器刷新）
     */
    private void tryRestoreOrderStatus(OmsOrder order) {
        LambdaQueryWrapper<OmsOrderAfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderAfterSale::getOrderNo, order.getOrderNo())
                .ne(OmsOrderAfterSale::getStatus, AfterSaleStatusEnum.CANCELED.getValue())
                .ne(OmsOrderAfterSale::getStatus, AfterSaleStatusEnum.AUDIT_REJECTED.getValue())
                .ne(OmsOrderAfterSale::getStatus, AfterSaleStatusEnum.REFUND_SUCCESS.getValue());

        long activeCount = orderAfterSaleService.count(wrapper);
        if (activeCount > 0) {
            // 仍有在途售后单，订单保持售后中（由聚合器维护）
            return;
        }

        // 全部售后单已终态：恢复涉及商品项（仅恢复仍处于售后中的 item，幂等）
        // 商品行主键：售后单.order_item_id（一售后单=一商品行，免明细 join）
        List<OmsOrderAfterSale> terminalAfterSales = orderAfterSaleService.lambdaQuery()
                .eq(OmsOrderAfterSale::getOrderId, order.getId())
                .in(OmsOrderAfterSale::getStatus, List.of(
                        AfterSaleStatusEnum.CANCELED.getValue(),
                        AfterSaleStatusEnum.AUDIT_REJECTED.getValue()))
                .list();
        for (OmsOrderAfterSale afterSale : terminalAfterSales) {
            if (afterSale.getOrderItemId() == null) {
                continue;
            }
            // pre_status 从售后明细取（售后申请时快照）
            OmsOrderAfterSaleItem afterSaleItem = orderAfterSaleItemService.lambdaQuery()
                    .eq(OmsOrderAfterSaleItem::getAfterSaleId, afterSale.getId())
                    .last("LIMIT 1")
                    .one();
            Integer preStatus = afterSaleItem != null ? afterSaleItem.getPreStatus() : null;
            if (preStatus == null) {
                continue;
            }
            OmsOrderItem item = orderItemService.getById(afterSale.getOrderItemId());
            if (item == null) {
                continue;
            }
            OrderItemStatusEnum restoreTarget = OrderItemStatusEnum.getByValue(preStatus);
            if (restoreTarget == null || !orderItemStateMachine.transform(item, restoreTarget)) {
                continue;
            }
            orderItemService.updateById(item);
        }

        // 订单状态由商品项聚合派生
        orderStatusAggregator.refreshOrderStatus(order.getId());
        log.info("订单所有售后申请已处理完成，商品项按快照恢复，订单号: {}", order.getOrderNo());
    }

    /**
     * 售后退款成功后商品项履约状态处理：
     * 全额退款（退款数量≥下单数量）→ 已退款(5)；部分退款 → 按售后前快照恢复
     */
    private void updateItemStatusAfterRefund(Long orderItemId, Integer preStatus) {
        if (orderItemId == null) {
            return;
        }
        OmsOrderItem item = orderItemService.getById(orderItemId);
        if (item == null) {
            return;
        }
        Integer quantity = item.getQuantity();
        Integer refundQuantity = item.getRefundQuantity();
        boolean fullRefund = refundQuantity != null && quantity != null && refundQuantity >= quantity;
        OrderItemStatusEnum target = fullRefund
                ? OrderItemStatusEnum.REFUNDED
                : OrderItemStatusEnum.getByValue(preStatus != null
                        ? preStatus : OrderItemStatusEnum.PENDING_SHIP.getValue());
        if (target == null) {
            return;
        }
        if (orderItemStateMachine.transform(item, target)) {
            orderItemService.updateById(item);
        }
    }

    /**
     * 处理快速退款
     * 适用于支付回调失败和发货前的退款场景
     * 改造后：走统一的售后流程，创建售后单后自动审核通过
     *
     * @param orderNo 订单编号
     * @return 退款处理结果
     */
    public boolean processQuickRefund(String orderNo) {
        log.info("开始处理快速退款，订单号: {}", orderNo);

        OmsOrder order = getOrderByOrderNo(orderNo);
        if (order == null) {
            log.error("订单不存在，无法发起退款: {}", orderNo);
            return false;
        }

        try {
            // 1. 检查订单状态，只有已支付的订单才能退款
            if (!OrderStatusEnum.PAID.getValue().equals(order.getStatus())) {
                log.error("订单状态不允许退款，订单号: {}, 当前状态: {}", orderNo, order.getStatus());
                return false;
            }

            // 2. 创建售后单并自动审核通过（仅退款类型）- 包含售后商品明细，涉及商品项已置为售后中
            Long afterSaleId = orderAfterSaleService.applyAfterSaleAutoApprove(
                    order,
                    AfterSaleTypeEnum.REFUND_ONLY.getValue(),  // 仅退款
                    order.getPaymentAmount(),                  // 全额退款
                    "系统自动退款"                                // 退款原因
            );

            // 3. 聚合刷新订单状态（涉及 item 已为售后中）
            orderStatusAggregator.refreshOrderStatus(order.getId());

            // 4. 调用售后退款流程处理退款
            boolean refundResult = processAfterSaleRefund(afterSaleId);
            if (!refundResult) {
                log.error("快速退款申请失败，订单号：{}", order.getOrderNo());
                return false;
            }

            log.info("快速退款请求已处理，订单号：{}", order.getOrderNo());
            return true;
        } catch (Exception e) {
            log.error("发起快速退款请求异常，订单号: {}", order.getOrderNo(), e);
            return false;
        }
    }


    /**
     * 处理售后退款
     * 适用于发货后的退款场景，需要创建售后单
     *
     * @param afterSaleId 售后单ID
     * @return 退款处理结果
     */
    public boolean processAfterSaleRefund(Long afterSaleId) {
        log.info("开始处理售后退款，售后单: {}", afterSaleId);

        OmsOrderAfterSale afterSale = orderAfterSaleService.getById(afterSaleId);
        if (afterSale == null) {
            log.error("售后单不存在，ID：{}", afterSaleId);
            return false;
        }

        /**
         * 订单状态检查
         */

        OmsOrder order = getOrderByOrderNo(afterSale.getOrderNo());
        if (order == null) {
            log.error("订单不存在，无法发起退款: {}", order.getOrderNo());
            return false;
        }

        if (!OrderStatusEnum.REFUNDING.getValue().equals(order.getStatus())) {
            log.error("订单状态不允许退款，订单号: {}, 当前状态: {}",
                    order.getOrderNo(), order.getStatus());
        }


        // 3. 检查售后单状态
        if (!AfterSaleStatusEnum.WAIT_REFUND.getValue().equals(afterSale.getStatus())) {
            log.error("售后单状态不允许退款，售后单ID: {}, 当前状态: {}",
                    afterSale.getId(), afterSale.getStatus());
            return false;
        }

        // 4. 修改售后单状态为退款中
        orderAfterSaleService.updateAfterSaleRefundStatus(
                afterSale,
                AfterSaleStatusEnum.REFUNDING.getValue(),
                null);

        // 5. 构建退款请求并调用支付服务
        RefundRequestDTO refundApplyDTO = RefundRequestDTO.builder()
                .applyNo(afterSale.getAfterSaleNo())
                .orderNo(order.getOrderNo())
                .paymentNo(order.getPaymentNo())
                .refundAmount(afterSale.getRefundAmount())
                .refundReason(afterSale.getReason())
                .refundType(RefundTypeEnum.AFTER_SALE_REFUND.getValue())
                .build();

        // 6. 调用支付服务的退款接口，由支付服务内部处理支付相关的查询和业务逻辑
        Result<RefundResultDTO> refundFeignResult = payFeignClient.refund(refundApplyDTO);
        if (!Result.isSuccess(refundFeignResult)) {
            log.error("退款申请失败，售后单号：{}", afterSale.getAfterSaleNo());
            return false;
        }

        return true;
    }

    /**
     * 创建统一的支付单（极简版）
     * 直接通过paymentNo关联所有订单，无需额外的orderNo字段
     *
     * @param order     订单对象
     * @param paymentNo 支付单号
     */
    /**
     * 生成支付内容描述
     *
     * @param items 订单商品行集合对象
     * @return 支付内容描述
     */
    private String generatePaymentContent(List<OrderSubmitForm.OrderItem> items) {
        String productName = items.get(0).getSpuName();
        String content = items.size() == 1 ? productName : productName + "等" + items.size() + "件商品";
        return content.length() > 16 ? content.substring(0, 13) + "..." : content;
    }

    /**
     * 导出售后单数据
     *
     * @param queryParams 查询参数
     * @param response    响应对象
     * @throws IOException IO异常
     */
    @Override
    public void exportAfterSales(AfterSalePageQuery queryParams, HttpServletResponse response) throws IOException {
        log.info("开始导出售后单数据");

        try {
            // 查询售后单数据
            List<AfterSaleExportVO> list = orderAfterSaleMapper.selectAfterSaleExportList(queryParams);

            // 设置响应头
            String fileName = "售后单数据.xlsx"; // 示例文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20"); // 替换空格为 %20
            // 兼容新旧浏览器
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            EasyExcel.write(response.getOutputStream(), AfterSaleExportVO.class)
                    .sheet("售后单数据")
                    .doWrite(list);

            log.info("售后单数据导出成功，共{}条", list.size());
        } catch (Exception e) {
            log.error("导出售后单数据异常", e);
            throw new BizException(OmsResultCode.AFTER_SALE_EXPORT_FAILED, (Object) e.getMessage());
        }
    }

    /**
     * 获取售后单统计数据
     *
     * @param queryParams 查询参数
     * @return 售后单统计信息
     */
    @Override
    public AfterSaleStatisticsVO getAfterSaleStatistics(AfterSalePageQuery queryParams) {
        log.info("获取售后单统计数据");
        try {
            // 一次查询获取所有统计数据
            AfterSaleStatisticsVO statistics = orderAfterSaleMapper.getAfterSaleStatistics(
                    queryParams,
                    AfterSaleStatusEnum.WAIT_AUDIT.getValue(),
                    AfterSaleStatusEnum.WAIT_RECEIVE.getValue(),
                    AfterSaleStatusEnum.REFUNDING.getValue()
            );

            log.info("售后单统计数据查询成功: {}", statistics);
            return statistics != null ? statistics : createEmptyStatistics();
        } catch (Exception e) {
            log.error("获取售后单统计数据异常", e);
            return createEmptyStatistics();
        }
    }

    /**
     * 创建空的统计结果对象
     *
     * @return 空的售后单统计信息
     */
    private AfterSaleStatisticsVO createEmptyStatistics() {
        return AfterSaleStatisticsVO.builder()
                .totalCount(0L)
                .totalRefund(BigDecimal.ZERO)
                .pendingAuditCount(0L)
                .pendingReceiveCount(0L)
                .refundingCount(0L)
                .build();
    }

    @Override
    public String recalculatePayment(String paymentNo) {
        // 查同支付单号下所有未取消订单
        List<OmsOrder> activeOrders = this.baseMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getPaymentNo, paymentNo)
                        .ne(OmsOrder::getStatus, OrderStatusEnum.CANCELED.getValue())
        );
        if (activeOrders.isEmpty()) {
            throw new BizException(OmsResultCode.PAY_ORDER_NO_VALID_ORDER);
        }
        // 汇总实付金额
        BigDecimal totalAmount = activeOrders.stream()
                .map(OmsOrder::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 调用支付服务更新金额
        com.deko.mall.pay.api.dto.PaymentRequest dto = new com.deko.mall.pay.api.dto.PaymentRequest();
        dto.setPaymentNo(paymentNo);
        dto.setPayAmount(totalAmount);
        payFeignClient.updatePaymentAmount(dto);
        return paymentNo;
    }

    @Override
    public String createPaymentForOrder(String orderNo) {
        OmsOrder order = getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(OmsResultCode.ORDER_NOT_FOUND);
        }
        if (!OrderStatusEnum.UNPAID.getValue().equals(order.getStatus())) {
            throw new BizException(OmsResultCode.ORDER_STATUS_CANNOT_PAY);
        }

        // 为这一个订单创建独立支付单
        PaymentRequest payReq = new PaymentRequest();
        payReq.setPayAmount(order.getPaymentAmount());
        payReq.setOrderNos(orderNo);
        payReq.setMemberId(order.getMemberId());
        Result<String> payResult = payFeignClient.createPayment(payReq);
        if (!Result.isSuccess(payResult)) {
            throw new BizException(OmsResultCode.PAY_CREATE_FAILED);
        }
        String newPaymentNo = payResult.getData();

        // 更新订单的支付单号
        order.setPaymentNo(newPaymentNo);
        this.updateById(order);

        return newPaymentNo;
    }
}