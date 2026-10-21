/* 订单相关API */
import { request } from '@/config/request'

// 订单状态常量
export const ORDER_STATUS = {
  UNPAID: 0,     // 待付款
  PAID: 1,       // 待发货
  DELIVERED: 2,  // 已发货/待收货
  COMPLETED: 3,  // 已完成
  CANCELED: 4,   // 已取消
  REFUNDING: 5,  // 售后中
  REFUNDED: 6    // 已退款
}

// 订单状态文本
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.UNPAID]: '待付款',
  [ORDER_STATUS.PAID]: '待发货',
  [ORDER_STATUS.DELIVERED]: '待收货',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELED]: '已取消',
  [ORDER_STATUS.REFUNDING]: '售后中',
  [ORDER_STATUS.REFUNDED]: '已退款'
}

// 订单列表
export function listOrdersWithPage(params) {
  return request({
    url: '/mall-oms/app-api/v1/orders',
    method: 'get',
    params: params,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 订单确认
export function confirm(skuId, count, channelCode, addressId) {
  const params = {
    skuId: skuId,
    count: count,
    channelCode: channelCode
  }
  if (addressId != null) {
    params.addressId = addressId
  }
  return request({
    url: '/mall-oms/app-api/v1/orders/confirm',
    method: 'post',
    params: params,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 订单提交
export function submit(data) {
  return request({
    url: '/mall-oms/app-api/v1/orders/submit',
    method: 'post',
    data: data,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 订单支付
export function pay(data) {
  return request({
    url: '/mall-oms/app-api/v1/orders/payment',
    method: 'post',
    data: data,
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 获取订单状态
export function getOrderStatus(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/info-status/' + orderNo,
    method: 'get',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 获取订单详情（包含完整的订单信息和商品项）
export function getOrderDetail(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/info/' + orderNo,
    method: 'get',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 为订单创建独立支付单（待付款列表重付场景）
export function createOrderPayment(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/' + orderNo + '/create-payment',
    method: 'post',
    headers: {
      'auth': true
    }
  })
}

// 取消订单
export function cancelOrder(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/cancel/' + orderNo,
    method: 'post',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 确认收货
export function confirmReceive(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/' + orderNo + '/confirm-receipt',
    method: 'post',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 删除订单
export function deleteOrder(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/' + orderNo,
    method: 'delete',
    headers: {
      'auth': true // 需要认证
    }
  })
}

// 快速退款
export function quickRefund(orderNo) {
  return request({
    url: '/mall-oms/app-api/v1/orders/' + orderNo + '/quick-refund',
    method: 'post',
    headers: {
      'auth': true // 需要认证
    }
  })
}
