/* 支付相关API（收银台） */
import { request } from '@/config/request'

// 获取收银台信息（支付渠道、原价、折扣、最终金额）
export const getCashierInfo = (paymentNo, deviceType) => {
  return request({
    url: `/mall-pay/app-api/v1/cashier?paymentNo=${paymentNo}` + (deviceType ? `&deviceType=${deviceType}` : ''),
    method: 'get',
    headers: {
      'auth': true
    }
  })
}

// 创建收银台支付请求
export const createCashierPayment = (data) => {
  return request({
    url: '/mall-pay/app-api/v1/cashier/pay',
    method: 'post',
    data: data,
    headers: {
      'auth': true
    }
  })
}

// 查询支付状态（从外部支付页返回后使用）
export const getPaymentStatus = (paymentNo) => {
  return request({
    url: `/mall-pay/app-api/v1/cashier/status?paymentNo=${paymentNo}`,
    method: 'get',
    headers: {
      'auth': true
    }
  })
}
