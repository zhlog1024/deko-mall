/**
 * 用户相关api
 */

import {request} from '@/config/request'



export function login(params) {
	return request({
		url: '/mall-auth/oauth2/token',
		// url:'api/auth/login',
		method: 'post',
		params,
		headers: {
			'Authorization': 'Basic bWFsbC1hcHA6MTIzNDU2' // 客户端信息Base64加密，明文：mall-app:123456
		}
	})
}




export function logout() {
	return request({
		url: '/mall-auth/oauth/logout',
		method: 'delete',
		headers: {
			'auth': true // 需要认证，通过
		}
	})
}

export function getUserInfo() {
	return request({
		url: '/mall-ums/app-api/v1/members/me',
		method: 'get',
		headers: {
			'auth': true
		}
	})
}

export function resetPassword(params) {
	return request({
		url: '/mall-ums/app-api/v1/members/reset-password',
		method: 'post',
		data: params,
	})
}

export function sendSmsCode(phoneNumber, countryCode) {
	return request({
		url: '/mall-system/api/v1/msg/register/sms_code',
		// url:'/mall-auth/api/v1/auth/sms_code',
		method: 'post',
		headers: {
			'Authorization': 'Basic bWFsbC1hcHA6MTIzNDU2' // 客户端信息Base64加密，明文：mall-app:123456
		},
		params: {
			mobile: phoneNumber,
			countryCode: countryCode || '+86',
			scene: 'REGISTER'

		}
	})
}
export const findAllProvince = (data) => {
	return request({
	  url: '/mall-system/api/v1/region/findAllProvince',
	  method: 'get',
	  headers: {
		'auth': true
	},
	params: data
	})
  }
export const findAllCountry = () => {
	return request({
	  url: '/mall-system/api/v1/region/findAllCountry',
	  method: 'get',
	  headers: {
		'auth': true
	},
	})
  }
  export const findAllCityByPid = data => {
	return request({
	  url: '/mall-system/api/v1/region/findAllCityByPid',
	  method: 'get',
	  headers: {
		'auth': true
	},
	params:data
	})
  }
  export const findAllDistrictByPid = data => {
	return request({
	  url: '/mall-system/api/v1/region/findAllDistrictByPid',
	  method: 'get',
	  headers: {
		'auth': true
	},
	params:data
	})
  }
  export const findAllTownByPid = data => {
	return request({
	  url: '/mall-system/api/v1/region/findAllTownByPid',
	  method: 'get',
	  headers: {
		'auth': true
	},
	params:data
  })
  }

//  更新会员信息
export function updateMembers(params) {
	return request({
		// url: '/mall-system/api/v1/msg/sms_code',
		url:'/mall-ums/app-api/v1/members/update',
		method: 'post',
		headers: {
			'auth': true
		},
		data:params,
	})
}
//  注销用户
export function delMembers(params) {
	return request({
		url:'/mall-ums/app-api/v1/members/del',
		method: 'post',
		headers: {
			'auth': true
		},
	})
}

// 获取用户资产列表
// 
export function getAssetList(params) {
	return request({
		url:'/mall-ums/app-api/v1/asset/list',
		method: 'post',
		headers: {
			'auth': true
		},
	})
}

  // 获取微信小程序菊花码
  export const getQrCode = data => {
	return request({
	  url: '/mall-system/api/v1/files/wxacode',
	  method: 'get',
	  headers: {
		'auth': true
	},
	params:data
  })
  }

  // 获取标准 QR 码（任何扫码工具都能识别）
  export const getStandardQrcode = content => {
	return request({
	  url: '/mall-system/api/v1/files/qrcode',
	  method: 'get',
	  headers: {
		'auth': true
	},
	params: { content: content }
  })
  }


