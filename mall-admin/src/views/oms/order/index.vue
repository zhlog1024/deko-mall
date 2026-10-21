<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-container">
      <el-form :model="queryParams" :inline="true">
        <!-- 经典检索区：label + 统一宽度 + 流式排列 -->
        <el-form-item>
          <el-input v-model="queryParams.keywords" placeholder="订单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="dateRangeTime"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            start-placeholder="下单时间"
            end-placeholder="结束"
          />
        </el-form-item>
        <el-form-item>
          <Dictionary v-model="queryParams.status" typeCode="ORDER_STATUS" multiple collapse-tags collapse-tags-tooltip placeholder="订单状态" />
        </el-form-item>
        <el-form-item>
          <Dictionary v-model="queryParams.orderType" typeCode="ORDER_TYPE" placeholder="订单类型" />
        </el-form-item>
        <el-form-item>
          <Dictionary v-model="queryParams.payChannel" typeCode="PAY_CHANNEL" placeholder="支付方式" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.memberId" placeholder="会员ID" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.phone" placeholder="用户手机号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.spuId" placeholder="商品ID" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.paymentNo" placeholder="支付单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.supplierId" placeholder="供应商ID" clearable @keyup.enter="handleQuery" />
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
          <el-button type="primary" plain @click="handleExport">
            <i-ep-download />
            导出
          </el-button>
          <el-button type="success" plain @click="batchDeliverVisible = true">
            <i-ep-FolderAdd />
            批量发货
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table ref="dataTable" v-loading="loading" :data="orderList" border>

      <el-table-column align="left" prop="orderNo" label="订单编号" width="200" show-overflow-tooltip />

      <el-table-column align="center" prop="orderTypeLabel" label="订单类型" width="100" show-overflow-tooltip />

      <el-table-column align="center" prop="memberId" label="会员ID" width="100" show-overflow-tooltip />

      <el-table-column align="center" label="订单来源" width="80">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.sourceLabel }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="订单状态" width="90">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.statusLabel }}</el-tag>
        </template>
      </el-table-column>


      <el-table-column align="center" label="订单金额" width="110">
        <template #default="scope">
          <span>{{ scope.row.totalAmount }}</span>
          <span v-if="scope.row.freightAmount && scope.row.freightAmount > 0" style="color:#909399;font-size:12px">
            /运费{{ scope.row.freightAmount }}
          </span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="支付金额" width="110">
        <template #default="scope">{{ scope.row.paymentAmount }}</template>
      </el-table-column>

      <el-table-column align="center" label="抵扣" width="110">
        <template #default="scope">
          <template v-if="scope.row.pointsDeductionAmount">
            <span v-if="scope.row.pointsDeductionAmount" style="color:#409EFF"> 积{{ scope.row.pointsDeductionAmount }}</span>
          </template>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="支付方式" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.payChannelLabel" size="small">{{ scope.row.payChannelLabel }}</el-tag>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="paymentChannelNo" label="支付申请号" width="180" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.paymentChannelNo || "-" }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="channelTradeNo" label="渠道交易号" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.channelTradeNo || "-" }}</span>
        </template>
      </el-table-column>

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

      <el-table-column align="center" label="发货信息" width="150">
        <template #default="scope">
          <template v-if="scope.row.carrierName">
            <div>{{ scope.row.carrierName }}</div>
            <div style="color:#909399;font-size:12px">{{ scope.row.trackingNo }}</div>
          </template>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="下单人" width="120">
        <template #default="scope">
          <div>{{ scope.row.buyerPhone || "-" }}</div>
        </template>
      </el-table-column>
      <el-table-column align="center" label="收货人" min-width="140" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.receiverName }} {{ scope.row.receiverPhone }}</span>
          <div style="color:#909399;font-size:12px">{{ scope.row.receiverCompleteAddress || scope.row.receiverProvince + scope.row.receiverCity + scope.row.receiverDistrict + scope.row.receiverAddress }}</div>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="createTime" label="创建时间" width="170" show-overflow-tooltip />

      <el-table-column align="center" label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="viewDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status == 1" type="primary" link size="small" @click="deliver(scope.row)">发货</el-button>
          <el-button
            v-if="(scope.row.status == 2 || scope.row.status == 3) && scope.row.needDelivery && scope.row.deliveryType !== 2"
            type="primary" link size="small"
            @click="viewMaterialflow(scope.row)"
          >查看物流</el-button>
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
      width="700px"
      :before-close="handleClose"
    >
      <el-tabs v-model="deliverTab" class="deliver-tabs">
        <el-tab-pane label="线上发货" name="online">
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
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="线下发货" name="offline">
          <el-form
            ref="offlineDeliverRef"
            :model="offlineDeliverInfo"
            :rules="offlineDeliverRules"
            label-width="120px"
          >
            <el-form-item label="订单编号">
              {{ deliverInfo.orderNo }}
            </el-form-item>
            <el-form-item label="发货凭证" prop="offlineProof">
              <multi-upload v-model="offlineDeliverInfo.offlineProof"></multi-upload>
              <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                请上传线下发货凭证图片，最多9张
              </div>
            </el-form-item>
            <el-form-item label="备注说明" prop="offlineRemark">
              <el-input
                v-model="offlineDeliverInfo.offlineRemark"
                placeholder="请输入备注说明，如：同城配送、私人交易等"
                type="textarea"
                :rows="3"
                clearable
              />
            </el-form-item>
            <el-form-item label="物流公司">
              <el-select
                v-model="offlineDeliverInfo.carrierCode"
                placeholder="请选择物流公司（可选）"
                clearable
              >
                <el-option
                  v-for="item in logisticsList"
                  :key="item.carrierCode"
                  :label="item.carrierName"
                  :value="item.carrierCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="物流单号">
              <el-input
                v-model="offlineDeliverInfo.trackingNo"
                placeholder="请输入物流单号（可选）"
                clearable
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeliver" :loading="submitLoading"
          >确定
        </el-button>
      </template>
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
        <el-descriptions-item label="会员ID">{{ detailData.memberId }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag>{{ detailData.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品总数">{{ detailData.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="订单来源">{{ detailData.sourceLabel }}</el-descriptions-item>
        <el-descriptions-item label="零售总价">¥{{ detailData.totalRetailPrice || 0 }}</el-descriptions-item>
        <el-descriptions-item label="供货总价">¥{{ detailData.totalSupplyPrice || 0 }}</el-descriptions-item>
        <el-descriptions-item label="运费金额">¥{{ detailData.freightAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="优惠券抵扣">¥{{ detailData.couponAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="订单总额">¥{{ detailData.totalAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="积分抵扣">
          <span v-if="detailData.pointsDeductionAmount > 0">¥{{ detailData.pointsDeductionAmount }} ({{ detailData.usePointsAmount }}积分)</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付立减">¥{{ detailData.paymentDiscountAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="应付金额" style="color: #ff4d4f; font-weight: bold;">¥{{ detailData.paymentAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="实付金额" style="color: #67C23A; font-weight: bold;">¥{{ (detailData.paymentAmount || 0) - (detailData.paymentDiscountAmount || 0) }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ detailData.refundAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detailData.payChannelLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单人">{{ detailData.buyerPhone || '-' }}</el-descriptions-item>
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
          <el-descriptions-item label="审核状态">
            <el-tag>{{ offlineDeliveryData.auditStatusLabel || '已通过' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ offlineDeliveryData.applyTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ offlineDeliveryData.auditTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注说明" :span="2">{{ offlineDeliveryData.offlineRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="凭证图片" :span="2">
            <div v-if="offlineDeliveryData.offlineProof" style="display: flex; gap: 8px; flex-wrap: wrap;">
              <el-image
                v-for="(url, index) in offlineDeliveryData.offlineProof.split(',')"
                :key="index"
                :src="url"
                :preview-src-list="offlineDeliveryData.offlineProof.split(',')"
                fit="cover"
                style="width: 80px; height: 80px; cursor: pointer;"
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

        <el-descriptions-item label="确认收货时间">{{ detailData.receiveTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 订单商品列表 -->
      <el-divider>订单商品</el-divider>
      <el-table :data="detailData.orderItems || []" border>
        <el-table-column label="商品图片" width="80">
          <template #default="scope">
            <img :src="scope.row.picUrl" width="50" height="50" />
          </template>
        </el-table-column>
        <el-table-column prop="skuName" label="商品规格" min-width="160" />
        <el-table-column prop="spuId" label="商品ID" align="center" width="100" />
        <el-table-column prop="skuId" label="规格ID" align="center" width="100" />
        <el-table-column prop="supplierId" label="供应商ID" align="center" width="100" />
        <el-table-column label="单价" align="center">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
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
        <el-table-column label="运费" align="center">
          <template #default="scope">¥{{ scope.row.freightAmount || 0 }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 批量发货弹窗 -->
    <BatchDeliverDialog
      v-model="batchDeliverVisible"
      @success="handleBatchDeliverSuccess"
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
  getOrderDetail,
  getOrderLogistics,
  getOrderPage,
} from "@/api/oms/order";
import { getOfflineDeliveryDetail, submitOfflineDelivery } from "@/api/oms/offlineDelivery";
import { Order, OrderQuery } from "@/api/oms/order/types";
import { parseTime } from "@/utils/index";
import BatchDeliverDialog from "./components/BatchDeliverDialog.vue";
import { useRoute } from "vue-router";
import MultiUpload from "@/components/Upload/MultiUpload.vue";
import Dictionary from "@/components/Dictionary/index.vue";

// 批量发货弹窗控制
const batchDeliverVisible = ref(false);
const handleBatchDeliverSuccess = () => {
  handleQuery();
};

const state = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined,
    orderType: undefined,
    memberId: "",
    mchId: "",
    supplierId: "",
    spuId: "",
    phone: "",
    keywords: "",
    payChannel: "",
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
  } as OrderQuery;
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
interface LogisticsItem {
  carrierCode: string;
  carrierName: string;
}

const logisticsList = ref<LogisticsItem[]>([]);
const getLogistics = async () => {
  let res = await getOrderLogistics();
  if ((res as any).code == "00000") {
    logisticsList.value = (res as any).data;
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

// 发货弹框Tab控制
const deliverTab = ref("online");
const offlineDeliverInfo = ref({
  orderId: undefined as string | undefined,
  offlineProof: "",
  offlineRemark: "",
  carrierCode: "",
  carrierName: "",
  trackingNo: "",
});
const offlineDeliverRef = ref();
const offlineDeliverRules = reactive({
  offlineProof: [
    { required: true, message: "请上传发货凭证", trigger: "change" },
  ],
  offlineRemark: [
    { required: true, message: "请输入备注说明", trigger: "blur" },
  ],
});
const submitLoading = ref(false);

const handleClose = (done: () => void) => {
  deliverRef.value?.clearValidate();
  deliverRef.value?.resetFields();
  offlineDeliverRef.value?.clearValidate();
  offlineDeliverRef.value?.resetFields();
  deliverTab.value = "online";
  done();
};
const deliver = (row: Order) => {
  deliverInfo.value.orderNo = row.orderNo;
  deliverInfo.value.deliveryCode = "";
  deliverInfo.value.deliverySn = "";
  deliverInfo.value.remark = "";
  offlineDeliverInfo.value.offlineProof = "";
  offlineDeliverInfo.value.offlineRemark = "";
  offlineDeliverInfo.value.carrierCode = "";
  offlineDeliverInfo.value.carrierName = "";
  offlineDeliverInfo.value.trackingNo = "";
  offlineDeliverInfo.value.orderId = row.id;
  deliverTab.value = "online";
  dialogVisible.value = true;
};

const handleDeliver = async () => {
  if (deliverTab.value === "online") {
    // 线上发货
    if (!deliverRef.value) return;
    await deliverRef.value.validate((valid: boolean) => {
      if (valid) {
        deliveryOrderFun();
      }
    });
  } else {
    // 线下发货
    if (!offlineDeliverRef.value) return;
    await offlineDeliverRef.value.validate((valid: boolean) => {
      if (valid) {
        submitOfflineDeliveryFun();
      }
    });
  }
};

const deliveryOrderFun = async () => {
  let res = await deliveryOrder(deliverInfo.value);
  if ((res as any).code == "00000") {
    ElMessage({
      type: "success",
      message: "发货成功",
    });
    dialogVisible.value = false;
    handleQuery();
  }
};

// 获取物流公司名称
const getCarrierName = (carrierCode: string) => {
  if (!carrierCode) return "";
  const carrier = logisticsList.value.find(
    (item: any) => item.carrierCode === carrierCode
  );
  return carrier ? carrier.carrierName : "";
};

const submitOfflineDeliveryFun = async () => {
  submitLoading.value = true;
  try {
    const carrierName = getCarrierName(offlineDeliverInfo.value.carrierCode);
    // offlineProof可能是数组或字符串，转换为逗号分隔的字符串
    const offlineProof = Array.isArray(offlineDeliverInfo.value.offlineProof)
      ? offlineDeliverInfo.value.offlineProof.join(",")
      : offlineDeliverInfo.value.offlineProof;
    const res = await submitOfflineDelivery({
      orderId: offlineDeliverInfo.value.orderId as any,
      offlineProof: offlineProof,
      offlineRemark: offlineDeliverInfo.value.offlineRemark,
      carrierCode: offlineDeliverInfo.value.carrierCode || undefined,
      carrierName: carrierName || undefined,
      trackingNo: offlineDeliverInfo.value.trackingNo || undefined,
    });
    if ((res as any).code == "00000") {
      ElMessage.success("线下发货提交成功");
      dialogVisible.value = false;
      handleQuery();
    } else {
      ElMessage.error((res as any).message || "提交失败");
    }
  } catch (error: any) {
    ElMessage.error(error.message || "提交失败");
  } finally {
    submitLoading.value = false;
  }
};

interface TrackEventItem {
  status: string;
  updateTime: string;
  trackEvent: string;
}

const logisInfo = reactive({
  carrierName: "",
  trackingNo: "",
  createTime: "",
  orderNo: "",
  trackEvent: [] as TrackEventItem[],
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
  receiveTime: "",
  remark: "",
  orderItems: [],
});

// 线下发货数据
const offlineDeliveryData = reactive<any>({
  id: undefined as number | undefined,
  offlineProof: "",
  offlineRemark: "",
  applyTime: "",
  auditStatus: undefined as number | undefined,
  auditStatusLabel: "",
  auditor: "",
  auditTime: "",
  auditRemark: "",
});

// 查看订单详情

const viewDetail = async (row: any) => {
  let res = await getOrderDetail(row.id);
  if ((res as any).code == "00000" && (res as any).data) {
    Object.assign(detailData, (res as any).data);
    // 获取线下发货详情
    try {
      const offlineRes = await getOfflineDeliveryDetail(row.id);
      if ((offlineRes as any).code == "00000" && (offlineRes as any).data) {
        Object.assign(offlineDeliveryData, (offlineRes as any).data);
      } else {
        resetOfflineDeliveryData();
      }
    } catch (error) {
      resetOfflineDeliveryData();
    }
    detailDialog.visible = true;
  }
};

// 重置线下发货数据
const resetOfflineDeliveryData = () => {
  offlineDeliveryData.id = undefined;
  offlineDeliveryData.offlineProof = "";
  offlineDeliveryData.offlineRemark = "";
  offlineDeliveryData.applyTime = "";
  offlineDeliveryData.auditStatus = undefined;
  offlineDeliveryData.auditStatusLabel = "";
  offlineDeliveryData.auditor = "";
  offlineDeliveryData.auditTime = "";
  offlineDeliveryData.auditRemark = "";
};

// 查看物流信息
const viewMaterialflow = async (row: Order) => {
  let res = await getLogisticsByOrderId({ trackingNo: row.trackingNo });
  if ((res as any).code == "00000" && (res as any).data) {
    let data = (res as any).data;
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

onMounted(() => {
  const route = useRoute();
  if (route.query.keywords) {
    state.queryParams.keywords = route.query.keywords as string;
  }
  if (route.query.paymentNo) {
    state.queryParams.paymentNo = route.query.paymentNo as string;
  }
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

/* 检索区：控件统一等宽 + 流式排列（无 label，placeholder 提示） */
.search-container .el-input,
.search-container .el-select,
.search-container .el-cascader,
.search-container .el-date-editor {
  width: 190px;
}
</style>
