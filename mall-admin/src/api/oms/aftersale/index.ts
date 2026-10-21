import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { AfterSaleQuery } from "./types";

/**
 * 获取售后单列表
 * @param queryParams 查询参数
 * @returns 
 */
export function getAfterSalePage(queryParams: AfterSaleQuery): AxiosPromise<any> {
  return request({
    url: "/mall-oms/api/v1/orders/aftersales",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取售后单详情
 * @param id 售后单ID
 * @returns 
 */
export function getAfterSaleDetail(id: string | number): AxiosPromise<any> {
  return request({
    url: `/mall-oms/api/v1/orders/aftersales/${id}`,
    method: "get",
  });
}

/**
 * 审核售后申请
 * @param id 售后单ID
 * @param data 审核信息
 * @returns 
 */
export function auditAfterSale(id: string | number, data: any): AxiosPromise<any> {
  return request({
    url: `/mall-oms/api/v1/orders/aftersales/${id}/audit`,
    method: "post",
    params: data,
  });
}

/**
 * 收货确认
 * @param id 售后单ID
 * @param approved 确认结果，true为确认收货，false为拒绝收货
 * @param remark 备注信息（可选）
 * @returns
 */
export function confirmReceive(id: string | number, approved: boolean, remark?: string): AxiosPromise<any> {
  return request({
    url: `/mall-oms/api/v1/orders/aftersales/${id}/receive`,
    method: "post",
    params: { approved, remark }
  });
}

/**
 * 执行退款
 * @param id 售后单ID
 * @returns 
 */
export function executeAfterSale(id: string | number): AxiosPromise<any> {
  return request({
    url: `/mall-oms/api/v1/orders/aftersales/${id}/refund`,
    method: "post",
  });
}

/**
 * 导出售后单
 * @param queryParams 查询参数
 * @returns 
 */
export function exportAfterSales(queryParams: AfterSaleQuery): AxiosPromise<any> {
  return request({
    url: "/mall-oms/api/v1/orders/aftersales/export",
    method: "get",
    params: queryParams,
    responseType: "arraybuffer",
  });
}

/**
 * 获取售后统计数据
 * @param queryParams 查询参数
 * @returns 
 */
export function sumAfterSaleAmount(queryParams: AfterSaleQuery): AxiosPromise<any> {
  return request({
    url: "/mall-oms/api/v1/orders/aftersales/statistics",
    method: "get",
    params: queryParams,
  });
}

/**
 * 根据物流单号查询物流详情
 * @param trackingNo 物流单号
 * @returns 物流详情
 */
export function getLogisticsDetail(trackingNo: string): AxiosPromise<any> {
  return request({
    url: "/mall-oms/api/v1/logistics/detail",
    method: "get",
    params: { trackingNo }
  });
}
