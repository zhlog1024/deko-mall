<script setup lang="ts">
defineOptions({
  name: "DiyPageList",
  inheritAttrs: false,
});

import {
  getDiyPageList,
  deleteDiyPage,
  copyDiyPage,
  updateDiyPageEnabled,
  getDiyHomeMode,
  saveDiyHomeMode,
} from "@/api/cms/diyPage";
import type {
  DiyPageItem,
  DiyPagePageQuery,
  DiyHomeModeConfig,
} from "@/api/cms/diyPage";
import { useRouter } from "vue-router";

const router = useRouter();
const queryFormRef = ref(ElForm);

const loading = ref(false);
const ids = ref<number[]>([]);
const pageData = ref<DiyPageItem[]>([]);
const total = ref(0);

const queryParams = reactive<DiyPagePageQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  isEnabled: undefined,
});

/**
 * 查询
 */
function handleQuery() {
  loading.value = true;
  getDiyPageList(queryParams)
    .then(({ data }) => {
      pageData.value = data.list;
      total.value = data.total;
    })
    .finally(() => {
      loading.value = false;
    });
}

/**
 * 重置查询
 */
function resetQuery() {
  queryFormRef.value.resetFields();
  queryParams.pageNum = 1;
  handleQuery();
}

/**
 * 行 checkbox change 事件
 */
function handleSelectionChange(selection: DiyPageItem[]) {
  ids.value = selection.map((item) => item.id);
}

/**
 * 新建页面
 */
function handleCreate() {
  const url = router.resolve({ path: "/cms/diy-design" });
  window.open(url.href, "_blank");
}

/**
 * 打开底部导航装修（新 tab 全屏展示）
 * 挂在内容管理下：/cms/diy-tabbar
 */
function handleTabbar() {
  const url = router.resolve({ path: "/cms/diy-tabbar" });
  window.open(url.href, "_blank");
}

/**
 * 打开装修页面（新 tab 全屏展示）
 */
function handleEdit(row: DiyPageItem) {
  const url = router.resolve({
    path: "/cms/diy-design",
    query: { diy_id: String(row.id) },
  });
  window.open(url.href, "_blank");
}

/**
 * 复制页面并跳转到新页面编辑
 */
function handleCopy(row: DiyPageItem) {
  copyDiyPage(row.id).then(({ data, code }) => {
    if (code === "00000" && data) {
      ElMessage.success("复制成功");
      const url = router.resolve({
        path: "/cms/diy-design",
        query: { diy_id: String(data) },
      });
      window.open(url.href, "_blank");
    }
  });
}

/**
 * 删除
 */
function handleDelete(row?: DiyPageItem) {
  const diyIds = row ? [row.id] : ids.value;
  if (diyIds.length === 0) {
    ElMessage.warning("请勾选删除项");
    return;
  }

  ElMessageBox.confirm(
    `确认删除已选中的 ${diyIds.length} 个页面？删除后不可恢复。`,
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(() => {
    loading.value = true;
    Promise.all(diyIds.map((id) => deleteDiyPage(id)))
      .then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      })
      .finally(() => {
        loading.value = false;
      });
  });
}

/**
 * 切换启用状态
 */
