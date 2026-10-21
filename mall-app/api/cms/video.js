/* 视频接口：游客可读，详情接口累计浏览量。 */
import { request } from '@/config/request'

/**
 * 视频分页列表（仅返回已启用未删；keyword/isRecommended/isHot 过滤）
 *
 * @param {Object} params {pageNum, pageSize, keyword, isRecommended, isHot, sortField}
 *                          item 含 title/cover/duration/accessCount/isRecommended/isHot/createTime（不含 videoUrl）
 */
export function listVideos(params) {
	return request({
		url: '/mall-cms/app-api/v1/video/page',
		method: 'get',
		params: params
	})
}

/**
 * 视频详情（浏览数由后端自增；返回含 videoUrl/description）
 *
 * @param {Number|String} id 视频ID
 */
export function getVideoDetail(id) {
	return request({
		url: '/mall-cms/app-api/v1/video/' + id,
		method: 'get'
	})
}
