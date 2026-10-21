import request from "@/utils/request";
import { AxiosPromise } from "axios";
import {
  CommentQuery,
  CommentVO,
  CommentPageResult,
  CommentReplyForm,
  CommentAuditForm,
} from "./types";

export function getCommentPage(
  queryParams: CommentQuery
): AxiosPromise<CommentPageResult> {
  return request({
    url: "/mall-pms/api/v1/comments/page",
    method: "get",
    params: queryParams,
  });
}

export function getCommentDetail(id: number): AxiosPromise<CommentVO> {
  return request({
    url: "/mall-pms/api/v1/comments/" + id,
    method: "get",
  });
}

export function replyComment(id: number, data: CommentReplyForm) {
  return request({
    url: "/mall-pms/api/v1/comments/" + id + "/reply",
    method: "put",
    data,
  });
}

export function auditComment(id: number, data: CommentAuditForm) {
  return request({
    url: "/mall-pms/api/v1/comments/" + id + "/audit",
    method: "put",
    data,
  });
}

export function deleteComment(id: number) {
  return request({
    url: "/mall-pms/api/v1/comments/" + id,
    method: "delete",
  });
}
