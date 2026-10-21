/*
 * model-custom（自定义块）数据源注册与取数（ShopXO diyapi/custominit 对齐 mall 化）
 * 目标:数据源类型与源一致:商品/文章/品牌/用户商品收藏/用户商品浏览 + 纯图文;
 *      展示模式与源一致(vertical/vertical-scroll/horizontal,列数1-4);
 *      表单字段与源一致(分类级联/品牌/关键字/数量/排序)。
 * 说明:ShopXO 该接口由后端动态下发;mall 端静态注册,选项经 ensure_custom_store_data()
 *      把分类/品牌注入装修器 store(filter-form const_key 读取),文章分类树前端拉取。
 */
import request from "@/utils/request";
import { getSpuPage } from "@/api/pms/goods";
import CommonAPI from "./common";
import { commonStore } from "../store";

/** 统一排序规则 radio(0降序/1升序,与源 common_data_order_by_rule_list) */
const order_by_rule_options = [
  { index: 0, name: "降序" },
  { index: 1, name: "升序" },
];
/** 商品排序(与源 common_goods_order_by_type_list 一致) */
const goods_order_by_type_options = [
  { index: 0, name: "综合" },
  { index: 1, name: "销量" },
  { index: 2, name: "热度" },
  { index: 3, name: "价格" },
  { index: 4, name: "最新" },
];
/** 文章排序(与 mall-cms orderByType:0综合/1人气/2最新 一致) */
const article_order_by_type_options = [
  { index: 0, name: "综合" },
  { index: 1, name: "人气" },
  { index: 2, name: "最新" },
];
/** 收藏排序(与源 common_goods_favor_order_by_type_list 一致) */
const favor_order_by_type_options = [
  { index: 0, name: "综合" },
  { index: 1, name: "销量" },
  { index: 2, name: "热度" },
  { index: 3, name: "价格" },
  { index: 4, name: "最新" },
];
/** 浏览排序(与源 common_goods_browse_order_by_type_list 一致) */
const browse_order_by_type_options = [
  { index: 0, name: "综合" },
  { index: 1, name: "销量" },
  { index: 2, name: "热度" },
  { index: 3, name: "价格" },
  { index: 4, name: "最新" },
  { index: 5, name: "用户浏览" },
];

const keyword_input = (placeholder: string, formName: string) => ({
  type: "input",
  config: { placeholder, type: "text" },
  title: "关键字",
  form_name: formName,
});
const number_input = (formName: string, def = 4) => ({
  type: "input",
  config: { default: def, type: "number" },
  title: "显示数量",
  form_name: formName,
});
/** 分类级联 select(选项经 store const_key 注入) */
const category_select = (
  formName: string,
  constKey: string,
  title = "分类",
  isMultiple = 1
) => ({
  type: "select",
  config: {
    placeholder: "请选择分类",
    is_level: 1,
    is_multiple: isMultiple,
    children: "children",
  },
  title,
  form_name: formName,
  const_key: constKey,
});
/** 品牌多选(选项经 store brand_list 注入) */
const brand_select = (formName: string) => ({
  type: "select",
  config: { is_multiple: 1 },
  title: "指定品牌",
  form_name: formName,
  const_key: "brand_list",
});
const radio_item = (title: string, formName: string, data: any[], def = 0) => ({
  type: "radio",
  title,
  form_name: formName,
  data,
  data_key: "index",
  data_name: "name",
  config: { default: def },
});

