/* 售后服务API及常量 */
import {request} from '@/config/request'

/**
 * 售后单状态枚举
 * 后端AfterSaleStatusEnum定义
 */
export const AFTER_SALE_STATUS = {
  WAIT_AUDIT: 1,      // 待审核
  AUDIT_REJECTED: 2,  // 已拒绝
  WAIT_RETURN: 3,     // 待退货
  RETURNING: 4,       // 退货中
  WAIT_RECEIVE: 5,    // 待收货
  WAIT_REFUND: 6,       // 待退款
  REFUNDING: 7,       // 退款中
  REFUND_SUCCESS: 8,  // 退款成功
  REFUND_FAIL: 9,     // 退款失败
  CANCELED: 10         // 已取消
};

/**
 * 售后单状态对应的文本
 */
export const AFTER_SALE_STATUS_TEXT = {
  1: '待审核',
  2: '已拒绝',
  3: '待退货',
  4: '退货中',
  5: '待收货',
  6: '待退款',
  7: '退款中',
  8: '退款成功',
  9: '退款失败',
  10: '已取消'
};

/**
 * 售后类型
 */
export const AFTER_SALE_TYPE = {
  REFUND_ONLY: '1', // 仅退款
  RETURN_REFUND: '2' // 退货退款
};

/**
 * 售后类型文本
 */
export const AFTER_SALE_TYPE_TEXT = {
  '1': '仅退款',
  '2': '退货退款'
};

/**
 * 常见退款原因
 */
export const REFUND_REASONS = [
  '商品质量问题',
  '商品与描述不符',
  '商品损坏',
  '未按约定时间发货',
  '其他原因'
];

// 申请售后
export function applyAfterSale(data) {
  return request({
    url: '/mall-oms/app-api/v1/orders/aftersale',
    method: 'post',
    data: data,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 取消售后申请
export function cancelAfterSale(id) {
  return request({
    url: `/mall-oms/app-api/v1/orders/aftersale/${id}/cancel`,
    method: 'post',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 提交退货物流信息
export function returnGoods(data) {
  return request({
    url: `/mall-oms/app-api/v1/orders/aftersale/delivery`,
    method: 'post',
    data,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 获取售后列表
export function getAfterSaleList(params) {
  return request({
    url: '/mall-oms/app-api/v1/orders/aftersale',
    method: 'get',
    params: params,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 获取售后详情
export function getAfterSaleDetail(id) {
  return request({
    url: `/mall-oms/app-api/v1/orders/aftersale/${id}`,
    method: 'get',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 获取物流公司列表
export function getCarrierList() {
  return request({
    url: `/mall-oms/app-api/v1/logistics/delivery/carriers`,
    method: 'get',
    headers: {
      'auth': true // 需要认证
    }
  })
}
