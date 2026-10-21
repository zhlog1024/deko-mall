import { AdvertForm, AdvertPageResult, AdvertQuery } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 获取广告分页列表
 */
export function getAdvertPage(
  queryParams: AdvertQuery
): AxiosPromise<AdvertPageResult> {
  return request({
    url: "/mall-sms/api/v1/adverts/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取广告详情
 */
export function getAdvertForm(id: number): AxiosPromise<AdvertForm> {
  return request({
    url: "/mall-sms/api/v1/adverts/" + id,
    method: "get",
  });
}

/**
 * 添加广告
 */
export function addAdvert(data: AdvertForm) {
  return request({
    url: "/mall-sms/api/v1/adverts",
    method: "post",
    data: data,
  });
}

/**
 * 修改广告
 */
export function updateAdvert(id: number, data: AdvertForm) {
  return request({
    url: "/mall-sms/api/v1/adverts/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除广告
 */
export function deleteAdverts(ids: string) {
  return request({
    url: "/mall-sms/api/v1/adverts/" + ids,
    method: "delete",
  });
}
