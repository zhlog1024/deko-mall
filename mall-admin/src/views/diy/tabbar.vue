<template>
  <div class="tabbar-designer-container">
    <TabbarDesigner />
  </div>
</template>

<script setup lang="ts">
import TabbarDesigner from './views/tabbar/index.vue';

// 导入装修器样式
import './styles/index.scss';
</script>

<style scoped lang="scss">
// 底部导航装修器需脱离后端框架的布局容器限制（同 DIY 页面装修壳）
.tabbar-designer-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000; // 确保在最上层
  background-color: #fff;
  overflow: hidden;
}

// 重置父容器的影响
:deep(.app-main) {
  height: 100% !important;
  min-height: 100% !important;
  padding: 0 !important;
  overflow: visible !important;
}

:deep(.main-container) {
  height: 100vh !important;
  overflow: visible !important;
}
</style>
