<template>
    <view :class="theme_view">
        <view class="page padding-main">
            <block v-if="hasLogin">
                <view class="panel-item padding-horizontal-main border-radius-main bg-white oh spacing-mb">
                    <view class="item padding-vertical-xxl padding-right-xxxl arrow-right oh" data-value="/pages/personal/personal" @tap="url_event">
                        <image :src="user.avatarUrl || default_avatar" mode="widthFix" class="circle br fl user-avatar"></image>
                        <view class="fl margin-left margin-top">
                            <view>{{ user.nickName || $t('login.login.6yfr9g') }}</view>
                            <view class="cr-grey text-size-sm margin-top-xs">{{ (user.mobile || null) != null ? user.mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '' }}</view>
                        </view>
                    </view>

                </view>

                <!-- 地址 & 账户 -->
                <view class="panel-item padding-horizontal-main border-radius-main bg-white oh spacing-mb">
                    <view class="item padding-vertical-xxl padding-right-xxxl arrow-right" data-value="/pages/user-address/user-address" @tap="url_event">
                        <text>{{ $t('setup-page.setup-page.shipping_address') }}</text>
                        <text class="fr cr-grey">{{ $t('setup.setup.5eltza') }}</text>
                    </view>
                </view>
            </block>

            <!-- 权限管理、清除缓存、客服电话、关于 -->
            <view class="panel-item padding-horizontal-main border-radius-main bg-white oh spacing-mb">
                <!-- #ifdef MP || APP -->
                <view class="item padding-vertical-xxl padding-right-xxxl arrow-right" @tap="open_setting_event">
                    <text>{{ $t('setup.setup.377uwg') }}</text>
                    <text class="fr cr-grey">{{ $t('setup.setup.5eltza') }}</text>
                </view>
                <!-- #endif -->
                <view class="item padding-vertical-xxl padding-right-xxxl arrow-right" @tap="remove_user_cache_event">
                    <text>{{ $t('setup.setup.5493ui') }}</text>
                    <text class="fr cr-grey">{{ $t('setup.setup.f53166') }}</text>
                </view>
                <view v-if="(common_app_customer_service_tel || null) != null" class="item padding-vertical-xxl padding-right-xxxl arrow-right" @tap="call_event">
                    <text>{{ $t('setup.setup.656fv1') }}</text>
                    <text class="fr cr-grey">{{ $t('setup.setup.f25wcx') }}</text>
                </view>
                <view v-if="hasLogin" class="item padding-vertical-xxl padding-right-xxxl arrow-right" data-value="/pages/logout/logout" @tap="url_event">
                    <text>{{ $t('setup-page.setup-page.account_cancel') }}</text>
                </view>
                <view v-if="home_use_multilingual_status == 1" class="item padding-vertical-xxl padding-right-xxxl arrow-right" @tap="open_language_event">
                    <text>{{ $t('setup.setup.r7jz13') }}</text>
                    <text class="fr cr-grey">{{ language }}</text>
                </view>
                <view class="item padding-vertical-xxl padding-right-xxxl arrow-right" data-value="/pages/about/about" @tap="url_event">
                    <text>{{ $t('setup.setup.tghrf2') }}</text>
                </view>
            </view>

            <!-- 退出登录 -->
            <view v-if="hasLogin" class="logout-section spacing-mt">
                <view class="logout-card bg-white border-radius-main" @tap="logout_event">
                    <text class="logout-text">{{ $t('setup-page.setup-page.logout') }}</text>
                </view>
            </view>

            <!-- 打开语言选择弹层-->
            <component-lang-switch ref="lang_switch" @popup_sub_language_event="popup_sub_language_event"></component-lang-switch>
        </view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentPopup from '@/components/popup/popup';
    import componentLangSwitch from '@/components/lang-switch/lang-switch';
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                default_avatar: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                common_app_customer_service_tel: null,
                home_user_login_type: [],
                home_use_multilingual_status: 0,
                language: '',
                user: null,
                hasLogin: false,
            };
        },

        components: {
            componentCommon,
            componentPopup,
            componentLangSwitch
        },
        
        computed: {
        },
        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            var language_key = app.globalData.get_language_value();
            var language_list = this.$t('language');
            this.setData({
                language: language_list[language_key],
            });
        },

        onShow() {
            app.globalData.page_event_onshow_handle();

            app.globalData.init_config(0, this, 'init_config');

            // 从 Storage 读取用户信息。
            var userInfo = app.globalData.get_user_cache_info() || null;
            this.user = userInfo;
            this.hasLogin = userInfo != null;

            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        methods: {
            init_config(status) {
                if ((status || false) == true) {
                    this.setData({
                        common_app_customer_service_tel: app.globalData.get_config('common_app_customer_service_tel'),
                        home_use_multilingual_status: app.globalData.get_config('home_use_multilingual_status'),
                        home_user_login_type: app.globalData.get_config('home_user_login_type'),
                    });
                } else {
                    app.globalData.is_config(this, 'init_config');
                }
            },

            url_event(e) {
                app.globalData.url_event(e);
            },

            open_setting_event() {
                app.globalData.open_setting_event();
            },

            call_event() {
                if (this.common_app_customer_service_tel == null) {
                    app.globalData.showToast(this.$t('setup.setup.utnr7g'));
                } else {
                    app.globalData.call_tel(this.common_app_customer_service_tel);
                }
            },

            remove_user_cache_event(e) {
                app.globalData.remove_user_cache_event(false);
            },

            logout_event() {
                uni.showModal({
                    title: this.$t('setup-page.setup-page.logout_confirm_title'),
                    content: this.$t('setup-page.setup-page.logout_confirm_content'),
                    cancelText: this.$t('common.cancel'),
                    confirmText: this.$t('common.confirm'),
                    confirmColor: '#FF6B6B',
                    success: (res) => {
                        if (res.confirm) {
                            // 统一清除登录缓存。
                            this.hasLogin = false;
                            this.user = null;
                            app.globalData.remove_user_cache_event();
                            app.globalData.showToast(this.$t('setup-page.setup-page.logout_success'), 'success');
                            setTimeout(function () {
                                app.globalData.url_open(app.globalData.app_tabbar_pages()[0]);
                            }, 1000);
                        }
                    }
                });
            },

            open_language_event () {
                if ((this.$refs.lang_switch || null) != null) {
                    this.$refs.lang_switch.lang_open_event();
                }
            },

            popup_sub_language_event(e) {
                this.setData({
                    language: e,
                });
                app.globalData.set_pages_navigation_bar_title();
                app.globalData.init_config();
            },
        },
    };
</script>
<style>
    @import './setup.css';
</style>
