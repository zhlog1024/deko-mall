/**
 * 内容管理-商品问答 API（mall-cms）
 * 说明:DIY 装修器"指定问答"选择器走此分页接口(照 article DIY 先例 link-articles 用管理端分页);
 * 完整问答管理页(增删改/回复)后续在 views/cms/ask 落地时在此扩展。
 */
import request from "@/utils/request";

const BASE = "/mall-cms/api/v1/ask";

/** 问答分页查询(管理端:keyword/spuId/isReply/isEnable/sortField 过滤) */
export interface AskPageQuery {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  spuId?: number;
  isReply?: number;
  isEnable?: number;
  sortField?: string;
}

/** 问答管理列表 VO(列表不带 answer/content 大文本) */
export interface AskPageVO {
  id?: number;
  spuId?: number;
  spuName?: string;
  spuPic?: string;
  title?: string;
  isReply?: number;
  isEnable?: number;
  accessCount?: number;
  askUser?: string;
  askUserId?: number;
  replyUser?: string;
  replyTime?: string;
  sort?: number;
  createTime?: string;
  updateTime?: string;
}

/** 问答分页列表 */
export function getAskPage(query: AskPageQuery) {
  return request<any, PageResult<AskPageVO>>({
    url: `${BASE}/page`,
    method: "get",
    params: query,
  });
}

/** 问答详情(编辑回显,含问题详情 content 与回答 answer) */
export interface AskDetailVO extends AskPageVO {
  content?: string;
  answer?: string;
}

/** 问答新增/编辑表单(运营代录;answer/isReply 由回复接口管理,编辑不触碰) */
export interface AskForm {
  id?: number;
  spuId?: number;
  spuName?: string;
  spuPic?: string;
  title: string;
  content?: string;
  /** 提问人昵称快照(运营代录可空) */
  askUser?: string;
  sort: number;
  isEnable: number;
}

/** 运营回复表单(回复即确认:后端自动置 isReply=1/replyUser/replyTime 并启用展示) */
export interface AskReplyForm {
  answer: string;
  /** 空则后端取当前登录人 */
  replyUser?: string;
}

/** 启用/停用表单(用户提问 0→1 即"确认展示") */
export interface AskEnableForm {
  isEnable: number;
}

/** 问答详情(编辑回显) */
export function getAskDetail(id: number) {
  return request<any, AskDetailVO>({
    url: `${BASE}/${id}`,
    method: "get",
  });
}

/** 新增问答(运营代录问题) */
export function addAsk(data: AskForm) {
  return request({
    url: BASE,
    method: "post",
    data,
  });
}

/** 编辑问答(标题/内容/商品/排序/启用;不动 answer/isReply) */
export function updateAsk(id: number, data: AskForm) {
  return request({
    url: `${BASE}/${id}`,
    method: "put",
    data,
  });
}

/** 回复问答(回复即确认展示) */
export function replyAsk(id: number, data: AskReplyForm) {
  return request({
    url: `${BASE}/${id}/reply`,
    method: "put",
    data,
  });
}

/** 确认/停用问答(启用展示开关) */
export function enableAsk(id: number, data: AskEnableForm) {
  return request({
    url: `${BASE}/${id}/enable`,
    method: "put",
    data,
  });
}

/** 删除问答(逻辑删除,支持批量) */
export function deleteAsks(ids: number[]) {
  return request({
    url: `${BASE}/${ids.join(",")}`,
    method: "delete",
  });
}
