package com.deko.mall.oms.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.oms.model.entity.order.OmsOrderItem;

/**
 * 订单商品信息表
 *
 * @author zhlog1024
 */
public interface OrderItemService extends IService<OmsOrderItem> {

    /**
     * 将订单项标记为已全额退款（全部数量退款）
     *
     * @param orderItemId 订单项ID
     * @return 是否更新成功
     */
    boolean updateItemFullyRefunded(Long orderItemId);

    /**
     * 检查订单所有商品是否全部退款
     *
     * @param orderId 订单ID
     * @return 是否全部退款完成
     */
    boolean checkAllItemsRefunded(Long orderId);
}
