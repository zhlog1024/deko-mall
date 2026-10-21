<template>
  <div class="setting-content">
    <template v-if="type == '1'">
      <model-plugins-video-tabs-content
        :value="form.content"
        :styles="form.style"
      />
    </template>
    <template v-else-if="type == '2'">
      <model-plugins-video-tabs-styles
        :value="form.style"
        :content="form.content"
      />
    </template>
  </div>
</template>
<script setup lang="ts">
/**
 * @description: 视频选项卡（设置）
 * @param type{String} 类型是进入内容组件还是样式组件
 * @param value{Object} 样式数据
 */
const props = defineProps({
  type: {
    type: String,
    default: "1",
  },
  value: {
    type: Object,
    default: () => ({}),
  },
});
const form = ref(props.value);
</script>
