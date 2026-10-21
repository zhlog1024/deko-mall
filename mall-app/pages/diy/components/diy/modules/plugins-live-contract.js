/* 统一直播装修组件的数据结构：兼容显示字段和分类名称，并补齐选项卡样式默认值。 */

// 显示字段默认值（0-标题 1-主播 2-观看数 3-直播状态）
const LIVE_FIELD_SHOW_DEFAULT = ['0', '1', '2', '3'];

/**
 * 空值判断（模块内自持，避免依赖 common.js，便于独立校验）
 */
function isEmptyValue(value) {
    return value === undefined || value === null || value === '';
}

/**
 * 解析显示字段：优先 field_show，其次 product_show_list；均为空时显示全部字段。
 *
 * @param {Object} content 组件配置
 * @returns {String[]} 显示字段标识
 */
export function resolve_live_field_show(content) {
    const source = content || {};
    const raw = !isEmptyValue(source.field_show) ? source.field_show : source.product_show_list;
    if (!Array.isArray(raw) || raw.length === 0) {
        return LIVE_FIELD_SHOW_DEFAULT.slice();
    }
    return raw.map((value) => String(value));
}

/**
 * 补齐选项卡与直播列表的样式默认值，避免缺值参与尺寸计算而产生 NaNrpx。
 * 默认值与装修器的 plugins-video-tabs、plugins-live 配置保持一致。
 *
 * @returns {Object} 样式默认值
 */
export function live_tabs_style_default() {
    return {
        // 分类栏（对齐 plugins-video-tabs.ts）
        tabs_size: 14,
        tabs_size_checked: 14,
        tabs_weight: 'bold',
        tabs_weight_checked: 'bold',
        // 选中态通过下划线渐变区分，文字颜色不变。
        tabs_color: 'rgba(51,51,51,1)',
        tabs_color_checked: 'rgba(51,51,51,1)',
        tabs_checked: [
            { color: '#FF2222', color_percentage: undefined },
            { color: '#FF9898', color_percentage: undefined },
        ],
        tabs_direction: '90deg',
        tabs_spacing: 20,
        tabs_sign_spacing: 4,
        tabs_icon_size: 14,
        tabs_icon_size_checked: 14,
        tabs_icon_color: 'rgba(51,51,51,1)',
        tabs_icon_color_checked: 'rgba(51,51,51,1)',
        tabs_img_height: 20,
        tabs_top_img_height: 39,
        tabs_adorn_icon_size: 15,
        tabs_adorn_img_height: 10,
        tabs_one_theme: '0',
        is_tabs_img_background: '0',
        is_tabs_adorn_img_background: '0',
        // 内嵌直播列表（对齐 plugins-live.ts，缺了会拼出 gap: undefinedpx）
        content_spacing: 10,
        is_roll: '0',
        interval_time: 3,
        rolling_fashion: 'translation',
    };
}

/**
 * 规范化分类列表：标题优先取 title，其次取 name，并补齐渲染所需字段。
 *
 * @param {Array} tabs_list 分类列表
 * @returns {Array} 规范化后的分类列表
 */
export function normalize_live_tabs_list(tabs_list) {
    const list = Array.isArray(tabs_list) ? tabs_list : [];
    return list.map((item) => {
        const row = item || {};
        return {
            ...row,
            title: !isEmptyValue(row.title) ? row.title : (row.name || ''),
            tabs_type: !isEmptyValue(row.tabs_type) ? row.tabs_type : '0',
            tabs_img: Array.isArray(row.tabs_img) ? row.tabs_img : [],
            tabs_icon: !isEmptyValue(row.tabs_icon) ? row.tabs_icon : '',
            desc: row.desc || '',
            is_sliding_fixed: !isEmptyValue(row.is_sliding_fixed) ? row.is_sliding_fixed : '0',
        };
    });
}

/**
 * 规范化直播选项卡配置（content + style 原地补齐）
 *
 * @param {Object} content 组件 content 配置
 * @param {Object} style   组件 style 配置
 * @returns {Object} { content, style }
 */
export function normalize_live_tabs(content, style) {
    const new_content = content || {};
    const new_style = style || {};

    // 1. 分类列表契约
    new_content.tabs_list = normalize_live_tabs_list(new_content.tabs_list);
    // 2. 选项卡风格（装修器暂无该配置项，缺省用风格1 = tabs-style-1）
    if (isEmptyValue(new_content.tabs_theme)) {
        new_content.tabs_theme = '0';
    }
    // 3. tabs 样式族 + 内嵌直播列表样式缺省值
    const tabs_style_default = live_tabs_style_default();
    Object.keys(tabs_style_default).forEach((key) => {
        if (isEmptyValue(new_style[key])) {
            new_style[key] = tabs_style_default[key];
        }
    });

    return { content: new_content, style: new_style };
}
