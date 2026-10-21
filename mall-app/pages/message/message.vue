<template>
    <view :class="theme_view">
        <scroll-view :scroll-y="true" class="scroll-box" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="data_list.length > 0" class="padding-horizontal-main padding-top-main">
                <view v-for="(item, index) in data_list" :key="index" class="padding-main border-radius-main bg-white oh spacing-mb">
                    <view class="oh">
                        <text class="fw-b">{{item.title}}</text>
                        <text class="fr cr-base">{{item.createTime}}</text>
                    </view>
                    <view class="web-html-content cr-grey margin-top-lg">
                        <mp-html :content="item.detail" />
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
                data_page_total: 0,
                data_page: 1,
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                data_is_loading: 0
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

            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }

            app.globalData.page_share_handle();
        },

        onPullDownRefresh() {
            this.setData({
                data_page: 1
            });
            this.get_data_list(1);
        },

        methods: {
            init() {
                var user = app.globalData.get_user_info(this, "init");
                if (user != false) {
                    this.get_data_list();
                } else {
                    this.setData({
                        data_list_loding_status: 0,
                        data_bottom_line_status: false
                    });
                }
            },

            get_data_list(is_mandatory) {
                if ((is_mandatory || 0) == 0) {
                    if (this.data_bottom_line_status == true) {
                        uni.stopPullDownRefresh();
                        return false;
                    }
                }

                if(this.data_is_loading == 1) {
                    return false;
                }
                this.setData({
                    data_is_loading: 1,
                    data_list_loding_status: 1
                });

                if(this.data_page > 1) {
                    uni.showLoading({
                        title: this.$t('common.loading_in_text'),
                    });
                }

                var self = this;
                request({
                    url: '/mall-ums/app-api/v1/messages',
                    method: 'POST',
                    headers: { auth: true },
                    data: {
                        pageNum: this.data_page,
                        pageSize: 20
                    }
                }).then(function(res) {
                    if(self.data_page > 1) {
                        uni.hideLoading();
                    }
                    uni.stopPullDownRefresh();

                    var data = res.data;
                    var list = data.list || [];
                    if (list.length > 0) {
                        if (self.data_page <= 1) {
                            var temp_data_list = list;
                        } else {
                            var temp_data_list = self.data_list || [];
                            for (var i in list) {
                                temp_data_list.push(list[i]);
                            }
                        }
                        var pageSize = 20;
                        var pageTotal = Math.ceil(data.total / pageSize);
                        self.setData({
                            data_list: temp_data_list,
                            data_total: data.total,
                            data_page_total: pageTotal,
                            data_list_loding_status: 3,
                            data_page: self.data_page + 1,
                            data_is_loading: 0
                        });

                        self.setData({
                            data_bottom_line_status: self.data_list.length >= data.total,
                        });
                    } else {
                        self.setData({
                            data_list_loding_status: 0,
                            data_is_loading: 0,
                        });
                        if (self.data_page <= 1) {
                            self.setData({
                                data_list: [],
                                data_bottom_line_status: false,
                            });
                        }
                    }
                }).catch(function() {
                    if(self.data_page > 1) {
                        uni.hideLoading();
                    }
                    uni.stopPullDownRefresh();
                    self.setData({
                        data_list_loding_status: 2,
                        data_is_loading: 0
                    });
                });
            },

            scroll_lower(e) {
                this.get_data_list();
            }
        }
    };
</script>
<style>
</style>
