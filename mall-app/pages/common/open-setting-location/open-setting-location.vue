<template>
    <view :class="theme_view">
        <view v-if="is_show_open_setting" class="open-setting-view">
            <view class="content bg-white">
                <view class="msg cr-grey">{{ $t('open-setting-location-page.open-setting-location-page.not_enabled') }}</view>
                <view class="value cr-base">{{ $t('open-setting-location-page.open-setting-location-page.please_go_to') }}<text>{{ $t('common.setup') }}</text>{{ $t('open-setting-location-page.open-setting-location-page.enable_location') }}</view>
                <button type="primary" open-type="openSetting" size="mini" @opensetting="setting_event">{{ $t('open-setting-location-page.open-setting-location-page.goto_settings') }}</button>
                <view class="tc margin-top-sm">
                    <navigator open-type="navigateBack" class="cp cr-grey dis-inline-block" hover-class="none">{{ $t('common.back') }}</navigator>
                </view>
            </view>
        </view>
        <view v-else class="open-setting-loding">
            <view class="loading-text">{{ $t('open-setting-location-page.open-setting-location-page.loading') }}</view>
            <view class="tc margin-top-sm">
                <navigator open-type="navigateBack" class="cp cr-grey dis-inline-block" hover-class="none">{{ $t('common.back') }}</navigator>
            </view>
        </view>
        <view v-if="error_msg" class="cr-red margin-top-lg padding-horizontal-main">{{ error_msg }}</view>
    </view>
</template>
<script>
const app = getApp();

export default {
    data() {
        return {
            theme_view: app.globalData.get_theme_value_view(),
            params: null,
            is_show_open_setting: false,
            auth: "scope.userLocation",
            cache_key: app.globalData.data.cache_userlocation_key,
            error_msg: null,
        };
    },

    onLoad(params) {
        this.setData({ params: params || {} });
        this.init();
    },

    methods: {
        init() {
            app.globalData.get_location_check(this.auth, this, "location_back_handle");
        },

        // 位置权限校验回调
        location_back_handle(status = 0) {
            if (status == 1) {
                if (parseInt(this.params.is_check_success_back || 0) == 1) {
                    uni.navigateBack();
                } else {
                    this.choose_location();
                }
            } else {
                this.setData({ is_show_open_setting: true });
            }
        },

        // 配置打开事件
        setting_event(e) {
            if (e.detail.authSetting[this.auth]) {
                this.setData({ is_show_open_setting: false });
                if (parseInt(this.params.is_check_success_back || 0) == 1) {
                    uni.navigateBack();
                } else {
                    this.choose_location();
                }
            }
        },

        // 打开位置选择
        choose_location() {
            // #ifdef APP
            uni.navigateBack();
            // #endif

            // #ifdef MP-WEIXIN
            // 微信新隐私协议：先触发 requirePrivacyAuthorize，授权通过后再调 chooseLocation
            if (wx.requirePrivacyAuthorize) {
                wx.requirePrivacyAuthorize({
                    success: () => {
                        this._do_choose_location();
                    },
                    fail: () => {
                        // 用户拒绝隐私授权
                        var result = { status: 3 };
                        uni.setStorageSync(this.cache_key, result);
                        uni.navigateBack();
                    }
                });
                return;
            }
            // #endif

            this._do_choose_location();
        },

        _do_choose_location() {
            uni.chooseLocation({
                success: (res) => {
                    res['status'] = 1;
                    res['lat'] = res.latitude;
                    res['lng'] = res.longitude;
                    delete res['latitude'];
                    delete res['longitude'];
                    uni.setStorageSync(this.cache_key, res);
                    uni.$emit('refresh', { location_success: true });
                    // #ifndef APP
                    setTimeout(function () {
                        uni.navigateBack();
                    }, 500);
                    // #endif
                },
                fail: (res) => {
                    var result = { ...(uni.getStorageSync(this.cache_key) || {}), ...{ status: 3 } };
                    uni.setStorageSync(this.cache_key, result);

                    var msg = res.errMsg || res.errorMessage || res.chooseLocation || '位置选择失败';
                    if (msg.indexOf('cancel') !== -1 || res.error == 11) {
                        // #ifndef APP
                        uni.navigateBack();
                        // #endif
                    } else {
                        this.setData({ error_msg: msg });
                        app.globalData.showToast(msg);
                    }
                }
            });
        }
    },
};
</script>
<style>
@import "./open-setting-location.css";
</style>
