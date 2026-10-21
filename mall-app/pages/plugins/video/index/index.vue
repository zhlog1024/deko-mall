<template>
    <view :class="theme_view">
        <!-- 视频列表 -->
        <view v-if="video_list.length > 0" class="padding-main">
            <view class="video-list">
                <view v-for="(item, index) in video_list" :key="index" class="video-list-item" @tap="video_item_event" :data-value="item.url">
                    <view class="pr oh video-list-cover">
                        <image :src="item.cover" class="video-list-img" mode="aspectFill"></image>
                        <view v-if="item.duration != null && item.duration > 0" class="video-list-duration">{{ item.duration_text }}</view>
                    </view>
                    <view class="video-list-title text-line-2">{{ item.title }}</view>
                    <view class="cr-grey-9 text-size-xs flex-row align-c margin-top-xs">
                        <iconfont name="icon-eye" size="22rpx" color="#999" propContainerDisplay="flex"></iconfont>
                        <text class="margin-left-xs">{{ item.access_count }}</text>
                        <view class="fw-b padding-horizontal-xs">·</view>
                        <text>{{ item.add_time_date }}</text>
                        <text v-if="parseInt(item.isRecommended || 0) == 1" class="video-list-tag margin-left-sm">推荐</text>
                        <text v-if="parseInt(item.isHot || 0) == 1" class="video-list-tag video-list-tag-hot margin-left-sm">热门</text>
                    </view>
                </view>
            </view>
            <!-- 加载更多状态 -->
            <view v-if="video_loading_more" class="tc cr-grey-9 text-size-xs padding-vertical-main">加载中...</view>
            <view v-else-if="video_list.length >= video_total && video_total > 0" class="tc cr-grey-9 text-size-xs padding-vertical-main">没有更多了</view>
        </view>
        <!-- 加载中/错误/无数据 -->
        <component-no-data v-else :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import { listVideos } from '@/api/cms/video.js';
    import { time_stamp } from '@/common/js/common/common.js';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                // 列表数据
                video_list: [],
                video_total: 0,
                video_page: 1,
                video_page_size: 10,
                video_loading_more: false,
                // 列表参数
                keyword: '',
                is_recommended: '',
                is_hot: '',
            };
        },
        components: {
            componentCommon,
            componentNoData,
        },
        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            // 设置参数（keyword 搜索 / isRecommended / isHot 运营入口筛选）
            params = app.globalData.launch_params_handle(params);
            this.setData({
                keyword: params.keyword || '',
                is_recommended: params.isRecommended || '',
                is_hot: params.isHot || '',
            });

            // 数据加载
            this.get_data_list(1);
        },
        onShow() {
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },
        // 下拉刷新
        onPullDownRefresh() {
            this.get_data_list(1);
        },
        // 触底加载更多
        onReachBottom() {
            if (this.video_loading_more || this.video_list.length >= this.video_total) {
                return false;
            }
            this.get_data_list(this.video_page + 1);
        },
        methods: {
            // 时长格式化（秒 → 时:分:秒 / 分:秒）
            format_duration(seconds) {
                var num = parseInt(seconds) || 0;
                if (num <= 0) {
                    return '';
                }
                var hour = Math.floor(num / 3600);
                var minute = Math.floor((num % 3600) / 60);
                var second = num % 60;
                var minute_text = minute < 10 ? '0' + minute : String(minute);
                var second_text = second < 10 ? '0' + second : String(second);
                if (hour > 0) {
                    var hour_text = hour < 10 ? '0' + hour : String(hour);
                    return hour_text + ':' + minute_text + ':' + second_text;
                }
                return minute_text + ':' + second_text;
            },
            // 列表行字段适配（mall VO camelCase → 展示字段）
            video_item_handle(item) {
                var row = item || {};
                var video_id = row.id != null ? row.id : '';
                return {
                    id: video_id,
                    url: video_id != null && video_id !== '' ? '/pages/plugins/video/detail/detail?id=' + video_id : '',
                    title: row.title || '',
                    cover: row.cover || '',
                    duration: row.duration != null ? row.duration : 0,
                    duration_text: this.format_duration(row.duration),
                    access_count: row.accessCount != null ? row.accessCount : (row.access_count || 0),
                    isRecommended: row.isRecommended != null ? row.isRecommended : (row.is_recommended || 0),
                    isHot: row.isHot != null ? row.isHot : (row.is_hot || 0),
                    add_time_date: time_stamp(row.createTime),
                };
            },
            // 分页加载列表
            get_data_list(page) {
                var is_first = page <= 1;
                if (is_first) {
                    this.setData({
                        data_list_loding_status: 1,
                    });
                } else {
                    this.setData({
                        video_loading_more: true,
                    });
                }
                var params_data = {
                    pageNum: page,
                    pageSize: this.video_page_size,
                };
                if (this.keyword != '') {
                    params_data['keyword'] = this.keyword;
                }
                if (this.is_recommended != '') {
                    params_data['isRecommended'] = parseInt(this.is_recommended);
                }
                if (this.is_hot != '') {
                    params_data['isHot'] = parseInt(this.is_hot);
                }
                var self = this;
                listVideos(params_data)
                    .then(function(res) {
                        uni.stopPullDownRefresh();
                        if (res.code === '00000') {
                            var page_data = res.data || null;
                            var page_list = (page_data != null && page_data.list != null) ? page_data.list : [];
                            var new_list = [];
                            for (var i = 0; i < page_list.length; i++) {
                                new_list.push(self.video_item_handle(page_list[i]));
                            }
                            var total = (page_data != null && page_data.total != null) ? page_data.total : 0;
                            self.setData({
                                video_list: is_first ? new_list : self.video_list.concat(new_list),
                                video_total: total,
                                video_page: page,
                                video_loading_more: false,
                                data_list_loding_status: new_list.length > 0 || !is_first ? 3 : 0,
                                data_list_loding_msg: new_list.length == 0 && is_first ? '暂无相关视频' : self.data_list_loding_msg,
                            });
                        } else {
                            self.setData({
                                video_loading_more: false,
                            });
                            if (is_first) {
                                self.setData({
                                    data_list_loding_status: 2,
                                    data_list_loding_msg: res.msg || '加载失败',
                                });
                            }
                        }
                    })
                    .catch(function() {
                        uni.stopPullDownRefresh();
                        self.setData({
                            video_loading_more: false,
                        });
                        if (is_first) {
                            self.setData({
                                data_list_loding_status: 2,
                                data_list_loding_msg: self.$t('common.internet_error_tips'),
                            });
                        }
                    });
            },
            // 详情跳转
            video_item_event(e) {
                var url = e.currentTarget.dataset.value;
                if (url != null && url != '') {
                    uni.navigateTo({
                        url: url,
                    });
                }
            },
        },
    };
