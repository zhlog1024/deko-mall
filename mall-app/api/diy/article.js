/* 文章接口。 */
import {request} from '@/config/request'

/**
 * 获取文章分类树
 *
 * @return {Array} 分类树 [{id,pid,name,children}]
 */
export function getArticleCategoryTree() {
	return request({
		url: '/mall-cms/app-api/v1/article/category-tree',
		method: 'get'
	})
}

/**
 * 获取文章分页列表
 *
 * @param {Object} params {categoryId, onlyCover, keyword, orderByType, orderByRule, pageNum, pageSize}
 */
export function listArticles(params) {
	return request({
		url: '/mall-cms/app-api/v1/article/list',
		method: 'get',
		params: params
	})
}

/**
 * 获取文章详情
 *
 * @param {Number|String} id 文章ID
 */
export function getArticleDetail(id) {
	return request({
		url: '/mall-cms/app-api/v1/article/' + id,
		method: 'get'
	})
}
