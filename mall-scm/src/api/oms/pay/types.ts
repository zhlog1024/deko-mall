/**
 * PayConfig配置查询参数
 */
export interface PayConfigQuery extends PageQuery {
  payChannel?: string;
  enabled?: number;
}

export interface PayProperties {
  appId?: string;
  mchId?: string;
  privateKey?: string;
  publicKey?: string;
  notifyUrl?: string;
  returnUrl?: string;
}

/**
 * PayConfig配置查询参数
 */
export interface PayConfig {
  id: number;
  payChannel: string;
  appId: string;
  mchId: string;
  privateKey: string;
  publicKey: string;
  paymentNotifyUrl: string;
  refundNotifyUrl: string;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
}

// 定义 DialogType 类型
export interface DialogType {
  title: string;
  visible: boolean;
}

/**
 * 支付配置分页
 */
export type PayConfigPageResult = PageResult<PayConfig[]>;
