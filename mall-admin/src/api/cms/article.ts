/**
 * 内容管理-文章 API（mall-cms）
 */
import request from "@/utils/request";
import type {
  ArticleQuery,
  ArticlePageVO,
  ArticleDetailVO,
  ArticleForm,
  CategoryNode,
  CategoryForm,
} from "./article/types";

const BASE = "/mall-cms/api/v1/article";
const CATEGORY_BASE = "/mall-cms/api/v1/article-category";

/** 文章分页列表 */
export function getArticlePage(query: ArticleQuery) {
  return request<any, PageResult<ArticlePageVO>>({
    url: `${BASE}/page`,
    method: "get",
    params: query,
  });
}

/** 文章详情(编辑回显) */
export function getArticleDetail(id: number) {
  return request<any, ArticleDetailVO>({
    url: `${BASE}/${id}`,
    method: "get",
  });
}

/** 新增文章 */
export function addArticle(data: ArticleForm) {
  return request({
    url: BASE,
    method: "post",
    data,
  });
}

/** 编辑文章 */
export function updateArticle(id: number, data: ArticleForm) {
  return request({
    url: `${BASE}/${id}`,
    method: "put",
    data,
  });
}

/** 删除文章(逻辑删除) */
export function deleteArticle(id: number) {
  return request({
    url: `${BASE}/${id}`,
    method: "delete",
  });
}

/** 文章分类树(含停用,管理用) */
export function getCategoryTree() {
  return request<any, CategoryNode[]>({
    url: `${CATEGORY_BASE}/tree`,
    method: "get",
  });
}

/** 新增文章分类 */
export function addCategory(data: CategoryForm) {
  return request({
    url: CATEGORY_BASE,
    method: "post",
    data,
  });
}

/** 编辑文章分类 */
export function updateCategory(id: number, data: CategoryForm) {
  return request({
    url: `${CATEGORY_BASE}/${id}`,
    method: "put",
    data,
  });
}

/** 删除文章分类 */
export function deleteCategory(id: number) {
  return request({
    url: `${CATEGORY_BASE}/${id}`,
    method: "delete",
  });
}
