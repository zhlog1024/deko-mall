<template>
    <!-- 视频列表使用快照渲染；URL 缺省时跳转视频详情页。 -->
    <view class="oh" :style="style_container">
        <view class="oh" :style="style_img_container">
            <view class="pr oh" :style="style">
                <view v-if="!['4'].includes(plugins_video_theme)" class="flex-wrap" :class="plugins_video_theme_class" :style="plugins_video_theme !== '3' ? plugins_video_spacing : ''">
                    <view v-for="(item, index) in data_list" :key="index" class="item oh" :style="plugins_video_style" :data-value="item.data.url" @tap="url_event">
                        <view :class="plugins_video_theme == '0' ? 'flex-row oh' : 'flex-col oh h'" :style="plugins_video_img_style">
                            <template v-if="plugins_video_theme !== '3'">
                                <view class="oh pr flex-row">
                                    <template v-if="item.new_cover.length > 0">
                                        <image :src="item.new_cover[0].url" class="img" :style="img_radius + img_size" mode="aspectFill" />
                                    </template>
                                    <template v-else>
                                        <image :src="item.data.cover" class="img" :style="img_radius + img_size" mode="aspectFill" />
                                    </template>
                                </view>
                            </template>
                            <view v-if="field_show.includes('0') || field_show.includes('1') || field_show.includes('2') || field_show.includes('3')" class="jc-sb flex-1" :class="plugins_video_theme == '3' ? 'flex-row align-c' : 'flex-col'" :style="plugins_video_theme !== '0' ? content_padding : ''">
                                <view class="flex-col" :class="plugins_video_theme == '3' ? 'flex-1 flex-width' : ''">
                                    <text v-if="field_show.includes('3')" class="title" :class="plugins_video_theme == '3' ? 'text-line-1' : 'text-line-2'" :style="plugins_video_name">{{ item.new_title ? item.new_title : item.data.title }}</text>
                                </view>
                                <view class="flex-row jc-sb gap-8" :class="plugins_video_theme == '3' ? 'margin-left' : 'align-e margin-top'">
                                    <text :style="plugins_video_date">{{ field_show.includes('0') ? item.data.add_time : '' }}</text>
                                    <view v-show="field_show.includes('1')" class="flex-row align-c gap-3" :style="plugins_video_page_view">
                                        <iconfont name="icon-eye" propContainerDisplay="flex" size="24rpx"></iconfont>
                                        <view>
                                            {{ item.data.access_count ? item.data.access_count : '' }}
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
                <view v-else class="oh" :class="plugins_video_theme_class">
                    <swiper class="swiper" circular :autoplay="is_roll ? true : false" :interval="interval_time" :next-margin="next_margin" :display-multiple-items="slides_per_group" :style="'height:' + carousel_height_computer">
                        <swiper-item v-for="(item1, index1) in plugins_video_carousel_list" :key="index1">
                            <view class="flex-row ht-auto" :style="plugins_video_spacing">
                                <view v-for="(item, index) in item1.split_list" :key="index" class="item oh ht-auto" :style="plugins_video_style" :data-value="item.data.url" @tap="url_event">
                                    <template v-if="!isEmpty(item)">
                                        <view class="oh flex-col ht-auto" :style="plugins_video_img_style">
                                            <view class="oh pr wh-auto ht-auto flex-row">
                                                <template v-if="item.new_cover.length > 0">
                                                    <image :src="item.new_cover[0].url" class="img" :style="img_radius + 'height:100%;'" mode="aspectFill" />
                                                </template>
                                                <template v-else>
                                                    <image :src="item.data.cover" class="img" :style="img_radius + 'height:100%;'" mode="aspectFill" />
                                                </template>
                                                <template v-if="field_show.includes('3') && name_float == '1'">
                                                    <text class="text-line-1" :style="plugins_video_name + float_name_style">{{ item.new_title ? item.new_title : item.data.title }}</text>
                                                </template>
                                            </view>
                                            <view v-if="field_show.includes('0') || field_show.includes('1') || field_show.includes('2') || (field_show.includes('3') && name_float == '0')" class="jc-sb flex-1 flex-col" :style="plugins_video_theme !== '0' ? content_padding : ''">
                                                <view class="flex-col">
                                                    <text v-if="field_show.includes('3') && name_float == '0'" class="title text-line-2" :style="plugins_video_name + plugins_video_name_height_computer">{{ item.new_title ? item.new_title : item.data.title }}</text>
                                                </view>
                                                <view :class="'flex-row jc-sb gap-8 align-e' + ((field_show.includes('3') && name_float == '0') || field_show.includes('2') ? ' margin-top' : '')">
                                                    <text :style="plugins_video_date">{{ field_show.includes('0') ? item.data.add_time : '' }}</text>
                                                    <view v-show="field_show.includes('1')" class="flex-row align-c gap-3" :style="plugins_video_page_view">
                                                        <iconfont name="icon-eye" propContainerDisplay="flex" size="24rpx"></iconfont>
                                                        <view>
                                                            {{ item.data.access_count ? item.data.access_count : '' }}
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
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    const app = getApp();
    import { isEmpty, common_styles_computer, common_img_computer, padding_computer, radius_computer, get_math, gradient_handle, background_computer, gradient_computer, margin_computer, box_shadow_computer, border_computer, old_margin, get_swiper_list, time_stamp } from '@/common/js/common/common.js';
    var system = app.globalData.get_system_info(null, null, true);
    var sys_width = app.globalData.window_width_handle(system.windowWidth);
    export default {
        props: {
            propValue: {
                type: Object,
                default: () => {},
            },
            // 是否使用公共样式
            propIsCommonStyle: {
                type: Boolean,
                default: true,
            },
            // 关键key
            propKey: {
                type: [String, Number],
                default: '',
            },
            // 组件渲染的下标
            propIndex: {
                type: Number,
                default: 1000000,
            },
        },
        data() {
            return {
                style_container: '',
                style_img_container: '',
                style: '',
                // 数据
                data_list: [],
                // 风格
                plugins_video_theme: '0',
                // 是否显示
                field_show: ['0', '1', '3'],
                // 视频标题
                plugins_video_name: '',
                // 日期
                plugins_video_date: '',
                // 浏览量
                plugins_video_page_view: '',
                // 内容圆角
                content_radius: '',
                // 图片圆角
                img_radius: '',
                // 内间距
                content_padding: '',
                // 内容间距
                content_spacing: '',
                // 视频间距
                plugins_video_spacing: '',
                plugins_video_style: '',
                plugins_video_img_style: '',
                // 轮播图定时轮播
                interval_time: 3000,
                // 轮播图是否滚动
                is_roll: 1,
                plugins_video_theme_class: '',
                // 轮播高度
                carousel_height_computer: '',
                // 视频内容高度
                plugins_video_name_height_computer: '',
                // 视频名称浮动样式
                float_name_style: '',
                name_float: '0',
                // 图片大小
                img_size: '',
                // 视频轮播数据
                plugins_video_carousel_list: [],
                // 一行显示的数量
                slides_per_group: 1,
                next_margin: '0rpx',
            };
        },
        watch: {
            propKey(val) {
                // 初始化
                this.init();
            },
        },
        created() {
            this.init();
        },
        methods: {
            isEmpty,
            // 初始化数据
            init() {
                const new_content = this.propValue.content || {};
                const new_style = this.propValue.style || {};
                // 视频风格（兼容历史/后续编辑器 key：theme / plugins_video_theme）
                const theme = !isEmpty(new_content.theme) ? new_content.theme : (!isEmpty(new_content.plugins_video_theme) ? new_content.plugins_video_theme : '0');
                // 快照渲染：指定(data_type 0=data_list) / 筛选(data_type 1=data_auto_list)，缺省互备
                const new_list = this.get_video_render_list(new_content);
                const spacing = new_style.plugins_video_spacing || 0;
                const rolling_fashion = new_style.rolling_fashion || '';
                this.setData({
                    name_float: !isEmpty(new_content.name_float) ? new_content.name_float : '0',
                    data_list: new_list,
                    plugins_video_theme_class: this.plugins_video_theme_class_computer(theme),
                    plugins_video_theme: theme,
                    field_show: this.field_show_handle(new_content.field_show != null ? new_content.field_show : new_content.is_show),
                    // 样式
                    plugins_video_name: 'font-size:' + (new_style.name_size || 14) * 2 + 'rpx;' + 'font-weight:' + (new_style.name_weight || 'bold') + ';' + 'color:' + (new_style.name_color || '#333') + ';',
                    plugins_video_date: 'font-size:' + (new_style.time_size || 12) * 2 + 'rpx;' + 'font-weight:' + (new_style.time_weight || '400') + ';' + 'color:' + (new_style.time_color || 'rgba(153, 153, 153, 1)') + ';',
                    plugins_video_page_view: 'font-size:' + (new_style.page_view_size || 12) * 2 + 'rpx;' + 'font-weight:' + (new_style.page_view_weight || '400') + ';' + 'color:' + (new_style.page_view_color || 'rgba(153, 153, 153, 1)') + ';',
                    content_radius: radius_computer(new_style.content_radius),
                    img_radius: radius_computer(new_style.img_radius),
                    // 内间距
                    content_padding: padding_computer(new_style.padding),
                    // 内容间距
                    content_spacing: `gap: ${ new_style.content_spacing || 0 }px;`,
                    // 视频间距
                    plugins_video_spacing: `gap: ${ spacing }px;`,
                    // 平移轮播：负边距露出下一屏（px 转 rpx 按设计宽 375 翻倍）
                    next_margin: rolling_fashion == 'translation' ? '-' + (spacing * 2) + 'rpx' : '0rpx',
                    // 视频内容高度
                    slides_per_group: rolling_fashion == 'translation' ? Number(new_content.carousel_col || 1) + 1 : 1,
                });
                // 默认数据
                const product_style_list = [
                    { name: '单列展示', value: '0', width:110, height: 83 },
                    { name: '两列展示（纵向）', value: '1', width:180, height: 180 },
                    { name: '大图展示', value: '2', width:0, height: 180 },
                    { name: '无图模式', value: '3', width:0, height: 0 },
                    { name: '左右滑动展示', value: '4', width:0, height: 0 },
                ];
                const scale = sys_width / 390;
                let img_style = ``;
                if (['0'].includes(theme)) {
                    // 图片宽度
                    if (typeof new_style.content_img_width == 'number') {
                        img_style += `width: ${ new_style.content_img_width * scale }px;`;
                    } else {
                        const list = product_style_list.filter(item => item.value == theme);
                        if (list.length > 0) {
                            img_style += `width: ${ list[0].width * scale }px;`;
                        } else {
                            img_style += 'width: auto;';
                        }
                    }
                }
                if (!['3', '4'].includes(theme)) {
                    // 图片高度
                    if (typeof new_style.content_img_height == 'number') {
                        img_style += `height: ${ new_style.content_img_height * scale }px;`;
                    } else {
                        const list = product_style_list.filter(item => item.value == theme);
                        if (list.length > 0) {
                            img_style += `height: ${ list[0].height * scale }px;`;
                        } else {
                            img_style += 'height: auto;';
                        }
                    }
                }
                // 背景图的处理
                const plugins_video_data = {
                    background_img_style: (new_style.plugins_video_background_img_style || null) != null ? new_style.plugins_video_background_img_style : '',
                    background_img: (new_style.plugins_video_background_img || null) != null ? new_style.plugins_video_background_img : '',
                }
                // 间距配置兼容（style.value.margin(源) / style.margin(mall DIY) / old_margin）
                const plugins_video_margin = ((new_style.value || null) != null && (new_style.value.margin || null) != null) ? new_style.value.margin : ((new_style.margin || null) != null ? new_style.margin : old_margin);
                const margin_width = (plugins_video_margin.margin_left || 0) + (plugins_video_margin.margin_right || 0);
                // 渐变效果
                const all_style = gradient_handle(new_style.plugins_video_color_list || [], new_style.plugins_video_direction || '') + margin_computer(plugins_video_margin) + box_shadow_computer(new_style) + border_computer(new_style);
                // 视频样式
                if (theme == '0') {
                    this.setData({
                        style: '',
                        img_size: img_style,
                        plugins_video_style: this.content_radius + all_style,
                        plugins_video_img_style: this.content_spacing + this.content_padding + background_computer(plugins_video_data)
                    });
                } else if (theme == '1') {
                    this.setData({
                        style: '',
                        img_size: img_style,
                        plugins_video_style: `width: calc(50% - ${(spacing + (margin_width * 2)) / 2}px);` + this.content_radius + all_style,
                        plugins_video_img_style: background_computer(plugins_video_data)
                    });
                } else if (theme == '2') {
                    this.setData({
                        style: '',
                        img_size: img_style,
                        plugins_video_style: this.content_radius + all_style,
                        plugins_video_img_style: background_computer(plugins_video_data)
                    });
                } else if (theme == '3') {
                    this.setData({
                        style: `background:#fff;` + this.content_radius,
                    });
                } else if (theme == '4') {
                    const temp_carousel_col = new_content.carousel_col || '1';
                    const carousel_num = Number(temp_carousel_col) || 1;
                    // 计算间隔的空间。(gap * gap数量) / 模块数量
                    let gap = temp_carousel_col !== '0' ? ((spacing * carousel_num - 1) + (margin_width * carousel_num)) / carousel_num : '0';
                    const multicolumn_columns_width = rolling_fashion == 'translation' ? `margin-right: ${ spacing + (plugins_video_margin.margin_right || 0) }px;width:100%;` : `width:calc(${100 / (carousel_num + 1)}% - ${gap * 2}rpx);min-width:calc(${100 / (carousel_num + 1)}% - ${gap * 2}rpx);`;
                    const { name_bg_color_list = [], name_bg_direction = '180deg', name_bg_radius, name_bg_padding, name_bg_margin } = new_style;
                    const data = {
                        color_list: name_bg_color_list,
                        direction: name_bg_direction,
                    }
                    let location = 'position:absolute;bottom:0;left:0;right:0;'
                    // 每页显示的数量
                    const num = carousel_num + 1;
                    // 轮播宽度
                    this.setData({
                        style: '',
                        // 滚动时间
                        interval_time: (new_style.interval_time || 3) * 1000,
                        // 是否滚动修改
                        is_roll: new_style.is_roll,
                        plugins_video_style: this.content_radius + all_style + multicolumn_columns_width,
                        // 轮播高度
                        carousel_height_computer: (new_style.plugins_video_height || 155) * scale + 'px',
                        // 视频内容高度
                        plugins_video_name_height_computer: `height:${(new_style.name_size || 14) * 2.4 * 2}rpx;line-height:${(new_style.name_size || 14) * 1.2 * 2}rpx;`,
                        float_name_style: gradient_computer(data) + (!isEmpty(name_bg_radius) ? radius_computer(name_bg_radius) : '') + (!isEmpty(name_bg_padding) ? padding_computer(name_bg_padding) : '' ) + (!isEmpty(name_bg_padding) ? margin_computer(name_bg_margin) : '') + location,
                        plugins_video_img_style: background_computer(plugins_video_data),
                        plugins_video_carousel_list: get_swiper_list(this.data_list, num, rolling_fashion),
                    });
                }

                if (this.propIsCommonStyle) {
                    this.setData({
                        style_container: common_styles_computer(new_style.common_style),
                        style_img_container: common_img_computer(new_style.common_style, this.propIndex),
                    });
                }
            },
            // 显示字段解析（编辑器 field_show，兼容 is_show；缺省时间+浏览量+标题）
            field_show_handle(raw) {
                if (raw instanceof Array && raw.length > 0) {
                    var list = [];
                    for (var i = 0; i < raw.length; i++) {
                        list.push(String(raw[i]));
                    }
                    return list;
                }
                return ['0', '1', '3'];
            },
            // 将视频快照转换为渲染字段，不额外发起请求。
            get_video_render_list(new_content) {
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
                        var item = this.video_item_handle(source_list[i]);
                        if (item != null) {
                            data_list.push(item);
                        }
                    }
                }
                return data_list;
            },
            // 单条视频快照字段适配（兼容 {data:{...}} 包装 / 扁平 mall DTO 两种快照结构）
            video_item_handle(item) {
                if (isEmpty(item)) {
                    return null;
                }
                var item_data = (item.data || null) != null ? item.data : item;
                var video_id = (item_data.id || null) != null ? item_data.id : (item.id || null);
                // 标题：编辑器可改标题(new_title) 优先
                var video_title = !isEmpty(item.new_title) ? item.new_title : (!isEmpty(item_data.title) ? item_data.title : '');
                // 封面：编辑器可替换封面(new_cover) 优先，缺省 data.cover
                var new_cover = this.cover_list_handle(item.new_cover);
                // 时间：快照 add_time_date 优先(兼容 addTimeDate / 老快照 add_time)，缺省 createTime 转换
                var add_time = !isEmpty(item_data.add_time_date) ? item_data.add_time_date : (!isEmpty(item_data.addTimeDate) ? item_data.addTimeDate : (!isEmpty(item_data.add_time) ? item_data.add_time : time_stamp(item_data.createTime)));
                // 浏览量：源 access_count / mall accessCount
                var access_count = item_data.access_count != null && item_data.access_count !== '' ? item_data.access_count : (item_data.accessCount != null ? item_data.accessCount : 0);
                // 跳转链接：快照 url 优先，缺省拼视频详情页
                var video_url = !isEmpty(item_data.url) ? item_data.url : (!isEmpty(item_data.jumpUrl) ? item_data.jumpUrl : (video_id != null ? '/pages/plugins/video/detail/detail?id=' + video_id : ''));
                // 内部页面链接归一（编辑器快照可能保存无前导 / 的相对路径）
                if (video_url != '' && video_url.indexOf('http') != 0 && video_url.indexOf('/') != 0) {
                    video_url = '/' + video_url;
                }
                return {
                    id: video_id != null ? video_id : get_math(),
                    new_title: !isEmpty(item.new_title) ? item.new_title : '',
                    new_cover: new_cover,
                    data: {
                        id: video_id,
                        title: video_title,
                        cover: new_cover.length > 0 ? new_cover[0].url : (item_data.cover || ''),
                        add_time: add_time,
                        access_count: access_count,
                        url: video_url,
                    },
                };
            },
            // 编辑器封面列表归一（兼容 [{url}] / [url 字符串] / []）
            cover_list_handle(new_cover) {
                if (isEmpty(new_cover) || !(new_cover instanceof Array)) {
                    return [];
                }
                var list = [];
                for (var i = 0; i < new_cover.length; i++) {
                    var row = new_cover[i];
                    if (!isEmpty(row)) {
                        if (typeof row == 'string') {
                            if (row != '') {
                                list.push({ url: row });
                            }
                        } else if (!isEmpty(row.url)) {
                            list.push({ url: row.url });
                        }
                    }
                }
                return list;
            },
            // 视频主题样式
            plugins_video_theme_class_computer(theme) {
                switch (theme) {
                    case '0':
                        return 'style1 flex-col';
                    case '1':
                        return 'style2 flex-row flex-wrap';
                    case '2':
                        return 'style3 flex-col';
                    case '3':
                        return 'style4 flex-col';
                    default:
                        return 'style5';
                }
            },
            // 跳转链接
            url_event(e) {
                app.globalData.url_event(e);
            },
        },
    };
</script>
<style lang="scss" scoped>
    .h {
        height: 100%;
    }
    .style1 {
        .item {
            max-width: 100%;
        }
    }
    .style2 {
        .item {
            .img {
                width: 100%;
            }
        }
    }
    .style3 {
        .item {
            width: 100%;
            .img {
                width: 100%;
            }
        }
    }
    .style4 {
        .item {
            width: 100%;
            &:not(:last-child) {
                border-bottom: 2rpx solid #eee;
            }
        }
    }
    .style5 {
        .item {
            width: 100%;
            .img {
                width: 100%;
            }
        }
    }
</style>
