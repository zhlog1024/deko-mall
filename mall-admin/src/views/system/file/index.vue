<script setup lang="ts">
defineOptions({
  name: "SystemFile",
  inheritAttrs: false,
});

import {
  getFilePage,
  getFileCategoryTree,
  saveFileCategoryApi,
  deleteFileCategoryApi,
  deleteFileByIdApi,
  moveFileCategoryApi,
  uploadFileApi,
  type FileInfo,
  type FileQuery,
  type FileCategory,
} from "@/api/file";

const queryFormRef = ref(ElForm);

const state = reactive({
  loading: true,
  ids: [] as number[],
  single: true,
  multiple: true,
  total: 0,
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    keywords: "",
    categoryId: undefined as number | undefined,
    fileType: "" as string,
  } as FileQuery,
  fileList: [] as FileInfo[],
  categoryTree: [] as FileCategory[],
  categoryDialog: {
    visible: false,
    title: "",
  },
  moveDialog: {
    visible: false,
  },
  categoryForm: {
    id: undefined as number | undefined,
    name: "",
    parentId: undefined as number | undefined,
    sort: 0,
  },
  moveForm: {
    categoryId: undefined as number | undefined,
  },
  categoryRules: {
    name: [{ required: true, message: "请输入分类名称", trigger: "blur" }],
  },
  currentFile: null as FileInfo | null,
  previewUrl: "",
  previewVisible: false,
  uploadDialogVisible: false,
  fileTypeOptions: [
    { value: "", label: "全部" },
    { value: "image", label: "图片" },
    { value: "video", label: "视频" },
    { value: "file", label: "文件" },
  ],
  defaultProps: {
    children: "children",
    label: "name",
  },
  uploadTargetCategoryId: undefined as number | undefined,
  uploadFileList: [] as any[],
  uploadLoading: false,
});

const {
  loading,
  multiple,
  queryParams,
  fileList,
  total,
  categoryTree,
  categoryDialog,
  moveDialog,
  categoryForm,
  moveForm,
  categoryRules,
  fileTypeOptions,
  previewVisible,
  previewUrl,
  currentFile,
  uploadDialogVisible,
  uploadTargetCategoryId,
  uploadFileList,
  uploadLoading,
  defaultProps,
} = toRefs(state);

// 加载分类树
function loadCategoryTree() {
  getFileCategoryTree().then(({ data }) => {
    state.categoryTree = data || [];
  });
}

// 查询文件列表
function handleQuery() {
  state.loading = true;
  getFilePage(state.queryParams).then(({ data }) => {
    state.fileList = data.list || [];
    state.total = data.total || 0;
    state.loading = false;
  });
}

// 重置查询
function resetQuery() {
  queryFormRef.value?.resetFields();
  state.queryParams.categoryId = undefined;
  handleQuery();
}

// 表格选择变化
function handleSelectionChange(selection: any) {
  state.ids = selection.map((item: any) => item.id);
  state.single = selection.length !== 1;
  state.multiple = selection.length === 0;
}

// 格式化文件大小
function formatFileSize(size: number): string {
  if (!size) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(size) / Math.log(k));
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

// 预览文件
function handlePreview(row: FileInfo) {
  state.currentFile = row;
  state.previewUrl = row.fileUrl || row.url || '';
  state.previewVisible = true;
}

// 打开上传弹窗
function handleUpload(row?: FileCategory) {
  state.uploadDialogVisible = true;
  state.uploadTargetCategoryId = row?.id;
  state.uploadFileList = [];
}

// 上传文件
function handleUploadChange(_file: any, fileList: any[]) {
  state.uploadFileList = fileList;
}

function handleUploadRemove(_file: any, fileList: any[]) {
  state.uploadFileList = fileList;
}

async function handleUploadExceed() {
  ElMessage.warning("文件数量超出限制");
}

async function submitUpload() {
  if (state.uploadFileList.length === 0) {
    ElMessage.warning("请选择要上传的文件");
    return;
  }

  state.uploadLoading = true;
  let successCount = 0;
  let failCount = 0;

  for (const file of state.uploadFileList) {
    if (file.status === "ready") {
      try {
        await uploadFileApi(file.raw, state.uploadTargetCategoryId);
        successCount++;
      } catch {
        failCount++;
      }
    }
  }

  state.uploadLoading = false;

  if (failCount === 0) {
    ElMessage.success(`上传成功 ${successCount} 个文件`);
    state.uploadDialogVisible = false;
    handleQuery();
    loadCategoryTree();
  } else {
    ElMessage.error(`成功 ${successCount} 个，失败 ${failCount} 个`);
  }
}

