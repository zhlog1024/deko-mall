<template>
    <view :class="theme_view + (is_show_ask_bar ? ' page-bottom-fixed' : '')">
        <!-- 问答列表 -->
        <view v-if="ask_list.length > 0" class="padding-main">
            <view class="border-radius-main padding-main bg-white">
                <view v-for="(item, index) in ask_list" :key="index" class="ask-list-item" @tap="ask_item_event" :data-value="item.url">
                    <view class="flex-row jc-sb align-c">
                        <view class="flex-1 flex-width ask-list-title text-line-2">{{ item.title }}</view>
                        <view class="ask-list-tag" :class="item.is_reply == 1 ? 'ask-list-tag-reply' : 'ask-list-tag-wait'">{{ item.is_reply == 1 ? '已回' : '未回' }}</view>
                    </view>
                    <view class="flex-row align-c cr-grey-9 text-size-xs margin-top-sm">
                        <text>{{ item.add_time_date }}</text>
                        <view class="fw-b padding-horizontal-xs">·</view>
                        <text>{{ item.access_count || 0 }}人浏览</text>
                    </view>
                </view>
                <!-- 加载更多状态 -->
                <view v-if="ask_loading_more" class="tc cr-grey-9 text-size-xs padding-vertical-main">加载中...</view>
                <view v-else-if="ask_list.length >= ask_total && ask_total > 0" class="tc cr-grey-9 text-size-xs padding-vertical-main">没有更多了</view>
            </view>
        </view>
        <!-- 加载中/错误/无数据 -->
        <component-no-data v-else :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>

        <!-- 提问入口（指定商品才展示） -->
        <view v-if="is_show_ask_bar" class="bottom-fixed">
            <view class="bottom-line-exclude">
                <view class="item bg-white round padding-vertical text-size fw-b flex-row jc-c align-c cr-main" @tap="ask_open_event">
                    <iconfont name="icon-edit" size="28rpx" color="#FF3F3F" propClass="margin-right-sm"></iconfont>
                    <text>我要提问</text>
                </view>
            </view>
        </view>

        <!-- 提问弹窗 -->
        <ask-popup ref="ask_popup" :prop-spu-id="spu_id" :prop-spu-name="spu_name" :prop-spu-pic="spu_pic" @onAskSuccess="ask_reload_handle"></ask-popup>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import askPopup from '@/pages/plugins/ask/components/ask-popup/ask-popup';
    import { listAsks } from '@/api/cms/ask.js';
    import { time_stamp } from '@/common/js/common/common.js';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                params: null,
                // 列表数据
                ask_list: [],
                ask_total: 0,
                ask_page: 1,
                ask_page_size: 10,
                ask_loading_more: false,
                // 是否展示底部提问栏（指定商品才展示）
                is_show_ask_bar: false,
                // 提问弹窗商品上下文
                spu_id: '',
                spu_name: '',
                spu_pic: '',
            };
        },
        components: {
            componentCommon,
            componentNoData,
            askPopup,
        },
        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            // 设置参数（spuId 指定商品；spuName/spuPic 提问快照）
            params = app.globalData.launch_params_handle(params);
            this.setData({
                params: params,
                spu_id: params.spuId || '',
                spu_name: params.spuName || '',
                spu_pic: params.spuPic || '',
                is_show_ask_bar: params.spuId != null && params.spuId !== '' && parseInt(params.spuId) > 0,
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
            if (this.ask_loading_more || this.ask_list.length >= this.ask_total) {
                return false;
            }
            this.get_data_list(this.ask_page + 1);
        },
        methods: {
            // 快照行字段适配（mall VO camelCase → 展示字段）
            ask_item_handle(item) {
                var row = item || {};
                var is_reply = row.isReply != null ? row.isReply : (row.is_reply != null ? row.is_reply : 0);
                var ask_id = row.id != null ? row.id : '';
                return {
                    id: ask_id,
                    url: ask_id != null && ask_id !== '' ? '/pages/plugins/ask/detail/detail?id=' + ask_id : '',
                    title: row.title || '',
                    is_reply: Number(is_reply) || 0,
                    add_time_date: time_stamp(row.createTime),
                    access_count: row.accessCount != null ? row.accessCount : (row.access_count || 0),
                    // 弹窗商品快照兜底（列表项自带商品快照）
                    spu_name: row.spuName || '',
                    spu_pic: row.spuPic || '',
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
                        ask_loading_more: true,
                    });
                }
                var params_data = {
                    pageNum: page,
                    pageSize: this.ask_page_size,
                };
                if (this.spu_id != null && this.spu_id !== '' && parseInt(this.spu_id) > 0) {
                    params_data['spuId'] = parseInt(this.spu_id);
                }
                var self = this;
                listAsks(params_data)
                    .then(function(res) {
                        uni.stopPullDownRefresh();
                        if (res.code === '00000') {
                            var page_data = res.data || null;
                            var page_list = (page_data != null && page_data.list != null) ? page_data.list : [];
                            var new_list = [];
                            for (var i = 0; i < page_list.length; i++) {
                                new_list.push(self.ask_item_handle(page_list[i]));
                            }
                            var total = (page_data != null && page_data.total != null) ? page_data.total : 0;
                            // 提问商品快照兜底（首屏首条带商品信息）
                            if (self.spu_name == '' && new_list.length > 0 && new_list[0].spu_name != '') {
                                self.spu_name = new_list[0].spu_name;
                                self.spu_pic = new_list[0].spu_pic;
                            }
                            self.setData({
                                ask_list: is_first ? new_list : self.ask_list.concat(new_list),
                                ask_total: total,
                                ask_page: page,
                                ask_loading_more: false,
                                data_list_loding_status: new_list.length > 0 || !is_first ? 3 : 0,
                                data_list_loding_msg: new_list.length == 0 && is_first ? '暂无相关问答' : self.data_list_loding_msg,
                            });
                        } else {
                            self.setData({
                                ask_loading_more: false,
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
                            ask_loading_more: false,
                        });
                        if (is_first) {
                            self.setData({
                                data_list_loding_status: 2,
                                data_list_loding_msg: self.$t('common.internet_error_tips'),
                            });
                        }
                    });
            },
            // 提问成功刷新
            ask_reload_handle() {
                this.get_data_list(1);
            },
            // 提问入口
            ask_open_event() {
                if ((this.$refs.ask_popup || null) != null) {
                    this.$refs.ask_popup.open();
                }
            },
            // 问答详情跳转
            ask_item_event(e) {
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
    .ask-list-item {
        padding: 24rpx 0;
        border-bottom: 2rpx solid #f5f5f5;
    }
    .ask-list-item:last-of-type {
        border-bottom: 0;
    }
    .ask-list-title {
        font-size: 30rpx;
        color: #333;
        margin-right: 20rpx;
        line-height: 1.5;
    }
    .ask-list-tag {
        flex-shrink: 0;
        font-size: 22rpx;
        padding: 4rpx 12rpx;
        border-radius: 6rpx;
        line-height: 1.4;
        align-self: flex-start;
    }
    .ask-list-tag-reply {
        color: #10BB78;
        background: rgba(16, 187, 120, 0.1);
    }
    .ask-list-tag-wait {
        color: #E3A51B;
        background: rgba(227, 165, 27, 0.12);
    }
</style>
