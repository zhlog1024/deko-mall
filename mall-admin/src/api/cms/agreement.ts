/**
 * 内容管理-协议 API
 */
import request from "@/utils/request";
import type {
  AgreementPageVO,
  AgreementQuery,
  AgreementForm,
} from "./agreement/types";

const BASE = "/mall-cms/api/v1/agreement";

/** 协议分页列表 */
export function getAgreementPage(query: AgreementQuery) {
  return request<any, PageResult<AgreementPageVO>>({
    url: `${BASE}/page`,
    method: "get",
    params: query,
  });
}

/** 协议详情 */
export function getAgreementDetail(id: number) {
  return request<any, AgreementPageVO>({
    url: `${BASE}/${id}`,
    method: "get",
  });
}

/** 新增协议 */
export function addAgreement(data: AgreementForm) {
  return request({
    url: BASE,
    method: "post",
    data,
  });
}

/** 修改协议 */
export function updateAgreement(id: number, data: AgreementForm) {
  return request({
    url: `${BASE}/${id}`,
    method: "put",
    data,
  });
}

/** 删除协议 */
export function deleteAgreement(id: number) {
  return request({
    url: `${BASE}/${id}`,
    method: "delete",
  });
}
