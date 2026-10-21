<template>
    <view :class="theme_view">
        <web-view :src="web_url" @message="onWebviewMessage"></web-view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                web_url: null
            };
        },

        components: {
            componentCommon
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);

            var url = (params.url || '');
            if (params.raw) {
            } else {
                url = decodeURIComponent(url) || null;
            }
            if (url != null) {
                if (url.indexOf('{token}') >= 0) {
                    var user = app.globalData.get_user_cache_info();
                    var token = user == null ? null : (user.token || null);
                    if (token != null) {
                        url = url.replace(/{token}/ig, token);
                    }
                }
            }
            this.setData({ web_url: url });
        },

        onShow() {
            app.globalData.page_event_onshow_handle();
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        methods: {
            // 接收 web-view 内部 H5 通过 uni.webView.postMessage 发送的消息
            onWebviewMessage: function(e) {
                var data = e.detail.data;
                if (!data || !data.length) return;
                var msg = data[data.length - 1];
                if (msg && msg.action === 'close') {
                    uni.navigateBack();
                }
            }
        }
    };
</script>