</script>
<style scoped>
    .video-list {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: space-between;
    }
    .video-list-item {
        width: calc(50% - 10rpx);
        background: #fff;
        border-radius: 12rpx;
        padding: 12rpx;
        box-sizing: border-box;
        margin-bottom: 20rpx;
    }
    .video-list-cover {
        border-radius: 8rpx;
        overflow: hidden;
        background: #f5f5f5;
    }
    .video-list-img {
        width: 100%;
        height: 300rpx;
        display: block;
    }
    .video-list-duration {
        position: absolute;
        right: 8rpx;
        bottom: 8rpx;
        z-index: 2;
        padding: 2rpx 8rpx;
        font-size: 20rpx;
        line-height: 1.5;
        color: #fff;
        background: rgba(0, 0, 0, 0.6);
        border-radius: 6rpx;
    }
    .video-list-title {
        margin-top: 12rpx;
        font-size: 28rpx;
        color: #333;
        line-height: 1.5;
        min-height: 84rpx;
    }
    .video-list-tag {
        flex-shrink: 0;
        font-size: 18rpx;
        padding: 0 8rpx;
        border-radius: 4rpx;
        line-height: 1.6;
        color: #E93B3D;
        background: rgba(233, 59, 61, 0.08);
    }
    .video-list-tag-hot {
        color: #FF6F00;
        background: rgba(255, 111, 0, 0.1);
    }
</style>
