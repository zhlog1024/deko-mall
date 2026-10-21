import request from "@/utils/request";

/**
 * 获取商品属性列表
 */
export function getAttributeList(params: object) {
  return request({
    url: "/mall-pms/scm-api/v1/attributes",
    method: "get",
    params: params,
  });
}

/**
 * 批量修改商品属性
 */
export function saveAttributeBatch(data: object) {
  return request({
    url: "/mall-pms/scm-api/v1/attributes/batch",
    method: "post",
    data: data,
  });
}
