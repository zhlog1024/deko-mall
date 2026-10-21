<!--
 * @Description  : 
 * @Version      : 1.0
 * @Author       : 
 * @Date         : 2025-06-23 19:06:07
 * @LastEditors  : zhlog1024
 * @LastEditTime : 2025-07-02 16:09:40
-->
<template>
  <view :class="theme_view">
    <view class="content">
    <!-- 上部分：资产总额 -->
    <view class="asset-header" :class="getHeaderClass">
      <view class="header-title">{{ title }}</view>
      <view class="header-stats">
        <view class="stat-item">
          <view class="stat-value">{{ assetInfo.availableNum }}</view>
          <view class="stat-label">{{ getUnitText }}</view>
        </view>
      </view>
      <view class="header-action" v-if="assetCode=='POINT'">
        <view class="action-btn" @click="handlePoint">{{ $t('balance.balance.points_transfer') }}</view>
      </view>
    </view>

    <!-- 下部分：流水明细 -->
    <view class="flow-content">
      <view class="section-title">{{ $t('balance.balance.flow_detail') }}</view>

      <view v-for="(item, index) in list" :key="index" class="flow-item">
        <!-- 顶部：备注 -->
        <view class="flow-remark">{{ item.remark }}</view>

        <!-- 中间：数量变化 -->
        <view class="flow-main">
          <view class="flow-num" :class="item.num > 0 ? 'plus' : 'minus'">
            {{ item.num > 0 ? '+' : '' }}{{ item.num }}
          </view>
        </view>

        <!-- 订单编号 -->
        <view class="flow-order" v-if="item.reqNo">
          <text class="order-text">{{ $t('balance.balance.order_no_prefix') }}{{ item.reqNo }}</text>
        </view>

        <!-- 底部：时间 -->
        <view class="flow-time">{{ item.createDatatime }}</view>
      </view>

      <component-bottom-line :propStatus="loadStatus === 'noMore'"></component-bottom-line>
      <noData v-if="!loading && list.length === 0" propStatus="0" :propMsg="$t('balance.balance.no_data')"></noData>
    </view>
    <view class="safe-area-bottom"></view>

    <!-- 积分转账弹窗 -->
    <u-popup
      ref="pointPopup"
      propType="center"
      :propRound="24"
      :propCloseable="false"
      @change="onPopupChange"
    >
      <view class="transfer-modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ $t('balance.balance.transfer_title') }}</text>
          <view class="close-btn" @click="closeTransferModal">
            <iconfont name="icon-close" size="32rpx" color="#999"></iconfont>
          </view>
        </view>
        
        <view class="transfer-tip">{{ $t('balance.balance.transfer_hint') }}</view>
        
        <view class="form-group">
          <view class="form-item">
            <text class="form-label">{{ $t('balance.balance.phone') }}</text>
            <input
              v-model="pointsTransferForm.receiverMobile"
              :placeholder="$t('balance.balance.phone_placeholder')"
              placeholder-class="form-placeholder"
              class="form-input"
              maxlength="11"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">{{ $t('balance.balance.amount') }}</text>
            <input
              v-model="pointsTransferForm.num"
              type="digit"
              :placeholder="$t('balance.balance.amount_placeholder')"
              placeholder-class="form-placeholder"
              class="form-input"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">{{ $t('balance.balance.remark') }}</text>
            <input
              v-model="pointsTransferForm.remark"
              :placeholder="$t('balance.balance.remark_placeholder')"
              placeholder-class="form-placeholder"
              class="form-input"
              maxlength="50"
            />
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="btn-cancel" @click="closeTransferModal">{{ $t('balance.balance.cancel') }}</button>
          <button class="btn-confirm" @click="handleConfirm">{{ $t('balance.balance.confirm') }}</button>
        </view>
      </view>
    </u-popup>
  </view>
  </view>
</template>

<script>
import {getAssetFlow, getAsset, transferPoints} from '@/api/pageUser'
import dayjs from 'dayjs'
import componentBottomLine from '@/components/bottom-line/bottom-line.vue'
import noData from '@/components/no-data/no-data.vue'
import uPopup from '@/components/u-popup/u-popup.vue'

