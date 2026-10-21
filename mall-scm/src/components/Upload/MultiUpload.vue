<!-- 多图上传组件 -->

<template>
  <div ref="uploadWrapRef" class="multi-upload">
    <el-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      multiple
      :before-upload="handleBeforeUpload"
      :http-request="handleUpload"
      :on-remove="handleRemove"
      :on-preview="previewImg"
      :limit="props.limit"
    >
      <i-ep-plus />
    </el-upload>

    <el-dialog v-model="dialogVisible">
      <img w-full :src="previewImgUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  UploadRawFile,
  UploadRequestOptions,
  UploadUserFile,
  UploadFile,
  UploadProps,
} from "element-plus";
import Sortable from "sortablejs";
import { uploadFileApi, deleteFileApi } from "@/api/file";

const emit = defineEmits(["update:modelValue"]);

const props = defineProps({
  /**
   * 文件路径集合
   */
  modelValue: {
    type: Array<string>,
    default: () => [],
  },
  /**
   * 文件上传数量限制
   */
  limit: {
    type: Number,
    default: 10,
  },
});

const previewImgUrl = ref("");
const dialogVisible = ref(false);

const uploadWrapRef = ref<HTMLElement>();
let sortableInstance: Sortable | null = null;

const fileList = ref([] as UploadUserFile[]);
watch(
  () => props.modelValue,
  (newVal: string[]) => {
    const filePaths = fileList.value.map((file) => file.url);
    // 监听modelValue文件集合值未变化时，跳过赋值
    if (
      filePaths.length > 0 &&
      filePaths.length === newVal.length &&
      filePaths.every((x) => newVal.some((y) => y === x)) &&
      newVal.every((y) => filePaths.some((x) => x === y))
    ) {
      return;
    }

    fileList.value = newVal.map((filePath) => {
      return { url: filePath } as UploadUserFile;
    });
  },
  { immediate: true }
);

/**
 * 初始化拖拽排序（绑定 el-upload 列表容器）
 */
const initSortable = () => {
  nextTick(() => {
    const listEl = uploadWrapRef.value?.querySelector(
      ".el-upload-list"
    ) as HTMLElement | null;
    if (!listEl) return;
    sortableInstance?.destroy();
    sortableInstance = Sortable.create(listEl, {
      animation: 150,
      // "+" 上传按钮不可拖拽、不可作为放置目标
      filter: ".el-upload__trigger",
      onEnd: (evt: { oldIndex?: number; newIndex?: number }) => {
        const { oldIndex, newIndex } = evt;
        if (oldIndex === undefined || newIndex === undefined || oldIndex === newIndex) {
          return;
        }
        // 防止拖到末尾 "+" 按钮之后
        let target = newIndex;
        if (target >= fileList.value.length) {
          target = fileList.value.length - 1;
        }
        const moved = fileList.value.splice(oldIndex, 1)[0];
        fileList.value.splice(target, 0, moved);
        emit(
          "update:modelValue",
          fileList.value.map((file) => file.url)
        );
      },
    });
  });
};

// 挂载与列表增删/回显后重新绑定
onMounted(() => initSortable());
watch(() => fileList.value.length, () => initSortable());

/**
 * 自定义图片上传
 */
async function handleUpload(options: UploadRequestOptions): Promise<any> {
  // 上传API调用
  const { data: fileInfo } = await uploadFileApi(options.file);

  // 上传成功需手动替换文件路径为远程URL，否则图片地址为预览地址 blob:http://
  const fileIndex = fileList.value.findIndex(
    (file) => file.uid == (options.file as any).uid
  );

  fileList.value.splice(fileIndex, 1, {
    name: fileInfo.name,
    url: fileInfo.url,
  } as UploadUserFile);

  emit(
    "update:modelValue",
    fileList.value.map((file) => file.url)
  );
}

/**
 * 删除图片
 */
function handleRemove(removeFile: UploadFile) {
  const filePath = removeFile.url;

  if (filePath) {
    deleteFileApi(filePath).then(() => {
      // 删除成功回调
      emit(
        "update:modelValue",
        fileList.value.map((file) => file.url)
      );
    });
  }
}

/**
 * 限制用户上传文件的格式和大小
 */
function handleBeforeUpload(file: UploadRawFile) {
  if (file.size > 2 * 1048 * 1048) {
    ElMessage.warning("上传图片不能大于2M");
    return false;
  }
  return true;
}

/**
 * 预览图片
 */
const previewImg: UploadProps["onPreview"] = (uploadFile) => {
  previewImgUrl.value = uploadFile.url!;
  dialogVisible.value = true;
};
</script>
