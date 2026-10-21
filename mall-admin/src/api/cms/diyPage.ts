import request from "@/utils/request";
import type { PageQuery } from "@/api/types";

export interface DiyPageItem {
  id: number;
  name: string;
  coverUrl?: string;
  isHome?: boolean;
  isEnabled?: boolean;
  createTime?: string;
  updateTime?: string;
}

export interface DiyPagePageQuery extends PageQuery {
  keyword?: string;
  isEnabled?: boolean | undefined;
}

import type { PageResult } from "@/api/types";

/**
 * 装修页面分页结果类型
 * 后端 PageResult.success() 返回：{ code: "00000", data: { list: [...], total: number }, msg: "..." }
 * request 拦截器返回整个响应对象，所以 res.data 就是 { list: [...], total: number }
 */
export type DiyPagePageResult = PageResult<DiyPageItem[]>;

/**
 * 获取装修页面分页列表（对应后端 GET /mall-cms/api/v1/diy-pages/pages）
 * 返回格式：{ code: "00000", data: { list: DiyPageItem[], total: number }, msg: "..." }
 */
export function getDiyPageList(params: DiyPagePageQuery) {
  return request<{ data: DiyPagePageResult }>({
    url: "/mall-cms/api/v1/diy-pages/pages",
    method: "get",
    params,
  });
}

/**
 * 删除装修页面
 */
export function deleteDiyPage(id: number) {
  return request({
    url: "/mall-cms/api/v1/diy-pages/" + id,
    method: "delete",
  });
}

/**
 * 更新页面启用状态
 */
export function updateDiyPageEnabled(id: number, isEnabled: boolean) {
  return request({
    url: "/mall-cms/api/v1/diy-pages/" + id + "/enabled",
    method: "put",
    data: { isEnabled },
  });
}

/**
 * 复制装修页面
 * @returns 新页面ID
 */
export function copyDiyPage(id: number) {
  return request<{ data: number }>({
    url: "/mall-cms/api/v1/diy-pages/" + id + "/copy",
    method: "post",
  });
}

/**
 * DIY首页配置项类型
 */
export interface DiyHomeModeItem {
  diyId?: number | null;
  timeStart?: string | null;
  timeEnd?: string | null;
}

/**
 * DIY首页按端配置（key: weixin/app/h5）
 */
export type DiyHomeModeConfig = Record<string, DiyHomeModeItem>;

/**
 * 读取首页DIY配置（GET /mall-cms/api/v1/diy-pages/home-mode）
 */
export function getDiyHomeMode() {
  return request<{ data: DiyHomeModeConfig | null }>({
    url: "/mall-cms/api/v1/diy-pages/home-mode",
    method: "get",
  });
}

/**
 * 保存首页DIY配置（PUT /mall-cms/api/v1/diy-pages/home-mode）
 */
export function saveDiyHomeMode(config: DiyHomeModeConfig) {
  return request({
    url: "/mall-cms/api/v1/diy-pages/home-mode",
    method: "put",
    data: config,
  });
}
