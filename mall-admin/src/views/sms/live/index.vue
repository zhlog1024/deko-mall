<!-- 直播管理 -->
<script setup lang="ts">
defineOptions({
  name: "LiveRoom",
  inheritAttrs: false,
});

import { getLiveRoomPage, getLiveRoomDetail, addLiveRoom, updateLiveRoom, deleteLiveRooms, changeLiveRoomStatus, refreshLiveStreamUrl, getLiveRoomGoods, bindLiveGoods, unbindLiveGoods, setExplainGoods, getLiveSettings } from "@/api/sms/live";
import type { LiveRoom, LiveRoomForm, LiveRoomQuery, LiveGoods, LiveSettings } from "@/api/sms/live/types";
import SelectContentList from "@/components/SelectContentList/index.vue";

const queryFormRef = ref(ElForm);
const dataFormRef = ref(ElForm);

const state = reactive({
  loading: true,
  ids: [] as number[],
  single: true,
  multiple: true,
  queryParams: { pageNum: 1, pageSize: 10 } as LiveRoomQuery,
  roomList: [] as LiveRoom[],
  total: 0,
  // 新增/编辑弹窗
  dialog: { title: "", visible: false } as DialogType,
  formData: {
    isLiveChat: 1,
    isLiveGoodsBuy: 1,
    isLiveLike: 1,
    isRecommended: 0,
    liveReconnectNumber: 3,
    liveReconnectIntervalTime: 3,
    liveGoodsExplainAutoCloseTime: 10,
    sort: 100,
  } as LiveRoomForm,
  rules: {
    title: [{ required: true, message: "请输入直播标题", trigger: "blur" }],
  },
  // 推流地址弹窗
  streamDialog: { visible: false } as DialogType,
  streamData: {} as LiveRoom,
  // 商品管理弹窗
  goodsDialog: { visible: false } as DialogType,
  currentRoomId: 0,
  goodsList: [] as LiveGoods[],
  goodsLoading: false,
  // 添加商品弹窗（统一商品选择组件）
  pickerVisible: false,
  // 全局设置弹窗
  settingsVisible: false,
  settings: {} as LiveSettings,
});

const {
  loading,
  multiple,
  queryParams,
  roomList,
  total,
  dialog,
  formData,
  rules,
  goodsList,
  settings,
  // 弹窗/数据（模板直接引用的 state 字段必须解构，否则模板访问为 undefined）
  streamDialog,
  streamData,
  goodsDialog,
  goodsLoading,
  pickerVisible,
  settingsVisible,
} = toRefs(state);

const STATUS_TAG = {
  0: { text: "未开播", type: "info" },
  1: { text: "直播中", type: "success" },
  2: { text: "离开", type: "warning" },
  3: { text: "封禁", type: "danger" },
};