/** 商品字段表(渲染子块可绑定,对齐源 goods data) */
const goods_field_list = [
  { name: "数据索引", field: "data_index", type: "text" },
  { name: "商品ID", field: "id", type: "text" },
  { name: "标题", field: "title", type: "text" },
  { name: "商品链接", field: "url", type: "link" },

  { name: "商品分类", field: "category_text", type: "text" },
  { name: "商品图片", field: "images", type: "images" },
  { name: "售价", field: "price", type: "text" },
  { name: "添加时间", field: "add_time", type: "text" },
];
/** 文章字段表(对齐源 article data) */
const article_field_list = [
  { name: "数据索引", field: "data_index", type: "text" },
  { name: "文章ID", field: "id", type: "text" },
  { name: "标题", field: "title", type: "text" },
  { name: "文章链接", field: "url", type: "link" },

  { name: "分类名称", field: "article_category_name", type: "text" },
  { name: "描述", field: "describe", type: "text" },
  { name: "封面图片", field: "cover", type: "images" },
  { name: "访问量", field: "access_count", type: "text" },
  { name: "添加时间", field: "add_time", type: "text" },
];
/** 品牌字段表 */
const brand_field_list = [
  { name: "数据索引", field: "data_index", type: "text" },
  { name: "品牌ID", field: "id", type: "text" },
  { name: "名称", field: "name", type: "text" },
  { name: "LOGO", field: "logo", type: "images" },
];
/** 用户商品源字段表 */
const user_goods_field_list = [
  { name: "数据索引", field: "data_index", type: "text" },
  { name: "商品ID", field: "id", type: "text" },
  { name: "标题", field: "title", type: "text" },
  { name: "商品链接", field: "url", type: "link" },

  { name: "商品图片", field: "images", type: "images" },
  { name: "商品价格", field: "price", type: "text" },
];

const appoint_header = (name: string, labelName: string, imgField: string) => [
  { field: "id", name, width: 90 },
  { field: imgField, name: "图片", type: "images", width: 90 },
  { field: labelName === "title" ? "title" : "name", name: labelName },
];

/**
 * 数据源注册表(与源一致:goods/article/brand/user-goods-favor/user-goods-browse)
 */
