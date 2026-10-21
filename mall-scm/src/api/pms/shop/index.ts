import { Shop, ShopUpdateForm } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 获取我的店铺信息（不存在时后端自动创建默认店铺）
 */
export function getMyShop(): AxiosPromise<Shop> {
  return request({
    url: "/mall-pms/scm-api/v1/shop",
    method: "get",
  });
}

/**
 * 修改我的店铺信息（店铺名称/logo/长图/简介）
 */
export function updateMyShop(data: ShopUpdateForm) {
  return request({
    url: "/mall-pms/scm-api/v1/shop",
    method: "put",
    data: data,
  });
}
