/**
 * 商品问答 DIY 快照行工具(ShopXO model-ask 迁移,mall 化)
 *
 * 渲染契约(对齐 shopxo-uniapp ask.vue/ask-tabs.vue 快照读取):
 * - 指定模式 data_list 行为包裹行 {id, new_title, new_cover, data:{title, add_time_date, is_reply, access_count, url,...}}
 * - 筛选模式 data_auto_list 行为裸行(字段同上)
 * - 后端(mall-cms AskItemDTO/AskVO)camelCase(createTime/isReply/accessCount...)统一在此归一到 snake 契约字段
 */
import { get_math } from "./index";
import { isEmpty } from "lodash";

/** 问答详情页地址前缀(App 端由渲染端子代理落地) */
export const ASK_DETAIL_PAGE = "/pages/plugins/ask/detail/detail?id=";

export interface AskRowData {
  id?: number | string;
  title?: string;
  add_time_date?: string;
  is_reply?: number | string;
  access_count?: number | string;
  url?: string;
  spu_id?: number | string;
  spu_name?: string;
  spu_pic?: string;
  [key: string]: any;
}

export interface AskRow {
  id: number | string;
  data: AskRowData;
  new_title: string;
  new_cover: uploadList[];
}

// 格式化时间 YYYY-MM-DD HH:mm(渲染契约 add_time_date)
export const format_ask_time = (time?: string | number): string => {
  if (time === undefined || time === null || time === "") {
    return "";
  }
  const str = String(time).replace("T", " ").trim();
  const matched = str.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})/);
  return matched ? matched[1] + " " + matched[2] : "";
};

/**
 * 问答数据归一:任意形态(shopxo snake 契约字段 / mall-cms camel DTO) → 渲染契约 snake 字段
 * 已存在的 snake 字段保留,缺失时用 mall 字段兜底;不丢其他扩展字段(spu_name/spu_pic 等)
 */
export const ask_data_normalize = (
  data: AskRowData | null | undefined
): AskRowData => {
  if (data === null || typeof data !== "object") {
    return {};
  }
  const dto = data as any;
  const id = data.id ?? dto.id ?? "";
  return {
    ...data,
    id,
    title: data.title ?? "",
    add_time_date: data.add_time_date ?? format_ask_time(dto.createTime),
    is_reply:
      data.is_reply !== undefined && data.is_reply !== null
        ? data.is_reply
        : dto.isReply ?? 0,
    access_count:
      data.access_count !== undefined && data.access_count !== null
        ? data.access_count
        : dto.accessCount ?? 0,
    url:
      data.url ??
      dto.url ??
      (id !== "" ? `${ASK_DETAIL_PAGE}${id}` : ""),
    spu_id: data.spu_id ?? dto.spuId,
    spu_name: data.spu_name ?? dto.spuName,
    spu_pic: data.spu_pic ?? dto.spuPic,
  };
};

/** 包裹行:{id, new_title, new_cover, data}("指定问答"回调/回显统一行结构) */
export const ask_row_wrap = (data: AskRowData): AskRow => ({
  id: get_math(),
  new_title: "",
  new_cover: [],
  data: ask_data_normalize(data),
});

/**
 * 快照列表统一归一为包裹行形态(画布渲染用)
 * 兼容已包裹 {new_title,new_cover,data} 与裸行/原始 mall DTO 两种形态
 */
export const ask_list_normalize = (list: any[]): AskRow[] => {
  if (!Array.isArray(list)) {
    return [];
  }
  return list.map((item) => {
    if (
      item &&
      typeof item === "object" &&
      Object.prototype.hasOwnProperty.call(item, "data")
    ) {
      return {
        id: !isEmpty(item.id) ? item.id : get_math(),
        new_title: item.new_title || "",
        new_cover: item.new_cover || [],
        data: ask_data_normalize(item.data),
      };
    }
    return {
      id: !isEmpty(item?.id) ? item.id : get_math(),
      new_title: "",
      new_cover: [],
      data: ask_data_normalize(item),
    };
  });
};
