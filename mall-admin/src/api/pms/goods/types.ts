/**
 * 商品查询参数类型声明
 */
export interface GoodsQuery extends PageQuery {
  name?: string;
  categoryId?: number;
  spuType?: string;
  auditStatus?: string;
  keywords?: string;
  status?: number; // 商品状态 (0-草稿，1-上架，2-下架)
  spuId?: number; // 商品 ID
  skuId?: number; // 规格 ID
  supplierId?: number; // 供应商 ID
}

/**
 * 商品列表项类型声明
 */
export interface Goods {
  id: string;
  name: string;
  categoryId?: any;
  brandId?: any;
  sales: number;
  picUrl?: any;
  album?: any;
  unit?: any;
  description: string;
  detail: string;
  status?: any;
  categoryName: string;
  brandName: string;
  spuType?: string; // 商品类型
  spuTypeDesc?: string; // 商品类型描述
  skuList: Sku[];
}

/**
 * 商品规格项类型声明
 */
export interface Sku {
  id: string;
  skuSn?: any;
  name: string;
  spuId?: any;
  specIds: string;
  price: string;
  stock: number;
  lockedstock?: any;
  picUrl?: any;
}

/**
 * 商品分页项类型声明
 */
export type GoodsPageResult = PageResult<Goods[]>;

/**
 * 商品表单数据类型声明
 */
export interface GoodsDetail {
  id?: string;
  name?: string;
  categoryId?: string;
  brandId?: string;
  picUrl?: string;
  album: string[];
  description?: string;
  detail?: string;
  auditStatus?: number;
  remark?: string;
  auditUser?: string;
  submitTime?: string;
  auditTime?: string;
  status?: number;

  attrList: any[];
  specList: any[];
  skuList: any[];
}