function handleQuery() {
  state.loading = true;
  getLiveRoomPage(state.queryParams).then(({ data }) => {
    state.roomList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  queryFormRef.value.resetFields();
  handleQuery();
}

function handleSelectionChange(selection: any) {
  state.ids = selection.map((item: any) => item.id);
  state.single = selection.length !== 1;
  state.multiple = !selection.length;
}

function handleAdd() {
  state.formData = {
    isLiveChat: 1,
    isLiveGoodsBuy: 1,
    isLiveLike: 1,
    isRecommended: 0,
    liveReconnectNumber: 3,
    liveReconnectIntervalTime: 3,
    liveGoodsExplainAutoCloseTime: 10,
    sort: 100,
  };
  state.dialog = { title: "新增直播间", visible: true };
}

function handleUpdate(row: LiveRoom) {
  state.dialog = { title: "编辑直播间", visible: true };
  getLiveRoomDetail(row.id).then(({ data }) => {
    state.formData = {
      id: data.id,
      title: data.title,
      cover: data.cover,
      anchorName: data.anchorName,
      anchorAvatar: data.anchorAvatar,
      sort: data.sort,
      // 功能配置回填（后端 LiveRoomPageVO 已含这些字段）
      isLiveChat: data.isLiveChat,
      isLiveGoodsBuy: data.isLiveGoodsBuy,
      isLiveLike: data.isLiveLike,
      liveTips: data.liveTips,
      liveReconnectNumber: data.liveReconnectNumber,
      liveReconnectIntervalTime: data.liveReconnectIntervalTime,
      liveGoodsExplainAutoCloseTime: data.liveGoodsExplainAutoCloseTime,
      isRecommended: data.isRecommended,
      remark: data.remark,
    };
  });
}

function submitForm() {
  dataFormRef.value.validate((valid: any) => {
    if (valid) {
      const roomId = state.formData.id;
      if (roomId) {
        updateLiveRoom(roomId, state.formData).then(() => {
          ElMessage.success("修改成功");
          cancel();
          handleQuery();
        });
      } else {
        addLiveRoom(state.formData).then(() => {
          ElMessage.success("新增成功");
          cancel();
          handleQuery();
        });
      }
    }
  });
}

function cancel() {
  state.formData.id = undefined;
  dataFormRef.value.resetFields();
  state.dialog.visible = false;
}

function handleDelete(row: LiveRoom) {
  const ids = [row.id || state.ids].join(",");
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      deleteLiveRooms(ids).then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      });
    })
    .catch(() => ElMessage.info("已取消删除"));
}

/** 变更状态 */
function handleStatus(row: LiveRoom, status: number) {
  const label = STATUS_TAG[status]?.text || "";
  ElMessageBox.confirm(`确认将直播间「${row.title}」设为【${label}】?`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      changeLiveRoomStatus(row.id, status).then(() => {
        ElMessage.success("操作成功");
        handleQuery();
      });
    })
    .catch(() => ElMessage.info("已取消"));
}

/** 推流地址 */
function handleStream(row: LiveRoom) {
  state.streamData = row;
  state.streamDialog = { title: "推流地址", visible: true };
}

function handleRefreshStream() {
  refreshLiveStreamUrl(state.streamData.id).then(({ data }) => {
    state.streamData = data;
    ElMessage.success("推流地址已重新生成");
  });
}

function copyText(text: string) {
  if (!text) return;
  navigator.clipboard?.writeText(text).then(() => ElMessage.success("已复制"));
}

/** 商品管理 */
function handleGoods(row: LiveRoom) {
  state.currentRoomId = row.id;
  state.goodsDialog = { title: `商品管理 - ${row.title}`, visible: true };
  loadGoods();
}

function loadGoods() {
  state.goodsLoading = true;
  getLiveRoomGoods(state.currentRoomId).then(({ data }) => {
    state.goodsList = data;
    state.goodsLoading = false;
  });
}

function handleUnbindGoods(row: LiveGoods) {
  ElMessageBox.confirm("确认解绑该商品?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      unbindLiveGoods(row.id).then(() => {
        ElMessage.success("解绑成功");
        loadGoods();
      });
    })
    .catch(() => ElMessage.info("已取消"));
}

function handleExplain(row: LiveGoods) {
  setExplainGoods(row.id)
    .then(() => {
      ElMessage.success("已设为讲解商品");
      loadGoods();
    })
    // 向用户展示请求失败原因。
    .catch(() => ElMessage.error("设置讲解商品失败，请重试"));
}

/** 添加商品（统一商品选择组件，多选后批量绑定） */
function openPicker() {
  state.pickerVisible = true;
}

function submitPickGoods(goods: any[]) {
  if (!goods.length) {
    ElMessage.warning("请先选择商品");
    return;
  }
  const promises = goods.map((item) =>
    bindLiveGoods({ roomId: state.currentRoomId, spuId: Number(item.id) })
  );
  Promise.all(promises).then(() => {
    ElMessage.success("绑定成功");
    state.pickerVisible = false;
    loadGoods();
  });
}

