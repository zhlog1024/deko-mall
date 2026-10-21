import { get_type } from "../utils/common";
import request from "@/utils/request";
import { isEmpty } from "lodash";

/**
 * 文章自动数据入参（沿用 shopxo 组件契约字段）
 */
interface ArticleAutoListParams {
  article_keywords?: string;
  article_category_ids?: string;
  article_order_by_type?: number | string;
  article_order_by_rule?: number | string;
  article_number?: number | string;
  article_is_cover?: number | string;
}

/**
 * 问答自动数据入参（沿用 shopxo ask 组件契约字段）
 */
interface AskAutoListParams {
  ask_keywords?: string;
  ask_is_reply?: string | number;
  ask_number?: number | string;
  ask_order_by_type?: number | string;
  ask_order_by_rule?: number | string;
}

/**
 * 直播自动数据入参（直播组件契约字段）
 */
interface LiveAutoListParams {
  keywords?: string;
  /** 状态筛选，多状态逗号分隔（如 "0,1"），空=不筛选 */
  status?: string;
  /** 是否仅推荐：null/不传=不筛选 */
  is_recommended?: number | null;
  number?: number | string;
}

/**
 * 视频自动数据入参（沿用 shopxo plugins-video 组件契约字段）
 */
interface VideoAutoListParams {
  video_keywords?: string;
  video_is_recommended?: string | number;
  video_is_hot?: string | number;
  video_number?: number | string;
  video_order_by_type?: number | string;
  video_order_by_rule?: number | string;
}

class CommonAPI {
  /**  链接初始化接口 */
  static getInit() {
    // 统一走 mall-cms 的装修器初始化接口
    return request({
      url: `/mall-cms/api/v1/diy-designer/init`,
      method: "get",
    });
  }
  /**  动态接口（默认 POST） */
  static getDynamicApi(url: string, data: any, is_header: boolean = false) {
    if (!isEmpty(url)) {
      return request({
        url: url,
        method: "post",
        ...(is_header ? { data: data } : { data }),
        ...(is_header
          ? {
              headers: {
                "Content-Type": "multipart/form-data",
              },
            }
          : {}),
      });
    }
    return Promise.reject("接口不存在");
  }
  /**  获取装修页面详情（GET 方法，id 作为路径参数） */
  static getDiyDetail(id: string | number) {
    return request({
      url: `/mall-cms/api/v1/diy-pages/${id}`,
      method: "get",
    });
  }

  /**
   * 获取文章分类树（启用状态，供装修器文章组件分类选择器使用）
   */
  static getArticleCategory() {
    return request({
      url: `/mall-cms/api/v1/diy-designer/article/category`,
      method: "get",
    });
  }

  /**
   * 文章自动数据（article-list/article-tabs "筛选文章" 模式）
   * 入参沿用 shopxo 组件契约字段（article_category_ids 为逗号串等），
   * 在此统一转换为 mall-cms ArticleAutoQuery 契约后请求
   */
  static getArticleAutoList(data: ArticleAutoListParams) {
    // 分类:兼容空(未选)、数组(级联多选值)、逗号串三种形态;
    // 过滤 NaN 与 0(Number("")===0),避免未选分类时传 [0] 导致后端展开为空集查不到数据
    const rawCategoryIds = data.article_category_ids;
    const categoryIds: number[] = (Array.isArray(rawCategoryIds)
      ? rawCategoryIds
      : String(rawCategoryIds || "")
          .split(",")
          .filter((s) => s.trim() !== "")
    )
      .map((id: any) => Number(id))
      .filter((id: number) => !Number.isNaN(id) && id > 0);
    const isCover = String(data.article_is_cover || "") === "1";
    // 排序规则：兼容历史数值(0=降序/1=升序) 与 asc/desc 字符串
    let orderByRule = "desc";
    if (String(data.article_order_by_rule || "") === "1") {
      orderByRule = "asc";
    } else if (
      ["asc", "desc"].includes(String(data.article_order_by_rule || ""))
    ) {
      orderByRule = String(data.article_order_by_rule);
    }
    return request({
      url: `/mall-cms/api/v1/diy-designer/article/auto-list`,
      method: "post",
      data: {
        categoryIds,
        onlyCover: isCover ? 1 : 0,
        keyword: data.article_keywords || "",
        orderByType: Number(data.article_order_by_type || 0),
        orderByRule,
        number: Number(data.article_number || 0),
      },
    });
  }

