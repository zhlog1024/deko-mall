<template>
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
import { computed } from 'vue';
import type { UploadRawFile, UploadRequestOptions } from 'element-plus';
import { uploadFileApi } from '@/api/file';

const props = defineProps({
  modelValue: { type: String, default: '' },
  width: { type: [String, Number], default: 150 },
  height: { type: [String, Number], default: 150 },
});

const emit = defineEmits(['update:modelValue']);
const imgUrl = useVModel(props, 'modelValue', emit);

const uploadStyle = computed(() => {
  const w = typeof props.width === 'number' ? `${props.width}px` : props.width;
  const h = typeof props.height === 'number' ? `${props.height}px` : props.height;
  return { '--upload-width': w, '--upload-height': h } as Record<string, string>;
});

async function uploadFile(options: UploadRequestOptions): Promise<any> {
  const { data: fileInfo } = await uploadFileApi(options.file);
  imgUrl.value = fileInfo.url;
}

function handleBeforeUpload(file: UploadRawFile) {
  if (file.size > 10 * 1048 * 1048) {
    ElMessage.warning('上传图片不能大于2M');
    return false;
  }
  return true;
}
</script>

<style scoped lang="scss">
.single-uploader {
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
    font-size: 28px;
    color: #8c939d;
  }
}
</style>
