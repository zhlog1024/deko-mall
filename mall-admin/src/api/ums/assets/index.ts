import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 资产流水
 */
export function getAssetFlow(data) {
  return request({
    url: "/mall-ums/api/v1/asset/flow",
    method: "post",
     params: data,
  });
}

// 新增
export function addAssetFlow(data) {
  return request({
    url: "/mall-ums/api/v1/asset/add",
    method: "post",
    params: data,
  });
}

// 扣减
export function subAssetFlow(data) {
  return request({
    url: "/mall-ums/api/v1/asset/deduct",
    method: "post",
    params: data,
  });
}


// 获取用户资产
export function getAssetList(data) {
  return request({
    url: "/mall-ums/api/v1/asset/list",
    method: "post",
    params: data, 
  })
}