export default {
  data() {
    return {
      theme_view: getApp().globalData.get_theme_value_view(),
      queryInfo: {
        pageNum: 1,
        pageSize: 10,
        assetCode: ''
      },
      list: [],
      loading: false,
      loadStatus: 'more',
      assetInfo: {},
      title: '',
      assetCode: '',
      showPoint: false,
      pointsTransferForm: {
        receiverMobile: '',
        num: '',
        remark: '',
      },
      rules: {
        receiverMobile: [
          { required: true, message: this.$t('balance.balance.enter_phone'), trigger: 'change' },
          { pattern: /^1[3-9]\d{9}$/, message: this.$t('balance.balance.enter_valid_phone'), trigger: 'change' }
        ],
        num: [
          { required: true, message: this.$t('balance.balance.enter_quantity'), trigger: 'change' },
          { pattern: /^[1-9]\d*$/, message: this.$t('balance.balance.enter_valid_quantity') }
        ]
      }
    }
  },
  components: {
    noData,
    componentBottomLine,
    uPopup
  },
  computed: {
    getUnitText() {
      const units = {
        'POINT': this.$t('balance.balance.points')
      }
      return units[this.assetCode] || this.$t('balance.balance.quantity')
    },
    getHeaderClass() {
      const classes = {
        'POINT': 'header-points'
      }
      return classes[this.assetCode] || 'header-default'
    }
  },
  watch: {
    showPoint(newVal) {
      if (newVal) {
        this.$refs.pointPopup.open()
      } else {
        this.$refs.pointPopup.close()
      }
    }
  },
  onShow() {},

  methods: {
    onPopupChange(e) {
      // 弹窗状态变化时同步 showPoint
      this.showPoint = e.show
    },
    closeTransferModal() {
      this.showPoint = false
    },
    handlePoint() {
      this.pointsTransferForm = {
        receiverMobile: '',
        num: '',
        remark: '',
      }
      this.showPoint = true
    },
    handleConfirm() {
      // 简单校验
      if (!this.pointsTransferForm.receiverMobile) {
        uni.showToast({ title: this.$t('balance.balance.enter_phone'), icon: 'none' })
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.pointsTransferForm.receiverMobile)) {
        uni.showToast({ title: this.$t('balance.balance.enter_valid_phone'), icon: 'none' })
        return
      }
      if (!this.pointsTransferForm.num) {
        uni.showToast({ title: this.$t('balance.balance.enter_quantity'), icon: 'none' })
        return
      }
      if (!/^[1-9]\d*$/.test(this.pointsTransferForm.num)) {
        uni.showToast({ title: this.$t('balance.balance.enter_valid_quantity'), icon: 'none' })
        return
      }

      let params = {
        ...this.pointsTransferForm,
        num: this.pointsTransferForm.num
      }
      transferPoints(params).then(res => {
        if (res.code === '00000') {
          uni.showToast({ title: this.$t('balance.balance.transfer_success') })
          this.showPoint = false
          this.queryInfo.pageNum = 1
          this.list = []
          this.getCustomersList('refresh')
          this.getAssetInfo()
        }
      })
    },
    loadMore() {
      if (this.loadStatus === 'more') {
        this.getCustomersList('loadMore')
      }
    },
    getCustomersList(type) {
      if (this.loadStatus === 'loading') {
        return
      }

      if (type !== 'loadMore') {
        this.queryInfo.pageNum = 1
        this.list = []
      } else {
        this.queryInfo.pageNum++
      }

      this.loadStatus = 'loading'
      const params = {
        ...this.queryInfo
      }

      getAssetFlow(params)
        .then(res => {
          const list = res.data.list.map(item => {
            return {
              ...item,
              createDatatime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss')
            }
          })

          if (type !== 'loadMore') {
            this.list = list
          } else {
            this.list = this.list.concat(list)
          }

          this.loadStatus = res.data.total <= this.list.length ? 'noMore' : 'more'
        })
        .finally(() => {
          this.loading = false
        })
    },
    getAssetInfo() {
      getAsset({ assetCode: this.assetCode }).then(res => {
        const keys = {
          'POINT': this.$t('balance.balance.my_points')
        }
        this.title = keys[this.assetCode] || this.$t('balance.balance.my_asset')
        uni.setNavigationBarTitle({
          title: this.title
        })
        this.assetInfo = res.data
      })
    }
  },
  onLoad(options) {
    this.queryInfo.id = options.id
    this.queryInfo.assetCode = options.type
    this.assetCode = options.type
    this.getCustomersList('refresh')
    this.getAssetInfo()
  },
  onReachBottom() {
    if (this.loadStatus === 'noMore') {
      this.loading = false
      return
    }
    this.getCustomersList('loadMore')
  }
}
</script>

