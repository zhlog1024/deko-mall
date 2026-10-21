<template>
    <view class="oh" :style="style_container">
        <view :style="style_img_container">
            <view :class="outer_class" :style="outer_style">
                <template v-if="!['2'].includes(theme)">
                    <view v-for="(item, index) in list" :key="index" class="pr oh" :style="layout_style" :data-value="item.url" @tap.stop="url_event">
                        <template v-if="theme == 0">
                            <view class="oh wh-auto ht-auto flex-col jc-sb gap-10" :style="layout_img_style">
                                <view class="flex-row gap-10 align-b">
                                    <view v-if="is_show('ranking')" :class="'top-style one' + (index + 1)">{{ index + 1 }}</view>
                                    <view :class="'flex-1' + title_class" :style="title_style">{{ item.title }}</view>
                                </view>
                                <view v-if="is_show('reply_status') || is_show('time') || is_show('page_view')" class="flex-row gap-10 align-c" :style="is_show('ranking') ? 'margin-left: 54rpx;' : ''">
                                    <view class="flex-row">
                                        <view v-if="is_show('reply_status')" :style="item.is_reply == 0 ? not_replied_yet_style : returned_style">
                                            <view :style="item.is_reply == 0 ? not_replied_yet_img_style : returned_img_style">
                                                {{ item.is_reply == 0 ? '未回' : '已回'}}
                                            </view>
                                        </view>
                                    </view>
                                    <span v-if="is_show('time')" :style="time_style">{{ item.add_time_date }}</span>
                                    <span v-if="is_show('page_view')" :style="page_view_style">共有{{ item.access_count }}浏览</span>
                                </view>
                            </view>
                        </template>
                        <template v-else>
                            <view class="oh flex-col gap-10 jc-sb" :style="layout_img_style">
                                <view class="flex-row gap-10 align-b">
                                    <view :class="title_class" :style="title_style">{{ item.title }}</view>
                                </view>
                                <view v-if="is_show('reply_status') || is_show('time')" class="flex-col gap-10">
                                    <span v-if="is_show('time')" :style="time_style">{{ item.add_time_date }}</span>
                                    <view class="flex-row">
                                        <view v-if="is_show('reply_status')" class="flex-row" :style="item.is_reply == 0 ? not_replied_yet_style : returned_style">
                                            <view :style="item.is_reply == 0 ? not_replied_yet_img_style : returned_img_style">
                                                {{ item.is_reply == 0 ? '未回' : '已回'}}
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </template>
                    </view>
                </template>
                <template v-else>
                    <swiper circular="true" :autoplay="new_style.is_roll == '1'" :interval="new_style.interval_time * 1000" :duration="500" :next-margin="new_style.rolling_fashion == 'translation' ? '-' + content_outer_spacing_magin : '0rpx'" :display-multiple-items="slides_per_group" :style="{ width: '100%', height: new_style.content_outer_height * new_scale + 'px' }">
                        <swiper-item v-for="(item1, index1) in ask_content_list" :key="index1">
                            <view class="flex-row wh-auto ht-auto" :style="outer_style">
                                <view v-for="(item, index) in item1.split_list" :key="index" class="pr oh" :style="layout_style" :data-value="item.url" @tap.stop="url_event">
                                    <template v-if="!isEmpty(item)">
                                        <view class="oh ht-auto flex-col gap-10 jc-sb" :style="layout_img_style">
                                            <view class="flex-row gap-10 align-b">
                                                <view :class="title_class" :style="title_style">{{ item.title }}</view>
                                            </view>
                                            <view v-if="is_show('reply_status') || is_show('time')" class="flex-col gap-10">
                                                <span v-if="is_show('time')" :style="time_style">{{ item.add_time_date }}</span>
                                                <view class="flex-row">
                                                    <view v-if="is_show('reply_status')" class="flex-row" :style="item.is_reply == 0 ? not_replied_yet_style : returned_style">
                                                        <view :style="item.is_reply == 0 ? not_replied_yet_img_style : returned_img_style">
                                                            {{ item.is_reply == 0 ? '未回' : '已回'}}
                                                        </view>
                                                    </view>
                                                </view>
                                            </view>
                                        </view>
                                    </template>
                                </view>
                            </view>
                        </swiper-item>
                    </swiper>
                </template>
            </view>
        </view>
    </view>
