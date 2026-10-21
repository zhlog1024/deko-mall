/**
 * 内容管理-自定义页面 类型定义（mall-cms）
 */

// 自定义页面分页查询
export interface CustomViewQuery {
  pageNum: number;
  pageSize: number;
  keywords?: string;
  isEnable?: number | undefined;
}

// 自定义页面列表项（管理端,不含正文代码,避免大字段）
export interface CustomViewPageVO {
  id: number;
  name: string;
  logo?: string;
  isEnable: number;
  isHeader: number;
  isFooter: number;
  isFullScreen: number;
  accessCount: number;
  seoTitle?: string;
  seoKeywords?: string;
  seoDesc?: string;
  createTime: string;
  updateTime?: string;
}

// 自定义页面详情(编辑回显,含整页代码)
export interface CustomViewDetailVO extends CustomViewPageVO {
  htmlContent?: string;
  cssContent?: string;
  jsContent?: string;
}

// 自定义页面新增/编辑表单(PUT 支持局部更新,行内开关只传 {isEnable})
export interface CustomViewForm {
  id?: number;
  name: string;
  logo?: string;
  htmlContent?: string;
  cssContent?: string;
  jsContent?: string;
  isEnable: number;
  isHeader: number;
  isFooter: number;
  isFullScreen: number;
  seoTitle?: string;
  seoKeywords?: string;
  seoDesc?: string;
}
