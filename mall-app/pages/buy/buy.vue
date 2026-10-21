<template>
    <view :class="theme_view">
        <view v-if="orders.length > 0" class="page-bottom-fixed">
            <view class="padding-horizontal-main padding-top-main bottom-line-exclude">
                <!-- 收货地址 -->
                <view class="padding-horizontal-main padding-top-main border-radius-main bg-white spacing-mb">
                    <view class="address arrow-right cp" @tap="address_event">
                        <view v-if="address != null" class="padding-bottom-main">
                            <view class="address-base">
                                <text v-if="address.alias" class="address-alias br-main cr-main round margin-right-sm text-size-xs">{{ address.alias }}</text>
                                <text>{{ address.consigneeName }}</text>
                                <text class="fr">{{ address.consigneeMobile }}</text>
                            </view>
                            <view class="address-detail oh margin-top-lg">
                                <view class="dis-inline-block va-m">
                                    <iconfont name="icon-location" size="34rpx" color="#999"></iconfont>
                                </view>
                                <view class="text fr">{{ address.provinceName }}{{ address.cityName }}{{ address.areaName }}{{ address.detailAddress }}</view>
                            </view>
                        </view>
                        <view v-else class="padding-top-xl padding-bottom-xxxl cr-grey">{{ $t('buy.buy.6rk813') }}</view>
                    </view>
                    <view class="address-divider spacing-mb"></view>
                </view>

                <!-- 多子订单分组展示 -->
                <view v-for="(order, oIndex) in orders" :key="oIndex">
                    <view class="goods-group-list padding-main border-radius-main bg-white spacing-mb">
                        <!-- 分组标题 -->
                        <view class="goods-group-hd oh br-b padding-bottom-main">
                            <text class="goods-group-title">{{ order.shopName || $t('buy.buy.supplier_fallback') }}</text>
                        </view>
                        <!-- 商品列表 -->
                        <view class="goods-content margin-bottom-sm">
                            <view v-for="(item, iIndex) in order.orderItems" :key="iIndex" class="goods-item padding-vertical-main oh br-b-dashed">
                                <view class="goods-item-content">
                                    <image class="goods-image fl radius" :src="item.picUrl" mode="aspectFill"></image>
                                    <view class="goods-base">
                                        <view class="goods-title multi-text">{{ item.skuName || item.spuName }}</view>
                                        <view class="oh pr margin-top-xs">
                                            <text class="fw-b va-m">{{ currency_symbol }}{{ item.price }}</text>
                                            <text v-if="item.retailPrice > item.price" class="original-price margin-left-sm">{{ currency_symbol }}{{ item.retailPrice }}</text>
                                            <text class="buy-number pa cr-grey">x{{ item.quantity }}</text>
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                        <!-- 运费 + 抵扣 -->
                        <view v-if="order.freightAmount > 0 || order.pointsDeductionAmount > 0" class="extension-list radius padding-sm">
                            <view v-if="order.freightAmount > 0" class="item oh padding-vertical-xs">
                                <text class="cr-base fl">{{ $t('buy.buy.freight') }}</text>
                                <text class="text-tips fr">{{ currency_symbol }}{{ order.freightAmount }}</text>
                            </view>
                            <view v-if="order.pointsDeductionAmount > 0" class="item oh padding-vertical-xs">
                                <text class="cr-base fl">{{ $t('buy.buy.points_deduction') }}</text>
                                <text class="text-tips fr cr-red">-{{ currency_symbol }}{{ order.pointsDeductionAmount }}</text>
                            </view>
                            <view v-if="order.hasForbiddenAreaItems" class="item oh padding-vertical-xs">
                                <text class="cr-red">{{ $t('buy.buy.forbidden_area') }}{{ order.forbiddenItemNames.join('、') }}</text>
                            </view>
                        </view>
                        <!-- 小计 -->
                        <view class="oh tr wh-auto goods-group-footer padding-top-xl">
                            <text class="sales-price">{{ currency_symbol }}{{ order.paymentAmount }}</text>
                        </view>
                    </view>
                </view>

                <!-- 留言 -->
                <view class="content-textarea-container padding-main border-radius-main bg-white spacing-mb">
                    <view class="content">
                        <textarea v-if="user_note_status" class="textarea" @blur="bind_user_note_blur_event" @input="bind_user_note_event" :focus="true" :value="user_note_value" maxlength="230" :placeholder="$t('buy.buy.order_note')"></textarea>
                        <view v-else @tap="bind_user_note_tap_event" :class="'textarea-view ' + (user_note_value ? '' : 'cr-grey')">{{ user_note_value || $t('buy.buy.order_note') }}</view>
                    </view>
                </view>
            </view>

            <!-- 底部提交栏 -->
            <view class="bottom-fixed" :style="bottom_fixed_style">
                <view class="bottom-line-exclude">
                    <view class="item oh round flex-row jc-sb align-c br-top-shadow bg-white padding-vertical-sm">
                        <view class="nav-base single-text padding-left-main fl">
                            <text>{{ $t('buy.buy.total') }}</text>
                            <text class="sales-price">{{ currency_symbol }}{{ totalPaymentAmount }}</text>
                        </view>
                        <view class="nav-submit padding-horizontal-main fr">
                            <button class="btn bg-main cr-white round text-size-md" type="default" @tap="buy_submit_event" :disabled="buy_submit_disabled" hover-class="none">{{ $t('buy.buy.4884hk') }}</button>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <view v-else>
            <component-no-data :propStatus="loading_status" :propMsg="loading_msg"></component-no-data>
        </view>

        <component-common ref="common"></component-common>

        <!-- 支付弹窗 -->
        <component-payment
            ref="payment"
            :propCurrencySymbol="payment_currency_symbol"
            :propCashierPayUrl="cashier_pay_url"
            :propPaymentList="payment_list"
            propPayDataKey="paymentNo"
            :propTempPayValue="temp_pay_value"
            :propPaymentId="payment_id"
            :propDefaultPaymentId="default_payment_id"
            :propPayPrice="pay_price"
            :propIsShowPayment="is_show_payment_popup"
            :propPaymentNo="paymentNo"
            :propDeviceType="deviceType"
            :propToFailPage="to_fail_page"
            :propToPageBack="to_page_back"
            :propIsRedirectTo="true"
            @close-payment-popup="on_payment_popup_close"
            @pay-success="on_pay_success"
            @pay-fail="on_pay_fail"
        ></component-payment>
    </view>
