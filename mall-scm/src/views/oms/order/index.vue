<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="orderNo" label="订单号">
          <el-input v-model="queryParams.keywords" placeholder="订单号" />
        </el-form-item>
        <el-form-item prop="memberId" label="会员ID">
          <el-input v-model="queryParams.memberId" placeholder="请输入会员ID" />
        </el-form-item>
        <el-form-item label="商品ID">
          <el-input v-model="queryParams.spuId" placeholder="请输入商品ID" />
        </el-form-item>
        <el-form-item label="用户手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="订单日期">
          <el-date-picker
            v-model="dateRangeTime"
            style="width: 260px"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item label="订单状态">
          <Dictionary
            v-model="queryParams.status"
            typeCode="ORDER_STATUS"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择订单状态"
          />
        </el-form-item>
      </el-form>
      <div class="search-operation">
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
        <el-button type="primary" @click="handleBatchDelivery">
          <i-ep-Van />
          批量发货
        </el-button>
      </div>
    </div>

    <el-table ref="dataTable" v-loading="loading" :data="orderList" border>

      <el-table-column align="left" prop="orderNo" label="订单编号" width="200" show-overflow-tooltip />

      <el-table-column label="商品" min-width="240">
        <template #default="scope">
          <div v-if="scope.row.orderItems && scope.row.orderItems.length" class="goods-cell">
            <div v-for="(item, idx) in scope.row.orderItems.slice(0, 2)" :key="idx" class="goods-row">
              <div class="goods-img-wrap">
                <el-image :src="item.picUrl" fit="cover" class="goods-img"
                          :preview-src-list="scope.row.orderItems.map(i => i.picUrl)" :initial-index="idx" preview-teleported />
                <el-tag v-if="item.status === 4" size="small" type="warning" class="goods-badge">售后</el-tag>
                <el-tag v-else-if="item.status === 5" size="small" type="danger" class="goods-badge">退款</el-tag>
                <el-tag v-else-if="item.status === 0" size="small" type="info" class="goods-badge">取消</el-tag>
              </div>
              <div class="goods-info">
                <div class="goods-name" :title="item.skuName">{{ item.skuName }}</div>
                <div class="goods-meta">商品ID:{{ item.spuId }} 规格ID:{{ item.skuId }}</div>
                <div class="goods-meta">¥{{ item.price }} × {{ item.quantity }}</div>
              </div>
            </div>
            <div v-if="scope.row.orderItems.length > 2" class="goods-more">等 {{ scope.row.orderItems.length }} 件商品</div>
          </div>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="订单状态">
        <template #default="scope">
          <el-tag>{{ scope.row.statusLabel }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="订单金额">
        <template #default="scope">
          <div>商品总额: ¥{{ scope.row.totalAmount }}</div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="运费">
        <template #default="scope">
          <div>¥{{ scope.row.freightAmount || 0 }}</div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="下单人信息">
        <template #default="scope">
          <div>{{ scope.row.buyerPhone || '-' }}</div>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="paymentTime" label="支付时间" />

      <el-table-column align="center" label="收货人信息">
        <template #default="scope">
          <div>{{ scope.row.receiverName }} {{ scope.row.receiverPhone }}</div>
          <div>{{ scope.row.receiverCompleteAddress || scope.row.receiverProvince + scope.row.receiverCity + scope.row.receiverDistrict }}</div>
        </template>
      </el-table-column>

      <el-table-column align="center" label="发货信息">
        <template #default="scope">
          <div>{{ scope.row.carrierName }}</div>
          <div style="color: #909399; font-size: 12px;">{{ scope.row.trackingNo }}</div>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="createTime" label="下单时间" />

      <el-table-column align="center" label="操作">
        <template #default="scope">
          <el-button type="primary" link @click="viewDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status == 1" type="primary" link @click="deliver(scope.row)">发货</el-button>
          <el-button
            v-if="(scope.row.status == 2 || scope.row.status == 3) && scope.row.needDelivery && scope.row.deliveryType !== 2"
            type="primary" link
            @click="viewMaterialflow(scope.row)"
          >物流</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页工具条 -->
    <el-pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 发货弹框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发货"
      width="800px"
      :before-close="handleClose"
    >
      <el-form
        ref="deliverRef"
        :model="deliverInfo"
        :rules="deliverRules"
        label-width="120px"
      >
        <el-form-item label="订单编号">
          {{ deliverInfo.orderNo }}
        </el-form-item>
        <el-form-item label="物流公司" prop="deliveryCode">
          <el-select
            v-model="deliverInfo.deliveryCode"
            class=""
            placeholder="请选择物流公司"
          >
            <el-option
              v-for="item in logisticsList"
              :key="item.carrierCode"
              :label="item.carrierName"
              :value="item.carrierCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="deliverySn">
          <el-input
            v-model="deliverInfo.deliverySn"
            placeholder="请输入快递单号"
            clearable
          />
        </el-form-item>
        <el-form-item label="发货备注">
          <el-input
            v-model="deliverInfo.remark"
            placeholder="请输入备注信息"
            type="textarea"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDeliver(deliverRef)"
            >确定
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
    <!--  物流信息 -->
    <el-dialog
      title="物流详情"
      v-model="logisInfo.visible"
      width="650px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <!-- 快递发货 -->

      <div class="logistics-box-sub">
        <div class="loging-box">
          <el-icon class="good-icon">
            <Van />
          </el-icon>
          <div>
            <p>
              <span>物流公司：{{ logisInfo.carrierName || "暂无" }}</span>
            </p>
            <p>
              <span>物流单号：{{ logisInfo.trackingNo || "暂无" }}</span>
            </p>
          </div>
        </div>
      </div>
      <div class="logistics-box-sub">
        <h3 class="">详细信息</h3>
        <div :class="{ 'collapse-box': true, stow: !logisInfo.collapse }">
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in logisInfo.trackEvent"
              :key="index"
              :timestamp="activity.status"
            >
              {{ activity.updateTime }} {{ activity.trackEvent }}
            </el-timeline-item>
          </el-timeline>
        </div>
        <el-button type="text" @click="packUp" v-if="!logisInfo.collapse"
          >展开
          <el-icon class="el-icon--top">
            <ArrowDown />
          </el-icon>
        </el-button>
        <el-button type="text" @click="packUp" v-if="logisInfo.collapse"
          >收起
          <el-icon class="el-icon--bottom">
            <ArrowUp />
          </el-icon>
        </el-button>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="logisInfo.visible = false"
            >确 定</el-button
          >
        </span>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="订单详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">
          <el-tag>{{ detailData.orderTypeLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag>{{ detailData.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品总数">{{ detailData.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="零售总价">¥{{ detailData.totalRetailPrice || 0 }}</el-descriptions-item>
        <el-descriptions-item label="供货总价">¥{{ detailData.totalSupplyPrice || 0 }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ detailData.freightAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="确认收货时间">{{ detailData.receiveTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结算时间">{{ detailData.settlementTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detailData.receiverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.receiverPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detailData.receiverCompleteAddress || '-' }}</el-descriptions-item>
        <!-- 发货信息 - 根据deliveryType区分显示 -->
        <el-descriptions-item label="发货类型">
          <template v-if="detailData.deliveryType === 2">
            <el-tag type="warning">线下发货</el-tag>
          </template>
          <template v-else-if="detailData.deliveryType === 1">
            <el-tag type="success">线上物流</el-tag>
          </template>
          <template v-else>
            <span style="color: #909399;">-</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ detailData.deliveryTime || '-' }}</el-descriptions-item>

        <!-- 线下发货详情 -->
        <template v-if="detailData.deliveryType === 2">
          <el-descriptions-item label="备注说明" :span="2">{{ detailData.offlineRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货凭证" :span="2">
            <div v-if="offlineDeliveryData.offlineProof" style="display: flex; gap: 8px; flex-wrap: wrap;">
              <el-image
                v-for="(url, idx) in offlineDeliveryData.offlineProof.split(',')"
                :key="idx"
                :src="url"
                :preview-src-list="offlineDeliveryData.offlineProof.split(',')"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 4px;"
              />
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
        </template>

        <!-- 线上物流详情 -->
        <template v-if="detailData.deliveryType === 1">
          <el-descriptions-item label="物流公司">{{ detailData.carrierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ detailData.trackingNo || '-' }}</el-descriptions-item>
        </template>

        <el-descriptions-item label="订单备注">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 订单商品列表 -->
      <el-divider>订单商品</el-divider>
      <el-table :data="detailData.orderItems || []" border>
        <el-table-column label="商品图片" width="80">
          <template #default="scope">
            <img :src="scope.row.picUrl" width="50" height="50" />
          </template>
        </el-table-column>
        <el-table-column prop="skuName" label="商品规格" />
        <el-table-column label="商品 ID" align="center" prop="spuId" />
        <el-table-column label="规格 ID" align="center" prop="skuId" />
        <el-table-column label="供应商ID" align="center" prop="supplierId" />
        <el-table-column label="零售价" align="center">
          <template #default="scope">¥{{ scope.row.retailPrice || 0 }}</template>
        </el-table-column>
        <el-table-column label="供货价" align="center">
          <template #default="scope">¥{{ scope.row.supplyPrice || 0 }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" align="center" width="80" />
        <el-table-column label="小计" align="center">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 批量发货弹窗 -->
    <BatchDeliverDialog
      v-if="batchDeliveryVisible"
      v-model="batchDeliveryVisible"
    />
  </div>
</template>
<!-- 订单管理 -->
<script setup lang="ts">
defineOptions({
  name: "Order",
  inheritAttrs: false,
});

import {
  deliveryOrder,
  exportOrders,
  getLogisticsByOrderId,
  getOfflineDeliveryDetail,
  getOrderDetail,
  getOrderLogistics,
  getOrderPage,
} from "@/api/oms/order";
import { Order, OrderQuery } from "@/api/oms/order/types";
import { type FormInstance } from "element-plus";
import BatchDeliverDialog from "./components/BatchDeliverDialog.vue";
import { parseTime } from "@/utils/index";
import Dictionary from "@/components/Dictionary/index.vue";

const queryFormRef = ref(ElForm);

const state = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined,
    memberId: "",
    keywords: "",
    spuId: "",
    phone: "",
  } as OrderQuery,
  orderList: [] as Order[],
  total: 0,
  dialogVisible: false,
});

const { loading, queryParams, orderList, total, dialogVisible } =
  toRefs(state);
const dateRangeTime = ref([]);

// 构建查询参数（含日期范围）
function buildQueryParams() {
  return {
    ...state.queryParams,
    beginDate: dateRangeTime.value ? parseTime(dateRangeTime.value[0]) : undefined,
    endDate: dateRangeTime.value ? parseTime(dateRangeTime.value[1]) : undefined,
  };
}

function handleQuery() {
  state.loading = true;
  getOrderPage(buildQueryParams()).then(({ data }) => {
    state.orderList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function handleExport() {
  exportOrders(buildQueryParams()).then((res) => {
    const fileData = res.data;
    const fileType =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8";
    const blob = new Blob([fileData], { type: fileType });
    let filename = "orders.xlsx"; // 默认文件名

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

function resetQuery() {
  queryFormRef.value.resetFields();
  dateRangeTime.value = [];
  handleQuery();
}

const handleSizeChange = (val: number) => {
  state.queryParams.pageSize = val;
  handleQuery();
};

const handleCurrentChange = (val: number) => {
  state.queryParams.pageNum = val;
  handleQuery();
};

// 发货相关
const logisticsList = ref([]);
const getLogistics = async () => {
  let res = await getOrderLogistics();
  if (res.code == "00000") {
    logisticsList.value = res.data;
  }
};
getLogistics();

const deliverInfo = ref({
  orderNo: "",
  deliveryCode: "",
  deliverySn: "",
  remark: "",
});
const deliverRef = ref();
const deliverRules = reactive({
  deliveryCode: [
    { required: true, message: "请选择物流公司", trigger: "change" },
  ],
  deliverySn: [{ required: true, message: "请输入物流单号", trigger: "blur" }],
});
const handleClose = (done) => {
  deliverRef.value.clearValidate();
  deliverRef.value.resetFields();
  done();
};
const deliver = (row) => {
  deliverInfo.value.orderNo = row.orderNo;

  dialogVisible.value = true;
};

const handleDeliver = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate((valid, fields) => {
    if (valid) {
      deliveryOrderFun();
    } else {
      console.log("error submit!", fields);
    }
  });
};

const deliveryOrderFun = async () => {
  let res = await deliveryOrder(deliverInfo.value);
  if (res.code == "00000") {
    ElMessage({
      type: "success",
      message: "发货成功",
    });
    dialogVisible.value = false;
    handleQuery();
  }
};

const logisInfo = reactive({
  carrierName: "",
  trackingNo: "",
  createTime: "",
  orderNo: "",
  trackEvent: "",
  visible: false,
  collapse: false,
});

// 订单详情弹窗
const detailDialog = reactive({
  visible: false,
});

const detailData = reactive<any>({
  orderNo: "",
  orderTypeLabel: "",
  memberId: "",
  statusLabel: "",
  totalQuantity: "",
  sourceLabel: "",
  totalRetailPrice: "",
  freightAmount: "",
  couponAmount: "",
  totalAmount: "",
  pointsDeductionAmount: "",
  usePointsAmount: "",
  paymentDiscountAmount: "",
  paymentAmount: "",
  refundAmount: "",
  payChannelLabel: "",
  paymentTime: "",
  createTime: "",
  buyerPhone: "",
  receiverName: "",
  receiverPhone: "",
  receiverCompleteAddress: "",
  carrierName: "",
  trackingNo: "",
  deliveryTime: "",
  remark: "",
  receiveTime: "",  // 确认收货时间
  settlementTime: "",  // 结算时间
  orderItems: [],
});

// 线下发货信息
const offlineDeliveryData = reactive<any>({
  id: undefined,
  offlineProof: "",
  offlineRemark: "",
  carrierCode: "",
  carrierName: "",
  trackingNo: "",
  auditor: "",
  auditTime: "",
});

// 查看订单详情




const viewDetail = async (row: any) => {
  let res = await getOrderDetail(row.id);
  if (res.code == "00000" && res.data) {
    Object.assign(detailData, res.data);
    // 获取线下发货信息
    try {
      const offlineRes = await getOfflineDeliveryDetail(row.id);
      if (offlineRes.code == "00000" && offlineRes.data) {
        Object.assign(offlineDeliveryData, offlineRes.data);
      } else {
        offlineDeliveryData.id = undefined;
      }
    } catch (e) {
      offlineDeliveryData.id = undefined;
    }
    detailDialog.visible = true;
  }
};

// 查看物流信息
const viewMaterialflow = async (row) => {
  let res = await getLogisticsByOrderId({ trackingNo: row.trackingNo });
  if (res.code == "00000" && res.data) {
    let data = res.data;
    logisInfo.carrierName = data.carrierName;
    logisInfo.trackingNo = data.trackingNo;
    // logisInfo.createTime = data.createTime
    logisInfo.orderNo = data.orderNo;
    logisInfo.trackEvent = data.tracks;
    logisInfo.visible = true;
  }
};

const packUp = () => {
  logisInfo.collapse = !logisInfo.collapse;
};

// 批量发货弹窗
const batchDeliveryVisible = ref(false);
const handleBatchDelivery = () => {
  batchDeliveryVisible.value = true;
};

onMounted(() => {
  handleQuery();
});
</script>
<style lang="scss" scoped>

.search-container {
  margin-bottom: 16px;

  :deep(.el-form-item) {
    min-width: 160px;
  }
}

.filter-item {
  width: 140px;
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

.product-info-box {
  display: flex;
  flex-wrap: wrap;
  margin-right: -16px;
}

.product-info {
  margin: 0 16px 16px 0;

  .img {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    overflow: hidden;
  }

  .text {
    text-align: center;
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

.el-timeline {
  padding: 0 10px;
}

.el-carousel__item h3 {
  color: #475669;
  opacity: 0.75;
  line-height: 300px;
  margin: 0;
  text-align: center;
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n + 1) {
  background-color: #d3dce6;
}

// 折叠加隐藏过渡效果
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.5s;
  // 防止折叠时元素溢出
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  height: 0px !important;
  opacity: 0;
}

.search-operation {
  margin-bottom: 16px;
}

/* 商品摘要列：图+名两行卡片 */
.goods-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 2px 0;
}

.goods-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.goods-img-wrap {
  position: relative;
  flex-shrink: 0;
}

.goods-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: block;
}

.goods-badge {
  position: absolute;
  right: -4px;
  top: -4px;
  transform: scale(0.8);
}

.goods-info {
  min-width: 0;
}

.goods-name {
  font-size: 12px;
  color: var(--el-text-color-primary);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.goods-more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}


/* 商品摘要列：图+名两行卡片 */
.goods-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 2px 0;
}

.goods-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.goods-img-wrap {
  position: relative;
  flex-shrink: 0;
}

.goods-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: block;
}

.goods-badge {
  position: absolute;
  right: -4px;
  top: -4px;
  transform: scale(0.8);
}

.goods-info {
  min-width: 0;
}

.goods-name {
  font-size: 12px;
  color: var(--el-text-color-primary);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.goods-more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}


</style>
