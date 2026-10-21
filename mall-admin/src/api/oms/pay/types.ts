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
  notifyUrl?: string;
  returnUrl?: string;
}

/**
 * 支付宝扩展配置
 */
export interface AlipayExtConfig {
  appPrivateKey: string;
  alipayPublicKey: string;
}

/**
 * 微信支付扩展配置
 */
export interface WechatPayExtConfig {
  apiV3Key: string;
  merchantPrivateKey: string;
  merchantSerialNo: string;
  platformCert: string;
  platformCertSerialNo?: string;
}

/** 扩展配置：微信 / 支付宝直连 */
export type PayExtConfig = AlipayExtConfig | WechatPayExtConfig | Record<string, any>;

export interface PayConfig {
  id: number;
  payChannel: string;
  appId: string;
  mchId: string;
  paymentNotifyUrl: string;
  refundNotifyUrl: string;
  enabled: boolean;
  /** 适用设备类型: H5,MINI,APP */
  deviceType?: string;
  /** admin识别名称 */
  channelName?: string;
  /** C端收银台展示名称 */
  cashierName?: string;
  /** 渠道图标URL */
  channelIcon?: string;
  /** 允许的用户ID集合，逗号分隔，为空所有用户可见 */
  allowedUserIds?: string;
  extConfig?: string;
  parsedExtConfig?: PayExtConfig;
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