const data_source_list = [
  {
    type: "goods",
    name: "商品",
    data: goods_field_list,
    custom_config: {
      show_type: ["vertical", "vertical-scroll", "horizontal"],
      show_number: [1, 2, 3, 4],
      data_type: [0, 1],
      is_type_show: "1",
      is_multiple: 1,
      appoint_config: {
        popup_title: "商品选择",
        data_url: "/mall-pms/api/v1/spu/page",
        method: "get",
        is_multiple: 1,
        show_data: { data_key: "id", data_name: "title", data_logo: "images" },
        header: appoint_header("商品ID", "title", "images"),
        search_filter_form_config: [
          category_select("category_ids", "goods_category", "商品分类"),
          keyword_input("请输入商品关键字", "keywords"),
        ],
      },
      filter_config: {
        data_url: "/mall-pms/api/v1/spu/page",
        method: "get",
        filter_form_config: [
          category_select("goods_category_ids", "goods_category", "商品分类"),
          brand_select("goods_brand_ids"),
          keyword_input("请输入商品关键字", "goods_keywords"),
          number_input("goods_number", 4),
          radio_item("排序类型", "goods_order_by_type", goods_order_by_type_options),
          radio_item("排序规则", "goods_order_by_rule", order_by_rule_options),
        ],
      },
    },
  },
  {
    type: "article",
    name: "文章",
    data: article_field_list,
    custom_config: {
      show_type: ["vertical", "vertical-scroll", "horizontal"],
      show_number: [1, 2, 3, 4],
      data_type: [0, 1],
      is_type_show: "1",
      is_multiple: 1,
      appoint_config: {
        popup_title: "文章选择",
        data_url: "/mall-cms/api/v1/article/page",
        method: "get",
        is_multiple: 1,
        show_data: { data_key: "id", data_name: "title", data_logo: "cover" },
        header: appoint_header("文章ID", "title", "cover"),
        search_filter_form_config: [
          category_select("category_ids", "article_category", "文章分类"),
          keyword_input("请输入文章关键字", "keywords"),
        ],
      },
      filter_config: {
        data_url: "/mall-cms/api/v1/article/page",
        method: "get",
        filter_form_config: [
          category_select("article_category_ids", "article_category", "文章分类"),
          keyword_input("请输入文章关键字", "article_keywords"),
          number_input("article_number", 4),
          radio_item("排序类型", "article_order_by_type", article_order_by_type_options),
          radio_item("排序规则", "article_order_by_rule", order_by_rule_options),
        ],
      },
    },
  },
  {
    type: "brand",
    name: "品牌",
    data: brand_field_list,
    custom_config: {
      show_type: ["vertical", "vertical-scroll", "horizontal"],
      show_number: [1, 2, 3, 4],
      data_type: [0, 1],
      is_type_show: "1",
      is_multiple: 1,
      appoint_config: {
        popup_title: "品牌选择",
        data_url: "/mall-pms/api/v1/brands/page",
        method: "get",
        is_multiple: 1,
        show_data: { data_key: "id", data_name: "name", data_logo: "logo" },
        header: appoint_header("品牌ID", "name", "logo"),
        search_filter_form_config: [
          category_select("category_ids", "brand_category", "品牌分类", 1),
          keyword_input("请输入品牌关键字", "keywords"),
        ],
      },
      filter_config: {
        data_url: "/mall-pms/api/v1/brands/page",
        method: "get",
        filter_form_config: [
          category_select("brand_category_ids", "brand_category", "品牌分类", 1),
          keyword_input("请输入品牌关键字", "brand_keywords"),
          number_input("brand_number", 4),
        ],
      },
    },
  },
  {
    type: "user-goods-favor",
    name: "用户商品收藏",
    data: user_goods_field_list,
    custom_config: {
      show_type: ["vertical", "vertical-scroll", "horizontal"],
      show_number: [1, 2, 3, 4],
      data_type: [1],
      is_type_show: 0,
      is_multiple: 1,
      appoint_config: null,
      filter_config: {
        data_url: "/mall-ums/app-api/v1/favor?targetType=GOODS",
        method: "get",
        filter_form_config: [
          keyword_input("请输入商品关键字", "goods_keywords"),
          number_input("goods_number", 4),
          radio_item("排序类型", "goods_order_by_type", favor_order_by_type_options),
          radio_item("排序规则", "goods_order_by_rule", order_by_rule_options),
        ],
      },
    },
  },
  {
    type: "user-goods-browse",
    name: "用户商品浏览",
    data: user_goods_field_list,
    custom_config: {
      show_type: ["vertical", "vertical-scroll", "horizontal"],
      show_number: [1, 2, 3, 4],
      data_type: [1],
      is_type_show: 0,
      is_multiple: 1,
      appoint_config: null,
      filter_config: {
        data_url: "/mall-ums/app-api/v1/goods-browse",
        method: "get",
        filter_form_config: [
          keyword_input("请输入商品关键字", "goods_keywords"),
          number_input("goods_number", 4),
          radio_item("排序类型", "goods_order_by_type", browse_order_by_type_options),
          radio_item("排序规则", "goods_order_by_rule", order_by_rule_options),
        ],
      },
    },
  },
];

/** 树形转换(装修器分类树 → filter-form 需要的 {id,name,children}) */
function tree_to_options(nodes: any[]): any[] {
  return (nodes || []).map((n: any) => ({
    id: n.id,
    name: n.name,
    children: tree_to_options(n.children || n.items || []),
  }));
}

/**
 * 把分类/品牌选项注入装修器 store(供 filter-form const_key 读取,与源常量一致)
 * goods_category: 商品分类树(装修器 init 已下发);article_category: 文章分类树(cms 拉取);
 * brand_list: 品牌列表(init 已下发);brand_category: mall 无品牌分类体系 → 置空。
 */
export function ensure_custom_store_data() {
  const common_store = commonStore();
  const common: any = common_store.common;
  try {
    if (common && !common.goods_category && common.goodsCategory) {
      common.goods_category = tree_to_options(common.goodsCategory);
    }
    if (common && !common.brand_list) {
      common.brand_list =
        (common.brandList || []).map((b: any) => ({
          id: b.id,
          name: b.name || "",
          logoUrl: b.logoUrl || b.logo || "",
        })) || [];
    }
    if (common && !common.brand_category) {
      common.brand_category = [];
    }
    if (common && !common.article_category) {
      CommonAPI.getArticleCategory()
        .then((res: any) => {
          const list = (res as any)?.data || [];
          if (list && list.length > 0 && !common.article_category) {
            common.article_category = tree_to_options(list);
          }
        })
        .catch(() => {});
    }
  } catch (e) {
    console.error("自定义数据源选项注入失败:", e);
  }
}

