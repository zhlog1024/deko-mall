<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索问题标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="回复状态" prop="isReply">
          <el-select
            v-model="queryParams.isReply"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="未回复" :value="0" />
            <el-option label="已回复" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="展示状态" prop="isEnable">
          <el-select
            v-model="queryParams.isEnable"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="未展示" :value="0" />
            <el-option label="已展示" :value="1" />
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

    <!-- 问答列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 12px">
        <el-button
          v-hasPerm="['cms:ask:add']"
          type="primary"
          @click="openDialog()"
          ><i-ep-plus />新增问答</el-button
        >
        <el-button
          v-hasPerm="['cms:ask:delete']"
          type="danger"
          plain
          :disabled="ids.length === 0"
          @click="handleBatchDelete"
          ><i-ep-delete />批量删除</el-button
        >
        <span
          v-if="ids.length > 0"
          style="margin-left: 8px; color: #909399; font-size: 13px"
          >已选 {{ ids.length }} 条</span
        >
      </div>
      <el-table
        v-loading="loading"
        :data="list"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column
          label="商品"
          prop="spuName"
          min-width="160"
          show-overflow-tooltip
        >
          <template #default="scope">
            <span v-if="scope.row.spuName">{{ scope.row.spuName }}</span>
            <span v-else style="color: #c0c4cc">未指定商品</span>
          </template>
        </el-table-column>
        <el-table-column
          label="问题标题"
          prop="title"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          label="提问人"
          prop="askUser"
          width="100"
          align="center"
          show-overflow-tooltip
        >
          <template #default="scope">
            <span v-if="scope.row.askUser">{{ scope.row.askUser }}</span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="回复状态" width="90" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.isReply === 1 ? 'success' : 'info'"
              disable-transitions
            >
              {{ scope.row.isReply === 1 ? "已回复" : "未回复" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="展示" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isEnable"
              :active-value="1"
              :inactive-value="0"
              @change="(val: any) => handleEnableChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="浏览"
          prop="accessCount"
          width="70"
          align="center"
        />
        <el-table-column
          label="创建时间"
          prop="createTime"
          width="170"
          align="center"
        />
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-hasPerm="['cms:ask:reply']"
              type="success"
              link
              size="small"
              @click="openReplyDialog(scope.row)"
            >
              <i-ep-chat-dot-round />回复
            </el-button>
            <el-button
              v-hasPerm="['cms:ask:edit']"
              type="primary"
              link
              size="small"
              @click="openDialog(scope.row.id)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              v-hasPerm="['cms:ask:delete']"
              type="danger"
              link
              size="small"
              @click="handleDelete(scope.row)"
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

    <!-- 新增/编辑问答弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="720px"
      :close-on-click-modal="false"
      @close="closeDialog"
    >
      <el-form
        ref="dataFormRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="关联商品" prop="spuName">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input
              v-model="formData.spuName"
              readonly
              clearable
              placeholder="未选择(点击右侧选择商品)"
              style="flex: 1"
              @clear="clearSpu"
            />
            <el-button @click="openSpuPicker">选择商品</el-button>
          </div>
          <div
            v-if="formData.spuId"
            style="
              color: #909399;
              font-size: 12px;
              line-height: 20px;
              margin-top: 2px;
            "
          >
            商品ID:{{ formData.spuId }}
          </div>
        </el-form-item>
        <el-form-item label="问题标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入问题标题"
            maxlength="120"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="提问人">
          <el-input
            v-model="formData.askUser"
            placeholder="选填;运营代录时填写提问人昵称,用户端提问留空展示为匿名"
            maxlength="30"
            clearable
          />
        </el-form-item>
        <el-form-item label="问题详情">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="4"
            placeholder="选填;问题的补充说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="10">
            <el-form-item label="排序">
              <el-input-number
                v-model="formData.sort"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="启用展示">
              <el-switch
                v-model="formData.isEnable"
                :active-value="1"
                :inactive-value="0"
                active-text="展示"
                inactive-text="不展示"
              />
              <div
                style="
                  color: #909399;
                  font-size: 12px;
                  line-height: 18px;
                  margin-top: 2px;
                "
              >
                用户提问默认不展示,可回复(回复即展示)或仅启用
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm"
          >保存</el-button
        >
      </template>
    </el-dialog>

    <!-- 回复问答弹窗 -->
    <el-dialog
      v-model="replyDialog.visible"
      :title="replyDialog.title"
      width="640px"
      :close-on-click-modal="false"
      @close="closeReplyDialog"
    >
      <el-alert
        v-if="replyDialog.row && replyDialog.row.isEnable !== 1"
        type="warning"
        :closable="false"
        show-icon
        title="该提问当前未展示;回复后自动确认展示并置为已回复"
        style="margin-bottom: 12px"
      />
      <el-form
        ref="replyFormRef"
        :model="replyForm"
        :rules="replyRules"
        label-width="90px"
      >
        <el-form-item label="商品">
          <span v-if="replyDialog.row && replyDialog.row.spuName">{{
            replyDialog.row.spuName
          }}</span>
          <span v-else style="color: #c0c4cc">未指定商品</span>
        </el-form-item>
        <el-form-item label="问题">
          <span>{{ replyDialog.row && replyDialog.row.title }}</span>
        </el-form-item>
        <el-form-item label="回答内容" prop="answer">
          <el-input
            v-model="replyForm.answer"
            type="textarea"
            :rows="6"
            placeholder="请输入回答内容,保存后前台立即展示"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeReplyDialog">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply"
          >保存回复</el-button
        >
      </template>
    </el-dialog>

    <!-- 商品选择弹窗(通用组件,单选) -->
    <SelectContentList
      v-if="pickerVisible"
      :dialog-visible="pickerVisible"
      :multiple="false"
      :selected-ids="spuSelectedIds"
      @close="closeSpuPicker"
      @confirm="handleSpuConfirm"
    />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsAsk", inheritAttrs: false });

import {
  getAskPage,
  getAskDetail,
  addAsk,
  updateAsk,
  replyAsk,
  enableAsk,
  deleteAsks,
} from "@/api/cms/ask";
import type {
  AskPageQuery,
  AskPageVO,
  AskDetailVO,
  AskForm,
} from "@/api/cms/ask";
import type { FormInstance } from "element-plus";
import SelectContentList from "@/components/SelectContentList/index.vue";

/** 商品选择组件回传行(rows 含 id/name/picUrl 等) */
interface SpuPickRow {
  id: number;
  name: string;
  picUrl?: string;
  [key: string]: unknown;
}

const queryFormRef = ref<FormInstance>();
const dataFormRef = ref<FormInstance>();
const replyFormRef = ref<FormInstance>();

const loading = ref(false);
const submitLoading = ref(false);
const replyLoading = ref(false);
const total = ref(0);
const list = ref<AskPageVO[]>([]);
const ids = ref<number[]>([]);

const queryParams = reactive<AskPageQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  isReply: undefined,
  isEnable: undefined,
});

