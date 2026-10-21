<template>
    <view :class="theme_view">
        <!-- 搜索 -->
        <component-nav-back :propFixed="false" propClass="bg-white cr-black" propColor="#333">
            <template slot="right" :class="is_mp_env ? 'top-search-width' : ''">
                <view class="margin-left-main" :class="is_mp_env ? '' : 'flex-1 flex-width'">
                    <component-search @onsearch="search_button_event" :propDefaultValue="search_keywords" :propIsOnEvent="true" :propIsRequired="false" propIconColor="#ccc" propPlaceholderClass="cr-grey-c" propBgColor="#f6f6f6"></component-search>
                </view>
            </template>
        </component-nav-back>
        <!-- 导航 -->
        <view class="nav-base bg-white">
            <block v-for="(item, index) in nav_status_list" :key="index">
                <view v-if="nav_status_index == index" class="item fl tc cr-main nav-active-line" :data-index="index" @tap="nav_event">{{ item.name }}</view>
                <view v-else class="item fl tc" :data-index="index" @tap="nav_event">{{ item.name }}</view>
            </block>
        </view>
        <!-- 订单列表 -->
        <scroll-view :scroll-y="true" class="order-scroll" :style="content_style" @scrolltolower="scroll_lower" lower-threshold="60">
            <view :class="nav_status_index == 1 && order_select_ids.length > 0 && home_is_enable_order_bulk_pay == 1 ? 'page-bottom-fixed' : ''">
                <view v-if="data_list.length > 0" class="padding-horizontal-main padding-top-main">
                    <view v-for="(item, index) in data_list" :key="index" class="list-item padding-horizontal-main padding-top-main border-radius-main bg-white oh spacing-mb">
                        <view class="item-base oh br-b padding-bottom-main">
                            <!-- 选择 -->
                            <view v-if="nav_status_index == 1 && home_is_enable_order_bulk_pay == 1" :data-price="item.total_price" :data-oid="item.id" :data-payment="item.payment_id" class="dis-inline-block va-m margin-right-lg" @tap="selected_event">
                                <iconfont :name="'icon-'+((order_select_ids.indexOf(item.id) != -1) ? 'selected-solid' : 'not-selected')" size="40rpx" :color="(order_select_ids.indexOf(item.id) != -1) ? theme_color : '#999'" propClass="fl margin-top-xs"></iconfont>
                            </view>
                            <!-- 基础信息 -->
                            <view class="dis-inline-block va-m cp" @tap="url_event" :data-value="item.warehouse_url || ''">
                                <image v-if="(item.warehouse_icon || null) != null" class="warehouse-group-icon va-m margin-right-sm" :src="item.warehouse_icon" mode="aspectFit"></image>
                                <text class="cr-base va-m">{{ item.warehouse_name }}</text>
                            </view>
                            <text class="fr cr-red">{{ item.status_name }}<text v-if="(item.is_under_line_text || null) != null">（{{ item.is_under_line_text }}）</text></text>
                        </view>
                        <view v-for="(detail, di) in item.items" :key="di" class="br-b-dashed oh padding-vertical-main">
                            <view :data-value="'/pages/user-order-detail/user-order-detail?id=' + item.id" @tap="url_event">
                                <image class="goods-image fl radius" :src="detail.images" mode="aspectFill"></image>
                                <view class="goods-base pr">
                                    <view class="multi-text">{{ detail.title }}</view>
                                    <view v-if="detail.spec != null" class="margin-top-sm">
                                        <block v-for="(sv, si) in detail.spec" :key="si">
                                            <text v-if="si > 0" class="cr-grey padding-left-xs padding-right-xs">;</text>
                                            <text class="cr-grey">{{ sv.value }}</text>
                                        </block>
                                    </view>
                                    <view class="margin-top-sm">
                                        <text class="fw-b">{{ item.currency_data.currency_symbol }}{{ detail.price }}</text>
                                        <text class="margin-left-sm">x{{ detail.buy_number }}</text>
                                    </view>
                                    <view v-if="item.is_can_launch_aftersale == 1 && (detail.orderaftersale_btn_text || null) != null" class="orderaftersale-btn-text cr-blue pa bg-white" @tap.stop="orderaftersale_event" :data-oid="item.id" :data-did="detail.id">{{
                                        detail.orderaftersale_btn_text
                                    }}</view>
                                </view>
                            </view>
                        </view>
                        <view class="padding-vertical-main tr cr-base text-size-sm">
                            <text>{{$t('user-order-detail.user-order-detail.423rmr')}}<text class="fw-b">{{ item.buy_number_count }}</text>{{$t('user-order-detail.user-order-detail.41ty94')}}<text class="sales-price margin-right-xs">{{ item.currency_data.currency_symbol }}{{ item.total_price }}</text></text>
                        </view>
                        <view
                            v-if="
                                (
                                    item.operate_data.is_cancel +
                                    item.operate_data.is_pay +
                                    item.operate_data.is_collect +
                                    item.operate_data.is_comments +
                                    item.operate_data.is_delete +
                                    (item.plugins_is_order_allot_button || 0) +
                                    (item.plugins_is_order_batch_button || 0) +
                                    (item.plugins_is_order_frequencycard_button || 0) +
                                    (item.plugins_delivery_data || 0) +
                                    (item.plugins_ordergoodsform_data || 0) +
                                    (item.plugins_orderresources_data || 0) +
                                    (item.plugins_is_orderfeed_button || 0)
                                 > 0 ) ||
                                (item.status == 2 && item.order_model != 2) ||
                                ((item.plugins_express_data || 0) == 1 && (item.express_data || null) != null)
                            "
                            class="item-operation tr br-t padding-top-main">
                            <button v-if="item.operate_data.is_cancel == 1" class="round bg-white cr-yellow br-yellow margin-bottom-main" type="default" size="mini" @tap="cancel_event" :data-value="item.id" :data-index="index" hover-class="none">{{$t('common.cancel')}}</button>
                            <button v-if="item.operate_data.is_pay == 1" class="round bg-white cr-green br-green margin-bottom-main" type="default" size="mini" @tap="pay_event" :data-oid="item.id" hover-class="none">{{$t('order.order.1i873j')}}</button>
                            <button v-if="item.operate_data.is_collect == 1" class="round bg-white cr-green br-green margin-bottom-main" type="default" size="mini" @tap="collect_event" :data-transactionid="item.weixin_collect_data || ''" :data-value="item.id" :data-index="index" hover-class="none">{{$t('orderallot-list.orderallot-list.w2w2w4')}}</button>
                            <button v-if="(item.plugins_express_data || 0) == 1 && (item.express_data || null) != null" class="round bg-white cr-main br-main margin-bottom-main" type="default" size="mini" @tap="url_event" :data-value="'/pages/plugins/express/detail/detail?oid=' + item.id" hover-class="none">{{$t('orderallot-list.orderallot-list.w2t242')}}</button>
                            <button v-if="(item.plugins_delivery_data || 0) > 0" class="round bg-white cr-main br-main margin-bottom-main" type="default" size="mini" @tap="url_event" :data-value="'/pages/plugins/delivery/logistics/logistics?id=' + item.plugins_delivery_data" hover-class="none">{{$t('orderallot-list.orderallot-list.w2t242')}}</button>
                            <button v-if="item.operate_data.is_comments == 1" class="round bg-white cr-green br-green margin-bottom-main" type="default" size="mini" @tap="url_event" :data-value="'/pages/user-order-comments/user-order-comments?orderNo='+item.id" hover-class="none">{{$t('common.comment')}}</button>
                            <button v-if="item.status == 2 && item.order_model != 2" class="round cr-base br margin-bottom-main" type="default" size="mini" @tap="rush_event" :data-value="item.id" :data-index="index" hover-class="none">{{$t('user-order.user-order.lp1v28')}}</button>
                            <button v-if="item.operate_data.is_delete == 1" class="round bg-white cr-red br-red margin-bottom-main" type="default" size="mini" @tap="delete_event" :data-value="item.id" :data-index="index" hover-class="none">{{$t('common.del')}}</button>
                            <button v-if="(item.plugins_ordergoodsform_data || 0) == 1" class="round bg-white cr-blue br-blue margin-bottom-main" type="default" size="mini" @tap="url_event" :data-value="'/pages/plugins/ordergoodsform/order/order?id=' + item.id" hover-class="none">{{$t('user-order.user-order.9l47b6')}}</button>
                            <button v-if="(item.plugins_orderresources_data || 0) == 1" class="round bg-white cr-blue br-blue margin-bottom-main" type="default" size="mini" @tap="url_event" :data-value="'/pages/plugins/orderresources/orderannex/orderannex?oid=' + item.id" hover-class="none">{{$t('user-order.user-order.fyuikn')}}</button>
                            <button v-if="(item.plugins_is_orderfeed_button || 0) == 1" class="round bg-white cr-blue br-blue margin-bottom-main" type="default" size="mini" @tap="url_event" :data-value="'/pages/plugins/orderfeed/form/form?oid=' + item.id" hover-class="none">{{$t('common.feed_text')}}</button>
                        </view>
                    </view>
                    <!-- 结尾 -->
                    <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
                </view>
                <view v-else>
                    <!-- 提示信息 -->
                    <component-no-data :propStatus="data_list_loding_status"></component-no-data>
                </view>
            </view>
        </scroll-view>

        <!-- 合并支付 -->
        <view v-if="nav_status_index == 1 && order_select_ids.length > 0 && home_is_enable_order_bulk_pay == 1" class="bottom-fixed" :style="bottom_fixed_style">
            <view class="bottom-line-exclude">
                <button class="item bg-main cr-white round text-size wh-auto" hover-class="none" @tap="pay_merge_event">{{$t('user-order.user-order.7571p2')}}</button>
            </view>
        </view>

        <!-- 支付组件 -->
        <component-payment
            ref="payment"
            :propCurrencySymbol="payment_currency_symbol"
            :propCashierPayUrl="cashier_pay_url"
            :propPaymentList="payment_list"
            propPayDataKey="paymentNo"
            :propTempPayValue="temp_pay_value"
            :propTempPayIndex="temp_pay_index"
            :propPaymentId="payment_id"
            :propDefaultPaymentId="default_payment_id"
            :propPayPrice="pay_price"
            :propIsShowPayment="is_show_payment_popup"
            :propPaymentNo="paymentNo"
            :propDeviceType="deviceType"
            :propToPageBack="to_page_back"
            @close-payment-popup="payment_popup_event_close"
            @pay-success="order_item_pay_success_handle"
            @pay-fail="order_item_pay_fail_handle"
        ></component-payment>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import config from '@/config'
    import { listOrdersWithPage, cancelOrder, deleteOrder, confirmReceive, createOrderPayment } from '@/api/oms/order.js'
    import { getCashierInfo } from '@/api/pay.js'
    import { request } from '@/config/request'
    import componentCommon from '@/components/common/common';
    import componentNavBack from '@/components/nav-back/nav-back';
    import componentSearch from '@/components/search/search';
    import componentPopup from '@/components/popup/popup';
    import componentNoData from '@/components/no-data/no-data';
    import componentBottomLine from '@/components/bottom-line/bottom-line';
    import componentPayment from '@/components/payment/payment';

    var common_static_url = app.globalData.get_static_url('common');
    // 状态栏高度
    var bar_height = parseInt(app.globalData.get_system_info('statusBarHeight', 0, true));
    // #ifdef MP-TOUTIAO || H5
    bar_height = 0;
    // #endif
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                theme_color: app.globalData.get_theme_color(),
                common_static_url: common_static_url,
                bottom_fixed_style: '',
                client_type: app.globalData.application_client_type(),
                status_bar_height: bar_height,
                is_mp_env: false,
                // #ifdef MP-WEIXIN || MP-BAIDU || MP-ALIPAY || MP-QQ || MP-KUAISHOU
                is_mp_env: true,
                // #endif
                params: {},
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                data_is_loading: 0,
                search_keywords: '',
                content_style: '',
                nav_status_list: [
                    { name: this.$t('common.all'), value: '-1' },
                    { name: this.$t('user.user.9u8e61'), value: '0' },
                    { name: this.$t('user.user.66714e'), value: '1' },
                    { name: this.$t('order.order.q820hx'), value: '2' },
                    { name: this.$t('order.order.15lr5l'), value: '3' },
                    { name: this.$t('order.order.1k98tk'), value: '4' },
                ],
                mult_payment_id_list: [],
                order_select_ids: [],
                mult_pay_price: [],
                nav_status_index: 0,
                // 基础配置
                home_is_enable_order_bulk_pay: 0,
                // 前往页面携带的参数
                to_page_back: { title: '查看订单', page: '/pages/user-order/user-order' },
                payment_currency_symbol: app.globalData.currency_symbol(),
                pay_price: 0,
                cashier_pay_url: config.url + '/mall-pay/app-api/v1/cashier/pay',
                paymentNo: '',
                deviceType: 'H5',
                // #ifdef MP-WEIXIN
                deviceType: 'MINI',
                // #endif
                // #ifdef APP
                deviceType: 'APP',
                // #endif
                payment_list: [],
                temp_pay_value: '',
                temp_pay_index: 0,
                payment_id: 0,
                default_payment_id: 0,
                is_show_payment_popup: false,
            };
        },

        components: {
            componentCommon,
            componentNavBack,
            componentSearch,
            componentPopup,
            componentNoData,
            componentBottomLine,
            componentPayment,
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            // 参数处理
            params = app.globalData.launch_params_handle(params);

            // 是否指定状态
            var nav_status_index = 0;
            if ((params.status || null) != null) {
                for (var i in this.nav_status_list) {
                    if (this.nav_status_list[i]['value'] == params.status) {
                        nav_status_index = i;
                        break;
                    }
                }
            }
            this.setData({
                params: params,
                nav_status_index: nav_status_index,
                search_keywords: params.keywords || '',
                content_style: 'height: calc(100vh - 80rpx - '+(this.status_bar_height+(this.client_type == 'h5' ? 55 : 50))+'px);',
            });

            // 初始化配置
            this.init_config();

            // 数据加载
            this.init();
        },

        onShow() {
            // 需要时重开支付弹窗
            if (this._needs_reopen_payment_popup) {
                this._needs_reopen_payment_popup = false;
                this._reopen_payment_popup();
                return;
            }
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }

            // 分享菜单处理
            app.globalData.page_share_handle();
        },

        // 下拉刷新
        onPullDownRefresh() {
            this.setData({
                data_page: 1,
            });
            this.get_data_list(1);
        },

        methods: {
            // 初始化配置
            init_config(status) {
                if ((status || false) == true) {
                    this.setData({
                        home_is_enable_order_bulk_pay: app.globalData.get_config('config.home_is_enable_order_bulk_pay'),
                    });
                } else {
                    app.globalData.is_config(this, 'init_config');
                }
            },

            // 获取数据
            init() {
                var user = app.globalData.get_user_info(this, 'init');
                if (user != false) {
                    // 获取数据
                    this.get_data_list();
                } else {
                    this.setData({
                        data_list_loding_status: 0,
                        data_bottom_line_status: false,
                    });
                }
            },

            // 获取数据
            get_data_list(is_mandatory) {
                if ((is_mandatory || 0) == 0 && this.data_bottom_line_status == true) {
                    uni.stopPullDownRefresh();
                    return false;
                }
                if (this.data_is_loading == 1) return false;
                
                this.setData({ data_is_loading: 1, data_list_loding_status: 1 });
                if (this.data_page > 1) {
                    uni.showLoading({ title: this.$t('common.loading_in_text') });
                }
                
                var order_status = (this.nav_status_list[this.nav_status_index] || null) == null ? -1 : this.nav_status_list[this.nav_status_index]['value'];
                
                var params = { pageNum: this.data_page, pageSize: 10 };
                if (order_status !== '-1') {
                    params.status = [parseInt(order_status)];
                }
                if (this.search_keywords) {
                    params.keywords = this.search_keywords;
                }
                listOrdersWithPage(params).then((res) => {
                    if (this.data_page > 1) uni.hideLoading();
                    uni.stopPullDownRefresh();
                    if (res.code === '00000' && res.data && res.data.list) {
                        var list = res.data.list || [];
                        var mapped = list.map(function(o) { return mapOrder(o); });
                        // 翻译 status_name（mapOrder 返回中文原文，在此统一翻译）
                        var statusMap = {
                            '待付款': this.$t('common.wait_pay'), '待发货': this.$t('common.wait_deliver'),
                            '待收货': this.$t('common.wait_receive'), '已完成': this.$t('common.completed'),
                            '已取消': this.$t('common.cancelled'), '售后中': this.$t('common.refunding'),
                            '已退款': this.$t('common.refunded'), '未知': this.$t('common.unknown'),
                        };
                        mapped.forEach(function(item) {
                            if (statusMap[item.status_name]) item.status_name = statusMap[item.status_name];
                        });
                        var temp = this.data_page <= 1 ? mapped : (this.data_list || []).concat(mapped);
                        var noMore = list.length < 10;
                        this.setData({
                            data_list: temp,
                            data_total: res.data.total || 0,
                            data_page: this.data_page + 1,
                            data_page_total: Math.ceil((res.data.total || 0) / 10),
                            data_list_loding_status: temp.length > 0 ? 3 : 0,
                            data_is_loading: 0,
                            data_bottom_line_status: temp.length > 0 && this.data_page > 1 && noMore,
                        });
                    } else {
                        this.setData({ data_list_loding_status: 0, data_is_loading: 0 });
                        if (this.data_page <= 1) {
                            this.setData({ data_list: [], data_bottom_line_status: false });
                        }
                    }
                }).catch(() => {
                    if (this.data_page > 1) uni.hideLoading();
                    uni.stopPullDownRefresh();
                    this.setData({ data_list_loding_status: 2, data_is_loading: 0 });
                    
                });
            },

            // 滚动加载
            scroll_lower(e) {
                this.get_data_list();
            },

            // 支付 - 为单个订单创建支付单后弹窗支付
            pay_event(e) {
                var orderNo = e.currentTarget.dataset.oid || '';
                if (!orderNo) {
                    app.globalData.showToast('订单号异常');
                    return;
                }

                uni.showLoading({ title: this.$t('common.processing_in_text') });
                var self = this;
                var newPaymentNo = null;

                createOrderPayment(orderNo).then(function(res) {
                    if (res.code !== '00000') {
                        throw new Error(res.msg || '创建支付单失败');
                    }
                    newPaymentNo = res.data;
                    return getCashierInfo(newPaymentNo, self.deviceType);
                }).then(function(res) {
                    uni.hideLoading();
                    if (res.code === '00000' && res.data) {
                        var payAmount = res.data.payAmount || 0;
                        var channels = (res.data.channels || []).map(function(ch) {
                            return {
                                id: ch.channelCode,
                                payment: ch.channelCode,
                                name: ch.channelName,
                                logo: ch.channelIcon || '',
                                tips: ch.channelDesc || '',
                                is_active: ch.enabled !== false ? 0 : 1,
                                meta: ch.meta || {}
                            };
                        });

                        var enabledList = channels.filter(function(c) { return c.is_active === 0; });
                        var defaultId = enabledList.length > 0 ? enabledList[0].id : 0;

                        self.setData({
                            is_show_payment_popup: true,
                            paymentNo: newPaymentNo,
                            temp_pay_value: newPaymentNo,
                            payment_list: channels,
                            payment_id: defaultId,
                            default_payment_id: defaultId,
                            pay_price: payAmount,
                        });
                    } else {
                        app.globalData.showToast(res.msg || '获取支付信息失败');
                    }
                }).catch(function(err) {
                    uni.hideLoading();
                    if (err && err.message) app.globalData.showToast(err.message);
                });
            },

            // 支付弹窗关闭
            payment_popup_event_close(e) {
                this.setData({
                    is_show_payment_popup: false,
                });
            },

            // 重新打开支付弹窗
            _reopen_payment_popup() {
                var self = this;
                if (!self.paymentNo) return;
                getCashierInfo(self.paymentNo, self.deviceType).then(function(cr) {
                    if (cr && cr.code === '00000' && cr.data) {
                        var channels = (cr.data.channels || []).map(function(ch) {
                            return {
                                id: ch.channelCode, payment: ch.channelCode,
                                name: ch.channelName, logo: ch.channelIcon || '',
                                tips: ch.channelDesc || '', is_active: ch.enabled !== false ? 0 : 1,
                                meta: ch.meta || {}
                            };
                        });
                        var enabledList = channels.filter(function(c) { return c.is_active === 0; });
                        var prevId = self._saved_payment_id || self.payment_id;
                        var keepId = enabledList.some(function(c) { return c.id === prevId; }) ? prevId : (enabledList.length > 0 ? enabledList[0].id : 0);
                        self.setData({
                            is_show_payment_popup: true,
                            payment_list: channels,
                            payment_id: keepId,
                            default_payment_id: keepId,
                            pay_price: cr.data.payAmount || 0
                        });
                    }
                });
            },

            // 支付成功回调 - 重新加载列表（弹窗由组件内部控制跳转paytips）
            order_item_pay_success_handle(data) {
                // 重新加载当前标签数据
                this.setData({
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false,
                });
                this.get_data_list(1);
            },

            // 支付失败回调
            order_item_pay_fail_handle(data) {
                this.setData({
                    is_show_payment_popup: false,
                });
            },

            // 取消
            cancel_event(e) {
                uni.showModal({
                    title: this.$t('common.warm_tips'),
                    content: this.$t('common.cancel_confirm_tips'),
                    confirmText: this.$t('common.confirm'),
                    cancelText: this.$t('common.no'),
                    success: (result) => {
                        if (result.confirm) {
                            var id = e.currentTarget.dataset.value;
                            var index = e.currentTarget.dataset.index;
                            uni.showLoading({ title: this.$t('common.processing_in_text') });
                            cancelOrder(id).then((res) => {
                                uni.hideLoading();
                                var temp = this.data_list;
                                temp[index]['status'] = 5;
                                temp[index]['status_name'] = this.$t('order.order.1k98tk');
                                temp[index]['operate_data']['is_cancel'] = 0;
                                temp[index]['operate_data']['is_pay'] = 0;
                                temp[index]['operate_data']['is_delete'] = 1;
                                temp[index]['is_can_launch_aftersale'] = 0;
                                this.setData({ data_list: temp });
                                app.globalData.showToast(res.msg, 'success');
                            }).catch(() => {
                                uni.hideLoading();
                                
                            });
                        }
                    },
                });
            },

            // 收货
            collect_event(e) {
                var transactionid = e.currentTarget.dataset.transactionid || null;
                if(transactionid == null || !uni.openBusinessView) {
                    this.collect_hand_handle(e);
                } else {
                    // 存在微信单号收货数据，则拉起微信确认收货组件、失败则调用手动收货模式
                    var self = this;
                    uni.openBusinessView({
                        businessType: 'weappOrderConfirm',
                        extraData: {
                            transaction_id: transactionid
                        },
                        success(res) {
                            self.collect_handle(e);
                        },
                        fail(res) {
                            if((res.status || null) == 'fail') {
                                self.collect_hand_handle(e);
                            }
                        }
                    });
                }
            },

            // 手动收货
            collect_hand_handle(e) {
                uni.showModal({
                    title: this.$t('common.warm_tips'),
                    content: this.$t('orderallot-list.orderallot-list.o3ouqv'),
                    confirmText: this.$t('common.confirm'),
                    cancelText: this.$t('common.no'),
                    success: (result) => {
                        if (result.confirm) {
                            this.collect_handle(e);
                        }
                    },
                });
            },

            // 收货处理
            collect_handle(e) {
                var id = e.currentTarget.dataset.value;
                var index = e.currentTarget.dataset.index;
                uni.showLoading({ title: this.$t('common.processing_in_text') });
                var self = this;
                confirmReceive(id).then((res) => {
                    uni.hideLoading();
                    var temp_data_list = self.data_list;
                    temp_data_list[index]['status'] = 4;
                    temp_data_list[index]['status_name'] = self.$t('order.order.15lr5l');
                    temp_data_list[index]['operate_data']['is_collect'] = 0;
                    temp_data_list[index]['operate_data']['is_comments'] = 1;
                    temp_data_list[index]['operate_data']['is_delete'] = 1;
                    self.setData({ data_list: temp_data_list });
                    app.globalData.showToast(res.msg, 'success');
                }).catch(() => {
                    uni.hideLoading();
                    
                });
            },

            // 删除
            delete_event(e) {
                uni.showModal({
                    title: this.$t('common.warm_tips'),
                    content: this.$t('common.delete_confirm_tips'),
                    confirmText: this.$t('common.confirm'),
                    cancelText: this.$t('common.no'),
                    success: (result) => {
                        if (result.confirm) {
                            var id = e.currentTarget.dataset.value;
                            var index = e.currentTarget.dataset.index;
                            uni.showLoading({ title: this.$t('common.processing_in_text') });
                            deleteOrder(id).then((res) => {
                                uni.hideLoading();
                                var temp = this.data_list;
                                temp.splice(index, 1);
                                this.setData({ data_list: temp, data_list_loding_status: temp.length == 0 ? 0 : 3, data_bottom_line_status: temp.length == 0 ? false : this.data_bottom_line_status });
                                app.globalData.showToast(res.msg, 'success');
                            }).catch(() => {
                                uni.hideLoading();
                                
                            });
                        }
                    },
                });
            },

            // 催催
            rush_event(e) {
                app.globalData.showToast(this.$t('user-order.user-order.f5qqhu'));
            },

            // 导航事件
            nav_event(e) {
                this.setData({
                    order_select_ids: [],
                    nav_status_index: e.currentTarget.dataset.index || 0,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });

                // 重新拉取数据
                this.get_data_list(1);
            },

            // 售后订单事件
            orderaftersale_event(e) {
                var oid = e.currentTarget.dataset.oid || 0;
                var did = e.currentTarget.dataset.did || 0;
                if (oid == 0 || did == 0) {
                    app.globalData.showToast(this.$t('user-order-detail.user-order-detail.5k6k56'));
                    return false;
                }

                // 进入售后页面
                app.globalData.url_open('/pages/user-orderaftersale-detail/user-orderaftersale-detail?oid=' + oid + '&did=' + did);
            },

            // 选中处理
            selected_event(e) {
                var oid = e.currentTarget.dataset.oid || 0;
                var price = e.currentTarget.dataset.price || 0;
                var payment_id = e.currentTarget.dataset.payment || '';
                var temp_select_ids = this.order_select_ids;
                var temp_mult_pay_price = this.mult_pay_price;
                var temp_payment_id = this.mult_payment_id_list;
                if (temp_select_ids.indexOf(oid) == -1) {
                    temp_select_ids.push(oid);
                    temp_mult_pay_price.push(price);
                    temp_payment_id.push(payment_id);
                } else {
                    for (var i in temp_select_ids) {
                        if (temp_select_ids[i] == oid) {
                            temp_select_ids.splice(i, 1);
                            temp_mult_pay_price.splice(i, 1);
                            temp_payment_id.splice(i, 1);
                        }
                    }
                }
                this.setData({
                    order_select_ids: temp_select_ids,
                    mult_pay_price: temp_mult_pay_price,
                    mult_payment_id_list: temp_payment_id,
                });
            },

            // 合并支付
            pay_merge_event(e) {
                var num = 0;
                if (this.mult_pay_price.length > 0) {
                    num = this.mult_pay_price.reduce((old, now) => {
                        return Number(old) + Number(now);
                    }, 0);
                }
                this.setData({
                    is_show_payment_popup: true,
                    temp_pay_value: this.order_select_ids.join(','),
                    pay_price: Math.round(parseFloat(num) * 100) / 100,
                    payment_id: this.order_select_ids.length > 1 ? this.default_payment_id : this.mult_payment_id_list[0],
                });
            },

            // url事件
            url_event(e) {
                app.globalData.url_event(e);
            },

            // 关键字搜索
            search_button_event(e) {
                this.setData({
                    search_keywords: e,
                    order_select_ids: [],
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.get_data_list(1);
            },
        },
    };

    // === mall-oms 适配 ===
    function mapOrder(o) {
        var items = (o.orderItems || []).map(function(oi) {
            return {
                id: oi.skuId,
                title: oi.skuName || oi.spuName || '',
                images: oi.picUrl || '',
                price: oi.price || 0,
                buy_number: oi.quantity || 1,
                spec: oi.specText ? [{ value: oi.specText }] : null,
            };
        });
        var sm = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '售后中', 6: '已退款' };
        // 页面状态值比订单服务状态值大 1，查询时需要转换。
        var srcStatus = o.status + 1;
        // 线下支付审核中
        var isOfflinePending = o.offlinePayAuditStatus === 0;
        return {
            id: o.orderNo,
            status: srcStatus,
            status_name: o.statusLabel || sm[o.status] || '未知',
            is_under_line_text: isOfflinePending ? '审核中' : null,
            items: items,
            buy_number_count: items.reduce(function(s, i) { return s + i.buy_number; }, 0),
            total_price: o.paymentAmount || o.totalAmount || 0,
            currency_data: { currency_symbol: '¥' },
            operate_data: {
                is_cancel: o.status == 0 ? 1 : 0,
                is_pay: isOfflinePending ? 0 : (o.status == 0 ? 1 : 0),
                is_collect: o.status == 2 ? 1 : 0,
                is_comments: o.canComment ? 1 : 0,
                is_delete: o.status == 4 ? 1 : 0,
            },
            order_model: 0,
            payment_id: o.paymentNo || 0,
            is_can_launch_aftersale: o.canApplyAfterSale ? 1 : 0,
            // 分组标题：优先店铺名，无则回退订单号（对齐行业订单头部显示店铺名）
            warehouse_name: o.shopName || o.orderNo || '',
            warehouse_url: '/pages/plugins/shop/detail/detail?id=' + (o.shopId || ''),
            warehouse_icon: o.shopLogo || '',
        };
    }
 </script>
<style scoped>
    @import './user-order.css';
</style>
