<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索视频标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="推荐" prop="isRecommended">
          <el-select
            v-model="queryParams.isRecommended"
            placeholder="全部"
            clearable
            style="width: 100px"
          >
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="热门" prop="isHot">
          <el-select
            v-model="queryParams.isHot"
            placeholder="全部"
            clearable
            style="width: 100px"
          >
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="isEnable">
          <el-select
            v-model="queryParams.isEnable"
            placeholder="全部"
            clearable
            style="width: 100px"
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

    <!-- 视频列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 12px">
        <el-button
          v-hasPerm="['cms:video:add']"
          type="primary"
          @click="openDialog()"
          ><i-ep-plus />新增视频</el-button
        >
        <el-button
          v-hasPerm="['cms:video:delete']"
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
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.cover"
              :src="scope.row.cover"
              :preview-src-list="[scope.row.cover]"
              preview-teleported
              fit="cover"
              style="width: 64px; height: 40px; border-radius: 4px"
            />
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column
          label="标题"
          prop="title"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="时长" width="90" align="center">
          <template #default="scope">
            <span>{{ formatDuration(scope.row.duration) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="浏览"
          prop="accessCount"
          width="70"
          align="center"
        />
        <el-table-column label="推荐" width="70" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.isRecommended === 1 ? 'danger' : 'info'"
              disable-transitions
              >{{ scope.row.isRecommended === 1 ? "是" : "否" }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="热门" width="70" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.isHot === 1 ? 'warning' : 'info'"
              disable-transitions
              >{{ scope.row.isHot === 1 ? "是" : "否" }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
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
          label="创建时间"
          prop="createTime"
          width="170"
          align="center"
        />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-hasPerm="['cms:video:comment']"
              type="success"
              link
              size="small"
              @click="openOperate(scope.row)"
            >
              <i-ep-chat-dot-round />评论/举报
            </el-button>
            <el-button
              v-hasPerm="['cms:video:edit']"
              type="primary"
              link
              size="small"
              @click="openDialog(scope.row.id)"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              v-hasPerm="['cms:video:delete']"
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

    <!-- 新增/编辑视频弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="760px"
      :close-on-click-modal="false"
      @close="closeDialog"
    >
      <el-form
        ref="dataFormRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="视频标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入视频标题"
            maxlength="60"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="视频文件" prop="videoUrl">
              <MultiUpload
                v-model="videoFiles"
                file-type="video"
                :limit="1"
                :file-size="200"
              />
              <div class="field-tip">上传 mp4/mov 等视频文件(≤200MB)</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面">
              <SingleUpload
                v-model="formData.cover"
                :width="160"
                :height="100"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="视频地址" prop="videoUrl">
          <el-input
            v-model="formData.videoUrl"
            placeholder="上传后自动填充,也可直接粘贴视频 URL(mp4/hls)"
            clearable
          />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="视频简介,用于前台展示"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="关联商品">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="formData.spuName" readonly clearable placeholder="未选择(点击右侧选择商品)" style="flex: 1" @clear="clearSpu" />
            <el-button @click="openSpuPicker">选择商品</el-button>
          </div>
          <div v-if="formData.spuId" style="color: #909399; font-size: 12px; line-height: 20px; margin-top: 2px;">
            商品ID:{{ formData.spuId }}<template v-if="formData.spuPrice">&nbsp;·&nbsp;¥{{ formData.spuPrice }}</template>
          </div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="时长">
              <el-input-number
                v-model="formData.duration"
                :min="0"
                :max="86400"
                controls-position="right"
                style="width: 100%"
              />
              <div class="field-tip">单位:秒(如 105 = 1分45秒)</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
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
          <el-col :span="8">
            <el-form-item label="推荐">
              <el-switch
                v-model="formData.isRecommended"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>
            <el-form-item label="热门">
              <el-switch
                v-model="formData.isHot"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="启用状态">
          <el-switch
            v-model="formData.isEnable"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm"
          >保存</el-button
        >
      </template>
    </el-dialog>

    <!-- 商品选择弹窗(单选,回填 spu 快照) -->
    <SelectContentList v-if="pickerVisible" :dialog-visible="pickerVisible" :multiple="false" :selected-ids="spuSelectedIds" @close="pickerVisible = false" @confirm="spuPickerConfirm" />

    <!-- 评论/举报运营管理弹窗 -->
    <el-dialog v-model="operateDialog.visible" :title="operateDialog.title" width="860px" top="6vh">
      <el-tabs v-model="operateTab">
        <el-tab-pane label="评论管理" name="comments">
          <el-form :inline="true" style="margin-bottom: 8px">
            <el-form-item label="关键词">
              <el-input v-model="commentQuery.keyword" placeholder="昵称/内容" clearable style="width: 180px" @keyup.enter="loadComments(1)" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadComments(1)">搜索</el-button>
              <el-button @click="resetCommentQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="commentLoading" :data="commentList" border>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="videoTitle" label="视频" min-width="140" show-overflow-tooltip />
            <el-table-column prop="nickname" label="用户" width="110" show-overflow-tooltip />
            <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip />
            <el-table-column label="回复数" prop="replyCount" width="70" align="center" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isEnable === 1 ? 'success' : 'info'">{{ scope.row.isEnable === 1 ? "正常" : "隐藏" }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="150" />
            <el-table-column label="操作" width="170" align="center">
              <template #default="scope">
                <el-button v-if="scope.row.isEnable !== 1" type="success" link @click="enableCommentOperate(scope.row, 1)">显示</el-button>
                <el-button v-else type="warning" link @click="enableCommentOperate(scope.row, 0)">隐藏</el-button>
                <el-button type="danger" link @click="deleteCommentOperate(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-if="commentTotal > 0" v-model:page="commentQuery.pageNum" v-model:limit="commentQuery.pageSize" :total="commentTotal" @pagination="loadComments()" style="margin-top: 8px" />
        </el-tab-pane>
        <el-tab-pane label="举报管理" name="reports">
          <el-form :inline="true" style="margin-bottom: 8px">
            <el-form-item label="状态">
              <el-select v-model="reportQuery.status" placeholder="全部" clearable style="width: 130px">
                <el-option label="待处理" :value="0" />
                <el-option label="已处理" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadReports(1)">搜索</el-button>
              <el-button @click="resetReportQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="reportLoading" :data="reportList" border>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="videoTitle" label="视频" min-width="140" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="110" />
            <el-table-column prop="content" label="说明" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">{{ scope.row.status === 0 ? "待处理" : "已处理" }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="150" />
            <el-table-column label="操作" width="110" align="center">
              <template #default="scope">
                <el-button v-if="scope.row.status === 0" type="primary" link @click="handleReportOperate(scope.row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-if="reportTotal > 0" v-model:page="reportQuery.pageNum" v-model:limit="reportQuery.pageSize" :total="reportTotal" @pagination="loadReports()" style="margin-top: 8px" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: "CmsVideo", inheritAttrs: false });

import {
  getVideoPage,
  getVideoDetail,
  addVideo,
  updateVideo,
  enableVideo,
  deleteVideos,
  getVideoCommentPage,
  deleteVideoCommentOperate,
  enableVideoCommentOperate,
  getVideoReportPage,
  handleVideoReport,
} from "@/api/cms/video";
import type {
  VideoPageQuery,
  VideoPageVO,
  VideoDetailVO,
  VideoForm,
  VideoCommentAdminVO,
  VideoReportAdminVO,
} from "@/api/cms/video";
import type { FormInstance } from "element-plus";
import SingleUpload from "@/components/Upload/SingleUpload.vue";
import MultiUpload from "@/components/Upload/MultiUpload.vue";
import SelectContentList from "@/components/SelectContentList/index.vue";

const queryFormRef = ref<FormInstance>();
const dataFormRef = ref<FormInstance>();

const loading = ref(false);
const submitLoading = ref(false);
const total = ref(0);
const list = ref<VideoPageVO[]>([]);
const ids = ref<number[]>([]);

const queryParams = reactive<VideoPageQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  isRecommended: undefined,
  isHot: undefined,
  isEnable: undefined,
});

const dialog = reactive({ visible: false, title: "" });
const formData = reactive<VideoForm>({
  id: undefined,
  title: "",
  cover: "",
  videoUrl: "",
  duration: 0,
  description: "",
  isRecommended: 0,
  isHot: 0,
  sort: 0,
  isEnable: 1,
  spuId: undefined,
  spuName: "",
  spuPic: "",
  spuPrice: "",
});

/** 视频文件列表(MultiUpload 契约 string[] url,上限 1 个;与 videoUrl 单向同步) */
const videoFiles = computed<string[]>({
  get: () => (formData.videoUrl ? [formData.videoUrl] : []),
  set: (val) => {
    formData.videoUrl = val && val.length > 0 ? val[0] : "";
  },
});

const rules = reactive({
  title: [{ required: true, message: "请输入视频标题", trigger: "blur" }],
  videoUrl: [
    { required: true, message: "请上传或填写视频地址", trigger: "blur" },
  ],
});

function handleQuery() {
  loading.value = true;
  getVideoPage(queryParams)
    .then(({ data }) => {
      list.value = data.list as VideoPageVO[];
      total.value = data.total;
    })
    .finally(() => (loading.value = false));
}

function resetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.pageNum = 1;
  handleQuery();
}

function handleSelectionChange(selection: VideoPageVO[]) {
  ids.value = selection.map((item) => item.id);
}

/** 时长(秒)→ 人类可读 */
function formatDuration(seconds?: number): string {
  const s = Number(seconds) || 0;
  if (s <= 0) return "-";
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return m > 0 ? `${m}分${sec}秒` : `${sec}秒`;
}

function handleEnableChange(row: VideoPageVO, val: number | string | boolean) {
  const next = Number(val) === 1 ? 1 : 0;
  enableVideo(row.id, { isEnable: next })
    .then(() => {
      ElMessage.success(next === 1 ? "已启用" : "已停用");
    })
    .catch(() => handleQuery());
}

// 新增/编辑

function openDialog(id?: number) {
  resetForm();
  if (id) {
    dialog.title = "编辑视频";
    getVideoDetail(id).then(({ data }) => {
      const detail = data as VideoDetailVO;
      formData.id = detail.id;
      formData.title = detail.title || "";
      formData.cover = detail.cover || "";
      formData.videoUrl = detail.videoUrl || "";
      formData.duration = detail.duration ?? 0;
      formData.description = detail.description || "";
      formData.isRecommended = detail.isRecommended === 1 ? 1 : 0;
      formData.isHot = detail.isHot === 1 ? 1 : 0;
      formData.sort = detail.sort ?? 0;
      formData.isEnable = detail.isEnable === 1 ? 1 : 0;
      formData.spuId = detail.spuId && detail.spuId > 0 ? detail.spuId : undefined;
      formData.spuName = detail.spuName || "";
      formData.spuPic = detail.spuPic || "";
      formData.spuPrice = detail.spuPrice || "";
      dialog.visible = true;
    });
  } else {
    dialog.title = "新增视频";
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
    title: "",
    cover: "",
    videoUrl: "",
    duration: 0,
    description: "",
    isRecommended: 0,
    isHot: 0,
    sort: 0,
    isEnable: 1,
    spuId: undefined,
    spuName: "",
    spuPic: "",
    spuPrice: "",
  });
}

