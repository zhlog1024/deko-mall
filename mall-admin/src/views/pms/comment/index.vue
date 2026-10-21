<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="商品名称">
          <el-input
            v-model="queryParams.spuName"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select
            v-model="queryParams.rating"
            placeholder="全部"
            clearable
            style="width: 100px"
          >
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="评论时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
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

    <el-card>
      <template #header>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleBatchDelete"
        >
          <i-ep-delete />
          批量删除
        </el-button>
      </template>

      <el-table
        v-loading="loading"
        :data="commentList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column prop="spuId" label="商品ID" width="80" align="center" />

        <el-table-column prop="spuName" label="商品名称" min-width="150" show-overflow-tooltip />

        <el-table-column label="商品图片" width="120" align="center">
          <template #default="scope">
            <el-popover v-if="scope.row.spuPic" placement="right" :width="400" trigger="hover">
              <img :src="scope.row.spuPic" width="400" height="400" />
              <template #reference>
                <img :src="scope.row.spuPic" style="max-width: 60px; max-height: 60px" />
              </template>
            </el-popover>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="skuId" label="规格ID" width="80" align="center">
          <template #default="scope">
            {{ scope.row.skuId || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="评分" width="160" align="center">
          <template #default="scope">
            <el-rate
              :model-value="Number(scope.row.rating)"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>

        <el-table-column prop="content" label="评论内容" min-width="180" show-overflow-tooltip />

        <el-table-column label="评论图片" width="90" align="center">
          <template #default="scope">
            <el-popover
              v-if="scope.row.images && scope.row.images.length > 0"
              placement="right"
              :width="400"
              trigger="hover"
            >
              <div style="display: flex; flex-wrap: wrap; gap: 8px">
                <el-image
                  v-for="(img, idx) in scope.row.images"
                  :key="idx"
                  :src="img"
                  :preview-src-list="scope.row.images"
                  :initial-index="Number(idx)"
                  style="width: 100px; height: 100px"
                  fit="cover"
                />
              </div>
              <template #reference>
                <el-tag type="primary">{{ scope.row.images.length }}张</el-tag>
              </template>
            </el-popover>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ statusMap[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="回复" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isReplied" type="success" size="small">已回复</el-tag>
            <el-tag v-else type="info" size="small">未回复</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="memberName" label="用户" width="100" show-overflow-tooltip />
        <el-table-column prop="createTime" label="评论时间" width="160" align="center" />

        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click.stop="showDetail(scope.row)">
              详情
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              link
              @click.stop="handleAudit(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              @click.stop="handleAudit(scope.row, 2)"
            >
              拒绝
            </el-button>
            <el-button type="warning" link @click.stop="showReply(scope.row)">
              回复
            </el-button>
            <el-button type="danger" link @click.stop="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="评论详情" width="650px">
      <template v-if="detailDialog.data">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品名称" :span="2">
            {{ detailDialog.data.spuName }}
          </el-descriptions-item>
          <el-descriptions-item label="商品ID">{{ detailDialog.data.spuId }}</el-descriptions-item>
          <el-descriptions-item label="规格ID">{{ detailDialog.data.skuId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="规格名称">{{ detailDialog.data.skuName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ detailDialog.data.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate :model-value="Number(detailDialog.data.rating)" disabled />
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(detailDialog.data.status)">
              {{ statusMap[detailDialog.data.status] || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ detailDialog.data.memberName }}</el-descriptions-item>
          <el-descriptions-item label="是否匿名">
            {{ detailDialog.data.isAnonymous ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="评论内容" :span="2">
            {{ detailDialog.data.content }}
          </el-descriptions-item>
          <el-descriptions-item label="评论图片" :span="2">
            <div
              v-if="detailDialog.data.images && detailDialog.data.images.length > 0"
              style="display: flex; gap: 8px; flex-wrap: wrap"
            >
              <el-image
                v-for="(img, idx) in detailDialog.data.images"
                :key="idx"
                :src="img"
                :preview-src-list="detailDialog.data.images"
                :initial-index="Number(idx)"
                style="width: 80px; height: 80px"
                fit="cover"
              />
            </div>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="回复内容" :span="2">
            {{ detailDialog.data.replyContent || '暂无回复' }}
          </el-descriptions-item>
          <el-descriptions-item label="评论时间">{{ detailDialog.data.createTime }}</el-descriptions-item>
          <el-descriptions-item label="回复时间">
            {{ detailDialog.data.replyTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialog.visible" title="回复评论" width="500px" @close="replyDialog.data = null">
      <el-form ref="replyFormRef" :model="replyDialog" :rules="replyRules" label-width="80px">
        <el-form-item v-if="replyDialog.data?.replyContent" label="原回复">
          <span class="cr-grey">{{ replyDialog.data.replyContent }}</span>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyDialog.replyContent"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyDialog.loading">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "Comment",
  inheritAttrs: false,
});

import {
  getCommentPage,
  getCommentDetail,
  replyComment,
  auditComment,
  deleteComment,
} from "@/api/pms/comment";

import type { CommentQuery, CommentVO } from "@/api/pms/comment/types";

const queryFormRef = ref(ElForm);
const replyFormRef = ref(ElForm);

const statusOptions = [
  { label: "待审核", value: 0 },
  { label: "已通过", value: 1 },
  { label: "已拒绝", value: 2 },
];

const statusMap: Record<number, string> = {
  0: "待审核",
  1: "已通过",
  2: "已拒绝",
};

const state = reactive({
  loading: true,
  ids: [] as number[],
  total: 0,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  } as CommentQuery,
  commentList: [] as CommentVO[],
  detailDialog: {
    visible: false,
    data: null as CommentVO | null,
  },
  replyDialog: {
    visible: false,
    loading: false,
    data: null as CommentVO | null,
    replyContent: "",
  },
  dateRange: [] as string[],
});

const {
  loading,
  ids,
  total,
  queryParams,
  commentList,
  detailDialog,
  replyDialog,
  dateRange,
} = toRefs(state);

const replyRules = {
  replyContent: [
    { required: true, message: "请输入回复内容", trigger: "blur" },
  ],
};

function handleQuery() {
  state.loading = true;
  const params = { ...state.queryParams };
  if (state.dateRange && state.dateRange.length === 2) {
    params.startTime = state.dateRange[0];
    params.endTime = state.dateRange[1];
  } else {
    params.startTime = undefined;
    params.endTime = undefined;
  }
  getCommentPage(params).then(({ data }) => {
    state.commentList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  state.dateRange = [];
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
  };
  handleQuery();
}

function handleSelectionChange(selection: CommentVO[]) {
  state.ids = selection.map((item) => item.id);
}

function handleBatchDelete() {
  if (state.ids.length === 0) return;
  ElMessageBox.confirm("确认删除选中的评论数据?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    const deletePromises = state.ids.map((id) => deleteComment(id));
    Promise.all(deletePromises).then(() => {
      ElMessage.success("批量删除成功");
      handleQuery();
    });
  });
}

function showDetail(row: CommentVO) {
  state.detailDialog.visible = true;
  state.detailDialog.data = row;
}

function showReply(row: CommentVO) {
  getCommentDetail(row.id).then(({ data }) => {
    state.replyDialog.data = data;
    state.replyDialog.replyContent = data.replyContent || "";
    state.replyDialog.visible = true;
  });
}

function submitReply() {
  replyFormRef.value.validate((valid: boolean) => {
    if (valid && state.replyDialog.data) {
      state.replyDialog.loading = true;
      replyComment(state.replyDialog.data.id, {
        replyContent: state.replyDialog.replyContent,
      })
        .then(() => {
          ElMessage.success("回复成功");
          state.replyDialog.visible = false;
          handleQuery();
        })
        .finally(() => {
          state.replyDialog.loading = false;
        });
    }
  });
}

function handleAudit(row: CommentVO, status: number) {
  const text = status === 1 ? "通过" : "拒绝";
  ElMessageBox.confirm(`确认${text}该评论?`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    auditComment(row.id, { status }).then(() => {
      ElMessage.success(`审核${text}`);
      handleQuery();
    });
  });
}

function handleDelete(row: CommentVO) {
  ElMessageBox.confirm("确认删除该评论?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteComment(row.id).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

function getStatusTagType(status: number) {
  switch (status) {
    case 0: return "warning";
    case 1: return "success";
    case 2: return "danger";
    default: return "info";
  }
}

onMounted(() => {
  handleQuery();
});
</script>
