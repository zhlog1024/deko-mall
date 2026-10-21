export interface ShopPageQuery extends PageQuery {
  shopId?: number;
  keywords?: string;
  ownerType?: string;
  status?: number;
}

export interface Shop {
  id: number;
  ownerType: string;
  ownerId: number;
  shopName: string;
  logo: string;
  logoLong: string;
  description: string;
  status: number;
  sort: number;
  score: number | null;
  goodsCount: number;
  favorCount: number;
  createTime: string;
}

export interface DialogType {
  title: string; // 弹窗标题
  visible: boolean; // 弹窗显示状态
}

export type ShopPageResult = PageResult<Shop[]>;
