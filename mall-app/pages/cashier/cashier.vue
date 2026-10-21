<template>
  <view :class="theme_view">
    <view class="page bg-white">
      <view class="content padding-horizontal-main tc">
        <block v-if="data_list_loding_status == 3">
          <!-- 支付金额 -->
          <view class="padding-vertical-xxxxl">
            <text class="cr-price fw-b text-size-lg">{{ payment_currency_symbol }}</text>
            <text class="cr-price fw-b text-size-xxl">{{ pay_price }}</text>
          </view>
          <view v-if="discount_amount > 0" class="margin-top-sm" @click="show_discount_detail">
            <text class="cr-grey text-size-sm">{{ $t('cashier.cashier.discount_saved') }} {{ payment_currency_symbol }}{{ discount_amount }} ›</text>
          </view>
          <view v-if="order_no" class="margin-top-sm">
            <text class="cr-grey text-size-sm">{{ $t('cashier.cashier.order_no_label') }}{{ order_no }}</text>
          </view>

          <!-- 支付渠道选择 -->
          <view v-if="channels.length > 0" class="channel-section margin-top-xxxxl padding-horizontal-main">
            <view class="channel-list bg-white border-radius-main">
              <block v-for="(item, index) in channels" :key="item.id">
                <view
                  class="channel-item flex-row jc-sb align-c padding-vertical-main padding-horizontal-main"
                  :class="index > 0 ? 'br-t-f5' : ''"
                  @click="select_channel(item.id)"
                >
                  <view class="flex-row align-c flex-1">
                    <image v-if="item.logo" class="channel-icon margin-right-sm" :src="item.logo" mode="widthFix"></image>
                    <iconfont v-else name="icon-wallet" size="40rpx" color="#999" />
                    <view class="flex-col">
                      <text class="channel-name">{{ item.name }}</text>
                      <text v-if="item.tips" class="cr-grey text-size-xs">{{ item.tips }}</text>
                    </view>
                  </view>
                  <iconfont
                    :name="selected_channel == item.id ? 'icon-selected-solid' : 'icon-not-selected'"
                    size="44rpx"
                    :color="selected_channel == item.id ? '#E22C08' : '#ccc'"
                  ></iconfont>
                </view>
                <!-- 线下支付：银行信息 + 凭证上传 -->
                <view v-if="item.id === 'OFFLINE_PAY' && selected_channel == item.id" class="offline-pay-sub br-t-f5 padding-main">
                  <!-- 银行账户信息 -->
                  <view v-if="bankInfo.bankName" class="bank-info bg-white border-radius-main padding-main margin-bottom-sm">
                    <view class="flex-row align-c margin-bottom-sm">
                      <text class="bank-info-icon margin-right-sm">🏦</text>
                      <text class="text-size-sm fw-b">{{ bankInfo.bankName }}</text>
                    </view>
                    <view class="bank-info-row flex-row jc-sb align-c padding-vertical-xs">
                      <text class="cr-grey text-size-xs">{{ $t('cashier.cashier.account_name') || '户名' }}</text>
                      <text class="text-size-sm">{{ bankInfo.accountName }}</text>
                    </view>
                    <view class="bank-info-row flex-row jc-sb align-c padding-vertical-xs">
                      <text class="cr-grey text-size-xs">{{ $t('cashier.cashier.account_no') || '卡号' }}</text>
                      <text class="text-size-sm account-no-text">{{ bankInfo.accountNo }}</text>
                    </view>
                    <view class="bank-info-actions flex-row jc-sb align-c margin-top-sm">
                      <text class="cr-main text-size-xs" @click="copyAccountNo">{{ $t('cashier.cashier.copy_account') || '一键复制卡号' }}</text>
                      <image v-if="bankInfo.qrcode" class="bank-qrcode" :src="bankInfo.qrcode" mode="widthFix" @click="previewQrcode"></image>
                    </view>
                  </view>
                  <text v-if="bankInfo.tips" class="cr-grey text-size-xs" style="line-height:1.6;">⚠️ {{ bankInfo.tips }}</text>

                  <!-- 凭证上传 -->
                  <view class="voucher-upload margin-top-sm">
                    <view class="flex-row align-c margin-bottom-sm">
                      <text class="text-size-sm fw-b">{{ $t('cashier.cashier.upload_voucher') || '📎 上传转账凭证' }}</text>
                    </view>
                    <component-upload
                      ref="offlineUpload"
                      :propMaxNum="9"
                      :propPathType="'common'"
                      @call-back="onVoucherUploaded"
                    ></component-upload>
                  </view>
                </view>
              </block>
            </view>
          </view>
          <view v-else class="margin-top-xxxxl padding-vertical-xxxxl tc">
            <text class="cr-grey text-size-sm">{{ $t('cashier.cashier.no_channel') }}</text>
          </view>

          <!-- 支付状态提示 -->
          <view class="margin-top-sm padding-bottom-xxxxl">
            <view :class="'cr-' + (pay_status == 1 ? 'green' : (pay_status == 2 ? 'red' : 'grey'))">{{ pay_msg }}</view>
          </view>

          <!-- 支付按钮 -->
          <view v-if="pay_status == 2 || pay_status == 0" class="margin-top-xxxxl padding-vertical-xxxxl">
            <!-- 线下支付：提交凭证按钮 -->
            <button v-if="selected_channel === 'OFFLINE_PAY'" class="bg-main br-main cr-white round text-size padding-horizontal-xxxxl" hover-class="none"
              @click="submitOfflinePay()"
              :disabled="!voucherUrls.length || submitting || pay_status == 1">
              {{ submitting ? ($t('cashier.cashier.submitting') || '提交中...') : ($t('cashier.cashier.submit_voucher') || '提交凭证，等待审核') }}
            </button>
            <!-- 其他支付渠道 -->
            <button v-else class="bg-main br-main cr-white round text-size padding-horizontal-xxxxl" hover-class="none"
              @click="pay_handle()"
              :disabled="!selected_channel || paying">
              {{ paying ? $t('cashier.cashier.paying') : (pay_btn_text || $t('cashier.cashier.confirm_pay')) }}
            </button>
          </view>
        </block>
        <block v-else>
          <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
        </block>

        <view v-if="is_back_btn && pay_status != 0" class="margin-top-xxxxl padding-top-xxxxl tc">
          <button class="bg-white br-main cr-main round text-size-sm padding-horizontal-xxxxl" size="mini" hover-class="none" open-type="launchApp" app-parameter="wechat">{{ pay_back_app_text }}</button>
        </view>
      </view>
    </view>

    <!-- 支付确认弹窗 -->
    <view v-if="payment_confirm_modal_status" class="payment-confirm-modal">
      <view class="content padding-xl margin-xxl tc bg-white border-radius-main">
        <view class="padding-vertical-xxxxl">{{ $t('cashier.cashier.pay_confirm_modal') }}</view>
        <view class="margin-top-lg">
          <button type="default" size="mini" class="bg-white br-black cr-black text-size-sm round margin-right-xxxxl" data-type="0" @click="payment_confirm_event">{{ $t('cashier.cashier.not_paid') }}</button>
          <button type="default" size="mini" class="bg-main br-main cr-white text-size-sm round margin-left-xxxxl" data-type="1" @click="payment_confirm_event">{{ $t('cashier.cashier.paid') }}</button>
        </view>
      </view>
    </view>

    <!-- 支付组件（隐藏，用于调起多平台支付） -->
    <component-payment
      ref="payment"
      :propPaymentList="channels"
      :propPaymentId="selected_channel"
      :propPayPrice="pay_price"
      :propCurrencySymbol="payment_currency_symbol"
      :propPaymentNo="paymentNo"
      :propDeviceType="deviceType"
      :propCashierPayUrl="cashier_pay_url"
      :propCode="wxCode"
      :propToFailPage="to_fail_page"
      :propToPageBack="to_page_back"
      :propIsRedirectTo="true"
      propPayDataKey="paymentNo"
      @pay-success="on_pay_success"
      @pay-fail="on_pay_fail"
    ></component-payment>

    <!-- 公共 -->
    <component-common ref="common" :propIsAppAdmin="false"></component-common>
  </view>
