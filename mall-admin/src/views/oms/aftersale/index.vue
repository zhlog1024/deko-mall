<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="afterSaleNo" label="售后单号">
          <el-input v-model="queryParams.afterSaleNo" placeholder="售后单号" />
        </el-form-item>

        <el-form-item prop="orderNo" label="订单编号">
          <el-input v-model="queryParams.orderNo" placeholder="订单编号" />
        </el-form-item>

        <el-form-item prop="memberId" label="会员ID">
          <el-input v-model="queryParams.memberId" placeholder="请输入会员ID" />
        </el-form-item>

        <el-form-item label="申请日期">
          <el-date-picker
            v-model="dateRangeTime"
            style="width: 260px"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item label="售后类型">
          <el-select
            v-model="queryParams.afterSaleType"
            class="filter-item"
            placeholder="售后状态"
            clearable
          >
            <el-option
              v-for="item in afterSaleTypeMap"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="售后状态">
          <el-select
            v-model="statusSelected"
            class="filter-item"
            placeholder="售后状态"
            clearable
            multiple
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="item in afterSaleStatusMap"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
          <el-button type="primary" @click="handleExport">
            <i-ep-download />
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table ref="dataTable" v-loading="loading" :data="afterSaleList" border>


      <el-table-column align="center" prop="afterSaleNo" label="售后单号" width="170" />
      <el-table-column align="center" prop="orderNo" label="订单编号" width="170" show-overflow-tooltip />

      <!-- 售后单 = 订单商品行维度：主行直接展示商品（1:1 恒单行） -->
      <el-table-column label="商品" min-width="240">
        <template #default="scope">
          <div v-if="scope.row.items && scope.row.items.length" class="aftersale-goods-row">
            <el-image :src="scope.row.items[0].picUrl" fit="cover" style="width:40px;height:40px;border-radius:4px;flex-shrink:0" />
            <div style="min-width:0;margin-left:8px">
              <div style="font-size:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:150px">{{ scope.row.items[0].skuName }}</div>
              <div style="font-size:12px;color:#909399">
                商品ID:{{ scope.row.items[0].spuId }} 规格ID:{{ scope.row.items[0].skuId }}
              </div>
              <div style="font-size:12px;color:#909399">×{{ scope.row.items[0].quantity }}</div>
            </div>
          </div>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <!-- 行级退款状态（商品行 status） -->
      <el-table-column align="center" label="退款状态" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.items && scope.row.items[0] && scope.row.items[0].status === 5" size="small" type="danger">已退款</el-tag>
          <el-tag v-else-if="scope.row.items && scope.row.items[0] && scope.row.items[0].status === 4" size="small" type="warning">售后中</el-tag>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <!-- 行级退款数量（部分退款） -->
      <el-table-column align="center" label="退款数量" width="90">
        <template #default="scope">
          <span v-if="scope.row.items && scope.row.items[0] && scope.row.items[0].refundQuantity > 0" style="color:#E6A23C">退{{ scope.row.items[0].refundQuantity }}件</span>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="memberId" label="会员ID" />
      <el-table-column align="center" label="供应商" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.supplierName || scope.row.supplierId || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="售后状态">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)"
            >{{ scope.row.statusLabel }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="refundAmount" label="退款金额">
        <template #default="scope">
          {{ scope.row.refundAmount }}
        </template>
      </el-table-column>

      <el-table-column align="center" label="退还">
        <template #default="scope">
          <div v-if="scope.row.usePointsAmount && scope.row.usePointsAmount > 0">
            积分: {{ scope.row.usePointsAmount }}
          </div>
          <div v-if="!scope.row.usePointsAmount" style="color: #909399;">
            -
          </div>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="reason" label="退款原因" />
      <el-table-column align="center" prop="createTime" label="申请时间" />

      <el-table-column align="left" label="操作" width="250">
        <template #default="scope">
          <!-- 根据售后单状态显示不同的操作按钮 -->

          <el-button type ="primary" size="small" @click="viewDetail(scope.row)"
          >详情
          </el-button>

          <el-button
            v-if="scope.row.trackingNo"
            type="info"
            size="small"
            @click="viewLogistics(scope.row)"
          >查看物流
          </el-button>


          <el-button
            v-if="scope.row.status === 1"
            type="success"
            size="small"
            @click="handleAudit(scope.row, true)"
            >审核通过
          </el-button>

          <el-button
            v-if="scope.row.status === 1"
            type="danger"
            size="small"
            @click="handleAudit(scope.row, false)"
            >拒绝申请
          </el-button>

          <el-button
            v-if="scope.row.status === 4 || scope.row.status === 5"
            type="success"
            size="small"
            @click="openReceiveDialog(scope.row)"
            >收货确认
          </el-button>

          <el-button
            v-if="scope.row.status === 6"
            type="warning"
            size="small"
            @click="handleRefund(scope.row)"
            >执行退款
          </el-button>




        </template>
      </el-table-column>
    </el-table>

    <!-- 分页工具条 -->
    <el-pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper,-> ,slot"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes="[10, 20, 50, 100]"
    >
    </el-pagination>

    <!-- 售后审核弹窗 -->
    <el-dialog
      v-model="auditDialog.visible"
      :title="auditDialog.approve ? '审核通过' : '拒绝申请'"
      width="500px"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="100px"
      >
        <el-form-item label="售后单号">
          {{ auditForm.afterSaleNo }}
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="auditDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit">确定</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 收货确认弹窗 -->
    <el-dialog
      v-model="receiveDialog.visible"
      title="收货确认"
      width="500px"
    >
      <el-form
        ref="receiveFormRef"
        :model="receiveForm"
        :rules="receiveRules"
        label-width="100px"
      >
        <el-form-item label="售后单号">
          {{ receiveForm.afterSaleNo }}
        </el-form-item>
        <el-form-item label="处理结果">
          <el-radio-group v-model="receiveForm.approved">
            <el-radio :label="true">确认收货</el-radio>
            <el-radio :label="false">拒绝收货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="receiveForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="receiveDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitReceive">确定</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 售后详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="售后详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="售后单号"
          >{{ afterSaleDetail.afterSaleNo }}
        </el-descriptions-item>
        <el-descriptions-item label="订单编号"
          >{{ afterSaleDetail.orderNo }}
        </el-descriptions-item>
        <el-descriptions-item label="会员ID"
          >{{ afterSaleDetail.memberId }}
        </el-descriptions-item>
        <el-descriptions-item label="售后状态">
          <el-tag :type="getStatusTagType(afterSaleDetail.status)"
            >{{ afterSaleDetail.statusLabel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="现金退还">
          <span style="color: #ff4d4f;">¥{{ afterSaleDetail.refundAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="抵扣退还">
          <div v-if="afterSaleDetail.usePointsAmount && afterSaleDetail.usePointsAmount > 0">
            积分: {{ afterSaleDetail.usePointsAmount }}
          </div>
          <div v-if="!afterSaleDetail.usePointsAmount" style="color: #909399;">
            -
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="申请原因"
          >{{ afterSaleDetail.reason }}
        </el-descriptions-item>
        <el-descriptions-item label="申请描述" :span="2"
          >{{ afterSaleDetail.description }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间"
          >{{ afterSaleDetail.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间"
          >{{ afterSaleDetail.handleTime }}
        </el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2"
          >{{ afterSaleDetail.auditRemark }}
        </el-descriptions-item>

        <template v-if="afterSaleDetail.trackingNo">
          <el-descriptions-item label="物流公司"
            >{{ afterSaleDetail.carrierName }}
          </el-descriptions-item>
          <el-descriptions-item label="物流单号"
            >{{ afterSaleDetail.trackingNo }}
          </el-descriptions-item>
        </template>

        <el-descriptions-item
          label="退款时间"
          v-if="afterSaleDetail.refundTime"
        >
          {{ afterSaleDetail.refundTime }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="proof-pics" v-if="afterSaleDetail.proofPics">
        <h4>凭证图片</h4>
        <div class="pics-container">
          <img
            v-for="(pic, index) in proofPicsArray"
            :key="index"
            :src="pic"
            class="proof-pic"
            @click="handlePreviewImage(pic)"
          />
        </div>
      </div>

      <h4>售后商品</h4>
      <el-table :data="afterSaleDetail.items" border>
        <el-table-column label="商品ID" align="center" prop="spuId" />
        <el-table-column label="规格ID" align="center" prop="skuId" />
        <el-table-column label="商品规格" align="center" prop="skuName" />
        <el-table-column label="图片" prop="picUrl">
          <template #default="scope">
            <img :src="scope.row.picUrl" width="40" />
          </template>
        </el-table-column>
        <el-table-column align="center" label="单价" prop="price">
          <template #default="scope"
            >{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column align="center" label="数量" prop="quantity" />
      </el-table>
    </el-dialog>

    <!-- 物流信息弹窗 -->
    <el-dialog
      title="物流详情"
      v-model="logisticsInfo.visible"
      width="650px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="logistics-box-sub">
        <div class="loging-box">
          <el-icon class="good-icon">
            <Van />
          </el-icon>
          <div>
            <p>
              <b>{{ logisticsInfo.carrierName }}</b>
            </p>
            <p>物流单号：{{ logisticsInfo.trackingNo }}</p>
          </div>
        </div>
      </div>

      <h4>物流轨迹</h4>
      <div
        class="collapse-box"
        :class="logisticsInfo.collapse ? 'open' : 'stow'"
      >
        <el-timeline class="info-box">
          <el-timeline-item
            v-for="(item, index) in logisticsInfo.trackEvent"
            :key="index"
            :timestamp="item.eventTime"
          >
            {{ item.trackEvent }}
          </el-timeline-item>
        </el-timeline>
      </div>

      <div style="text-align: center; margin-top: 10px">
        <el-button
          type="text"
          @click="toggleCollapse"
          v-if="!logisticsInfo.collapse"
        >
          展开
          <el-icon class="el-icon--bottom">
            <ArrowDown />
          </el-icon>
        </el-button>
        <el-button
          type="text"
          @click="toggleCollapse"
          v-if="logisticsInfo.collapse"
        >
          收起
          <el-icon class="el-icon--bottom">
            <ArrowUp />
          </el-icon>
        </el-button>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="logisticsInfo.visible = false"
            >确定</el-button
          >
        </span>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog v-model="imagePreview.visible" title="图片预览" width="800px">
      <img
        :src="imagePreview.url"
        style="width: 100%; max-height: 70vh; object-fit: contain"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "AfterSale",
  inheritAttrs: false,
});

import {
  auditAfterSale,
  confirmReceive,
  executeAfterSale,
  exportAfterSales,
  getAfterSaleDetail,
  getAfterSalePage,
  getLogisticsDetail,
} from "@/api/oms/aftersale";
import {
  AfterSale,
  AfterSaleDetail,
  AfterSaleQuery,
} from "@/api/oms/aftersale/types";
import { parseTime } from "@/utils/index";
import { ArrowDown, ArrowUp, Van } from "@element-plus/icons-vue";
import { FormInstance } from "element-plus";

const queryFormRef = ref<FormInstance>();
const auditFormRef = ref<FormInstance>();

// 售后状态映射
const afterSaleStatusMap = [
  { label: "待审核", value: 1 },
  { label: "已拒绝", value: 2 },
  { label: "待退货", value: 3 },
  { label: "退货中", value: 4 },
  { label: "待收货", value: 5 },
  { label: "待退款", value: 6 },
  { label: "退款中", value: 7 },
  { label: "退款成功", value: 8 },
  { label: "退款失败", value: 9 },
  { label: "已取消", value: 10 },
];

// 售后状态映射
const afterSaleTypeMap = [
  { label: "仅退款", value: 1 },
  { label: "退货退款", value: 2 },
];

// 售后状态对应的Tag类型
const getStatusTagType = (status: number): string => {
  const statusMap: Record<number, string> = {
    1: "info", // 待审核 - 蓝色
    2: "danger", // 已拒绝 - 红色
    3: "warning", // 待退货 - 黄色
    4: "warning", // 退货中 - 黄色
    5: "warning", // 待收货 - 黄色
    6: "primary", // 退款中 - 蓝色
    7: "primary", // 退款中 - 蓝色
    8: "success", // 退款成功 - 绿色
    9: "danger", // 退款失败 - 红色
    10: "info", // 已取消 - 灰色
  };
  return statusMap[status] || "";
};

const state = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined,
    memberId: "",
    keywords: "",
  } as AfterSaleQuery,
  afterSaleList: [] as AfterSale[],
  total: 0,
});

// 使用ref来存储多选状态值
const statusSelected = ref<number[]>([]);

const { loading, queryParams, afterSaleList, total } = toRefs(state);
const dateRangeTime = ref([]);

// 审核弹窗
const auditDialog = reactive({
  visible: false,
  approve: true,
});

const auditForm = reactive({
  id: "",
  afterSaleNo: "",
  remark: "",
});

// 收货确认弹窗
const receiveDialog = reactive({
  visible: false,
});

const receiveFormRef = ref<FormInstance>();
const receiveForm = reactive({
  id: "",
  afterSaleNo: "",
  approved: true as boolean,
  remark: "",
});

const receiveRules = reactive({
  remark: [{ required: true, message: "请输入备注信息", trigger: "blur" }],
});

const auditRules = reactive({
  remark: [{ required: true, message: "请输入备注信息", trigger: "blur" }],
});

// 详情弹窗
const detailDialog = reactive({
  visible: false,
});

const afterSaleDetail = reactive<AfterSaleDetail>({
  id: 0,
  afterSaleNo: "",
  orderNo: "",
  memberId: 0,
  status: 0,
  statusLabel: "",
  totalAmount: 0,
  refundAmount: 0,
  reason: "",
  description: "",
  proofPics: "",
  trackingNo: "",
  carrierName: "",
  carrierCode: "",
  createTime: "",
  handleTime: "",
  auditRemark: "",
  refundTime: "",
  items: [],
});

// 计算属性：证明图片数组
const proofPicsArray = computed(() => {
  if (!afterSaleDetail.proofPics) return [];
  return afterSaleDetail.proofPics.split(",");
});

// 图片预览
const imagePreview = reactive({
  visible: false,
  url: "",
});

// 物流信息
const logisticsInfo = reactive({
  carrierName: "",
  trackingNo: "",
  orderNo: "",
  trackEvent: [] as any[],
  visible: false,
  collapse: false,
});

// 退款统计
const refundAmount = reactive({
  totalRefund: 0,
});

// 查询售后单列表
function handleQuery() {
  state.loading = true;

  // 处理状态多选，转换为逗号分隔的字符串
  if (statusSelected.value && statusSelected.value.length > 0) {
    state.queryParams.status = statusSelected.value.join(",");
  } else {
    state.queryParams.status = undefined;
  }

  let params = {
    ...state.queryParams,
    beginDate: dateRangeTime.value ? parseTime(dateRangeTime.value[0]) : "",
    endDate: dateRangeTime.value ? parseTime(dateRangeTime.value[1]) : "",
  };
  getAfterSalePage(params).then(({ data }) => {
    state.afterSaleList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

// 重置查询条件
function resetQuery() {
  queryFormRef.value?.resetFields();
  dateRangeTime.value = [];
  statusSelected.value = [];
  handleQuery();
}

// 导出售后单
function handleExport() {
  // 处理状态多选，转换为逗号分隔的字符串
  if (statusSelected.value && statusSelected.value.length > 0) {
    state.queryParams.status = statusSelected.value.join(",");
  } else {
    state.queryParams.status = undefined;
  }

  let params = {
    ...state.queryParams,
    beginDate: dateRangeTime.value ? parseTime(dateRangeTime.value[0]) : "",
    endDate: dateRangeTime.value ? parseTime(dateRangeTime.value[1]) : "",
  };
  exportAfterSales(params).then((res) => {
    const fileData = res.data;
    const fileType =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8";
    const blob = new Blob([fileData], { type: fileType });
    let filename = "aftersales.xlsx"; // 默认文件名

    const downloadUrl = window.URL.createObjectURL(blob);

    const downloadLink = document.createElement("a");
    downloadLink.href = downloadUrl;
    downloadLink.download = filename;

    document.body.appendChild(downloadLink);
    downloadLink.click();

    document.body.removeChild(downloadLink);
    window.URL.revokeObjectURL(downloadUrl);
  });
}

// 审核售后申请
function handleAudit(row: AfterSale, approve: boolean) {
  auditDialog.visible = true;
  auditDialog.approve = approve;
  auditForm.id = row.id.toString();
  auditForm.afterSaleNo = row.afterSaleNo;
  auditForm.remark = "";
}

// 提交审核
function submitAudit() {
  auditFormRef.value?.validate((valid) => {
    if (valid) {
      // 改为params方式传参（查询参数）
      auditAfterSale(auditForm.id, {
        approve: auditDialog.approve,
        remark: auditForm.remark,
      }).then(() => {
        ElMessage.success("操作成功");
        auditDialog.visible = false;
        handleQuery();
      });
    }
  });
}

// 确认收货
function openReceiveDialog(row: AfterSale) {
  receiveDialog.visible = true;
  receiveForm.id = row.id.toString();
  receiveForm.afterSaleNo = row.afterSaleNo;
  receiveForm.approved = true;
  receiveForm.remark = "";
}

function submitReceive() {
  receiveFormRef.value?.validate((valid) => {
    if (!valid) return;
    const approved = receiveForm.approved;
    const remark = receiveForm.remark;
    confirmReceive(receiveForm.id, approved, remark).then(() => {
      ElMessage.success(approved ? "确认收货成功" : "已拒绝收货");
      receiveDialog.visible = false;
      handleQuery();
    });
  });
}

// 执行退款
function handleRefund(row: AfterSale) {
  ElMessageBox.confirm("确认执行退款操作?", "提示", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    executeAfterSale(row.id).then(() => {
      ElMessage.success("退款操作已执行");
      handleQuery();
    });
  });
}

// 查看详情
function viewDetail(row: AfterSale) {
  getAfterSaleDetail(row.id).then(({ data }) => {
    Object.assign(afterSaleDetail, data);
    detailDialog.visible = true;
  });
}

// 查看物流
function viewLogistics(row: AfterSale) {
  if (!row.trackingNo) {
    ElMessage.warning("暂无物流信息");
    return;
  }

  // 直接使用物流单号查询物流详情
  getLogisticsDetail(row.trackingNo)
    .then(({ data }) => {
      if (data) {
        logisticsInfo.carrierName = data.carrierName || row.carrierName; // 优先使用返回值，兜底使用售后单信息
        logisticsInfo.trackingNo = data.trackingNo || row.trackingNo;
        logisticsInfo.orderNo = row.orderNo; // 使用售后单关联的订单号
        logisticsInfo.trackEvent = data.tracks;
        logisticsInfo.visible = true;
        logisticsInfo.collapse = false;
      } else {
        ElMessage.warning("暂无物流信息");
      }
    })
    .catch(() => {
      ElMessage.error("查询物流信息失败");
    });
}

// 展开/收起物流信息
function toggleCollapse() {
  logisticsInfo.collapse = !logisticsInfo.collapse;
}

// 预览图片
function handlePreviewImage(url: string) {
  imagePreview.url = url;
  imagePreview.visible = true;
}

// 分页相关
const handleSizeChange = (val: number) => {
  state.queryParams.pageSize = val;
  handleQuery();
};

const handleCurrentChange = (val: number) => {
  state.queryParams.pageNum = val;
  handleQuery();
};

onMounted(() => {
  handleQuery();
});
</script>

<style lang="scss" scoped>
.filter-item {
  width: 120px;
}

.price {
  display: inline-block;
  margin-right: 20px;
  color: red;
}

.logistics-box-sub {
  .loging-box {
    display: flex;
    align-items: flex-start;

    p {
      margin: 0;
    }
  }

  .good-icon {
    font-size: 40px;
    border-radius: 50%;
    margin-right: 16px;
  }
}

.info-box {
  max-height: 300px;
  overflow: auto;
}

.collapse-box {
  overflow: hidden;
  padding-top: 5px;

  &.stow {
    height: 100px;
  }

  &.open {
    height: auto;
  }
}

.proof-pics {
  margin: 20px 0;

  .pics-container {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .proof-pic {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 4px;
    cursor: pointer;

    &:hover {
      transform: scale(1.05);
      transition: transform 0.3s;
    }
  }
}

// 折叠加隐藏过渡效果
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.5s;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  height: 0px !important;
  opacity: 0;
}

/* 售后单商品列 */
.aftersale-goods {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 2px 0;
}
.aftersale-goods-row {
  display: flex;
  align-items: center;
}
</style>
