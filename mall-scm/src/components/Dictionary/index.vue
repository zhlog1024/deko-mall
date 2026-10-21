<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :multiple="multiple"
    :collapse-tags="collapseTags"
    :collapse-tags-tooltip="collapseTags"
    clearable
    @change="handleChange"
  >
    <el-option
      v-for="option in options"
      :key="option.value"
      :label="option.label"
      :value="option.value"
    />
  </el-select>
</template>

<style scoped>
.el-select {
  width: 100%;
  min-width: 120px;
}
</style>

<script setup lang="ts">
import { getDictOptions } from "@/api/system/dict";

const props = defineProps({
  /**
   * 字典类型编码(eg: 性别-gender)
   */
  typeCode: {
    type: String,
    required: true,
  },
  modelValue: {
    type: [String, Number, Array],
    default: undefined,
  },
  placeholder: {
    type: String,
    default: "请选择",
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否多选
   */
  multiple: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否折叠标签（多选时使用）
   */
  collapseTags: {
    type: Boolean,
    default: true,
  },
});

const emits = defineEmits(["update:modelValue"]);

const options: Ref<OptionType[]> = ref([]);

// 根据 multiple 属性初始化不同的值
const selectedValue = ref<string | number | string[] | number[] | undefined>();

watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal === undefined || newVal === null) {
      selectedValue.value = props.multiple ? [] : undefined;
      return;
    }
    selectedValue.value = newVal as any;
  },
  { immediate: true }
);

watch(
  [options, () => props.modelValue],
  ([newOptions, newModelValue]) => {
    if (newOptions.length === 0) return;
    if (newModelValue === undefined || newModelValue === null) {
      selectedValue.value = props.multiple ? [] : undefined;
      return;
    }
    selectedValue.value = newModelValue as any;
  }
);

function handleChange(val?: any) {
  emits("update:modelValue", val);
}

onBeforeMount(() => {
  getDictOptions(props.typeCode).then((response) => {
    options.value = response.data;
  });
});
</script>
