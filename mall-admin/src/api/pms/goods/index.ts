import { GoodsDetail, GoodsPageResult, GoodsQuery } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 获取商品分页列表
 */
export function getSpuPage(
  queryParams: GoodsQuery
): AxiosPromise<GoodsPageResult> {
  return request({
    url: "/mall-pms/api/v1/spu/page",
    method: "get",
    params: queryParams,
  });
}


// 获取商品skuList
export function getSKuPage(queryParams:any) {
  return request({
    url: "/mall-pms/api/v1/sku/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * 实时计算SKU价格
 */
export function calculateSkuPrice(data: {
  id?: number;
  retailPrice?: number;
  supplyPrice?: number;
}) {
  return request({
    url: "/mall-pms/api/v1/sku/calculate-price",
    method: "post",
    data,
  });
}

/**
 * 实时计算SKU毛利率
 */
export function calculateSkuMargin(data: {
  id?: number;
  retailPrice?: number;
  supplyPrice?: number;
}) {
  return request({
    url: "/mall-pms/api/v1/sku/calculate-margin",
    method: "post",
    data,
  });
}

/**
 * 计算SPU定价信息（价格+毛利率+商品类型）
 */
export function calculateSpuPricing(data: {
  spuId?: number;
  spuType?: string;
  skuList: Array<{
    id?: number;
    retailPrice?: number;
    supplyPrice?: number;
  }>;
}) {
  return request({
    url: "/mall-pms/api/v1/spu/calculate-pricing",
    method: "post",
    data,
  });
}


/**
 * 获取商品详情
 */
export function getSpuDetail(id: string): AxiosPromise<GoodsDetail> {
  return request({
    url: "/mall-pms/api/v1/spu/" + id + "/detail",
    method: "get",
  });
}

/**
 * 添加商品
 */
export function addSpu(data: object) {
  return request({
    url: "/mall-pms/api/v1/spu",
    method: "post",
    data: data,
  });
}

/**
 * 修改商品
 */
export function updateSpu(id: number, data: object) {
  return request({
    url: "/mall-pms/api/v1/spu/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除商品
 */
export function deleteSpu(ids: string) {
  return request({
    url: "/mall-pms/api/v1/spu/" + ids,
    method: "delete",
  });
}

/**
 * 审核商品
 */
export function auditSpu(id: string | number, data: any) {
  return request({
    url: `/mall-pms/api/v1/spu/audit/${id}`,
    method: "put",
    data,
  });
}

/**
 * 上架商品
 * 
 * @param id 商品ID
 */
export function publishSpu(id: string | number) {
  return request({
    url: `/mall-pms/api/v1/spu/${id}/publish`,
    method: "put",
  });
}

/**
 * 下架商品
 * 
 * @param id 商品ID
 */
export function unpublishSpu(id: string | number) {
  return request({
    url: `/mall-pms/api/v1/spu/${id}/unpublish`,
    method: "put",
  });
}

/**
 * 批量上架商品
 *
 * @param ids 商品ID列表
 */
export function batchPublishSpu(ids: (string | number)[]) {
  return request({
    url: `/mall-pms/api/v1/spu/batch/publish`,
    method: "put",
    data: ids,
  });
}

/**
 * 批量下架商品
 *
 * @param ids 商品ID列表
 */
export function batchUnpublishSpu(ids: (string | number)[]) {
  return request({
    url: `/mall-pms/api/v1/spu/batch/unpublish`,
    method: "put",
    data: ids,
  });
}
