<template>
    <view :class="theme_view">
        <view v-if="(data || null) != null">
            <!-- 搜索 -->
            <block v-if="(data.is_header || 0) == 1">
                <!-- 搜索框 -->
                <view class="padding-main bg-white">
                    <component-search propClass="br" :propPlaceholder="$t('customview.customview.726k7y')"></component-search>
                </view>
            </block>

            <!-- 内容 -->
            <mp-html :content="data.html_content" />

            <!-- 结尾 -->
            <block v-if="(data.is_footer || 0) == 1">
                <!-- 结尾 -->
                <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
            </block>
        </view>
        <view v-else>
            <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
        </view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import { getCustomViewDetail } from '@/api/diy/customview.js';
    import componentCommon from '@/components/common/common';
    import componentSearch from "@/components/search/search";
    import componentNoData from "@/components/no-data/no-data";
    import componentBottomLine from "@/components/bottom-line/bottom-line";
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_bottom_line_status: false,
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                params: {},
                data: null,
                // 自定义分享信息
                share_info: {}
            };
        },

        components: {
            componentCommon,
            componentSearch,
            componentNoData,
            componentBottomLine
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            this.setData({
                params: app.globalData.launch_params_handle(params),
            });
        },

        onShow() {
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 加载数据
            this.get_data();

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
            // 获取数据（mall-cms /app-api/v1/custom-view/{id},成功码 '00000' 由 request 统一判定）
            get_data() {
                var id = (this.params || {}).id;
                if (id == null || id === '') {
                    uni.stopPullDownRefresh();
                    this.setData({
                        data_bottom_line_status: false,
                        data_list_loding_status: 2,
                        data_list_loding_msg: this.$t('common.data_empty_tips') || '页面不存在'
                    });
                    return;
                }
                getCustomViewDetail(id)
                    .then((body) => {
                        uni.stopPullDownRefresh();
                        var detail = (body || {}).data || null;
                        this.setData({
                            data: detail,
                            data_list_loding_msg: '',
                            data_list_loding_status: 0,
                            data_bottom_line_status: true
                        });
                        if (detail != null) {
                            // 基础自定义分享
                            this.setData({
                                share_info: {
                                    title: detail.name,
                                    desc: '',
                                    path: '/pages/customview/customview',
                                    query: 'id=' + detail.id,
                                    img: detail.logo
                                }
                            });
                            // 标题名称
                            uni.setNavigationBarTitle({
                                title: detail.name
                            });
                        }
                        // 分享菜单处理
                        app.globalData.page_share_handle(this.share_info);
                    })
                    .catch(() => {
                        uni.stopPullDownRefresh();
                        this.setData({
                            data_bottom_line_status: false,
                            data_list_loding_status: 2,
                            data_list_loding_msg: this.$t('common.internet_error_tips')
                        });
                        app.globalData.showToast(this.$t('common.internet_error_tips'));
                    });
            }
        }
    };
</script>
<style>
</style>