const dialog = reactive({ visible: false, title: "" });
const formData = reactive<AskForm>({
  id: undefined,
  spuId: undefined,
  spuName: "",
  spuPic: "",
  title: "",
  content: "",
  askUser: "",
  sort: 0,
  isEnable: 1,
});

const replyDialog = reactive<{
  visible: boolean;
  title: string;
  row: AskPageVO | null;
}>({ visible: false, title: "", row: null });
const replyForm = reactive<{ answer: string }>({ answer: "" });

const rules = reactive({
  title: [{ required: true, message: "请输入问题标题", trigger: "blur" }],
});

const replyRules = reactive({
  answer: [{ required: true, message: "请输入回答内容", trigger: "blur" }],
});

/** 商品选择弹窗 */
const pickerVisible = ref(false);
const spuSelectedIds = computed(() =>
  formData.spuId && formData.spuId > 0 ? [formData.spuId] : []
);

function handleQuery() {
  loading.value = true;
  getAskPage(queryParams)
    .then(({ data }) => {
      list.value = data.list as AskPageVO[];
      total.value = data.total;
    })
    .finally(() => (loading.value = false));
}

function resetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.pageNum = 1;
  handleQuery();
}

function handleSelectionChange(selection: AskPageVO[]) {
  ids.value = selection.map((item) => item.id);
}

/** 展示开关(用户提问 0→1 即"确认展示",不回复也展示) */
function handleEnableChange(row: AskPageVO, val: number | string | boolean) {
  const next = Number(val) === 1 ? 1 : 0;
  enableAsk(row.id, { isEnable: next })
    .then(() => {
      ElMessage.success(next === 1 ? "已启用,前台展示该提问" : "已停用");
    })
    .catch(() => handleQuery());
}

// 新增/编辑

