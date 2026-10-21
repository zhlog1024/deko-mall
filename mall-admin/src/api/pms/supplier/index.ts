import { Supplier, SupplierPageQuery, SuppliersPageResult } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

export function getSupplierPages(
  queryParams: SupplierPageQuery
): AxiosPromise<SuppliersPageResult> {
  return request({
    url: "/mall-pms//api/v1/suppliers/page",
    method: "get",
    params: queryParams,
  });
}

export function getSupplier(id: number): AxiosPromise<Supplier> {
  return request({
    url: "/mall-pms/api/v1/suppliers/" + id,
    method: "get",
  });
}

export function addSupplier(data: object) {
  return request({
    url: "/mall-pms/api/v1/suppliers",
    method: "post",
    data: data,
  });
}

export function updateSupplier(id: number, data: object) {
  return request({
    url: "/mall-pms/api/v1/suppliers/" + id,
    method: "put",
    data: data,
  });
}

export function auditSupplier(id: string, data:object) {
  return request({
    url: "/mall-pms/api/v1/suppliers/" + id+"/audit",
    method: "put",
    data: data,
  });
}
