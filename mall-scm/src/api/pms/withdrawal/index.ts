import { SupplierWithdrawal, SupplierWithdrawalPageQuery, SupplierWithdrawalsPageResult, WithdrawalInfoVO } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

export function getSupplierWithdrawalPages(
  queryParams: SupplierWithdrawalPageQuery
): AxiosPromise<SupplierWithdrawalsPageResult> {
  return request({
    url: "/mall-pms/scm-api/v1/supplier-withdrawals/page",
    method: "get",
    params: queryParams,
  });
}

export function getSupplierWithdrawal(id: number): AxiosPromise<SupplierWithdrawal> {
  return request({
    url: "/mall-pms/scm-api/v1/supplier-withdrawals/" + id,
    method: "get",
  });
}

export function addSupplierWithdrawal(data: object) {
  return request({
    url: "/mall-pms/scm-api/v1/supplier-withdrawals",
    method: "post",
    data: data,
  });
}


export function auditSupplierWithdrawals(id: string, data:object) {
  return request({
    url: "/mall-pms/scm-api/v1/supplier-withdrawals/" + id+"/audit",
    method: "put",
    data: data,
  });
}

/**
 * 获取提现所需信息（可提现余额和银行账户信息）
 * @returns 提现所需信息
 */
export function getWithdrawalInfoApi(): AxiosPromise<WithdrawalInfoVO> {
  return request({
    url: "/mall-pms/scm-api/v1/supplier-withdrawals/withdrawal-info",
    method: "get"
  });
}
