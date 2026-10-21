<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量发货"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="batch-deliver-container">
      <!-- 步骤指示 -->
      <el-steps :active="step" finish-status="success" align-center class="steps">
        <el-step title="上传文件" />
        <el-step title="确认发货" />
        <el-step title="完成" />
      </el-steps>

      <!-- 步骤1：上传文件 -->
      <div v-show="step === 0" class="step-content">
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".xlsx,.xls"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 Excel 文件，请
                <el-link type="primary" @click="downloadTemplate">下载模板</el-link>
              </div>
            </template>
          </el-upload>
        </div>
        <div v-if="uploadError" class="error-tip">
          <el-icon><warning /></el-icon>
          {{ uploadError }}
        </div>
      </div>

      <!-- 步骤2：确认发货 -->
      <div v-show="step === 1" class="step-content">
        <div class="confirm-info">
          <el-icon class="info-icon"><info-filled /></el-icon>
          <span>确认发货 {{ fileData.length }} 个订单？</span>
        </div>
        <el-table :data="fileData" max-height="300" size="small" border>
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="deliveryCode" label="物流公司编码" width="120" />
          <el-table-column prop="deliverySn" label="物流单号" width="180" />
          <el-table-column prop="remark" label="备注" />
        </el-table>
      </div>

      <!-- 步骤3：完成 -->
      <div v-show="step === 2" class="step-content">
        <div class="result-area">
          <el-result
            :icon="result.successCount > 0 ? (result.failCount > 0 ? 'warning' : 'success') : 'error'"
            :title="resultTitle"
            :sub-title="resultSubTitle"
          >
            <template #extra>
              <div v-if="result.failItems && result.failItems.length > 0" class="fail-details">
                <h4>失败订单：</h4>
                <el-table :data="result.failItems" max-height="200" size="small" border>
                  <el-table-column prop="orderNo" label="订单编号" width="180" />
                  <el-table-column prop="reason" label="失败原因" />
                </el-table>
              </div>
            </template>
          </el-result>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="step === 0" @click="handleClose">取消</el-button>
        <el-button v-if="step === 1" @click="step = 0">上一步</el-button>
        <el-button
          v-if="step === 0 && fileData.length > 0"
          type="primary"
          @click="handleNext"
        >
          下一步
        </el-button>
        <el-button
          v-if="step === 1"
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          确认发货
        </el-button>
        <el-button v-if="step === 2" type="primary" @click="handleClose">完成</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from "vue";
import { ElMessage, type UploadFile, type UploadInstance } from "element-plus";
import { UploadFilled, Warning, InfoFilled } from "@element-plus/icons-vue";
import * as XLSX from "xlsx";
import { batchDelivery } from "@/api/oms/order";
import { BatchDeliverForm, BatchDeliverResult } from "@/api/oms/order/types";

const emit = defineEmits<{
  (e: "update:modelValue", visible: boolean): void;
  (e: "success"): void;
}>();

const props = defineProps<{
  modelValue: boolean;
}>();

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const step = ref(0);
const loading = ref(false);
const uploadRef = ref<UploadInstance>();
const file = ref<UploadFile | null>(null);
const fileData = ref<any[]>([]);
const uploadError = ref("");
const result = reactive<BatchDeliverResult>({
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  successOrderNos: [],
  failItems: [],
});

const resultTitle = computed(() => {
  if (result.successCount > 0 && result.failCount > 0) {
    return "部分发货成功";
  } else if (result.successCount > 0) {
    return "全部发货成功";
  } else {
    return "发货失败";
  }
});

const resultSubTitle = computed(() => {
  return `成功：${result.successCount} 个，失败：${result.failCount} 个`;
});

// 重置表单状态
const resetState = () => {
  step.value = 0;
  file.value = null;
  fileData.value = [];
  uploadError.value = "";
  result.totalCount = 0;
  result.successCount = 0;
  result.failCount = 0;
  result.successOrderNos = [];
  result.failItems = [];
  // 清空上传组件
  uploadRef.value?.clearFiles();
};

// 监听弹窗显示状态，关闭时重置
watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      resetState();
    }
  }
);

// 处理文件选择
const handleFileChange = (uploadFile: UploadFile) => {
  uploadError.value = "";
  file.value = uploadFile;

  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      const data = e.target?.result;
      const workbook = XLSX.read(data, { type: "array" });
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
      const jsonData = XLSX.utils.sheet_to_json(firstSheet);

      // 解析并验证数据
      const parsedData: any[] = [];
      for (let i = 0; i < jsonData.length; i++) {
        const row = jsonData[i] as any;
        // 支持多种列名格式
        const orderNo = row["订单编号"] || row["orderNo"] || row["订单号"];
        const deliveryCode = row["物流公司编码"] || row["deliveryCode"];
        const deliverySn = row["物流单号"] || row["deliverySn"] || row["快递单号"];
        const remark = row["备注"] || row["remark"];

        if (!orderNo) {
          uploadError.value = `第 ${i + 2} 行：缺少订单编号`;
          return;
        }
        if (!deliveryCode) {
          uploadError.value = `第 ${i + 2} 行：缺少物流公司编码`;
          return;
        }
        if (!deliverySn) {
          uploadError.value = `第 ${i + 2} 行：缺少物流单号`;
          return;
        }

        parsedData.push({
          orderNo: String(orderNo).trim(),
          deliveryCode: String(deliveryCode).trim(),
          deliverySn: String(deliverySn).trim(),
          remark: remark ? String(remark).trim() : "",
        });
      }

      if (parsedData.length === 0) {
        uploadError.value = "文件中没有有效数据";
        return;
      }

      fileData.value = parsedData;
    } catch (error) {
      uploadError.value = "解析文件失败，请检查文件格式";
      console.error("Parse Excel error:", error);
    }
  };
  reader.readAsArrayBuffer(uploadFile.raw!);
};

