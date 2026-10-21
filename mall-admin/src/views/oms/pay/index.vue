<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="payChannel" label="支付渠道">
          <el-select
            v-model="queryParams.payChannel"
            placeholder="支付渠道"
            clearable
            :style="{width: '200px'}"
          >
            <el-option
              v-for="item in channelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-select
            v-model="queryParams.enabled"
            placeholder="启用状态"
            clearable
            :style="{width: '200px'}"
          >
            <el-option label="启用" value="true" />
            <el-option label="禁用" value="false" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <i-ep-search />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <i-ep-refresh />
            重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <i-ep-plus />
            新增
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table ref="dataTable" v-loading="loading" :data="payConfigList" border>
      <el-table-column
        align="center"
        prop="id"
        label="配置ID"
        class="table-column"
      />
      <el-table-column
        align="center"
        prop="payChannel"
        label="支付渠道"
        class="table-column"
      >
        <template #default="scope">
          <el-tag
            :type="getPayChannelTagType(scope.row.payChannel)"
            effect="plain"
            size="small"
          >
            {{
              scope.row.channelName || scope.row.payChannel
            }}
          </el-tag>
        </template>
      </el-table-column>
      >

      <el-table-column
        align="center"
        prop="appId"
        label="应用ID"
        class="table-column"
      />

      <el-table-column
        align="center"
        prop="mchId"
        label="商户ID"
        class="table-column"
      />

      <el-table-column
        align="center"
        prop="enabled"
        label="启用状态"
        class="table-column"
      >
        <template #default="scope">
          <span
            class="status-tag"
            :class="scope.row.enabled ? 'enabled' : 'disabled'"
          >
            {{
              statusMap.find((item) => item.value === scope.row.enabled)?.label
            }}
          </span>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        prop="cashierName"
        label="收银台展示"
        width="120"
        class="table-column"
      />

      <el-table-column
        align="center"
        prop="deviceType"
        label="适用端"
        width="120"
        class="table-column"
      />

      <el-table-column
        align="center"
        label="图标"
        width="80"
        class="table-column"
      >
        <template #default="scope">
          <el-avatar v-if="scope.row.channelIcon" :src="scope.row.channelIcon" :size="32" shape="square" />
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        prop="createTime"
        label="创建时间"
        class="table-column"
      />
      <el-table-column
        align="center"
        prop="updateTime"
        label="更新时间"
        class="table-column"
      />
      <el-table-column
        align="center"
        label="操作"
        width="180"
        class="table-column"
      >
        <template #default="scope">
          <div class="action-buttons">
            <el-button
              type="primary"
              @click="handleUpdate(scope.row)"
              plain
              class="action-button"
            >
              <i-ep-edit class="button-icon" />
              修改
            </el-button>

            <el-button
              v-if="scope.row.enabled === false"
              @click="toggleShelf(scope.row)"
              type="primary"
              plain
              class="action-button"
            >
              <i-ep-check class="button-icon" />
              启用
            </el-button>
            <el-button
              v-else-if="scope.row.enabled === true"
              @click="toggleShelf(scope.row)"
              type="danger"
              plain
              class="action-button"
            >
              <i-ep-close class="button-icon" />
              禁用
            </el-button>
          </div>
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

    <!-- 表单弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      top="5vh"
      width="700px"
    >
      <el-form
        ref="dataFormRef"
        :model="payConfig"
        :rules="rules"
        label-width="120px"
      >
        <!-- 基本配置（微信/支付宝直连 + 线下；全额抵扣由系统自动走，不在此配置） -->
        <el-card class="config-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><i-ep-setting class="header-icon" /> 基本配置</span>
            </div>
          </template>

          <el-form-item label="支付渠道" prop="payChannel">
            <el-select
              v-model="payConfig.payChannel"
              class="filter-item"
              placeholder="请选择"
              :disabled="!!payConfig.id"
              @change="onChannelChange"
            >
              <el-option
                v-for="item in channelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="应用ID" prop="appId">
            <el-input v-model="payConfig.appId" placeholder="请输入应用ID" />
          </el-form-item>

          <el-form-item label="商户ID" prop="mchId">
            <el-input v-model="payConfig.mchId" placeholder="请输入商户ID" />
          </el-form-item>

          <el-form-item label="支付回调地址" prop="paymentNotifyUrl">
            <el-input
              v-model="payConfig.paymentNotifyUrl"
              placeholder="请输入支付回调地址"
            />
          </el-form-item>

          <el-form-item label="退款回调地址" prop="refundNotifyUrl">
            <el-input
              v-model="payConfig.refundNotifyUrl"
              placeholder="请输入退款回调地址"
            />
          </el-form-item>

          <el-form-item label="适用端" prop="deviceType">
            <el-input
              v-model="payConfig.deviceType"
              placeholder="H5,MINI,APP（逗号分隔）"
            />
          </el-form-item>

          <el-form-item label="收银台展示" prop="cashierName">
            <el-input v-model="payConfig.cashierName" placeholder="C端用户看到的名称" />
          </el-form-item>

          <el-form-item label="用户白名单" prop="allowedUserIds">
            <el-input v-model="payConfig.allowedUserIds" placeholder="用户ID，逗号分隔，留空则所有用户可见" />
          </el-form-item>

          <el-form-item label="图标" prop="channelIcon">
            <single-upload v-model="payConfig.channelIcon" :width="80" :height="80" />
          </el-form-item>
        </el-card>

        <AlipayConfig
          v-if="isAlipay"
          v-model:extConfig="extConfigObj"
        />
        <WechatConfig
          v-if="isWechat"
          v-model:extConfig="extConfigObj"
        />
        <OfflinePayConfig
          v-if="isOfflinePay"
          v-model:extConfig="extConfigObj"
        />
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="handleCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { DialogType, PayConfig, PayConfigQuery } from "@/api/oms/pay/types";
import { addPayConfig, getPayConfig, getPayConfigPage, updatePayConfig } from "@/api/oms/pay";
import AlipayConfig from "./components/AlipayConfig.vue";
import WechatConfig from "./components/WechatConfig.vue";
import OfflinePayConfig from "./components/OfflinePayConfig.vue";
import SingleUpload from "@/components/Upload/SingleUpload.vue";

