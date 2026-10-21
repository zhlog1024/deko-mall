<template>
    <view :class="theme_view">
        <!-- 顶部内容 -->
        <view class="top-content">
            <!-- 内容 -->
            <view class="content">
                <view class="user-top bg-white">
                    <view class="head-base pr flex-row jc-sb align-c">
                        <!-- 左侧头像 -->
                        <view class="head-left">
                            <image data-value="/pages/personal/personal" @tap="url_event" @error="user_avatar_error" class="head-avatar circle bg-white va-m" :src="avatar" mode="widthFix"></image>
                            <view class="va-m dis-inline-block margin-left-lg base-info">
                                <view class="flex-col align-b" data-value="/pages/personal/personal" @tap="url_event">
                                    <view class="va-m fw-b text-size single-text">{{ nickname }}</view>
                                    <view v-if="(user || null) != null && (user.number_code || null) != null" class="head-id border-radius-sm padding-horizontal-sm margin-top-sm dis-inline-block fw-b">
                                        <text class="text-size-xs">ID </text>
                                        <text class="text-size-xss padding-left-xs pr bottom-xs">{{ user.number_code }}</text>
                                    </view>
                                    <view v-if="(user || null) != null && ((user.memberType || null) != null || ((user.tagName || null) != null && (user.tagName || '') != '0'))" class="user-tags-row margin-top-sm">
                                        <text v-if="(user.memberType || null) != null" class="user-tag">{{ user.memberType }}</text>
                                        <text v-if="(user.tagName || null) != null && (user.tagName || '') != '0'" class="user-tag">{{ user.tagName }}</text>
                                    </view>
                                </view>
                            </view>
                        </view>
                        <!-- 右上角 -->
                        <view class="head-right cr-black">
                            <view class="item pr dis-inline-block margin-left-xxl" data-value="/pages/setup/setup" data-login="0" @tap="url_event">
                                <iconfont name="icon-setup" size="46rpx"></iconfont>
                            </view>
                            <view class="item pr dis-inline-block margin-left-xxl" data-value="/pages/message/message" @tap="url_event">
                                <iconfont name="icon-smallbell" size="46rpx"></iconfont>
                                <view class="badge-icon pa">
                                    <component-badge :propNumber="message_total"></component-badge>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>

                <!-- 中间导航 -->
                <view class="user-bottom padding-horizontal-main">
                    <!-- 订单导航 -->
                    <block v-if="(main_navigation_data || null) != null && main_navigation_data.length > 0">
                        <block v-for="(item, index) in main_navigation_data" :key="index">
                            <block v-if="(item.extension_data || null) != null && item.extension_data.length > 0">
                                <view class="nav-list bg-white padding-vertical-main border-radius-main spacing-mb">
                                    <view class="nav-content flex-row jc-sb align-c">
                                        <!-- 副导航（订单子状态） -->
                                        <view class="nav-list-sub oh flex-row jc-sa align-c flex-1">
                                            <block v-for="(items, index2) in item.extension_data" :key="index2">
                                                <view class="flex-1 cp" :data-value="items.url" @tap="url_event">
                                                    <view class="item pr tc">
                                                        <view class="badge-icon pa">
                                                            <component-badge :propNumber="items.count"></component-badge>
                                                        </view>
                                                        <iconfont :name="items.icon" size="44rpx" color="#5D4037" />
                                                        <view class="item-name cr-base text-size-sm margin-top-xs">{{ items.name }}</view>
                                                    </view>
                                                </view>
                                            </block>
                                        </view>
                                        <!-- 主导航（全部订单） -->
                                        <view class="nav-all-order-goods pr" :data-value="item.url || item.event_value" @tap="url_event">
                                            <view class="item pr tc">
                                                <iconfont :name="item.icon" size="44rpx" color="#5D4037" />
                                                <view class="item-name cr-base text-size-sm margin-top-xs">{{ item.name }}</view>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </block>
                        </block>
                    </block>

                    <!-- 我的服务 -->
                    <view class="service-nav padding-main border-radius-main bg-white spacing-mb">
                        <view class="section-title padding-horizontal-xs padding-bottom-main">
                            <text>{{ $t('common.my_services') }}</text>
                        </view>
                        <!-- 列表模式 -->
                        <view v-if="nav_show_model_type == 1" class="nav-list">
                            <block v-for="(item, index) in navigation" :key="index">
                                <view :data-value="item.event_value" :data-type="item.event_type" @tap="navigation_event" :class="'nav-item cp padding-main ' + (index > 0 ? 'br-t-e' : '')">
                                    <view class="arrow-right">
                                        <iconfont :name="item.icon" size="40rpx" :color="item.color || '#5D4037'" propClass="va-m" />
                                        <text class="item-name va-m cr-base margin-left-sm text-size-sm">{{ item.name }}</text>
                                    </view>
                                </view>
                            </block>
                            <!-- 联系客服 -->
                            <view v-if="(common_app_customer_service_tel || null) != null" class="nav-item br-t cp padding-main" @tap="call_event">
                                <view class="arrow-right">
                                    <iconfont name="icon-customer-service" size="40rpx" color="#333333" propClass="va-m" />
                                    <text class="item-name va-m cr-base margin-left-sm text-size-sm">{{ $t('common.tel_service') }}</text>
                                </view>
                            </view>
                            <view class="nav-item br-t cp padding-main" data-value="/pages/setup/setup" @tap="url_event">
                                <view class="arrow-right">
                                    <iconfont name="icon-setup" size="40rpx" color="#333333" propClass="va-m" />
                                    <text class="item-name va-m cr-base margin-left-sm text-size-sm">{{ $t('common.setup') }}</text>
                                </view>
                            </view>
                        </view>
                        <!-- 九方格模式 -->
                        <view v-else class="nav-box oh margin-top-sm">
                            <block v-for="(item, index) in navigation" :key="index">
                                <view :data-value="item.event_value" :data-type="item.event_type" @tap="navigation_event" class="nav-item padding-vertical-main padding-horizontal-xs fl tc cp">
                                    <iconfont :name="item.icon" size="44rpx" :color="item.color || '#5D4037'" />
                                    <view class="item-name single-text cr-base text-size-sm margin-top-sm">{{ item.name }}</view>
                                </view>
                            </block>
                            <!-- 联系客服 -->
                            <view v-if="(common_app_customer_service_tel || null) != null" class="nav-item padding-vertical-main padding-horizontal-xs fl tc cp" @tap="call_event">
                                <iconfont name="icon-customer-service" size="44rpx" color="#333333" />
                                <view class="item-name single-text cr-base text-size-sm margin-top-sm">{{ $t('common.tel_service') }}</view>
                            </view>
                            <view class="nav-item padding-vertical-main padding-horizontal-xs fl tc cp" data-value="/pages/setup/setup" @tap="url_event">
                                <iconfont name="icon-setup" size="44rpx" color="#333333" />
                                <view class="item-name single-text cr-base text-size-sm margin-top-sm">{{ $t('common.setup') }}</view>
                            </view>
                        </view>
                    </view>

                    <!-- 会员服务 -->
                    <view v-if="(user || null) != null && member_services.length > 0" class="service-nav padding-main border-radius-main bg-white spacing-mb">
                        <view class="section-title padding-horizontal-xs padding-bottom-main">
                            <text>{{ $t('common.member_services') }}</text>
                        </view>
                        <!-- 列表模式 -->
                        <view v-if="nav_show_model_type == 1" class="nav-list">
                            <block v-for="(item, index) in member_services" :key="index">
                                <view v-if="item.show !== false" :data-value="item.event_value" :data-type="item.event_type" @tap="navigation_event" :class="'nav-item cp padding-main ' + (index > 0 ? 'br-t-e' : '')">
                                    <view class="arrow-right">
                                        <iconfont :name="item.icon" size="40rpx" :color="item.color || '#5D4037'" propClass="va-m" />
                                        <text class="item-name va-m cr-base margin-left-sm text-size-sm">{{ item.name }}</text>
                                    </view>
                                </view>
                            </block>
                        </view>
                        <!-- 九方格模式 -->
                        <view v-else class="nav-box oh margin-top-sm">
                            <block v-for="(item, index) in member_services" :key="index">
                                <view v-if="item.show !== false" :data-value="item.event_value" :data-type="item.event_type" @tap="navigation_event" class="nav-item padding-vertical-main padding-horizontal-xs fl tc cp">
                                    <iconfont :name="item.icon" size="44rpx" :color="item.color || '#5D4037'" />
                                    <view class="item-name single-text cr-base text-size-sm margin-top-sm">{{ item.name }}</view>
                                </view>
                            </block>
                        </view>
                    </view>

                    <!-- 用户中心公告 -->
                    <view v-if="(common_user_center_notice || null) != null" class="padding-horizontal-main">
                        <uni-notice-bar class="padding-0 margin-0" show-icon scrollable :text="common_user_center_notice" background-color="transparent" color="#666" />
                    </view>

                    <!-- 版权信息 -->
                    <component-copyright></component-copyright>
                </view>
            </view>
        </view>

        <!-- 在线客服 -->
        <component-online-service :propIsNav="true" :propIsBar="true" :propIsSpread="false"></component-online-service>

        <!-- 快捷导航 -->
        <component-quick-nav :propIsNav="true" :propIsBar="true"></component-quick-nav>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentQuickNav from '@/components/quick-nav/quick-nav';
    import componentBadge from '@/components/badge/badge';
    import componentCopyright from '@/components/copyright/copyright';
    import componentOnlineService from '@/components/online-service/online-service';
    import iconfont from '@/components/iconfont/iconfont';
    import { getUserInfo } from '@/api/user';

    var common_static_url = app.globalData.get_static_url('common');
    var static_url = app.globalData.get_static_url('user');
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                common_static_url: common_static_url,
                load_status: 0,
                static_url: static_url,
                avatar: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                user: null,
                nickname: '',
                message_total: 0,
                // 订单导航
                main_navigation_data: [],
                // 我的服务导航
                navigation: [],
                // 会员服务导航
                member_services: [],
                // 基础配置
                common_app_customer_service_tel: null,
                common_user_center_notice: null,
                // 用户中心菜单展示模式
                nav_show_model_type: app.globalData.get_config('user_center_nav_show_model_type', 0),
            };
        },

        components: {
            componentCommon,
            componentQuickNav,
            componentBadge,
            componentCopyright,
            componentOnlineService,
            iconfont
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
        },

        onShow() {
            app.globalData.page_event_onshow_handle();

            // 资源设置
            this.set_resources_data();

            // 数据加载
            this.init();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show({object: this, method: 'init'});
            }
        },

        // 下拉刷新
        onPullDownRefresh(e) {
            this.init();
        },

        // #ifndef MP
        onNavigationBarButtonTap(e) {
            var index = e.index;
            if (index === 0) {
                if(this.is_login()) {
                    app.globalData.url_open('/pages/setup/setup');
                }
            } else if (index === 1) {
                // #ifdef APP-PLUS
                var pages = getCurrentPages();
                var page = pages[pages.length - 1];
                var currentWebview = page.$getAppWebview();
                currentWebview.hideTitleNViewButtonRedDot({ index: index });
                // #endif
                uni.navigateTo({
                    url: '/pageProfile/pages/notice/notice'
                });
            }
        },
        // #endif

        methods: {
            // 资源设置
            set_resources_data() {
                // 当前用户信息
                var user = app.globalData.get_user_cache_info() || null;

                this.setData({
                    user: user,
                    nickname: this.nickname || this.$t('common.guest'),
                });
            },

            // 获取数据
            init(status = 0) {
                if(this.user == null) {
                    app.globalData.get_user_info(this, 'init', 1);
                }
                if(status == 1) {
                    this.set_resources_data();
                }

                this.get_data();
                this.set_user_base();
                app.globalData.page_share_handle();
            },

            // 设置用户基础信息
            set_user_base() {
                if(this.user != null) {
                    if ((this.user.avatarUrl || this.user.avatar || null) != null) {
                        this.setData({
                            avatar: this.user.avatarUrl || this.user.avatar,
                        });
                    }
                    if ((this.user.nickName || this.user.user_name_view || null) != null) {
                        this.setData({
                            nickname: this.user.nickName || this.user.user_name_view,
                        });
                    }
                }
            },

            // 获取用户中心数据
            get_data() {
                var self = this;
                // 构建基础导航数据
                this.build_navigation();

                if (this.user != null) {
                    getUserInfo().then(function(res) {
                        uni.stopPullDownRefresh();
                        if (res.code === '00000' && res.data) {
                            var data = res.data;
                            // 最近一次会员信息（供门店入口等异步重算使用）
                            self._member_data = data;
                            // 更新用户信息缓存
                            var store = uni.getStorageSync('user') || {};
                            store.userInfo = data;
                            uni.setStorageSync('user', store);

                            // 刷新页面数据
                            self.setData({
                                user: data,
                                avatar: data.avatarUrl || self.avatar,
                                nickname: data.nickName || self.nickname,
                                message_total: data.messageTotal || 0,
                            });

                            // 更新订单状态计数
                            self.update_order_status_counts(data);
                            // 更新会员服务入口
                            self.update_member_services(data);
                        }
                    }).catch(function() {
                        uni.stopPullDownRefresh();
                    });
                } else {
                    uni.stopPullDownRefresh();
                }
            },

            // 构建基础导航
            build_navigation() {
            	this.common_app_customer_service_tel = app.globalData.get_config('common_app_customer_service_tel', null);
                // 订单子状态
                var order_status_list = [
                    { name: this.$t('common.wait_pay'), status: 0, count: 0, icon: 'icon-wait-payment', url: '/pages/user-order/user-order?status=0' },
                    { name: this.$t('common.wait_deliver'), status: 1, count: 0, icon: 'icon-wait-delivery', url: '/pages/user-order/user-order?status=1' },
                    { name: this.$t('common.wait_receive'), status: 2, count: 0, icon: 'icon-wait-receive-delivery', url: '/pages/user-order/user-order?status=2' },
                    { name: this.$t('common.after_sale'), status: 5, count: 0, icon: 'icon-refund-aftersales', url: '/pages/user-orderaftersale/user-orderaftersale' },
                ];

                var main_navigation_data = [{
                    name: this.$t('common.all_orders'),
                    icon: 'icon-order-list',
                    url: '/pages/user-order/user-order?status=100',
                    extension_data: order_status_list
                }];

                // 我的服务
                var navigation = [
                    { name: this.$t('common.shipping_address'), icon: 'icon-address', color: '#5677fc', event_value: '/pages/user-address/user-address', event_type: 'page' },
                    { name: this.$t('common.goods_favor'), icon: 'icon-collect', color: '#ff7159', event_value: '/pages/user-favor/user-favor', event_type: 'page' },
                    { name: this.$t('common.my_footprint'), icon: 'icon-time', color: '#5677fc', event_value: '/pages/user-goods-browse/user-goods-browse', event_type: 'page' },
                    { name: this.$t('common.my_review'), icon: 'icon-comment', color: '#ff7159', event_value: '/pages/user-goods-comments/user-goods-comments', event_type: 'page' },
                    { name: this.$t('common.shop_favor'), icon: 'icon-store', color: '#ff9f43', event_value: '/pages/plugins/shop/favor/favor', event_type: 'page' },
                ];

                this.setData({
                    main_navigation_data: main_navigation_data,
                    navigation: navigation,
                });
            },

            // 更新订单状态计数
            update_order_status_counts(data) {
                var statusCounts = data.orderStatusCounts || {};
                if (this.main_navigation_data.length > 0 && this.main_navigation_data[0].extension_data) {
                    var ext = this.main_navigation_data[0].extension_data;
                    for (var i in ext) {
                        ext[i].count = statusCounts['status_' + ext[i].status] || 0;
                    }
                    this.setData({ main_navigation_data: this.main_navigation_data });
                }
            },

            // 更新会员服务入口
            update_member_services(data) {
                var services = [
                    { name: this.$t('common.points_zone'), icon: 'icon-points-terse', color: '#ff7159', event_value: '', event_type: 'point', show: true },
                ];
                this.setData({ member_services: services });
            },

            // 重算会员服务入口
            refresh_member_services() {
                if (this.user == null) {
                    return;
                }
                var data = this._member_data || this.user;
                this.update_member_services(data);
            },

            // 清除缓存（退出登录）
            remove_user_cache_event(e) {
                this.setData({
                    user: null,
                    nickname: '',
                    avatar: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                // 头部统计栏
                head_nav_list: [],
                    member_services: [],
                });
                app.globalData.remove_user_cache_event();
                this.set_resources_data();
                this.get_data();
            },

            // 远程自定义导航事件
            navigation_event(e) {
                if(this.is_login()) {
                    var type = e.currentTarget.dataset.type;
                    var value = e.currentTarget.dataset.value;
                    if (type === 'share') {
                        // 海报页已移除，走系统分享
                        app.globalData.page_share_handle();
                    } else if (type === 'point') {
                        this.navTobalance('POINT');
                    } else if (type === 'page' && value) {
                        uni.navigateTo({ url: value });
                    }
                }
            },

            // 跳转资产专区
            navTobalance(type) {
                var user = this.user || {};
                var assets = user.assets || [];
                var asset = assets.find(function(item) { return item.assetCode === type; }) || {};
                var url = '/pageUser/pages/balance?type=' + type + '&id=' + (asset.id || '');
                uni.navigateTo({ url: url });
            },

            // 复制文本
            copyText(value) {
                var self = this;
                uni.setClipboardData({
                    data: value,
                    success: function() {
                        uni.showToast({ title: self.$t('common.copy_success') });
                    }
                });
            },

            // 联系客服
            call_event() {
                if (this.common_app_customer_service_tel == null) {
                    app.globalData.showToast(this.$t('common.customer_service_not_set'));
                } else {
                    app.globalData.call_tel(this.common_app_customer_service_tel);
                }
            },

            // url事件
            url_event(e) {
                var login = e.currentTarget.dataset.login;
                if(login === undefined || login == 1) {
                    if(this.is_login()) {
                        app.globalData.url_event(e);
                    }
                } else {
                    app.globalData.url_event(e);
                }
            },

            // 头像加载错误
            user_avatar_error(e) {
                this.setData({
                    avatar: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                });
            },

            // 是否登录
            is_login() {
                if((this.user || null) == null) {
                    app.globalData.url_open('/pages/login/login?event_callback=init');
                    return false;
                }
                return true;
            }
        },
    };
</script>
<style>
    @import './user.css';
</style>