/**
 * 获取数据源类型注册表(编辑器"动态数据"下拉,与源一致)
 */
export function getCustominit() {
  ensure_custom_store_data();
  return Promise.resolve({
    code: "00000",
    msg: "",
    data: { data_source: data_source_list },
  });
}

/** 时间归一 */
function fmtTime(v?: string): string {
  return v ? String(v).replace("T", " ").slice(0, 19) : "";
}

/**
 * mall 行 → 源契约行(供快照保存/预览渲染,字段对齐源 data 表)
 */
export function toCustomRow(source: string, row: any): Record<string, any> {
  if (!row) {
    return {};
  }
  const pic = row.picUrl || row.coverUrl || row.cover || "";
  if (
    source === "goods" ||
    source === "user-goods-favor" ||
    source === "user-goods-browse"
  ) {
    const goods = source !== "goods" && row.goods ? row.goods : row;
    const gpic = goods.picUrl || goods.coverUrl || pic || "";
    return {
      id: goods.id ?? row.id,
      url: "/pages/goods-detail/goods-detail?id=" + (goods.id ?? row.id),
      title: goods.name || goods.title || row.title || "",
      images: gpic,
      price: goods.retailPrice || goods.price || "",
      add_time: fmtTime(row.createTime || goods.createTime),
      data_source_row: row,
    };
  }
  if (source === "article") {
    const apic = row.cover || pic || "";
    return {
      id: row.id,
      url: "/pages/article-detail/article-detail?id=" + row.id,
      title: row.title || "",
      describe: row.description || row.describe || "",
      cover: apic,
      article_category_name: row.categoryName || "",
      access_count: row.accessCount || row.access_count || 0,
      add_time: fmtTime(row.createTime || row.add_time),
      data_source_row: row,
    };
  }
  // brand
  const logo = row.logoUrl || row.logo || "";
  return {
    id: row.id,
    name: row.name || "",
    logo,
    add_time: "",
    data_source_row: row,
  };
}

/**
 * 编辑端"筛选数据"拉取并归一(自动模式)
 * goods→pms spu/page;article→cms 文章自动;
 * user-goods-favor/browse 为用户私有数据,编辑端无 app 用户上下文,返回空(运行端按登录用户实时展示,与源一致)
 */
export async function fetchAutoSource(
  source: string,
  cfg: any
): Promise<Record<string, any>[]> {
  if (source === "article") {
    const res: any = await CommonAPI.getArticleAutoList({
      article_category_ids: cfg?.article_category_ids || "",
      article_keywords: cfg?.article_keywords || cfg?.keywords || "",
      article_number: cfg?.article_number || 4,
      article_order_by_type: cfg?.article_order_by_type ?? 0,
      article_order_by_rule: cfg?.article_order_by_rule ?? 0,
      article_is_cover: cfg?.article_is_cover || "0",
    });
    const list = (res as any)?.data || [];
    return list.map((row: any) => toCustomRow("article", row));
  }
  if (source === "user-goods-favor" || source === "user-goods-browse") {
    console.warn(
      "自定义块用户数据源(user-goods-favor/browse)运行端按当前登录用户实时展示,编辑端不预取"
    );
    return [];
  }
  // goods
  const res: any = await getSpuPage({
    pageNum: 1,
    pageSize: Number(cfg?.goods_number || 4),
    keywords: cfg?.goods_keywords || cfg?.keywords || "",
  });
  const data = (res as any)?.data || {};
  const list = data?.list || data?.records || [];
  return list.map((row: any) => toCustomRow("goods", row));
}

export default {
  getCustominit,
  toCustomRow,
  fetchAutoSource,
  ensure_custom_store_data,
};
