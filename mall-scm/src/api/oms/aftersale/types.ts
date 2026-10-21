export interface AfterSaleQuery {
  pageNum: number;
  pageSize: number;
  status?: number;
  afterSaleType?: number;
  memberId?: string;
  orderNo?: string;
  afterSaleNo?: string;
  beginDate?: string;
  endDate?: string;
}

export interface AfterSale {
  id: number;
  afterSaleNo: string;
  orderNo: string;
  memberId: number;
  status: number;
  statusLabel: string;
  totalAmount: number;
  refundAmount: number;
  reason: string;
  description: string;
  proofPics: string;
  trackingNo: string;
  carrierName: string;
  carrierCode: string;
  createTime: string;
  orderItems: OrderItem[];
}

export interface OrderItem {
  id: number;
  skuId: number;
  skuName: string;
  picUrl: string;
  price: number;
  retailPrice: number;
  supplyPrice: number;
  quantity: number;
}

export interface AfterSaleDetail {
  id: number;
  afterSaleNo: string;
  orderNo: string;
  memberId: number;
  status: number;
  statusLabel: string;
  totalAmount: number;
  refundAmount: number;
  reason: string;
  description: string;
  proofPics: string;
  trackingNo: string;
  carrierName: string;
  carrierCode: string;
  createTime: string;
  handleTime: string;
  auditRemark: string;
  refundTime: string;
  orderItems: OrderItem[];
}

export interface DialogType {
  title: string;
  visible: boolean;
}
