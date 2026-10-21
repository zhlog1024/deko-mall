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
  score: number;
  createTime: string;
}

export interface ShopUpdateForm {
  shopName: string;
  logo: string;
  logoLong: string;
  description: string;
}
