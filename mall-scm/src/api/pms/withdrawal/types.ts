export interface SupplierWithdrawalPageQuery extends PageQuery {
  supplierId?: number;
  keywords?: string;
  status?: number;
}

export interface SupplierWithdrawal {
  id: number;
  loginName: string;
  supplierCode: string;
  supplierName: string;
  contactPhone: string;
  settlementBank: string;
  settlementAccount: string;
  businessLicense: string;
  status: number;
  statusName: string;
  auditStatus: number;
  auditStatusName: string;
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface SupplierWithdrawalForm {
  id: number;
  withdrawalNo: string;
  supplierId: string;
  supplierName: string;
  amount: string;
  fee: string;
  actualAmount: string;
  status: string;
  statusLabel: string;
  bankName: string;
  bankAccount: string;
  accountName: string;
  paymentTime: string;
  transactionNo: string;
  remark: string;
  createTime: string;
  updateTime: string;
}

// 定义 DialogType 类型
export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}

export type SupplierWithdrawalsPageResult = PageResult<SupplierWithdrawal[]>;

// 提现信息VO，与后端 WithdrawalInfoVO 对应
export interface WithdrawalInfoVO {
  withdrawableBalance: number; // 可提现余额(分)
  settlementInfo: {
    bankName: string; // 开户银行
    bankAccount: string; // 银行账号
    accountName: string; // 开户名
  };
}
