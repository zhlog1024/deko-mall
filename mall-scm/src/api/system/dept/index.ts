import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { DeptForm, DeptQuery, DeptVO } from "./types";

/**
 * 部门树形表格
 */
export function listDepts(queryParams?: DeptQuery): AxiosPromise<DeptVO[]> {
  return request({
    url: "/mall-system/api/v1/dept",
    method: "get",
    params: queryParams,
  });
}

/**
 * 部门下拉列表
 */
export function getDeptOptions(): AxiosPromise<OptionType[]> {
  return request({
    url: "/mall-system/api/v1/dept/options",
    method: "get",
  });
}

/**
 * 获取部门详情
 */
export function getDeptForm(id: number): AxiosPromise<DeptForm> {
  return request({
    url: "/mall-system/api/v1/dept/" + id + "/form",
    method: "get",
  });
}

/**
 * 新增部门
 */
export function addDept(data: DeptForm) {
  return request({
    url: "/mall-system/api/v1/dept",
    method: "post",
    data: data,
  });
}

/**
 * 修改部门
 */
export function updateDept(id: number, data: DeptForm) {
  return request({
    url: "/mall-system/api/v1/dept/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除部门
 */
export function deleteDept(ids: string) {
  return request({
    url: "/mall-system/api/v1/dept/" + ids,
    method: "delete",
  });
}