<style lang="scss" scoped>
/* 暖色极简主义 - 资产专区页，使用主题变量 */
.content {
  min-height: 100vh;
  background-color: #FAF8F5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 渐变头部通用样式 - 保留原有配色
.asset-header {
  padding: 40rpx 32rpx 48rpx;
  text-align: center;
  color: #fff;

  .header-title {
    font-size: 26rpx;
    opacity: 0.9;
    margin-bottom: 24rpx;
    font-weight: 500;
  }

  .header-stats {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.18);
    border-radius: 20rpx;
    padding: 32rpx 48rpx;
    margin: 0 16rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 56rpx;
        font-weight: 700;
        line-height: 1.2;
        letter-spacing: 2rpx;
      }

      .stat-label {
        font-size: 24rpx;
        opacity: 0.9;
        margin-top: 8rpx;
      }
    }
  }

  .header-action, .header-actions {
    margin-top: 28rpx;

    .action-btn {
      display: inline-block;
      background-color: rgba(255, 255, 255, 0.2);
      color: #fff;
      font-size: 26rpx;
      padding: 14rpx 40rpx;
      border-radius: 36rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.3);
      backdrop-filter: blur(10rpx);

      &.small {
        font-size: 22rpx;
        padding: 10rpx 28rpx;
        margin: 0 8rpx;
      }
    }
  }
}

// 不同资产类型的渐变色 - 保持不变
.header-points {
  background: linear-gradient(135deg, #ff7159 0%, #ffab91 100%);
}

.header-gold {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
}

.header-silver {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.header-default {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

// 下部分：流水明细
.flow-content {
  padding: 0 24rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #2D2D2D;
    padding: 28rpx 8rpx 20rpx;
  }
}

.flow-item {
  margin-bottom: 16rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(93, 64, 55, 0.06);
  overflow: hidden;
  padding: 28rpx;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:active {
    transform: scale(0.98);
  }

  .flow-remark {
    font-size: 28rpx;
    font-weight: 600;
    color: #2D2D2D;
    margin-bottom: 16rpx;
    line-height: 1.4;
  }

  .flow-main {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12rpx;

    .flow-num {
      font-size: 40rpx;
      font-weight: 700;

      &.plus {
        color: #52c41a;
      }

      &.minus {
        color: #FF6B6B;
      }
    }

    .flow-info {
      .info-text {
        font-size: 26rpx;
        color: #6B6B6B;
      }
    }
  }

  .flow-order {
    margin-top: 10rpx;
    .order-text {
      font-size: 24rpx;
      color: #ADADAD;
    }
  }

  .flow-time {
    font-size: 24rpx;
    color: #ADADAD;
    margin-top: 8rpx;
  }
}

.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
}

// 积分转账弹窗样式
.transfer-modal-content {
  width: 600rpx;
  max-width: 90vw;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  position: relative;
  padding: 32rpx 32rpx 24rpx;
  text-align: center;
  border-bottom: 1rpx solid #F0F0F0;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2D2D2D;
  }
  
  .close-btn {
    position: absolute;
    right: 24rpx;
    top: 50%;
    transform: translateY(-50%);
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    
    &:active {
      opacity: 0.6;
    }
  }
}

.transfer-tip {
  font-size: 24rpx;
  color: #999999;
  text-align: center;
  padding: 20rpx 32rpx;
}

.form-group {
  padding: 0 32rpx;
  
  .form-item {
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .form-label {
      display: block;
      font-size: 28rpx;
      color: #2D2D2D;
      font-weight: 500;
      margin-bottom: 12rpx;
    }
    
    .form-input {
      width: 100%;
      height: 76rpx;
      padding: 0 20rpx;
      background-color: #F5F5F5;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #2D2D2D;
      border: 2rpx solid transparent;
      transition: all 0.3s ease;
      box-sizing: border-box;
      
      &:focus {
        background-color: #FFFFFF;
        border-color: #FF7159;
      }
    }
    
    .form-placeholder {
      color: #CCCCCC;
      font-size: 26rpx;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx 32rpx;
  
  button {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 30rpx;
    border-radius: 44rpx;
    border: none;
    font-weight: 500;
    transition: all 0.3s ease;
    
    &::after {
      border: none;
    }
    
    &:active {
      transform: scale(0.98);
      opacity: 0.8;
    }
  }
  
  .btn-cancel {
    background-color: #F5F5F5;
    color: #666666;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #FF7159 0%, #FFAB91 100%);
    color: #FFFFFF;
  }
}
</style>
