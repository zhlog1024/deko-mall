import defaultCommon from './index';
import commonTop from './common-top';

/**
 * 视频选项卡(ShopXO model-plugins-video-tabs 迁移,mall 化)
 * 顶层 plugins_video_theme/carousel_col/field_show/name_float 作用于视频列表;tabs_list 每 tab 独立数据配置
 * 数据行契约与 model-plugins-video-list 一致(指定= data_list 包裹行,筛选= data_auto_list 裸行)
 * mall 无视频分类(category 二期)且无封面过滤,content/tab 不存 category_ids/is_cover/角标
 */
interface VideoRowData {
  id?: number | string;
  title?: string;
  cover?: string;
  url?: string;
  add_time?: string;
  access_count?: number | string;
  duration?: number | string;
  is_recommended?: number | string;
  is_hot?: number | string;
}
interface VideoRow {
  id: number | string;
  data: VideoRowData;
  new_title: string;
  new_cover: uploadList[];
}
interface PluginsVideoTabsItem {
  id: string;
  tabs_type: string;
  tabs_img: uploadList[];
  tabs_icon: string;
  is_sliding_fixed: string;
  title: string;
  desc: string;
  data_type: string;
  keywords: string;
  data_ids: string;
  data_list: VideoRow[];
  data_auto_list: VideoRowData[];
  number: number;
  // 排序类型:0综合(默认 sort+创建时间) 1最新(createTime) 2浏览(accessCount) 3时长(duration)
  order_by_type: number;
  // 排序规则:0降序/1升序(综合忽略,固定 sort asc+创建时间倒序)
  order_by_rule: number;
  // 推荐/热门过滤(空=全部/1=是/0=否)
  is_recommended: string;
  is_hot: string;
}
interface DefaultPluginsVideoTabs {
  content: {
    content_top: object;
    justification: string;
    tabs_theme: string;
    tabs_adorn_icon: string;
    tabs_adorn_img: uploadList[];
    is_general_safe_distance: string;
    tabs_top_up: string;
    // 视频列表风格(同 plugins-video-list theme:0单列 1两列 2大图 3无图 4左右滑动)
    plugins_video_theme: string;
    // 轮播列数(plugins_video_theme==4)
    plugins_video_carousel_col: string;
    tabs_list: PluginsVideoTabsItem[];
    tabs_active_index: number;
    // 是否显示:0时间 1浏览 3标题
    field_show: string[];
    // 标题浮起(plugins_video_theme==4)
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
    plugins_video_content_direction: string;
    plugins_video_content_color_list: color_list[];
    plugins_video_content_background_img_style: string;
    plugins_video_content_background_img: string[];
    plugins_video_content_radius: radiusStyle;
    plugins_video_content_margin: marginStyle;
    plugins_video_content_padding: paddingStyle;
    plugins_video_content_spacing: number;
    plugins_video_content: object;
    plugins_video_direction: string;
    plugins_video_color_list: color_list[];
    plugins_video_background_img_style: string;
    plugins_video_background_img: uploadList[];
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
    plugins_video_spacing: number;
    content_spacing: number;
    plugins_video_height: number;
    content_img_width: number;
    content_img_height: number;
    interval_time: number; // 滚动时间
    is_roll: number; // 是否轮播(1/0)
    rolling_fashion: string; // 滚动方式 translation 平移 cut-screen 切屏
    common_style: object;
  };
}
const defaultPluginsVideoTabs: DefaultPluginsVideoTabs = {
  content: {
    content_top: {
      ...commonTop,
    },
    justification: 'left',
    // 选项卡风格
    tabs_theme: '0',
    // 选中装饰图标
    tabs_adorn_icon: 'checked-smooth',
    is_general_safe_distance: '0',
    tabs_adorn_img: [],
    // 是否置顶
    tabs_top_up: '0',
    // 视频列表风格
    plugins_video_theme: '0',
    plugins_video_carousel_col: '2',
    // 选项卡数据
    tabs_list: [
      {
        id: '1',
        tabs_type: '0',
        tabs_img: [],
        tabs_icon: '',
        is_sliding_fixed: '0',
        title: '热门推荐',
        desc: '简介',
        data_type: '0',
        keywords: '',
        data_ids: '',
        number: 4,
        order_by_type: 0,
        order_by_rule: 0,
        is_recommended: '',
        is_hot: '',
        data_list: [],
        data_auto_list: [],
      },
      {
        id: '2',
        tabs_type: '0',
        tabs_img: [],
        tabs_icon: '',
        is_sliding_fixed: '0',
        title: '测试一',
        desc: '简介',
        data_type: '0',
        keywords: '',
        data_ids: '',
        number: 4,
        order_by_type: 0,
        order_by_rule: 0,
        is_recommended: '',
        is_hot: '',
        data_list: [],
        data_auto_list: [],
      },
      {
        id: '3',
        tabs_type: '0',
        tabs_img: [],
        tabs_icon: '',
        is_sliding_fixed: '0',
        title: '测试二',
        desc: '简介',
        data_type: '0',
        keywords: '',
        data_ids: '',
        number: 4,
        order_by_type: 0,
        order_by_rule: 0,
        is_recommended: '',
        is_hot: '',
        data_list: [],
        data_auto_list: [],
      },
      {
        id: '4',
        tabs_type: '0',
        tabs_img: [],
        tabs_icon: '',
        is_sliding_fixed: '0',
        title: '测试三',
        desc: '简介',
        data_type: '0',
        keywords: '',
        data_ids: '',
        number: 4,
        order_by_type: 0,
        order_by_rule: 0,
        is_recommended: '',
        is_hot: '',
        data_list: [],
        data_auto_list: [],
      },
    ],
    // 选中的内容索引
    tabs_active_index: 0,
    field_show: ['0', '1', '3'],
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
    plugins_video_content_direction: '90deg',
    plugins_video_content_color_list: [{ color: '', color_percentage: undefined }],
    plugins_video_content_background_img_style: '2',
    plugins_video_content_background_img: [],
    plugins_video_content_radius: {
      radius: 0,
      radius_top_left: 0,
      radius_top_right: 0,
      radius_bottom_left: 0,
      radius_bottom_right: 0,
    },
    plugins_video_content_margin: {
      margin: 0,
      margin_top: 0,
      margin_bottom: 0,
      margin_left: 0,
      margin_right: 0,
    },
    plugins_video_content_padding: {
      padding: 0,
      padding_top: 0,
      padding_bottom: 10,
      padding_left: 10,
      padding_right: 10,
    },
    plugins_video_content: {
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
    plugins_video_content_spacing: 0,
    // 视频背景设置
    plugins_video_direction: '90deg',
    plugins_video_color_list: [{ color: '#fff', color_percentage: undefined }],
    plugins_video_background_img_style: '0',
    plugins_video_background_img: [],
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
    plugins_video_spacing: 10, // 视频间距
    content_spacing: 10, // 内容间距
    plugins_video_height: 155, // 视频高度(theme==4)
    content_img_width: 110,
    content_img_height: 83,
    interval_time: 3, // 滚动时间
    is_roll: 1, // 是否轮播
    rolling_fashion: 'translation', // 滚动方式 translation 平移 cut-screen 切屏
    // 公共样式
    common_style: defaultCommon,
  },
};

export default defaultPluginsVideoTabs;
