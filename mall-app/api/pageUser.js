import {request} from '@/config/request'

// 查看物流

export function getlogistics(params) {
	return request({
		
		url:'/mall-oms/app-api/v1/logistics',
		method: 'get',
		headers: {
			'auth': true
		},
		params,
	})
}

// 查看物流公司信息
export function getdeliveryOrderInfo(orderId) {
	return request({
		url:`/mall-oms/api/v1/orders/delivery/${orderId}`,
		method: 'get',
		headers: {
			'auth': true
		},
	})
}

// 余额收益

export function assetFlow(params) {
	return request({
		url:`/mall-ums/app-api/v1/members/asset-flow`,
		method: 'post',
		headers: {
			'auth': true
		},
		data:params,
	})
}

// 用户积分获取
export function getUserPoints(params) {
	return request({
		url:`/mall-ums/app-api/v1/points`,
		method: 'get',
		headers: {
			'auth': true
		},
	})
}	

// 积分流水

export function getPointsList(params) {
	return request({
		url:`/mall-ums/app-api/v1/points/flow`,
		method: 'post',
		headers: {
			'auth': true
		},
		params:params,
	})
}	

// 获取会员资产

export function getAsset(params) {
		return request({
		url:`/mall-ums/app-api/v1/asset/get`,
		method: 'get',
		headers: {
			'auth': true,
			"content-type": "application/x-www-form-urlencoded",
		},
		params:params,
	})
}

// 获取资产流水
export function getAssetFlow(params) {
	return request({
		url:`/mall-ums/app-api/v1/asset/flow`,
		method: 'post',
		headers: {
			'auth': true
		},
		params:params,
	})	
}

// 积分转账

export function transferPoints(params) {
	return request({
		url:`/mall-ums/app-api/v1/asset/transfer`,
		method: 'post',
		headers: {
			'auth': true
		},
		data:params,
	})	
}
