/**
 * 内容管理-自定义页面 API（mall-cms）
 */
import request from "@/utils/request";
import type {
  CustomViewQuery,
  CustomViewPageVO,
  CustomViewDetailVO,
  CustomViewForm,
} from "./custom-view/types";

const BASE = "/mall-cms/api/v1/custom-view";

/** 自定义页面分页列表 */
export function getCustomViewPage(query: CustomViewQuery) {
  return request<any, PageResult<CustomViewPageVO>>({
    url: `${BASE}/page`,
    method: "get",
    params: query,
  });
}

/** 自定义页面详情(编辑回显,含整页代码) */
export function getCustomViewDetail(id: number) {
  return request<any, CustomViewDetailVO>({
    url: `${BASE}/${id}`,
    method: "get",
  });
}

/** 新增自定义页面 */
export function addCustomView(data: CustomViewForm) {
  return request({
    url: BASE,
    method: "post",
    data,
  });
}

/** 编辑自定义页面(支持局部字段更新,行内开关只传 {isEnable}) */
export function updateCustomView(id: number, data: Partial<CustomViewForm>) {
  return request({
    url: `${BASE}/${id}`,
    method: "put",
    data,
  });
}

/** 删除自定义页面(逻辑删除) */
export function deleteCustomView(id: number) {
  return request({
    url: `${BASE}/${id}`,
    method: "delete",
  });
}
