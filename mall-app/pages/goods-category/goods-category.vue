<template>
    <view :class="theme_view">
        <view :class="popup_status ? 'fixed-top' : ''">
            <view class="pr" :class="is_single_page == 1 ? 'margin-top-xxxl single-page-top' : ''">
                <block v-if="category_list.length > 0">
                    <view v-if="category_list.length > 0" class="goods-top-bg pa top-0 left-0 right-0 wh-auto oh">
                        <image :src="theme_static_url + 'top-bg.png'" mode="widthFix" class="wh-auto"></image>
                    </view>
                    <!-- 搜索框 -->
                    <block v-if="is_single_page == 0">
                        <view class="nav-search padding-horizontal-main pr" :style="'padding-top:' + (status_bar_height + 5) + 'px;'">
                            <view class="goods-top-search-bg pa top-0 left-0 right-0 bottom-0 wh-auto oh">
                                <image :src="theme_static_url + 'top-bg.png'" mode="widthFix" class="wh-auto"></image>
                            </view>
                            <block v-if="category_goods_is_search_alone == 1">
                                <component-search :propPlaceholder="$t('customview.customview.726k7y')"></component-search>
                            </block>
                            <block v-else>
                                <component-search @onsearch="search_button_event" :propIsOnEvent="true" :propIsRequired="false" :propPlaceholder="$t('customview.customview.726k7y')"></component-search>
                            </block>
                        </view>
                    </block>
                    <view class="category-container">
                        <!-- 分类内容 -->
                        <view :class="'category-content bs-bb pr ' + (category_show_level == 0 ? 'goods-model' : '')" :style="category_content_style">
                            <block v-if="category_show_level == 1">
                                <!-- 一级模式 -->
                                <scroll-view scroll-y class="ht-auto" :show-scrollbar="false">
                                    <view class="model-one padding-sm oh flex-row flex-wrap">
                                        <block v-for="(item, index) in category_list" :key="index">
                                            <view class="content-item cp" :data-value="item.id" @tap="category_event">
                                                <view class="padding-sm">
                                                    <view class="content bg-white border-radius-main">
                                                        <image v-if="(item[category_goods_model_icon_field] || null) != null" :src="item[category_goods_model_icon_field]" mode="aspectFit" class="icon circle"></image>
                                                        <view class="text single-text margin-top-sm" :class="'text-random-' + (Math.floor(Math.random() * 9) + 1)">{{ item.name }}</view>
                                                        <view class="desc multi-text margin-top-xs padding-top-xss cr-grey-9 text-size-xss">{{ item.describe }}</view>
                                                    </view>
                                                </view>
                                            </view>
                                        </block>
                                    </view>
                                </scroll-view>
                            </block>
                            <block v-else>
                                <!-- 商品列表模式 -->
                                <block v-if="category_show_level == 0">
                                    <!-- 一级导航 -->
                                    <view class="top-nav wh-auto pa scroll-view-horizontal bottom-0">
                                        <scroll-view :scroll-x="true" :show-scrollbar="false" :scroll-with-animation="true" :scroll-into-view="'one-nav-item-' + nav_active_index" class="top-nav-scroll">
                                            <block v-for="(item, index) in category_list" :key="index">
                                                <view class="item tc cp dis-inline-block text-size-xss" :id="'one-nav-item-' + index" :data-index="index" :data-itemtwoindex="-1" :data-itemthreeindex="-1" @tap="nav_event">
                                                    <view :class="'icon-content circle br auto ' + (nav_active_index == index ? 'border-color-main' : '')">
                                                        <image :src="(item[category_goods_model_icon_field] || null) == null ? common_static_url + 'images.png' : item[category_goods_model_icon_field]" mode="aspectFit" class="icon dis-block auto wh-auto ht-auto circle"></image>
                                                    </view>
                                                    <view class="padding-left-sm padding-right-sm round margin-top-xs" :class="nav_active_index == index ? 'bg-main border-color-main cr-white' : ''">{{ item.name }} </view>
                                                </view>
                                            </block>
                                        </scroll-view>
                                        <component-nav-more class="nav-more-top" :propTop="search_height + 'px'" :propStatus="popup_status" @open-popup="open_popup_event">
                                            <scroll-view scroll-y="true" class="nav-list-more">
                                                <view class="flex-row flex-wrap align-c">
                                                    <block v-for="(item, index) in category_list" :key="index">
                                                        <view class="item tc cr-base cp text-size-xss" :id="'one-nav-item-' + index" :data-index="index" :data-itemtwoindex="-1" :data-itemthreeindex="-1" @tap="nav_event">
                                                            <view :class="'icon-content circle br auto ' + (nav_active_index == index ? 'border-color-main' : '')">
                                                                <image :src="(item[category_goods_model_icon_field] || null) == null ? common_static_url + 'images.png' : item[category_goods_model_icon_field]" mode="aspectFit" class="icon dis-block auto wh-auto ht-auto circle"></image>
                                                            </view>
                                                            <view class="dis-inline-block padding-left-sm padding-right-sm round margin-top-xs" :class="nav_active_index == index ? 'bg-main border-color-main cr-white' : ''">
                                                                {{ item.name }}
                                                            </view>
                                                        </view>
                                                    </block>
                                                </view>
                                            </scroll-view>
                                        </component-nav-more>
                                    </view>
                                    <!-- 二级导航 -->
                                    <view v-if="category_one_subset_count > 0" class="left-nav" :style="left_content_actual_style">
                                        <scroll-view :scroll-y="true" :show-scrollbar="false" class="ht-auto">
                                            <view :class="'text-size-sm item tc cr-base cp oh ' + (nav_active_item_two_index == -1 ? 'nav-active cr-main nav-left-border' : '')" :data-index="nav_active_index" :data-itemtwoindex="-1" :data-itemthreeindex="-1" @tap="nav_event">
                                                <text>{{ $t('common.all') }}</text>
                                            </view>
                                            <block v-if="(data_content || null) != null && (data_content.items || null) != null && data_content.items.length > 0">
                                                <block v-for="(item, index) in data_content.items" :key="index">
                                                    <view :class="'text-size-sm item tc cr-base cp oh ' + (nav_active_item_two_index == index ? 'nav-active cr-main nav-left-border' : '')" :data-index="nav_active_index" :data-itemtwoindex="index" :data-itemthreeindex="-1" @tap="nav_event">
                                                        <text>{{ item.name }}</text>
                                                    </view>
                                                </block>
                                            </block>
                                        </scroll-view>
                                    </view>
                                    <!-- 商品列表 -->
                                    <view :class="'goods-right-content bg-white pa bs-bb ' + (category_one_subset_count > 0 ? '' : 'category-one-subset-content')">
                                        <scroll-view :scroll-y="true" :show-scrollbar="false" class="ht-auto goods-list" :scroll-top="scroll_top" @scroll="scroll_event" @scrolltolower="scroll_lower" lower-threshold="60">
                                            <view class="padding-left-main" :style="right_content_actual_style">
                                                <!-- 操作导航 -->
                                                <view class="goods-list-top-nav bg-white">
                                                    <!-- 排序 -->
                                                    <view class="nav-sort-content oh">
                                                        <block v-for="(item, index) in search_nav_sort_list" :key="index">
                                                            <view class="sort-item tc fl cp" :data-index="index" @tap="nav_sort_event">
                                                                <text class="cr-base va-m text-size-sm">{{ item.name }}</text>
                                                                <image v-if="(item.icon || null) != null" class="sort-icon va-m" :src="common_static_url + 'sort-' + item.icon + '-icon.png'" mode="aspectFill"></image>
                                                            </view>
                                                        </block>
                                                    </view>
                                                    <!-- 三级导航 -->
                                                    <view v-if="(data_three_content || null) != null && (data_three_content.items || null) != null && data_three_content.items.length > 0" class="word-list scroll-view-horizontal padding-bottom-main">
                                                        <scroll-view :scroll-x="true" :show-scrollbar="false" :scroll-with-animation="true" :scroll-into-view="'three-nav-item-' + nav_active_item_three_index">
                                                            <view :class="'word-icon dis-inline-block text-size-xs round padding-top-xs padding-bottom-xs padding-left padding-right ' + (nav_active_item_three_index == -1 ? 'bg-main-light br-main-light cr-main' : 'br-grey cr-grey')" :data-index="nav_active_index" :data-itemtwoindex="nav_active_item_two_index" :data-itemthreeindex="-1" @tap="nav_event">{{ $t('common.all') }}</view>
                                                            <block v-for="(item, index) in data_three_content.items" :key="index">
                                                                <view :class="'word-icon dis-inline-block text-size-xs round padding-top-xs padding-bottom-xs padding-left padding-right ' + (nav_active_item_three_index == index ? 'bg-main-light br-main-light cr-main' : 'br-grey cr-grey')" :id="'three-nav-item-' + index" :data-index="nav_active_index" :data-itemtwoindex="nav_active_item_two_index" :data-itemthreeindex="index" @tap="nav_event">{{ item.name }}</view>
                                                            </block>
                                                        </scroll-view>
                                                    </view>
                                                </view>
                                                <!-- 右侧商品列表 -->
                                                <view v-if="(data_list || null) != null && data_list.length > 0" class="oh">
                                                    <view v-for="(item, index) in data_list" :key="index" class="item oh pr spacing-mb">
                                                        <!-- 商品主体内容 -->
                                                        <view class="flex-row jc-sb" data-type="type" :data-index="index" :data-value="item.goods_url + '&is_opt_back=1'" @tap="goods_event">
                                                            <image :src="item.images" mode="widthFix" class="goods-img radius dis-block"></image>
                                                            <view class="goods-base flex-col jc-sb">
                                                                <view class="goods-base-content">
                                                                    <view class="goods-title multi-text">{{ item.title }}</view>
                                                                    <view v-if="(item.simple_desc || null) != null" class="simple-desc cr-grey-9 text-size-xs margin-top-sm single-text">{{ item.simple_desc }}</view>
                                                                </view>
                                                                <view v-if="(item.show_field_price_status || 0) == 1" class="oh flex-row align-c">
                                                                    <text class="sales-price">
                                                                        <text class="text-size-xss">{{ item.show_price_symbol }}</text>
                                                                        <text class="text-size">{{ item.min_price }}</text>
                                                                    </text>
                                                                    <text v-if="(item.sales_count || 0) > 0" class="cr-grey-9 text-size-xss" style="margin-left:16rpx">{{ $t('goods-list.goods-list.sales_count') }}{{ item.sales_count }}</text>
                                                                </view>
                                                            </view>
                                                        </view>
                                                        <!-- 标签插件 -->
                                                        <view v-if="(plugins_label_data || null) != null && plugins_label_data.data.length > 0" :class="'plugins-label oh pa plugins-label-' + ((plugins_label_data.base.is_user_goods_label_icon || 0) == 0 ? 'text' : 'img') + ' plugins-label-' + (plugins_label_data.base.user_goods_show_style || 'top-left')">
                                                            <block v-for="(lv, li) in plugins_label_data.data" :key="li">
                                                                <view v-if="(lv.goods_ids || null) != null && lv.goods_ids.indexOf(item.id) != -1" class="lv dis-inline-block va-m" :data-value="(plugins_label_data.base.is_user_goods_label_url || 0) == 1 ? lv.url || '' : ''" @tap="url_event">
                                                                    <view v-if="(plugins_label_data.base.is_user_goods_label_icon || 0) == 0" class="round cr-white bg-main text-size-xs fl" :style="('background-color:'+(lv.bg_color || '#666'))+' !important;'+('color:'+(lv.text_color || '#fff'))+' !important;'">
                                                                        {{ lv.name }}
                                                                    </view>
                                                                    <block v-else>
                                                                        <image v-if="(lv.icon || null) != null" class="dis-block" :src="lv.icon" mode="scaleToFill"></image>
                                                                    </block>
                                                                </view>
                                                            </block>
                                                        </view>
                                                    </view>
                                                </view>
                                                <block v-else>
                                                    <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg" :propBackBtn="false" propPage="goods-category-item"></component-no-data>
                                                </block>
                                            </view>
                                        </scroll-view>
                                    </view>
                                </block>
                                <block v-else>
                                    <!-- 一级导航 -->
                                    <view class="left-nav ht-auto">
                                        <scroll-view :scroll-y="true" class="ht-auto" :show-scrollbar="false">
                                            <view :class="common_site_type != 4 ? 'left-content-actual ht-auto' : ''">
                                                <view class="ht-auto padding-0" :style="left_content_actual_style">
                                                    <block v-for="(item, index) in category_list" :key="index">
                                                        <view :class="'text-size-sm item tc cr-base cp oh ' + (nav_active_index == index ? 'nav-active cr-main nav-left-border' : '')" :data-index="index" :data-itemindex="-1" @tap="nav_event">
                                                            <text>{{ item.name }}</text>
                                                        </view>
                                                    </block>
                                                </view>
                                            </view>
                                        </scroll-view>
                                    </view>
                                    <view class="right-container pa">
                                        <scroll-view :scroll-y="true" class="ht-auto bg-white" :show-scrollbar="false">
                                            <!-- 一级内容基础容 -->
                                            <view v-if="(data_content || null) != null" class="oh">
                                                <view class="padding-top-main padding-horizontal-main">
                                                    <!-- 一级基础信息 -->
                                                    <block v-if="(data_content.banner_images || null) !== null">
                                                        <view :class="'one-content border-radius-main cp ' + (category_show_level == 3 ? 'spacing-mb' : '')" :data-value="data_content.id" @tap="category_event">
                                                            <image :src="data_content.banner_images" mode="scaleToFill" class="wh-auto radius"></image>
                                                        </view>
                                                    </block>
                                                    <block v-else>
                                                        <view v-if="(data_content.vice_name || null) != null || (data_content.describe || null) != null" :class="'one-content border-radius-main cp pr ' + (category_show_level == 3 ? 'spacing-mb' : '')" :data-value="data_content.id" @tap="category_event">
                                                            <image :src="theme_static_url + 'level2-content-bg.png'" mode="scaleToFill" class="wh-auto ht-auto radius pa top-0 left-0"></image>
                                                            <view class="pr padding-main">
                                                                <view v-if="(data_content.vice_name || null) != null" class="text-size-md fw-b" :style="'color:' + data_content.bg_color + ';'">
                                                                    {{ data_content.vice_name }}
                                                                </view>
                                                                <view v-if="(data_content.describe || null) != null" class="text-size-xs cr-grey margin-top-sm">{{ data_content.describe }}</view>
                                                            </view>
                                                        </view>
                                                    </block>
                                                    <!-- 一二级数据渲染 -->
                                                    <block v-if="(data_content.items || null) != null && data_content.items.length > 0">
                                                        <!-- 二级模式 -->
                                                        <block v-if="category_show_level == 2">
                                                            <view class="two-content oh border-radius-main spacing-mb flex-row flex-wrap">
                                                                <block v-for="(v, index) in data_content.items" :key="index">
                                                                    <view class="content-item tc cp two-img" :data-value="v.id" @tap="category_event">
                                                                        <view class="content">
                                                                            <image v-if="(v.icon || null) != null" :src="v.icon" mode="aspectFit" class="icon radius margin-bottom-main"></image>
                                                                            <view class="single-text text-size-sm cr-grey">{{ v.name }}</view>
                                                                        </view>
                                                                    </view>
                                                                </block>
                                                            </view>
                                                        </block>
                                                        <!-- 三级模式 -->
                                                        <block v-if="category_show_level == 3">
                                                            <block v-for="(v, index) in data_content.items" :key="index">
                                                                <view class="spacing-nav-title flex-row jc-sb align-c">
                                                                    <view class="flex-1">
                                                                        <text class="text-wrapper text-size-md">{{ v.name }}</text>
                                                                        <text v-if="(v.describe || null) != null" class="vice-name margin-left-lg cr-grey">{{ v.describe }}</text>
                                                                    </view>
                                                                    <view :data-value="v.id" @tap="category_event" class="arrow-right padding-right cr-grey text-size-xs">{{ $t('common.more') }}</view>
                                                                </view>
                                                                <view v-if="(v.items || null) != null && v.items.length > 0" class="oh border-radius-main spacing-mb flex-row flex-wrap">
                                                                    <block v-for="(vs, index2) in v.items" :key="index2">
                                                                        <view class="content-item two-img tc cp" :data-value="vs.id" @tap="category_event">
                                                                            <view class="content">
                                                                                <image v-if="(vs.icon || null) != null" :src="vs.icon" mode="aspectFit" class="icon radius margin-bottom-main"></image>
                                                                                <view class="single-text text-size-sm cr-grey">{{ vs.name }}</view>
                                                                            </view>
                                                                        </view>
                                                                    </block>
                                                                </view>
                                                            </block>
                                                        </block>
                                                    </block>
                                                    <block v-else>
                                                        <!-- 提示信息 -->
                                                        <component-no-data propStatus="0" :propMsg="$t('goods-category.goods-category.7e4m68')"></component-no-data>
                                                    </block>
                                                </view>
                                            </view>
                                            <view v-else>
                                                <!-- 提示信息 -->
                                                <component-no-data propStatus="0" :propMsg="$t('goods-category.goods-category.7e4m68')"></component-no-data>
                                            </view>
                                        </scroll-view>
                                    </view>
                                </block>
                            </block>
                        </view>

                        <view v-if="category_list.length == 0 && data_list_loding_status != 0">
                            <!-- 提示信息 -->
                            <component-no-data :propStatus="data_list_loding_status"></component-no-data>
                        </view>

                    </view>
                </block>
                <block v-else>
                    <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg" propPage="goods-category" :propIsHeader="true"></component-no-data>
                </block>

            </view>
        </view>
        
        <!-- 公共 -->
        <component-common ref="common" @onFooterHeight="footer_height_value_event" :propIsFooterSeat="false"></component-common>
    </view>
