<!--
 * @Description  : 
 * @Version      : 1.0
 * @Author       : 
 * @Date         : 2025-01-17 10:14:01
 * @LastEditors  : zhlog1024
 * @LastEditTime : 2025-03-12 15:54:17
-->
<template>
  <section class="app-main">
    <router-view>
      <template #default="{ Component, route }">
        <transition
          enter-active-class="animate__animated animate__fadeIn"
          mode="out-in"
        >
          <keep-alive :include="cachedViews">
            <component :is="Component" :key="route.path" />
          </keep-alive>
        </transition>
      </template>
    </router-view>
  </section>
</template>

<script setup lang="ts">
import { useTagsViewStore } from "@/store";

const cachedViews = computed(() => useTagsViewStore().cachedViews); // 缓存页面集合
console.log(cachedViews,'cachedViews');
</script>

<style lang="scss" scoped>
.app-main {
  position: relative;
  width: 100%;
  min-height: calc(100vh - $navbar-height);
  overflow: hidden;
  background-color: var(--el-bg-color-page);
}

.hasTagsView .app-main {
  min-height: calc(100vh - $navbar-height - $tags-view-height);
}

.fixed-header + .app-main {
  min-height: 100vh;
  padding-top: $navbar-height;
}

.hasTagsView .fixed-header + .app-main {
  min-height: 100vh;
  padding-top: $navbar-height + $tags-view-height;
}

.layout-mix,
.layout-top {
  .fixed-header + .app-main {
    padding-top: 0;
  }
}

.layout-mix {
  .app-main {
    height: calc(100vh - $navbar-height);
    padding-top: 0;
    overflow-y: auto;
  }

  .hasTagsView .app-main {
    height: calc(100vh - $navbar-height - $tags-view-height);
    min-height: calc(100vh - $navbar-height - $tags-view-height);
  }

  .fixed-header + .app-main {
    min-height: calc(100vh - $navbar-height);
  }

  .hasTagsView .fixed-header + .app-main {
    height: calc(100vh - $navbar-height);
    min-height: calc(100vh - $navbar-height);
    padding-top: $tags-view-height;
  }
}

.layout-top {
  .hasTagsView .fixed-header + .app-main {
    padding-top: $tags-view-height;
  }
}
</style>
