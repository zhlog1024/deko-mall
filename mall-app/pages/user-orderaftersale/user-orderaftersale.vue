<template>
    <view :class="theme_view">
        <!-- 搜索 -->
        <component-nav-back :propFixed="false" propClass="bg-white cr-black" propColor="#333">
            <template slot="right" :class="is_mp_env ? 'top-search-width' : ''">
                <view class="margin-left-main" :class="is_mp_env ? '' : 'flex-1 flex-width'">
                    <component-search @onsearch="search_button_event" :propDefaultValue="search_keywords" :propIsOnEvent="true" :propIsRequired="false" propIconColor="#ccc" propPlaceholderClass="cr-grey-c" propBgColor="#f6f6f6"></component-search>
                </view>
            </template>
        </component-nav-back>
        <!-- 导航 -->
        <view class="nav-base bg-white">
            <block v-for="(item, index) in nav_status_list" :key="index">
                <view v-if="nav_status_index == index" class="item fl tc cr-main nav-active-line" :data-index="index" @tap="nav_event">{{ item.name }}</view>
                <view v-else class="item fl tc" :data-index="index" @tap="nav_event">{{ item.name }}</view>
            </block>
        </view>
        <!-- 订单列表 -->
        <scroll-view :scroll-y="true" class="scroll-box" :style="content_style" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="data_list.length > 0" class="padding-horizontal-main padding-top-main">
                <view v-for="(item, index) in data_list" :key="index" class="list-item padding-horizontal-main padding-top-main border-radius-main bg-white oh spacing-mb">
                    <view class="item-base oh br-b padding-bottom-main">
                        <text class="cr-base">{{ item.afterSaleNo }}</text>
                        <text class="fr cr-red">{{ item.status_text }}</text>
                    </view>
                    <view :data-value="'/pages/user-orderaftersale-detail/user-orderaftersale-detail?id=' + item.id" @tap="url_event" class="goods-item oh cp">
                        <image class="goods-image fl radius" :src="item.order_data.items.images" mode="aspectFill"></image>
                        <view class="goods-base pr">
                            <view class="multi-text">{{ item.order_data.items.title }}</view>
                            <view v-if="item.order_data.items.spec != null" class="margin-top-sm">
                                <block v-for="(sv, si) in item.order_data.items.spec" :key="si">
                                    <text v-if="si > 0" class="cr-grey padding-left-xs padding-right-xs">;</text>
                                    <text class="cr-grey">{{ sv.value }}</text>
                                </block>
                            </view>
                            <view class="margin-top-sm">
                                <text class="fw-b">{{ item.order_data.currency_data.currency_symbol }}{{ item.order_data.items.price }}</text>
                                <text class="margin-left-sm">x{{ item.order_data.items.buy_number }}</text>
                            </view>
                        </view>
                    </view>
                    <view class="item-describe padding-vertical-main">
                        <text class="cr-base">{{ item.type_text }}</text>
                        <text class="cr-grey margin-left-sm margin-right-sm">/</text>
                        <text class="cr-base">{{ item.reason }}</text>
                        <block v-if="item.price > 0">
                            <text class="cr-grey margin-left-sm margin-right-sm">/</text>
                            <text class="sales-price text-size-sm">{{ item.order_data.currency_data.currency_symbol }}{{ item.price }}</text>
                        </block>
                        <block v-if="item.number > 0">
                            <text class="cr-grey margin-left-sm margin-right-sm">/</text>
                            <text class="cr-main fw-b">{{ item.number }}</text>
                        </block>
                    </view>
                    <view v-if="item.status == 1 || item.status == 3" class="item-operation tr br-t padding-vertical-main">
                        <button v-if="item.status == 1 || item.status == 3" class="br-yellow cr-yellow bg-white round" type="default" size="mini" @tap="cancel_event" :data-value="item.id" :data-index="index" hover-class="none">{{$t('common.cancel')}}</button>
                        <button v-if="item.status == 3 && item.type == 2" class="br-green cr-green bg-white round" type="default" size="mini" @tap="delivery_event" :data-id="item.id" :data-index="index" hover-class="none">{{$t('user-orderaftersale.user-orderaftersale.10c251')}}</button>
                    </view>
                </view>
            </view>
            <view v-else>
                <!-- 提示信息 -->
                <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
            </view>

            <!-- 结尾 -->
            <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
        </scroll-view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNavBack from '@/components/nav-back/nav-back';
    import componentSearch from '@/components/search/search';
    import { getAfterSaleList, cancelAfterSale } from '@/api/oms/afterSale.js'
    import componentNoData from "@/components/no-data/no-data";
    import componentBottomLine from "@/components/bottom-line/bottom-line";
    
    // 状态栏高度
    var bar_height = parseInt(app.globalData.get_system_info('statusBarHeight', 0, true));
    // #ifdef MP-TOUTIAO || H5
    bar_height = 0;
    // #endif
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                params: null,
                data_list_loding_status: 1,
                data_list_loding_msg: "",
                data_bottom_line_status: false,
                data_is_loading: 0,
                client_type: app.globalData.application_client_type(),
                status_bar_height: bar_height,
                is_mp_env: false,
                // #ifdef MP-WEIXIN || MP-BAIDU || MP-ALIPAY || MP-QQ || MP-KUAISHOU
                is_mp_env: true,
                // #endif
                // 接口数据
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                search_keywords: '',
                // 售后导航使用后端状态码。
                nav_status_list: [
                    { name: this.$t('common.all'), value: '-1' },
                    { name: this.$t('user-orderaftersale.user-orderaftersale.2kzi0t'), value: '1' },
                    { name: this.$t('user-orderaftersale.user-orderaftersale.1kcn16'), value: '3' },
                    { name: this.$t('user-orderaftersale.user-orderaftersale.3wggcu'), value: '6' },
                    { name: this.$t('order.order.15lr5l'), value: '8' },
                    { name: this.$t('user-orderaftersale.user-orderaftersale.1p5456'), value: '2' },
                    { name: this.$t('order.order.1k98tk'), value: '10' },
                ],
                nav_status_index: 0,
            };
        },

        components: {
            componentCommon,
            componentNavBack,
            componentSearch,
            componentNoData,
            componentBottomLine,
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            // 参数处理
            params = app.globalData.launch_params_handle(params);

            // 是否指定状态
            var nav_status_index = 0;
            if ((params.status || null) != null) {
                for (var i in this.nav_status_list) {
                    if (this.nav_status_list[i]["value"] == params.status) {
                        nav_status_index = i;
                        break;
                    }
                }
            }
            this.setData({
                params: params,
                nav_status_index: nav_status_index,
                search_keywords: params.keywords || '',
                content_style: 'height: calc(100vh - 80rpx - '+(this.status_bar_height+(this.client_type == 'h5' ? 55 : 50))+'px);',
            });

            // 数据加载
            this.init();
        },

        onShow() {
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }

            // 分享菜单处理
            app.globalData.page_share_handle();
        },

        // 下拉刷新
        onPullDownRefresh() {
            this.setData({
                data_page: 1,
            });
            this.get_data_list(1);
        },

        methods: {
            // 获取数据
            init() {
                var user = app.globalData.get_user_info(this, "init");
                if (user != false) {
                    this.get_data_list();
                } else {
                    this.setData({
                        data_list_loding_status: 0,
                        data_bottom_line_status: false,
                    });
                }
            },

            // 获取数据
            get_data_list(is_mandatory) {
                if ((is_mandatory || 0) == 0 && this.data_bottom_line_status == true) {
                    uni.stopPullDownRefresh();
                    return false;
                }
                if (this.data_is_loading == 1) return false;
                this.setData({ data_is_loading: 1, data_list_loding_status: 1 });
                if (this.data_page > 1) {
                    uni.showLoading({ title: this.$t('common.loading_in_text') });
                }
                var status = (this.nav_status_list[this.nav_status_index] || null) == null ? -1 : this.nav_status_list[this.nav_status_index]["value"];
                getAfterSaleList({
                    pageNum: this.data_page,
                    pageSize: 10,
                    status: status !== '-1' ? [parseInt(status)] : undefined,
                    keywords: this.search_keywords || undefined,
                }).then((res) => {
                    if (this.data_page > 1) uni.hideLoading();
                    uni.stopPullDownRefresh();
                    if (res.code === '00000' && res.data && res.data.list) {
                        var list = res.data.list || [];
                        var mapped = list.map(function(o) { return mapAfterSale(o); });
                        var temp = this.data_page <= 1 ? mapped : (this.data_list || []).concat(mapped);
                        var noMore = list.length < 10;
                        this.setData({
                            data_list: temp,
                            data_total: res.data.total || 0,
                            data_page: this.data_page + 1,
                            data_page_total: Math.ceil((res.data.total || 0) / 10),
                            data_list_loding_status: temp.length > 0 ? 3 : 0,
                            data_is_loading: 0,
                            data_bottom_line_status: temp.length > 0 && this.data_page > 1 && noMore,
                        });
                    } else {
                        this.setData({ data_list_loding_status: 0, data_is_loading: 0 });
                        if (this.data_page <= 1) this.setData({ data_list: [], data_bottom_line_status: false });
                    }
                }).catch(() => {
                    if (this.data_page > 1) uni.hideLoading();
                    uni.stopPullDownRefresh();
                    this.setData({ data_list_loding_status: 2, data_is_loading: 0 });
                    app.globalData.showToast(this.$t('common.internet_error_tips'));
                });
            },

            // 滚动加载
            scroll_lower(e) {
                this.get_data_list();
            },

            // 导航事件
            nav_event(e) {
                this.setData({
                    nav_status_index: e.currentTarget.dataset.index || 0,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.get_data_list(1);
            },

            // 取消
            cancel_event(e) {
                uni.showModal({
                    title: this.$t('common.warm_tips'),
                    content: this.$t('common.cancel_confirm_tips'),
                    confirmText: this.$t('common.confirm'),
                    cancelText: this.$t('common.no'),
                    success: (result) => {
                        if (result.confirm) {
                            var id = e.currentTarget.dataset.value || 0;
                            var index = e.currentTarget.dataset.index || 0;
                            uni.showLoading({ title: this.$t('common.processing_in_text') });
                            cancelAfterSale(id).then(() => {
                                uni.hideLoading();
                                var temp = this.data_list;
                                temp.splice(index, 1);
                                this.setData({ data_list: temp, data_list_loding_status: temp.length == 0 ? 0 : 3, data_bottom_line_status: temp.length == 0 ? false : this.data_bottom_line_status });
                                app.globalData.showToast(this.$t('common.success'));
                            }).catch(() => {
                                uni.hideLoading();
                                app.globalData.showToast(this.$t('common.fail'));
                            });
                        }
                    },
                });
            },

            // 退货
            delivery_event(e) {
                var id = e.currentTarget.dataset.id || 0;
                if (id == 0) {
                    app.globalData.showToast(this.$t('user-order-detail.user-order-detail.5k6k56'));
                    return false;
                }

                // 进入已有售后详情页，打开退货物流弹窗
                app.globalData.url_open('/pages/user-orderaftersale-detail/user-orderaftersale-detail?id=' + id + '&is_delivery_popup=1');
            },

            // url事件
            url_event(e) {
                app.globalData.url_event(e);
            },

            // 关键字搜索
            search_button_event(e) {
                this.setData({
                    search_keywords: e,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.get_data_list(1);
            },
        },
    };

    function mapAfterSale(o) {
        var firstItem = (o.items && o.items.length > 0) ? o.items[0] : {};
        return Object.assign({}, o, {
            order_data: { items: { title: firstItem.skuName||'', images: firstItem.picUrl||'', price: firstItem.price||0, buy_number: firstItem.quantity||1, spec: null }, currency_data:{currency_symbol:'¥'} },
            add_time: o.createTime||'', status_text: o.statusLabel||'未知',
            type_text: o.afterSaleType==2?'退货退款':'仅退款', type: o.afterSaleType,
            reason: o.reason||'', price: o.refundAmount||0, number: firstItem.quantity||0,
            status: o.status, is_cancel: (o.status==1||o.status==3)?1:0,
        });
    }
</script>
<style>
    @import "./user-orderaftersale.css";
</style>
