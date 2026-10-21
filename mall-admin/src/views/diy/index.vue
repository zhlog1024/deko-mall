<template>
  <div class="designer-container">
    <DesignerLayout />
  </div>
</template>

<script setup lang="ts">
import DesignerLayout from './views/index.vue';

// 导入装修器样式
import './styles/index.scss';
</script>

<style lang="scss" scoped>
// 装修器需要脱离后端框架的布局容器限制
.designer-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000; // 确保在最上层
  background-color: #f0f2f5;
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

