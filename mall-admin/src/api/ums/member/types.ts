/**
 * 会员查询参数类型声明
 */
export interface MemberQuery extends PageQuery {
  memberId?: number;
  nickName?: string;
  mobile?: string;
  status?: number;
  tagName?: string;
}

/**
 * 会员分页列表项声明
 */
export interface Member {
  id: string;
  nickName: string;
  mobile: string;
  avatarUrl?: string;
  status: number;
  tagName?: string;
  createTime?: string;
}

export interface Address {
  id: string;
  memberId: string;
  consigneeName: string;
  consigneeMobile: string;
  province: string;
  city: string;
  area: string;
  detailAddress: string;
  zipCode?: any;
  defaulted: number;
}

/**
 * 会员分页项类型声明
 */
export type MemberPageResult = PageResult<Member[]>;

/**
 * 会员表单类型声明
 */
export interface MemberForm {
  id: number | undefined;
  title: string;
  picUrl: string;
  beginTime: string;
  endTime: string;
  status: number;
  sort: number;
  url: string;
  remark: string;
}
