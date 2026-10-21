<template>
  <div>
    <!-- 上传组件 -->
    <el-upload
      ref="singleUploadRef"
      action=""
      class="single-uploader"
      :show-file-list="false"
      :before-upload="handleBeforeUpload"
      :http-request="uploadImage"
    >
      <el-image v-if="imgUrl" :src="imgUrl" class="single-uploader__image" />
      <el-icon v-else class="single-uploader__plus"><Plus /></el-icon>
      <!-- 删除图标 -->
      <el-icon
        v-if="props.showClose && imgUrl"
        class="single-uploader__remove"
        @click.stop="handleRemove(imgUrl)"
        ><Close
      /></el-icon>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { Plus, Close } from "@element-plus/icons-vue";
import {
  ElMessage,
  ElUpload,
  UploadRawFile,
  UploadRequestOptions,
} from "element-plus";
import { uploadFileApi, deleteFileApi } from "@/api/file";

const emit = defineEmits(["update:modelValue"]);
const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
  /**
   * 是否显示右上角的删除图片按钮
   */
  showClose: {
    type: Boolean,
    default: false,
  },
});

const imgUrl = computed<string | undefined>({
  get() {
    return props.modelValue;
  },
  set(val) {
    // imgUrl改变时触发修改父组件绑定的v-model的值
    emit("update:modelValue", val);
  },
});

/**
 * 自定义图片上传
 */
async function uploadImage(options: UploadRequestOptions): Promise<any> {
  const response = await uploadFileApi(options.file);
  imgUrl.value = response.data.filePath;
}

/**
 * 删除图片
 */
// 删除图片
function handleRemove(fileUrl?: string) {
  imgUrl.value = "";
}

/**
 * 在 before-upload 钩子中限制用户上传文件的格式和大小
 */
function handleBeforeUpload(file: UploadRawFile) {
  const isLt2M = file.size / 1024 / 1024 < 10;
  if (!isLt2M) {
    ElMessage.warning("上传图片不能大于10M");
  }
  return true;
}
</script>

<style lang="scss" scoped>
.single-uploader {
  width: 146px;
  height: 146px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  text-align: center;
  &:hover {
    border-color: var(--el-color-primary);
  }
  .single-uploader__image {
    width: 146px;
    height: 146px;
    display: block;
  }
  .single-uploader__plus {
    width: 146px;
    height: 157px;
    font-size: 28px;
    color: #8c939d;
    text-align: center;
  }
  .single-uploader__remove {
    font-size: 12px;
    color: #000 !important;
    margin-top: 0px !important;
    position: absolute;
    right: 0;
    top: 0;
    color: #409eff;
  }
}
</style>