</template>

<script>
    const app = getApp();
    import { isEmpty, common_styles_computer, common_img_computer, gradient_handle, padding_computer, radius_computer, background_computer, border_computer, box_shadow_computer, gradient_computer, margin_computer, get_swiper_list, get_math, time_stamp } from '@/common/js/common/common.js';
    var system = app.globalData.get_system_info(null, null, true);
    var sys_width = app.globalData.window_width_handle(system.windowWidth);
    export default {
        props: {
            propValue: {
                type: Object,
                default: () => ({}),
            },
            propKey: {
                type: [String, Number],
                default: '',
            },
            // 组件渲染的下标
            propIndex: {
                type: Number,
                default: 0,
            },
            // 是否使用公共样式
            propIsCommonStyle: {
                type: Boolean,
                default: true,
            },
        },
        data() {
            return {
                form: {},
                new_style: {},
                list: [],
                theme: '', // 选择的风格
                content_outer_spacing: '', // 商品间距
                content_outer_spacing_magin: '', // 商品间距
                // 最外层不同风格下的显示
                outer_class: '',
                outer_style: '',
                // 不同风格下的样式
                layout_style: '',
                layout_img_style: '',
                style_container: '', // 公共样式
                style_img_container: '',
                ask_content_list: [],
                slides_per_group: 1,
                // 内容样式
                title_class: '',
                title_style: '',
                time_style: '',
                page_view_style: '',
                // 图片大小
                img_size: '',
                new_scale: 1,
                // 已回未回信息
                not_replied_yet_style: '',
                not_replied_yet_img_style: '',
                returned_style: '',
                returned_img_style: '',
            };
        },
        watch: {
            propKey(val) {
                this.init();
            },
            propValue(new_value, old_value) {
                this.init();
            },
        },
        created() {
            this.init();
        },
        methods: {
            isEmpty,
            init() {
                const new_content = this.propValue.content || null;
                const new_style = this.propValue.style || null;
                if (new_content != null && new_style != null) {
                    // 问答风格（兼容历史/后续编辑器 key 差异）
                    const theme = !isEmpty(new_content.theme) ? new_content.theme : (!isEmpty(new_content.ask_theme) ? new_content.ask_theme : '0');
                    // 快照渲染：指定(data_type 0=data_list) / 筛选(data_type 1=data_auto_list)，缺省互备
                    const new_list = this.get_ask_render_list(new_content);
                    const scale = sys_width / 390;
                    // 最外层不同风格下的显示
                    const flex = ['0', '2'].includes(theme) ? 'flex-col ' : 'container ';
                    const wrap = theme == '2' ? '' : 'flex-wrap ';
                    const content_outer_spacing = new_style.content_outer_spacing || 0;
                    this.setData({
                        form: new_content,
                        new_style: new_style,
                        theme: theme,
                        outer_class: flex + wrap + 'oh',
                        outer_style: ['0', '2'].includes(theme) ? `gap: ${ content_outer_spacing * 2 + 'rpx' };` : `column-gap: ${ content_outer_spacing * 2 + 'rpx' }`,
                        content_outer_spacing_magin: content_outer_spacing * 2 + 'rpx',
                        list: new_list,
                        new_scale: scale,
                        title_class: new_content.title_display_method == '0' ? '' : 'text-line-' + new_content.title_display_method,
                        title_style: this.trends_config('title', new_style) + 'word-break: break-all;',
                        time_style: this.trends_config('time', new_style),
                        page_view_style: this.trends_config('page_view', new_style),
                        ask_content_list: get_swiper_list(new_list, new_content.carousel_col || 1, new_style.rolling_fashion),
                        not_replied_yet_style: this.button_style(new_style.not_replied_yet_style),
                        not_replied_yet_img_style: this.button_img_style(new_style.not_replied_yet_style),
                        returned_style: this.button_style(new_style.returned_style),
                        returned_img_style: this.button_img_style(new_style.returned_style),
                        img_size: '',
                        layout_style: this.get_layout_style(new_content, new_style, theme),
                        layout_img_style: this.get_layout_img_style(new_content, new_style),
                        slides_per_group: new_style.rolling_fashion == 'translation' ? (new_content.carousel_col || 1) : 1,
                        style_container: this.propIsCommonStyle ? common_styles_computer(new_style.common_style) : '', // 公共样式
                        style_img_container: this.propIsCommonStyle ? common_img_computer(new_style.common_style, this.propIndex) : '', // 图片样式
                    });
                }
            },
            // 使用 DIY 问答快照渲染，不额外发起请求。
            get_ask_render_list(new_content) {
                var data_list = [];
                var source_list = [];
                if (new_content.data_type == '1') {
                    // 筛选：优先 data_auto_list，缺省回退 data_list
                    source_list = !isEmpty(new_content.data_auto_list) ? new_content.data_auto_list : new_content.data_list;
                } else {
                    // 指定：优先 data_list，缺省回退 data_auto_list
                    source_list = !isEmpty(new_content.data_list) ? new_content.data_list : new_content.data_auto_list;
                }
                if (!isEmpty(source_list)) {
                    for (var i = 0; i < source_list.length; i++) {
                        var item = this.ask_item_handle(source_list[i]);
                        if (item != null) {
                            data_list.push(item);
                        }
                    }
                }
                return data_list;
            },
            // 单条问答快照字段适配（兼容 {data:{...}} 包装 / 扁平 mall DTO 两种快照结构）
            ask_item_handle(item) {
                if (isEmpty(item)) {
                    return null;
                }
                var item_data = (item.data || null) != null ? item.data : item;
                var ask_id = (item_data.id || null) != null ? item_data.id : (item.id || null);
                // 标题：编辑器可改标题(new_title) 优先
                var ask_title = !isEmpty(item.new_title) ? item.new_title : (!isEmpty(item_data.title) ? item_data.title : '');
                // 未回/已回：兼容 is_reply(源) / isReply(mall)
                var is_reply = item_data.is_reply != null ? item_data.is_reply : (item_data.isReply != null ? item_data.isReply : 0);
                // 时间：快照 add_time_date 优先，缺省由 createTime 转换
                var add_time_date = !isEmpty(item_data.add_time_date) ? item_data.add_time_date : (!isEmpty(item_data.addTimeDate) ? item_data.addTimeDate : time_stamp(item_data.createTime));
                // 浏览量：源 access_count / mall accessCount
                var access_count = item_data.access_count != null && item_data.access_count !== '' ? item_data.access_count : (item_data.accessCount != null ? item_data.accessCount : 0);
                // 跳转链接：快照 url 优先，缺省拼问答详情页
                var ask_url = !isEmpty(item_data.url) ? item_data.url : (!isEmpty(item_data.jumpUrl) ? item_data.jumpUrl : (ask_id != null ? '/pages/plugins/ask/detail/detail?id=' + ask_id : ''));
                // 内部页面链接归一（编辑器快照可能保存无前导 / 的相对路径）
                if (ask_url != '' && ask_url.indexOf('http') != 0 && ask_url.indexOf('/') != 0) {
                    ask_url = '/' + ask_url;
                }
                return {
                    id: ask_id != null ? ask_id : get_math(),
                    title: ask_title,
                    is_reply: Number(is_reply) || 0,
                    add_time_date: add_time_date,
                    access_count: access_count,
                    url: ask_url,
                };
            },
            button_style(style) {
                return `${ gradient_computer(style)}; ${ border_computer(style) } ${ radius_computer(style.font_radius)}`;
            },
            button_img_style(style) {
                return `${ padding_computer(style.font_padding) } ${ background_computer(style) }font-weight:${style.font_typeface}; font-size: ${style.font_size}px;color: ${style.font_color};`;
            },
            // 根据传递的参数，从对象中取值
            trends_config(key, new_style) {
                return this.style_config(new_style[`ask_${key}_typeface`], new_style[`ask_${key}_size`], new_style[`ask_${key}_color`]);
            },
            // 根据传递的值，显示不同的内容
            style_config(typeface, size, color) {
                return `font-weight:${typeface}; font-size: ${size}px;color: ${color};line-height: 1.5;`;
            },
            // 容器样式
            get_layout_style(form, new_style, theme) {
                const { ask_margin, ask_radius, ask_color_list, ask_direction } = new_style;
                const radius = radius_computer(ask_radius);
                const margin = theme == '1' ? {
                    ...ask_margin,
                    margin_bottom: ((new_style.ask_margin && new_style.ask_margin.margin_bottom) || 0) + (new_style.content_outer_spacing || 0),
                } : ask_margin;
                // 计算每个容器的大小
                let size_style = ``;
                const ask_left_right_width = (ask_margin.margin_left || 0) + (ask_margin.margin_right || 0);
                if (theme == '2') {
                    // 如果不是平移的时候执行
                    if (new_style.rolling_fashion != 'translation') {
                        size_style = `width: ${this.get_multicolumn_columns_width(new_style, form)};min-width: ${this.get_multicolumn_columns_width(new_style, form)};height: ${new_style.content_outer_height * (sys_width / 390) + 'px'};`;
                    } else {
                        size_style = `margin-right: ${ ((new_style.content_outer_spacing || 0) * 2) + ((ask_margin.margin_right || 0) * 2) }rpx;width: 100%;height: 100%;`;
                    }
                } else if (theme == '0') {
                    size_style = `width: calc(100% - ${ ask_left_right_width * 2 }rpx);`;
                }
                const gradient = gradient_handle(ask_color_list, ask_direction) + margin_computer(margin) + border_computer(new_style) + box_shadow_computer(new_style);

                return `${radius} ${ gradient } ${ size_style }`;
            },
            get_multicolumn_columns_width(new_style, form) {
                const carousel_col = parseInt(form.carousel_col) || 1;
                // 计算间隔的空间。(gap * gap数量) / 模块数量
                let gap = ((new_style.content_outer_spacing || 0) * (carousel_col - 1)) / carousel_col;
                return `calc(${100 / carousel_col}% - ${gap * 2}rpx)`;
            },
            is_show(val) {
                return (this.form.is_show || []).includes(val);
            },
            // 容器图片样式
            get_layout_img_style(form, new_style) {
                const { ask_padding, ask_background_img_style, ask_background_img } = new_style;
                const padding = padding_computer(ask_padding);
                const data = {
                    background_img_style: ask_background_img_style,
                    background_img: ask_background_img,
                }
                return padding + background_computer(data);
            },
            url_event(e) {
                app.globalData.url_event(e);
            },
        },
    };
</script>

<style scoped lang="scss">
.one1 {
    color: #fff !important;
    background: #FF6565 !important;
}
.one2 {
    color: #fff !important;
    background: #FF9F2F !important;
}
.one3 {
    color: #fff !important;
    background: #FFC889 !important;
}
.top-style {
    color: #EBAB2A;
    background: #fff;
    padding: 0 10rpx;
    width: 34rpx;
    height: 34rpx;
    font-weight: 400;
    font-size: 24rpx;
    line-height: 34rpx;
    text-align: right;
    font-style: normal;
    border-radius: 8rpx;
    text-align: center;
    box-sizing: border-box;
}
.container {
    column-count: 2;
}
.gap-10 {
    gap: 20rpx;
}
</style>
