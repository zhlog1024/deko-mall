<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="会员ID">
          <el-input
            v-model="queryParams.memberId"
            placeholder="请输入会员ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题关键字"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="消息类型" clearable style="width: 120px">
            <el-option label="系统通知" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="已读状态">
          <el-select v-model="queryParams.isRead" placeholder="已读状态" clearable style="width: 120px">
            <el-option label="未读" :value="0" />
            <el-option label="已读" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><i-ep-search />搜索</el-button>
          <el-button @click="resetQuery"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-header">
      <el-button type="primary" @click="openSendDialog"><i-ep-plus />发送消息</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="dataList"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="memberId" label="会员ID" width="100" align="center" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="info">系统通知</el-tag>
            <el-tag v-else>其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已读" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isRead === 1" type="success">已读</el-tag>
            <el-tag v-else type="warning">未读</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" fixed="right" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetailDialog(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentMessage.id }}</el-descriptions-item>
        <el-descriptions-item label="会员ID">{{ currentMessage.memberId }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentMessage.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="currentMessage.type === 1">系统通知</el-tag>
          <el-tag v-else>其他</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已读">
          <el-tag v-if="currentMessage.isRead === 1" type="success">已读</el-tag>
          <el-tag v-else type="warning">未读</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentMessage.createTime }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="message-detail" v-html="currentMessage.detail" />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发送消息弹窗 -->
    <el-dialog v-model="sendVisible" title="发送消息" width="500px" @close="resetSendForm">
      <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-width="80px">
        <el-form-item label="会员ID" prop="memberId">
          <el-input v-model="sendForm.memberId" placeholder="留空则发送给所有用户" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入消息标题" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="detail">
          <el-input
            v-model="sendForm.detail"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容（支持HTML）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendLoading" @click="handleSend">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "MessageManagement" });

import { getMessagePage, getMessageDetail, sendMessage, deleteMessage, batchDeleteMessages } from "@/api/ums/message";
import { MessageQuery, MessageVO } from "@/api/ums/message/types";
import { ElMessage, ElMessageBox, FormInstance } from "element-plus";

const loading = ref(false);
const dataList = ref<MessageVO[]>([]);
const total = ref(0);
const selectedIds = ref<number[]>([]);
const queryParams = reactive<MessageQuery>({
  pageNum: 1,
  pageSize: 10,
});

function handleQuery() {
  loading.value = true;
  getMessagePage(queryParams).then(({ data }) => {
    dataList.value = data.list;
    total.value = data.total;
    loading.value = false;
  });
}

function resetQuery() {
  queryParams.pageNum = 1;
  queryParams.memberId = undefined;
  queryParams.title = undefined;
  queryParams.type = undefined;
  queryParams.isRead = undefined;
  handleQuery();
}

function handleSelectionChange(selection: MessageVO[]) {
  selectedIds.value = selection.map((item) => item.id);
}

// 详情
const detailVisible = ref(false);
const currentMessage = ref<Partial<MessageVO>>({});
function openDetailDialog(row: MessageVO) {
  getMessageDetail(row.id).then(({ data }) => {
    currentMessage.value = data;
    detailVisible.value = true;
  });
}

// 删除
function handleDelete(id: number) {
  ElMessageBox.confirm("确认删除该消息？", "提示", { type: "warning" }).then(() => {
    deleteMessage(id).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning("请先选择消息");
    return;
  }
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条消息？`, "提示", { type: "warning" }).then(() => {
    batchDeleteMessages(selectedIds.value).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

// 发送
const sendVisible = ref(false);
const sendLoading = ref(false);
const sendFormRef = ref<FormInstance>();
const sendForm = reactive({
  memberId: "" as string,
  title: "",
  detail: "",
});
const sendRules = {
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  detail: [{ required: true, message: "请输入内容", trigger: "blur" }],
};

function openSendDialog() {
  sendVisible.value = true;
}

function resetSendForm() {
  sendForm.memberId = "";
  sendForm.title = "";
  sendForm.detail = "";
  sendFormRef.value?.resetFields();
}

function handleSend() {
  sendFormRef.value?.validate((valid) => {
    if (!valid) return;
    sendLoading.value = true;
    sendMessage({
      memberId: sendForm.memberId ? Number(sendForm.memberId) : undefined,
      title: sendForm.title,
      detail: sendForm.detail,
      type: 1,
    })
      .then(() => {
        ElMessage.success("发送成功");
        sendVisible.value = false;
        handleQuery();
      })
      .finally(() => {
        sendLoading.value = false;
      });
  });
}

onMounted(() => {
  handleQuery();
});
</script>

<style scoped>
.table-header {
  margin-bottom: 16px;
}
.message-detail {
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.6;
}
</style>