function submitForm() {
  dataFormRef.value?.validate((valid) => {
    if (!valid) return;
    const url = (formData.videoUrl || "").trim();
    if (!url) {
      ElMessage.warning("请上传或填写视频地址");
      return;
    }
    submitLoading.value = true;
    const payload: VideoForm = {
      id: formData.id,
      title: (formData.title || "").trim(),
      cover: formData.cover || "",
      videoUrl: url,
      duration: formData.duration ?? 0,
      description: (formData.description || "").trim(),
      isRecommended: formData.isRecommended === 1 ? 1 : 0,
      isHot: formData.isHot === 1 ? 1 : 0,
      sort: formData.sort ?? 0,
      isEnable: formData.isEnable === 1 ? 1 : 0,
      spuId: formData.spuId && formData.spuId > 0 ? formData.spuId : undefined,
      spuName: formData.spuName || "",
      spuPic: formData.spuPic || "",
      spuPrice: formData.spuPrice || "",
    };
    const api = payload.id
      ? updateVideo(payload.id, payload)
      : addVideo(payload);
    api
      .then(() => {
        ElMessage.success(payload.id ? "修改成功" : "新增成功");
        closeDialog();
        handleQuery();
      })
      .finally(() => (submitLoading.value = false));
  });
}

// 关联商品

