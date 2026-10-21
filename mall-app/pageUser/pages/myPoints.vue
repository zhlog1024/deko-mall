<template>
  <view :class="theme_view">
    <view class="total">
    <!-- 渐变头部 -->
    <view class="points-header">
      <view class="header-title">{{ $t('myPoints.myPoints.total_beans') }}</view>
      <view class="header-stats">
        <view class="stat-item">
          <view class="stat-value">{{ userPoints.availablePoints }}</view>
          <view class="stat-label">{{ $t('myPoints.myPoints.bean_count') }}</view>
        </view>
      </view>
    </view>
    <!-- 积分流水 -->
    <view class="balance-content">
      <view class="section-title">{{ $t('myPoints.myPoints.flow_detail') }}</view>
      <view class="balance-list">
        <view class="balance-list-item" v-for="(item, index) in list" :key="index">
          <view class="transTitle">
            <text class="lable">{{ $t('myPoints.myPoints.type_label') }}</text>
            <text class="value">{{ item.remark }}</text>
          </view>
          <view class="transTitle">
            <text class="lable">{{ $t('myPoints.myPoints.time_label') }}</text>
            <text class="value">{{ item.createTime }}</text>
          </view>
          <view class="transTitle">
            <text class="lable">{{ $t('myPoints.myPoints.points_label') }}</text>
            <text class="value">{{ item.points }}</text>
          </view>
          <view class="transTitle">
            <text class="lable">{{ $t('myPoints.myPoints.order_no_label') }}</text>
            <text class="value">{{ item.orderNo }}</text>
          </view>
        </view>
      </view>
      <view class="loading-con" v-if="list.length"></view>
      <noData v-if="!loading && list.length === 0" propStatus="0" :propMsg="$t('myPoints.myPoints.no_data')"></noData>
      <view class="safe-area-bottom"></view>
      <uni-popup ref="codePopup" type="center">
        <view class="code-modal">
          <view class="modal-title">{{ $t('myPoints.myPoints.transfer_phone_hint') }}</view>
          <view class="slot-content pr">
            <input
              class="modal-input clearable-input"
              v-model="phone"
              type="text"
              :placeholder="$t('myPoints.myPoints.phone_placeholder')"
            />
            <view class="input-clear-btn pa" v-if="phone" @click="phone = ''">
              <iconfont name="icon-close" size="28rpx" color="#999"></iconfont>
            </view>
          </view>
          <view class="modal-actions">
            <button class="btn-cancel" @click="showCode = false">{{ $t('myPoints.myPoints.cancel') }}</button>
            <button class="btn-confirm" @click="handleConfirm">{{ $t('myPoints.myPoints.confirm') }}</button>
          </view>
        </view>
      </uni-popup>
    </view>
  </view>
  </view>
</template>

<script>
import dayjs from 'dayjs'
import noData from '@/components/no-data/no-data.vue'
import { getUserPoints, getPointsList } from '@/api/pageUser'

export default {
  data() {
    return {
      theme_view: getApp().globalData.get_theme_value_view(),
      userPredeposit: 2000,
      queryInfo: {
        pageNum: 1,
        pageSize: 10
      },
      list: [],
      loading: false,
      showCode: false,
      phone: '',
      userPoints: {
        availablePoints: 0
      },
      total: 0,
      types: ['获得', '使用', '过期', '锁定', '解锁']
    }
  },
  computed: {
    loadStatus() {
      return this.queryInfo.pageNum === 0 ? 'nomore' : 'loading'
    }
  },
  watch: {
    showCode(newVal) {
      if (newVal) {
        this.$refs.codePopup.open()
      } else {
        this.$refs.codePopup.close()
      }
    }
  },
  onShow() {},

  methods: {
    async getpoints() {
      let res = await getUserPoints()
      if (res.code === '00000') {
        this.userPoints = res.data
      }
    },
    search() {
      this.queryInfo.pageNum = 1
      this.list = []
      this.getCustomersList()
    },
    changeSort() {
      this.queryInfo.isDesc = this.queryInfo.isDesc === 1 ? 0 : 1
      this.queryInfo.pageNum = 1
      this.list = []
      this.getCustomersList()
    },
    getCustomersList() {
      this.loading = true
      getPointsList(this.queryInfo)
        .then(res => {
          const list = res.data.list.map(item => {
            return {
              ...item,
              createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss')
            }
          })
          this.list = this.queryInfo.pageNum <= 1 ? list : this.list.concat(list)
          this.queryInfo.pageNum =
            res.data.length < this.queryInfo.pageSize ? 0 : this.queryInfo.pageNum + 1
          this.total = res.data.total
        })
        .finally(() => {
          this.loading = false
        })
    },
    onStatusRangeChange({ value }) {
      this.queryInfo.type = value
      this.queryInfo.pageNum = 1
      this.getCustomersList()
    },
    setShowModel() {
      this.phone = ''
      this.showCode = true
    },
    handleConfirm() {}
  },
  onLoad() {
    this.getpoints()
    this.getCustomersList()
  },
  onReachBottom() {
    if (this.list.length >= this.total) {
      this.loading = false
      return
    }
    this.getCustomersList()
  }
}
</script>

<style lang="scss" scoped>
.total {
  min-height: 100vh;
  padding: 0;
  box-sizing: border-box;
  background-color: #f5f5f5;
}

// 渐变头部样式
.points-header {
  padding: 32rpx 32rpx 36rpx;
  text-align: center;
  color: #fff;
  background: linear-gradient(135deg, #ff7159 0%, #ffab91 100%);

  .header-title {
    font-size: 24rpx;
    opacity: 0.9;
    margin-bottom: 20rpx;
  }

  .header-stats {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.15);
    border-radius: 12rpx;
    padding: 24rpx 20rpx;
    margin: 0 16rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 48rpx;
        font-weight: 600;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 22rpx;
        opacity: 0.9;
        margin-top: 6rpx;
      }
    }
  }

  .header-action {
    margin-top: 24rpx;

    .action-btn {
      display: inline-block;
      background-color: rgba(255, 255, 255, 0.2);
      color: #fff;
      font-size: 26rpx;
      padding: 14rpx 40rpx;
      border-radius: 36rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.3);
    }
  }
}

// 流水内容区域
.balance-content {
  padding: 0 30rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    padding: 32rpx 0 24rpx;
  }
}

.balance-list-item {
  margin-bottom: 20rpx;
  background-color: #fff;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
  overflow: hidden;
  padding: 24rpx 30rpx;

  .transTitle {
    margin-bottom: 10rpx;

    .lable {
      font-size: 26rpx;
      font-weight: 400;
      color: #999;
      margin-right: 12rpx;
    }
    font-size: 28rpx;
    color: #333;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
}

.clearable-input {
  padding-right: 56rpx;
}
.input-clear-btn {
  top: 50%;
  right: 12rpx;
  transform: translateY(-50%);
  padding: 8rpx;
}
</style>
