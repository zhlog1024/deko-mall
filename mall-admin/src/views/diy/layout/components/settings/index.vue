<template>
  <!-- 顶部导航栏 -->
  <div class="settings">
    <card-container
      class="settings-title flex-row jc-sb align-c mb-8"
      padding="2.1rem 3.8rem"
    >
      <div class="title">{{ value.name }}</div>
      <el-radio-group
        v-model="radio"
        class="radio-group"
        size="large"
        is-button
      >
        <el-radio-button class="radio-item" value="1">内容</el-radio-button>
        <el-radio-button class="radio-item" value="2">样式</el-radio-button>
      </el-radio-group>
    </card-container>
    <div
      ref="scrollTop"
      class="setting-content"
      :style="value.key == 'custom' ? '' : 'background-color: #fff;'"
    >
      <!-- 基础组件 -->
      <!-- 页面设置 -->
      <template v-if="value.key == 'page-settings'">
        <page-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 基础组件 -->
      <template v-else-if="value.key == 'video'">
        <model-video-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 搜索框 -->
      <template v-else-if="value.key == 'search'">
        <model-search-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 导航组 -->
      <template v-else-if="value.key == 'nav-group'">
        <model-nav-group-setting
          :type="radio"
          :value="value.com_data"
          @set_offset_top="set_offset_top"
        />
      </template>
      <!-- 轮播图 -->
      <template v-else-if="value.key == 'carousel'">
        <model-carousel-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 公告 -->
      <template v-else-if="value.key == 'notice'">
        <model-notice-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 自定义（ShopXO model-custom） -->
      <template v-else-if="value.key == 'custom'">
        <model-custom-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 选项卡 -->
      <template v-else-if="value.key == 'tabs'">
        <model-tabs-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 选项卡轮播 -->
      <template v-else-if="value.key == 'tabs-carousel'">
        <model-tabs-carousel-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 选项卡魔方 -->
      <template v-else-if="value.key == 'tabs-magic'">
        <model-tabs-magic-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 商品列表 -->
      <template v-else-if="value.key == 'goods-list'">
        <model-goods-list-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 商品魔方 -->
      <template v-else-if="value.key == 'goods-magic'">
        <model-goods-magic-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 文章列表 -->
      <template v-else-if="value.key == 'article-list'">
        <model-article-list-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 店铺（多商户） -->
      <template v-else-if="value.key == 'shop'">
        <model-shop-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 直播列表（插件） -->
      <template v-else-if="value.key == 'plugins-live'">
        <model-plugins-live-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 直播选项卡（插件） -->
      <template v-else-if="value.key == 'plugins-live-tabs'">
        <model-plugins-live-tabs-setting
          :type="radio"
          :value="value.com_data"
        />
      </template>
      <!-- 商品选项卡 -->
      <template v-else-if="value.key == 'goods-tabs'">
        <model-goods-tabs-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 文章选项卡 -->
      <template v-else-if="value.key == 'article-tabs'">
        <model-article-tabs-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 商品问答 -->
      <template v-else-if="value.key == 'ask'">
        <model-ask-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 商品问答选项卡 -->
      <template v-else-if="value.key == 'ask-tabs'">
        <model-ask-tabs-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 视频 -->
      <template v-else-if="value.key == 'plugins-video'">
        <model-plugins-video-list-setting
          :type="radio"
          :value="value.com_data"
        />
      </template>
      <!-- 视频选项卡 -->
      <template v-else-if="value.key == 'plugins-video-tabs'">
        <model-plugins-video-tabs-setting
          :type="radio"
          :value="value.com_data"
        />
      </template>
      <!-- 数据选项卡 -->
      <template v-else-if="value.key == 'data-tabs'">
        <model-data-tabs-setting
          :type="radio"
          :value="value.com_data"
          @set_offset_top="set_offset_top"
        />
      </template>
      <!-- 图片魔方 -->
      <template v-else-if="value.key == 'img-magic'">
        <model-img-magic-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 图片魔方 -->
      <template v-else-if="value.key == 'data-magic'">
        <model-data-magic-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 热区 -->
      <template v-else-if="value.key == 'hot-zone'">
        <model-hot-zone-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 底部导航 -->
      <template v-else-if="value.key == 'footer-nav'">
        <footer-nav-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 工具组件 -->
      <!-- 辅助空白 -->
      <template v-else-if="value.key == 'auxiliary-blank'">
        <model-auxiliary-blank-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 辅助线 -->
      <template v-else-if="value.key == 'row-line'">
        <model-auxiliary-line-setting :type="radio" :value="value.com_data" />
      </template>
      <!--富文本 -->
      <template v-else-if="value.key == 'rich-text'">
        <model-rich-text-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 标题 -->
      <template v-else-if="value.key == 'title'">
        <model-title-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 用户信息 -->
      <template v-else-if="value.key == 'user-info'">
        <model-user-info-setting :type="radio" :value="value.com_data" />
      </template>
      <!-- 悬浮按钮 -->
      <template v-else-if="value.key == 'float-window'">
        <model-float-window-setting :type="radio" :value="value.com_data" />
      </template>
    </div>
  </div>
</template>
<script setup lang="ts">
const props = defineProps({
  value: {
    type: Object,
    default: () => ({}),
  },
});

const radio = ref("1"); // 创建一个响应式的数字变量，初始值为0

const scrollTop = ref<HTMLElement | null>(null);
const set_offset_top = (scrollY: number) => {
  if (scrollTop.value) {
    // 选中的滚动到指定位置
    scrollTop.value.scrollTo({ top: scrollY - 160, behavior: "smooth" });
  }
};
</script>
<style lang="scss" scoped>
@use "../../../styles/variables" as *;
.settings {
  width: 46rem;
  overflow: auto;
  display: flex;
  flex-direction: column;
  .settings-title {
    height: 7.8rem;
    .title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }
    .radio-group {
      background: #f4f4f4;
      border-radius: 100rem;
      .el-radio-button {
        overflow: hidden;
        border-radius: 100rem;
        :deep(.el-radio-button__inner) {
          border: 0;
          background: #f4f4f4;
        }
        :deep(
          .el-radio-button__original-radio:checked + .el-radio-button__inner
        ) {
          background: $cr-primary;
        }
      }
    }
  }
  .setting-content {
    height: calc(100vh - 14.8rem);
    overflow: auto;
  }
  :deep(.el-input-number) {
    width: 100%;
  }
}
@media screen and (max-width: 1560px) {
  .settings {
    width: 40rem;
  }
}
</style>
