<template>
    <view :class="theme_view + (is_show_ask_bar ? ' page-bottom-fixed' : '')">
        <!-- 问答详情 -->
        <view v-if="(info || null) != null" class="padding-main">
            <!-- 问题卡片 -->
            <view class="padding-main border-radius-main bg-white spacing-mb">
                <view v-if="(info.title || null) != null" class="fw-b text-size-xl spacing-mb">{{ info.title }}</view>
                <view class="cr-grey-9 text-size-xs flex-row align-c spacing-mb">
                    <text v-if="(info.add_time_date || null) != null">{{ info.add_time_date }}</text>
                    <text v-if="(info.add_time_date || null) != null" class="fw-b padding-horizontal-xs">·</text>
                    <text>共{{ info.access_count || 0 }}人浏览</text>
                    <text v-if="(info.askUser || null) != null && info.askUser != ''" class="margin-left-main single-text">{{ info.askUser }} 提问</text>
                </view>
                <!-- 问题详情 -->
                <view v-if="(info.content || null) != null && info.content != ''">
                    <mp-html v-if="is_html_content(info.content)" :content="info.content" />
                    <view v-else class="ask-detail-content-text">{{ info.content }}</view>
                </view>
                <!-- 关联商品 -->
                <view v-if="parseInt(info.spuId || 0) > 0" class="goods-link spacing-mt bg-grey-f9 padding-main border-radius-sm" @tap="goods_url_event">
                    <view class="flex-row align-c">
                        <image v-if="(info.spuPic || null) != null && info.spuPic != ''" :src="info.spuPic" mode="aspectFill" class="img border-radius-sm margin-right-main"></image>
                        <view class="flex-1 flex-width padding-right-sm">
                            <view class="ask-goods-title multi-text">{{ info.spuName || '相关商品' }}</view>
                            <view class="cr-grey-9 text-size-xs margin-top-xs">查看该商品详情</view>
                        </view>
                        <iconfont name="icon-arrow-right" color="#999"></iconfont>
                    </view>
                </view>
            </view>
            <!-- 回答区 -->
            <view v-if="is_reply_show()" class="padding-main border-radius-main bg-white spacing-mb">
                <view class="flex-row jc-sb align-c">
                    <view class="flex-row align-c">
                        <text class="fw-b">官方回复</text>
                        <text v-if="(info.replyUser || null) != null && info.replyUser != ''" class="cr-grey-9 text-size-xs margin-left-sm">· {{ info.replyUser }}</text>
                    </view>
                    <view v-if="(info.reply_time_date || null) != null" class="cr-grey-9 text-size-xs">{{ info.reply_time_date }}</view>
                </view>
                <view class="padding-top-main">
                    <mp-html v-if="is_html_content(info.answer)" :content="info.answer" />
                    <view v-else class="ask-detail-content-text">{{ info.answer }}</view>
                </view>
            </view>
            <view v-else class="padding-main border-radius-main bg-white spacing-mb tc cr-grey-9 text-size-xs padding-top-xxl padding-bottom-xxl">
                该问题暂未回复，敬请期待
            </view>
        </view>
        <!-- 加载中/错误/无数据 -->
        <component-no-data v-else :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>

        <!-- 提问入口（关联商品存在才展示） -->
        <view v-if="is_show_ask_bar" class="bottom-fixed">
            <view class="bottom-line-exclude">
                <view class="item bg-white round padding-vertical text-size fw-b flex-row jc-c align-c cr-main" @tap="ask_bar_event">
                    <iconfont name="icon-edit" size="28rpx" color="#FF3F3F" propClass="margin-right-sm"></iconfont>
                    <text>我要提问</text>
                </view>
            </view>
        </view>

        <!-- 提问弹窗 -->
        <ask-popup ref="ask_popup" :prop-spu-id="spu_id" :prop-spu-name="spu_name" :prop-spu-pic="spu_pic" @onAskSuccess="ask_success_event"></ask-popup>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import askPopup from '@/pages/plugins/ask/components/ask-popup/ask-popup';
    import { getAskDetail } from '@/api/cms/ask.js';
    import { time_stamp } from '@/common/js/common/common.js';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                params: null,
                info: null,
                // 是否展示底部提问栏（关联商品存在才展示）
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

            this.setData({
                params: app.globalData.launch_params_handle(params),
            });

            // 数据加载
            this.get_data();
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
            this.get_data();
        },
        methods: {
            // 是否 html 富文本
            is_html_content(str) {
                return /<\/?[a-z][\s\S]*>/i.test(str || '');
            },
            // 是否展示回答区
            is_reply_show() {
                var info = this.info || {};
                return parseInt(info.isReply || info.is_reply || 0) == 1 && (info.answer || null) != null && info.answer != '';
            },
            // 数据加载
            get_data() {
                if (this.params == null || (this.params.id || null) == null) {
                    this.setData({
                        data_list_loding_status: 0,
                        data_list_loding_msg: '参数错误',
                    });
                    uni.stopPullDownRefresh();
                    return false;
                }
                var self = this;
                getAskDetail(this.params.id)
                    .then(function(res) {
                        uni.stopPullDownRefresh();
                        if (res.code === '00000') {
                            var info = res.data || null;
                            if (info != null) {
                                // 字段适配（mall VO camelCase → 展示字段）
                                info.add_time_date = time_stamp(info.createTime);
                                info.reply_time_date = time_stamp(info.replyTime);
                                self.setData({
                                    info: info,
                                    data_list_loding_status: 3,
                                    is_show_ask_bar: parseInt(info.spuId || 0) > 0,
                                    spu_id: info.spuId || '',
                                    spu_name: info.spuName || '',
                                    spu_pic: info.spuPic || '',
                                });
                                // 标题
                                if ((info.title || null) != null) {
                                    uni.setNavigationBarTitle({ title: info.title });
                                }
                            } else {
                                self.setData({
                                    data_list_loding_status: 0,
                                    data_list_loding_msg: '该问答不存在或已下线',
                                });
                            }
                        } else {
                            self.setData({
                                data_list_loding_status: 2,
                                data_list_loding_msg: res.msg || '加载失败',
                            });
                        }
                    })
                    .catch(function() {
                        uni.stopPullDownRefresh();
                        self.setData({
                            data_list_loding_status: 2,
                            data_list_loding_msg: self.$t('common.internet_error_tips'),
                        });
                    });
            },
            // 关联商品跳转
            goods_url_event() {
                var spu_id = (this.info || {}).spuId || '';
                if (spu_id != null && spu_id != '') {
                    uni.navigateTo({
                        url: '/pages/goods-detail/goods-detail?id=' + spu_id,
                    });
                }
            },
            // 提问按钮
            ask_bar_event() {
                if ((this.$refs.ask_popup || null) != null) {
                    this.$refs.ask_popup.open();
                }
            },
            // 提问成功
            ask_success_event() {
                // 重新拉取详情（后台确认展示前的本次数据不变化，仅兜底刷新）
                this.get_data();
            },
        },
    };
</script>
<style scoped>
    .ask-detail-content-text {
        font-size: 28rpx;
        color: #333;
        line-height: 1.6;
        word-break: break-all;
        white-space: pre-wrap;
    }
    .goods-link .img {
        width: 100rpx;
        height: 100rpx;
        background: #fff;
        flex-shrink: 0;
    }
    .goods-link .ask-goods-title {
        font-size: 28rpx;
        color: #333;
    }
</style>
