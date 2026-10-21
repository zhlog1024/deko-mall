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
 * 禁售区域分页查询参数
 */
export interface ForbiddenAreaPageQuery extends PageQuery {
  regionNames: string;
  regionCodes: string;
  supplierId: number;
}

/**
 * 禁售区域信息
 */
export interface ForbiddenArea {
  id: number;
  supplierId: number;
  regionNames: string;
  regionCodes: string;
  remark: string;
  createTime: string;
  updateTime: string;
}

/**
 * 禁售区域创建/更新参数
 */
export interface ForbiddenAreaForm {
  regionNames: string;
  regionCodes: string;
  remark?: string;
}

/**
 * 对话框类型
 */
export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}

/**
 * 分页结果类型
 */
export type ForbiddenAreaPageResult = PageResult<ForbiddenArea[]>;
