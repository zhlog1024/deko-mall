/**
 * 订单查询参数
 */
export interface OrderQuery extends PageQuery {
  keywords: string | undefined;
  status: number[] | undefined;
  memberId: string | undefined;
  spuId?: string;  // 商品ID
  phone?: string;  // 用户手机号
}

/**
 * 订单分页列表项
 */
export interface Order {
  id: string;
  orderNo: string;
  totalAmount: string;
  payAmount: string;
  payType: number;
  status: number;
  totalQuantity: number;
  createTime: string;
  memberId: string;
  sourceType: number;
  orderItems: OrderItem[];
  // 新增字段
  paymentTime?: string;
  deliveryTime?: string;
  receiveTime?: string;
  receiverName?: string;
  receiverPhone?: string;
  receiverProvince?: string;
  receiverCity?: string;
  receiverDistrict?: string;
  receiverAddress?: string;
  receiverCompleteAddress?: string;
  buyerPhone?: string;
  supplierId?: string;
  carrierName?: string;
  trackingNo?: string;
  needDelivery?: boolean;
  // 供货总价
  totalSupplyPrice?: number;
}
export interface OrderItem {
  id: string;
  orderId: string;
  spuId?: string;  // 商品 ID
  skuId: string;
  skuName: string;
  picUrl: string;
  price: string;
  quantity: number;
  totalAmount: number;
  status?: number;  // 履约状态: 0取消/1待发货/2已发货/3已收货/4售后中/5已退款
  supplyPrice?: number;  // 供货价
  supplierId?: string;  // 供应商ID
}

/**
 * 订单分页
 */
export type OrderPageResult = PageResult<Order[]>;

/**
 * 枚举项
 */
export interface EnumItem {
  value: any;
  label: string;
}

/**
 * 订单枚举VO
 */
export interface OrderEnumVO {
  orderTypes: EnumItem[];
  orderStatuses: EnumItem[];
}

/**
 * 订单表单
 */
export interface OrderDetail {
  id: number | undefined;
  title: string;
  picUrl: string;
  beginTime: string;
  endTime: string;
  status: number;
  sort: number;
  url: string;
  remark: string;
}

// 定义 DialogType 类型
export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}


/**
 * 订单状态枚举
 */
export enum OrderStatusEnum {
  /**
   * 待付款
   */
  WAIT_PAY = 1,
  /**
   * 待发货
   */
  WAIT_SHIPPING = 2,
  /**
   * 已发货
   */
  SHIPPED = 3,

  /**
   * 已完成
   */
  FINISHED = 4,
  /**
   * 已关闭
   */
  CLOSED = 5,
  /**
   * 已取消
   */
  CANCELED = 6,
}

/**
 * 批量发货明细项
 */
export interface BatchDeliverItem {
  orderNo: string;
  deliveryCode: string;
  deliverySn: string;
  remark?: string;
}

/**
 * 批量发货表单
 */
export interface BatchDeliverForm {
  items: BatchDeliverItem[];
}

/**
 * 批量发货失败项
 */
export interface BatchDeliverFailItem {
  orderNo: string;
  reason: string;
}

/**
 * 批量发货结果
 */
export interface BatchDeliverResult {
  totalCount: number;
  successCount: number;
  failCount: number;
  successOrderNos: string[];
  failItems: BatchDeliverFailItem[];
}