  /** 文章指定数据（article-list/article-tabs "指定文章" 已保存 id 快照回显） */
  static getArticleList(data: { ids: number[] }) {
    return request({
      url: `/mall-cms/api/v1/diy-designer/article/list`,
      method: "post",
      data: { ids: data.ids },
    });
  }

  /**
   * 问答自动数据（ask/ask-tabs "筛选问答" 模式）
   * 入参沿用 shopxo ask 组件契约字段,在此统一转换为 mall-cms ask auto-list 查询参数(GET)
   */
  static getAskAutoList(data: AskAutoListParams) {
    // 回复状态:''(全部)/0未回/1已回 → 空转 null
    const replyRaw = String(data.ask_is_reply ?? "");
    const isReply = replyRaw === "" ? null : Number(replyRaw);
    // 排序规则:兼容历史数值(0=降序/1=升序) 与 asc/desc 字符串
    let orderByRule = "desc";
    if (String(data.ask_order_by_rule || "") === "1") {
      orderByRule = "asc";
    } else if (
      ["asc", "desc"].includes(String(data.ask_order_by_rule || ""))
    ) {
      orderByRule = String(data.ask_order_by_rule);
    }
    return request({
      url: `/mall-cms/api/v1/diy-designer/ask/auto-list`,
      method: "get",
      params: {
        keyword: data.ask_keywords || "",
        isReply,
        number: Number(data.ask_number || 0),
        orderByType: Number(data.ask_order_by_type || 0),
        orderByRule,
      },
    });
  }

  /** 问答指定数据（ask/ask-tabs "指定问答" 已保存 id 快照回显,mall-cms by-ids） */
  static getAskList(data: { ids: number[] }) {
    return request({
      url: `/mall-cms/api/v1/diy-designer/ask/list`,
      method: "get",
      params: { ids: data.ids },
    });
  }

  /**
   * 视频自动数据（plugins-video/plugins-video-tabs "筛选视频" 模式）
   * 入参沿用 shopxo plugins-video 组件契约字段,在此统一转换为 mall-cms plugins-video auto-list 查询参数(GET)
   */
  static getVideoAutoList(data: VideoAutoListParams) {
    // 推荐/热门:''(全部)/1(是)/0(否) → 空转 null(后端空=不筛选,避免把 0 当"非推荐"误筛)
    const recommendedRaw = String(data.video_is_recommended ?? "");
    const isRecommended =
      recommendedRaw === "" ? null : Number(recommendedRaw);
    const hotRaw = String(data.video_is_hot ?? "");
    const isHot = hotRaw === "" ? null : Number(hotRaw);
    // 排序规则:兼容历史数值(0=降序/1=升序) 与 asc/desc 字符串
    let orderByRule = "desc";
    if (String(data.video_order_by_rule ?? "") === "1") {
      orderByRule = "asc";
    } else if (
      ["asc", "desc"].includes(String(data.video_order_by_rule ?? ""))
    ) {
      orderByRule = String(data.video_order_by_rule);
    }
    return request({
      url: `/mall-cms/api/v1/diy-designer/plugins-video/auto-list`,
      method: "get",
      params: {
        keyword: data.video_keywords || "",
        isRecommended,
        isHot,
        number: Number(data.video_number || 0),
        orderByType: Number(data.video_order_by_type || 0),
        orderByRule,
      },
    });
  }

  /**
   * 直播自动数据（装修器直播列表/直播选项卡 自动获取模式取数）
   * 走 mall-cms 装修器接口，由 mall-cms Feign 调 mall-sms；
   * 不落快照：直播间状态实时变化，落快照会让 app 一直显示编辑器那一刻的旧状态。
   */
  static getLiveAutoList(data: LiveAutoListParams) {
    return request({
      url: `/mall-cms/api/v1/diy-designer/live/auto-list`,
      method: "get",
      params: {
        keywords: data.keywords || "",
        // 多选状态以逗号串下发（如 "0,1"），空串=不筛选
        status: data.status || "",
        isRecommended: data.is_recommended ?? null,
        number: Number(data.number || 0),
      },
    });
  }

  /** 视频指定数据（plugins-video/plugins-video-tabs "指定视频" 已保存 id 快照回显,mall-cms by-ids） */
  static getVideoList(data: { ids: number[] }) {
    return request({
      url: `/mall-cms/api/v1/diy-designer/plugins-video/list`,
      method: "get",
      params: { ids: data.ids },
    });
  }
}

export default CommonAPI;
