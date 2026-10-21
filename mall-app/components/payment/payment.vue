<template>
    <view :class="theme_view">
        <!-- 支付二维码展示 -->
        <component-popup :propShow="popup_view_pay_qrcode_is_show" propPosition="bottom" @onclose="popup_view_pay_qrcode_event_close">
            <view class="padding-top-xxxl padding-bottom-xxxl padding-left-xxxl padding-right-xxxl tc">
                <block v-if="(popup_view_pay_data || null) == null || (popup_view_pay_data.qrcode_url || null) == null || (popup_view_pay_data.name || null) == null || (popup_view_pay_data.order_no || null) == null">
                    <text class="cr-grey">{{$t('payment.payment.973g2e')}}</text>
                </block>
                <block v-else>
                    <view class="fw-b text-size cr-base margin-bottom-sm">{{ popup_view_pay_data.name }}</view>
                    <view class="dis-inline-block">
                        <w-qrcode :options="{code:popup_view_pay_data.qrcode_url, size:450}"></w-qrcode>
                    </view>
                    <view v-if="(popup_view_pay_data.msg || null) != null" class="cr-yellow margin-top-sm">{{ popup_view_pay_data.msg }}</view>
                    <!-- #ifdef H5 -->
                    <view v-if="popup_view_pay_data.pay_url != null" class="margin-top-xl">
                        <a :href="popup_view_pay_data.pay_url" target="_blank" class="dis-inline-block cr-green">{{$t('payment.payment.z3y296')}}</a>
                    </view>
                    <!-- #endif -->
                </block>
            </view>
        </component-popup>
        <!-- 支付方式 popup -->
        <component-popup :propShow="is_show_payment_popup" propPosition="bottom" @onclose="payment_popup_event_close">
            <view class="poupon-title padding-main tc text-size-md pr">{{$t('payment.payment.iu792d')}}<iconfont name="icon-close-line" propClass="pa right-0 margin-right-main margin-top-xs" size="30rpx" color="#999" @tap="payment_popup_event_close"></iconfont>
            </view>
            <view class="payment-price tc padding-top-sm padding-bottom-sm br-b">
                <text class="text-size-md">{{ propCurrencySymbol }}</text>
                {{ propPayPrice }}
            </view>
            <view v-if="payment_list.length > 0" class="oh">
                <view class="payment-list">
                    <scroll-view scroll-y="true" class="scroll-y wh-auto" :style="{ maxHeight: isOfflineSelected ? '750rpx' : '430rpx' }">
                        <block v-for="(item, index) in payment_list" :key="index">
                            <view class="item br-b flex-row jc-sb align-c" :data-value="item.id" @tap="checked_payment">
                                <view class="flex-1">
                                    <image v-if="(item.logo || null) != null" class="icon va-m margin-right-sm" :src="item.logo" mode="widthFix"></image>
                                    <text class="va-m">{{ item.name }}</text>
                                    <text v-if="(item.tips || null) !== null" class="va-m cr-red">（{{ item.tips }}）</text>
                                </view>
                                <iconfont :name="payment_id == item.id ? 'icon-selected-solid' : 'icon-not-selected'" size="44rpx" :color="payment_id == item.id ? '#E22C08' : '#ccc'"></iconfont>
                            </view>
                            <!-- 线下支付：银行信息 + 凭证上传 -->
                            <view v-if="isOfflineSelected && item.payment === 'OFFLINE_PAY'" class="offline-pay-sub br-t-f5 padding-main">
                                <view v-if="offlineBankInfo.bankName" class="offline-info-area">
                                    <!-- 银行信息卡片 -->
                                    <view class="bank-card">
                                        <view class="bank-card-hd flex-row align-c jc-sb">
                                            <text class="text-size-sm fw-b">{{ offlineBankInfo.bankName }}</text>
                                            <text class="cr-grey text-size-xs">{{ offlineBankInfo.accountName }}</text>
                                        </view>
                                        <view class="bank-card-bd flex-row align-c jc-sb">
                                            <text class="account-no">{{ offlineBankInfo.accountNo }}</text>
                                            <text class="copy-btn cr-main text-size-xs" @tap.stop="copyOfflineAccount">复制</text>
                                        </view>
                                    </view>
                                    <text v-if="offlineBankInfo.tips" class="tips-text cr-grey text-size-xs">{{ offlineBankInfo.tips }}</text>

                                    <!-- 凭证上传 -->
                                    <view class="voucher-section">
                                        <view class="upload-row">
                                            <block v-for="(url, idx) in offlineVoucherUrls" :key="idx">
                                                <view class="upload-thumb" @tap.stop="previewOfflineVoucher(idx)">
                                                    <image :src="url" mode="aspectFill" class="thumb-img"></image>
                                                    <view class="thumb-del" @tap.stop="removeOfflineVoucher(idx)">×</view>
                                                </view>
                                            </block>
                                            <view v-if="offlineVoucherUrls.length < 9" class="upload-thumb upload-add-btn" @tap.stop="chooseOfflineVoucher">
                                                <text class="add-icon">+</text>
                                                <text class="cr-grey text-size-xs">凭证</text>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </block>
                    </scroll-view>
                </view>
                <view class="payment-submit">
                    <view class="bottom-line-exclude">
                        <button class="bg-main br-main cr-white round text-size" type="default" hover-class="none" @tap="popup_payment_event" :disabled="submit_disabled_status">{{$t('payment.payment.25r53g')}}</button>
                    </view>
                </view>
            </view>
            <view v-else class="padding-top-xxxl padding-bottom-xxxl oh bg-white tc cr-grey">{{$t('payment.payment.058a46')}}</view>
        </component-popup>

        <!-- 支付html展示 -->
        <component-popup :propShow="popup_view_pay_html_is_show" propPosition="bottom" @onclose="popup_view_pay_html_event_close">
            <view class="popup-pay-html-content padding-top-xxxl padding-bottom-xxxl padding-left-xxxl padding-right-xxxl tc">
                <block v-if="(popup_view_pay_data || null) == null">
                    <text class="cr-grey">{{$t('payment.payment.973g2e')}}</text>
                </block>
                <block v-else>
                    <mp-html :content="popup_view_pay_data" />
                </block>
            </view>
        </component-popup>

        <!-- 支付中提示弹窗 -->
        <view v-if="payment_confirm_modal_status" class="payment-confirm-modal">
            <view class="content padding-xl margin-xxl tc bg-white border-radius-main">
                <view class="padding-vertical-xxxxl">{{$t('common.payment_in_text')}}</view>
                <view class="margin-top-lg">
                    <button type="default" size="mini" class="bg-white br-black cr-black text-size-sm round margin-right-xxxxl" data-type="0" @tap="payment_confirm_event">{{$t('common.not_have_name')}}</button>
                    <button type="default" size="mini" class="bg-main br-main cr-white text-size-sm round margin-left-xxxxl" data-type="1" @tap="payment_confirm_event">{{$t('order.order.s8g966')}}</button>
                </view>
            </view>
        </view>
    </view>
