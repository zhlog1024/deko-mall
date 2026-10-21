import defaultCommon from './index';
import commonTop from './common-top';

/**
 * 直播选项卡组件默认配置
 * <p>
 * 结构对齐 plugins-video-tabs.ts（ShopXO model-plugins-video-tabs 迁移件，已与 app 同构），
 * 字段对齐 app 渲染器：mall-app/pages/diy/components/diy/plugins-live-tabs.vue
 * + modules/tabs-view.vue + plugins-live-list.vue。
 * <p>
 * 关键点（历史上踩过的坑）：
 * - 分类名用 title（渲染器 tabs-view 读 item.title），不是 name
 * - tabs 样式族必须齐全，否则 tabs-view 拼出 font-size: NaNrpx / padding-left: NaNrpx，分类栏塌陷
 * - 内嵌直播列表读的是列表契约字段（content_img_* / content_spacing / plugins_live_spacing /
 *   name_size / time_size / page_view_size / is_roll / interval_time / rolling_fashion），缺了会
 *   拼出 gap: undefinedpx
 * - 顶层 plugins_live_theme / plugins_live_carousel_col 作用于内嵌直播列表；tabs_list 每 tab 独立数据配置
 * - 数据行契约：指定= data_list 包裹行，筛选= data_auto_list 裸行
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
interface LiveTabItem {
  id: string;
  tabs_type: string;
  tabs_img: any[];
  tabs_icon: string;
  is_sliding_fixed: string;
  title: string;
  desc: string;
  data_type: string;
  keywords: string;
  live_status: string[];
  number: number;
  order_by_type: string;
  order_by_rule: string;
  is_recommended: string;
  data_ids: string;
  is_cover: string;
  data_list: LiveRow[];
  data_auto_list: LiveRowData[];
}
interface DefaultPluginsLiveTabs {
  content: {
    content_top: object;
    justification: string;
    tabs_theme: string;
    tabs_adorn_icon: string;
    tabs_adorn_img: any[];
    is_general_safe_distance: string;
    tabs_top_up: string;
    plugins_live_theme: string;
    plugins_live_carousel_col: number;
    tabs_list: LiveTabItem[];
    tabs_active_index: number;
    field_show: string[];
    name_float: string;
  };
  style: {
    tabs_one_theme: string;
    tabs_checked: color_list[];
    is_tabs_adorn_img_background: string;
    tabs_adorn_icon_size: number;
    tabs_adorn_img_radius: radiusStyle;
    tabs_adorn_img_height: number;
    tabs_direction: string;
    tabs_weight_checked: string;
    tabs_size_checked: number;
    tabs_color_checked: string;
    tabs_weight: string;
    tabs_size: number;
    tabs_color: string;
    tabs_icon_color_checked: string;
    tabs_icon_size_checked: number;
    tabs_icon_color: string;
    tabs_icon_size: number;
    is_tabs_img_background: string;
    tabs_img_radius: radiusStyle;
    tabs_img_height: number;
    tabs_spacing: number;
    tabs_sign_spacing: number;
    tabs_bg_direction: string;
    tabs_bg_color_list: color_list[];
    tabs_bg_background_img_style: string;
    tabs_bg_background_img: string[];
    tabs_radius: radiusStyle;
    tabs_margin: marginStyle;
    tabs_padding: paddingStyle;
    tabs_content: object;
    plugins_live_content_direction: string;
    plugins_live_content_color_list: color_list[];
    plugins_live_content_background_img_style: string;
    plugins_live_content_background_img: string[];
    plugins_live_content_radius: radiusStyle;
    plugins_live_content_margin: marginStyle;
    plugins_live_content_padding: paddingStyle;
    plugins_live_content_spacing: number;
    plugins_live_content: object;
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
    tabs_top_img_height: number;
    tabs_top_img_radius: radiusStyle;
    more_icon_class: string;
    more_icon_color: string;
    more_icon_size: number;
    more_button_text_color: string;
    more_button_text_selected_color: string;
    more_button_direction: string;
    more_button_color_list: color_list[];
    more_button_radius: radiusStyle;
    common_style: object;
  };
}

const defaultTab = (id: string, title: string): LiveTabItem => ({
  id,
  // 分类展示类型(0-文字 1-图标/图片)
  tabs_type: '0',
  tabs_img: [],
  tabs_icon: '',
  is_sliding_fixed: '0',
  // 分类名（渲染器读 title）
  title,
  desc: '',
  // 数据来源(0-手动选择;1-自动获取，默认自动走接口)
  data_type: '1',
  keywords: '',
  // 状态筛选(0-未开播 1-直播中 2-离开；空=全部)
  live_status: [],
  number: 6,
  order_by_type: '0',
  order_by_rule: '0',
  is_recommended: '',
  data_ids: '',
  is_cover: '0',
  data_list: [],
  data_auto_list: [],
});

const defaultPluginsLiveTabs: DefaultPluginsLiveTabs = {
  content: {
    content_top: {
      ...commonTop,
    },
    // 选项卡对齐方式
    justification: 'left',
    // 选项卡风格(0-风格1 1-风格2 2-风格3 3-风格4 4-风格5)
    tabs_theme: '0',
    // 选中装饰图标
    tabs_adorn_icon: 'checked-smooth',
    // 是否使用安全距离
    is_general_safe_distance: '0',
    tabs_adorn_img: [],
    // 选项卡是否吸顶
    tabs_top_up: '0',
    // 直播区风格(0单列 1两列 2大图 3无图 4左右滑动)
    plugins_live_theme: '0',
    // 轮播列数(plugins_live_theme==4)
    plugins_live_carousel_col: 3,
    // 分类数据
    tabs_list: [defaultTab('1', '全部'), defaultTab('2', '直播中')],
    // 选中的内容索引
    tabs_active_index: 0,
    // 显示字段(0-标题 1-主播 2-观看数 3-直播状态)
    field_show: ['0', '1', '2', '3'],
    // 标题浮起(plugins_live_theme==4)
    name_float: '0',
  },
  style: {
    // 选中样式风格
    tabs_one_theme: '0',
    tabs_checked: [
      { color: '#FF2222', color_percentage: undefined },
      { color: '#FF9898', color_percentage: undefined },
    ],
    is_tabs_adorn_img_background: '0',
    tabs_adorn_icon_size: 15,
    tabs_adorn_img_radius: {
      radius: 0,
      radius_top_left: 0,
      radius_top_right: 0,
      radius_bottom_left: 0,
      radius_bottom_right: 0,
    },
    tabs_adorn_img_height: 10,
    tabs_direction: '90deg',
    tabs_weight_checked: 'bold',
    tabs_size_checked: 14,
    tabs_color_checked: 'rgba(51,51,51,1)',
    tabs_weight: 'bold',
    tabs_size: 14,
    tabs_color: 'rgba(51,51,51,1)',
    tabs_icon_color_checked: 'rgba(51,51,51,1)',
    tabs_icon_size_checked: 14,
    tabs_icon_color: 'rgba(51,51,51,1)',
    tabs_icon_size: 14,
    is_tabs_img_background: '0',
    tabs_img_radius: {
      radius: 0,
      radius_top_left: 0,
      radius_top_right: 0,
      radius_bottom_left: 0,
      radius_bottom_right: 0,
    },
    tabs_img_height: 20,
    // tabs间距
    tabs_spacing: 20,
    tabs_sign_spacing: 4,
    // 选项卡背景设置
    tabs_bg_direction: '90deg',
    tabs_bg_color_list: [{ color: '', color_percentage: undefined }],
    tabs_bg_background_img_style: '2',
    tabs_bg_background_img: [],
    tabs_radius: {
      radius: 0,
      radius_top_left: 0,
      radius_top_right: 0,
      radius_bottom_left: 0,
      radius_bottom_right: 0,
    },
    tabs_margin: {
      margin: 0,
      margin_top: 0,
      margin_bottom: 0,
      margin_left: 0,
      margin_right: 0,
    },
    tabs_padding: {
      padding: 10,
      padding_top: 10,
      padding_bottom: 10,
      padding_left: 10,
      padding_right: 10,
    },
    tabs_content: {
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
    },
    // 视频内容区域设置
    plugins_live_content_direction: '90deg',
    plugins_live_content_color_list: [{ color: '', color_percentage: undefined }],
    plugins_live_content_background_img_style: '2',
    plugins_live_content_background_img: [],
    plugins_live_content_radius: {
      radius: 0,
      radius_top_left: 0,
      radius_top_right: 0,
      radius_bottom_left: 0,
      radius_bottom_right: 0,
    },
    plugins_live_content_margin: {
      margin: 0,
      margin_top: 0,
      margin_bottom: 0,
      margin_left: 0,
      margin_right: 0,
    },
    plugins_live_content_padding: {
      padding: 0,
      padding_top: 0,
      padding_bottom: 10,
      padding_left: 10,
      padding_right: 10,
    },
    plugins_live_content: {
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
    },
    plugins_live_content_spacing: 0,
    // 视频背景设置
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
    // 选项卡风格4 顶部图片高度/圆角
    tabs_top_img_height: 39,
    tabs_top_img_radius: {
      radius: 100,
      radius_top_left: 100,
      radius_top_right: 100,
      radius_bottom_left: 100,
      radius_bottom_right: 100,
    },
    // 更多按钮/图标（tabs-view 的"全部选项卡"弹窗）
    more_icon_class: 'category-more',
    more_icon_color: '#000',
    more_icon_size: 14,
    more_button_text_color: '#666',
    more_button_text_selected_color: '#666',
    more_button_direction: '90deg',
    more_button_color_list: [{ color: '#eee', color_percentage: undefined }],
    more_button_radius: {
      radius: 57,
      radius_top_left: 57,
      radius_top_right: 57,
      radius_bottom_left: 57,
      radius_bottom_right: 57,
    },

    // 公共样式
    common_style: defaultCommon,
  },

};

export default defaultPluginsLiveTabs;
