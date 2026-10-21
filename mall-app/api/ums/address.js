
import {request} from '@/config/request'


export function list(memberId) {
	return request({
		url: '/mall-ums/app-api/v1/addresses',
		method: 'get',
		params: {
			memberId: memberId
		},
		headers: {
			'auth': true // 需要认证
		}
	})
}


export function add(data) {
	return request({
		url: '/mall-ums/app-api/v1/addresses',
		method: 'post',
		data: data,
		headers: {
			'auth': true // 需要认证
		}
	})
}


export function update(id, data) {
	return request({
		url: '/mall-ums/app-api/v1/addresses/' + id,
		method: 'put',
		data: data,
		headers: {
			'auth': true // 需要认证
		}
	})
}


export function del(id) {
	return request({
		url: '/mall-ums/app-api/v1/addresses/' + id,
		method: 'delete',
		headers: {
			'auth': true // 需要认证
		}
	})
}

// 智能地址识别
export function parseAddress(rawAddress) {
	return request({
		url: '/mall-ums/app-api/v1/addresses/parse',
		method: 'post',
		data: { rawAddress },
		headers: {
			'auth': true
		}
	})
}