</template>
<script>
    const app = getApp();
    import config from '@/config';
    import base64 from '@/common/js/lib/base64.js';
    import componentPopup from '@/components/popup/popup';
    export default {
        name: 'pay',
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                // 支付方式列表
                payment_list: [],
                // 弹窗开关
                is_show_payment_popup: false,
                popup_view_pay_qrcode_is_show: false,
                // 支付弹窗展示数据
                popup_view_pay_data: null,
                // 定时器
                pay_statuc_check_timer: null,
                // 支付id
                payment_id: 0,
                submit_disabled_status: true,
                order_id: 0,
                popup_view_pay_html_is_show: false,
                // 打开url地址定时任务和状态
                open_pay_url_timer: null,
                open_pay_url_status: true,
                // 微信小程序授权码
                code: '',
                // 支付返回数据
                pay_response_data: {},
                // 支付确认弹窗
                payment_confirm_modal_status: false,
                // 微信app
                weixinapp: null,
                // 线下支付
                isOfflineSelected: false,
                offlineBankInfo: {},
                offlineVoucherUrls: [],
                offlineSubmitting: false,
            };
        },
        props: {
            propCurrencySymbol: {
                type: String,
                default: app.globalData.currency_symbol(),
            },
            propPayUrl: {
                type: String,
                default: '',
            },
            propQrcodeUrl: {
                type: String,
                default: '',
            },
            propPaymentList: {
                type: Array,
                default: () => {
                    return [];
                },
            },
            propIsShowPayment: {
                type: Boolean,
                default: false,
            },
            // 订单id
            propTempPayValue: {
                type: [String, Number],
                default: '',
            },
            // 订单id参数名，默认为id
            propPayDataKey: {
                type: String,
                default: 'id',
            },
            // 订单下标  ---- 用于处理支付成功后前端修改成功状态
            propTempPayIndex: {
                type: [Number, String],
                default: 0,
            },
            // 支付id 值为0表示没有配置支付方式
            propPaymentId: {
                type: [Number, String],
                default: 0,
            },
            // 默认支付id 值为0表示没有默认
            propDefaultPaymentId: {
                type: [Number, String],
                default: 0,
            },
            // 付款金额
            propPayPrice: {
                type: [Number, String],
                default: 0,
            },
            // 支付跳转页面------跳转成功页面---后返回的页面
            propToPageBack: {
                type: Object,
                default: () => {
                    return {};
                },
            },
            // 指定所有页面跳转到指定页面------除现金支付外
            propToPage: {
                type: String,
                default: '',
            },
            // 现金支付-指定跳转页面----不进入充值成功页面：不配置则表示不跳转
            propToAppointPage: {
                type: String,
                default: '',
            },
            // 支付失败跳转页面------不传则停留在当前页面
            propToFailPage: {
                type: String,
                default: '',
            },
            //是否需要关闭页面进行跳转
            propIsRedirectTo: {
                type: Boolean,
                default: false,
            },
            // 判断错误时是否需要弹窗提示
            propIsFailAlert: {
                type: Boolean,
                default: true,
            },
            // 支付单号（新后端）
            propPaymentNo: {
                type: String,
                default: '',
            },
            // 设备类型 H5/MINI/APP
            propDeviceType: {
                type: String,
                default: 'H5',
            },
            // 收银台支付请求地址
            propCashierPayUrl: {
                type: String,
                default: '/mall-pay/app-api/v1/cashier/pay',
            },
            // 微信小程序授权码（由父组件传入，避免直接赋值）
            propCode: {
                type: String,
                default: '',
            },
            // 银行卡ID（由父组件传入，避免直接赋值）
            // 银行卡短信验证token（由父组件传入）
        },
        components: {
            componentPopup,
        },
        watch: {
            propCode(value) {
                this.setData({ code: value || '' });
            },
            propPaymentList(value, old_value) {
                this.setData({ payment_list: value });
                for (var i in value || []) {
                }
                // 线下支付：提取银行信息 + 如果是唯一/首选项则直接标记选中
                var hasOffline = false;
                for (var k in value || []) {
                    if (value[k].payment === 'OFFLINE_PAY') {
                        var meta = value[k].meta || {};
                        hasOffline = true;
                        this.setData({
                            offlineBankInfo: {
                                bankName: meta.bankName || '',
                                accountName: meta.accountName || '',
                                accountNo: meta.accountNo || '',
                                qrcode: meta.qrcode || '',
                                tips: meta.tips || ''
                            },
                            isOfflineSelected: value.length === 1 || (this.propDefaultPaymentId && this.propDefaultPaymentId == value[k].id)
                        });
                        break;
                    }
                }
                if (!hasOffline) {
                    this.setData({ isOfflineSelected: false });
                }
            },
            // 是否显示支付方式
            propIsShowPayment(new_val, old_val) {
                if (new_val !== old_val) {
                    var bool = true;
                    if (this.payment_list.length == 1) {
                        bool = false;
                        this.setData({
                            payment_id: this.payment_list[0].id,
                        });
                    } else {
                        var new_payment_id = (this.propPaymentId && this.propPaymentId != 0) ? this.propPaymentId : this.propDefaultPaymentId;
                        for (var i in this.payment_list) {
                            if (this.payment_list[i].id == new_payment_id) {
                                bool = false;
                                break;
                            }
                        }
                        this.setData({
                            payment_id: new_payment_id || 0,
                        });
                    }
                    
                    this.setData({
                        is_show_payment_popup: new_val,
                        submit_disabled_status: bool,
                    });

                    // 弹窗打开时提取线下支付银行信息
                    if (new_val) {
                        this.extractOfflineBankInfo();
                    }
                }
            },
        },
        // 页面被展示
        created: function () {
            this.setData({
                payment_list: this.propPaymentList,
                payment_id: Number(this.propPaymentId) == 0 ? this.propDefaultPaymentId : Number(this.propPaymentId),
                paymentNo: this.propPaymentNo,
            });

            // #ifdef APP
            // 是否存在微信app
            var self = this;
            plus.share.getServices(res => {
                let weixinapp = null;
                for(let i in res) {
                    if(res[i].id == 'weixin') {
                        weixinapp = res[i];
                        break;
                    }
                }
                if(weixinapp) {
                    self.setData({
                        weixinapp: weixinapp
                    });
                }
            });
            // #endif
        },
        methods: {
            // 支付弹窗关闭
            payment_popup_event_close(e) {
                this.setData({
                    is_show_payment_popup: false,
                });
                this.$emit('close-payment-popup', false);
            },
            // 支付二维码展示窗口事件
            popup_view_pay_qrcode_event_close(e) {
                // 关闭弹窗
                this.setData({
                    popup_view_pay_qrcode_is_show: false,
                });
                // 清除定时和支付数据
                clearInterval(this.pay_statuc_check_timer);
            },

            // 选择支付方式
            // 提取线下支付银行信息（选中或弹窗打开时调用）
            extractOfflineBankInfo() {
                var currentId = this.payment_id;
                if (!currentId) return;
                var selectedItem = null;
                for (var j = 0; j < this.payment_list.length; j++) {
                    if (this.payment_list[j].id == currentId) {
                        selectedItem = this.payment_list[j];
                        break;
                    }
                }
                if (selectedItem && selectedItem.payment === 'OFFLINE_PAY') {
                    var meta = selectedItem.meta || {};
                    this.setData({
                        offlineBankInfo: {
                            bankName: meta.bankName || '',
                            accountName: meta.accountName || '',
                            accountNo: meta.accountNo || '',
                            qrcode: meta.qrcode || '',
                            tips: meta.tips || ''
                        },
                        isOfflineSelected: true
                    });
                }
            },

            checked_payment(e) {
                var selectedId = e.currentTarget.dataset.value;
                var isOffline = false;
                for (var i in this.payment_list) {
                    if (this.payment_list[i].id == selectedId) {
                        isOffline = this.payment_list[i].payment === 'OFFLINE_PAY';
                        break;
                    }
                }
                this.setData({
                    payment_id: selectedId,
                    submit_disabled_status: isOffline ? (this.offlineVoucherUrls.length === 0) : false,
                    isOfflineSelected: isOffline,
                });
                if (isOffline) {
                    this.extractOfflineBankInfo();
                }
            },


            // 支付弹窗发起支付
            popup_payment_event() {
                // 线下支付：校验凭证
                if (this.isOfflineSelected && (!this.offlineVoucherUrls || this.offlineVoucherUrls.length === 0)) {
                    app.globalData.showToast(this.$t('cashier.cashier.upload_voucher_first'));
                    return;
                }
                if (this.submit_disabled_status) {
                    app.globalData.showToast(this.$t('payment.payment.x6d585'));
                    return false;
                }
                // 不提前关闭弹窗，银行卡 SMS 验证需要弹窗保持打开
                var self = this;
                // #ifdef MP-WEIXIN
                uni.login({
                    success: function (res) {
                        self.pay_handle(self.propTempPayValue, self.payment_id, [], res.code);
                    },
                    fail: function () {
                        uni.showToast({ title: '微信授权失败', icon: 'none' });
                    }
                });
                // #endif
                // #ifndef MP-WEIXIN
                this.pay_handle(this.propTempPayValue, this.payment_id);
                // #endif
            },

            // 支付方法
            pay_handle(order_id, payment_id = 0, payment_list = [], wxCode = '') {
                if((payment_list || null) != null && payment_list.length > 0) {
                    this.setData({ payment_list: payment_list });
                }
                this.pay_handle_event(order_id, payment_id, wxCode);
            },

            // 支付处理（适配新后端 cashier API）
            pay_handle_event(order_id, payment_id = 0, wxCode = '') {
                var payment = null;
                if ((payment_id || 0) != 0) {
                    for (var i in this.payment_list) {
                        if (this.payment_list[i]['id'] == (payment_id || this.payment_id)) {
                            payment = this.payment_list[i];
                        }
                    }
                    if (payment == null) {
                        app.globalData.showToast('支付方式不可用');
                        return false;
                    }
                }

                // 获取设备类型
                var deviceType = this.propDeviceType;
                // #ifdef MP-WEIXIN
                deviceType = 'MINI';
                // #endif
                // #ifdef APP-PLUS
                deviceType = 'APP';
                // #endif

                // 构建新后端请求参数
                var post_data = {
                    paymentNo: this.propPaymentNo || order_id,
                    payChannel: payment ? payment.payment : '',
                    deviceType: deviceType
                };
                // 线下支付：带上凭证
                if (payment && payment.payment === 'OFFLINE_PAY') {
                    post_data.voucherUrls = this.offlineVoucherUrls;
                }

                // #ifdef MP-WEIXIN
                if (wxCode) {
                    post_data.code = wxCode;
                }
                // #endif

                var payUrl = this.propCashierPayUrl;

                uni.showLoading({ title: '支付中...', mask: true });
                var self = this;
                uni.request({
                    url: payUrl,
                    method: 'POST',
                    data: post_data,
                    dataType: 'json',
                    header: {
                        'Content-Type': 'application/json',
                        'Authorization': uni.getStorageSync('token')
                    },
                    success: (res) => {
                        uni.hideLoading();
                        var responseData = res.data;
                        var payData = responseData.data;
                        this.setData({ pay_response_data: payData || {} });

                        if (responseData.code === '00000' && payData) {
                            // 线下支付：关闭弹窗 + 提示等待审核
                            if (payment && payment.payment === 'OFFLINE_PAY') {
                                this.setData({ is_show_payment_popup: false });
                                this.$emit('pay-success');
                                uni.showToast({ title: self.$t('cashier.cashier.voucher_submitted'), icon: 'success' });
                                return;
                            }

                            // 全抵扣/直接成功
                            if (payment && payment.payment === 'FULL_DEDUCTION') {
                                this.order_item_pay_success_handle(payData, order_id);
                                return;
                            }


                            // 支付宝 WAP (H5表单提交)
                            // #ifdef H5
                            if (payment && (payment.payment === 'ALIPAY_WAP' || payment.payment === 'Alipay')) {
                                if (payData.payParam) {
                                    var div = document.createElement('div');
                                    div.innerHTML = payData.payParam;
                                    document.body.appendChild(div);
                                    var fm = document.forms;
                                    if (fm.length > 0) {
                                        fm[fm.length - 1].submit();
                                        this.setData({ payment_confirm_modal_status: true });
                                        return;
                                    }
                                }
                            }
                            // #endif

                            // 支付宝 APP 支付
                            // #ifdef APP-PLUS
                            if (payment && payment.payment === 'ALIPAY_APP') {
                                if (payData.payParam) {
                                    uni.requestPayment({
                                        provider: 'alipay',
                                        orderInfo: payData.payParam,
                                        success: function(res) {
                                            self.order_item_pay_success_handle(payData, order_id);
                                        },
                                        fail: function(err) {
                                            self.order_item_pay_fail_handle(payData, order_id, '支付宝支付失败');
                                        }
                                    });
                                    return;
                                }
                            }
                            // #endif

                            // 微信 APP 支付
                            // #ifdef APP-PLUS
                            if (payment && payment.payment === 'WECHAT_APP') {
                                if (payData.payParam) {
                                    var wxAppParams = (typeof payData.payParam === 'string') ? JSON.parse(payData.payParam) : payData.payParam;
                                    uni.requestPayment({
                                        provider: 'wxpay',
                                        orderInfo: wxAppParams,
                                        success: function(res) {
                                            self.order_item_pay_success_handle(payData, order_id);
                                        },
                                        fail: function(err) {
                                            self.order_item_pay_fail_handle(payData, order_id, '微信支付失败');
                                        }
                                    });
                                    return;
                                }
                            }
                            // #endif

                            // 映射新响应格式到旧处理器兼容格式
                            var mappedData = self._map_cashier_response(payData, payment);
                            if (mappedData._skip) return;

                            // 使用原有平台处理器分发
                            if ((mappedData.is_success || 0) == 1) {
                                self.order_item_pay_success_handle(mappedData, order_id, false);
                                app.globalData.showToast('支付成功', 'success');
                                setTimeout(() => { self.to_success_page_event(); }, 2000);
                            } else {
                                let payment_type = Number(mappedData.is_payment_type || 0);
                                if (payment_type === 0) {
                                    // #ifdef APP
                                    self.app_pay_handle(self, mappedData, order_id);
                                    // #endif
                                    // #ifdef MP-WEIXIN || MP-ALIPAY || MP-BAIDU
                                    self.mp_pay_handle(self, mappedData, order_id);
                                    // #endif
                                    // #ifdef H5
                                    self.h5_pay_handle(self, mappedData, order_id);
                                    // #endif
                                } else if (payment_type === 1) {
                                    uni.showModal({
                                        content: responseData.msg || '线下支付',
                                        showCancel: false,
                                        confirmText: '确定',
                                        success(r) {
                                            if (r.confirm) self.to_other(order_id);
                                        },
                                    });
                                } else if (payment_type === 2) {
                                    self.order_item_pay_success_handle(mappedData, order_id);
                                }
                            }
                        } else {
                            self.order_item_pay_fail_handle(payData, order_id, responseData.msg || '支付请求失败');
                        }
                    },
                    fail: () => {
                        uni.hideLoading();
                        app.globalData.showToast('网络异常，请稍后重试');
                    },
                });
            },








            // 银行卡短信验证码确认支付（供 cashier.vue 调用）

            // 将新后端 cashier 响应映射为旧处理器兼容格式
            _map_cashier_response(payData, payment) {
                var channelPayment = payment ? payment.payment : '';

                // WECHAT_JSAPI: payParam 是微信支付参数 JSON 字符串
                if (channelPayment === 'WECHAT_JSAPI' || channelPayment === 'Weixin') {
                    var wxParams = (typeof payData.payParam === 'string') ? JSON.parse(payData.payParam) : payData.payParam;
                    return {
                        is_success: 0,
                        is_payment_type: 0,
                        data: wxParams,
                        payment: payment,
                        _skip: false
                    };
                }

                // 默认: 线上支付
                return {
                    is_success: 0,
                    is_payment_type: 0,
                    data: payData.payParam || payData,
                    payment: payment,
                    _skip: false
                };
            },
            // APP支付
            app_pay_handle(self, data, order_id) {
                var arr = {
                    Alipay: 'alipay',
                    AlipayCert: 'alipay',
                    ALIPAY_APP: 'alipay',
                    Weixin: 'wxpay',
                    WECHAT_APP: 'wxpay',
                    PayPal: 'paypal'
                }
                var pay_value = arr[data.payment.payment] || null;
                if(pay_value != null) {
                    uni.getProvider({
                        service: 'payment',
                        success: function (res) {
                            if(~res.provider.indexOf(pay_value)) {
                                var pay_data = ((data.data.pay_data || null) == null) ? data.data : data.data.pay_data;
                                uni.requestPayment({
                                    provider: pay_value,
                                    orderInfo: pay_data,
                                    success: function (res) {
                                        // 是否需要回调捕获
                                        var call_back_url = data.data.call_back_url || null;
                                        if(call_back_url != null) {
                                            uni.request({url: call_back_url, method: 'GET'});
                                        }
                    
                                        // 成功处理数据
                                        self.order_item_pay_success_handle(data, order_id);
                                    },
                                    fail: function (err) {
                                        self.order_item_pay_fail_handle(data, order_id, self.$t('paytips.paytips.6y488i'));
                                    }
                                });
                            } else {
                                app.globalData.showToast(data.payment.payment+self.$t('payment.payment.bv637f'));
                            }
                        }
                    });
                } else {
                    // 是否打开微信app
                    var status = true;
                    if(data.data.substr(0, 12) == 'weixinapp://') {
                        if(this.weixinapp == null) {
                            app.globalData.showToast(this.$t('detail.detail.86g7e1'));
                            return false;
                        }
                        var weixin_original_id = app.globalData.get_config('common_app_mini_weixin_share_original_id', null);
                        if(weixin_original_id == null) {
                            app.globalData.showToast(this.$t('detail.detail.567uyh'));
                            return false;
                        }
                        this.weixinapp.launchMiniProgram({
                            id: weixin_original_id,
                            type: 0,
                            path: data.data.substr(12)
                        });
                        status = false;
                    }

                    // 先清除定时任务
                    if(self.open_pay_url_timer != null) {
                        clearTimeout(self.open_pay_url_timer);
                    }
                    // 显示加载层
                    uni.showLoading({
                        title: self.$t('common.loading_in_text'),
                        mask: true
                    });
                    // 设置打开url状态
                    self.setData({
                        open_pay_url_status: true
                    });
                    // 打开url
                    if(status) {
                        plus.runtime.openURL(data.data, function(error) {
                            uni.hideLoading();
                            // 打开url失败、并进入提示失败环节
                            self.setData({
                                open_pay_url_status: false
                            });
                            self.order_item_pay_fail_handle(data, order_id, error.message+'('+error.code+')');
                        });
                    }
                    // 定时3秒后提示用户确认支付状态
                    self.open_pay_url_timer = setTimeout(function() {
                        if(self.open_pay_url_status) {
                            uni.hideLoading();
                            uni.showModal({
                                content: self.$t('payment.payment.sdfs31'),
                                showCancel: true,
                                cancelText: self.$t('common.not_have_name'),
                                confirmText: self.$t('order.order.s8g966'),
                                success(res) {
                                    if (res.confirm) {
                                        self.order_item_pay_success_handle(data, order_id);
                                    } else {
                                        self.order_item_pay_fail_handle(data, order_id, self.$t('paytips.paytips.6y488i'));
                                    }
                                },
                            });
                        }
                    }, 3000);
                }
            },
            // 快手小程序
            kuaishou_pay_handle(self, data, order_id) {
                uni.pay({
                    orderInfo: data.data,
                    serviceId: '1',
                    success: (res) => {
                        // 数据设置
                        self.order_item_pay_success_handle(data, order_id);
                    },
                    fail: (res) => {
                        self.order_item_pay_fail_handle(data, order_id, self.$t('paytips.paytips.6y488i'));
                    },
                });
            },
            // 头条小程序非普通交易支付处理
            toutiao_transaction_pay_handle(self, data, order_id) {
                if(!uni.canIUse('requestOrder') || !uni.canIUse('getOrderPayment')) {
                    app.globalData.showToast(self.$t('payment.payment.4dszme'));
                    return false;
                }
                uni.requestOrder({
                    data: data.data.data,
                    byteAuthorization: data.data.auth,
                    success: (res) => {
                        uni.getOrderPayment({
                            orderId: res.orderId,
                            success: (res) => {
                                // 数据设置
                                self.order_item_pay_success_handle(data, order_id);
                            },
                            fail: (res) => {
                                self.order_item_pay_fail_handle(data, order_id, self.$t('paytips.paytips.6y488i'));
                            }
                        });
                    },
                    fail: (res) => {
                        app.globalData.showToast(res.errMsg+'('+res.errNo+')');
                    }
                });
            },
            // 小程序: 微信、支付宝、百度、头条、QQ
            mp_pay_handle(self, data, order_id) {
                // 是否打开另一个小程序
                if (typeof data.data != 'string' && (data.data.appid || null) != null && (data.data.path || null) != null && (data.data.order_no || null) != null) {
                    uni.navigateToMiniProgram({
                        appId: data.data.appid,
                        path: data.data.path,
                        extraData: data.data.extra_data || {},
                        success(res) {
                            // 支付状态验证
                            self.pay_status_check_handle(self, data, order_id);

                            // 提示弹窗
                            self.setData({
                                payment_confirm_modal_status: true,
                            });
                        },
                        fail(res) {
                            app.globalData.showToast(self.$t('paytips.paytips.6y488i'));
                        }
                    });
                } else {
                    uni.requestPayment({
                        // #ifdef MP-ALIPAY || MP-BAIDU || MP-TOUTIAO
                        orderInfo: data.data,
                        // #endif
                        // #ifdef MP-QQ
                        package: data.data,
                        // #endif
                        // #ifdef MP-WEIXIN
                        timeStamp: data.data.timeStamp,
                        nonceStr: data.data.nonceStr,
                        package: data.data.package,
                        signType: data.data.signType,
                        paySign: data.data.paySign,
                        // #endif
                        // #ifdef MP-TOUTIAO
                        service: 5,
                        // #endif
                        success: (res) => {
                            // #ifdef MP-ALIPAY
                            if (res.resultCode != 9000) {
                                self.order_item_pay_fail_handle(data, order_id, res.memo || self.$t('paytips.paytips.6y488i'));
                                return false;
                            }
                            // #endif
                            // #ifdef MP-TOUTIAO
                            if (res.code != 0) {
                                self.order_item_pay_fail_handle(data, order_id, self.$t('paytips.paytips.6y488i'));
                                return false;
                            }
                            // #endif
                            // 数据设置
                            self.order_item_pay_success_handle(data, order_id);
                        },
                        fail: (res) => {
                            let msg = res.memo || res.errMsg || null;
                            if(msg != null) {
                                let code = res.resultCode || res.errCode || res.errNo || null;
                                if(code != null) {
                                    msg += '('+code+')';
                                }
                                if(msg.indexOf('cancel') != -1) {
                                    msg = null;
                                }
                            }
                            self.order_item_pay_fail_handle(data, order_id, msg || self.$t('paytips.paytips.6y488i'));
                        },
                    });
                }
            },
            // QQ支付处理
            qq_pay_handle(self, data, order_id) {
                // 是否微信支付
                if (data.payment.payment == 'Weixin') {
                    uni.requestWxPayment({
                        url: data.data,
                        referer: app.globalData.data.request_url,
                        success: function (res) {
                            app.globalData.alert({
                                msg: self.$t('payment.payment.k2i010'),
                                is_show_cancel: 0,
                            });
                            // 支付接口调用成功，但是不知道是否支付成功，所以需要重新获取列表数据
                            self.$emit('reset-event');
                        },
                        fail: function (res) {
                            self.order_item_pay_fail_handle(data, order_id, self.$t('paytips.paytips.6y488i'));
                        },
                    });
                } else {
                    self.mp_pay_handle(self, data, order_id);
                }
            },
            // h5支付处理
            h5_pay_handle(self, data, order_id) {
                // 字符串则为跳转地址直接进入
                if (typeof data.data == 'string') {
                    window.location.href = data.data;
                } else {
                    var status = false;
                    // 微信jsapi
                    if (data.payment.payment == 'Weixin' && (data.data.appId || null) != null && (data.data.timeStamp || null) != null && (data.data.nonceStr || null) != null && (data.data.package || null) != null && (data.data.signType || null) != null && (data.data.paySign || null) != null) {
                        status = true;

                        function onBridgeReady() {
                            WeixinJSBridge.invoke(
                                'getBrandWCPayRequest',
                                {
                                    appId: data.data.appId,
                                    timeStamp: data.data.timeStamp,
                                    nonceStr: data.data.nonceStr,
                                    package: data.data.package,
                                    signType: data.data.signType,
                                    paySign: data.data.paySign,
                                },
                                function (res) {
                                    if (res.err_msg == 'get_brand_wcpay_request:ok') {
                                        // 数据设置
                                        self.order_item_pay_success_handle(data, order_id);
                                    } else {
                                        self.order_item_pay_fail_handle(data, order_id, res.err_msg);
                                    }
                                }
                            );
                        }
                        if (typeof WeixinJSBridge == 'undefined') {
                            if (document.addEventListener) {
                                document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                            } else if (document.attachEvent) {
                                document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                                document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                            }
                        } else {
                            onBridgeReady();
                        }
                    }
                    // 二维码展示
                    if ((data.data.qrcode_url || null) != null && (data.data.name || null) != null && (data.data.order_no || null) != null) {
                        status = true;
                        // 显示支付窗口
                        self.setData({
                            popup_view_pay_data: data.data,
                            popup_view_pay_qrcode_is_show: true,
                        });
                        // 状态验证
                        self.pay_status_check_handle(self, data, order_id);
                    }
                    // 返回html表单
                    if ((data.data.html || null) != null) {
                        status = true;
                        var div = document.createElement('paydivform');
                        div.innerHTML = data.data.html;
                        document.body.appendChild(div);
                        var fm = document.forms;
                        var fm_len = fm.length;
                        if (fm_len > 0) {
                            fm[fm_len - 1].submit();
                        }
                    }
                    // 未匹配到的支付处理方式
                    if (!status) {
                        app.globalData.showToast(data.payment.name + self.$t('payment.payment.2rw3qh'));
                    }
                }
            },

            // 支付状态验证处理
            pay_status_check_handle(self, data, order_id) {
                // 先清除已存在的定时
                clearInterval(self.pay_statuc_check_timer);
                // 定时校验支付状态
                var timer = setInterval(function () {
                    uni.request({
                        url: self.propQrcodeUrl,
                        method: 'POST',
                        data: {
                            order_no: data.data.order_no,
                        },
                        dataType: 'json',
                        success: (res) => {
                            if (res.data.code == 0) {
                                // 清除定时、支付数据、弹窗
                                clearInterval(self.pay_statuc_check_timer);
                                self.setData({
                                    popup_view_pay_data: null,
                                    popup_view_pay_qrcode_is_show: false,
                                    payment_confirm_modal_status: false,
                                });
                                // 数据设置
                                self.order_item_pay_success_handle(data, order_id);
                            } else {
                                // -333支付中、其它状态则提示错误
                                if (res.data.code != -333) {
                                    clearInterval(self.pay_statuc_check_timer);
                                    app.globalData.showToast(res.data.msg);
                                }
                            }
                        },
                        fail: () => {
                            clearInterval(self.pay_statuc_check_timer);
                            self.order_item_pay_fail_handle(data, order_id, self.$t('common.internet_error_tips'));
                        },
                    });
                }, 3000);
                self.setData({
                    pay_statuc_check_timer: timer,
                });
            },

            // 支付确认弹窗事件
            payment_confirm_event(e) {
                // 关闭弹窗清除定时任务
                this.setData({
                    payment_confirm_modal_status: false,
                });
                clearInterval(this.pay_statuc_check_timer);
                // 回调处理
                if(parseInt(e.currentTarget.dataset.type || 0) == 1) {
                    this.order_item_pay_success_handle(this.pay_response_data, this.order_id);
                } else {
                    this.order_item_pay_fail_handle(this.pay_response_data, this.order_id, this.$t('paytips.paytips.6y488i'));
                }
            },

            // 支付成功数据设置 data:后台返回的参数， order_id: 订单id，is_to_page，是否需要跳转页面的参数控制
            order_item_pay_success_handle(data, order_id, is_to_page = true) {
                let back_data = {
                    data: data,
                    order_id: order_id,
                    temp_pay_index: this.propTempPayIndex,
                    payment_id: this.payment_id,
                    is_to_page: is_to_page,
                };
                this.$emit('pay-success', back_data);
                if (is_to_page) {
                    this.to_success_page_event();
                }
            },
            // 支付失败数据设置 data:后台返回的参数， order_id: 订单id, msg: 错误提示信息
            order_item_pay_fail_handle(data, order_id, msg) {
                let back_data = {
                    data: data,
                    order_id: order_id,
                    temp_pay_index: this.propTempPayIndex,
                    payment_id: this.payment_id,
                };
                this.$emit('pay-fail', back_data);
                this.to_fail_page_event(msg);
            },
            // 成功跳转
            to_success_page_event() {
                if (this.propToPage) {
                    // 跳转支付页面
                    app.globalData.url_open(this.propToPage, true);
                } else {
                    let url_data = {
                        code: '9000',
                    };
                    url_data = Object.assign({}, url_data, this.propToPageBack);
                    // 跳转支付页面
                    app.globalData.url_open('/pages/paytips/paytips?params=' + encodeURIComponent(base64.encode(JSON.stringify(url_data))), this.propIsRedirectTo);
                }
            },
            // 失败跳转
            to_fail_page_event(msg) {
                let to_fail_page = this.propToFailPage || null;
                if (to_fail_page != null) {
                    let join = (to_fail_page.indexOf('?') == -1) ? '?' : '&';
                    to_fail_page += join+'msg='+msg;
                    if (this.propIsFailAlert) {
                        // 现金支付
                        uni.showModal({
                            content: msg,
                            showCancel: false,
                            confirmText: this.$t('common.confirm'),
                            success(res) {
                                if (res.confirm) {
                                    // 跳转支付页面
                                    app.globalData.url_open(to_fail_page, true);
                                }
                            },
                        });
                    } else {
                        // 跳转支付页面
                        app.globalData.url_open(to_fail_page, true);
                    }
                } else {
                    if (msg) {
                        app.globalData.showToast(msg);
                    }
                }
            },
            to_other(order_id) {
                if (this.propToAppointPage) {
                    // 跳转订单列表页
                    app.globalData.url_open(this.propToAppointPage, true);
                }
            },
            // 页面卸载
            onUnload(e) {
                clearInterval(this.pay_statuc_check_timer);
            },
            // 支付html展示窗口事件
            popup_view_pay_html_event_close(e) {
                this.setData({
                    popup_view_pay_html_is_show: false,
                });
                this.to_other();

                let back_data = {
                    temp_pay_index: this.propTempPayIndex,
                    payment_id: this.payment_id,
                };
                this.$emit('pay-html-close', back_data);
            },

            // 线下支付
            chooseOfflineVoucher() {
                var self = this;
                var remain = 9 - (this.offlineVoucherUrls || []).length;
                if (remain <= 0) return;
                uni.chooseImage({
                    count: remain,
                    sizeType: ['compressed'],
                    sourceType: ['album', 'camera'],
                    success: function(res) {
                        var tempFiles = res.tempFilePaths;
                        self.uploadVoucherOneByOne(tempFiles, 0, []);
                    }
                });
            },

            uploadVoucherOneByOne(files, idx, uploadedUrls) {
                var self = this;
                if (idx >= files.length) {
                    var newUrls = (self.offlineVoucherUrls || []).concat(uploadedUrls);
                    self.setData({ offlineVoucherUrls: newUrls, submit_disabled_status: newUrls.length === 0 });
                    return;
                }
                uni.showLoading({ title: '上传中...' });
                uni.uploadFile({
                    url: config.url + '/mall-system/api/v1/files/upload',
                    filePath: files[idx],
                    name: 'file',
                    header: { 'Authorization': uni.getStorageSync('token') },
                    success: function(uploadRes) {
                        uni.hideLoading();
                        try {
                            var data = JSON.parse(uploadRes.data);
                            if (data.code === '00000' && data.data && data.data.url) {
                                uploadedUrls.push(data.data.url);
                            }
                        } catch(e) {}
                        self.uploadVoucherOneByOne(files, idx + 1, uploadedUrls);
                    },
                    fail: function() {
                        uni.hideLoading();
                        self.uploadVoucherOneByOne(files, idx + 1, uploadedUrls);
                    }
                });
            },

            removeOfflineVoucher(idx) {
                var urls = (this.offlineVoucherUrls || []).slice();
                urls.splice(idx, 1);
                this.setData({ offlineVoucherUrls: urls, submit_disabled_status: urls.length === 0 });
            },

            previewOfflineVoucher(idx) {
                uni.previewImage({
                    urls: this.offlineVoucherUrls,
                    current: this.offlineVoucherUrls[idx]
                });
            },

            copyOfflineAccount() {
                if (!this.offlineBankInfo.accountNo) return;
                uni.setClipboardData({
                    data: this.offlineBankInfo.accountNo,
                    success: function() {
                        uni.showToast({ title: this.$t('common.copy_success'), icon: 'success', duration: 1500 });
                    }
                });
            },
        },
    };
