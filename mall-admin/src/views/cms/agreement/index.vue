<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="协议标识" prop="document">
          <el-select
            v-model="queryParams.document"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option label="用户注册协议" value="userregister" />
            <el-option label="隐私政策" value="userprivacy" />
            <el-option label="注销协议" value="userlogout" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字" prop="keywords">
          <el-input
            v-model="queryParams.keywords"
            placeholder="搜索标题"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"
            ><i-ep-search />搜索</el-button
          >
          <el-button @click="resetQuery"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <el-button
          v-hasPerm="['cms:agreement:add']"
          type="success"
          @click="openDialog()"
        >
          <i-ep-plus />新增
        </el-button>
      </template>
      <el-table
        v-loading="loading"
        :data="list"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column
          label="协议标识"
          prop="document"
          width="150"
          align="center"
        >
          <template #default="scope">
            <el-tag>{{ documentLabel(scope.row.document) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="协议标题"
          prop="title"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="更新时间"
          prop="updateTime"
          width="180"
          align="center"
        />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-hasPerm="['cms:agreement:edit']"
              type="primary"
              link
              size="small"
              @click="openDialog(scope.row.id)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              v-hasPerm="['cms:agreement:delete']"
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

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="900px"
      :close-on-click-modal="false"
      @close="closeDialog"
    >
      <el-form
        ref="dataFormRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="协议标识" prop="document">
          <el-select
            v-model="formData.document"
            placeholder="请选择"
            :disabled="!!formData.id"
          >
            <el-option label="用户注册协议" value="userregister" />
            <el-option label="隐私政策" value="userprivacy" />
            <el-option label="注销协议" value="userlogout" />
          </el-select>
        </el-form-item>
        <el-form-item label="协议标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入协议标题" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="协议内容" prop="content">
          <div v-if="dialog.visible" style="width: 100%">
            <WangEditor
              v-model="formData.content"
              :key="formData.id"
              style="border: 1px solid #ccc; min-height: 400px"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsAgreement", inheritAttrs: false });

import {
  getAgreementPage,
  getAgreementDetail,
  addAgreement,
  updateAgreement,
  deleteAgreement,
} from "@/api/cms/agreement";
import type {
  AgreementPageVO,
  AgreementQuery,
  AgreementForm,
} from "@/api/cms/agreement/types";
import type { FormInstance } from "element-plus";
import WangEditor from "@/components/WangEditor/index.vue";

const queryFormRef = ref<FormInstance>();
const dataFormRef = ref<FormInstance>();

const loading = ref(false);
const submitLoading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<AgreementQuery>({ pageNum: 1, pageSize: 10 });
const list = ref<AgreementPageVO[]>();

const dialog = reactive({ visible: false, title: "" });
const formData = reactive<AgreementForm & { id?: number }>({
  document: "userregister",
  title: "",
  content: "",
  status: 1,
});

const rules = reactive({
  document: [{ required: true, message: "请选择协议标识", trigger: "change" }],
  title: [{ required: true, message: "请输入协议标题", trigger: "blur" }],
});

function documentLabel(doc: string) {
  const map: Record<string, string> = {
    userregister: "注册协议",
    userprivacy: "隐私政策",
    userlogout: "注销协议",
  };
  return map[doc] || doc;
}

function handleQuery() {
  loading.value = true;
  getAgreementPage(queryParams)
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

function handleSelectionChange(selection: AgreementPageVO[]) {
  ids.value = selection.map((item) => item.id);
}

function handleStatusChange(row: AgreementPageVO) {
  updateAgreement(row.id, {
    document: row.document,
    title: row.title,
    content: row.content,
    status: row.status,
  });
}

function openDialog(id?: number) {
  resetForm();
  if (id) {
    dialog.title = "修改协议";
    getAgreementDetail(id).then(({ data }) => {
      formData.id = data.id;
      formData.document = data.document;
      formData.title = data.title;
      formData.content = data.content;
      formData.status = data.status;
      dialog.visible = true;
    });
  } else {
    dialog.title = "新增协议";
    dialog.visible = true;
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
    document: "userregister",
    title: "",
    content: "",
    status: 1,
  });
}

function submitForm() {
  dataFormRef.value?.validate((valid) => {
    if (!valid) return;
    submitLoading.value = true;
    const api = formData.id
      ? updateAgreement(formData.id, { ...formData })
      : addAgreement({ ...formData });
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
  ElMessageBox.confirm("确认删除该协议？").then(() => {
    deleteAgreement(id).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

onMounted(() => {
  handleQuery();
});
</script>
