import defaultCommon from './index';
import commonTop from './common-top';

/**
 * 商品问答列表(ShopXO model-ask 迁移,mall 化)
 * 数据契约对齐源渲染:指定= data_list 包裹行 {id,new_title,new_cover,data:{title,add_time_date,is_reply,access_count,url,...}},
 * 筛选= data_auto_list 裸行(与 shopxo App 渲染器读取方式一致)
 * 无分类(mall-cms cms_ask 按商品维度),content 不存 category_ids
 */
interface AskRowData {
  id?: number | string;
  title?: string;
  add_time_date?: string;
  is_reply?: number | string;
  access_count?: number | string;
  url?: string;
  spu_id?: number | string;
  spu_name?: string;
  spu_pic?: string;
}
interface AskRow {
  id: number | string;
  data: AskRowData;
  new_title: string;
  new_cover: uploadList[];
}
interface DefaultAsk {
  content: {
    content_top: object;
    // 风格:0单列 1两列(纵向) 2左右滑动
    theme: string;
    // 轮播列数(theme==2)
    carousel_col: number;
    data_type: string;
    keywords: string;
    data_ids: string;
    data_list: AskRow[];
    data_auto_list: AskRowData[];
    number: number;
    order_by_type: number;
    order_by_rule: number;
    // 回复状态过滤(空=全部/0未回/1已回)
    is_reply: string;
    // 是否显示:reply_status/time/page_view/ranking
    is_show: string[];
    // 标题显示方式 0自动 1一行 2两行
    title_display_method: string;
  };
  style: {
    ask_color_list: color_list[];
    ask_direction: string;
    ask_background_img_style: string;
    ask_background_img: uploadList[];
    ask_margin: marginStyle;
    ask_padding: paddingStyle;
    ask_radius: radiusStyle;
    content_img_width: number;
    content_img_height: number;
    content_spacing: number;
    content_outer_spacing: number;
    content_outer_height: number;
    ask_title_color: string;
    ask_title_typeface: string;
    ask_title_size: number;
    ask_time_color: string;
    ask_time_typeface: string;
    ask_time_size: number;
    ask_page_view_color: string;
    ask_page_view_typeface: string;
    ask_page_view_size: number;
    is_roll: string;
    interval_time: number;
    rolling_fashion: string;
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
    returned_style: object;
    not_replied_yet_style: object;
    common_style: object;
  };
}
const defaultAsk: DefaultAsk = {
  content: {
    content_top: {
      ...commonTop,
    },
    theme: '0',
    carousel_col: 1,
    data_type: '0',
    keywords: '',
    data_ids: '',
    data_list: [],
    data_auto_list: [],
    number: 4,
    order_by_type: 0,
    order_by_rule: 0,
    is_reply: '',
    is_show: ['time', 'page_view', 'reply_status', 'ranking'],
    title_display_method: '0',
  },
  style: {
    ask_color_list: [{ color: '#fff', color_percentage: undefined }],
    ask_direction: '90deg',
    ask_background_img_style: '2',
    ask_background_img: [],
    ask_margin: {
      margin: 0,
      margin_top: 0,
      margin_bottom: 0,
      margin_left: 0,
      margin_right: 0,
    },
    ask_padding: {
      padding: 10,
      padding_top: 10,
      padding_bottom: 10,
      padding_left: 10,
      padding_right: 10,
    },
    ask_radius: {
      radius: 8,
      radius_top_left: 8,
      radius_top_right: 8,
      radius_bottom_left: 8,
      radius_bottom_right: 8,
    },
    content_img_width: 50,
    content_img_height: 50,
    content_spacing: 10,
    content_outer_spacing: 10,
    content_outer_height: 204,
    ask_title_color: '#333',
    ask_title_typeface: 'bold',
    ask_title_size: 14,
    ask_time_color: '#999',
    ask_time_typeface: '400',
    ask_time_size: 11,
    ask_page_view_color: '#999',
    ask_page_view_typeface: '400',
    ask_page_view_size: 11,
    // 是否滚动
    is_roll: '1',
    interval_time: 3,
    rolling_fashion: 'translation', // 滚动方式  translation 平移 cut-screen 切屏
    border_is_show: '0',
    border_color: '#eee',
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
    returned_style: {
      color_list: [{ color: '#10BB78', color_percentage: undefined }],
      direction: '90deg',
      background_img_style: '2',
      background_img: [],
      font_color: '#fff',
      font_size: 12,
      font_typeface: '400',
      font_padding: {
        padding: 0,
        padding_top: 0,
        padding_bottom: 0,
        padding_left: 7,
        padding_right: 7,
      },
      font_radius: {
        radius: 4,
        radius_top_left: 4,
        radius_top_right: 4,
        radius_bottom_left: 4,
        radius_bottom_right: 4,
      },
      border_is_show: '0',
      border_color: '#eee',
      border_style: 'solid',
      border_size: {
        padding: 1,
        padding_top: 1,
        padding_right: 1,
        padding_bottom: 1,
        padding_left: 1,
      },
    },
    not_replied_yet_style: {
      color_list: [{ color: '#E3CD35', color_percentage: undefined }],
      direction: '90deg',
      background_img_style: '2',
      background_img: [],
      font_color: '#fff',
      font_size: 12,
      font_typeface: '400',
      font_padding: {
        padding: 0,
        padding_top: 0,
        padding_bottom: 0,
        padding_left: 7,
        padding_right: 7,
      },
      font_radius: {
        radius: 4,
        radius_top_left: 4,
        radius_top_right: 4,
        radius_bottom_left: 4,
        radius_bottom_right: 4,
      },
      border_is_show: '0',
      border_color: '#eee',
      border_style: 'solid',
      border_size: {
        padding: 1,
        padding_top: 1,
        padding_right: 1,
        padding_bottom: 1,
        padding_left: 1,
      },
    },
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

export default defaultAsk;