/** 可配置渠道（对齐 PayChannelEnum，不含 FULL_DEDUCTION） */
const PAY_CHANNEL_OPTIONS = [
  { value: "WECHAT_JSAPI", label: "微信JSAPI", deviceType: "APPLET", cashierName: "微信支付" },
  { value: "WECHAT_APP", label: "微信APP", deviceType: "APP", cashierName: "微信支付" },
  { value: "ALIPAY_WAP", label: "支付宝WAP", deviceType: "H5", cashierName: "支付宝" },
  { value: "ALIPAY_APP", label: "支付宝APP", deviceType: "APP", cashierName: "支付宝" },
  { value: "OFFLINE_PAY", label: "线下支付", deviceType: "H5,APPLET,APP", cashierName: "线下支付" },
] as const;

const queryFormRef = ref(ElForm);
const dataFormRef = ref(ElForm);
const loading = ref(false);
const total = ref(0);
const payConfigList = ref<PayConfig[]>([]);
const dialog = reactive<DialogType>({ visible: false, title: "" });

const payConfig = reactive<PayConfig>({
  id: 0, payChannel: "", appId: "", mchId: "",
  paymentNotifyUrl: "", refundNotifyUrl: "", enabled: false,
  deviceType: "", channelName: "", cashierName: "", channelIcon: "", allowedUserIds: "", extConfig: "",
});

const extConfigObj = ref<Record<string, any>>({});

const channelOptions = PAY_CHANNEL_OPTIONS.map(({ value, label }) => ({ value, label }));

const isAlipay = computed(() => !!payConfig.payChannel?.startsWith("ALIPAY"));
const isWechat = computed(() => !!payConfig.payChannel?.startsWith("WECHAT"));
const isOfflinePay = computed(() => payConfig.payChannel === "OFFLINE_PAY");

const onChannelChange = (channel: string) => {
  const opt = PAY_CHANNEL_OPTIONS.find((item) => item.value === channel);
  if (!opt) return;
  payConfig.channelName = opt.label;
  if (!payConfig.cashierName) payConfig.cashierName = opt.cashierName;
  if (!payConfig.deviceType) payConfig.deviceType = opt.deviceType;
  extConfigObj.value = {};
};

const queryParams = reactive<PayConfigQuery>({
  pageNum: 1,
  pageSize: 10,
  payChannel: "",
  enabled: undefined,
});

const rules = {
  payChannel: [
    { required: true, message: "请选择支付渠道", trigger: "change" },
  ],
};

const statusMap = [
  { label: "禁用", value: false },
  { label: "启用", value: true },
];

// 查询列表
const handleQuery = async () => {
  try {
    loading.value = true;
    const { data } = await getPayConfigPage(queryParams);
    // 全额抵扣由系统自动走，不在运营配置列表展示
    payConfigList.value = (data.list || []).filter(
      (c: PayConfig) => c.payChannel !== "FULL_DEDUCTION"
    );
    total.value = data.total;
  } catch (error) {
    ElMessage.error("获取支付配置列表失败");
  } finally {
    loading.value = false;
  }
};

// 修改支付配置
const handleUpdate = async (row: PayConfig) => {
  try {
    const { data } = await getPayConfig(row.id as number);
    Object.assign(payConfig, data);
    // 解析扩展配置 JSON → 对象，传给子组件
    if (data.extConfig) {
      try { extConfigObj.value = JSON.parse(data.extConfig); } catch {}
    } else {
      extConfigObj.value = {};
    }
    dialog.title = "修改支付配置";
    dialog.visible = true;
  } catch {
    ElMessage.error("获取支付配置详情失败");
  }
};

