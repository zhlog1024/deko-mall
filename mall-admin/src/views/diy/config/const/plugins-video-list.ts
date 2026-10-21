import defaultCommon from './index';
import commonTop from './common-top';

/**
 * 视频列表(ShopXO model-plugins-video-list 迁移,mall 化)
 * 数据契约对齐源渲染:指定= data_list 包裹行 {id,new_title,new_cover,data:{title,cover,url,add_time,access_count,...}},
 * 筛选= data_auto_list 裸行(与 shopxo App 渲染器读取方式一致)
 * mall 无视频分类(category 二期)且无封面过滤,content 不存 category_ids/is_cover/角标;
 * 排序仅一维 order_by_type:0综合(默认)/1最新/2浏览/3时长 → mall-cms sortField(默认/createTime/accessCount/duration)
 * 行渲染字段 title/cover/url/add_time/access_count 对齐源渲染器消费字段
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
interface DefaultPluginsVideoList {
  content: {
    content_top: object;
    // 风格:0单列 1两列(纵向) 2大图 3无图 4左右滑动
    theme: string;
    // 轮播列数(theme==4)
    carousel_col: string;
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
    // 是否显示:0时间 1浏览 3标题
    field_show: string[];
    // 标题浮起(theme==4)
    name_float: string;
  };
  style: {
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
const defaultPluginsVideoList: DefaultPluginsVideoList = {
  content: {
    content_top: {
      ...commonTop,
    },
    theme: '0',
    carousel_col: '2',
    data_type: '0',
    keywords: '',
    data_ids: '',
    data_list: [],
    data_auto_list: [],
    number: 4,
    order_by_type: 0,
    order_by_rule: 0,
    is_recommended: '',
    is_hot: '',
    field_show: ['0', '1', '3'],
    name_float: '0',
  },
  style: {
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

export default defaultPluginsVideoList;