function openDialog(id?: number) {
  resetForm();
  if (id) {
    dialog.title = "编辑问答";
    getAskDetail(id).then(({ data }) => {
      const detail = data as AskDetailVO;
      formData.id = detail.id;
      formData.spuId =
        detail.spuId && detail.spuId > 0 ? detail.spuId : undefined;
      formData.spuName = detail.spuName || "";
      formData.spuPic = detail.spuPic || "";
      formData.title = detail.title || "";
      formData.content = detail.content || "";
      formData.askUser = detail.askUser || "";
      formData.sort = detail.sort ?? 0;
      formData.isEnable = detail.isEnable === 1 ? 1 : 0;
      dialog.visible = true;
    });
  } else {
    dialog.title = "新增问答";
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
    spuId: undefined,
    spuName: "",
    spuPic: "",
    title: "",
    content: "",
    askUser: "",
    sort: 0,
    isEnable: 1,
  });
}

function submitForm() {
  dataFormRef.value?.validate((valid) => {
    if (!valid) return;
    submitLoading.value = true;
    const payload: AskForm = {
      id: formData.id,
      spuId: formData.spuId && formData.spuId > 0 ? formData.spuId : 0,
      spuName: formData.spuName?.trim() || "",
      spuPic: formData.spuPic || "",
      title: (formData.title || "").trim(),
      content: (formData.content || "").trim(),
      askUser: (formData.askUser || "").trim(),
      sort: formData.sort ?? 0,
      isEnable: formData.isEnable === 1 ? 1 : 0,
    };
    const api = payload.id ? updateAsk(payload.id, payload) : addAsk(payload);
    api
      .then(() => {
        ElMessage.success(payload.id ? "修改成功" : "新增成功");
        closeDialog();
        handleQuery();
      })
      .finally(() => (submitLoading.value = false));
  });
}

// 商品选择

function openSpuPicker() {
  pickerVisible.value = true;
}

function closeSpuPicker() {
  pickerVisible.value = false;
}

function handleSpuConfirm(rows: SpuPickRow[], payload: { ids: number[] }) {
  const row = rows[0];
  const pickedId = row?.id ?? payload.ids[0];
  if (pickedId !== undefined) {
    formData.spuId = pickedId;
    if (row) {
      formData.spuName = row.name;
      formData.spuPic = row.picUrl || "";
    } else {
      // 单选未重选(保持原选择):保留原 spuName 快照即可
      formData.spuId = pickedId;
    }
  }
  closeSpuPicker();
}

function clearSpu() {
  formData.spuId = undefined;
  formData.spuName = "";
  formData.spuPic = "";
}

// 回复

function openReplyDialog(row: AskPageVO) {
  replyDialog.row = row;
  replyDialog.title = row.isReply === 1 ? "编辑回复" : "回复问答";
  replyForm.answer = "";
  replyLoading.value = true;
  replyDialog.visible = true;
  // 已回复过的问答预填原回答,便于修改
  if (row.isReply === 1) {
    getAskDetail(row.id)
      .then(({ data }) => {
        replyForm.answer = (data as AskDetailVO).answer || "";
      })
      .finally(() => (replyLoading.value = false));
  } else {
    replyLoading.value = false;
  }
}

function closeReplyDialog() {
  replyDialog.visible = false;
  replyDialog.row = null;
  replyForm.answer = "";
  replyFormRef.value?.clearValidate();
}

function submitReply() {
  const row = replyDialog.row;
  if (!row) return;
  replyFormRef.value?.validate((valid) => {
    if (!valid) return;
    replyLoading.value = true;
    replyAsk(row.id, { answer: (replyForm.answer || "").trim() })
      .then(() => {
        ElMessage.success("回复成功,该提问已确认展示");
        closeReplyDialog();
        handleQuery();
      })
      .finally(() => (replyLoading.value = false));
  });
}

// 删除

function handleDelete(row: AskPageVO) {
  ElMessageBox.confirm(
    `确认删除该问答「${row.title}」？删除后前台不再展示。`
  ).then(() => {
    deleteAsks([row.id]).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

function handleBatchDelete() {
  if (ids.value.length === 0) return;
  ElMessageBox.confirm(
    `确认删除选中的 ${ids.value.length} 条问答？删除后前台不再展示。`
  ).then(() => {
    deleteAsks(ids.value).then(() => {
      ElMessage.success("批量删除成功");
      handleQuery();
    });
  });
}

onMounted(() => {
  handleQuery();
});
</script>
