<!--
  * @Description  : 单图上传组件（尺寸统一由 width/height 控制，容器/图片/图标共用 CSS 变量，杜绝固定尺寸残留）
  * @Version      : 2.0
  * 对齐 mall-admin 端 SingleUpload 实现；图标额外 flex 居中
-->
<template>
  <!-- 上传组件 -->
  <el-upload
    v-model="imgUrl"
    class="single-uploader"
    :style="uploadStyle"
    :show-file-list="false"
    list-type="picture-card"
    :before-upload="handleBeforeUpload"
    :http-request="uploadFile"
  >
    <img
      v-if="imgUrl"
      :src="imgUrl"
      class="single-uploader__image"
    />
    <el-icon v-else class="single-uploader__icon"><i-ep-plus /></el-icon>
  </el-upload>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { UploadRawFile, UploadRequestOptions } from "element-plus";
import { uploadFileApi } from "@/api/file";

const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
  /**
   * 上传框宽度（数字按 px，或传字符串如 '300px'）
   */
  width: {
    type: [String, Number],
    default: 150,
  },
  /**
   * 上传框高度（数字按 px，或传字符串如 '100px'）
   */
  height: {
    type: [String, Number],
    default: 150,
  },
});

const emit = defineEmits(["update:modelValue"]);
const imgUrl = useVModel(props, "modelValue", emit);

// 统一尺寸来源：CSS 变量，容器/图片/空状态图标全部跟随，保证上传前后尺寸一致
const uploadStyle = computed(() => {
  const w = typeof props.width === "number" ? `${props.width}px` : props.width;
  const h = typeof props.height === "number" ? `${props.height}px` : props.height;
  return { "--upload-width": w, "--upload-height": h } as Record<string, string>;
});

/**
 * 自定义图片上传
 */
async function uploadFile(options: UploadRequestOptions): Promise<any> {
  const { data: fileInfo } = await uploadFileApi(options.file);
  imgUrl.value = fileInfo.url;
}

/**
 * 限制用户上传文件的格式和大小
 */
function handleBeforeUpload(file: UploadRawFile) {
  if (file.size > 10 * 1048 * 1048) {
    ElMessage.warning("上传图片不能大于2M");
    return false;
  }
  return true;
}
</script>

<style scoped lang="scss">
.single-uploader {
  overflow: hidden;
  cursor: pointer;
  border: 1px var(--el-border-color) solid;
  border-radius: 6px;

  &:hover {
    border-color: var(--el-color-primary);
  }

  :deep(.el-upload--picture-card) {
    width: var(--upload-width) !important;
    height: var(--upload-height) !important;
  }

  &__image {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #8c939d;
  }
}
</style>
