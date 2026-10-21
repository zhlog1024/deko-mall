/* 商品问答接口：游客可读，提问需登录。 */
import { request } from '@/config/request'

/**
 * 问答分页列表（仅返回已启用未删；spuId 空=全部启用问答）
 *
 * @param {Object} params {pageNum, pageSize, spuId, keyword, isReply}
 */
export function listAsks(params) {
	return request({
		url: '/mall-cms/app-api/v1/ask/page',
		method: 'get',
		params: params
	})
}

/**
 * 问答详情（浏览数由后端自增）
 *
 * @param {Number|String} id 问答ID
 */
export function getAskDetail(id) {
	return request({
		url: '/mall-cms/app-api/v1/ask/' + id,
		method: 'get'
	})
}

/**
 * 用户提问（需登录；提交后 is_enable=0，运营确认/回复后展示）
 *
 * @param {Object} data {spuId, spuName, spuPic, title, content}
 */
export function submitAsk(data) {
	return request({
		url: '/mall-cms/app-api/v1/ask',
		method: 'post',
		data: data
	})
}
