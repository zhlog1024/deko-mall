<template>
    <view :class="theme_view">
        <scroll-view :scroll-y="true" class="scroll-box bg-white">
            <view class="page-bottom-fixed">
                <view class="padding-main">
                    <view class="padding-main border-radius-main bg-white spacing-mb">
                        <view class="fw-b text-size-lg">{{$t('logout.logout.u10002')}}</view>
                        <view class="cr-grey margin-top-lg text-size-sm">
                            {{$t('logout.logout.9vfzz4')}}
                        </view>
                    </view>
                </view>
                <view class="bottom-fixed" :style="bottom_fixed_style">
                    <view class="bottom-line-exclude">
                        <button class="item bg-grey br-grey cr-base round text-size fl" type="default" size="mini" hover-class="none" @tap="logout_submit_event">{{$t('logout.logout.u10002')}}</button>
                        <button class="item bg-main br-main cr-white round text-size fr" type="default" size="mini" hover-class="none" @tap="logout_cancel_event">{{$t('common.cancel')}}</button>
                    </view>
                </view>
            </view>
        </scroll-view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                bottom_fixed_style: '',
            };
        },

        components: {
            componentCommon,
            componentNoData,
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
        },

        onShow() {
            app.globalData.page_event_onshow_handle();

            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        methods: {
            // 注销提交
            logout_submit_event(e) {
                if (e.alert_status != 0 && e.alert_status != 1) {
                    app.globalData.alert({
                        msg: this.$t('logout.logout.9vfzz4'),
                        is_show_cancel: 1,
                        object: this,
                        method: 'logout_submit_event',
                    });
                    return false;
                }

                if (e.alert_status == 1) {
                    // 直接返回成功，不调用后端接口
                    app.globalData.remove_user_cache_event();
                    app.globalData.showToast(this.$t('common.del_success'), 'success');
                    setTimeout(function () {
                        app.globalData.url_open(app.globalData.app_tabbar_pages()[0]);
                    }, 1500);
                }
            },

            logout_cancel_event() {
                app.globalData.page_back_prev_event();
            },
        },
    };
</script>
<style>
    @import './logout.css';
</style>