</template>

<script>
    const app = getApp();
    import config from '@/config'
    import { request } from '@/config/request';
    import { getCashierInfo } from '@/api/pay.js'
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import componentPayment from '@/components/payment/payment';
    import iconfont from '@/components/iconfont/iconfont';
    import base64 from '@/common/js/lib/base64.js';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                theme_color: app.globalData.get_theme_color(),
                currency_symbol: app.globalData.currency_symbol(),
                loading_status: 1,
                loading_msg: '',
                buy_submit_disabled: false,
                bottom_fixed_style: '',
                orderToken: '',
                orders: [],
                totalPaymentAmount: '0.00',
                subOrderCount: 0,
                address: null,
                skuId: null,
                count: 1,
                cartSkuIds: [],
                user_note_value: '',
                user_note_status: false,
                // 支付弹窗
                is_show_payment_popup: false,
                payment_result_handled: false,
                paymentNo: '',
                payment_list: [],
                pay_price: 0,                payment_id: 0,
              default_payment_id: 0,
              temp_pay_value: '',

                deviceType: 'H5',
                // #ifdef MP-WEIXIN
                deviceType: 'MINI',
                // #endif
                // #ifdef APP-PLUS
                deviceType: 'APP',
                // #endif
                cashier_pay_url: '',
                payment_currency_symbol: app.globalData.currency_symbol(),
                to_page_back: {
                    title: this.$t('buy.buy.view_order'),
                    page: '/pages/user-order/user-order',
                },
                to_fail_page: '/pages/user-order/user-order',
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentPayment,
            iconfont
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            if (params.data) {
                try {
                    var rawData = JSON.parse(decodeURIComponent(base64.decode(decodeURIComponent(params.data))));
                    if (rawData.buy_type === 'goods') {
                        var goodsData = JSON.parse(decodeURIComponent(base64.decode(decodeURIComponent(rawData.goods_data))))[0];
                        this.skuId = goodsData.goods_id;
                        this.count = goodsData.stock || 1;
                    }
                } catch (e) {
                    console.error('解析订单参数失败', e);
                }
            }
            // 会员卡立即开通：裸 skuId 跳订单确认页
            if (params.skuId) {
                this.skuId = parseInt(params.skuId);
                this.count = parseInt(params.count) || 1;
            }
            // 购物车结算：选中 SKU 列表
            if (params.skuIds) {
                this.cartSkuIds = params.skuIds.split(',').map(function(id) { return parseInt(id); });
            }
            // 删除地址缓存（防跨用户串数据）
            uni.removeStorageSync(app.globalData.data.cache_buy_user_address_select_key);
            this.init();
        },

        onShow() {
            app.globalData.page_event_onshow_handle();
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
            // 已有支付单时重开支付弹窗（跳过 init）
            if (this._needs_reopen_payment_popup) {
                this._needs_reopen_payment_popup = false;
                this._reopen_payment_popup();
                return;
            }
            // 从地址选择页返回后刷新数据
            if (this.orderToken) {
                this.init();
            }
        },

        methods: {
            init() {
                var self = this;
                self.loading_status = 1;
                var confirmParams = {};
                if (self.skuId) {
                    confirmParams.skuId = self.skuId;
                    confirmParams.count = self.count;
                }
                // 从地址选择页返回时，读取缓存地址
                var cacheAddress = uni.getStorageSync(app.globalData.data.cache_buy_user_address_select_key);
                if (cacheAddress) {
                    self.setData({ address: cacheAddress });
                    confirmParams.addressId = cacheAddress.id;
                } else if (self.address && self.address.id) {
                    confirmParams.addressId = self.address.id;
                }

                request({
                    url: '/mall-oms/app-api/v1/orders/confirm',
                    method: 'POST',
                    params: confirmParams,
                    headers: { auth: true }
                }).then(function(res) {
                    if (res.code === '00000') {
                        var data = res.data;
                        var orders = (data.orders || []).map(function(order) {
                            return {
                                shopId: order.shopId,
                                shopName: order.shopName,
                                spuType: order.spuType,
                                orderItems: (order.orderItems || []).map(function(item) {
                                    return {
                                        skuId: item.skuId,
                                        skuName: item.skuName,
                                        spuName: item.spuName,
                                        picUrl: item.picUrl,
                                        price: item.price,
                                        retailPrice: item.retailPrice || 0,
                                        quantity: item.quantity
                                    };
                                }),
                                freightAmount: order.freightAmount || 0,
                                paymentAmount: order.paymentAmount || 0,
                                productDeductionAmount: order.productDeductionAmount || 0,
                                pointsDeductionAmount: order.pointsDeductionAmount || 0,
                                hasForbiddenAreaItems: order.hasForbiddenAreaItems || false,
                                forbiddenItemNames: order.forbiddenItemNames || []
                            };
                        });
                        self.setData({
                            orderToken: data.orderToken,
                            orders: orders,
                            totalPaymentAmount: data.totalPaymentAmount || 0,
                            subOrderCount: data.subOrderCount || orders.length,
                            address: self.address || data.defaultAddress || null,
                            loading_status: 3
                        });
                    } else {
                        self.setData({ loading_status: 2, loading_msg: res.msg || self.$t('buy.buy.load_failed') });
                    }
                }).catch(function() {
                    self.setData({ loading_status: 2, loading_msg: self.$t('buy.buy.network_retry') });
                });
            },

            address_event() {
                var self = this;
                uni.navigateTo({ url: '/pages/user-address/user-address?is_back=1' });
            },

            bind_user_note_event(e) {
                this.setData({ user_note_value: e.detail.value });
            },
            bind_user_note_tap_event() {
                this.setData({ user_note_status: true });
            },
            bind_user_note_blur_event() {
                this.setData({ user_note_status: false });
            },

            // 支付弹窗关闭
            on_payment_popup_close() {
                this.setData({ is_show_payment_popup: false });
                if (!this.payment_result_handled) {
                    uni.redirectTo({ url: '/pages/user-order/user-order' });
                }
                this.payment_result_handled = false;
            },

            // 重新打开支付弹窗（已有订单重复点击提交）
            _reopen_payment_popup() {
                var self = this;
                if (!self.paymentNo) return;
                getCashierInfo(self.paymentNo, self.deviceType).then(function(cr) {
                    if (cr && cr.code === '00000' && cr.data) {
                        var channels = (cr.data.channels || []).map(function(ch) {
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
                        var prevId = self._saved_payment_id || self.payment_id;
                        var keepId = enabledList.some(function(c) { return c.id === prevId; }) ? prevId : (enabledList.length > 0 ? enabledList[0].id : 0);
                        self.setData({
                            is_show_payment_popup: true,
                            payment_list: channels,
                            payment_id: keepId,
                            default_payment_id: keepId,
                            pay_price: cr.data.payAmount || 0,
                            cashier_pay_url: config.url + '/mall-pay/app-api/v1/cashier/pay',
                        });
                    } else {
                        uni.showToast({ title: self.$t('buy.buy.get_pay_failed'), icon: 'none' });
                    }
                }).catch(function() {
                    uni.showToast({ title: self.$t('buy.buy.network_error'), icon: 'none' });
                });
            },

            // 支付成功 → 跳转 paytips
            on_pay_success() {
                this.setData({ payment_result_handled: true });
                const url_data = Object.assign({ code: '9000' }, this.to_page_back);
                uni.redirectTo({ url: '/pages/paytips/paytips?params=' + encodeURIComponent(base64.encode(JSON.stringify(url_data))) });
            },

            // 支付失败 → 跳转订单列表
            on_pay_fail() {
                this.setData({ payment_result_handled: true });
                uni.redirectTo({ url: '/pages/user-order/user-order' });
            },

            // 提交订单并弹窗支付
            buy_submit_event() {
                var self = this;
                // 已有 paymentNo 说明已提交过订单，直接重开弹窗，避免重复提交
                if (self.paymentNo) {
                    self._reopen_payment_popup();
                    return;
                }
                if (!self.address) {
                    uni.showToast({ title: this.$t('buy.buy.6rk813'), icon: 'none' });
                    return;
                }
                // 检查禁售
                var hasForbidden = false;
                for (var i = 0; i < self.orders.length; i++) {
                    if (self.orders[i].hasForbiddenAreaItems) {
                        hasForbidden = true;
                        break;
                    }
                }
                if (hasForbidden) {
                    uni.showToast({ title: this.$t('buy.buy.forbidden_submit'), icon: 'none' });
                    return;
                }

                self.setData({ buy_submit_disabled: true });

                // 平铺所有子订单的 orderItems
                var flatItems = [];
                for (var i = 0; i < self.orders.length; i++) {
                    var items = self.orders[i].orderItems;
                    for (var j = 0; j < items.length; j++) {
                        flatItems.push({
                            skuId: items[j].skuId,
                            quantity: items[j].quantity
                        });
                    }
                }

                request({
                    url: '/mall-oms/app-api/v1/orders/submit',
                    method: 'POST',
                    data: {
                        orderToken: self.orderToken,
                        orderSource: 'APP',
                        shippingAddress: {
                            id: self.address.id,
                            consigneeName: self.address.consigneeName,
                            consigneeMobile: self.address.consigneeMobile,
                            province: self.address.provinceName,
                            city: self.address.cityName,
                            district: self.address.areaName,
                            detailAddress: self.address.detailAddress
                        },
                        paymentAmount: self.totalPaymentAmount,
                        remark: self.user_note_value,
                        shareUserId: parseInt((uni.getStorageSync('user') || {}).referrer) || undefined,
                        orderItems: flatItems
                    },
                    headers: { auth: true }
                }).then(function(res) {
                    self.setData({ buy_submit_disabled: false });
                    if (res.code === '00000') {
                        var result = res.data;
                        if (result.needPay) {
                            // 弹窗支付，不跳转收银台
                            self.setData({ cashier_pay_url: config.url + '/mall-pay/app-api/v1/cashier/pay' });
                            getCashierInfo(result.paymentNo, self.deviceType).then(function(cr) {
                                if (cr && cr.code === '00000' && cr.data) {
                                    var channels = (cr.data.channels || []).map(function(ch) {
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
                                        paymentNo: result.paymentNo,
                                        temp_pay_value: result.paymentNo,
                                        payment_list: channels,
                                        payment_id: defaultId,
                                        default_payment_id: defaultId,
                                        pay_price: cr.data.payAmount || 0,
                                        cashier_pay_url: config.url + '/mall-pay/app-api/v1/cashier/pay',
                                    });
                                } else {
                                    uni.showToast({ title: self.$t('buy.buy.get_pay_failed_retry'), icon: 'none' });
                                }
                            }).catch(function() {
                                uni.showToast({ title: self.$t('buy.buy.network_error'), icon: 'none' });
                            });
                        } else {
                            var tipsParams = base64.encode(JSON.stringify({
                                code: '9000',
                                msg: self.$t('buy.buy.pay_success'),
                                title: self.$t('buy.buy.view_order'),
                                order_no: result.orderNo,
                                page: '/pages/user-order/user-order'
                            }));
                            uni.redirectTo({ url: '/pages/paytips/paytips?params=' + encodeURIComponent(tipsParams) });
                        }
                    } else {
                        uni.showToast({ title: res.msg || self.$t('buy.buy.submit_failed'), icon: 'none' });
                    }
                }).catch(function() {
                    self.setData({ buy_submit_disabled: false });
                    uni.showToast({ title: self.$t('buy.buy.network_retry'), icon: 'none' });
                });
            }
        }
    };
</script>

<style>
@import './buy.css';
</style>
