import request from "@/utils/request";
import { AxiosPromise } from "axios";

// 分页列表
export function getSpuGroups(queryParams?: any): AxiosPromise<any> {
  return request({
    url: "/mall-pms/pms-api/v1/spu-groups/pages",
    method: "get",
    params: queryParams,
  });
}

// 启用分组列表
export function listEnabledSpuGroups(): AxiosPromise<any> {
  return request({
    url: "/mall-pms/pms-api/v1/spu-groups/enabled",
    method: "get",
  });
}

// 获取详情
export function getSpuGroupDetail(id: number): AxiosPromise<any> {
  return request({
    url: `/mall-pms/pms-api/v1/spu-groups/${id}`,
    method: "get",
  });
}

// 获取分组商品详情列表
export function getSpuGroupSpus(id: number): AxiosPromise<any> {
  return request({
    url: `/mall-pms/pms-api/v1/spu-groups/${id}/spus/details`,
    method: "get",
  });
}

// 创建分组（含商品）
export function createSpuGroup(data: any): AxiosPromise<any> {
  return request({
    url: "/mall-pms/pms-api/v1/spu-groups/with-spus",
    method: "post",
    data,
  });
}

// 更新分组（含商品）
export function updateSpuGroup(id: number, data: any): AxiosPromise<any> {
  return request({
    url: `/mall-pms/pms-api/v1/spu-groups/${id}/with-spus`,
    method: "put",
    data,
  });
}

// 删除分组
export function deleteSpuGroup(id: number): AxiosPromise<any> {
  return request({
    url: `/mall-pms/pms-api/v1/spu-groups/${id}`,
    method: "delete",
  });
}

// 获取商品（SpuPageVO 格式，用于装修组件）
export function getSpuList(queryParams?: any): AxiosPromise<any> {
  return request({
    url: "/mall-pms/app-api/v1/spu-groups/page/spu-page-vo",
    method: "post",
    data: queryParams,
  });
}

// 根据ID批量查询SPU信息
export function batchGetSpuInfo(ids: number[]): AxiosPromise<any> {
  return request({
    url: "/mall-pms/api/v1/spu/batch-info",
    method: "post",
    data: ids,
  });
}
