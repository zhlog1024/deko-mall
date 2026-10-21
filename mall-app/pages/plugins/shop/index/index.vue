<template>
    <view :class="theme_view">
        <!-- 搜索 -->
        <component-nav-back :propFixed="false" propClass="bg-white cr-black" propColor="#333" :style="'padding-top:' + status_bar_height + 'px;'">
            <template slot="right" :class="is_mp_env ? 'top-search-width' : ''">
                <view class="margin-left-main" :class="is_mp_env ? '' : 'flex-1 flex-width'">
                    <component-search @onsearch="search_button_event" :propDefaultValue="search_keywords" :propIsOnEvent="true" :propIsRequired="false" propIconColor="#ccc" propPlaceholderClass="cr-grey-c" propBgColor="#f6f6f6"></component-search>
                </view>
            </template>
        </component-nav-back>
        <!-- 分类 -->
        <scroll-view v-if="(shop_category || null) != null && shop_category.length > 0" class="nav-base scroll-view-horizontal bg-white oh" scroll-x="true">
            <block v-for="(item, index) in shop_category" :key="index">
                <view :class="'item cr-grey dis-inline-block padding-horizontal-main ' + (nav_active_value == item.id ? 'cr-main nav-active-line fw-b' : '')" @tap="nav_event" :data-value="item.id">
                    {{ item.name }}
                </view>
            </block>
        </scroll-view>
        <!-- 列表 -->
        <scroll-view :scroll-y="true" class="scroll-box plugins-shop-data-list" :style="content_style" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="(data_list || null) != null && data_list.length > 0" class="data-list padding-horizontal-main padding-top-main oh">
                <block v-for="(item, index) in data_list" :key="index">
                    <view :data-value="item.url" @tap="url_event" class="item border-radius-main bg-white oh cp spacing-mb">
                        <image :src="item.logo_long" class="logo" mode="aspectFit"></image>
                        <view class="tc">
                            <view class="single-text padding-horizontal-main padding-top-main">
                                <!-- 标题 -->
                                <text class="fw-b text-size-md va-m">{{ item.name }}</text>
                                <!-- 认证信息 -->
                                <view v-if="(item.icon_list || null) != null && item.icon_list.length > 0" class="auth-icon dis-inline-block margin-left-sm">
                                    <block v-for="(itemiv, indexix) in item.icon_list" :key="indexix">
                                        <image :src="itemiv.icon" class="icon va-m" mode="aspectFill"></image>
                                    </block>
                                </view>
                            </view>
                            <view class="cr-grey padding-main">
                                <text class="multi-text">{{ item.describe }}</text>
                            </view>
                            <view class="oh br-t-dashed padding-main">
                                <view class="fl cr-grey-9 single-text"
                                    >{{$t('common.goods')}}<text class="cr-black fw-b padding-left-sm">{{ item.goods_count }}</text></view
                                >
                                <view class="fr cr-grey-9 single-text"
                                    >{{$t('goods-category.goods-category.at5p35')}}<text class="cr-black fw-b padding-left-sm">{{ item.goods_sales_count }}</text></view
                                >
                            </view>
                        </view>
                    </view>
                </block>
            </view>
            <view v-else>
                <!-- 提示信息 -->
                <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
            </view>

            <!-- 结尾 -->
            <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
        </scroll-view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNavBack from '@/components/nav-back/nav-back';
    import componentSearch from '@/components/search/search';
    import componentNoData from '@/components/no-data/no-data';
    import componentBottomLine from '@/components/bottom-line/bottom-line';
    import { request } from '@/config/request';
    // 公共静态资源地址（默认店铺占位图）
    var common_static_url = app.globalData.get_static_url('common');
    var default_shop_image = common_static_url + 'default-images.jpg';
    // 状态栏高度
    var bar_height = parseInt(app.globalData.get_system_info('statusBarHeight', 0, true));
    // #ifdef MP-TOUTIAO || H5
    bar_height = 0;
    // #endif
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                data_bottom_line_status: false,
                client_type: app.globalData.application_client_type(),
                status_bar_height: bar_height,
                is_mp_env: false,
                // #ifdef MP-WEIXIN || MP-BAIDU || MP-ALIPAY || MP-QQ || MP-KUAISHOU
                is_mp_env: true,
                // #endif
                data_is_loading: 0,
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                params: null,
                data_base: null,
                shop_category: [],
                nav_active_value: 0,
                search_keywords: '',
                content_style: '',
                // 自定义分享信息
                share_info: {},
            };
        },
        components: {
            componentCommon,
            componentNavBack,
            componentSearch,
            componentNoData,
            componentBottomLine,
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            // 参数处理
            params = app.globalData.launch_params_handle(params);

            this.setData({
                params: params,
                nav_active_value: params.category_id || 0,
                search_keywords: params.keywords || '',
                content_style: 'height: calc(100vh - 80rpx - '+(this.status_bar_height+(this.client_type == 'h5' ? 55 : 50))+'px);',
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
            if(this.data_list_loding_status === 1) {
                uni.stopPullDownRefresh();
            } else {
                this.setData({
                    data_page: 1,
                });
                this.get_data_list(1);
            }
        },

        methods: {
            // 初始化
            get_data(params = {}) {
                // #ifdef APP
                // 网络检查
                if((params || null) == null || (params.loading || 0) == 0) {
                    app.globalData.network_type_handle(this, 'get_data', params);
                    return false;
                }
                // #endif

                // 店铺广场初始化（店铺分类暂无，直接加载列表）
                this.get_data_list(1);
                // 分享菜单处理
                app.globalData.page_share_handle(this.share_info);
            },

            // 获取数据列表
            get_data_list(is_mandatory) {
                // 分页是否还有数据
                if ((is_mandatory || 0) == 0) {
                    if (this.data_bottom_line_status == true) {
                        uni.stopPullDownRefresh();
                        return false;
                    }
                }
                // 是否加载中
                if (this.data_is_loading == 1) {
                    return false;
                }
                this.setData({
                    data_is_loading: 1,
                });
                // 加载loding
                if(this.data_page > 1) {
                    uni.showLoading({
                        title: this.$t('common.loading_in_text'),
                    });
                }
                // 获取数据
                const self = this;
                request({
                    url: '/mall-pms/app-api/v1/shop/pages',
                    method: 'GET',
                    params: {
                        pageNum: this.data_page,
                        pageSize: 10,
                        keywords: this.search_keywords || '',
                    },
                }).then(function(res) {
                    if(self.data_page > 1) {
                        uni.hideLoading();
                    }
                    uni.stopPullDownRefresh();
                    if (res.code === '00000' && res.data != null) {
                        var data = res.data;
                        // 转换为店铺列表结构。
                        var list = data.list || [];
                        var total = data.total || 0;
                        var tempShopList = list.map(function(item) {
                            return {
                                id: item.id,
                                url: '/pages/plugins/shop/detail/detail?id=' + item.id,
                                name: item.shopName,
                                logo: item.logo || default_shop_image,
                                logo_long: item.logoLong || item.logo || default_shop_image,
                                describe: item.description || '',
                                icon_list: [],
                                goods_count: item.goodsCount || 0,
                                goods_sales_count: 0,
                            };
                        });
                        if (tempShopList.length > 0) {
                            if (self.data_page <= 1) {
                                var temp_data_list = tempShopList;
                            } else {
                                var temp_data_list = self.data_list || [];
                                for (var i in tempShopList) {
                                    temp_data_list.push(tempShopList[i]);
                                }
                            }
                            var pageTotal = Math.ceil(total / 10);
                            self.setData({
                                data_list: temp_data_list,
                                data_total: total,
                                data_page_total: pageTotal,
                                data_list_loding_status: 3,
                                data_page: self.data_page + 1,
                                data_is_loading: 0,
                            });
                            // 是否还有数据
                            self.setData({
                                data_bottom_line_status: self.data_list.length > 0 && self.data_page > 1 && self.data_page > pageTotal,
                            });
                        } else {
                            self.setData({
                                data_list_loding_status: 0,
                                data_is_loading: 0,
                            });
                            if (self.data_page <= 1) {
                                self.setData({
                                    data_list: [],
                                    data_bottom_line_status: false,
                                });
                            }
                        }
                    } else {
                        self.setData({
                            data_list_loding_status: 0,
                            data_list_loding_msg: res.msg || '加载失败',
                            data_is_loading: 0,
                        });
                        app.globalData.showToast(res.msg || '加载失败');
                    }
                }).catch(function() {
                    if(self.data_page > 1) {
                        uni.hideLoading();
                    }
                    uni.stopPullDownRefresh();
                    self.setData({
                        data_list_loding_status: 2,
                        data_is_loading: 0,
                    });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 滚动加载
            scroll_lower(e) {
                this.get_data_list();
            },

            // 关键字搜索
            search_button_event(e) {
                this.setData({
                    search_keywords: e,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.get_data_list(1);
            },

            // 导航事件
            nav_event(e) {
                this.setData({
                    nav_active_value: e.currentTarget.dataset.value || 0,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.get_data_list(1);
            },

            // url事件
            url_event(e) {
                app.globalData.url_event(e);
            }
        },
    };
</script>
<style>
    @import './index.css';
</style>
