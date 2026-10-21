package com.deko.mall.oms.service.order.strategy;

import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.pms.model.dto.SkuInfoDTO;

import java.util.Map;

/**
 * 订单处理策略接口
 */
public interface OrderProcessStrategy {
    /**
     * 判断是否支持该类型订单
     *
     * @param submitForm 订单提交表单
     * @param skuMap     商品SKU信息映射
     * @return 是否支持
     */
    boolean supports(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap);

    /**
     * 处理订单前的准备工作
     *
     * @param submitForm 订单提交表单
     * @param skuMap     商品SKU信息映射
     */
    void preProcess(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap);

    /**
     * 验证订单
     *
     * @param submitForm 订单提交表单
     * @param skuMap     商品SKU信息映射
     */
    void validateOrder(OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap);

    /**
     * 计算订单金额
     *
     * @param order      订单实体
     * @param submitForm 订单提交表单
     * @param skuMap     商品SKU信息映射
     */
    void calculateAmount(OmsOrder order, OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap);

    /**
     * 创建订单后的处理
     *
     * @param order      订单实体
     * @param submitForm 订单提交表单
     * @param skuMap     商品SKU信息映射
     */
    void postProcess(OmsOrder order, OrderSubmitForm submitForm, Map<Long, SkuInfoDTO> skuMap);
}
