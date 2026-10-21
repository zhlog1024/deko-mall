<template>
    <view :class="theme_view">
        <block v-if="data_list_loding_status == 1">
            <component-no-data :propStatus="data_list_loding_status"></component-no-data>
        </block>
        <block v-else>
            <!-- 评分 -->
            <view v-if="goods_score != null" class="score-container oh padding-main">
                <view class="bg-white border-radius-main padding-main flex-row jc-sb align-c">
                    <view class="score tc">
                        <view class="cr-base">{{ $t('goods-comment-page.goods-comment-page.goods_score') }}</view>
                        <view class="value cr-main">{{ goods_score.avg || "0.0" }}</view>
                    </view>
                    <view class="progress tc border-radius-main flex-1 flex-width">
                        <block v-if="goods_score.total > 0">
                            <block v-for="(item, index) in goods_score.rating" :key="index">
                                <view v-if="item.portion > 0" :class="'progress-bar ' + progress_class[index]" :style="'width: ' + item.portion + '%;'">{{ item.name }}</view>
                            </block>
                        </block>
                        <text v-else class="cr-grey">{{ $t('goods-comment-page.goods-comment-page.no_score') }}</text>
                    </view>
                </view>
            </view>

            <!-- 列表 -->
            <scroll-view :scroll-y="true" class="scroll-box" @scrolltolower="scroll_lower" lower-threshold="60">
                <view class="padding-horizontal-main goods-comment">
                    <component-goods-comments :propData="data_list" :propIsReply="true" propClass="bg-white padding-main border-radius-main"></component-goods-comments>
                    <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
                </view>
            </scroll-view>
        </block>

        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from "@/components/no-data/no-data";
    import componentBottomLine from "@/components/bottom-line/bottom-line";
    import componentGoodsComments from "@/pages/goods-detail/components/goods-comments/goods-comments";
    import { getGoodsComments, getGoodsCommentScore } from '@/api/pms/comment';

    var static_url = app.globalData.get_static_url("home");
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                static_url: static_url,
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                data_is_loading: 0,
                data_list: [],
                data_page_total: 0,
                data_page: 1,
                goods_score: null,
                params: null,
                progress_class: ["progress-bar-danger", "progress-bar-warning", "progress-bar-secondary", "", "progress-bar-success"],
            };
        },
        components: {
            componentCommon,
            componentNoData,
            componentBottomLine,
            componentGoodsComments,
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            this.setData({
                params: params,
            });
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
                data_page: 1,
            });
            this.get_data_list(1);
        },

        methods: {
            init() {
                this.get_goods_score();
                this.get_data_list();
            },

            get_goods_score() {
                var self = this;
                getGoodsCommentScore(this.params.goods_id).then(function(res) {
                    if (res.code === '00000') {
                        self.setData({
                            goods_score: res.data || null,
                        });
                    }
                }).catch(function() {});
            },

            get_data_list(is_mandatory) {
                if ((this.params.goods_id || null) == null) {
                    uni.stopPullDownRefresh();
                    this.setData({
                        data_bottom_line_status: false,
                        data_list_loding_status: 2,
                    });
                    return;
                }

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
                self.setData({ data_is_loading: 1 });

                getGoodsComments(this.params.goods_id, {
                    pageNum: this.data_page,
                    pageSize: 10
                }).then(function(res) {
                    uni.stopPullDownRefresh();
                    if (res.code === '00000') {
                        var data = res.data;
                        var records = (data.records || []).map(function(item) {
                            return {
                                user: {
                                    avatar: item.isAnonymous ? '' : (item.memberAvatar || ''),
                                    user_name_view: item.isAnonymous ? self.$t('goods-comment-page.goods-comment-page.anonymous_user') : (item.memberName || self.$t('goods-comment-page.goods-comment-page.user'))
                                },
                                rating: item.rating,
                                content: item.content,
                                images: item.images || [],
                                add_time_date: item.createTime || '',
                                msg: item.skuName || '',
                                is_reply: item.isReplied || 0,
                                reply: item.replyContent || ''
                            };
                        });
                        if (records.length > 0) {
                            if (self.data_page <= 1) {
                                var temp_data_list = records;
                            } else {
                                var temp_data_list = self.data_list || [];
                                var temp_data = records;
                                for (var i in temp_data) {
                                    temp_data_list.push(temp_data[i]);
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
                    } else {
                        self.setData({
                            data_list_loding_status: 0,
                            data_is_loading: 0,
                        });
                    }
                }).catch(function() {
                    uni.stopPullDownRefresh();
                    self.setData({
                        data_list_loding_status: 2,
                        data_is_loading: 0,
                    });
                });
            },

            scroll_lower(e) {
                this.get_data_list();
            },

            comment_images_show_event(e) {
                var index = e.currentTarget.dataset.index;
                var ix = e.currentTarget.dataset.ix;
                uni.previewImage({
                    current: this.data_list[index]["images"][ix],
                    urls: this.data_list[index]["images"],
                });
            },
        },
    };
</script>
<style>
    @import "./goods-comment.css";
</style>