/** 全局设置 */
function handleSettings() {
  state.settingsVisible = true;
  getLiveSettings().then(({ data }) => {
    state.settings = data;
  });
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item>
          <el-button type="success" @click="handleAdd"><i-ep-plus />新增</el-button>
          <el-button type="danger" :disabled="multiple" @click="handleDelete"><i-ep-delete />删除</el-button>
          <el-button @click="handleSettings"><i-ep-setting />直播设置</el-button>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="queryParams.keywords"
            placeholder="标题/主播/编号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="直播状态" clearable style="width: 130px">
            <el-option label="未开播" :value="0" />
            <el-option label="直播中" :value="1" />
            <el-option label="离开" :value="2" />
            <el-option label="封禁" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><i-ep-search /> 搜索</el-button>
          <el-button @click="resetQuery"><i-ep-refresh /> 重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      v-loading="loading"
      :data="roomList"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" min-width="5" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column label="封面" width="80">
        <template #default="scope">
          <el-popover placement="right" :width="300" trigger="hover">
            <img :src="scope.row.cover" width="300" />
            <template #reference>
              <img v-if="scope.row.cover" :src="scope.row.cover" style="max-width: 50px; max-height: 50px" />
            </template>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="title" min-width="160" label="直播标题" show-overflow-tooltip />
      <el-table-column prop="anchorName" width="100" label="主播" />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="(STATUS_TAG as any)[scope.row.status]?.type">
            {{ (STATUS_TAG as any)[scope.row.status]?.text || scope.row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="onlineCount" width="80" label="在线" align="center" />
      <el-table-column prop="likeCount" width="80" label="点赞" align="center" />
      <el-table-column prop="liveStartTime" width="150" label="开播时间" />
      <el-table-column prop="sort" width="70" label="排序" align="center" />
      <el-table-column label="操作" align="center" width="360">
        <template #default="scope">
          <el-button type="primary" link @click="handleStream(scope.row)">推流地址</el-button>
          <el-button type="success" link @click="handleGoods(scope.row)">商品</el-button>
          <el-button v-if="scope.row.status !== 1" type="success" link @click="handleStatus(scope.row, 1)">开播</el-button>
          <el-button v-if="scope.row.status === 1" type="warning" link @click="handleStatus(scope.row, 2)">离开</el-button>
          <el-button v-if="scope.row.status === 2" type="success" link @click="handleStatus(scope.row, 1)">回来</el-button>
          <el-button v-if="scope.row.status !== 0" type="info" link @click="handleStatus(scope.row, 0)">下播</el-button>
          <el-button v-if="scope.row.status !== 3" type="danger" link @click="handleStatus(scope.row, 3)">封禁</el-button>
          <el-button type="primary" link @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页工具条 -->
    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="640px">
      <el-form ref="dataFormRef" :model="formData" :rules="rules" label-width="110px">
        <el-form-item label="直播标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入直播标题" />
        </el-form-item>
        <el-form-item label="封面图">
          <single-upload v-model="formData.cover" />
        </el-form-item>
        <el-form-item label="主播昵称">
          <el-input v-model="formData.anchorName" placeholder="请输入主播昵称" />
        </el-form-item>
        <el-form-item label="主播头像">
          <single-upload v-model="formData.anchorAvatar" />
        </el-form-item>
        <el-form-item label="功能开关">
          <el-checkbox v-model="formData.isLiveChat" :true-value="1" :false-value="0">聊天</el-checkbox>
          <el-checkbox v-model="formData.isLiveGoodsBuy" :true-value="1" :false-value="0">商品购买</el-checkbox>
          <el-checkbox v-model="formData.isLiveLike" :true-value="1" :false-value="0">点赞</el-checkbox>
        </el-form-item>
        <el-form-item label="欢迎语">
          <el-input v-model="formData.liveTips" placeholder="直播间欢迎语（可选）" />
        </el-form-item>
        <el-form-item label="断流重连">
          <el-input-number v-model="formData.liveReconnectNumber" :min="0" :max="10" /> 次，
          <el-input-number v-model="formData.liveReconnectIntervalTime" :min="1" :max="60" /> 秒间隔
        </el-form-item>
        <el-form-item label="讲解自动关闭">
          <el-input-number v-model="formData.liveGoodsExplainAutoCloseTime" :min="1" :max="120" /> 秒
        </el-form-item>
        <el-form-item label="推荐">
          <el-checkbox v-model="formData.isRecommended" :true-value="1" :false-value="0">
            推荐到装修页「直播列表」组件
          </el-checkbox>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 推流地址弹窗 -->
    <el-dialog v-model="streamDialog.visible" :title="streamDialog.title" width="640px">
      <el-form label-width="90px">
        <el-form-item label="流名">
          <el-input :model-value="streamData.streamName" readonly>
            <template #append>
              <el-button @click="copyText(streamData.streamName)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="推流地址">
          <el-input :model-value="streamData.pushUrl" readonly type="textarea" :rows="3">
            <template #append>
              <el-button @click="copyText(streamData.pushUrl)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="拉流地址">
          <el-input :model-value="streamData.pullUrl" readonly type="textarea" :rows="2">
            <template #append>
              <el-button @click="copyText(streamData.pullUrl)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-alert
            type="info"
            :closable="false"
            title="将推流地址填入 OBS / 腾讯云直播助手即可开播；地址有效期24小时，过期后点击下方按钮重新生成。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleRefreshStream">重新生成</el-button>
          <el-button @click="streamDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 商品管理弹窗 -->
    <el-dialog v-model="goodsDialog.visible" :title="goodsDialog.title" width="760px">
      <div style="margin-bottom: 12px">
        <el-button type="success" @click="openPicker"><i-ep-plus />添加商品</el-button>
      </div>
      <el-table v-loading="goodsLoading" :data="goodsList" border>
        <el-table-column label="图片" width="70">
          <template #default="scope">
            <img v-if="scope.row.images" :src="scope.row.images" style="max-width: 40px; max-height: 40px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" min-width="200" label="商品标题" show-overflow-tooltip />
        <el-table-column prop="price" width="90" label="价格" />
        <el-table-column prop="inventory" width="70" label="库存" align="center" />
        <el-table-column label="讲解中" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.live_goods_is_explain === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.live_goods_is_explain !== 1" type="warning" link @click="handleExplain(scope.row)">设讲解</el-button>
            <el-button type="danger" link @click="handleUnbindGoods(scope.row)">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加商品（统一商品选择组件） -->
    <SelectContentList
      v-if="pickerVisible"
      :dialog-visible="pickerVisible"
      :multiple="true"
      :selected-ids="[]"
      @close="pickerVisible = false"
      @confirm="submitPickGoods"
    />

    <!-- 全局设置弹窗 -->
    <el-dialog v-model="settingsVisible" title="全局直播设置" width="640px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="应用名称">{{ settings.applicationName }}</el-descriptions-item>
        <el-descriptions-item label="推流域名">{{ settings.pushDomain }}</el-descriptions-item>
        <el-descriptions-item label="播放域名">{{ settings.playDomain }}</el-descriptions-item>
        <el-descriptions-item label="WS 地址">{{ settings.wsHost }}:{{ settings.wsPort }}（{{ settings.wsIsWss === 1 ? "wss" : "ws" }}）</el-descriptions-item>
        <el-descriptions-item label="播放 HTTPS">{{ settings.playUseHttps ? "是" : "否" }}</el-descriptions-item>
        <el-descriptions-item label="腾讯云 SecretId">{{ settings.tencentSecretId }}</el-descriptions-item>
      </el-descriptions>
      <el-alert
        style="margin-top: 12px"
        type="info"
        :closable="false"
        title="以上配置来自 Nacos（mall-sms.yaml → live.*），修改需在 Nacos 中调整后刷新配置。"
      />
    </el-dialog>
  </div>
</template>
