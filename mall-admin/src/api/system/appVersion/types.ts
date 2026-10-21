/**
 * APP版本查询参数
 */
export interface AppVersionQuery extends PageQuery {
  keywords?: string;
  platform?: string;
  isEnabled?: number;
}

/**
 * APP版本分页对象
 */
export interface AppVersionPageVO {
  id: number;
  versionName: string;
  versionCode: string;
  platform: string;
  updateUrl?: string;
  updateContent?: string;
  isForceUpdate?: number;
  isEnabled?: number;
  createTime?: string;
  updateTime?: string;
}

/**
 * APP版本分页
 */
export type AppVersionPageResult = PageResult<AppVersionPageVO[]>;

/**
 * APP版本表单
 */
export interface AppVersionForm {
  id?: number;
  versionName: string;
  versionCode: string;
  platform: string;
  updateUrl?: string;
  updateContent?: string;
  isForceUpdate?: number;
  isEnabled: number;
}

/**
 * APP版本详情
 */
export type AppVersionDetail = AppVersionPageVO;
