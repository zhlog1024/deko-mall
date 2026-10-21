import { request } from '@/config/request'

/**
 * 获取 App 端全局配置
 * 对应后端：/mall-system/app-api/v1/config/common
 */
export function getCommonConfig() {
	return request({
		url: '/mall-system/app-api/v1/config/common',
		method: 'get'
	})
}