function toggleEnabled(row: DiyPageItem) {
  const text = row.isEnabled ? "禁用" : "启用";
  ElMessageBox.confirm(`确认要${text}页面"${row.name}"吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    loading.value = true;
    try {
      await updateDiyPageEnabled(row.id, !row.isEnabled);
      ElMessage.success(`${text}成功`);
      handleQuery();
    } finally {
      loading.value = false;
    }
  });
}

// ========== 首页配置（按端 diy_id + 定时，对齐源工程 common_app_home_data_diy_mode） ==========
const homeModeVisible = ref(false);
const homeModeSaving = ref(false);
const homeModeLoading = ref(false);
const pageOptions = ref<DiyPageItem[]>([]);
// 端配置表单：{ weixin: {diyId, timeStart, timeEnd}, app: {...}, h5: {...} }
const CLIENT_TYPE_LABELS: Record<string, string> = {
  weixin: "微信小程序",
  app: "APP",
  h5: "H5",
};
const homeModeForm = reactive<DiyHomeModeConfig>({
  weixin: { diyId: null, timeStart: null, timeEnd: null },
  app: { diyId: null, timeStart: null, timeEnd: null },
  h5: { diyId: null, timeStart: null, timeEnd: null },
});

/**
 * 打开首页配置弹窗：加载 DIY 页面列表 + 当前配置
 */
function handleHomeMode() {
  homeModeVisible.value = true;
  homeModeLoading.value = true;
  // 首页配置下拉仅列出已启用页面（对齐源工程 home-diy：is_enable=1），
  // 避免选中停用页导致 /home 因启用锁定返回 null 而首页空白
  getDiyPageList({ pageNum: 1, pageSize: 200, keyword: "", isEnabled: true })
    .then(({ data }) => {
      pageOptions.value = data.list || [];
    })
    .finally(() => {});
  getDiyHomeMode()
    .then(({ data }) => {
      if (data) {
        ["weixin", "app", "h5"].forEach((key) => {
          const item = (data as any)[key];
          homeModeForm[key] = {
            diyId: item?.diyId ?? item?.diy_id ?? null,
            timeStart: item?.timeStart ?? item?.time_start ?? null,
            timeEnd: item?.timeEnd ?? item?.time_end ?? null,
          };
        });
      }
    })
    .catch(() => {
      // 读取失败保留默认空表单
    })
    .finally(() => {
      homeModeLoading.value = false;
    });
}

/**
 * 保存首页配置
 */
function handleHomeModeSave() {
  // 构建后端契约：snake_case + 仅提交有 diy_id 的端
  const payload: Record<string, any> = {};
  ["weixin", "app", "h5"].forEach((key) => {
    const item = homeModeForm[key];
    if (item.diyId) {
      payload[key] = {
        diy_id: Number(item.diyId),
        time_start: item.timeStart || null,
        time_end: item.timeEnd || null,
      };
    }
  });
  if (Object.keys(payload).length === 0) {
    ElMessage.warning("请至少为一个小程序/APP/H5 端指定首页页面");
    return;
  }
  homeModeSaving.value = true;
  saveDiyHomeMode(payload as DiyHomeModeConfig)
    .then(() => {
      ElMessage.success("首页配置保存成功");
      homeModeVisible.value = false;
      handleQuery();
    })
    .finally(() => {
      homeModeSaving.value = false;
    });
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="页面名称" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入页面名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="isEnabled">
          <el-select
            v-model="queryParams.isEnabled"
            placeholder="请选择启用状态"
            clearable
            style="width: 160px"
          >
            <el-option :value="true" label="已启用" />
            <el-option :value="false" label="未启用" />
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
    </div>

    <!-- 卡片区域 -->
    <el-card shadow="never">
      <template #header>
        <el-button type="primary" @click="handleCreate">
          <i-ep-plus />
          新建页面
        </el-button>
        <el-button type="warning" plain @click="handleHomeMode">
          <i-ep-set-up />
          首页配置
        </el-button>
        <el-button type="success" plain @click="handleTabbar">
          <i-ep-menu />
          底部导航
        </el-button>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleDelete()"
        >
          <i-ep-delete />
          删除
        </el-button>
      </template>

      <el-table
        v-loading="loading"
        :data="pageData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="封面图" width="100" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverUrl"
              :src="scope.row.coverUrl"
              fit="cover"
              :preview-src-list="[scope.row.coverUrl]"
              preview-teleported
              style="width: 60px; height: 60px; border-radius: 4px"
            />
            <span v-else class="no-cover">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column label="首页" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isHome" type="success" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isEnabled" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="scope">
            <el-button
              v-if="scope.row.isEnabled"
              type="danger"
              link
              @click.stop="toggleEnabled(scope.row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              @click.stop="toggleEnabled(scope.row)"
            >
              启用
            </el-button>
            <el-button type="primary" link @click.stop="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="primary" link @click.stop="handleCopy(scope.row)">
              复制
            </el-button>
            <el-button type="danger" link @click.stop="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="handleQuery"
      />
    </el-card>

    <!-- 首页配置弹窗（按端 diy_id + 定时，对齐源工程 common_app_home_data_diy_mode） -->
    <el-dialog
      v-model="homeModeVisible"
      title="首页配置"
      width="680px"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-loading="homeModeLoading">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="分别为微信小程序 / APP / H5 指定首页装修页面；可配置生效起止时间（留空表示长期生效）。"
          style="margin-bottom: 16px"
        />
        <el-form label-width="110px" v-for="(label, key) in CLIENT_TYPE_LABELS" :key="key">
          <el-form-item :label="label + '首页'">
            <el-select
              v-model="homeModeForm[key].diyId"
              placeholder="选择首页装修页面"
              clearable
              filterable
              style="width: 240px"
            >
              <el-option
                v-for="opt in pageOptions"
                :key="opt.id"
                :label="opt.name + (opt.isEnabled ? '' : '（停用）')"
                :value="opt.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="生效时间">
            <el-date-picker
              v-model="homeModeForm[key].timeStart"
              type="datetime"
              placeholder="开始时间（可空）"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 220px; margin-right: 8px"
            />
            <span style="margin: 0 4px; color: #999">至</span>
            <el-date-picker
              v-model="homeModeForm[key].timeEnd"
              type="datetime"
              placeholder="结束时间（可空）"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 220px; margin-left: 8px"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="homeModeVisible = false">取 消</el-button>
        <el-button type="primary" :loading="homeModeSaving" @click="handleHomeModeSave">
          保 存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.no-cover {
  color: #999;
  font-size: 12px;
}
</style>
