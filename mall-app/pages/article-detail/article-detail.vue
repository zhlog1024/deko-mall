<template>
    <view :class="theme_view">
        <view v-if="(data || null) != null" class="padding-main">
            <view class="padding-main border-radius-main bg-white spacing-mb">
                <view class="fw-b text-size-xl">{{ data.title }}</view>
                <view class="cr-grey margin-top-lg oh br-t padding-top-main">
                    <view class="fl">
                        <text>{{$t('article-detail.article-detail.728374')}}</text>
                        <text>{{ data.add_time }}</text>
                    </view>
                    <view class="fr">
                        <text class="margin-left-xxxl">{{$t('article-detail.article-detail.j92ru0')}}</text>
                        <text>{{ data.access_count }}</text>
                    </view>
                </view>
            </view>
            <view class="padding-main border-radius-main bg-white oh web-html-content spacing-mb">
                <mp-html :content="data.content" />
            </view>
        </view>
        <view v-else>
            <!-- 提示信息 -->
            <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
        </view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from "@/components/no-data/no-data";
    import { time_stamp } from '@/common/js/common/common.js';
    import { getArticleDetail } from '@/api/diy/article.js';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list_loding_status: 1,
                data_list_loding_msg: "",
                params: null,
                data: null,
                // 自定义分享信息
                share_info: {},
            };
        },

        components: {
            componentCommon,
            componentNoData,
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
            // 初始化
            get_data() {
                if (this.params == null || (this.params.id || null) == null) {
                    this.setData({
                        data_list_loding_status: 0,
                        data_list_loding_msg: this.$t('common.no_data'),
                    });
                    uni.stopPullDownRefresh();
                    return false;
                }

                getArticleDetail(this.params.id)
                    .then((data) => {
                        uni.stopPullDownRefresh();
                        var article = data.data || null;
                        if (article != null) {
                            // 外链文章直接重定向；同时覆盖从分享链接进入详情页的场景。
                            if ((article.jumpUrl || null) != null && article.jumpUrl != '') {
                                app.globalData.open_web_view(article.jumpUrl, true)
                                return
                            }
                            // 字段适配（createTime → add_time）
                            article.add_time = time_stamp(article.createTime);
                            article.access_count = article.accessCount || 0;
                            this.setData({
                                data_list_loding_status: 3,
                                data: article,
                            });

                            // 基础自定义分享
                            this.setData({
                                share_info: {
                                    title: article.title || '',
                                    desc: article.description || '',
                                    path: "/pages/article-detail/article-detail",
                                    query: "id=" + article.id,
                                    img: article.cover || '',
                                },
                            });

                            // 标题
                            uni.setNavigationBarTitle({ title: article.title });
                        } else {
                            this.setData({
                                data_list_loding_status: 0,
                                data_list_loding_msg: this.$t('common.no_data'),
                            });
                        }

                        // 分享菜单处理
                        app.globalData.page_share_handle(this.share_info);
                    })
                    .catch(() => {
                        uni.stopPullDownRefresh();
                        this.setData({
                            data_list_loding_status: 2,
                        });
                        app.globalData.showToast(this.$t('common.internet_error_tips'));
                    });
            }
        },
    };
</script>
<style>
    @import "./article-detail.css";
</style>
