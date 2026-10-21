import { request } from '@/config/request'

/**
 * 获取商品评论列表
 */
export function getGoodsComments(spuId, params = {}) {
  return request({
    url: '/mall-pms/app-api/v1/goods/' + spuId + '/comments',
    method: 'GET',
    params: params
  })
}

/**
 * 获取商品评分统计
 */
export function getGoodsCommentScore(spuId) {
  return request({
    url: '/mall-pms/app-api/v1/goods/' + spuId + '/comment-score',
    method: 'GET'
  })
}

/**
 * 提交订单评论
 */
export function submitOrderComments(orderId, data) {
  return request({
    url: '/mall-pms/app-api/v1/orders/' + orderId + '/comments',
    method: 'POST',
    data: data
  })
}

/**
 * 获取我的评论列表
 */
export function getMyComments(params = {}) {
  return request({
    url: '/mall-pms/app-api/v1/member/comments/page',
    method: 'GET',
    params: params
  })
}
