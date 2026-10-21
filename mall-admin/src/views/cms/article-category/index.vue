<template>
  <div class="app-container">
    <el-card shadow="never">
      <div style="margin-bottom: 12px">
        <el-button
          v-hasPerm="['cms:category:add']"
          type="primary"
          @click="openCatDialog('top')"
          ><i-ep-plus />新增一级分类</el-button
        >
        <span style="margin-left: 12px; color: #999; font-size: 12px"
          >删除前需先清空子分类与该分类下文章</span
        >
      </div>
      <el-table
        v-loading="catLoading"
        :data="categoryTree"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column label="分类名称" prop="name" min-width="260" />
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isEnable"
              :active-value="1"
              :inactive-value="0"
              @change="handleCatEnableChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-hasPerm="['cms:category:add']"
              type="primary"
              link
              size="small"
              @click="openCatDialog('child', scope.row)"
            >
              <i-ep-plus />子分类
            </el-button>
            <el-button
              v-hasPerm="['cms:category:edit']"
              type="primary"
              link
              size="small"
              @click="openCatDialog('edit', scope.row)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              v-hasPerm="['cms:category:delete']"
              type="danger"
              link
              size="small"
              @click="handleCatDelete(scope.row)"
            >
              <i-ep-delete />删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类编辑弹窗 -->
    <el-dialog
      v-model="catDialog.visible"
      :title="catDialog.title"
      width="460px"
      :close-on-click-modal="false"
      @close="closeCatDialog"
    >
      <el-form
        ref="catFormRef"
        :model="catForm"
        :rules="catRules"
        label-width="90px"
      >
        <el-form-item label="父分类">
          <el-input :model-value="catForm.parentName" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="catForm.name"
            placeholder="请输入分类名称"
            maxlength="30"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="catForm.isEnable"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeCatDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="catSubmitLoading"
          @click="submitCatForm"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsArticleCategory", inheritAttrs: false });

import {
  getCategoryTree,
  addCategory,
  updateCategory,
  deleteCategory,
} from "@/api/cms/article";
import type { CategoryNode, CategoryForm } from "@/api/cms/article/types";
import type { FormInstance } from "element-plus";

const catFormRef = ref<FormInstance>();

const catLoading = ref(false);
const catSubmitLoading = ref(false);
const catDialog = reactive({ visible: false, title: "" });
const categoryTree = ref<CategoryNode[]>([]);
const catForm = reactive<CategoryForm & { parentName?: string }>({
  pid: 0,
  name: "",
  sort: 0,
  isEnable: 1,
  parentName: "顶级分类",
});

const catRules = reactive({
  name: [{ required: true, message: "请输入分类名称", trigger: "blur" }],
});

function loadCategoryTree() {
  catLoading.value = true;
  getCategoryTree()
    .then(({ data }) => {
      categoryTree.value = data;
    })
    .finally(() => (catLoading.value = false));
}

function catNameMap() {
  const map = new Map<number, string>();
  const walk = (list: CategoryNode[]) => {
    list.forEach((node) => {
      map.set(node.id, node.name);
      walk(node.children || []);
    });
  };
  walk(categoryTree.value);
  return map;
}

function openCatDialog(mode: "top" | "child" | "edit", row?: CategoryNode) {
  catFormRef.value?.resetFields();
  if (mode === "top") {
    catDialog.title = "新增一级分类";
    Object.assign(catForm, {
      id: undefined,
      pid: 0,
      name: "",
      sort: 0,
      isEnable: 1,
      parentName: "顶级分类",
    });
  } else if (mode === "child" && row) {
    catDialog.title = `新增子分类`;
    Object.assign(catForm, {
      id: undefined,
      pid: row.id,
      name: "",
      sort: 0,
      isEnable: 1,
      parentName: row.name,
    });
  } else if (mode === "edit" && row) {
    catDialog.title = "编辑分类";
    const names = catNameMap();
    Object.assign(catForm, {
      id: row.id,
      pid: row.pid,
      name: row.name,
      sort: row.sort ?? 0,
      isEnable: row.isEnable,
      parentName: row.pid === 0 ? "顶级分类" : names.get(row.pid) || "顶级分类",
    });
  }
  catDialog.visible = true;
}

function closeCatDialog() {
  catDialog.visible = false;
}

function submitCatForm() {
  catFormRef.value?.validate((valid) => {
    if (!valid) return;
    catSubmitLoading.value = true;
    const payload = {
      pid: catForm.pid,
      name: catForm.name,
      sort: catForm.sort ?? 0,
      isEnable: catForm.isEnable,
    };
    const api = catForm.id
      ? updateCategory(catForm.id, payload)
      : addCategory(payload);
    api
      .then(() => {
        ElMessage.success(catForm.id ? "修改成功" : "新增成功");
        closeCatDialog();
        loadCategoryTree();
      })
      .finally(() => (catSubmitLoading.value = false));
  });
}

function handleCatEnableChange(row: CategoryNode) {
  updateCategory(row.id, {
    pid: row.pid,
    name: row.name,
    sort: row.sort ?? 0,
    isEnable: row.isEnable,
  }).then(() => {
    ElMessage.success("更新成功");
  });
}

function handleCatDelete(row: CategoryNode) {
  ElMessageBox.confirm(
    `确认删除分类「${row.name}」？存在子分类或文章时将无法删除。`
  ).then(() => {
    deleteCategory(row.id).then(() => {
      ElMessage.success("删除成功");
      loadCategoryTree();
    });
  });
}

onMounted(() => {
  loadCategoryTree();
});
</script>
