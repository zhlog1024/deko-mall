/* 直播接口。 */
import { request } from '@/config/request'

/**
 * 直播首页/搜索初始化数据（状态Tabs/轮播/基础信息）
 */
export function getLiveInit() {
	return request({
		url: '/mall-sms/app-api/v1/live/rooms/tabs',
		method: 'get'
	})
}

/**
 * 装修首页直播列表（diy autolivelist，返回数组）
 * @param {Object} params {keywords, status, number}
 */
export function getDiyLiveList(params) {
	return request({
		url: '/mall-sms/app-api/v1/live/diy-list',
		method: 'get',
		params: params
	})
}

/**
 * 直播搜索记录页数据（热门搜索）
 */
export function getLiveHotSearch() {
	return request({
		url: '/mall-sms/app-api/v1/live/rooms/hot-search',
		method: 'get'
	})
}

/**
 * 直播间列表（按状态筛选）
 * @param {String} status 状态(空-全部;0-未开播;1-直播中;2-离开)
 */
export function getLiveRoomList(status) {
	return request({
		url: '/mall-sms/app-api/v1/live/rooms',
		method: 'get',
		params: { status: status }
	})
}

/**
 * 直播间详情
 * @param {Number|String} id 直播间ID
 */
export function getLiveRoomDetail(id) {
	return request({
		url: '/mall-sms/app-api/v1/live/rooms/' + id,
		method: 'get'
	})
}

/**
 * 直播间商品橱窗
 * @param {Number|String} id 直播间ID
 */
export function getLiveRoomGoods(id) {
	return request({
		url: '/mall-sms/app-api/v1/live/rooms/' + id + '/goods',
		method: 'get'
	})
}

/**
 * 直播搜索初始化数据
 */
export function getLiveSearchInit() {
	return request({
		url: '/mall-sms/app-api/v1/live/search/tabs',
		method: 'get'
	})
}

/**
 * 直播搜索列表
 * @param {String} keyword 关键词
 * @param {String} status 状态(空-全部;0-未开播;1-直播中;2-离开)
 * @param {Number} page 页码（默认1）
 * @param {String} by 排序依据(default-默认;hot-人气;new-最新)
 */
export function searchLiveRooms(keyword, status, page, by) {
	return request({
		url: '/mall-sms/app-api/v1/live/rooms/search',
		method: 'get',
		params: { keyword: keyword, status: status, page: page || 1, pageSize: 20, by: by || '' }
	})
}
