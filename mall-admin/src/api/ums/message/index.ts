import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { MessageQuery, MessagePageResult, MessageVO, MessageForm } from "./types";

export function getMessagePage(queryParams: MessageQuery): AxiosPromise<MessagePageResult> {
  return request({
    url: "/mall-ums/api/v1/messages",
    method: "get",
    params: queryParams,
  });
}

export function getMessageDetail(id: number): AxiosPromise<{ data: MessageVO }> {
  return request({
    url: "/mall-ums/api/v1/messages/" + id,
    method: "get",
  });
}

export function sendMessage(data: MessageForm) {
  return request({
    url: "/mall-ums/api/v1/messages",
    method: "post",
    data,
  });
}

export function deleteMessage(id: number) {
  return request({
    url: "/mall-ums/api/v1/messages/" + id,
    method: "delete",
  });
}

export function batchDeleteMessages(ids: number[]) {
  return request({
    url: "/mall-ums/api/v1/messages/batch",
    method: "delete",
    data: ids,
  });
}
