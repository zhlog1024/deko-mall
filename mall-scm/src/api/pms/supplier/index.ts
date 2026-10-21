import { Supplier, SupplierPageQuery, SuppliersPageResult } from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";


export function getSupplier(): AxiosPromise<Supplier> {
  return request({
    url: "/mall-pms/scm-api/v1/suppliers",
    method: "get",
  });
}

export function addSupplier(data: object) {
  return request({
    url: "/mall-pms/scm-api/v1/suppliers",
    method: "post",
    data: data,
  });
}

export function updateSupplier(id: number, data: object) {
  return request({
    url: "/mall-pms/scm-api/v1/suppliers/" + id,
    method: "put",
    data: data,
  });
}
