import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { PayConfig, PayConfigPageResult, PayConfigQuery } from "./types";

/**
 * 条件查询PayConfig配置
 */
export function getPayConfigPage(
  queryParams: PayConfigQuery
): AxiosPromise<PayConfigPageResult> {
  return request({
    url: "/mall-pay/api/v1/pay/config/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * 根据ID查询PayConfig配置
 */
export function getPayConfig(id: number): AxiosPromise<PayConfig> {
  return request({
    url: "/mall-pay/api/v1/pay/config/" + id,
    method: "get",
  });
}

/**
 * 更新PayConfig配置
 */
export function updatePayConfig(data: PayConfig) {
  return request({
    url: "/mall-pay/api/v1/pay/config/"+data.id,
    method: "put",
    data: data,
  });
}

/**
 * 新增PayConfig配置
 */
export function addPayConfig(data: PayConfig) {
  return request({
    url: "/mall-pay/api/v1/pay/config",
    method: "post",
    data: data,
  });
}

/** 线下支付审核分页 */
export function getOfflinePayPage(params: any) {
  return request({
    url: "/mall-pay/api/v1/pay/offline/page",
    method: "get",
    params,
  });
}

/** 审核线下支付 */
export function auditOfflinePayment(paymentNo: string, data: { approved: boolean; remark?: string }) {
  return request({
    url: "/mall-pay/api/v1/pay/offline/" + paymentNo + "/audit",
    method: "post",
    params: data,
  });
}
