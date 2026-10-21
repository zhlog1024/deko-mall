import request from "@/utils/request";
import { AxiosPromise } from "axios";
import {
  DictTypeQuery,
  DictTypePageResult,
  DictTypeForm,
  DictQuery,
  DictForm,
  DictPageResult,
} from "./types";

/**
 * 字典类型分页列表
 */
export function getDictTypePage(
  queryParams: DictTypeQuery
): AxiosPromise<DictTypePageResult> {
  return request({
    url: "/mall-system/api/v1/dict/types/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * 字典类型表单数据
 */
export function getDictTypeForm(id: number): AxiosPromise<DictTypeForm> {
  return request({
    url: "/mall-system/api/v1/dict/types/" + id + "/form",
    method: "get",
  });
}

/**
 * 新增字典类型
 */
export function addDictType(data: DictTypeForm) {
  return request({
    url: "/mall-system/api/v1/dict/types",
    method: "post",
    data: data,
  });
}

/**
 * 修改字典类型
 */
export function updateDictType(id: number, data: DictTypeForm) {
  return request({
    url: "/mall-system/api/v1/dict/types/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除字典类型
 */
export function deleteDictTypes(ids: string) {
  return request({
    url: "/mall-system/api/v1/dict/types/" + ids,
    method: "delete",
  });
}

/**
 * 获取字典类型的数据项
 *
 * @param typeCode 字典类型编码
 */
export function getDictOptions(typeCode: string): AxiosPromise<OptionType[]> {
  return request({
    url: "/mall-system/api/v1/dict/types/" + typeCode + "/items",
    method: "get",
  });
}

/**
 * 字典分页列表
 */
export function getDictPage(
  queryParams: DictQuery
): AxiosPromise<DictPageResult> {
  return request({
    url: "/mall-system/api/v1/dict/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取字典表单数据
 */
export function getDictFormData(id: number): AxiosPromise<DictForm> {
  return request({
    url: "/mall-system/api/v1/dict/" + id + "/form",
    method: "get",
  });
}

/**
 * 新增字典
 */
export function addDict(data: DictForm) {
  return request({
    url: "/mall-system/api/v1/dict",
    method: "post",
    data: data,
  });
}

/**
 * 修改字典项
 */
export function updateDict(id: number, data: DictForm) {
  return request({
    url: "/mall-system/api/v1/dict/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除字典
 *
 * @param ids 字典项ID，多个以英文逗号(,)分割
 */
export function deleteDict(ids: string) {
  return request({
    url: "/mall-system/api/v1/dict/" + ids,
    method: "delete",
  });
}
