<script setup lang="ts">
defineOptions({
  name: "AppVersion",
  inheritAttrs: false,
});

import {
  getAppVersionPage,
  getAppVersionDetail,
  addAppVersion,
  updateAppVersion,
  deleteAppVersions,
} from "@/api/system/appVersion";

import {
  AppVersionPageVO,
  AppVersionQuery,
  AppVersionForm,
} from "@/api/system/appVersion/types";

const queryFormRef = ref(ElForm);
const dataFormRef = ref(ElForm);

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<AppVersionQuery>({
  pageNum: 1,
  pageSize: 10,
});

const versionList = ref<AppVersionPageVO[]>();

const dialog = reactive<DialogType>({
  visible: false,
});

const formData = reactive<AppVersionForm>({
  versionName: "",
  versionCode: "",
  platform: "android",
  isForceUpdate: 0,
  isEnabled: 1,
});

const rules = reactive({
  versionName: [{ required: true, message: "请输入版本名称", trigger: "blur" }],
  versionCode: [{ required: true, message: "请输入版本号", trigger: "blur" }],
  platform: [{ required: true, message: "请选择平台", trigger: "change" }],
  isEnabled: [{ required: true, message: "请选择启用状态", trigger: "change" }],
});

function handleQuery() {
  loading.value = true;
  getAppVersionPage(queryParams)
    .then(({ data }) => {
      versionList.value = data.list;
      total.value = data.total;
    })
    .finally(() => {
      loading.value = false;
    });
}

function resetQuery() {
  queryFormRef.value.resetFields();
  queryParams.pageNum = 1;
  handleQuery();
}

function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.id);
}

function openDialog(id?: number) {
  dialog.visible = true;
  if (id) {
    dialog.title = "修改APP版本";
    getAppVersionDetail(id).then(({ data }) => {
      Object.assign(formData, data);
    });
  } else {
    dialog.title = "新增APP版本";
  }
}

function handleSubmit() {
  dataFormRef.value.validate((isValid: boolean) => {
    if (isValid) {
      loading.value = true;
      const versionId = formData.id;
      if (versionId) {
        updateAppVersion(versionId, formData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            handleQuery();
          })
          .finally(() => (loading.value = false));
      } else {
        addAppVersion(formData)
          .then(() => {
            ElMessage.success("新增成功");
            closeDialog();
            handleQuery();
          })
          .finally(() => (loading.value = false));
      }
    }
  });
}

function closeDialog() {
  dialog.visible = false;
  resetForm();
}

function resetForm() {
  dataFormRef.value.resetFields();
  dataFormRef.value.clearValidate();
  formData.id = undefined;
  formData.versionName = "";
  formData.versionCode = "";
  formData.platform = "android";
  formData.updateUrl = "";
  formData.updateContent = "";
  formData.isForceUpdate = 0;
  formData.isEnabled = 1;
}

function handleDelete(id?: number) {
  const deleteIds = [id || ids.value].join(",");
  if (!deleteIds) {
    ElMessage.warning("请勾选删除项");
    return;
  }
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteAppVersions(deleteIds).then(() => {
      ElMessage.success("删除成功");
      resetQuery();
    });
  });
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keywords">
          <el-input
            v-model="queryParams.keywords"
            placeholder="版本名称/版本号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="平台" prop="platform">
          <el-select v-model="queryParams.platform" placeholder="全部" clearable>
            <el-option label="Android" value="android" />
            <el-option label="iOS" value="ios" />
            <el-option label="小程序" value="mini_program" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery()">
            <i-ep-search />搜索
          </el-button>
          <el-button @click="resetQuery()"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <template #header>
        <el-button
          v-hasPerm="['sys:app_version:add']"
          type="success"
          @click="openDialog()"
        >
          <i-ep-plus />新增
        </el-button>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleDelete()"
        >
          <i-ep-delete />删除
        </el-button>
      </template>

      <el-table
        v-loading="loading"
        highlight-current-row
        :data="versionList"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="版本名称" prop="versionName" min-width="120" />
        <el-table-column label="版本号" prop="versionCode" width="120" />
        <el-table-column label="平台" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.platform === 'android'" type="success">Android</el-tag>
            <el-tag v-else-if="scope.row.platform === 'ios'" type="primary">iOS</el-tag>
            <el-tag v-else type="info">{{ scope.row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="强制更新" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isForceUpdate === 1" type="danger">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isEnabled === 1" type="success">启用</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="180" />
        <el-table-column fixed="right" label="操作" align="center" width="180">
          <template #default="scope">
            <el-button
              v-hasPerm="['sys:app_version:edit']"
              type="primary"
              link
              size="small"
              @click.stop="openDialog(scope.row.id)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              v-hasPerm="['sys:app_version:delete']"
              type="primary"
              link
              size="small"
              @click.stop="handleDelete(scope.row.id)"
            >
              <i-ep-delete />删除
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

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @close="closeDialog"
    >
      <el-form
        ref="dataFormRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="版本名称" prop="versionName">
          <el-input v-model="formData.versionName" placeholder="如 v2.0.0" />
        </el-form-item>
        <el-form-item label="版本号" prop="versionCode">
          <el-input v-model="formData.versionCode" placeholder="如 2.0.0" />
        </el-form-item>
        <el-form-item label="平台" prop="platform">
          <el-select v-model="formData.platform" placeholder="请选择平台">
            <el-option label="Android" value="android" />
            <el-option label="iOS" value="ios" />
            <el-option label="小程序" value="mini_program" />
          </el-select>
        </el-form-item>
        <el-form-item label="下载地址" prop="updateUrl">
          <el-input v-model="formData.updateUrl" placeholder="APK/IPA下载地址" />
        </el-form-item>
        <el-form-item label="更新内容" prop="updateContent">
          <el-input
            v-model="formData.updateContent"
            type="textarea"
            :rows="4"
            placeholder="JSON数组格式，如 [&quot;修复登录闪退问题&quot;,&quot;新增深色模式&quot;]"
          />
        </el-form-item>
        <el-form-item label="强制更新" prop="isForceUpdate">
          <el-radio-group v-model="formData.isForceUpdate">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="isEnabled">
          <el-radio-group v-model="formData.isEnabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
