<template>
  <div class="custom-view-editor">
    <!-- 顶部操作栏 -->
    <div class="editor-nav">
      <el-button text @click="handleBack"> <i-ep-arrow-left />返回 </el-button>
      <span class="nav-title">{{ isEdit ? "编辑" : "新增" }}自定义页面</span>
      <span class="nav-tip"
        >整页 HTML 由 app 端 mp-html 渲染,CSS/JS 仅 web 端生效</span
      >
      <div class="nav-actions">
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          <i-ep-check />保存
        </el-button>
      </div>
    </div>

    <div class="editor-body">
      <!-- 页面内容 -->
      <div class="editor-main">
        <!-- 基础信息条 -->
        <div class="base-bar">
          <div class="base-field">
            <span class="base-label required">页面名称</span>
            <el-input
              v-model="formData.name"
              placeholder="请输入页面名称"
              maxlength="60"
              style="width: 240px"
            />
          </div>
          <div class="base-field">
            <span class="base-label">启用</span>
            <el-switch
              v-model="formData.isEnable"
              :active-value="1"
              :inactive-value="0"
            />
          </div>
          <div class="base-field">
            <span class="base-label">包含头部</span>
            <el-switch
              v-model="formData.isHeader"
              :active-value="1"
              :inactive-value="0"
            />
          </div>
          <div class="base-field">
            <span class="base-label">包含尾部</span>
            <el-switch
              v-model="formData.isFooter"
              :active-value="1"
              :inactive-value="0"
            />
          </div>
          <div class="base-field">
            <span class="base-label">满屏展示</span>
            <el-switch
              v-model="formData.isFullScreen"
              :active-value="1"
              :inactive-value="0"
            />
          </div>
        </div>

        <!-- 代码编辑器(Ace 三栏 + 实时预览) -->
        <div class="editor-panel">
          <page-code-editor
            v-model:html-content="formData.htmlContent"
            v-model:css-content="formData.cssContent"
            v-model:js-content="formData.jsContent"
          />
        </div>
      </div>

      <!-- 页面标识与 SEO -->
      <div class="editor-side">
        <el-card shadow="never" class="side-card">
          <template #header>
            <span class="side-title">页面 logo</span>
          </template>
          <SingleUpload v-model="formData.logo" :width="160" :height="100" />
        </el-card>

        <el-card shadow="never" class="side-card">
          <template #header>
            <span class="side-title">SEO 设置(选填)</span>
          </template>
          <el-form label-position="top" class="seo-form">
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
                :rows="4"
                maxlength="230"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsCustomViewEdit", inheritAttrs: false });

import { useRoute, useRouter } from "vue-router";
import {
  getCustomViewDetail,
  addCustomView,
  updateCustomView,
} from "@/api/cms/custom-view";
import type { CustomViewForm } from "@/api/cms/custom-view/types";
import SingleUpload from "@/components/Upload/SingleUpload.vue";
import PageCodeEditor from "./components/page-code-editor.vue";

const route = useRoute();
const router = useRouter();

const id = computed(() => Number(route.query.id) || 0);
const isEdit = computed(() => id.value > 0);

const submitLoading = ref(false);

// 新页面的编辑占位内容。
const PLACEHOLDER: Record<string, string> = {
  html: "<!-- html -->",
  css: "/* css */",
  js: "/* javascript */",
};

const formData = reactive<CustomViewForm>({
  name: "",
  logo: "",
  htmlContent: "",
  cssContent: "",
  jsContent: "",
  isEnable: 1,
  isHeader: 1,
  isFooter: 1,
  isFullScreen: 0,
  seoTitle: "",
  seoKeywords: "",
  seoDesc: "",
});

onMounted(() => {
  if (isEdit.value) {
    loadDetail();
  } else {
    // 新增:预置
    formData.htmlContent = PLACEHOLDER.html;
    formData.cssContent = PLACEHOLDER.css;
    formData.jsContent = PLACEHOLDER.js;
  }
});

function loadDetail() {
  getCustomViewDetail(id.value)
    .then(({ data }) => {
      if (!data) return;
      const detail = data as CustomViewForm;
      Object.assign(formData, {
        name: detail.name ?? "",
        logo: detail.logo ?? "",
        htmlContent: detail.htmlContent ?? "",
        cssContent: detail.cssContent ?? "",
        jsContent: detail.jsContent ?? "",
        isEnable: detail.isEnable ?? 1,
        isHeader: detail.isHeader ?? 1,
        isFooter: detail.isFooter ?? 1,
        isFullScreen: detail.isFullScreen ?? 0,
        seoTitle: detail.seoTitle ?? "",
        seoKeywords: detail.seoKeywords ?? "",
        seoDesc: detail.seoDesc ?? "",
      });
    })
    .catch(() => {
      ElMessage.error("页面数据加载失败,请返回列表重试");
    });
}

/** 剔除占位注释,避免纯占位内容入库 */
function cleanCode(value: string, placeholder: string) {
  return value.trim() === placeholder.trim() ? "" : value;
}

function submitForm() {
  if (!formData.name.trim()) {
    ElMessage.warning("请输入页面名称");
    return;
  }
  submitLoading.value = true;
  const payload: CustomViewForm = {
    ...formData,
    htmlContent: cleanCode(formData.htmlContent ?? "", PLACEHOLDER.html),
    cssContent: cleanCode(formData.cssContent ?? "", PLACEHOLDER.css),
    jsContent: cleanCode(formData.jsContent ?? "", PLACEHOLDER.js),
  };
  const api = isEdit.value
    ? updateCustomView(id.value, payload)
    : addCustomView(payload);
  api
    .then(() => {
      ElMessage.success(isEdit.value ? "保存成功" : "新增成功");
      if (isEdit.value) {
        // 编辑后保留当前页面。
        return;
      }
      // 新增:返回列表
      handleBack();
    })
    .finally(() => {
      submitLoading.value = false;
    });
}

function handleBack() {
  // 由列表新标签打开时直接关闭当前页签
  if (window.opener) {
    window.close();
    return;
  }
  router.back();
}
</script>

<style scoped lang="scss">
.custom-view-editor {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  min-height: 620px;
  box-sizing: border-box;
}

/* ---------- 顶部操作栏 ---------- */
.editor-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 4px 12px;

  .nav-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .nav-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .nav-actions {
    margin-left: auto;
  }
}

/* ---------- 主体 ---------- */
.editor-body {
  display: flex;
  flex: 1;
  gap: 12px;
  min-height: 0;
}

.editor-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

/* 基础信息条 */
.base-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20px;
  padding: 10px 14px;
  margin-bottom: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);

  .base-field {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .base-label {
    font-size: 13px;
    color: var(--el-text-color-regular);
    white-space: nowrap;

    &.required::before {
      content: "*";
      margin-right: 2px;
      color: var(--el-color-danger);
    }
  }
}

/* Ace 代码编辑器容器(HTML/CSS/JS + 实时预览) */
.editor-panel {
  flex: 1;
  min-height: 0;
}

/* ---------- 右侧栏 ---------- */
.editor-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 300px;
  overflow-y: auto;
  flex-shrink: 0;

  .side-card {
    flex-shrink: 0;

    :deep(.el-card__header) {
      padding: 10px 16px;
    }
  }

  .side-title {
    font-size: 14px;
    font-weight: 600;
  }
}

@media screen and (max-width: 1200px) {
  .editor-side {
    width: 260px;
  }
}
</style>
