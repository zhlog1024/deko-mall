<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-container">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="支付单号">
          <el-input v-model="queryParams.paymentNo" placeholder="请输入支付单号" clearable />
        </el-form-item>
        <el-form-item label="会员ID">
          <el-input v-model="queryParams.memberId" placeholder="请输入会员ID" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="queryParams.auditStatus as any"
            class="filter-item"
            placeholder="请选择审核状态"
            clearable
          >
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
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
      </div>
    </div>

    <el-table v-loading="loading" :data="tableData" border size="small">
      <el-table-column align="center" label="支付单号" width="200" show-overflow-tooltip>
        <template #default="scope">
          <el-button type="primary" link size="small" @click="viewOrder(scope.row)">{{ scope.row.paymentNo }}</el-button>
        </template>
      </el-table-column>

      <el-table-column align="center" prop="memberId" label="会员ID" width="80" />

      <el-table-column align="center" label="支付金额" width="100">
        <template #default="scope">¥{{ scope.row.payAmount || 0 }}</template>
      </el-table-column>

      <el-table-column align="center" label="凭证" width="120">
        <template #default="scope">
          <div v-if="scope.row.voucherUrls" style="display:flex;gap:4px;justify-content:center;">
            <img
              v-for="(url, idx) in parseVouchers(scope.row.voucherUrls)"
              :key="idx"
              :src="url"
              style="width:36px;height:36px;cursor:pointer;border-radius:4px;object-fit:cover;"
              @click="previewVouchers(parseVouchers(scope.row.voucherUrls), idx)"
            />
          </div>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="审核状态" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.auditStatus === 0" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 1" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 2" type="danger" size="small">已驳回</el-tag>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="重传" width="50">
        <template #default="scope">{{ scope.row.resubmitCount || 0 }}</template>
      </el-table-column>

      <el-table-column align="center" prop="createTime" label="提交时间" width="155" />

      <el-table-column align="center" label="审核信息" min-width="160">
        <template #default="scope">
          <template v-if="scope.row.auditStatus !== 0">
            <div>{{ scope.row.auditor || '-' }}</div>
            <div style="color:#909399;font-size:12px">{{ scope.row.auditTime || '' }}</div>
            <div v-if="scope.row.auditRemark" style="color:#909399;font-size:12px">{{ scope.row.auditRemark }}</div>
          </template>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="220" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.auditStatus === 0">
            <el-button type="success" size="small" @click="handleAudit(scope.row, true)">通过</el-button>
            <el-button type="danger" size="small" @click="handleAudit(scope.row, false)">驳回</el-button>
          </template>
          <template v-else>
            <el-tag v-if="scope.row.auditStatus === 1" type="success" size="small" effect="plain">已通过</el-tag>
            <el-tag v-else type="danger" size="small" effect="plain">已驳回</el-tag>
          </template>
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
      @size-change="handleQuery"
      @current-change="handleQuery"
    />

    <!-- 审核弹窗（驳回时填写备注） -->
    <el-dialog v-model="auditDialog.visible" title="驳回" width="400px">
      <el-input
        v-model="auditDialog.remark"
        type="textarea"
        :rows="3"
        placeholder="请填写驳回理由"
      />
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 凭证预览 -->
    <el-dialog v-model="previewVisible" title="凭证预览" width="600px" destroy-on-close>
      <div style="text-align:center;">
        <img :src="previewUrl" style="max-width:100%;max-height:70vh;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getOfflinePayPage, auditOfflinePayment } from "@/api/oms/pay";
import { ElMessage } from "element-plus";

const router = useRouter();

const loading = ref(false);
const total = ref(0);
const tableData = ref<any[]>([]);
const dateRange = ref([]);

const previewVisible = ref(false);
const previewUrl = ref("");

const auditDialog = reactive({
  visible: false,
  remark: "",
  row: null as any,
});

const viewOrder = (row: any) => {
  router.push({ path: "/order", query: { paymentNo: row.paymentNo } });
};

const previewVouchers = (urls: string[], idx: number) => {
  previewUrl.value = urls[idx] || urls[0];
  previewVisible.value = true;
};

const queryParams = reactive({
  paymentNo: "",
  memberId: "",
  auditStatus: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const parseVouchers = (str: string) => {
  try { return JSON.parse(str) as string[]; } catch { return []; }
};

const handleQuery = async () => {
  loading.value = true;
  try {
    const params: any = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      auditStatus: queryParams.auditStatus,
    };
    if (queryParams.paymentNo) params.paymentNo = queryParams.paymentNo;
    if (queryParams.memberId) params.memberId = queryParams.memberId;
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0];
      params.endTime = dateRange.value[1];
    }
    const { data } = await getOfflinePayPage(params);
    tableData.value = data.list || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  queryParams.paymentNo = "";
  queryParams.memberId = "";
  queryParams.auditStatus = undefined;
  queryParams.pageNum = 1;
  dateRange.value = [];
  handleQuery();
};

const handleAudit = async (row: any, approved: boolean) => {
  if (approved) {
    try {
      await auditOfflinePayment(row.paymentNo, { approved: true });
      ElMessage.success("审核通过");
      handleQuery();
    } catch {
      ElMessage.error("操作失败");
    }
  } else {
    auditDialog.row = row;
    auditDialog.remark = "";
    auditDialog.visible = true;
  }
};

const confirmReject = async () => {
  if (!auditDialog.remark) {
    ElMessage.warning("请填写驳回理由");
    return;
  }
  try {
    await auditOfflinePayment(auditDialog.row.paymentNo, {
      approved: false,
      remark: auditDialog.remark,
    });
    ElMessage.success("已驳回");
    auditDialog.visible = false;
    handleQuery();
  } catch {
    ElMessage.error("操作失败");
  }
};

onMounted(() => handleQuery());
</script>
