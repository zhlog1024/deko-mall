<template>
    <!-- 文章列表 -->
    <view class="oh" :style="style_container">
        <view class="oh" :style="style_img_container">
            <view class="pr oh" :style="style">
                <view v-if="!['4'].includes(article_theme)" class="flex-wrap" :class="article_theme_class" :style="article_theme !== '3' ? article_spacing : ''">
                    <view v-for="(item, index) in data_list" :key="index" class="item oh" :style="article_style" :data-value="item.data.url" @tap="url_event">
                        <view :class="article_theme == '0' ? 'flex-row oh' : 'flex-col oh ht-auto'" :style="article_img_style">
                            <template v-if="article_theme !== '3'">
                                <view class="oh pr flex-row">
                                    <template v-if="item.new_cover.length > 0">
                                        <image :src="item.new_cover[0].url" class="img" :style="img_radius + img_size" mode="aspectFill" />
                                    </template>
                                    <template v-else>
                                        <image :src="item.data.cover" class="img" :style="img_radius + img_size" mode="aspectFill" />
                                    </template>
                                    <!-- 角标 -->
                                    <subscriptIndex :propValue="propValue"></subscriptIndex>
                                </view>
                            </template>
                            <view v-if="field_show.includes('0') || field_show.includes('1') || field_show.includes('2') || field_show.includes('3')" class="jc-sb flex-1" :class="article_theme == '3' ? 'flex-row align-c' : 'flex-col'" :style="article_theme !== '0' ? content_padding : ''">
                                <view class="flex-col" :class="article_theme == '3' ? 'flex-1 flex-width' : ''" :style="'gap:' + name_desc_space + 'px;'">
                                    <view v-if="field_show.includes('3')" class="title" :class="article_theme == '3' ? 'text-line-1' : 'text-line-2'" :style="article_name">{{ item.new_title ? item.new_title : item.data.title }}</view>
                                    <view v-if="field_show.includes('2')" :class="'desc ' + field_desc_row == '2' ? 'text-line-2' : 'text-line-1'" :style="article_desc">{{ item.data.describe || '' }}</view>
                                </view>
                                <view class="flex-row jc-sb gap-8" :class="article_theme == '3' ? 'margin-left' : 'align-e margin-top'">
                                    <view :style="article_date">{{ field_show.includes('0') ? item.data.add_time : '' }}</view>
                                    <view v-show="field_show.includes('1')" class="flex-row align-c gap-3" :style="article_page_view">
                                        <iconfont name="icon-eye" propContainerDisplay="flex"></iconfont>
                                        <view>
                                            {{ item.data.access_count ? item.data.access_count : '' }}
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
                <view v-else class="oh" :class="article_theme_class">
                    <swiper class="swiper" circular :autoplay="is_roll ? true : false" :interval="interval_time" :next-margin="next_margin" :display-multiple-items="slides_per_group" :style="'height:' + carousel_height_computer">
                        <swiper-item v-for="(item1, index1) in article_carousel_list" :key="index1">
                            <view class="flex-row ht-auto" :style="article_spacing">
                                <view v-for="(item, index) in item1.split_list" :key="index" class="item oh ht-auto" :style="article_style" :data-value="item.data.url" @tap="url_event">
                                    <template v-if="!isEmpty(item)">
                                        <view class="oh flex-col ht-auto" :style="article_img_style">
                                            <view class="oh pr wh-auto ht-auto flex-row">
                                                <template v-if="item.new_cover.length > 0">
                                                    <image :src="item.new_cover[0].url" class="img" :style="img_radius + 'height:100%;'" mode="aspectFill" />
                                                </template>
                                                <template v-else>
                                                    <image :src="item.data.cover" class="img" :style="img_radius + 'height:100%;'" mode="aspectFill" />
                                                </template>
                                                <template v-if="field_show.includes('3') && name_float == '1'">
                                                    <view class="text-line-1" :style="article_name + float_name_style">{{ item.new_title ? item.new_title : item.data.title }}</view>
                                                </template>
                                                <!-- 角标 -->
                                                <subscriptIndex :propValue="propValue"></subscriptIndex>
                                            </view>
                                            <view v-if="field_show.includes('0') || field_show.includes('1') || field_show.includes('2') || (field_show.includes('3') && name_float == '0')" class="jc-sb flex-1 flex-col" :style="article_theme !== '0' ? content_padding : ''">
                                                <view class="flex-col" :style="'gap:' + name_desc_space + 'px;'">
                                                    <view v-if="field_show.includes('3') && name_float == '0'" class="title text-line-2" :style="article_name + article_name_height_computer">{{ item.new_title ? item.new_title : item.data.title }}</view>
                                                    <view v-if="field_show.includes('2')" :class="'desc ' + field_desc_row == '2' ? 'text-line-2' : 'text-line-1'" :style="article_desc">{{ item.data.describe || '' }}</view>
                                                </view>
                                                <view :class="'flex-row jc-sb gap-8 align-e' + ((field_show.includes('3') && name_float == '0') || field_show.includes('2') ? ' margin-top' : '')">
                                                    <view :style="article_date">{{ field_show.includes('0') ? item.data.add_time : '' }}</view>
                                                    <view v-show="field_show.includes('1')" class="flex-row align-c gap-3" :style="article_page_view">
                                                        <iconfont name="icon-eye" propContainerDisplay="flex"></iconfont>
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
    import { listArticles } from '@/api/diy/article.js';
    import subscriptIndex from '@/pages/diy/components/diy/modules/subscript.vue';
    var system = app.globalData.get_system_info(null, null, true);
    var sys_width = app.globalData.window_width_handle(system.windowWidth);
    export default {
        components: {
            subscriptIndex,
        },
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
                article_theme: '0',

                // 是否显示
                field_show: ['0', '1'],
                // 文章
                article_name: '',
                // 描述
                article_desc: '',
                // 日期
                article_date: '',
                // 浏览量
                article_page_view: '',
                // 内容圆角
                content_radius: '',
                // 图片圆角
                img_radius: '',
                // 内间距
                content_padding: '',
                // 内容间距
                content_spacing: '',
                // 文章间距
                article_spacing: '',
                // article_item_height: '',

                article_style: '',
                article_img_style: '',
                // 轮播图定时轮播
                interval_time: 2000,
                // 轮播图是否滚动
                is_roll: 1,

                article_theme_class: '',
                // 轮播高度
                carousel_height_computer: '',
                // 文章内容高度
                article_name_height_computer: '',
                // 文章名称浮动样式
                float_name_style: '',
                name_float: '0',
                // 图片大小
                img_size: '',
                // 文章轮播数据
                article_carousel_list: [],
                // 文章描述间距
                name_desc_space: 0,
                // 一行显示的数量
                slides_per_group: 1,
                next_margin: '0rpx',
                field_desc_row: '1',
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
                // 描述样式
                const desc_size = new_style.desc_size;
                let desc_style = 'font-size:' + desc_size + 'px;line-height:' + desc_size + 'px;height:' + desc_size + 'px;color:' + new_style.desc_color + ';';
                if (new_content.field_desc_row == '2') {
                    desc_style = 'font-size:' + desc_size + 'px;line-height:' + (desc_size > 0 ? desc_size + 3 : 0 ) + 'px;height:'+ (desc_size > 0 ? (desc_size + 3) * 2 : 0) + 'px;color:' + new_style.desc_color + ';';
                }
                this.setData({
                    field_desc_row: new_content.field_desc_row,
                    name_float: !isEmpty(new_content.name_float) ? new_content.name_float : '0',
                    // 数据：data_type == '0' 指定文章(快照 data_list)；data_type == '1' 筛选文章(快照 data_auto_list，空则由列表接口动态获取)
                    data_list: this.get_article_render_list(new_content),
                    article_theme_class: this.article_theme_class_computer(new_content.theme),
                    article_theme: new_content.theme,
                    field_show: new_content.field_show,
                    // 样式
                    article_name: 'font-size:' + new_style.name_size + 'px;' + 'font-weight:' + new_style.name_weight + ';' + 'color:' + new_style.name_color + ';',
                    article_desc: desc_style,
                    article_date: 'font-size:' + new_style.time_size + 'px;' + 'font-weight:' + new_style.time_weight + ';' + 'color:' + new_style.time_color + ';',
                    article_page_view: 'font-size:' + new_style.page_view_size + 'px;' + 'font-weight:' + new_style.page_view_weight + ';' + 'color:' + new_style.page_view_color + ';',
                    content_radius: radius_computer(new_style.content_radius),
                    img_radius: radius_computer(new_style.img_radius),
                    // 内间距
                    content_padding: padding_computer(new_style.padding),
                    // 内容间距
                    content_spacing: `gap: ${new_style.content_spacing}px;`,
                    // 文章间距
                    article_spacing: `gap: ${new_style.article_spacing}px;`,
                    // 描述间距
                    name_desc_space: parseInt(new_style.name_desc_space),
                    next_margin: new_style.rolling_fashion == 'translation' ? '-' + new_style.article_spacing_margin + 'px' : '0rpx',
                    // 文章内容高度
                    slides_per_group: new_style.rolling_fashion == 'translation' ?  Number(new_content.carousel_col) + 1 : 1,

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
                if (['0'].includes(new_content.theme)) {
                    // 图片宽度
                    if (typeof new_style.content_img_width == 'number') {
                        img_style += `width: ${ new_style.content_img_width * scale }px;`;
                    } else {
                        const list = product_style_list.filter(item => item.value == new_content.theme);
                        if (list.length > 0) {
                            img_style += `width: ${ list[0].width * scale }px;`;
                        } else {
                            img_style += 'width: auto;';
                        }
                    }
                }
                if (!['3', '4'].includes(new_content.theme)) {
                    // 图片宽度
                    if (typeof new_style.content_img_height == 'number') {
                        img_style += `height: ${ new_style.content_img_height * scale }px;`;
                    } else {
                        const list = product_style_list.filter(item => item.value == new_content.theme);
                        if (list.length > 0) {
                            img_style += `height: ${ list[0].height * scale }px;`;
                        } else {
                            img_style += 'height: auto;';
                        }
                    }
                }
                // 背景图的处理
                const article_data = {
                    background_img_style: new_style?.article_background_img_style || '',
                    background_img: new_style?.article_background_img || '',
                }
                const article_margin = new_style.value?.margin || old_margin;
                const margin_width = article_margin.margin_left + article_margin.margin_right;
                // 渐变效果
                const all_style = gradient_handle(new_style?.article_color_list || [], new_style?.article_direction || '') + margin_computer(article_margin) + box_shadow_computer(new_style) + border_computer(new_style);
                // 文章样式
                if (this.article_theme == '0') {
                    this.setData({
                        img_size: img_style,
                        article_style:  this.content_radius + all_style,
                        article_img_style: this.content_spacing + this.content_padding + background_computer(article_data)
                    });
                } else if (this.article_theme == '1') {
                    this.setData({
                        img_size: img_style,
                        article_style: `width: calc(50% - ${(new_style.article_spacing + (margin_width * 2)) / 2}px);` + this.content_radius + all_style,
                        article_img_style: background_computer(article_data)
                    });
                } else if (this.article_theme == '2') {
                    this.setData({
                        img_size: img_style,
                        article_style: this.content_radius + all_style,
                        article_img_style: background_computer(article_data)
                    });
                } else if (this.article_theme == '3') {
                    this.setData({
                        style: `background:#fff;` + this.content_radius,
                    });
                } else if (this.article_theme == '4') {
                    // 更新轮播图的key，确保更换时能重新更新轮播图
                    const temp_carousel_col = new_content.carousel_col || '1';
                    // 计算间隔的空间。(gap * gap数量) / 模块数量
                    let gap = temp_carousel_col !== '0' ? ((new_style.article_spacing * temp_carousel_col - 1) + (margin_width * temp_carousel_col)) / temp_carousel_col : '0';
                    const multicolumn_columns_width = new_style.rolling_fashion == 'translation' ? `margin-right: ${ new_style.article_spacing + article_margin.margin_right }px;width:100%;` : `width:calc(${100 / (Number(temp_carousel_col) + 1)}% - ${gap * 2}rpx);min-width:calc(${100 / (Number(temp_carousel_col) + 1)}% - ${gap * 2}rpx);`;
                    const { name_bg_color_list = [], name_bg_direction = '180deg', name_bg_radius, name_bg_padding, name_bg_margin } = new_style;
                    const data = {
                        color_list: name_bg_color_list,
                        direction: name_bg_direction,
                    }
                    let location = 'position:absolute;bottom:0;left:0;right:0;'
                    // 每页显示的数量
                    const num = Number(temp_carousel_col) + 1;
                    // 轮播宽度
                    this.setData({
                        // 滚动时间
                        interval_time: (new_style.interval_time || 2) * 1000,
                        // 是否滚动修改
                        is_roll: new_style.is_roll,
                        // article_item_height: `height: ${new_style.article_height }px`,
                        article_style: this.content_radius + all_style + multicolumn_columns_width,
                        // 轮播高度
                        carousel_height_computer: new_style.article_height * scale + 'px',
                        // 文章内容高度
                        article_name_height_computer: `height:${new_style.name_size * 2.4 * 2}rpx;line-height:${new_style.name_size * 1.2 * 2}rpx;`,
                        float_name_style: gradient_computer(data) + (!isEmpty(name_bg_radius) ? radius_computer(name_bg_radius) : '') + (!isEmpty(name_bg_padding) ? padding_computer(name_bg_padding) : '' ) + (!isEmpty(name_bg_padding) ? margin_computer(name_bg_margin) : '') + location,
                        article_img_style: background_computer(article_data),
                        article_carousel_list: get_swiper_list(this.data_list, num, new_style.rolling_fashion),
                    });
                }

                if (this.propIsCommonStyle) {
                    this.setData({
                        style_container: common_styles_computer(new_style.common_style),
                        style_img_container: common_img_computer(new_style.common_style, this.propIndex),
                    });
                }

                // 筛选文章（data_type == '1' 自动模式）：快照为空则动态获取
                // 兼容历史/其它约定：data_type == '0' 但无快照且携带筛选参数时同样动态获取
                var has_auto_config = new_content.category_ids != null || new_content.category != null || !isEmpty(new_content.keywords);
                if (new_content.data_type == '1' || (new_content.data_type == '0' && isEmpty(new_content.data_list) && isEmpty(new_content.data_auto_list) && has_auto_config)) {
                    this.get_auto_data_list_handle(new_content, new_style);
                }
            },
            // 文章渲染数据（按 DIY 数据来源取数并做字段适配）
            get_article_render_list(new_content) {
                var data_list = [];
                if (new_content.data_type == '0') {
                    // 指定文章：使用快照 data_list
                    if (!isEmpty(new_content.data_list)) {
                        for (var i = 0; i < new_content.data_list.length; i++) {
                            data_list.push(this.article_item_handle(new_content.data_list[i]));
                        }
                    }
                } else if (new_content.data_type == '1') {
                    // 筛选文章：优先使用快照 data_auto_list
                    if (!isEmpty(new_content.data_auto_list)) {
                        for (var j = 0; j < new_content.data_auto_list.length; j++) {
                            data_list.push(this.article_item_handle(new_content.data_auto_list[j]));
                        }
                    }
                }
                return data_list;
            },
            // 将文章快照或 DTO 转换为渲染字段。
            article_item_handle(item) {
                if (isEmpty(item)) {
                    return { id: get_math(), new_title: '', new_cover: [], data: {} };
                }
                var item_data = (item.data || null) != null ? item.data : item;
                var article_id = (item_data.id || null) != null ? item_data.id : (item.id || null);
                var article_title = !isEmpty(item.new_title) ? item.new_title : (item_data.title || '');
                var article_url = '';
                if ((item_data.url || null) != null && item_data.url != '') {
                    article_url = item_data.url;
                } else if ((item_data.jumpUrl || null) != null && item_data.jumpUrl != '') {
                    article_url = item_data.jumpUrl;
                } else if (article_id != null) {
                    article_url = '/pages/article-detail/article-detail?id=' + article_id;
                }
                return {
                    id: article_id != null ? article_id : get_math(),
                    new_title: !isEmpty(item.new_title) ? item.new_title : '',
                    new_cover: !isEmpty(item.new_cover) ? item.new_cover : [],
                    data: {
                        id: article_id,
                        title: article_title,
                        describe: (item_data.describe || null) != null && item_data.describe != '' ? item_data.describe : (item_data.description || ''),
                        cover: item_data.cover || '',
                        add_time: (item_data.add_time || null) != null ? item_data.add_time : time_stamp(item_data.createTime),
                        access_count: (item_data.access_count != null && item_data.access_count !== '') ? item_data.access_count : (item_data.accessCount || 0),
                        url: article_url,
                    },
                };
            },
            // 筛选文章（自动模式）：根据 com_data 组装参数调用文章列表接口
            get_auto_data_list_handle(new_content, new_style) {
                // 快照已有数据时优先使用快照，避免重复请求
                if (!isEmpty(new_content.data_auto_list)) {
                    return;
                }
                var params = {
                    pageNum: 1,
                };
                // 显示数量
                var number = parseInt(new_content.number) || 10;
                params['pageSize'] = number > 50 ? 50 : number;
                // 分类（兼容 category_ids 数组 / category 单个;多分类走 categoryIds 逗号串,后端含子级递归）
                var category_ids = new_content.category_ids != null ? new_content.category_ids : new_content.category;
                var category_id = null;
                var category_ids_multi = [];
                if (category_ids != null && category_ids !== '') {
                    if (category_ids instanceof Array) {
                        category_ids_multi = category_ids;
                    } else if (typeof category_ids == 'number' || !isNaN(parseFloat(String(category_ids)))) {
                        category_ids_multi = String(category_ids).split(',').filter(function(n) { return n !== '' && n !== '0' && !isNaN(parseInt(n)); });
                    }
                }
                if (category_ids_multi.length > 0) {
                    category_id = category_ids_multi[0];
                }
                if (category_ids_multi.length > 1) {
                    params['categoryIds'] = category_ids_multi.join(',');
                } else if (category_id != null && category_id != 0) {
                    params['categoryId'] = parseInt(category_id);
                }
                // 仅取有封面
                if (new_content.is_cover == '1' || new_content.is_cover == 1) {
                    params['onlyCover'] = 1;
                }
                // 关键字
                if (!isEmpty(new_content.keywords)) {
                    params['keyword'] = new_content.keywords;
                }
                // 排序（order_by_type/order_by_rule，兼容历史 sort/sort_rules 配置）
                var order_by_type = new_content.order_by_type != null ? new_content.order_by_type : new_content.sort;
                var order_by_rule = new_content.order_by_rule != null ? new_content.order_by_rule : new_content.sort_rules;
                if (order_by_type != null && order_by_type !== '') {
                    params['orderByType'] = parseInt(order_by_type);
                }
                if (order_by_rule != null && order_by_rule !== '') {
                    if (String(order_by_rule) == '1') {
                        params['orderByRule'] = 'asc';
                    } else if (String(order_by_rule) == '0') {
                        params['orderByRule'] = 'desc';
                    } else {
                        params['orderByRule'] = order_by_rule;
                    }
                }
                // 请求列表接口
                listArticles(params)
                    .then((data) => {
                        var page_data = data.data || null;
                        var page_list = (page_data != null && page_data.list != null) ? page_data.list : [];
                        var data_list = [];
                        for (var i in page_list) {
                            data_list.push(this.article_item_handle(page_list[i]));
                        }
                        var set_data_obj = {
                            data_list: data_list,
                        };
                        // 左右滑动风格（theme 4）需要同步更新轮播数据
                        if (this.article_theme == '4') {
                            var rolling_fashion = (new_style || {}).rolling_fashion;
                            var carousel_num = Number(new_content.carousel_col || '1') + 1;
                            set_data_obj['article_carousel_list'] = get_swiper_list(data_list, carousel_num, rolling_fashion);
                        }
                        this.setData(set_data_obj);
                    })
                    .catch(() => {});
            },
            // 文章主题样式
            article_theme_class_computer(theme) {
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
