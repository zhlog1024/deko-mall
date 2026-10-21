import { Shop, ShopPageQuery, ShopPageResult } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

export function getShopPages(
  queryParams: ShopPageQuery
): AxiosPromise<ShopPageResult> {
  return request({
    url: "/mall-pms/api/v1/shops/page",
    method: "get",
    params: queryParams,
  });
}

export function getShop(id: number): AxiosPromise<Shop> {
  return request({
    url: "/mall-pms/api/v1/shops/" + id,
    method: "get",
  });
}

export function updateShop(
  id: number,
  data: { shopName?: string; logo?: string; logoLong?: string; description?: string; sort?: number; status?: number; score?: number | null }
) {
  return request({
    url: "/mall-pms/api/v1/shops/" + id,
    method: "put",
    params: data,
  });
}
