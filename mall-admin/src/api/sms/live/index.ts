import {
  LiveRoom,
  LiveRoomForm,
  LiveRoomGoodsForm,
  LiveRoomPageResult,
  LiveRoomQuery,
  LiveSettings,
  LiveGoods,
} from "./types";
import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 直播间分页列表
 */
export function getLiveRoomPage(queryParams: LiveRoomQuery): AxiosPromise<LiveRoomPageResult> {
  return request({
    url: "/mall-sms/api/v1/live/rooms/page",
    method: "get",
    params: queryParams,
  });
}

/**
 * 直播间详情（管理端）
 */
export function getLiveRoomDetail(id: number): AxiosPromise<LiveRoom> {
  return request({
    url: "/mall-sms/api/v1/live/rooms/" + id,
    method: "get",
  });
}

/**
 * 新增直播间
 */
export function addLiveRoom(data: LiveRoomForm) {
  return request({
    url: "/mall-sms/api/v1/live/rooms",
    method: "post",
    data,
  });
}

/**
 * 编辑直播间
 */
export function updateLiveRoom(id: number, data: LiveRoomForm) {
  return request({
    url: "/mall-sms/api/v1/live/rooms/" + id,
    method: "put",
    data,
  });
}

/**
 * 删除直播间
 */
export function deleteLiveRooms(ids: string) {
  return request({
    url: "/mall-sms/api/v1/live/rooms/" + ids,
    method: "delete",
  });
}

/**
 * 变更直播状态（1开播/2离开/0下播/3封禁）
 */
export function changeLiveRoomStatus(id: number, status: number) {
  return request({
    url: `/mall-sms/api/v1/live/rooms/${id}/status`,
    method: "put",
    params: { status },
  });
}

/**
 * 重新生成推拉流地址
 */
export function refreshLiveStreamUrl(id: number): AxiosPromise<LiveRoom> {
  return request({
    url: `/mall-sms/api/v1/live/rooms/${id}/refresh-stream`,
    method: "post",
  });
}

/**
 * 直播间商品橱窗
 */
export function getLiveRoomGoods(roomId: number): AxiosPromise<LiveGoods[]> {
  return request({
    url: `/mall-sms/api/v1/live/rooms/${roomId}/goods`,
    method: "get",
  });
}

/**
 * 绑定直播商品
 */
export function bindLiveGoods(data: LiveRoomGoodsForm) {
  return request({
    url: "/mall-sms/api/v1/live/rooms/goods",
    method: "post",
    data,
  });
}

/**
 * 解绑直播商品
 */
export function unbindLiveGoods(goodsId: number) {
  return request({
    url: "/mall-sms/api/v1/live/rooms/goods/" + goodsId,
    method: "delete",
  });
}

/**
 * 设置讲解商品
 */
export function setExplainGoods(goodsId: number) {
  return request({
    url: `/mall-sms/api/v1/live/rooms/goods/${goodsId}/explain`,
    method: "put",
  });
}

/**
 * 全局直播设置
 */
export function getLiveSettings(): AxiosPromise<LiveSettings> {
  return request({
    url: "/mall-sms/api/v1/live/settings",
    method: "get",
  });
}