</template>

<script>
const app = getApp();
import config from '@/config';
import componentCommon from '@/components/common/common';
import componentNoData from '@/components/no-data/no-data';
import componentPayment from '@/components/payment/payment';
import componentUpload from '@/components/upload/upload';
import iconfont from '@/components/iconfont/iconfont';
import { request } from '@/config/request';
import { getCashierInfo } from '@/api/pay.js';
export default {
  data() {
    return {
      theme_view: app.globalData.get_theme_value_view(),
      payment_currency_symbol: app.globalData.currency_symbol(),
      data_list_loding_status: 1,
      data_list_loding_msg: '',
      scene: 0,
      scene_back_arr: [1069, 1038, 1089, 1090, 1001, 1131, 1187],
      is_back_btn: false,
      pay_price: '0.00',
      discount_amount: 0,
      order_no: '',
      channels: [],
      selected_channel: '',
      paymentNo: '',
      deviceType: 'H5',
      cashier_pay_url: config.url + '/mall-pay/app-api/v1/cashier/pay',
      wxCode: '',
      paying: false,
      submitting: false,
      pay_status: 0,
      pay_msg: '',
      payment_confirm_modal_status: false,
      is_first: 1,
      pay_btn_text: '',
      pay_back_app_text: '',
      to_fail_page: '/pages/user-order/user-order',
      to_page_back: { title: '', page: '/pages/user-order/user-order' },
      // 支付完成（成功/失败/查看订单）后的回跳页，默认 C 端零售订单列表；
      // 门店进货等 B2B 链路通过 onLoad 的 backUrl 覆盖，避免付完钱落到零售订单列表
      default_back_url: '/pages/user-order/user-order',
      // 线下支付相关
      bankInfo: {},
      voucherUrls: []
    };
  },

  components: {
    componentCommon,
    componentNoData,
    componentPayment,
    componentUpload,
    iconfont
  },


  onLoad(options) {
    app.globalData.page_event_onload_handle(options);

    // #ifdef MP-WEIXIN
    this.deviceType = 'MINI';
    // #endif
    // #ifdef APP-PLUS
    this.deviceType = 'APP';
    // #endif

    var paymentNo = options.paymentNo;
    if (!paymentNo) {
      this.setData({
        data_list_loding_status: 0,
        data_list_loding_msg: this.$t('cashier.cashier.missing_payment_no')
      });
      return;
    }
    this.paymentNo = paymentNo;
    this.pay_back_app_text = this.$t('cashier.cashier.back_to_app');
    this.to_page_back.title = this.$t('cashier.cashier.view_order');
    // 回跳页：仅接受站内绝对路径，非法值一律回退默认页（防开放重定向）
    var back_url = this.resolve_back_url(options.backUrl);
    if (back_url) {
      this.to_fail_page = back_url;
      this.to_page_back.page = back_url;
    }
    this.pay_btn_text = this.$t('cashier.cashier.confirm_pay');
    this.get_data();
  },

  onShow() {
    app.globalData.page_event_onshow_handle();
    if ((this.$refs.common || null) != null) {
      this.$refs.common.on_show();
    }
  },

  onPullDownRefresh() {
    this.get_data();
  },

  methods: {
    // 解析调用方传入的回跳地址（onLoad 的 options.backUrl）：
    // 只放行站内绝对路径（以 / 开头、非 // 协议相对、无 scheme），其余返回空串走默认页（防开放重定向）
    resolve_back_url(value) {
      if (!value) {
        return '';
      }
      var url = value;
      try {
        url = decodeURIComponent(value);
      } catch {
        // 非法编码：沿用原值由下方白名单校验兜底
      }
      if (url.indexOf('/') !== 0 || url.indexOf('//') === 0 || url.indexOf('://') !== -1) {
        return '';
      }
      return url;
    },

    // 获取收银台数据
    get_data() {
      var self = this;
      getCashierInfo(this.paymentNo, this.deviceType).then(function (res) {
        uni.stopPullDownRefresh();
        if (res && res.data) {
          self.pay_price = res.data.payAmount || '0.00';
          self.discount_amount = res.data.paymentDiscountAmount || 0;
          self.order_no = res.data.orderNo || '';

          // 映射渠道为 payment.vue 兼容格式
          var channels = (res.data.channels || []).map(function (ch) {
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
          self.channels = channels;

          // 提取线下支付的银行信息
          var offlineChannel = channels.find(function (c) { return c.id === 'OFFLINE_PAY'; });
          if (offlineChannel && offlineChannel.meta) {
            self.bankInfo = {
              bankName: offlineChannel.meta.bankName || '',
              accountName: offlineChannel.meta.accountName || '',
              accountNo: offlineChannel.meta.accountNo || '',
              qrcode: offlineChannel.meta.qrcode || '',
              tips: offlineChannel.meta.tips || ''
            };
          }

          // 默认选中第一个可用渠道
          var enabledList = channels.filter(function (c) { return c.is_active === 0; });
          self.selected_channel = enabledList.length > 0 ? enabledList[0].id : '';

          self.setData({
            data_list_loding_status: 3,
            data_list_loding_msg: '',
            channels: channels,
            selected_channel: self.selected_channel
          });
        } else {
          self.setData({
            is_first: 0,
            data_list_loding_status: 0,
            data_list_loding_msg: res && res.msg ? res.msg : this.$t('cashier.cashier.get_info_failed')
          });
        }
      }).catch(function () {
        uni.stopPullDownRefresh();
        self.setData({
          is_first: 0,
          data_list_loding_status: 0,
          data_list_loding_msg: self.$t('cashier.cashier.network_error_retry')
        });
      });
    },

    // 选择支付渠道
    select_channel(id) {
      if (this.selected_channel == id) return;
      this.selected_channel = id;
    },

    // 发起支付
    pay_handle() {
      if (!this.selected_channel) {
        uni.showToast({ title: this.$t('cashier.cashier.select_channel'), icon: 'none' });
        return;
      }
      this.paying = true;
      var self = this;
      // #ifdef MP-WEIXIN
      uni.login({
        success: function (res) {
          self.wxCode = res.code;
          self.$refs.payment.pay_handle(self.paymentNo, self.selected_channel, [], res.code);
        },
        fail: function () {
          self.paying = false;
          uni.showToast({ title: this.$t('cashier.cashier.wechat_auth_failed'), icon: 'none' });
        }
      });
      // #endif
      // #ifndef MP-WEIXIN
      self.$refs.payment.pay_handle(self.paymentNo, self.selected_channel);
      // #endif
    },

    // 支付成功回调
    on_pay_success() {
      this.paying = false;
      this.setData({
        pay_status: 1,
        pay_msg: this.$t('cashier.cashier.pay_success')
      });
    },

    // 支付失败回调
    on_pay_fail(e) {
      this.paying = false;
      this.setData({
        is_first: 0,
        pay_status: 2,
        pay_msg: this.$t('cashier.cashier.pay_failed_retry'),
        pay_btn_text: this.$t('cashier.cashier.retry_pay')
      });
    },

    // 支付确认弹窗
    payment_confirm_event(e) {
      this.setData({ payment_confirm_modal_status: false });
      if (parseInt(e.currentTarget.dataset.type || 0) == 1) {
        this.setData({
          pay_status: 1,
          pay_msg: this.$t('cashier.cashier.pay_success')
        });
      } else {
        this.setData({
          pay_status: 2,
          pay_msg: this.$t('cashier.cashier.pay_failed_retry')
        });
      }
    },

    // 优惠明细
    show_discount_detail() {
      uni.showModal({
        title: this.$t('cashier.cashier.discount_detail'),
        content: this.$t('cashier.cashier.discount_content') + ' ' + this.payment_currency_symbol + this.discount_amount,
        showCancel: false
      });
    },

    // 线下支付相关
    //
    // 凭证上传回调
    onVoucherUploaded(urls) {
      // upload 组件 call-back 返回上传后的 URL 或 URL 数组
      if (typeof urls === 'string') {
        this.voucherUrls = [urls];
      } else if (Array.isArray(urls)) {
        this.voucherUrls = urls;
      }
    },

    // 提交线下支付凭证
    submitOfflinePay() {
      if (!this.voucherUrls || this.voucherUrls.length === 0) {
        uni.showToast({ title: this.$t('cashier.cashier.upload_voucher_first') || '请先上传支付凭证', icon: 'none' });
        return;
      }
      this.submitting = true;
      var self = this;
      request({
        url: this.cashier_pay_url,
        method: 'POST',
        data: {
          paymentNo: this.paymentNo,
          payChannel: 'OFFLINE_PAY',
          deviceType: this.deviceType,
          voucherUrls: this.voucherUrls
        },
        headers: { auth: true }
      }).then(function(res) {
        self.submitting = false;
        if (res.code === '00000') {
          self.setData({
            pay_status: 1,
            pay_msg: self.$t('cashier.cashier.voucher_submitted') || '凭证已提交，等待审核'
          });
          // 延迟跳转到订单页面（门店进货等场景由 backUrl 覆盖为进货单列表）
          setTimeout(function() {
            uni.redirectTo({ url: self.to_page_back.page || self.default_back_url });
          }, 2000);
        } else {
          uni.showToast({ title: res.msg || (self.$t('cashier.cashier.submit_failed') || '提交失败'), icon: 'none' });
        }
      }).catch(function() {
        self.submitting = false;
        uni.showToast({ title: self.$t('cashier.cashier.network_error') || '网络异常，请重试', icon: 'none' });
      });
    },

    // 一键复制卡号
    copyAccountNo() {
      if (!this.bankInfo.accountNo) return;
      uni.setClipboardData({
        data: this.bankInfo.accountNo,
        success: function() {
          uni.showToast({ title: this.$t('common.copy_success'), icon: 'success', duration: 1500 });
        }
      });
    },

    // 预览收款码大图
    previewQrcode() {
      if (!this.bankInfo.qrcode) return;
      uni.previewImage({
        urls: [this.bankInfo.qrcode],
        current: this.bankInfo.qrcode
      });
    }
  }
};
</script>

<style scoped>
@import './cashier.css';

.cr-price {
  color: #E22C08;
}

.channel-section {
  margin-top: 40rpx;
}

.channel-list {
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.channel-item {
  min-height: 100rpx;
}

.channel-icon {
  width: 50rpx;
  height: 50rpx;
}

.channel-name {
  font-size: 30rpx;
  color: #2D2D2D;
}

/* 支付确认弹窗 */
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

/* 线下支付 */
.offline-pay-sub {
  background: #f8f8f8;
}
.offline-pay-sub .bank-info {
  border: 1rpx solid #e8e8e8;
}
.offline-pay-sub .bank-info-icon {
  font-size: 32rpx;
}
.offline-pay-sub .bank-info-row {
  border-bottom: 1rpx dashed #f0f0f0;
}
.offline-pay-sub .account-no-text {
  letter-spacing: 2rpx;
}
.offline-pay-sub .bank-qrcode {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
  border: 1rpx solid #e8e8e8;
}
.offline-pay-sub .voucher-upload {
  min-height: 120rpx;
}
</style>
