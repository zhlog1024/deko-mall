import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { BatchDeliverForm, BatchDeliverResult, OrderPageResult, OrderQuery, OrderEnumVO } from "./types";

/**
 * 获取订单分页列表
 */
export function getOrderPage(
  queryParams: OrderQuery
): AxiosPromise<OrderPageResult> {
  return request({
    url: "/mall-oms/scm-api/v1/orders",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取订单分页列表
 */
export function exportOrders(queryParams: OrderQuery): AxiosPromise<any> {
  // 改为any或Blob类型
  return request({
    url: "/mall-oms/scm-api/v1/orders/export",
    method: "get",
    params: queryParams,
    responseType: "arraybuffer",
  });
}

/**
 * 批量发货
 *
 * @param data 批量发货表单
 */
export function batchDelivery(data: BatchDeliverForm): AxiosPromise<BatchDeliverResult> {
  return request({
    url: "/mall-oms/scm-api/v1/orders/batch-delivery",
    method: "post",
    data,
  });
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderId: number) {
  return request({
    url: "/mall-oms/scm-api/v1/orders/" + orderId,
    method: "get",
  });
}

//获取订单物流接口
export function getOrderLogistics(): AxiosPromise<any> {
  return request({
    url: "/mall-oms/api/v1/logistics/delivery/carriers",
    method: "get",
  });
}

// 发货
export const deliveryOrder = (data: any) => {
  return request({
    url: "/mall-oms/api/v1/orders/delivery",
    method: "post",
    data,
  });
};

// 查看发货信息
export const getDeliveryInfo = (orderId: number) => {
  return request({
    url: "/mall-oms/api/v1/orders/delivery/" + orderId,
    method: "get",
  });
};

// 查询物流轨迹
export const getLogistics = (params: Object) => {
  return request({
    url: "/mall-oms/api/v1/logistics/query",
    method: "get",
    params,
  });
};

// 用户端查询物流轨迹
export const getLogisticsByOrderId = (params: Object) => {
  return request({
    url: "/mall-oms/api/v1/logistics/detail",
    method: "get",
    params,
  });
};

/**
 * 获取线下发货详情
 * @param orderId 订单ID
 */
export const getOfflineDeliveryDetail = (orderId: number) => {
  return request({
    url: "/mall-oms/scm-api/v1/orders/" + orderId + "/offline-delivery",
    method: "get",
  });
};

/**
 * 获取订单枚举
 */
export function getOrderEnums(): AxiosPromise<OrderEnumVO> {
  return request({
    url: "/mall-oms/scm-api/v1/orders/enums",
    method: "get",
  });
}