// 提交表单
const submitForm = async () => {
  const form = unref(dataFormRef);
  if (!form) return;
  await form.validate(async (valid: boolean) => {
    if (valid) {
      const submitData = { ...payConfig };
      submitData.extConfig = JSON.stringify(extConfigObj.value || {});
      if (!submitData.channelName) {
        submitData.channelName =
          PAY_CHANNEL_OPTIONS.find((item) => item.value === submitData.payChannel)?.label ||
          submitData.payChannel;
      }
      delete (submitData as any).parsedExtConfig;
      try {
        if (payConfig.id) { await updatePayConfig(submitData); ElMessage.success("修改成功"); }
        else { await addPayConfig(submitData); ElMessage.success("新增成功"); }
        dialog.visible = false;
        await handleQuery();
        handleCancel();
      } catch { ElMessage.error("操作失败"); }
    }
  });
};

// 切换状态
const toggleShelf = async (row: PayConfig) => {
  try {
    const newStatus = !row.enabled;
    await updatePayConfig({ ...row, enabled: newStatus });
    ElMessage.success("状态更新成功");
    await handleQuery();
  } catch (error) {
    ElMessage.error("状态更新失败");
  }
};

// 取消
const handleCancel = () => {
  dialog.visible = false;
  dataFormRef.value?.resetFields();
  extConfigObj.value = {};
  Object.assign(payConfig, {
    id: undefined, payChannel: "", appId: "", mchId: "",
    paymentNotifyUrl: "", refundNotifyUrl: "", enabled: false,
    deviceType: "", channelName: "", cashierName: "", channelIcon: "", allowedUserIds: "", extConfig: "",
  });
};

// 重置
const resetQuery = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize = 10;
  queryParams.payChannel = "";
  queryParams.enabled = undefined;
  handleQuery();
};

// 获取支付渠道标签类型
const getPayChannelTagType = (
  payChannel: string
): "primary" | "success" | "warning" | "info" | "danger" => {
  if (payChannel.includes("ALIPAY")) return "primary";
  if (payChannel.includes("WECHAT")) return "success";
  if (payChannel.includes("OFFLINE_PAY")) return "warning";
  return "info";
};

// 新增
const handleAdd = () => {
  dialog.title = "新增支付渠道配置";
  dialog.visible = true;
  extConfigObj.value = {};
  Object.assign(payConfig, {
    id: undefined, payChannel: "", appId: "", mchId: "",
    paymentNotifyUrl: "", refundNotifyUrl: "", enabled: false,
    deviceType: "", channelName: "", cashierName: "", channelIcon: "", allowedUserIds: "", extConfig: "",
  });
};

onMounted(() => {
  handleQuery();
});
</script>

<style lang="scss" scoped>
.search-container {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.filter-item {
  width: 200px;
}

.config-card {
  margin-bottom: 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.config-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ext-config-card {
  margin-top: 25px;
  border-left: 4px solid #409eff;
}

/* 全额支付说明样式 */
.deduction-info {
  line-height: 1.8;
}

.deduction-info ul {
  margin: 10px 0;
  padding-left: 20px;
}

.deduction-info li {
  margin: 5px 0;
}

.config-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}

.form-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.header-icon {
  margin-right: 8px;
  font-size: 18px;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

/* 表格样式美化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .el-table__header-wrapper th {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
}

/* 分页样式 */
:deep(.el-pagination) {
  margin-top: 20px;
  justify-content: center;
}

/* 按钮样式 */
.btn-small {
  padding: 6px 12px;
  font-size: 12px;
  margin: 0 5px;
}

/* 表单项样式美化 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-textarea__inner) {
  border-radius: 4px;
}

/* 卡片样式过渡 */
.el-card {
  transition: all 0.3s ease;
}

/* 状态标签 */
.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;

  &.enabled {
    background-color: #f0f9eb;
    color: #67c23a;
    border: 1px solid #c2e7b0;
  }

  &.disabled {
    background-color: #fef0f0;
    color: #f56c6c;
    border: 1px solid #fbc4c4;
  }
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.action-button {
  margin: 0;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  }
}

.button-icon {
  margin-right: 4px;
  font-size: 14px;
}

/* 表格样式增强 */
:deep(.el-table) {
  .el-table__row {
    transition: all 0.3s;

    &:hover {
      background-color: #f5f7fa !important;
    }
  }

  // 缩小表格首尾行距离
  .el-table__body td {
    padding: 8px 0;
  }
}

/* 搜索区域样式 */
.search-container {
  margin-bottom: 18px;
  padding: 20px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .operation-group {
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;

    .add-button {
      font-weight: 500;
      letter-spacing: 0.5px;
      padding: 10px 20px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
  }

  .search-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;

    .search-item {
      margin-right: 15px;
      margin-bottom: 0;
    }

    .search-select {
      width: 180px !important;
    }

    .search-buttons {
      margin-left: 0;

      .reset-button,
      .add-button {
        margin-left: 10px;
      }
    }
  }
}
</style>
