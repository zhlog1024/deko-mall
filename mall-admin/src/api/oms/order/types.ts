/**
 * 订单查询参数
 */
export interface OrderQuery extends PageQuery {
  orderNo?: string;
  status?: number[];
  orderType?: string;  // 订单类型筛选
  keywords?: string;
  memberId?: string;
  mchId?: string;
  supplierId?: string;  // 供应商ID
  spuId?: string;  // 商品ID
  phone?: string;  // 用户手机号
  payChannel?: string;
  paymentChannelNo?: string;
  channelTradeNo?: string;
  beginDate?: string;
  endDate?: string;
  orderScene?: string; // 订单场景
}

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
 * 订单分页列表项
 */
export interface Order {
  id: string;
  orderNo: string;
  paymentNo?: string;  // 业务支付单号
  orderType?: string;  // 订单类型
  orderTypeLabel?: string;  // 订单类型标签
  totalAmount: string;  // 订单总金额 (元)
  freightAmount?: string;  // 运费金额 (元)
  payAmount: string;  // 订单总金额 (元)
  paymentAmount?: string;  // 支付金额 (元)
  payType: number;
  payChannelLabel?: string;  // 支付方式标签
  paymentChannelNo?: string;  // 支付申请号
  channelTradeNo?: string;  // 渠道交易号
  status: number;
  statusLabel?: string;  // 订单状态标签
  totalQuantity: number;
  createTime: string;
  paymentTime?: string;  // 订单支付时间
  deliveryTime?: string;  // 订单发货时间
  receiveTime?: string;  // 订单收货时间
  cancelTime?: string;  // 订单取消时间
  memberId: string;
  sourceType: number;
  sourceLabel?: string;  // 订单来源标签
  remark?: string;  // 订单备注
  orderItems: OrderItem[];
  // 收货地址字段
  receiverName?: string;
  receiverPhone?: string;
  receiverProvince?: string;
  receiverCity?: string;
  receiverDistrict?: string;
  receiverAddress?: string;
  receiverCompleteAddress?: string;
  receiverZipCode?: string;  // 收货邮编
  buyerPhone?: string;
  supplierId?: string;
  carrierName?: string;
  trackingNo?: string;
  // 售后相关
  canApplyAfterSale?: boolean;  // 是否可以申请售后
  canQuickRefund?: boolean;  // 是否可以快速退款
  // 抵扣相关字段
  usePointsAmount?: number;  // 使用的积分金额 (元)
  pointsDeductionAmount?: number;  // 积分抵扣金额 (元)
  paymentDiscountAmount?: number;  // 支付抵扣金额
  // 供货总价
  totalSupplyPrice?: number;  // 供货总价 (元)
  orderScene?: string; // 订单场景
  storeName?: string; // 履约/自提门店名称
}
export interface OrderItem {
  id: string;
  orderId: string;
  spuId?: string;
  skuId: string;
  skuName: string;
  skuSn?: string; // 商品编码（入仓确认展示用）
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
 * 批量发货项
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
