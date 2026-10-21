<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keywords">
          <el-input
            v-model="queryParams.keywords"
            placeholder="搜索标题/简介"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select
            v-model="queryParams.categoryId"
            :data="treeSelectData"
            :render-after-expand="false"
            clearable
            placeholder="全部"
            style="width: 200px"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="状态" prop="isEnable">
          <el-select
            v-model="queryParams.isEnable"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"
            ><i-ep-search />搜索</el-button
          >
          <el-button @click="resetQuery"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文章列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 12px">
        <el-button
          v-hasPerm="['cms:article:add']"
          type="primary"
          @click="openDialog()"
          ><i-ep-plus />新增文章</el-button
        >
      </div>
      <el-table v-loading="loading" :data="list" border>
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column
          label="标题"
          prop="title"
          min-width="220"
          show-overflow-tooltip
        >
          <template #default="scope">
            <span
              v-if="scope.row.titleColor"
              :style="{ color: scope.row.titleColor }"
              >{{ scope.row.title }}</span
            >
            <span v-else>{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="分类"
          prop="categoryName"
          width="130"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column label="封面" width="90" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.cover"
              :src="scope.row.cover"
              :preview-src-list="[scope.row.cover]"
              preview-teleported
              fit="cover"
              style="width: 56px; height: 40px; border-radius: 4px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isEnable"
              :active-value="1"
              :inactive-value="0"
              @change="handleFieldChange(scope.row, 'isEnable')"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="访问量"
          prop="accessCount"
          width="80"
          align="center"
        />
        <el-table-column
          label="创建时间"
          prop="createTime"
          width="170"
          align="center"
        />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-hasPerm="['cms:article:edit']"
              type="primary"
              link
              size="small"
              @click="openDialog(scope.row.id)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              v-hasPerm="['cms:article:delete']"
              type="danger"
              link
              size="small"
              @click="handleDelete(scope.row.id)"
            >
              <i-ep-delete />删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="handleQuery"
      />
    </el-card>

    <!-- 文章编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="1000px"
      :close-on-click-modal="false"
      top="4vh"
      @close="closeDialog"
    >
      <el-form
        ref="dataFormRef"
        :model="formData"
        :rules="rules"
        label-width="90px"
      >
        <el-row :gutter="20">
          <el-col :span="14">
            <el-form-item label="标题" prop="title">
              <el-input
                v-model="formData.title"
                placeholder="请输入文章标题"
                maxlength="60"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="所属分类" prop="categoryId">
              <el-tree-select
                v-model="formData.categoryId"
                :data="treeSelectData"
                :render-after-expand="false"
                placeholder="请选择分类"
                style="width: 100%"
                check-strictly
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="简介">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="文章摘要,用于列表展示"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="14">
            <el-form-item label="封面">
              <SingleUpload
                v-model="formData.cover"
                :width="160"
                :height="100"
              />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="标题颜色">
              <el-color-picker v-model="formData.titleColor" />
              <el-button
                v-if="formData.titleColor"
                link
                type="primary"
                size="small"
                style="margin-left: 8px"
                @click="formData.titleColor = undefined"
              >
                清除
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图集">
          <MultiUpload v-model="imagesList" :limit="9" file-type="img" />
        </el-form-item>
        <el-form-item label="外链跳转">
          <el-input
            v-model="formData.jumpUrl"
            placeholder="选填;填写后详情页点击将跳转外部链接"
            clearable
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="formData.isEnable"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="正文">
          <div v-if="dialog.visible" style="width: 100%">
            <WangEditor
              v-model="formData.content"
              :key="formData.id ?? 'new'"
              style="border: 1px solid #ccc; min-height: 360px"
            />
          </div>
        </el-form-item>
        <el-collapse style="border: none">
          <el-collapse-item title="SEO 设置(选填)" name="seo">
            <el-form-item label="SEO标题">
              <el-input v-model="formData.seoTitle" maxlength="100" />
            </el-form-item>
            <el-form-item label="SEO关键字">
              <el-input v-model="formData.seoKeywords" maxlength="130" />
            </el-form-item>
            <el-form-item label="SEO描述">
              <el-input
                v-model="formData.seoDesc"
                type="textarea"
                :rows="2"
                maxlength="230"
              />
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitForm"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsArticle", inheritAttrs: false });

import {
  getArticlePage,
  getArticleDetail,
  addArticle,
  updateArticle,
  deleteArticle,
  getCategoryTree,
} from "@/api/cms/article";
import type {
  ArticleQuery,
  ArticlePageVO,
  ArticleDetailVO,
  ArticleForm,
  CategoryNode,
} from "@/api/cms/article/types";
import type { FormInstance } from "element-plus";
import WangEditor from "@/components/WangEditor/index.vue";
import SingleUpload from "@/components/Upload/SingleUpload.vue";
import MultiUpload from "@/components/Upload/MultiUpload.vue";

