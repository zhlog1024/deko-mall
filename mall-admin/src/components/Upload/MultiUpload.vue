<!--
  多文件上传组件
  返回 UploadFileItem[] 格式
-->
<template>
  <div ref="uploadWrapRef" class="multi-upload">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :accept="accept"
      :limit="limit"
      :before-upload="handleBeforeUpload"
      :http-request="handleUpload"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      list-type="picture-card"
      multiple
    >
      <el-icon><i-ep-plus /></el-icon>
    </el-upload>

    <el-dialog v-model="previewVisible">
      <img v-if="previewType === 'image'" :src="previewUrl" class="preview-img" alt="preview" />
      <video v-else-if="previewType === 'video'" :src="previewUrl" controls class="preview-video"></video>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFile } from 'element-plus'
import Sortable from 'sortablejs'
import { uploadFileApi } from '@/api/file'
import type { UploadFileItem } from '@/api/file'

const props = defineProps({
  modelValue: {
    type: Array as () => UploadFileItem[],
    default: () => [],
  },
  limit: {
    type: Number,
    default: 10,
  },
  fileSize: {
    type: Number,
    default: 10, // MB
  },
  fileType: {
    type: String as () => 'img' | 'video' | 'file',
    default: 'img',
  },
})

const emit = defineEmits(['update:modelValue'])

const uploadRef = ref()
const uploadWrapRef = ref<HTMLElement>()
const fileList = ref<any[]>([])
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'video'>('image')

let sortableInstance: Sortable | null = null

/**
 * 初始化拖拽排序（绑定 el-upload 列表容器）
 */
const initSortable = () => {
  nextTick(() => {
    const listEl = uploadWrapRef.value?.querySelector(
      '.el-upload-list'
    ) as HTMLElement | null
    if (!listEl) return
    sortableInstance?.destroy()
    sortableInstance = Sortable.create(listEl, {
      animation: 150,
      // "+" 上传按钮不可拖拽、不可作为放置目标
      filter: '.el-upload__trigger',
      onEnd: (evt: { oldIndex?: number; newIndex?: number }) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex === undefined || newIndex === undefined || oldIndex === newIndex) {
          return
        }
        // 防止拖到末尾 "+" 按钮之后
        let target = newIndex
        if (target >= fileList.value.length) {
          target = fileList.value.length - 1
        }
        const moved = fileList.value.splice(oldIndex, 1)[0]
        fileList.value.splice(target, 0, moved)
        // 输出字符串数组格式（与后端 subPicUrls 字段对应）
        emit('update:modelValue', fileList.value.map((f: any) => f.url).filter(Boolean))
      },
    })
  })
}

// 挂载与列表增删/回显后重新绑定
onMounted(() => initSortable())
watch(() => fileList.value.length, () => initSortable())

const accept = computed(() => {
  switch (props.fileType) {
    case 'img':
      return '.jpg,.jpeg,.png,.gif,.bmp,.webp,.svg'
    case 'video':
      return '.mp4,.avi,.mov,.wmv,.flv,.mkv,.webm'
    default:
      return ''
  }
})

// 监听外部值
watch(
  () => props.modelValue,
  (newVal) => {
    if (!Array.isArray(newVal)) return
    const fileUrls = fileList.value.map((f: any) => f.url).filter(Boolean)
    const newUrls = newVal
      .map((item: any) => (typeof item === 'string' ? item : item.url))
      .filter(Boolean)
    // 内容完全一致时跳过，避免上传过程中父组件回写覆盖 fileList（破坏 uid 定位）
    if (
      fileUrls.length > 0 &&
      fileUrls.length === newUrls.length &&
      fileUrls.every((x) => newUrls.includes(x)) &&
      newUrls.every((y) => fileUrls.includes(y))
    ) {
      return
    }
    fileList.value = newVal.map((item: any) => {
      // 如果 item 是字符串（URL），转换为 UploadFileItem 格式
      if (typeof item === 'string') {
        return {
          url: item,
          name: item.split('/').pop() || 'image',
        }
      }
      // 如果 item 已经是对象，保持原有逻辑
      return {
        ...item,
        url: item.url,
        name: item.original || item.title || '',
      }
    })
  },
  { immediate: true, deep: true }
)

// 上传前校验
const handleBeforeUpload = (file: File) => {
  const maxSize = props.fileSize * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning(`文件大小不能超过 ${props.fileSize}MB`)
    return false
  }
  return true
}

// 自定义上传
const handleUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  try {
    const res: any = await uploadFileApi(file as File)

    const uploadItem: UploadFileItem = {
      id: res.data?.id || res.id,
      url: res.data?.url || res.url || res.filePath,
      original: res.data?.originalName || res.originalName || file.name,
      ext: res.data?.fileExt || file.name.split('.').pop(),
      size: file.size,
      type: props.fileType,
    }
    onSuccess(uploadItem)

    // 以 el-upload 的 fileList 为唯一数据源：批量选择时文件已按选择顺序追加到末尾，
    // 按 uid 定位原地替换为远程 URL，顺序=选择顺序，且并发上传不会互相覆盖
    const fileIndex = fileList.value.findIndex((f: any) => f.uid === options.file.uid)
    if (fileIndex > -1) {
      fileList.value.splice(fileIndex, 1, {
        ...(fileList.value[fileIndex] || {}),
        uid: options.file.uid,
        name: uploadItem.original || file.name,
        url: uploadItem.url,
        status: 'success',
      })
    }
    // 输出字符串数组格式（与后端 subPicUrls 字段对应）
    emit('update:modelValue', fileList.value.map((f: any) => f.url).filter(Boolean))
  } catch (error: any) {
    onError(error)
  }
}

// 文件移除
const handleRemove = (file: UploadFile) => {
  // 基于 el-upload fileList 按 uid 定位删除，保证与渲染顺序一致
  const index = fileList.value.findIndex((f: any) => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
    emit('update:modelValue', fileList.value.map((f: any) => f.url).filter(Boolean))
  }
}

// 文件数量超限
const handleExceed = () => {
  ElMessage.warning(`最多上传 ${props.limit} 个文件`)
}

// 预览
const handlePreview = (file: UploadFile) => {
  previewUrl.value = file.url || ''
  if (file.url?.match(/\.(mp4|avi|mov|wmv|flv|mkv|webm)$/i)) {
    previewType.value = 'video'
  } else {
    previewType.value = 'image'
  }
  previewVisible.value = true
}

// 监听 el-upload 的 preview 事件
watch(
  () => uploadRef.value,
  (upload) => {
    if (upload) {
      upload.handlePreview = handlePreview
    }
  }
)
</script>

<style scoped lang="scss">
.multi-upload {
  :deep(.el-upload-list--picture-card) {
    .el-upload-list__item-actions {
      .el-icon-view,
      .el-icon-delete {
        font-size: 18px;
      }
    }
  }
}
</style>