</script>
<style scoped>
    /**
    * 支付方式
    */
    .payment-price {
        font-size: 80rpx;
    }
    .payment-list .scroll-y {
        max-height: 430rpx;
    }
    .payment-list .item {
        padding: 24rpx 28rpx 24rpx 32rpx;
    }
    .payment-list .icon {
        width: 50rpx;
        height: 50rpx !important;
    }
    .payment-submit {
        padding: 40rpx;
    }

    /**
     * 支付确认弹窗
     */
    .payment-confirm-modal {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        background-color: rgba(0, 0, 0, 0.6);
        z-index: 100;
        width: 100%;
        height: 100%;
    }
    .payment-confirm-modal .content {
        margin-top: 60%;
        z-index: 101;
    }
    /* 银行卡子列表 */

    .offline-pay-sub { background: #F8F8F8; }
    .offline-info-area { padding: 0 10rpx; }
    .bank-card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 16rpx; border: 1rpx solid #eee; }
    .bank-card-hd { margin-bottom: 16rpx; }
    .bank-card-bd { border-top: 1rpx dashed #eee; padding-top: 16rpx; }
    .account-no { font-size: 32rpx; font-weight: 700; letter-spacing: 4rpx; color: #2D2D2D; }
    .copy-btn { background: #FFF0ED; padding: 8rpx 20rpx; border-radius: 20rpx; }
    .tips-text { display: block; margin-bottom: 20rpx; line-height: 1.5; }
    .voucher-section { margin-top: 8rpx; }
    .upload-row { display: flex; flex-wrap: wrap; gap: 16rpx; }
    .upload-thumb { width: 140rpx; height: 140rpx; border-radius: 8rpx; position: relative; overflow: hidden; border: 1rpx solid #e8e8e8; }
    .upload-thumb .thumb-img { width: 100%; height: 100%; }
    .upload-thumb .thumb-del { position: absolute; top: 0; right: 0; width: 36rpx; height: 36rpx; background: rgba(0,0,0,0.45); color: #fff; font-size: 24rpx; display: flex; align-items: center; justify-content: center; border-radius: 0 0 0 8rpx; }
    .upload-add-btn { display: flex; flex-direction: column; align-items: center; justify-content: center; background: #f5f5f5; border-style: dashed; border-color: #ddd; }
    .upload-add-btn .add-icon { font-size: 48rpx; color: #bbb; line-height: 1; }
</style>