// 移除文件
const handleFileRemove = () => {
  file.value = null;
  fileData.value = [];
  uploadError.value = "";
};

// 下载模板
const downloadTemplate = () => {
  // 物流公司编码说明
  const deliveryCompanies = [
    { code: "shunfeng", name: "顺丰速运" },
    { code: "shunfengkuaiyun", name: "顺丰快运" },
    { code: "jtexpress", name: "极兔速递" },
    { code: "yuantong", name: "圆通速递" },
    { code: "zhongtong", name: "中通快递" },
    { code: "zhongtongkuaiyun", name: "中通快运" },
    { code: "yunda", name: "韵达快递" },
    { code: "yundakuaiyun", name: "韵达快运" },
    { code: "shentong", name: "申通快递" },
    { code: "youzhengguonei", name: "邮政快递包裹" },
    { code: "youzhengdsbk", name: "邮政电商标快" },
    { code: "jd", name: "京东物流" },
    { code: "jingdongkuaiyun", name: "京东快运" },
    { code: "ems", name: "EMS" },
    { code: "debangkuaidi", name: "德邦快递" },
    { code: "youzhengbk", name: "邮政标准快递" },
    { code: "danniao", name: "菜鸟速递" },
    { code: "debangwuliu", name: "德邦物流" },
    { code: "kuayue", name: "跨越速运" },
    { code: "annengwuliu", name: "安能快运" },
    { code: "jinguangsudikuaijian", name: "京广速递" },
    { code: "baishiwuliu", name: "百世快运" },
    { code: "sxjdfreight", name: "顺心捷达" },
    { code: "annto", name: "安得物流" },
  ];

  // 创建工作簿
  const wb = XLSX.utils.book_new();

  // Sheet1: 批量发货模板
  const templateData = [
    {
      订单编号: "OD2026012272616960",
      物流公司编码: "shunfeng",
      物流单号: "SF1234567890",
      备注: "",
    }
  ];
  const ws1 = XLSX.utils.json_to_sheet(templateData);
  XLSX.utils.book_append_sheet(wb, ws1, "批量发货模板");

  // Sheet2: 物流公司编码说明
  const companyData = deliveryCompanies.map((item) => ({
    编码: item.code,
    物流公司名称: item.name,
  }));
  const ws2 = XLSX.utils.json_to_sheet(companyData);
  XLSX.utils.book_append_sheet(wb, ws2, "物流公司编码说明");

  // 下载文件
  XLSX.writeFile(wb, "批量发货模板.xlsx");
};

// 下一步
const handleNext = () => {
  if (fileData.value.length === 0) {
    ElMessage.warning("请先上传发货文件");
    return;
  }
  step.value = 1;
};

// 提交发货
const handleSubmit = async () => {
  loading.value = true;
  try {
    const form: BatchDeliverForm = {
      items: fileData.value,
    };
    const res = await batchDelivery(form);
    Object.assign(result, res.data);
    step.value = 2;
    emit("success");

    if (result.failCount === 0) {
      ElMessage.success(`批量发货成功，共 ${result.successCount} 个订单`);
    } else {
      ElMessage.warning(`部分发货成功，成功：${result.successCount}，失败：${result.failCount}`);
    }
  } catch (error: any) {
    ElMessage.error(error.message || "批量发货失败");
    console.error("Batch delivery error:", error);
  } finally {
    loading.value = false;
  }
};

// 关闭弹窗
const handleClose = () => {
  dialogVisible.value = false;
};
</script>

<style scoped lang="scss">
.batch-deliver-container {
  padding: 20px 0;
}

.steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 200px;
}

.upload-area {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.upload-demo {
  width: 100%;
}

.error-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f56c6c;
  margin-top: 20px;
  font-size: 14px;

  .el-icon {
    margin-right: 5px;
  }
}

.confirm-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 16px;

  .info-icon {
    color: #409eff;
    margin-right: 10px;
    font-size: 20px;
  }
}

.result-area {
  padding: 20px 0;
}

.fail-details {
  margin-top: 20px;
  text-align: left;

  h4 {
    margin-bottom: 10px;
    color: #f56c6c;
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>

