<template>
    <view :class="theme_view">
        <view class="page-bottom-fixed padding-main">
            <view v-if="data_list.length > 0">
                <view v-for="(item, index) in data_list" :key="index" class="item padding-vertical-xl padding-left-xl bg-white spacing-mb flex-row jc-sb align-c border-radius-main">
                    <view class="flex-1 flex-width" @tap="address_conent_event" :data-index="index">
                        <view class="flex-row align-c">
                            <text v-if="is_default == item.id" class="default-address margin-right-sm text-size-xss border-radius-sm">{{ $t('user-address-page.user-address-page.default_tag') }}</text>
                            <text v-if="(item.alias || null) != null" class="address-alias br-main cr-main margin-right-sm text-size-xss border-radius-sm">{{ item.alias }}</text>
                            <view class="single-text flex-1 flex-width fw-b">
                                <text>{{ item.provinceName || '' }}{{ item.cityName || '' }}{{ item.areaName || '' }}{{ item.detailAddress || '' }}</text>
                            </view>
                        </view>
                        <view class="flex-row align-c flex-wrap padding-top-main">
                            <view class="padding-right-main">{{ item.consigneeName }}</view>
                            <view class="cr-grey">{{ item.consigneeMobile }}</view>
                        </view>
                    </view>
                    <view class="padding-vertical-main padding-left-main padding-right-xl" @tap="address_edit_event" :data-index="index">
                        <iconfont name="icon-edit" size="24rpx"></iconfont>
                    </view>
                </view>
            </view>
            <view v-else>
                <component-no-data :propStatus="data_list_loding_status"></component-no-data>
            </view>

            <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
        </view>

        <view class="bottom-fixed" :style="bottom_fixed_style">
            <view class="bottom-line-exclude">
                <button class="item cr-white round text-size wh-auto" type="default" hover-class="none" @tap="address_add_event" :style="btn_bg_color">{{ $t('user-address-page.user-address-page.add_address') }}</button>
            </view>
        </view>

        <component-common ref="common"></component-common>
    </view>
</template>

<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import componentBottomLine from '@/components/bottom-line/bottom-line';
    import iconfont from '@/components/iconfont/iconfont';
    import { list } from '@/api/ums/address.js';

    const theme_color = app.globalData.get_theme_color();
    const theme_color_light = app.globalData.get_theme_color(null, true);

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                theme_color: theme_color,
                theme_color_light: theme_color_light,
                btn_bg_color: 'background:linear-gradient(107deg, ' + theme_color_light + ' 0%, ' + theme_color + ' 100%)',
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                bottom_fixed_style: '',
                data_list: [],
                params: null,
                is_default: 0,
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentBottomLine,
            iconfont,
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            this.setData({ params: params || {} });
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
            this.get_data_list();
        },

        methods: {
            init() {
                this.get_data_list();
            },

            get_data_list() {
                this.setData({ data_list_loding_status: 1 });

                var memberId = app.globalData.get_user_cache_info('id', 0) || app.globalData.get_user_cache_info('memberId', 0);
                list(memberId).then((res) => {
                    uni.stopPullDownRefresh();
                    if (res.code == '00000') {
                        var data_list = res.data || [];
                        if (data_list.length > 0) {
                            var is_default = 0;
                            for (var i in data_list) {
                                if (data_list[i]['defaulted'] == 1) {
                                    is_default = data_list[i]['id'];
                                }
                            }
                            this.setData({
                                data_list: data_list,
                                is_default: is_default,
                                data_list_loding_status: 3,
                                data_bottom_line_status: true,
                            });
                        } else {
                            this.setData({
                                data_list: [],
                                data_list_loding_status: 0,
                                data_bottom_line_status: false,
                            });
                        }
                    } else {
                        this.setData({ data_list_loding_status: 0 });
                        if (app.globalData.is_login_check(res, this, 'get_data_list')) {
                            app.globalData.showToast(res.msg);
                        }
                    }
                }).catch(() => {
                    uni.stopPullDownRefresh();
                    this.setData({ data_list_loding_status: 2 });
                    app.globalData.showToast('网络异常，请重试');
                });
            },

            address_conent_event(e) {
                var index = e.currentTarget.dataset.index || 0;
                var data = this.data_list[index];
                if ((this.params.is_back || 0) == 1 || (this.params.source || 0) == 1) {
                    this.address_choose_cache_save_back(data);
                } else {
                    var copy_val = '收件人：' + data.consigneeName + '\n电话：' + data.consigneeMobile + '\n地址：' + (data.provinceName || '') + (data.cityName || '') + (data.areaName || '') + (data.detailAddress || '');
                    app.globalData.text_copy_event(copy_val);
                }
            },

            address_choose_cache_save_back(address) {
                uni.setStorageSync(app.globalData.data.cache_buy_user_address_select_key, address);
                uni.navigateBack();
            },

            address_add_event(e) {
                var temp_params = this.params || {};
                temp_params['id'] = 0;
                var query = app.globalData.json_to_url_params(temp_params);
                app.globalData.url_open('/pages/user-address-save/user-address-save?' + query);
            },

            address_edit_event(e) {
                var index = e.currentTarget.dataset.index || 0;
                var data = this.data_list[index] || null;
                if (data == null) {
                    app.globalData.showToast('地址数据异常');
                    return false;
                }
                var temp_params = this.params || {};
                temp_params['id'] = data.id;
                var query = app.globalData.json_to_url_params(temp_params);
                app.globalData.url_open('/pages/user-address-save/user-address-save?' + query);
            },

            address_map_event(e) {
                var index = e.currentTarget.dataset.index || 0;
                var data = this.data_list[index] || null;
                if (data == null) {
                    app.globalData.showToast('地址数据异常');
                    return false;
                }
                var name = data.alias || data.consigneeName || '';
                var address = (data.provinceName || '') + (data.cityName || '') + (data.areaName || '') + (data.detailAddress || '');
                app.globalData.open_location(data.lng, data.lat, name, address);
            },
        },
    };
</script>
<style>
    @import './user-address.css';
</style>
