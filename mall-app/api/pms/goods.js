import {request} from '@/config/request'
/**
 * 获取商品分页列表
 * 
 * @param {Object} params
 */
export function listSpuPages(params) {
	return request({
		url: '/mall-pms/app-api/v1/spu/pages',
		method: 'get',
		params: params
	})
}

/**
 * 获取秒杀商品列表
 * 
 * @param {Object} params
 */
export function listSeckillingSpus() {
	return request({
		url: '/mall-pms/app-api/v1/spu/seckilling',
		method: 'get',
	})
}

/**
 * 获取商品详情
 *
 * @param {Object} spuId
 */
export function getSpuDetail(spuId) {
	return request({
		url: '/mall-pms/app-api/v1/spu/' + spuId,
		method: 'get',
		headers: {
			'auth': true // 需要认证，通过
		}
	})
}


// 获取商品列表
export function getGoodsList(params) {
	return request({
		url: '/mall-pms/app-api/v1/spu-groups/page',
		method: 'post',
		data: params,
		// headers: {
		// 	'auth': true // 需要认证，通过
		// }
	})
}

/**
 * 获取筛选商品列表（SpuPageVO 格式，用于装修组件）
 * 
 * @param {Object} params
 */
export function getFilteredSpuList(params) {
	return request({
		url: '/mall-pms/app-api/v1/spu-groups/page/spu-page-vo',
		method: 'post',
		data: params
	})
}
