package com.deko.mall.oms.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSaleItem;

import java.util.List;

/**
 * 订单售后商品明细服务接口
 */
public interface OrderAfterSaleItemService extends IService<OmsOrderAfterSaleItem> {

    /**
     * 根据售后ID获取商品明细
     *
     * @param afterSaleId 售后ID
     * @return 商品明细列表
     */
    List<OmsOrderAfterSaleItem> getByAfterSaleId(Long afterSaleId);

    /**
     * 批量保存售后商品明细
     *
     * @param items 商品明细列表
     * @return 保存结果
     */
    boolean saveBatch(List<OmsOrderAfterSaleItem> items);
}
