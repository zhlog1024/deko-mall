import { request } from '@/config/request'

/**
 * 获取首页装修配置
 */
export function getHomeDiyPage() {
	return request({
		url: '/mall-cms/app-api/v1/diy-pages/home',
		method: 'get'
	})
}

/**
 * 获取 App 底部导航配置（数据自治：DIY 域数据由 mall-diy 提供，不再依赖 mall-system config/common 下发）
 */
export function getAppTabbar() {
	return request({
		url: '/mall-cms/app-api/v1/diy-pages/app-tabbar',
		method: 'get'
	})
}

/**
 * 获取指定DIY页面配置
 * @param {Number} id 页面ID
 * @param {Number} is_cache 是否使用缓存 0-优先缓存 1-强制刷新
 */
export function getDiyPageDetail(id, is_cache = 1) {
	return request({
		url: `/mall-cms/app-api/v1/diy-pages/${id}`,
		method: 'get',
		data: {
			is_cache: is_cache
		}
	})
}