// 新增分类
function handleAddCategory() {
  state.categoryDialog = {
    visible: true,
    title: "新增分类",
  };
  state.categoryForm = {
    id: undefined,
    name: "",
    parentId: undefined,
    sort: 0,
  };
}

// 编辑分类
function handleEditCategory(row: FileCategory) {
  state.categoryDialog = {
    visible: true,
    title: "编辑分类",
  };
  state.categoryForm = {
    id: row.id,
    name: row.name,
    parentId: row.parentId,
    sort: row.sort || 0,
  };
}

// 保存分类
function submitCategory() {
  const formEl = window.document.querySelector(".category-form") as any;
  formEl?.validate((valid: boolean) => {
    if (valid) {
      saveFileCategoryApi(state.categoryForm).then(() => {
        ElMessage.success("保存成功");
        state.categoryDialog.visible = false;
        loadCategoryTree();
      });
    }
  });
}

// 删除分类
function handleDeleteCategory(row: FileCategory) {
  ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteFileCategoryApi(row.id).then(() => {
      ElMessage.success("删除成功");
      loadCategoryTree();
    });
  });
}

// 删除文件
function handleDelete(row: FileInfo) {
  ElMessageBox.confirm("确定要删除该文件吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteFileByIdApi(row.id).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

// 批量删除文件
function handleBatchDelete() {
  if (state.ids.length === 0) {
    ElMessage.warning("请选择要删除的文件");
    return;
  }
  ElMessageBox.confirm(`确定要删除选中的 ${state.ids.length} 个文件吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    Promise.all(state.ids.map((id) => deleteFileByIdApi(id))).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

// 打开转移分类弹窗
function handleMove() {
  if (state.ids.length === 0) {
    ElMessage.warning("请选择要转移的文件");
    return;
  }
  state.moveDialog.visible = true;
  state.moveForm.categoryId = undefined;
}

// 提交转移分类
function submitMoveCategory() {
  if (!state.moveForm.categoryId) {
    ElMessage.warning("请选择目标分类");
    return;
  }
  moveFileCategoryApi(state.ids, state.moveForm.categoryId).then(() => {
    ElMessage.success("转移成功");
    state.moveDialog.visible = false;
    handleQuery();
  });
}

// 分类节点点击
function handleCategoryNodeClick(data: any) {
  state.queryParams.categoryId = data.id;
  handleQuery();
}

onMounted(() => {
  loadCategoryTree();
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="file-container">
      <!-- 左侧分类树 -->
      <div class="left-panel">
        <div class="panel-header">
          <span>分类管理</span>
          <el-button type="primary" link @click="handleAddCategory">
            <i-ep-plus />
          </el-button>
        </div>
        <el-scrollbar height="calc(100vh - 260px)">
          <el-tree
            :data="categoryTree"
            :props="defaultProps"
            node-key="id"
            default-expand-all
            @node-click="handleCategoryNodeClick"
          >
            <template #default="{ data }">
              <span class="tree-node">
                <span class="node-label">{{ data.name }}</span>
                <span class="node-actions">
                  <el-button type="primary" link @click.stop="handleUpload(data)">
                    <i-ep-upload />
                  </el-button>
                  <el-button type="primary" link @click.stop="handleEditCategory(data)">
                    <i-ep-edit />
                  </el-button>
                  <el-button type="danger" link @click.stop="handleDeleteCategory(data)">
                    <i-ep-delete />
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>

      <!-- 右侧文件列表 -->
      <div class="right-panel">
        <!-- 搜索表单 -->
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item prop="keywords">
            <el-input
              v-model="queryParams.keywords"
              placeholder="搜索文件名"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item prop="fileType">
            <el-select v-model="queryParams.fileType" placeholder="文件类型" style="width: 120px" clearable>
              <el-option
                v-for="item in fileTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">
              <i-ep-search />
              搜索
            </el-button>
            <el-button @click="resetQuery">
              <i-ep-refresh />
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 操作按钮 -->
        <div class="mb-10">
          <el-button type="primary" @click="handleUpload()">
            <i-ep-upload />
            上传文件
          </el-button>
          <el-button :disabled="multiple" @click="handleMove">
            <i-ep-right />
            转移分类
          </el-button>
          <el-button :disabled="multiple" type="danger" @click="handleBatchDelete">
            <i-ep-delete />
            批量删除
          </el-button>
        </div>

        <!-- 文件列表 -->
        <el-table
          v-loading="loading"
          :data="fileList"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column label="文件" min-width="200">
            <template #default="{ row }">
              <div class="file-item">
                <el-image
                  v-if="row.fileType === 'image'"
                  :src="row.fileUrl || row.url"
                  :preview-src-list="[(row.fileUrl || row.url)]"
                  fit="contain"
                  class="file-thumb"
                />
                <div v-else class="file-icon">
                  <i-ep-document v-if="row.fileType === 'file'" />
                  <i-ep-video-camera v-else-if="row.fileType === 'video'" />
                  <i-ep-picture v-else />
                </div>
                <div class="file-info">
                  <div class="file-name" :title="row.name">{{ row.name }}</div>
                  <div class="file-original" v-if="row.originalName !== row.name">
                    {{ row.originalName }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="fileSize" label="大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="80">
            <template #default="{ row }">
              {{ row.fileType === "image" ? "图片" : row.fileType === "video" ? "视频" : "文件" }}
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="createTime" label="上传时间" width="160" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handlePreview(row)">
                <i-ep-view />
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">
                <i-ep-delete />
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <pagination
          v-if="total > 0"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="handleQuery"
        />
      </div>
    </div>

    <!-- 上传弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文件"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="上传至">
          <el-tree-select
            v-model="uploadTargetCategoryId"
            :data="categoryTree"
            :props="defaultProps"
            check-strictly
            node-key="id"
            placeholder="默认上传到根目录"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :file-list="uploadFileList"
            :on-change="handleUploadChange"
            :on-remove="handleUploadRemove"
            :on-exceed="handleUploadExceed"
            multiple
            style="width: 100%"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="submitUpload">开始上传</el-button>
      </template>
    </el-dialog>

    <!-- 分类弹窗 -->
    <el-dialog
      v-model="categoryDialog.visible"
      :title="categoryDialog.title"
      width="500px"
    >
      <el-form :model="categoryForm" :rules="categoryRules" label-width="80px" class="category-form">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 转移分类弹窗 -->
    <el-dialog v-model="moveDialog.visible" title="转移分类" width="400px">
      <el-form>
        <el-form-item label="目标分类">
          <el-tree-select
            v-model="moveForm.categoryId"
            :data="categoryTree"
            :props="defaultProps"
            check-strictly
            node-key="id"
            placeholder="请选择目标分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitMoveCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="800px">
      <div class="preview-container">
        <el-image
          v-if="currentFile?.fileType === 'image'"
          :src="previewUrl"
          fit="contain"
          style="width: 100%; max-height: 600px"
        />
        <video
          v-else-if="currentFile?.fileType === 'video'"
          :src="previewUrl"
          controls
          style="width: 100%; max-height: 600px"
        />
        <div v-else class="file-preview-other">
          <i-ep-document style="font-size: 80px; color: #409eff" />
          <p>{{ currentFile?.name }}</p>
          <el-button type="primary" :href="previewUrl" target="_blank">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.file-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);

  .left-panel {
    width: 260px;
    background: #fff;
    border-radius: 4px;
    padding: 15px;
    flex-shrink: 0;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      font-weight: 600;
      font-size: 15px;
    }

    :deep(.el-tree) {
      .tree-node {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        padding-right: 5px;

        .node-label {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .node-actions {
          display: none;
          gap: 3px;
        }
      }

      .el-tree-node__content:hover .node-actions {
        display: flex;
      }
    }
  }

  .right-panel {
    flex: 1;
    background: #fff;
    border-radius: 4px;
    padding: 15px;
    overflow: auto;
  }
}

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;

  .file-thumb {
    width: 50px;
    height: 50px;
    border-radius: 4px;
    flex-shrink: 0;
  }

  .file-icon {
    width: 50px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 24px;
    color: #909399;
    flex-shrink: 0;
  }

  .file-info {
    overflow: hidden;

    .file-name {
      font-size: 13px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-original {
      font-size: 12px;
      color: #909399;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.file-preview-other {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 40px;
}
</style>
