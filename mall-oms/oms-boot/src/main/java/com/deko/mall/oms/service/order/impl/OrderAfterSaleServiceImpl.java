package com.deko.mall.oms.service.order.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.utils.IdGenerator;
import com.deko.mall.oms.converter.OrderAfterSaleConverter;
import com.deko.mall.oms.converter.OrderAfterSaleItemConverter;
import com.deko.mall.oms.enums.AfterSaleStatusEnum;
import com.deko.mall.oms.enums.AfterSaleTypeEnum;
import com.deko.mall.oms.enums.OrderItemStatusEnum;
import com.deko.mall.oms.mapper.OrderAfterSaleMapper;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSale;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSaleItem;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;
import com.deko.mall.oms.model.form.OrderAfterSaleForm;
import com.deko.mall.oms.model.form.OrderAfterSaleItemForm;
import com.deko.mall.oms.model.form.ReturnLogisticsForm;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.vo.AfterSalePageVO;
import com.deko.mall.oms.model.vo.OmsOrderAfterSaleItemVO;
import com.deko.mall.oms.service.order.OrderAfterSaleItemService;
import com.deko.mall.oms.service.order.statemachine.OrderItemStateMachine;
import com.deko.mall.oms.service.order.OrderAfterSaleService;
import com.deko.mall.oms.service.order.OrderItemService;
import com.deko.mall.pms.api.SupplierFeignClient;
import com.deko.mall.pms.model.dto.SupplierDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单售后服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAfterSaleServiceImpl extends ServiceImpl<OrderAfterSaleMapper, OmsOrderAfterSale> implements OrderAfterSaleService {

    // OrderService已移除，避免循环依赖
    private final OrderAfterSaleConverter orderAfterSaleConverter;
    private final OrderAfterSaleItemConverter orderAfterSaleItemConverter;
    private final OrderAfterSaleItemService orderAfterSaleItemService;
    private final OrderItemService orderItemService;
    private final OrderItemStateMachine orderItemStateMachine;
    private final IdGenerator idGenerator;
    private final SupplierFeignClient supplierFeignClient;


    /**
     * 创建售后申请
     * 注意：该方法专注于核心业务逻辑，不负责订单状态验证和事务管理
     *
     * @param form  售后申请表单
     * @param order 订单实体（由门面层提供）
     * @return 创建的售后单ID
     */
    @Override
    public Long applyAfterSale(OrderAfterSaleForm form, OmsOrder order) {
        // 直接使用传入的order参数，不再调用orderService
        String orderNo = form.getOrderNo();
        Long memberId = SecurityUtils.getMemberId();

        log.debug("开始创建售后单，订单号：{}，用户ID：{}", orderNo, memberId);

        // 4. 生成售后单号
        String afterSaleNo = idGenerator.generateAfterSaleNo();

        // 5. 创建售后记录
        // 供应商快照来源：售后单涉及的商品项（多供应商订单按商品项归属供应商，而非订单）
        SupplierSnapshot supplierSnapshot = resolveSupplierSnapshot(form.getOrderItems());
        Long afterSaleItemId = form.getOrderItems() != null && form.getOrderItems().size() == 1
                ? form.getOrderItems().get(0).getOrderItemId() : null;
        OmsOrderAfterSale afterSale = OmsOrderAfterSale.builder()
                .afterSaleNo(afterSaleNo)
                .orderId(order.getId())
                .orderNo(orderNo)
                .orderItemId(afterSaleItemId)
                .memberId(memberId)
                .supplierId(supplierSnapshot.supplierId)
                .supplierName(supplierSnapshot.supplierName)
                .shopId(order.getShopId())
                .shopName(order.getShopName())
                .afterSaleType(form.getAfterSaleType())
                .refundAmount(form.getRefundAmount())
                .applyPics(form.getApplyPics())
                .reason(form.getReason())
                .description(form.getDescription())
                .status(AfterSaleStatusEnum.WAIT_AUDIT.getValue())
                // 从订单复制抵扣信息
                .usePointsAmount(order.getUsePointsAmount())
                .build();


        this.save(afterSale);

        // 6. 创建售后商品明细
        if (form.getOrderItems() != null && !form.getOrderItems().isEmpty()) {
            // 使用转换器转换售后商品表单为实体
            List<OmsOrderAfterSaleItem> items = orderAfterSaleConverter.itemForms2Entities(form.getOrderItems());
            // 收集关联的orderItemId，批量查询零售价和供货价
            List<Long> orderItemIds = form.getOrderItems().stream()
                    .map(com.deko.mall.oms.model.form.OrderAfterSaleItemForm::getOrderItemId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, OmsOrderItem> orderItemMap = Collections.emptyMap();
            if (!orderItemIds.isEmpty()) {
                List<OmsOrderItem> orderItems = orderItemService.listByIds(orderItemIds);
                orderItemMap = orderItems.stream()
                        .collect(Collectors.toMap(OmsOrderItem::getId, oi -> oi, (a, b) -> a));
            }
            for (OmsOrderAfterSaleItem item : items) {
                item.setAfterSaleId(afterSale.getId());
                if (item.getOrderItemId() != null) {
                    OmsOrderItem oi = orderItemMap.get(item.getOrderItemId());
                    if (oi != null) {
                        item.setRetailPrice(oi.getRetailPrice());
                        item.setSupplyPrice(oi.getSupplyPrice());
                        // 商品SKU编码快照（下单商品项 sku_sn）
                        item.setSkuSn(oi.getSkuSn());
                        // 售后前商品项状态快照（售后拒绝/取消/部分退款时恢复用）
                        item.setPreStatus(oi.getStatus() != null ? oi.getStatus() : OrderItemStatusEnum.PENDING_SHIP.getValue());
                    }
                }
            }
            // 批量保存售后商品
            orderAfterSaleItemService.saveBatch(items);

            // 商品项状态流转：涉及 item → 售后中（1/2/3 → 4，守卫校验）
            for (OmsOrderAfterSaleItem item : items) {
                if (item.getOrderItemId() == null) {
                    continue;
                }
                OmsOrderItem oi = orderItemMap.get(item.getOrderItemId());
                if (oi != null && orderItemStateMachine.transform(oi, OrderItemStatusEnum.REFUNDING)) {
                    orderItemService.updateById(oi);
                }
            }
        }
        return afterSale.getId();
    }

    /**
     * 审核售后申请
     * 注意：该方法专注于核心业务逻辑，不负责参数验证和事务管理
     * 这些由调用该方法的OrderService门面方法负责
     *
     * @param id       售后单ID
     * @param approve  是否通过审核
     * @param remark   审核备注
     * @param handleBy 处理人
     */
    @Override
    public void auditAfterSale(Long id, boolean approve, String remark, String handleBy) {
        // 获取售后申请
        OmsOrderAfterSale afterSale = this.getById(id);

        // 更新售后状态
        if (approve) {
            // 审核通过
            if (afterSale.getAfterSaleType() == AfterSaleTypeEnum.REFUND_ONLY.getValue()) {
                // 仅退款，直接进入待退款状态
                afterSale.setStatus(AfterSaleStatusEnum.WAIT_REFUND.getValue());
            } else {
                // 退货退款，进入待退货状态
                afterSale.setStatus(AfterSaleStatusEnum.WAIT_RETURN.getValue());
            }
        } else {
            // 审核拒绝
            afterSale.setStatus(AfterSaleStatusEnum.AUDIT_REJECTED.getValue());
        }

        // 更新售后记录审核信息
        afterSale.setHandleTime(LocalDateTime.now());
        afterSale.setHandleBy(handleBy);
        afterSale.setHandleNote(remark);
        this.updateById(afterSale);

        log.debug("售后审核业务逻辑执行完成，售后ID: {}, 审核结果: {}", id, approve ? "通过" : "拒绝");
    }

    /**
     * 更新售后单退款状态
     *
     * @param afterSale  售后单
     * @param status     退款状态
     * @param refundTime 退款时间，如果为成功状态需提供
     * @return 操作结果
     */
    @Override
    public boolean updateAfterSaleRefundStatus(OmsOrderAfterSale afterSale, Integer status, LocalDateTime refundTime) {
        if (afterSale == null || status == null) {
            return false;
        }

        log.debug("更新售后单状态，ID: {}, 状态: {}", afterSale.getId(), status);

        // 设置状态
        afterSale.setStatus(status);

        // 设置退款时间（如果提供）
        if (refundTime != null) {
            afterSale.setRefundTime(refundTime);
        }

        // 更新售后单
        return this.updateById(afterSale);
    }


    @Override
    public Page<AfterSalePageVO> getAfterSalePage(AfterSalePageQuery queryParams) {
        log.debug("查询售后分页列表, 会员ID:{}, 状态:{}, 关键字:{}", queryParams.getMemberId(), queryParams.getStatus(), queryParams.getKeywords());

        // 1. 分页查询（条件统一在 XML baseWhere 中处理，避免 Java/XML 双重维护）
        Page<OmsOrderAfterSale> page = baseMapper.selectPageByQuery(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );

        // 5. 转换为视图对象
        Page<AfterSalePageVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            log.debug("查询到售后记录数量: {}", page.getRecords().size());
            // 收集售后单ID
            List<Long> afterSaleIds = page.getRecords().stream()
                    .map(OmsOrderAfterSale::getId)
                    .collect(Collectors.toList());

            // 批量查询售后商品项
            Map<Long, List<OmsOrderAfterSaleItemVO>> itemsMap = new HashMap<>();
            // 收集全部售后明细的商品行ID，批量查 oms_order_item 带出商品行售后状态（一售后单=一商品行）
            Map<Long, Long> afterSaleIdItemMap = new HashMap<>();
            Map<Long, OmsOrderItem> orderItemMap = new HashMap<>();
            for (Long afterSaleId : afterSaleIds) {
                List<OmsOrderAfterSaleItem> items = orderAfterSaleItemService.getByAfterSaleId(afterSaleId);
                if (items == null || items.isEmpty()) {
                    continue;
                }
                List<OmsOrderAfterSaleItemVO> afterSaleItemVOS = orderAfterSaleItemConverter.entity2ListVO(items);
                for (OmsOrderAfterSaleItem item : items) {
                    if (item.getOrderItemId() != null) {
                        afterSaleIdItemMap.put(afterSaleId, item.getOrderItemId());
                    }
                }
                itemsMap.put(afterSaleId, afterSaleItemVOS);
            }
            if (!afterSaleIdItemMap.isEmpty()) {
                orderItemMap = orderItemService.listByIds(new java.util.ArrayList<>(afterSaleIdItemMap.values()))
                        .stream().collect(Collectors.toMap(OmsOrderItem::getId, oi -> oi, (a, b) -> a));
                // 商品行售后状态回填（status/refundStatus/refundQuantity/refundAmount）
                for (Map.Entry<Long, List<OmsOrderAfterSaleItemVO>> entry : itemsMap.entrySet()) {
                    Long orderItemId = afterSaleIdItemMap.get(entry.getKey());
                    OmsOrderItem oi = orderItemId != null ? orderItemMap.get(orderItemId) : null;
                    if (oi == null) continue;
                    for (OmsOrderAfterSaleItemVO itemVO : entry.getValue()) {
                        itemVO.setStatus(oi.getStatus());
                        itemVO.setRefundStatus(oi.getRefundStatus());
                        itemVO.setRefundQuantity(oi.getRefundQuantity());
                        itemVO.setRefundAmount(oi.getRefundAmount());
                    }
                }
            }

            // 使用MapStruct批量转换为VO对象
            List<AfterSalePageVO> voList = page.getRecords().stream()
                    .map(entity -> {
                        // 使用转换器转换基础属性
                        AfterSalePageVO vo = orderAfterSaleConverter.entity2PageVO(entity);
                        // 设置售后商品项
                        vo.setItems(itemsMap.getOrDefault(entity.getId(), Collections.emptyList()));
                        return vo;
                    })
                    .collect(Collectors.toList());

            resultPage.setRecords(voList);
        } else {
            log.debug("未查询到售后记录");
            resultPage.setRecords(Collections.emptyList());
        }

        return resultPage;
    }


    /**
     * 获取售后详情
     *
     * @param id 售后ID
     * @return 售后详情
     */
    @Override
    public AfterSalePageVO getAfterSaleDetail(Long id) {
        log.debug("获取售后详情, ID:{}", id);

        // 获取售后单信息
        OmsOrderAfterSale afterSale = this.getById(id);
        if (afterSale == null) {
            log.warn("售后申请不存在, ID:{}", id);
            return null;
        }
        // 获取售后商品明细
        List<OmsOrderAfterSaleItem> items = orderAfterSaleItemService.getByAfterSaleId(id);
        // 使用转换器转换实体为VO
        AfterSalePageVO detailVO = orderAfterSaleConverter.entity2DetailVO(afterSale);

        List<OmsOrderAfterSaleItemVO> afterSaleItemVOS = orderAfterSaleItemConverter.entity2ListVO(items);
        // 设置售后商品列表
        detailVO.setItems(items != null ? afterSaleItemVOS : Collections.emptyList());

        // 填充退货地址（仅退货退款且已审批通过）
        if (AfterSaleStatusEnum.WAIT_RETURN.getValue().equals(afterSale.getStatus())
                && AfterSaleTypeEnum.RETURN_REFUND.getValue().equals(afterSale.getAfterSaleType())) {
            try {
                var result = supplierFeignClient.getById(afterSale.getSupplierId());
                if (com.deko.mall.common.result.Result.isSuccess(result) && result.getData() != null) {
                    var supplier = result.getData();
                    if (StringUtils.isNotBlank(supplier.getReturnAddress())) {
                        var addr = new AfterSalePageVO.ReturnAddress();
                        addr.setName(supplier.getReturnContact());
                        addr.setTel(supplier.getReturnPhone());
                        addr.setAddress(supplier.getReturnAddress());
                        detailVO.setReturnAddress(addr);
                    }
                }
            } catch (Exception e) {
                log.warn("查询供应商退货地址失败, supplierId={}, error={}", afterSale.getSupplierId(), e.getMessage());
            }
        }

        return detailVO;
    }


    /**
     * 填写退货物流信息
     * 注意：作为领域服务，该方法只处理核心业务逻辑，不负责事务管理和参数验证
     *
     * @param returnLogisticsForm 退货物流信息表单
     * @return 操作结果
     */
    @Override
    public boolean returnGoods(ReturnLogisticsForm returnLogisticsForm) {
        Long afterSaleId = returnLogisticsForm.getAfterSaleId();
        // 获取售后申请
        OmsOrderAfterSale afterSale = this.getById(afterSaleId);
        if (afterSale == null) {
            log.error("售后申请不存在，ID: {}", afterSaleId);
            return false;
        }

        log.debug("处理退货物流信息: [售后ID: {}, 物流公司: {}, 物流公司编码: {}, 物流单号: {}]",
                afterSaleId,
                returnLogisticsForm.getCarrierName(),
                returnLogisticsForm.getCarrierCode(),
                returnLogisticsForm.getTrackingNo());

        // 更新售后信息
        afterSale.setCarrierName(returnLogisticsForm.getCarrierName());
        afterSale.setCarrierCode(returnLogisticsForm.getCarrierCode());
        afterSale.setTrackingNo(returnLogisticsForm.getTrackingNo());
        afterSale.setStatus(AfterSaleStatusEnum.WAIT_RECEIVE.getValue()); // 直接更新状态为待收货，跳过退货中状态
        afterSale.setReturnTime(LocalDateTime.now());

        // 设置发货人手机号
        afterSale.setSenderPhone(returnLogisticsForm.getSenderPhone());

        // 设置备注信息（如果有）
        if (StringUtils.isNotBlank(returnLogisticsForm.getRemark())) {
            afterSale.setRemark(returnLogisticsForm.getRemark());
        }

        // 设置退货凭证图片（如果有）
        if (StringUtils.isNotBlank(returnLogisticsForm.getReturnPics())) {
            afterSale.setReturnPics(returnLogisticsForm.getReturnPics());
            log.debug("已保存退货凭证图片，售后ID: {}", afterSaleId);
        }

        boolean result = this.updateById(afterSale);

        if (result) {
            log.info("退货物流信息更新成功，售后ID: {}", afterSaleId);
        } else {
            log.warn("退货物流信息更新失败，售后ID: {}", afterSaleId);
        }

        return result;
    }

    public boolean cancelAfterSale(Long id) {
        // 获取售后申请，加载数据
        OmsOrderAfterSale afterSale = this.getById(id);
        log.debug("开始取消售后单，售后ID: {}, 当前状态: {}", id, afterSale.getStatus());

        // 更新售后状态为已取消
        afterSale.setStatus(AfterSaleStatusEnum.CANCELED.getValue());
        boolean updated = this.updateById(afterSale);

        // 返回操作结果，订单状态恢复由门面服务负责
        log.debug("售后取消操作完成，售后ID: {}", id);
        return updated;
    }

    @Override
    public Page<AfterSalePageVO> getRefundPage(AfterSalePageQuery queryParams) {
        return null;
    }

    @Override
    public boolean checkAfterSaleRefund(String orderNo, String refundNo) {
        return false;
    }

    /**
     * 创建售后单并自动审核通过（用于快速退款/支付异常退款场景）
     * 该方法创建售后单后直接进入待退款状态，无需人工审核
     *
     * @param order        订单实体
     * @param refundType   退款类型（1-仅退款，2-退货退款）
     * @param refundAmount 退款金额(单位：元)
     * @param reason       退款原因
     * @return 售后单ID
     */
    @Override
    public Long applyAfterSaleAutoApprove(OmsOrder order, Integer refundType, BigDecimal refundAmount, String reason) {
        log.info("创建自动审核售后单，订单号：{}，退款类型：{}，金额：{}",
                order.getOrderNo(), refundType, refundAmount);

        // 1. 生成售后单号
        String afterSaleNo = idGenerator.generateAfterSaleNo();

        // 2. 创建售后记录（直接设置为待退款状态，跳过审核）
        // 供应商快照来源：订单商品项（快速退款场景，多供应商订单按商品项归属）
        List<OmsOrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OmsOrderItem::getOrderId, order.getId())
                .eq(OmsOrderItem::getDeleted, 0)
                .list();
        OmsOrderItem firstItem = orderItems.stream().findFirst().orElse(null);
        OmsOrderAfterSale afterSale = OmsOrderAfterSale.builder()
                .afterSaleNo(afterSaleNo)
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderItemId(firstItem != null ? firstItem.getId() : null)
                .memberId(order.getMemberId())
                .supplierId(firstItem != null ? firstItem.getSupplierId() : null)
                .supplierName(firstItem != null ? firstItem.getSupplierName() : null)
                .shopId(order.getShopId())
                .shopName(order.getShopName())
                .afterSaleType(refundType)
                .refundAmount(refundAmount)
                .reason(reason)
                // 快速退款场景直接跳过审核，进入待退款状态
                .status(AfterSaleStatusEnum.WAIT_REFUND.getValue())
                .handleBy("系统自动")
                .handleTime(LocalDateTime.now())
                .handleNote("系统自动审核通过")
                // 从订单复制抵扣信息
                .usePointsAmount(order.getUsePointsAmount())
                .build();

        this.save(afterSale);

        // 3. 创建售后商品明细（从订单商品复制，orderItems 已在上方查询）
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OmsOrderAfterSaleItem> afterSaleItems = new ArrayList<>();
            for (OmsOrderItem orderItem : orderItems) {
                OmsOrderAfterSaleItem afterSaleItem = OmsOrderAfterSaleItem.builder()
                        .afterSaleId(afterSale.getId())
                        .orderItemId(orderItem.getId())
                        .spuId(orderItem.getSpuId())
                        .skuId(orderItem.getSkuId())
                        .skuSn(orderItem.getSkuSn())
                        .skuName(orderItem.getSkuName())
                        .picUrl(orderItem.getPicUrl())
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getPrice())
                        .retailPrice(orderItem.getRetailPrice())
                        .supplyPrice(orderItem.getSupplyPrice())
                        .totalAmount(orderItem.getTotalAmount())
                        // 售后前商品项状态快照（快速退款场景：待发货/已发货）
                        .preStatus(orderItem.getStatus() != null ? orderItem.getStatus()
                                : OrderItemStatusEnum.PENDING_SHIP.getValue())
                        .build();
                afterSaleItems.add(afterSaleItem);
            }
            orderAfterSaleItemService.saveBatch(afterSaleItems);

            // 商品项状态流转：涉及 item → 售后中
            for (OmsOrderItem orderItem : orderItems) {
                if (orderItemStateMachine.transform(orderItem, OrderItemStatusEnum.REFUNDING)) {
                    orderItemService.updateById(orderItem);
                }
            }
            log.info("自动审核售后单创建售后商品明细成功，售后单号：{}，商品数量：{}", afterSaleNo, afterSaleItems.size());
        }

        log.info("自动审核售后单创建成功，售后单号：{}", afterSaleNo);
        return afterSale.getId();
    }

    @Override
    public BigDecimal sumRefundedAmount(Long orderId) {
        List<OmsOrderAfterSale> list = this.lambdaQuery()
                .eq(OmsOrderAfterSale::getOrderId, orderId)
                .notIn(OmsOrderAfterSale::getStatus, List.of(
                        AfterSaleStatusEnum.AUDIT_REJECTED.getValue(),
                        AfterSaleStatusEnum.REFUND_FAIL.getValue(),
                        AfterSaleStatusEnum.CANCELED.getValue()
                ))
                .list();
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;
        return list.stream()
                .map(OmsOrderAfterSale::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 解析售后单供应商快照：从售后涉及的订单商品项取（多供应商订单按商品项归属）
     */
    private SupplierSnapshot resolveSupplierSnapshot(List<OrderAfterSaleItemForm> forms) {
        if (forms == null || forms.isEmpty()) {
            return new SupplierSnapshot(null, null);
        }
        List<Long> itemIds = forms.stream()
                .map(OrderAfterSaleItemForm::getOrderItemId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (itemIds.isEmpty()) {
            return new SupplierSnapshot(null, null);
        }
        OmsOrderItem first = orderItemService.listByIds(itemIds).stream().findFirst().orElse(null);
        return first != null
                ? new SupplierSnapshot(first.getSupplierId(), first.getSupplierName())
                : new SupplierSnapshot(null, null);
    }

    private record SupplierSnapshot(Long supplierId, String supplierName) {
    }

}
