import defaultCommon from './index';
import commonTop from './common-top';

interface DefaultShop {
    content: {
        content_top: object;
        theme: string;
        carousel_col: number;
        shop_desc: string;
        shop_desc_row: string;
        data_type: string;
        data_ids: string;
        data_list: [];
        data_auto_list: [];
        category_ids: string[];
        number: number;
        keywords: string;
        is_goods_list: string;
        is_right_show: string;
        right_type: string;
        right_img: [];
        right_icon: string;
        right_text: string;
    };
    style: {
        shop_color_list: color_list[];
        shop_direction: string;
        shop_background_img_style: string;
        shop_background_img: [];
        shop_img_radius: radiusStyle;
        shop_margin: marginStyle;
        shop_padding: paddingStyle;
        shop_radius: radiusStyle;
        content_img_width: number;
        content_img_height: number;
        content_spacing: number;
        content_outer_spacing: number;
        content_outer_height: number;
        shop_title_img_width: number;
        shop_title_img_height: number;
        shop_title_img_radius: radiusStyle;
        shop_title_img_inner_spacing: number;
        shop_title_img_outer_spacing: number;
        shop_title_color: string;
        shop_title_typeface: string;
        shop_title_size: number;
        shop_desc_color: string;
        shop_desc_typeface: string;
        shop_desc_size: number;
        right_style: object;
        is_roll: string;
        interval_time: number;
        rolling_fashion: string;
        border_is_show: string;
        border_color: string;
        border_style: string;
        border_size: paddingStyle;
        box_shadow_color: string;
        box_shadow_x: number;
        box_shadow_y: number;
        box_shadow_blur: number;
        box_shadow_spread: number;
        common_style: object;
    };
}

const defaultShop: DefaultShop = {
    content: {
        content_top: {
            ...commonTop,
        },
        theme: '0',
        carousel_col: 1,
        shop_desc: '1',
        shop_desc_row: '1',
        data_type: '1',
        data_ids: '',
        data_list: [],
        data_auto_list: [],
        category_ids: [],
        number: 4,
        keywords: '',
        is_goods_list: '0',
        is_right_show: '1',
        right_type: 'img-icon',
        right_img: [],
        right_icon: 'arrow-right',
        right_text: '',
    },
    style: {
        shop_color_list: [{ color: '#fff', color_percentage: undefined }],
        shop_direction: '90deg',
        shop_background_img_style: '2',
        shop_background_img: [],
        shop_img_radius: {
            radius: 4,
            radius_top_left: 4,
            radius_top_right: 4,
            radius_bottom_left: 4,
            radius_bottom_right: 4,
        },
        shop_margin: {
            margin: 0,
            margin_top: 0,
            margin_bottom: 0,
            margin_left: 0,
            margin_right: 0,
        },
        shop_padding: {
            padding: 10,
            padding_top: 10,
            padding_bottom: 10,
            padding_left: 10,
            padding_right: 10,
        },
        shop_radius: {
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
        shop_title_img_width: 12,
        shop_title_img_height: 12,
        shop_title_img_radius: {
            radius: 0,
            radius_top_left: 0,
            radius_top_right: 0,
            radius_bottom_left: 0,
            radius_bottom_right: 0,
        },
        shop_title_img_inner_spacing: 5,
        shop_title_img_outer_spacing: 5,
        shop_title_color: '#333',
        shop_title_typeface: 'bold',
        shop_title_size: 14,
        shop_desc_color: '#666',
        shop_desc_typeface: '400',
        shop_desc_size: 12,
        right_style: {
            color_list: [{ color: '', color_percentage: undefined }],
            direction: '90deg',
            color: '#ccc',
            size: 12,
            img_width: 14,
            img_height: 14,
            margin: 0,
            margin_top: 0,
            margin_bottom: 0,
            margin_left: 0,
            margin_right: 0,
            padding: 0,
            padding_top: 0,
            padding_bottom: 0,
            padding_left: 0,
            padding_right: 0,
            radius: 0,
            radius_top_left: 0,
            radius_top_right: 0,
            radius_bottom_left: 0,
            radius_bottom_right: 0,
        },
        is_roll: '1',
        interval_time: 3,
        rolling_fashion: 'translation',
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
        box_shadow_color: '',
        box_shadow_x: 0,
        box_shadow_y: 0,
        box_shadow_blur: 0,
        box_shadow_spread: 0,
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

export default defaultShop;
