export interface SupplierPageQuery extends PageQuery {
  keywords?: string;
  status?: number;
  auditStatus?: number;
}

export interface Supplier {
  id: number;
  loginName: string;
  supplierCode: string;
  supplierName: string;
  businessLicense: string;
  status: number;
  statusName: string;
  auditStatus: number;
  auditStatusName: string;
  remark: string;
  settlement: SupplierSettlement;
  createTime: string;
  updateTime: string;
}

export interface SupplierSettlement {
  id: number;
  supplierId: number;
  bankName: string;
  bankAccount: string;
  accountName: string;
  createTime: string;
  updateTime: string;
}

export interface SupplierForm {
  id: number;
  loginName: string;
  supplierName: string;
  supplierCode: string;
  businessLicense: string;
  settlementForm: SupplierSettlementForm; // 表单字段
}

export interface SupplierSettlementForm {
  id: number;
  supplierId: number;
  bankName: string;
  bankAccount: string;
  accountName: string;
}

// 定义 DialogType 类型
export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}

export type SuppliersPageResult = PageResult<Supplier[]>;
