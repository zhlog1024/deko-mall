/**
 * 内容管理-文章 类型定义（mall-cms）
 */

// 文章分页查询
export interface ArticleQuery {
  pageNum: number;
  pageSize: number;
  keywords?: string;
  categoryId?: number;
  isEnable?: number | undefined;
}

// 文章列表项（管理端）
export interface ArticlePageVO {
  id: number;
  title: string;
  description?: string;
  categoryId: number;
  categoryName?: string;
  cover?: string;
  isEnable: number;
  accessCount: number;
  createTime: string;
  updateTime?: string;
}

// 文章详情（编辑回显,含正文）
export interface ArticleDetailVO extends ArticlePageVO {
  titleColor?: string;
  jumpUrl?: string;
  content?: string;
  images?: string;
  shareImages?: string;
  seoTitle?: string;
  seoKeywords?: string;
  seoDesc?: string;
}

// 文章新增/编辑表单
export interface ArticleForm {
  id?: number;
  title: string;
  description?: string;
  categoryId: number;
  titleColor?: string;
  jumpUrl?: string;
  isEnable: number;
  content?: string;
  cover?: string;
  images?: string;
  shareImages?: string;
  seoTitle?: string;
  seoKeywords?: string;
  seoDesc?: string;
}

// 文章分类树节点
export interface CategoryNode {
  id: number;
  pid: number;
  name: string;
  sort?: number;
  isEnable: number;
  children?: CategoryNode[];
}

// 文章分类表单
export interface CategoryForm {
  id?: number;
  pid: number;
  name: string;
  sort: number;
  isEnable: number;
}
