<template>
  <div class="setting-content">
    <template v-if="type == '1'">
      <model-ask-content :value="form.content" :styles="form.style" />
    </template>
    <template v-else-if="type == '2'">
      <model-ask-styles :value="form.style" :content="form.content" />
    </template>
  </div>
</template>
<script setup lang="ts">
/**
 * @description: 商品问答（设置）
 * @param type{String} 类型(1：内容,2：样式)
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
