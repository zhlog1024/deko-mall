import defaultCommon from './index';
import commonTop from './common-top';

/**
 * 直播列表组件默认配置
 * <p>
 * 结构对齐 plugins-video-list.ts（ShopXO model-plugins-video-list 迁移件，已与 app 同构），
 * 字段对齐 app 渲染器：mall-app/pages/diy/components/diy/plugins-live-list.vue
 * <p>
 * 关键点（历史上踩过的坑）：
 * - 显示字段统一用 field_show（兼容历史字段 product_show_list，app 侧双读）
 * - 字体/背景/间距/轮播字段必须齐全，缺了 app 会拼出 font-size: NaNrpx / gap: undefinedpx
 * - plugins_live_height 是 theme==4 的轮播高度，默认 0 会让轮播区高度为 0（不可见）
 * - 数据行契约：指定= data_list 包裹行 {id,new_title,new_cover,data:{...}}，
 *   筛选= data_auto_list 裸行（与 shopxo App 渲染器读取方式一致）
 */
interface LiveRowData {
  id?: number | string;
  title?: string;
  cover?: string;
  url?: string;
  seller_name?: string;
  status?: number | string;
  status_name?: string;
  online_count?: number | string;
}
interface LiveRow {
  id: number | string;
  data: LiveRowData;
  new_title: string;
  new_cover: any[];
}
interface DefaultPluginsLiveList {
  content: {
    content_top: object;
    // 风格:0单列 1两列(纵向) 2大图 3无图 4左右滑动
    theme: string;
    // 轮播列数(theme==4)
    carousel_col: string;
    data_type: string;
    keywords: string;
    // 状态筛选(0-未开播 1-直播中 2-离开；空=全部)
    live_status: string[];
    data_ids: string;
    data_list: LiveRow[];
    data_auto_list: LiveRowData[];
    number: number;
    order_by_type: string;
    order_by_rule: string;
    is_recommended: string;
    // 是否显示:0标题 1主播 2观看数 3直播状态
    field_show: string[];
    // 标题浮起(theme==4)
    name_float: string;
    is_cover: string;
  };
  style: {
    plugins_live_direction: string;
    plugins_live_color_list: color_list[];
    plugins_live_background_img_style: string;
    plugins_live_background_img: uploadList[];
    name_weight: string;
    name_size: number;
    name_color: string;
    name_bg_color_list: color_list[];
    name_bg_direction: string;
    name_bg_margin: marginStyle;
    name_bg_padding: paddingStyle;
    name_bg_radius: radiusStyle;
    time_weight: string;
    time_size: number;
    time_color: string;
    page_view_weight: string;
    page_view_size: number;
    page_view_color: string;
    content_radius: radiusStyle;
    img_radius: radiusStyle;
    margin: marginStyle;
    padding: paddingStyle;
    border_is_show: string;
    border_color: string;
    border_style: string;
    border_size: paddingStyle;
    // 阴影
    box_shadow_color: string;
    box_shadow_x: number;
    box_shadow_y: number;
    box_shadow_blur: number;
    box_shadow_spread: number;
    plugins_live_spacing: number;
    content_spacing: number;
    plugins_live_height: number;
    content_img_width: number;
    content_img_height: number;
    interval_time: number; // 滚动时间
    is_roll: number; // 是否轮播(1/0)
    rolling_fashion: string; // 滚动方式 translation 平移 cut-screen 切屏
    common_style: object;
  };
}

const defaultPluginsLiveList: DefaultPluginsLiveList = {
  content: {
    content_top: {
      ...commonTop,
    },
    theme: '0',
    carousel_col: '2',
    // 数据来源(0-手动选择;1-自动获取，默认自动走接口)
    data_type: '1',
    keywords: '',
    live_status: [],
    data_ids: '',
    data_list: [],
    data_auto_list: [],
    number: 6,
    order_by_type: '0',
    order_by_rule: '0',
    is_recommended: '',
    field_show: ['0', '1', '2', '3'],
    name_float: '0',
    is_cover: '0',
  },
  style: {
    plugins_live_direction: '90deg',
    plugins_live_color_list: [{ color: '#fff', color_percentage: undefined }],
    plugins_live_background_img_style: '0',
    plugins_live_background_img: [],
    name_weight: 'bold',
    name_size: 14,
    name_color: '#333',
    name_bg_color_list: [{ color: 'rgba(0, 0, 0, 0.7)', color_percentage: undefined }],
    name_bg_direction: '180deg',
    name_bg_margin: {
      margin: 0,
      margin_top: 0,
      margin_bottom: 0,
      margin_left: 0,
      margin_right: 0,
    },
    name_bg_padding: {
      padding: 0,
      padding_top: 0,
      padding_bottom: 0,
      padding_left: 10,
      padding_right: 10,
    },
    name_bg_radius: {
      radius: 0,
      radius_top_left: 0,
      radius_top_right: 0,
      radius_bottom_left: 4,
      radius_bottom_right: 4,
    },
    time_weight: '400',
    time_size: 12,
    time_color: 'rgba(153, 153, 153, 1)',
    page_view_weight: '400',
    page_view_size: 12,
    page_view_color: 'rgba(153, 153, 153, 1)',
    // 内容圆角
    content_radius: {
      radius: 8,
      radius_top_left: 8,
      radius_top_right: 8,
      radius_bottom_left: 8,
      radius_bottom_right: 8,
    },
    // 图片圆角
    img_radius: {
      radius: 4,
      radius_top_left: 4,
      radius_top_right: 4,
      radius_bottom_left: 4,
      radius_bottom_right: 4,
    },
    margin: {
      margin: 0,
      margin_top: 0,
      margin_bottom: 0,
      margin_left: 0,
      margin_right: 0,
    },
    // 内间距
    padding: {
      padding: 10,
      padding_top: 10,
      padding_bottom: 10,
      padding_left: 10,
      padding_right: 10,
    },
    // 边框样式
    border_is_show: '0',
    border_color: '#FF3F3F',
    border_style: 'solid',
    border_size: {
      padding: 1,
      padding_top: 1,
      padding_right: 1,
      padding_bottom: 1,
      padding_left: 1,
    },
    // 阴影
    box_shadow_color: '',
    box_shadow_x: 0,
    box_shadow_y: 0,
    box_shadow_blur: 0,
    box_shadow_spread: 0,
    plugins_live_spacing: 10, // 视频间距
    content_spacing: 10, // 内容间距
    plugins_live_height: 155, // 视频高度(theme==4)
    content_img_width: 110,
    content_img_height: 83,
    interval_time: 3, // 滚动时间
    is_roll: 1, // 是否轮播
    rolling_fashion: 'translation', // 滚动方式 translation 平移 cut-screen 切屏
    // 公共样式
    common_style: {
      ...defaultCommon,
      padding: 10,
      padding_top: 10,
      padding_bottom: 10,
      padding_left: 10,
      padding_right: 10,
    },
  },

};

export default defaultPluginsLiveList;
