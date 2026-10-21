import request from "@/utils/request";
import { AxiosPromise } from "axios";
import {
  AppVersionQuery,
  AppVersionPageResult,
  AppVersionForm,
  AppVersionDetail,
} from "./types";

/**
 * APP版本分页列表
 */
export function getAppVersionPage(
  queryParams: AppVersionQuery
): AxiosPromise<AppVersionPageResult> {
  return request({
    url: "/mall-system/api/v1/app-version/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * APP版本详情
 */
export function getAppVersionDetail(
  id: number
): AxiosPromise<AppVersionDetail> {
  return request({
    url: "/mall-system/api/v1/app-version/" + id,
    method: "get",
  });
}

/**
 * 新增APP版本
 */
export function addAppVersion(data: AppVersionForm) {
  return request({
    url: "/mall-system/api/v1/app-version",
    method: "post",
    data: data,
  });
}

/**
 * 修改APP版本
 */
export function updateAppVersion(id: number, data: AppVersionForm) {
  return request({
    url: "/mall-system/api/v1/app-version/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除APP版本
 */
export function deleteAppVersions(ids: string) {
  return request({
    url: "/mall-system/api/v1/app-version/" + ids,
    method: "delete",
  });
}