// 树形数据转 el-tree-select 需要的 {value,label,children} 结构
interface TreeSelectNode {
  value: number;
  label: string;
  children: TreeSelectNode[];
}
function toSelectTree(list: CategoryNode[]): TreeSelectNode[] {
  return (list || []).map((item) => ({
    value: item.id,
    label: item.name,
    children: toSelectTree(item.children || []),
  }));
}

const queryFormRef = ref<FormInstance>();
const dataFormRef = ref<FormInstance>();

const loading = ref(false);
const submitLoading = ref(false);
const total = ref(0);
const list = ref<ArticlePageVO[]>([]);
const categoryTree = ref<CategoryNode[]>([]);
const treeSelectData = computed(() => toSelectTree(categoryTree.value));

const queryParams = reactive<ArticleQuery>({
  pageNum: 1,
  pageSize: 10,
  isEnable: undefined,
});

const dialog = reactive({ visible: false, title: "" });
const formData = reactive<ArticleForm>({
  title: "",
  description: "",
  categoryId: undefined as unknown as number,
  titleColor: undefined,
  jumpUrl: "",
  isEnable: 1,
  content: "",
  cover: "",
  seoTitle: "",
  seoKeywords: "",
  seoDesc: "",
});
const imagesList = ref<string[]>([]);

const rules = reactive({
  title: [{ required: true, message: "请输入文章标题", trigger: "blur" }],
  categoryId: [
    { required: true, message: "请选择所属分类", trigger: "change" },
  ],
});

function loadCategoryTree() {
  getCategoryTree().then(({ data }) => {
    categoryTree.value = data;
  });
}

function handleQuery() {
  loading.value = true;
  getArticlePage(queryParams)
    .then(({ data }) => {
      list.value = data.list;
      total.value = data.total;
    })
    .finally(() => (loading.value = false));
}

function resetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.pageNum = 1;
  handleQuery();
}

function handleFieldChange(row: ArticlePageVO, field: "isEnable") {
  updateArticle(row.id, { [field]: row[field] } as unknown as ArticleForm).then(
    () => {
      ElMessage.success("更新成功");
    }
  );
}

function openDialog(id?: number) {
  resetForm();
  if (id) {
    dialog.title = "编辑文章";
    getArticleDetail(id).then(({ data }) => {
      Object.assign(formData, data, { id });
      imagesList.value = parseImages(data.images);
      dialog.visible = true;
    });
  } else {
    dialog.title = "新增文章";
    dialog.visible = true;
  }
}

/**
 * 解析图集为 url 字符串数组。
 * 与 MultiUpload 的 v-model 契约(string[] url)一致,并兼容历史脏数据:
 * 一维字符串数组、对象数组([{url}])、null 等非法项一律过滤。
 */
function parseImages(images?: string): string[] {
  if (!images) return [];
  try {
    const arr = JSON.parse(images);
    if (!Array.isArray(arr)) return [];
    return arr
      .map((item: unknown) =>
        typeof item === "string" ? item : (item as { url?: string })?.url
      )
      .filter(
        (url): url is string => typeof url === "string" && url.length > 0
      );
  } catch {
    return [];
  }
}

function closeDialog() {
  dialog.visible = false;
  resetForm();
}

function resetForm() {
  dataFormRef.value?.resetFields();
  Object.assign(formData, {
    id: undefined,
    title: "",
    description: "",
    categoryId: undefined,
    titleColor: undefined,
    jumpUrl: "",
    isEnable: 1,
    content: "",
    cover: "",
    seoTitle: "",
    seoKeywords: "",
    seoDesc: "",
  });
  imagesList.value = [];
}

function submitForm() {
  dataFormRef.value?.validate((valid) => {
    if (!valid) return;
    submitLoading.value = true;
    const payload: ArticleForm = {
      ...formData,
      images: imagesList.value.length
        ? JSON.stringify(imagesList.value)
        : "",
    };
    const api = formData.id
      ? updateArticle(formData.id, payload)
      : addArticle(payload);
    api
      .then(() => {
        ElMessage.success(formData.id ? "修改成功" : "新增成功");
        closeDialog();
        handleQuery();
      })
      .finally(() => (submitLoading.value = false));
  });
}

function handleDelete(id: number) {
  ElMessageBox.confirm("确认删除该文章？删除后前台不再展示。").then(() => {
    deleteArticle(id).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

onMounted(() => {
  handleQuery();
  loadCategoryTree();
});
</script>
