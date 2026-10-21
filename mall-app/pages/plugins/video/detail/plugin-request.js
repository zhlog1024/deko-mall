/* 将视频页面操作映射到 mall-cms 视频接口，响应统一包装为 {code, data}。 */
import { request } from '@/config/request'

/**
 * 通用请求：把源 action 映射到 mall 端点并返回源结构包装
 *
 * @param {String} action 源 action 名（detail/lastnextdata/comments/commentsreplylist/data/givethumbs/delete/report）
 * @param {Object} data   源请求体（字段按源语义，内部映射到 mall 契约）
 * @returns {Promise<{code:Number, data:Object, msg:String}>} 包装后响应（上层 data.code==0 判断不变）
 */
export function videoPluginRequest(action, data = {}) {
	// action → {url片段, method, 字段映射}
	const M = {
		detail: { url: '/mall-cms/app-api/v1/video-plugin/detail', method: 'GET', map: { id: 'id' } },
		lastnextdata: { url: '/mall-cms/app-api/v1/video-plugin/lastnextdata', method: 'GET', map: { id: 'id', is_last: 'isLast', is_next: 'isNext' } },
		// 评论顶层分页（源 data 端点：加载某视频评论列表）
		data: { url: '/mall-cms/app-api/v1/video-plugin/comments', method: 'GET', map: { id: 'videoId', page: 'pageNum' } },
		// 评论回复分页（pid 子级）
		commentsreplylist: { url: '/mall-cms/app-api/v1/video-plugin/comments', method: 'GET', map: { video_id: 'videoId', video_comments_id: 'pid', page: 'pageNum', page_size: 'pageSize' } },
		// 发表评论/回复（POST body）
		comments: { url: '/mall-cms/app-api/v1/video-plugin/comments', method: 'POST', map: { video_id: 'videoId', video_comments_id: 'pid', reply_comments_id: 'replyPid', content: 'content', images: 'images' } },
		// 点赞/取消（POST）
		givethumbs: { url: '/mall-cms/app-api/v1/video-plugin/givethumbs', method: 'POST', map: { video_id: 'videoId', video_comments_id: 'commentId' } },
		// 删除评论（DELETE，id 在路径）
		delete: { url: '/mall-cms/app-api/v1/video-plugin/comments/{id}', method: 'DELETE', map: { id: 'id' } },
		// 举报（POST；源提交 {id, reason, type} → mall {videoId, content, type}）
		report: { url: '/mall-cms/app-api/v1/video-plugin/report', method: 'POST', map: { id: 'videoId', reason: 'content', type: 'type' } },
	}
	const conf = M[action]
	if (!conf) {
		return Promise.reject({ code: -1, msg: '未知请求 action: ' + action })
	}
	// 源参数 → mall 参数
	const mallParams = {}
	const mallBody = {}
	Object.keys(data).forEach((k) => {
		const target = conf.map[k]
		if (target == null) {
			return
		}
		// 显式空值按源语义（如 is_last 0）也要传 → 全部透传目标键
		if (conf.method === 'GET') {
			mallParams[target] = data[k]
		} else {
			mallBody[target] = data[k]
		}
	})
	// delete 路径注入
	let url = conf.url
	if (url.indexOf('{id}') > -1) {
		url = url.replace('{id}', (data.id != null ? data.id : (mallBody.id || mallParams.id || '')))
	}
	const options = {
		url: url,
		method: conf.method.toLowerCase(),
	}
	if (conf.method === 'GET') {
		options.params = mallParams
	} else if (conf.method !== 'DELETE') {
		options.data = mallBody
	} else {
		options.params = {}
	}
	// mall request 返回 {code:'00000', data, msg} → 包装成源结构 {code:0, data}
	return request(options).then((res) => {
		if (res != null && res.code === '00000') {
			return { code: 0, data: res.data, msg: res.msg || '' }
		}
		return { code: (res != null && res.code != null) ? res.code : -1, data: null, msg: (res != null && res.msg != null) ? res.msg : '请求失败', login: res != null && res.code === 'A0230' }
	}).catch((err) => {
		return { code: -1, data: null, msg: (err != null && err.msg != null) ? err.msg : '网络错误', login: false }
	})
}
