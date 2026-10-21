<template>
    <view :class="theme_view">
        <scroll-view :scroll-y="true" class="scroll-box" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="data_list.length > 0" class="padding-horizontal-main padding-top-main">
                <view class="flex-row jc-sb align-c padding-bottom-main">
                    <text class="cr-grey text-size-sm">{{ $t('user-goods-browse-page.user-goods-browse-page.record_count') }} {{ data_list.length }} {{ $t('user-goods-browse-page.user-goods-browse-page.records') }}</text>
                    <text class="cr-grey text-size-sm cp" @tap="clear_all">{{ $t('user-goods-browse-page.user-goods-browse-page.clear_all') }}</text>
                </view>
                <view v-for="(item, index) in data_list" :key="index" class="padding-main border-radius-main oh bg-white pr spacing-mb">
                    <view :data-value="'/pages/goods-detail/goods-detail?id=' + item.id" @tap="url_event" class="cp">
                        <image class="goods-image fl radius" :src="item.picUrl" mode="aspectFill"></image>
                        <view class="goods-base">
                            <view class="goods-title multi-text padding-right-xxxxl">{{item.name}}</view>
                            <view class="oh margin-top-sm">
                                <text class="sales-price">{{currency_symbol}}{{item.price}}</text>
                                <text v-if="item.unit" class="cr-grey text-size-xs margin-left-sm">{{item.unit}}</text>
                                <text v-if="item.originalPrice > 0" class="original-price margin-left-sm">{{currency_symbol}}{{item.originalPrice}}</text>
                            </view>
                        </view>
                    </view>
                    <view class="dis-inline-block pa top-xxxxl right-xxxxl" @tap="item_event" :data-value="item.browseId" :data-index="index">
                        <iconfont name="icon-ellipsis" size="40rpx" color="#999"></iconfont>
                    </view>
                </view>
            </view>
            <view v-else>
                <component-no-data :propStatus="data_list_loding_status"></component-no-data>
            </view>
            <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
        </scroll-view>
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import { request } from '@/config/request';
    import componentCommon from '@/components/common/common';
    import componentNoData from "@/components/no-data/no-data";
    import componentBottomLine from "@/components/bottom-line/bottom-line";
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list: [],
                data_total: 0,
                data_page: 1,
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                data_is_loading: 0,
                currency_symbol: app.globalData.currency_symbol()
            };
        },
        components: {
            componentCommon,
            componentNoData,
            componentBottomLine
        },
        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
        },
        onShow() {
            app.globalData.page_event_onshow_handle();
            this.init();
            this.init_config();
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
            app.globalData.page_share_handle();
        },
        onPullDownRefresh() {
            this.setData({ data_page: 1 });
            this.get_data_list(1);
        },
        methods: {
            init_config(status) {
                if ((status || false) == true) {
                    this.setData({ currency_symbol: app.globalData.get_config('currency_symbol') });
                } else {
                    app.globalData.is_config(this, 'init_config');
                }
            },
            init() {
                var user = app.globalData.get_user_info(this, "init");
                if (user != false) {
                    this.get_data_list();
                } else {
                    this.setData({ data_list_loding_status: 0, data_bottom_line_status: false });
                }
            },
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
                var self = this;
                request({
                    url: '/mall-ums/app-api/v1/goods-browse',
                    method: 'POST',
                    headers: { auth: true },
                    data: { pageNum: this.data_page, pageSize: 20 }
                }).then(function(res) {
                    if (self.data_page > 1) uni.hideLoading();
                    uni.stopPullDownRefresh();
                    var list = res.data.list || [];
                    if (list.length > 0) {
                        var temp = self.data_page <= 1 ? list : (self.data_list || []).concat(list);
                        self.setData({
                            data_list: temp,
                            data_total: res.data.total,
                            data_list_loding_status: 3,
                            data_page: self.data_page + 1,
                            data_is_loading: 0,
                            data_bottom_line_status: temp.length >= res.data.total
                        });
                    } else {
                        self.setData({ data_list_loding_status: 0, data_is_loading: 0 });
                        if (self.data_page <= 1) {
                            self.setData({ data_list: [], data_bottom_line_status: false });
                        }
                    }
                }).catch(function() {
                    if (self.data_page > 1) uni.hideLoading();
                    uni.stopPullDownRefresh();
                    self.setData({ data_list_loding_status: 2, data_is_loading: 0 });
                });
            },
            scroll_lower(e) {
                this.get_data_list();
            },
            item_event(e) {
                var self = this;
                var id = e.currentTarget.dataset.value;
                var index = e.currentTarget.dataset.index;
                wx.showActionSheet({
                    itemList: [self.$t('cart.cart.8tt78i'), self.$t('common.del_record')],
                    success: function(res) {
                        if (res.tapIndex == 0) {
                            self.goods_favor_handle(self, index);
                        } else {
                            self.goods_delete_handle(self, id, index);
                        }
                    }
                });
            },
            goods_delete_handle(self, id, index) {
                uni.showLoading({ title: self.$t('common.processing_in_text') });
                request({
                    url: '/mall-ums/app-api/v1/goods-browse/' + id,
                    method: 'DELETE',
                    headers: { auth: true }
                }).then(function() {
                    uni.hideLoading();
                    var temp = self.data_list;
                    temp.splice(index, 1);
                    self.setData({ data_list: temp });
                    if (temp.length == 0) {
                        self.setData({ data_list_loding_status: 0, data_bottom_line_status: false });
                    }
                }).catch(function() {
                    uni.hideLoading();
                });
            },
            goods_favor_handle(self, index) {
                var spuId = self.data_list[index].id;
                uni.showLoading({ title: self.$t('common.processing_in_text') });
                request({
                    url: '/mall-ums/app-api/v1/favor/toggle/GOODS/' + spuId,
                    method: 'POST',
                    headers: { auth: true }
                }).then(function() {
                    uni.hideLoading();
                }).catch(function() {
                    uni.hideLoading();
                });
            },
            url_event(e) {
                app.globalData.url_event(e);
            },

            // 清除全部
            clear_all() {
                var self = this;
                wx.showModal({
                    title: self.$t('user-goods-browse-page.user-goods-browse-page.clear_confirm_title'),
                    content: self.$t('user-goods-browse-page.user-goods-browse-page.clear_confirm_content'),
                    cancelText: self.$t('common.cancel'),
                    confirmText: self.$t('common.confirm'),
                    success: function(res) {
                        if (res.confirm) {
                            request({
                                url: '/mall-ums/app-api/v1/goods-browse/clear',
                                method: 'DELETE',
                                headers: { auth: true }
                            }).then(function() {
                                self.setData({
                                    data_list: [],
                                    data_list_loding_status: 0,
                                    data_bottom_line_status: false
                                });
                            });
                        }
                    }
                });
            }
        }
    };
</script>
<style>
    @import './user-goods-browse.css';
</style>
