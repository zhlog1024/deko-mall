export interface SupplierPageQuery extends PageQuery {
  keywords?: string;
  status?: number;
  auditStatus?: number;
  supplierId?: number;
  supplierName?: string;
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
  shopName: string;
  shopLogo: string;
  shopDescription: string;
  returnContact?: string;
  returnPhone?: string;
  returnAddress?: string;
  createTime: string;
  updateTime: string;
}


export interface SupplierForm {
  id: number;
  loginName: string;
  supplierName: string;
  supplierCode: string;
  businessLicense: string;
  returnContact?: string;
  returnPhone?: string;
  returnAddress?: string;
  shopName?: string;
  shopLogo?: string;
  shopDescription?: string;
}


// 定义 DialogType 类型
export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}

export type SuppliersPageResult = PageResult<Supplier[]>;
