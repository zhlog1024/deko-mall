/* 自定义页面接口。 */
import {request} from '@/config/request'

/**
 * 自定义页面详情(启用且未删,访问量+1)
 *
 * @param {Number|String} id 页面ID
 * @return {Object} {id,name,logo,htmlContent,isHeader,isFooter,isFullScreen}
 */
export function getCustomViewDetail(id) {
	return request({
		url: '/mall-cms/app-api/v1/custom-view/' + id,
		method: 'get'
	})
}
