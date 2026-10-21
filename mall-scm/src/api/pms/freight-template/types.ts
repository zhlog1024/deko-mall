/**
 * 分页查询基础类型
 */
export interface PageQuery {
  pageNum: number;
  pageSize: number;
}

/**
 * 分页结果基础类型
 */
export interface PageResult<T> {
  list: T;
  total: number;
}

/**
 * 运费模板分页查询参数
 */
export interface FreightTemplatePageQuery extends PageQuery {
  templateName?: string;
  chargeType?: string; // BY_WEIGHT: 按重量计费, BY_PIECE: 按件计费 
  isFreeShipping?: number; // 0-否, 1-是
  isDefault?: number; // 0-否, 1-是
}

/**
 * 运费模板信息
 */
export interface FreightTemplate {
  id: number;
  templateName: string;
  countryId?: number; // 国家区域ID
  chargeType: string; // BY_WEIGHT: 按重量计费, BY_PIECE: 按件计费
  isFreeShipping: number; // 0-否, 1-是
  isDefault: number; // 0-否, 1-是
  remark?: string;
  rules?: FreightRuleVO[];
  createTime?: string;
  updateTime?: string;
}

/**
 * 运费规则VO
 */
export interface FreightRuleVO {
  id?: number;
  regionCodes: string; // 区域编码集合,多个以逗号分隔
  regionNames: string; // 区域名称集合,多个以逗号分隔
  firstItem: number; // 首件数量(个)或首重(kg*100)
  firstFee: number; // 首费(分)
  continueItem: number; // 续件数量(个)或续重(kg*100)
  continueFee: number; // 续费(分)
}

/**
 * 运费模板创建/更新参数
 */
export interface FreightTemplateForm {
  id?: number;
  supplierId?: number;
  templateName: string;
  chargeType: string; // BY_WEIGHT: 按重量计费, BY_PIECE: 按件计费
  isFreeShipping: number; // 0-否, 1-是
  isDefault: number; // 0-否, 1-是
  remark?: string;
  rules: FreightRuleVO[];
}

/**
 * 对话框类型
 */
export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}

/**
 * 分页结果类型
 */
export type FreightTemplatePageResult = PageResult<FreightTemplate[]>;
