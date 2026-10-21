/**
 * 分页查询基础类型
 */
export interface PageQuery {
  pageNum: number;
  pageSize: number;
}

/**
 * 分页结果基础类型
 */
export interface PageResult<T> {
  list: T;
  total: number;
}

/**
 * 后端统一响应信封（request 拦截器已返回 response.data）
 *
 * 业务侧仍需判断 code === '00000'。
 */
export interface ApiResult<T> {
  code: string;
  msg: string;
  data: T;
}