/** 商品选择弹窗 */
const pickerVisible = ref(false);
const spuSelectedIds = computed(() =>
  formData.spuId && formData.spuId > 0 ? [formData.spuId] : []
);

function openSpuPicker() {
  pickerVisible.value = true;
}

/** SelectContentList 单选确认:rows 含 id/name/picUrl/price */
function spuPickerConfirm(rows: any[]) {
  const row = rows && rows.length > 0 ? rows[0] : null;
  if (row) {
    formData.spuId = Number(row.id);
    formData.spuName = row.name || "";
    formData.spuPic = row.picUrl || "";
    formData.spuPrice = row.price != null ? String(row.price) : "";
  }
  pickerVisible.value = false;
}

function clearSpu() {
  formData.spuId = undefined;
  formData.spuName = "";
  formData.spuPic = "";
  formData.spuPrice = "";
}

// 评论/举报运营管理

const operateDialog = reactive({ visible: false, title: "" });
const operateTab = ref("comments");

const commentList = ref<VideoCommentAdminVO[]>([]);
const commentTotal = ref(0);
const commentLoading = ref(false);
const commentQuery = reactive({ videoId: undefined as number | undefined, keyword: "", pageNum: 1, pageSize: 10, isEnable: undefined as number | undefined });

const reportList = ref<VideoReportAdminVO[]>([]);
const reportTotal = ref(0);
const reportLoading = ref(false);
const reportQuery = reactive({ videoId: undefined as number | undefined, keyword: "", status: undefined as number | undefined, pageNum: 1, pageSize: 10 });

