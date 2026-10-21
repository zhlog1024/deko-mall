import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { MemberQuery, MemberPageResult } from "./types";

/**
 * 获取会员分页列表
 */
export function getMemberPage(
  queryParams: MemberQuery
): AxiosPromise<MemberPageResult> {
  return request({
    url: "/mall-ums/api/v1/members",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取会员详情
 */
export function getMemberDetail(id: number) {
  return request({
    url: "/mall-ums/api/v1/members/" + id,
    method: "get",
  });
}

/**
 * 添加会员
 */
export function addMember(data: object) {
  return request({
    url: "/mall-ums/api/v1/members",
    method: "post",
    data: data,
  });
}

/**
 * 添加会员
 */
export function updateMember(id: number, data: object) {
  return request({
    url: "/mall-ums/api/v1/members/" + id,
    method: "put",
    data: data,
  });
}

