/**
 * 直播 DIY 数据行工具
 *
 * 渲染契约（对齐 shopxo-uniapp plugins-live-list.vue 的读取方式）：
 *   裸行 { id, title, cover, url, seller_name, status, status_name, online_count }
 *   包裹行 { id, new_title, new_cover, data:{...裸行} }
 *
 * 后端（mall-sms LiveRoomItemDTO，经 mall-cms 转发）按项目约定出网为 camelCase
 * （sellerName / statusName / onlineCount），统一在此归一到 snake 契约字段。
 */

/** 直播渲染裸行（契约字段） */
export interface LiveRowData {
  id?: number | string;
  title?: string;
  cover?: string;
  /** 点击跳转地址（直播详情页） */
  url?: string;
  /** 主播昵称 */
  seller_name?: string;
  /** 直播状态(0-未开播 1-直播中 2-离开 3-封禁) */
  status?: number | string;
  status_name?: string;
  /** 在线人数 */
  online_count?: number | string;
  [key: string]: any;
}

/** 直播包裹行（画布/快照行结构，与 app normalize_list_item 产物一致） */
export interface LiveRow {
  id: number | string;
  data: LiveRowData;
  new_title: string;
  new_cover: any[];
}

/**
 * 裸行归一：camelCase → 直播渲染契约字段（snake_case）
 * 已是 snake 的字段原样保留，便于手动模式的历史数据复用同一函数。
 */
export const live_data_normalize = (row: any): LiveRowData => ({
  id: row?.id,
  title: row?.title ?? "",
  cover: row?.cover ?? "",
  url: row?.url ?? "",
  seller_name: row?.seller_name ?? row?.sellerName ?? "",
  status: row?.status,
  status_name: row?.status_name ?? row?.statusName ?? "",
  online_count: row?.online_count ?? row?.onlineCount ?? 0,
});

/** 批量归一为包裹行（装修器画布直接消费） */
export const live_list_normalize = (rows: any): LiveRow[] =>
  (Array.isArray(rows) ? rows : []).map((row) => ({
    id: row?.id ?? 0,
    new_title: "",
    new_cover: [],
    data: live_data_normalize(row),
  }));
