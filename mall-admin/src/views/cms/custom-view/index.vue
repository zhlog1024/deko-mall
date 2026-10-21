<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keywords">
          <el-input
            v-model="queryParams.keywords"
            placeholder="搜索页面名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
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

    <!-- 自定义页面列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 12px">
        <el-button type="primary" @click="openEditor()"
          ><i-ep-plus />新增</el-button
        >
        <span style="margin-left: 12px; color: #999; font-size: 12px"
          >支持 HTML 编辑与实时预览；CSS/JS 仅在 Web 端生效</span
        >
      </div>
      <el-table v-loading="loading" :data="list" border>
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column
          label="名称"
          prop="name"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="name-cell">
              <el-image
                v-if="scope.row.logo"
                :src="scope.row.logo"
                :preview-src-list="[scope.row.logo]"
                preview-teleported
                fit="cover"
                class="logo-img"
              />
              <span v-else class="logo-img logo-img--empty">-</span>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isEnable"
              :active-value="1"
              :inactive-value="0"
              @change="handleEnableChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="头部" width="70" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isHeader === 1" type="success" size="small"
              >是</el-tag
            >
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="尾部" width="70" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isFooter === 1" type="success" size="small"
              >是</el-tag
            >
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="满屏" width="70" align="center">
          <template #default="scope">
            <el-tag
              v-if="scope.row.isFullScreen === 1"
              type="success"
              size="small"
              >是</el-tag
            >
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="访问量"
          prop="accessCount"
          width="80"
          align="center"
        />
        <el-table-column label="SEO标题" width="150" align="center">
          <template #default="scope">
            <span>{{ scope.row.seoTitle || "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          prop="createTime"
          width="170"
          align="center"
        />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              @click="openEditor(scope.row.id)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
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
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsCustomView", inheritAttrs: false });

import { useRouter } from "vue-router";
import {
  getCustomViewPage,
  updateCustomView,
  deleteCustomView,
} from "@/api/cms/custom-view";
import type {
  CustomViewQuery,
  CustomViewPageVO,
} from "@/api/cms/custom-view/types";
import type { FormInstance } from "element-plus";

const router = useRouter();

const queryFormRef = ref<FormInstance>();

const loading = ref(false);
const total = ref(0);
const list = ref<CustomViewPageVO[]>([]);

const queryParams = reactive<CustomViewQuery>({
  pageNum: 1,
  pageSize: 10,
  isEnable: undefined,
});

function handleQuery() {
  loading.value = true;
  getCustomViewPage(queryParams)
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

/** 行内启用开关:局部 PUT 只传 {isEnable},避免覆盖代码字段 */
function handleEnableChange(row: CustomViewPageVO) {
  updateCustomView(row.id, { isEnable: row.isEnable }).then(() => {
    ElMessage.success("更新成功");
  });
}

/** 在新标签页编辑；弹窗被拦截时使用当前页面。 */
function openEditor(id?: number) {
  const url = router.resolve({
    name: "CmsCustomViewEdit",
    query: id ? { id: String(id) } : {},
  });
  const opened = window.open(url.href, "_blank");
  if (!opened) {
    router.push({
      name: "CmsCustomViewEdit",
      query: id ? { id: String(id) } : {},
    });
  }
}

function handleDelete(id: number) {
  ElMessageBox.confirm("确认删除该自定义页面？删除后前台不再展示。").then(
    () => {
      deleteCustomView(id).then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      });
    }
  );
}

// 编辑页在新标签保存并关闭后,切回本列表标签时自动刷新
function onWindowFocus() {
  handleQuery();
}

onMounted(() => {
  handleQuery();
  window.addEventListener("focus", onWindowFocus);
});

onBeforeUnmount(() => {
  window.removeEventListener("focus", onWindowFocus);
});
</script>

<style scoped lang="scss">
.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .logo-img {
    width: 36px;
    height: 36px;
    border-radius: 4px;
    flex-shrink: 0;

    &--empty {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #c0c4cc;
      font-size: 14px;
    }
  }
}
</style>
