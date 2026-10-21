/* 店铺接口。 */
import {request} from '@/config/request'

/**
 * 店铺广场分页列表
 *
 * @param {Object} params {pageNum, pageSize, keywords}
 */
export function listShopPages(params) {
	return request({
		url: '/mall-pms/app-api/v1/shop/pages',
		method: 'get',
		params: params
	})
}

/**
 * 店铺详情（含店铺商品分页）
 *
 * @param {Number} shopId
 */
export function getShopDetail(shopId) {
	return request({
		url: '/mall-pms/app-api/v1/shop/' + shopId,
		method: 'get'
	})
}

/**
 * 收藏店铺（统一收藏中心）
 *
 * @param {Number} shopId
 */
export function addShopFavor(shopId) {
	return request({
		url: '/mall-ums/app-api/v1/favor/toggle/SHOP/' + shopId,
		method: 'post',
		headers: { auth: true }
	})
}

/**
 * 取消收藏店铺（统一收藏中心）
 *
 * @param {Number} favorId 收藏记录ID
 */
export function cancelShopFavor(favorId) {
	return request({
		url: '/mall-ums/app-api/v1/favor/' + favorId,
		method: 'delete',
		headers: { auth: true }
	})
}

/**
 * 我的收藏店铺列表（统一收藏中心）
 *
 * @param {Number} pageNum
 * @param {Number} pageSize
 */
export function getMyFavorShops(pageNum, pageSize) {
	return request({
		url: '/mall-ums/app-api/v1/favor?targetType=SHOP',
		method: 'post',
		data: { pageNum: pageNum, pageSize: pageSize },
		headers: { auth: true }
	})
}
