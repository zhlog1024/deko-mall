<template>
  <view class="ccp-wrapper" v-if="enabled">
    <!-- 触发区：区号 + 箭头 -->
    <view class="ccp-trigger" @tap="openPicker">
      <text class="ccp-code">{{ selectedCode }}</text>
      <text class="ccp-arrow">▼</text>
    </view>
    <!-- 分隔线 -->
    <view class="ccp-divider"></view>

    <!-- 弹窗选择器 -->
    <view class="ccp-overlay" v-if="pickerVisible" @tap="closePicker">
      <view class="ccp-popup" @tap.stop="">
        <view class="ccp-popup-header">
          <text class="ccp-popup-title">选择国家/地区</text>
          <text class="ccp-popup-close" @tap="closePicker">✕</text>
        </view>
        <view class="ccp-search">
          <input
            type="text"
            v-model="searchText"
            placeholder="搜索国家名称或区号"
            class="ccp-search-input"
            @input="onSearch"
          />
        </view>
        <scroll-view class="ccp-list" scroll-y="true">
          <view
            v-for="item in filteredList"
            :key="item.phoneCode"
            class="ccp-item"
            :class="{ 'ccp-item-active': item.phoneCode === selectedCode }"
            @tap="selectCountry(item)"
          >
            <text class="ccp-item-flag">{{ item.flagEmoji || '' }}</text>
            <text class="ccp-item-name">{{ item.nameZh }}</text>
            <text class="ccp-item-code">{{ item.phoneCode }}</text>
          </view>
          <view v-if="filteredList.length === 0" class="ccp-empty">
            <text>未找到匹配的国家</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
const app = getApp();

export default {
  name: 'CountryCodePicker',
  props: {
    value: {
      type: String,
      default: '+86',
    },
  },
  data() {
    return {
      enabled: false,
      selectedCode: '+86',
      selectedLength: '11',
      countries: [],
      searchText: '',
      pickerVisible: false,
    };
  },
  computed: {
    filteredList() {
      if (!this.searchText) return this.countries;
      const keyword = this.searchText.toLowerCase();
      return this.countries.filter((c) => {
        return (
          c.nameZh.includes(keyword) ||
          c.nameEn.toLowerCase().includes(keyword) ||
          c.phoneCode.includes(keyword)
        );
      });
    },
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.enabled =
        parseInt(app.globalData.get_config('enable_international_phone', 0)) === 1;
      if (!this.enabled) {
        this.selectedCode = '+86';
        this.selectedLength = '11';
        return;
      }
      const lastCountry = uni.getStorageSync('selected_country_code');
      if (lastCountry) {
        this.selectedCode = lastCountry.phoneCode || '+86';
        this.selectedLength = lastCountry.phoneLength || '11';
      } else {
        this.selectedCode = this.value || '+86';
      }
      this.loadCountries();
    },
    loadCountries() {
      const { request } = require('@/config/request');
      request({
        url: '/mall-system/app-api/v1/country-codes',
        method: 'get',
      })
        .then((res) => {
          if (res.code === '00000' && res.data) {
            this.countries = res.data;
          }
        })
        .catch(() => {
          this.countries = [{ phoneCode: '+86', nameZh: '中国', nameEn: 'China', phoneLength: '11' }];
        });
    },
    openPicker() {
      this.searchText = '';
      this.pickerVisible = true;
    },
    closePicker() {
      this.pickerVisible = false;
    },
    selectCountry(item) {
      this.selectedCode = item.phoneCode;
      this.selectedLength = item.phoneLength || '11';
      this.pickerVisible = false;
      uni.setStorageSync('selected_country_code', {
        phoneCode: item.phoneCode,
        phoneLength: item.phoneLength,
      });
      this.$emit('change', {
        countryCode: item.phoneCode,
        phoneLength: item.phoneLength,
      });
    },
    onSearch() {},
    getSelectedCode() {
      return this.selectedCode;
    },
  },
};
</script>

<style scoped>
/* 外层：与父级胶囊容器对齐 */
.ccp-wrapper {
  display: flex;
  align-items: center;
  height: 100%;
  flex-shrink: 0;
}

/* 触发区 */
.ccp-trigger {
  display: flex;
  align-items: center;
  padding: 0 20rpx 0 30rpx;
}
.ccp-code {
  font-size: 28rpx;
  color: #333;
  margin-right: 8rpx;
  white-space: nowrap;
}
.ccp-arrow {
  font-size: 16rpx;
  color: #999;
}

/* 分隔竖线 */
.ccp-divider {
  width: 1rpx;
  height: 28rpx;
  background: #ddd;
  margin-right: 20rpx;
}

/* 弹窗 (不变) */
.ccp-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.ccp-popup {
  width: 100%;
  max-height: 70vh;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  display: flex;
  flex-direction: column;
}
.ccp-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #eee;
}
.ccp-popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.ccp-popup-close {
  font-size: 36rpx;
  color: #999;
  padding: 10rpx;
}
.ccp-search {
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}
.ccp-search-input {
  width: 100%;
  height: 70rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.ccp-list {
  flex: 1;
  max-height: 55vh;
}
.ccp-item {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}
.ccp-item-active {
  background: #f0f7ff;
}
.ccp-item-flag {
  font-size: 36rpx;
  margin-right: 20rpx;
}
.ccp-item-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}
.ccp-item-code {
  font-size: 26rpx;
  color: #999;
}
.ccp-empty {
  text-align: center;
  padding: 60rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
