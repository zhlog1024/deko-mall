/**
 * 视频 DIY 快照行工具(ShopXO model-plugins-video-list/-tabs 迁移,mall 化)
 *
 * 渲染契约(对齐 shopxo-uniapp plugins-video-list.vue/plugins-video-tabs.vue 快照读取):
 * - 指定模式 data_list 行为包裹行 {id, new_title, new_cover, data:{title, cover, url, add_time, access_count,...}}
 * - 筛选模式 data_auto_list 行为裸行(字段同上)
 * - 后端(mall-cms VideoItemDTO/VideoVO)camelCase(createTime/accessCount/videoUrl...)统一在此归一到 snake 契约字段
 */
import { get_math } from "./index";
import { isEmpty } from "lodash";

/** 视频详情页地址前缀(App 端由渲染端子代理落地,同源 detail?id= 详情页) */
export const VIDEO_DETAIL_PAGE = "/pages/plugins/video/detail/detail?id=";

/** 视频渲染裸行(契约字段,对齐源渲染器消费:title/cover/url/add_time/access_count) */
export interface VideoRowData {
  id?: number | string;
  title?: string;
  cover?: string;
  /** 点击跳转地址(视频详情页) */
  url?: string;
  /** 展示时间 YYYY-MM-DD HH:mm */
  add_time?: string;
  access_count?: number | string;
  duration?: number | string;
  is_recommended?: number | string;
  is_hot?: number | string;
  [key: string]: any;
}

/** 视频包裹行(画布/快照行结构) */
export interface VideoRow {
  id: number | string;
  data: VideoRowData;
  new_title: string;
  new_cover: uploadList[];
}

// 格式化时间 YYYY-MM-DD HH:mm(渲染契约 add_time)
export const format_video_time = (time?: string | number): string => {
  if (time === undefined || time === null || time === "") {
    return "";
  }
  const str = String(time).replace("T", " ").trim();
  const matched = str.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})/);
  return matched ? matched[1] + " " + matched[2] : "";
};

/**
 * 视频数据归一:任意形态(shopxo snake 契约字段 / mall-cms camel DTO) → 渲染契约 snake 字段
 * 已存在的 snake 字段保留,缺失时用 mall 字段兜底;不丢其他扩展字段(videoUrl/duration 等)
 */
export const video_data_normalize = (
  data: VideoRowData | null | undefined
): VideoRowData => {
  if (data === null || typeof data !== "object") {
    return {};
  }
  const dto = data as any;
  const id = data.id ?? dto.id ?? "";
  return {
    ...data,
    id,
    title: data.title ?? "",
    cover: data.cover ?? "",
    add_time: data.add_time ?? format_video_time(dto.createTime),
    access_count:
      data.access_count !== undefined && data.access_count !== null
        ? data.access_count
        : dto.accessCount ?? 0,
    url:
      data.url ??
      dto.url ??
      (id !== "" ? `${VIDEO_DETAIL_PAGE}${id}` : ""),
    duration:
      data.duration !== undefined && data.duration !== null
        ? data.duration
        : dto.duration ?? 0,
    is_recommended:
      data.is_recommended !== undefined && data.is_recommended !== null
        ? data.is_recommended
        : dto.isRecommended ?? 0,
    is_hot:
      data.is_hot !== undefined && data.is_hot !== null
        ? data.is_hot
        : dto.isHot ?? 0,
  };
};

/** 包裹行:{id, new_title, new_cover, data}("指定视频"回调/回显统一行结构) */
export const video_row_wrap = (data: VideoRowData): VideoRow => ({
  id: get_math(),
  new_title: "",
  new_cover: [],
  data: video_data_normalize(data),
});

/**
 * 快照列表统一归一为包裹行形态(画布渲染用)
 * 兼容已包裹 {new_title,new_cover,data} 与裸行/原始 mall DTO 两种形态
 */
export const video_list_normalize = (list: any[]): VideoRow[] => {
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
        data: video_data_normalize(item.data),
      };
    }
    return {
      id: !isEmpty(item?.id) ? item.id : get_math(),
      new_title: "",
      new_cover: [],
      data: video_data_normalize(item),
    };
  });
};