function openOperate(row: VideoPageVO) {
  operateDialog.title = `评论/举报管理 - ${row.title || "视频"}`;
  operateDialog.visible = true;
  operateTab.value = "comments";
  commentQuery.videoId = row.id;
  reportQuery.videoId = row.id;
  loadComments(1);
  loadReports(1);
}

function loadComments(page?: number) {
  if (page) commentQuery.pageNum = page;
  commentLoading.value = true;
  getVideoCommentPage({ ...commentQuery })
    .then(({ data }) => {
      commentList.value = data.list as VideoCommentAdminVO[];
      commentTotal.value = data.total;
    })
    .finally(() => (commentLoading.value = false));
}

function resetCommentQuery() {
  commentQuery.keyword = "";
  commentQuery.pageNum = 1;
  loadComments(1);
}

function enableCommentOperate(row: VideoCommentAdminVO, isEnable: number) {
  enableVideoCommentOperate(row.id, isEnable).then(() => {
    ElMessage.success("操作成功");
    loadComments();
  });
}

function deleteCommentOperate(row: VideoCommentAdminVO) {
  ElMessageBox.confirm(`确认删除评论 #${row.id}?`, "警告", { type: "warning" })
    .then(() => {
      deleteVideoCommentOperate(row.id).then(() => {
        ElMessage.success("删除成功");
        loadComments();
      });
    })
    .catch(() => {});
}

function loadReports(page?: number) {
  if (page) reportQuery.pageNum = page;
  reportLoading.value = true;
  getVideoReportPage({ ...reportQuery })
    .then(({ data }) => {
      reportList.value = data.list as VideoReportAdminVO[];
      reportTotal.value = data.total;
    })
    .finally(() => (reportLoading.value = false));
}

function resetReportQuery() {
  reportQuery.status = undefined;
  reportQuery.pageNum = 1;
  loadReports(1);
}

function handleReportOperate(row: VideoReportAdminVO) {
  handleVideoReport(row.id, 1).then(() => {
    ElMessage.success("已处理");
    loadReports();
  });
}

// 删除

function handleDelete(row: VideoPageVO) {
  ElMessageBox.confirm(
    `确认删除该视频「${row.title}」？删除后前台不再展示。`
  ).then(() => {
    deleteVideos([row.id]).then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
  });
}

function handleBatchDelete() {
  if (ids.value.length === 0) return;
  ElMessageBox.confirm(
    `确认删除选中的 ${ids.value.length} 条视频？删除后前台不再展示。`
  ).then(() => {
    deleteVideos(ids.value).then(() => {
      ElMessage.success("批量删除成功");
      handleQuery();
    });
  });
}

onMounted(() => {
  handleQuery();
});
</script>

<style scoped>
.field-tip {
  color: #909399;
  font-size: 12px;
  line-height: 18px;
  margin-top: 4px;
  white-space: nowrap;
}
</style>
