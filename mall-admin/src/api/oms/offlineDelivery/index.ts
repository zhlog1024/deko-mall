/* 线下发货相关接口（订单详情页使用） */
import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 提交线下发货
 */
export function submitOfflineDelivery(data: any): AxiosPromise<any> {
  return request({
    url: "/mall-oms/api/v1/orders/offline-delivery",
    method: "post",
    data,
  });
}

/**
 * 获取线下发货详情
 * @param orderId 订单ID
 */
export function getOfflineDeliveryDetail(orderId: number): AxiosPromise<any> {
  return request({
    url: `/mall-oms/api/v1/orders/${orderId}/offline-delivery`,
    method: "get",
  });
}
