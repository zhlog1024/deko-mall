<template>
    <view :class="theme_view">
        <scroll-view :scroll-y="true" class="scroll-box" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="data_list.length > 0" class="data-list padding-horizontal-main padding-top-main">
                <view v-for="(item, index) in data_list" :key="index" class="item padding-main border-radius-main oh bg-white spacing-mb">
                    <view class="base oh br-b padding-bottom-main">
                        <text class="cr-base">{{ item.createTime }}</text>
                        <text class="fr cr-main">{{ ratingText(item.rating) }}</text>
                    </view>
                    <view class="content margin-top">
                        <view class="margin-top-xs flex-row align-c">
                            <view class="cr-grey margin-right-xl">{{ $t('user-goods-comments-page.user-goods-comments-page.product') }}</view>
                            <view class="cr-base flex-1 flex-width flex-row align-c">
                                <image class="br radius margin-right-sm goods-img" :src="item.images && item.images[0] || ''" mode="aspectFit"></image>
                                <text class="single-text flex-1 flex-width">{{ item.spuName }}</text>
                            </view>
                        </view>
                        <view class="margin-top-xs flex-row align-c">
                            <view class="cr-grey margin-right-xl">{{ $t('user-goods-comments-page.user-goods-comments-page.review_content') }}</view>
                            <view class="cr-base flex-1 flex-width">{{ item.content }}</view>
                        </view>
                        <view class="margin-top-xs flex-row align-c" v-if="item.replyContent">
                            <view class="cr-grey margin-right-xl">{{ $t('user-goods-comments-page.user-goods-comments-page.merchant_reply') }}</view>
                            <view class="cr-main-pair flex-1 flex-width">{{ item.replyContent }}</view>
                        </view>
                        <view class="margin-top-xs flex-row align-c">
                            <view class="cr-grey margin-right-xl">{{ $t('user-goods-comments-page.user-goods-comments-page.status') }}</view>
                            <view class="cr-base flex-1 flex-width">{{ item.status === 0 ? '待审核' : item.status === 1 ? '已通过' : '已拒绝' }}</view>
                        </view>
                    </view>
                    <view class="item-operation tr br-t padding-top-main margin-top-main" v-if="false">
                        <button class="round bg-white br-red cr-red margin-left-lg" type="default" size="mini" hover-class="none" @tap="delete_event" :data-index="index" :data-id="item.id">{{ $t('user-goods-comments-page.user-goods-comments-page.delete') }}</button>
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
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import componentBottomLine from '@/components/bottom-line/bottom-line';
    import { getMyComments } from '@/api/pms/comment';
    import { request } from '@/config/request';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                data_is_loading: 0,
                params: null,
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentBottomLine,
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            this.setData({ params: params });
        },

        onShow() {
            app.globalData.page_event_onshow_handle();
            this.init();
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        onPullDownRefresh() {
            this.setData({ data_page: 1 });
            this.get_data_list(1);
        },

        methods: {
            init() {
                var user = app.globalData.get_user_info(this, 'init');
                if (user != false) {
                    this.setData({ data_page: 1 });
                    this.get_data_list(1);
                } else {
                    this.setData({
                        data_list_loding_status: 0,
                        data_bottom_line_status: false,
                    });
                }
            },

            get_data_list(is_mandatory) {
                if ((is_mandatory || 0) == 0) {
                    if (this.data_bottom_line_status == true) {
                        uni.stopPullDownRefresh();
                        return;
                    }
                }
                if (this.data_is_loading == 1) {
                    return;
                }
                var self = this;
                self.setData({ data_is_loading: 1, data_list_loding_status: 1 });

                getMyComments({ pageNum: this.data_page, pageSize: 10 }).then(function(res) {
                    uni.stopPullDownRefresh();
                    if (res.code === '00000') {
                        var data = res.data;
                        if (data.records.length > 0) {
                            if (self.data_page <= 1) {
                                var temp_data_list = data.records;
                            } else {
                                var temp_data_list = self.data_list || [];
                                for (var i in data.records) {
                                    temp_data_list.push(data.records[i]);
                                }
                            }
                            self.setData({
                                data_list: temp_data_list,
                                data_total: data.total,
                                data_page_total: data.pages,
                                data_list_loding_status: 3,
                                data_page: self.data_page + 1,
                                data_is_loading: 0,
                            });
                            self.setData({
                                data_bottom_line_status: self.data_list.length > 0 && self.data_page > 1 && self.data_page > data.pages,
                            });
                        } else {
                            self.setData({ data_list_loding_status: 0, data_is_loading: 0 });
                            if (self.data_page <= 1) {
                                self.setData({ data_list: [], data_bottom_line_status: false });
                            }
                        }
                    } else {
                        self.setData({ data_list_loding_status: 0, data_is_loading: 0 });
                    }
                }).catch(function() {
                    uni.stopPullDownRefresh();
                    self.setData({ data_list_loding_status: 2, data_is_loading: 0 });
                });
            },

            scroll_lower(e) {
                this.get_data_list();
            },

            delete_event(e) {
                var index = e.currentTarget.dataset.index || 0;
                var id = e.currentTarget.dataset.id;
                var temp_data = this.data_list;
                if (!id) return;

                uni.showModal({
                    title: '提示',
                    content: '确认删除该评价？',
                    confirmText: '确认',
                    cancelText: '取消',
                    success: function(result) {
                        if (result.confirm) {
                            uni.showLoading({ title: '处理中...' });
                            request({
                                url: '/mall-pms/app-api/v1/member/comments/' + id,
                                method: 'DELETE'
                            }).then(function(res) {
                                uni.hideLoading();
                                if (res.code === '00000') {
                                    temp_data.splice(index, 1);
                                    self.setData({ data_list: temp_data });
                                    app.globalData.showToast('删除成功', 'success');
                                } else {
                                    app.globalData.showToast(res.data.msg || '删除失败');
                                }
                            }).catch(function() {
                                uni.hideLoading();
                                app.globalData.showToast('网络错误');
                            });
                        }
                    },
                });
            },

            ratingText(rating) {
                var map = { 1: '非常差', 2: '差', 3: '一般', 4: '好', 5: '非常好' };
                return map[rating] || '';
            },
        },
    };
</script>
<style scoped>
    .goods-img {
        width: 80rpx;
        height: 80rpx;
    }
    .scroll-box {
        height: calc(100vh);
    }
    .single-text {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .item {
        font-size: 26rpx;
    }
</style>