</template>

<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentSearch from '@/components/search/search';
    import componentNoData from '@/components/no-data/no-data';
    import componentNavMore from '@/components/nav-more/nav-more';
    import componentPopup from '@/components/popup/popup';
    import iconfont from '@/components/iconfont/iconfont';
    import { getCategoryList } from '@/api/pms/category.js';
    import { listSpuPages } from '@/api/pms/goods.js';

    var theme_static_url = app.globalData.get_static_url('goods-category');
    var common_static_url = app.globalData.get_static_url('common');
    var bar_height = parseInt(app.globalData.get_system_info('statusBarHeight', 0));
    // #ifdef MP-TOUTIAO
    bar_height = 0;
    // #endif

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                theme_static_url: theme_static_url,
                common_static_url: common_static_url,
                status_bar_height: bar_height,
                data_bottom_line_status: false,
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                data_is_loading: 0,
                load_status: 0,
                user: null,
                params: null,
                tabbar_params: null,
                common_site_type: 0,
                category_list: [],
                data_content: null,
                data_three_content: null,
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                currency_symbol: app.globalData.currency_symbol(),
                is_first: 1,
                search_keywords_value: '',
                nav_active_index: 0,
                nav_active_item_two_index: -1,
                nav_active_item_three_index: -1,
                scroll_top: 0,
                scroll_top_old: 0,
                category_one_subset_count: 0,
                category_show_level: 0,
                search_nav_sort_index: 0,
                search_nav_sort_list: [],
                share_info: {},
                is_single_page: app.globalData.is_current_single_page() || 0,
                category_goods_model_icon_field: 'realistic_images',
                category_goods_is_search_alone: app.globalData.get_config('category_goods_is_search_alone', 1),
                category_goods_is_show_cart_nav: app.globalData.get_config('category_goods_is_show_cart_nav', 0),
                theme_color: app.globalData.get_theme_color(),
                popup_status: false,
                search_height: 0,
                window_bottom_height: 0,
                // #ifdef H5
                window_bottom_height: (app.globalData.get_config('is_use_native_tabbar', 0) == 1) ? (uni.getWindowInfo().windowBottom || 50) : 0,
                // #endif
                category_content_style: '',
                left_content_actual_style: '',
                right_content_actual_style: '',
                footer_height_value: 0,
                plugins_label_data: null,
                data_show_type_value: 0,
            };
        },

        components: {
            componentCommon,
            componentSearch,
            componentNoData,
            componentNavMore,
            componentPopup,
            iconfont
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            this.setData({
                params: params,
            });
        },

        onShow() {
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 资源设置
            this.set_resources_data();

            // 基础参数
            this.setData({
                user: app.globalData.get_user_cache_info(),
                tabbar_params: app.globalData.get_page_tabbar_switch_params(),
            });

            // 数据加载
            this.init();

            // 初始化配置
            if(app.globalData.get_config('status') == 1) {
                app.globalData.init_config(0, this, 'init_config', true);
            } else {
                app.globalData.is_config(this, 'init_config');
            }

            // 清除tab参数
            app.globalData.remove_page_tabbar_switch_params();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show({object: this, method: 'init'});
            }
        },

        methods: {
            set_resources_data() {
                var search_nav_sort_list = [
                    {
                        name: this.$t('goods-category.goods-category.x69aow'),
                        field: 'default',
                        sort: 'asc',
                        icon: null,
                    },
                    {
                        name: this.$t('goods-category.goods-category.at5p35'),
                        field: 'sales',
                        sort: 'asc',
                        icon: 'default',
                    },
                    {
                        name: this.$t('goods-category.goods-category.g2u3lf'),
                        field: 'price',
                        sort: 'asc',
                        icon: 'default',
                    },
                    {
                        name: this.$t('goods-category.goods-category.5p4ksj'),
                        field: 's.id',
                        sort: 'asc',
                        icon: 'default',
                    },
                ];
                this.setData({
                    search_nav_sort_list,
                });
            },
            // 初始化配置
            init_config(status) {
                if ((status || false) == true) {
                    var showLevel = app.globalData.get_config('category_show_level');
                    var searchAlone = app.globalData.get_config('category_goods_is_search_alone');
                    var updateData = {
                        common_site_type: app.globalData.get_config('common_site_type'),
                        currency_symbol: app.globalData.get_config('currency_symbol'),
                    };
                    if (showLevel != null) {
                        updateData.category_show_level = showLevel;
                    }
                    if (searchAlone != null) {
                        updateData.category_goods_is_search_alone = searchAlone;
                    }
                    this.setData(updateData);
                }
            },

            // 获取数据
            init(params = {}) {
                var self = this;
                if (self.category_list.length > 0 && self.is_first != 1) {
                    // 已有分类数据，仅刷新商品列表
                    if (self.category_show_level == 0) {
                        self.get_goods_list(1);
                    }
                    return;
                }

                this.setData({ data_list_loding_status: 1 });

                getCategoryList().then(function (res) {
                    uni.stopPullDownRefresh();
                    var data = res.data || [];
                    var temp_category = self.buildCategoryTree(data);
                    var active_index = self.nav_active_index;

                    if (temp_category.length > 0) {
                        // tab切换参数
                        if ((self.tabbar_params || null) != null && (self.tabbar_params.id || null) != null) {
                            for (var i in temp_category) {
                                if (temp_category[i]['id'] == self.tabbar_params.id) {
                                    active_index = parseInt(i);
                                    break;
                                }
                            }
                        }
                        if ((self.params || null) != null && (self.params.id || null) != null) {
                            for (var i in temp_category) {
                                if (temp_category[i]['id'] == self.params.id) {
                                    active_index = parseInt(i);
                                    break;
                                }
                            }
                        }

                        var category_one_subset_count = 0;
                        for (var i in temp_category) {
                            if ((temp_category[i]['items'] || null) != null && temp_category[i]['items'].length > 0) {
                                category_one_subset_count++;
                            }
                        }

                        // 检测是否有分类图片
                        var has_category_image = false;
                        for (var j in temp_category) {
                            if ((temp_category[j][self.category_goods_model_icon_field] || null) != null) {
                                has_category_image = true;
                                break;
                            }
                        }

                        var upd_data = {
                            load_status: 1,
                            category_list: temp_category,
                            data_content: temp_category[active_index] || null,
                            nav_active_index: active_index,
                            category_one_subset_count: category_one_subset_count,
                            has_category_image: has_category_image,
                        };
                        if (self.nav_active_index != active_index) {
                            upd_data['data_page'] = 1;
                            upd_data['data_list'] = [];
                            upd_data['data_list_loding_status'] = 1;
                            upd_data['nav_active_item_two_index'] = -1;
                            upd_data['nav_active_item_three_index'] = -1;
                        }
                        if (self.category_show_level != 0) {
                            upd_data['data_list_loding_status'] = temp_category.length == 0 ? 0 : 3;
                            upd_data['data_bottom_line_status'] = true;
                        }
                        self.setData(upd_data);

                        if (self.category_show_level == 0) {
                            self.get_goods_list(1);
                        }

                        if (self.is_first == 1) {
                            var timer = setInterval(function () {
                                if (self.search_height == 0) {
                                    self.search_height_computer();
                                } else {
                                    clearInterval(timer);
                                }
                            }, 500);
                        }

                        self.content_actual_size_handle();
                        self.setData({ is_first: 0 });
                    } else {
                        self.setData({
                            data_list_loding_status: 0,
                            data_list_loding_msg: '暂无分类数据',
                        });
                    }

                    self.setData({
                        share_info: { path: '/pages/goods-category/goods-category' },
                    });
                    setTimeout(function () {
                        app.globalData.page_share_handle(self.share_info);
                    }, 3000);
                }).catch(function () {
                    uni.stopPullDownRefresh();
                    self.setData({
                        data_list_loding_status: 2,
                        data_list_loding_msg: '网络错误',
                    });
                });
            },

            // 构建分类树（适配 mall-app API 返回格式）
            buildCategoryTree(list) {
                var result = [];
                for (var i in list) {
                    var item = list[i];
                    // 过滤空数据项
                    if (!item || !item.id || !item.name) {
                        continue;
                    }
                    result.push({
                        id: item.id,
                        name: item.name,
                        icon: item.iconUrl,
                        big_images: item.iconUrl,
                        realistic_images: item.iconUrl,
                        describe: item.description || '',
                        items: (item.children || []).map(function (child) {
                            return {
                                id: child.id,
                                name: child.name,
                                icon: child.iconUrl,
                                big_images: child.iconUrl,
                                realistic_images: child.iconUrl,
                                items: (child.children || []).map(function (gc) {
                                    return {
                                        id: gc.id,
                                        name: gc.name,
                                        icon: gc.iconUrl,
                                        big_images: gc.iconUrl,
                                        realistic_images: gc.iconUrl
                                    };
                                })
                            };
                        })
                    });
                }
                result.sort(function (a, b) { return a.id - b.id; });
                return result;
            },

            // 将 SpuPageVO 转换为商品列表的渲染结构。
            convertGoodsItem(spu) {
                return {
                    id: spu.id,
                    title: spu.name,
                    images: spu.picUrl,
                    goods_url: '/pages/goods-detail/goods-detail?id=' + spu.id,
                    simple_desc: spu.simpleDesc || '',
                    min_price: spu.price || 0,
                    sales_count: spu.sales || 0,
                    show_field_price_status: 1,
                    show_price_symbol: this.currency_symbol,
                    show_price_unit: '',
                    is_error: 0,
                    error_msg: '',
                    buy_number: 0,
                    is_exist_many_spec: spu.isExistManySpec || 0,
                    buy_min_number: 1,
                    buy_max_number: 0,
                    spec_buy_min_number: 0,
                    spec_buy_max_number: 0,
                    inventory: 99999,
                    inventory_unit: '',
                    default_sku_id: spu.defaultSkuId || null,
                };
            },

            // 内容实际大小处理
            content_actual_size_handle() {
                var footer_height = this.footer_height_value;
                var footer_height_unit = 'px';
                // #ifdef H5
                if(app.globalData.is_pc()) {
                    var system = app.globalData.get_system_info(null, null, true);
                    if(system.windowWidth <= 960) {
                        footer_height *= 2;
                        footer_height_unit = 'rpx';
                    }
                }
                // #endif
                this.setData({
                    category_content_style: 'height:calc(100vh - ' + (this.search_height + this.window_bottom_height)+'px - '+footer_height+footer_height_unit+');',
                });
            },

            // 获取商品列表
            get_goods_list(is_mandatory) {
                if ((is_mandatory || 0) == 0 && this.data_bottom_line_status == true) {
                    uni.stopPullDownRefresh();
                    return false;
                }
                if (this.data_is_loading == 1) return false;
                this.setData({ data_is_loading: 1 });

                var sortItem = this.search_nav_sort_list[this.search_nav_sort_index];
                var queryParams = {
                    pageNum: this.data_page,
                    pageSize: 10,
                    keywords: this.search_keywords_value || '',
                };

                if ((this.data_content || null) != null) {
                    queryParams['categoryId'] = this.data_content['id'];
                    queryParams['categoryLevel'] = 1;
                    if (this.nav_active_item_two_index != -1 && this.data_content.items && this.data_content.items[this.nav_active_item_two_index]) {
                        queryParams['categoryId'] = this.data_content.items[this.nav_active_item_two_index]['id'];
                        queryParams['categoryLevel'] = 2;
                    }
                    if (this.data_three_content != null && this.nav_active_item_three_index != -1 && this.data_three_content.items && this.data_three_content.items[this.nav_active_item_three_index]) {
                        queryParams['categoryId'] = this.data_three_content.items[this.nav_active_item_three_index]['id'];
                        queryParams['categoryLevel'] = 3;
                    }
                }

                if (sortItem.field !== 'default') {
                    queryParams['sortField'] = sortItem.field;
                    queryParams['sort'] = sortItem.sort;
                }

                var self = this;
                listSpuPages(queryParams).then(function (response) {
                    self.setData({ data_is_loading: 0 });
                    uni.stopPullDownRefresh();

                    var data = response.data || {};
                    var rawList = data.list || [];
                    var total = data.total || 0;
                    var list = rawList.map(function(spu) { return self.convertGoodsItem(spu); });

                    if (list.length > 0) {
                        if (self.data_page <= 1) {
                            var temp_data_list = list;
                        } else {
                            var temp_data_list = self.data_list.concat(list);
                        }
                        var pageTotal = Math.ceil(total / 10);
                        self.setData({
                            data_list: temp_data_list,
                            data_total: total,
                            data_page_total: pageTotal,
                            data_list_loding_status: 3,
                            data_page: self.data_page + 1,
                            data_bottom_line_status: temp_data_list.length > 0 && self.data_page > 1 && self.data_page > pageTotal,
                        });
                    } else {
                        self.setData({ data_list_loding_status: 0, data_total: 0 });
                        if (self.data_page <= 1) {
                            self.setData({ data_list: [], data_bottom_line_status: false });
                        }
                    }
                }).catch(function () {
                    self.setData({
                        data_is_loading: 0,
                        data_list_loding_status: 2,
                        data_list_loding_msg: '网络错误',
                    });
                    uni.stopPullDownRefresh();
                });
            },

            // 重置滑动位置
            reset_scroll() {
                this.setData({
                    scroll_top: this.scroll_top_old,
                });
                this.$nextTick(() => {
                    this.setData({
                        scroll_top: 0,
                    });
                });
            },

            // 滑动事件位置记录
            scroll_event(e) {
                this.setData({
                    scroll_top_old: e.detail.scrollTop,
                });
            },

            // 滚动加载
            scroll_lower(e) {
                this.get_goods_list();
            },

            // 导航事件
            nav_event(e) {
                var index = e.currentTarget.dataset.index;
                var two_index = e.currentTarget.dataset.itemtwoindex;
                var three_index = e.currentTarget.dataset.itemthreeindex;
                var temp_data_content = this.category_list[index] || null;
                this.setData({
                    nav_active_index: index,
                    nav_active_item_two_index: two_index,
                    nav_active_item_three_index: three_index,
                    data_content: temp_data_content,
                    data_three_content: null,
                    popup_status: false,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.reset_scroll();
                this.get_goods_list(1);
            },

            // 打开弹窗
            open_popup_event(e) {
                this.setData({
                    popup_status: e,
                });
            },

            // 分类事件
            category_event(e) {
                app.globalData.url_open('/pages/goods-search/goods-search?category_id=' + e.currentTarget.dataset.value);
            },

            // 搜索事件
            search_button_event(e) {
                // 商品列表模式 && 未配置强制跳转搜索页 → 页内搜索
                if (this.category_show_level == 0 && this.category_goods_is_search_alone != 1) {
                    this.setData({
                        search_keywords_value: e || '',
                        data_page: 1,
                        data_list: [],
                        data_list_loding_status: 1,
                        data_bottom_line_status: false
                    });
                    this.get_goods_list(1);
                } else {
                    // 进入搜索页面
                    app.globalData.url_open('/pages/goods-search/goods-search' + ((e || null) == null ? '' : '?keywords=' + e));
                }
            },

            // 商品事件
            goods_event(e) {
                var index = e.currentTarget.dataset.index;
                var goods = this.data_list[index];
                app.globalData.goods_data_cache_handle(goods.id, goods);
                app.globalData.url_event(e);
            },

            // url事件
            url_event(e) {
                app.globalData.url_event(e);
            },



            // 计算搜索框的高度
            search_height_computer() {
                const query = uni.createSelectorQuery();
                query
                    .select('.nav-search')
                    .boundingClientRect((res) => {
                        if ((res || null) != null) {
                            // 获取搜索框高度
                            this.setData({
                                search_height: res.height,
                            });
                            this.content_actual_size_handle();
                        }
                    })
                    .exec();
            },

            // 排序事件
            nav_sort_event(e) {
                var index = e.currentTarget.dataset.index || 0;
                var temp_search_nav_sort = this.search_nav_sort_list;
                var temp_sort = temp_search_nav_sort[index]['sort'] == 'desc' ? 'asc' : 'desc';
                for (var i in temp_search_nav_sort) {
                    if (i != index) {
                        if (temp_search_nav_sort[i]['icon'] != null) {
                            temp_search_nav_sort[i]['icon'] = 'default';
                        }
                        temp_search_nav_sort[i]['sort'] = 'desc';
                    }
                }
                temp_search_nav_sort[index]['sort'] = temp_sort;
                if (temp_search_nav_sort[index]['icon'] != null) {
                    temp_search_nav_sort[index]['icon'] = temp_sort;
                }

                this.setData({
                    search_nav_sort_index: index,
                    search_nav_sort_list: temp_search_nav_sort,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.reset_scroll();
                this.get_goods_list(1);
            },

            // 底部菜单高度
            footer_height_value_event(value) {
                this.setData({
                    footer_height_value: value
                });
                this.content_actual_size_handle();
            }
        },
    };
</script>
<style scoped>
    @import './goods-category.css';
</style>
<style>
    .nav-more-top .more {
        display: flex;
        align-items: center;
        justify-content: center;
        padding-top: 0 !important;
        padding-bottom: 0 !important;
    }
</style>
