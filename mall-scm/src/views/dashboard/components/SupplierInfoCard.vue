<template>
  <el-card shadow="never" class="supplier-info-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="text-[var(--el-text-color-secondary)]">供应商信息</span>
        <el-tag type="success">基本信息</el-tag>
      </div>
    </template>

    <div v-if="loading" class="my-4 flex justify-center">
      <el-skeleton style="width: 100%" :rows="5" animated />
    </div>

    <div v-else-if="!supplierInfo.id" class="no-data-container">
      <el-empty description="暂无供应商信息" />
      <div class="mt-4 flex justify-center">
        <el-button type="primary" @click="handleGoToSupplier">
          <i-ep-plus class="mr-1" />
          前往注册
        </el-button>
      </div>
    </div>

    <div v-else class="supplier-info-content">
      <el-row :gutter="20">
        <el-col :span="16">
          <!-- 基本信息区域 -->
          <div class="info-section">
            <div class="section-title">基本信息</div>
            <div class="info-row">
              <div class="info-label">供应商名称:</div>
              <div class="info-value">{{ supplierInfo.supplierName }}</div>
            </div>

            <div class="info-row">
              <div class="info-label">供应商账号:</div>
              <div class="info-value">{{ supplierInfo.loginName || "未设置" }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">审核状态:</div>
              <div class="info-value">
                <el-tag
                  :type="
                    supplierInfo.auditStatus === 1
                      ? 'success'
                      : supplierInfo.auditStatus === 2
                        ? 'danger'
                        : 'warning'
                  "
                  size="small"
                >
                  {{ supplierInfo.auditStatusLabel }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <!-- 结算信息区域 -->
          <div class="info-section mt-4">
            <div class="section-title">结算信息</div>
            <div class="info-row">
              <div class="info-label">开户银行:</div>
              <div class="info-value">
                {{ supplierInfo.settlement?.bankName || "未设置" }}
              </div>
            </div>
            <div class="info-row">
              <div class="info-label">开户名:</div>
              <div class="info-value">
                {{ supplierInfo.settlement?.accountName || "未设置" }}
              </div>
            </div>
            <div class="info-row">
              <div class="info-label">银行账号:</div>
              <div class="info-value">
                {{ supplierInfo.settlement?.bankAccount || "未设置" }}
              </div>
            </div>
          </div>
        </el-col>
        
        <!-- 营业执照展示区域 -->
        <el-col :span="8">
          <div class="license-section">
            <div class="section-title">营业执照</div>
            <div v-if="supplierInfo.businessLicense" class="license-content">
              <el-image
                :src="supplierInfo.businessLicense"
                fit="contain"
                :preview-src-list="[supplierInfo.businessLicense]"
                :initial-index="0"
                class="license-image"
                alt="营业执照"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><icon-picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            <el-empty v-else description="暂无营业执照" :image-size="80" />
          </div>
        </el-col>
      </el-row>

      <div class="mt-4 flex justify-end">
        <el-button type="primary" size="small" @click="handleGoToSupplierEdit">
          <i-ep-edit class="mr-1" />
          编辑信息
        </el-button>
      </div>
    </div>
  </el-card>

  <!-- 编辑弹窗 -->
  <el-dialog
    v-model="state.dialog.visible"
    :title="state.dialog.title"
    width="600px"
  >
    <el-form
      ref="dataFormRef"
      :model="state.formData"
      label-width="100px"
      :rules="rules"
    >
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input
          v-model="state.formData.supplierName"
          placeholder="请输入供应商名称"
        />
      </el-form-item>

      <el-form-item label="供应商账号" prop="loginName">
        <el-input
          v-model="state.formData.loginName"
          placeholder="请输入登录账号"
          disabled
        />
      </el-form-item>

      <el-form-item label="营业执照" prop="businessLicense">
        <div class="license-upload-container">
          <single-upload v-model="state.formData.businessLicense" />
          <div v-if="state.formData.businessLicense" class="license-preview">
            <el-image 
              :src="state.formData.businessLicense" 
              fit="cover" 
              class="license-thumbnail"
              :preview-src-list="[state.formData.businessLicense]"
            />
          </div>
        </div>
        <div class="form-item-help">上传营业执照图片，支持JPG、PNG格式</div>
      </el-form-item>


      <el-form-item label="开户银行" prop="settlementForm.bankName" :rules="rules.bankName">
        <el-input
          v-model="state.formData.settlementForm.bankName"
          placeholder="请输入开户银行"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="开户名" prop="settlementForm.accountName" :rules="rules.accountName">
        <el-input
          v-model="state.formData.settlementForm.accountName"
          placeholder="请输入开户名"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="银行账号" prop="settlementForm.bankAccount" :rules="rules.bankAccount">
        <el-input
          v-model="state.formData.settlementForm.bankAccount"
          placeholder="请输入银行账号"
          maxlength="21"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
defineOptions({
  name: "SupplierInfoCard",
  inheritAttrs: false,
});

import { useRouter } from "vue-router";
import { Picture as IconPicture } from '@element-plus/icons-vue';
import { updateSupplier } from "@/api/pms/supplier";

const router = useRouter();
const dataFormRef = ref();

const props = defineProps({
  loading: {
    type: Boolean,
    default: false,
  },
  supplierInfo: {
    type: Object,
    default: () => ({}),
  },
});

// 创建响应式状态
const state = reactive({
  dialog: {
    visible: false,
    title: "",
  },
  formData: {
    id: 0,
    supplierName: "",
    supplierCode: "",
    loginName: "",
    businessLicense: "",
    settlementForm: {
      id: 0,
      supplierId: 0,
      bankName: "",
      accountName: "",
      bankAccount: "",
    },
  },
});

// 前往供应商注册
function handleGoToSupplier() {
  router.push({ path: "/pms/supplier" });
}

// 前往编辑供应商信息
function handleGoToSupplierEdit() {
  // 打开编辑弹窗而不是跳转页面
  state.dialog = { visible: true, title: "编辑供应商信息" };

  // 复制当前数据到表单
  state.formData = {
    id: props.supplierInfo.id,
    supplierName: props.supplierInfo.supplierName,
    supplierCode: props.supplierInfo.supplierCode,
    loginName: props.supplierInfo.loginName || "",
    businessLicense: props.supplierInfo.businessLicense || "",
    settlementForm: {
      id: props.supplierInfo.settlement?.id || 0,
      supplierId: props.supplierInfo.id,
      bankName: props.supplierInfo.settlement?.bankName || "",
      accountName: props.supplierInfo.settlement?.accountName || "",
      bankAccount: props.supplierInfo.settlement?.bankAccount || "",
    },
  };
}

// 提交表单
function submitForm() {
  dataFormRef.value.validate((isValid) => {
    if (isValid) {
      updateSupplier(state.formData.id, state.formData)
        .then(() => {
          ElMessage.success("修改成功");
          state.dialog.visible = false;
          // 刷新数据
          emit("refresh");
        })
        .catch((error) => {
          console.error("更新供应商信息失败:", error);
          ElMessage.error("更新失败，请稍后重试");
        });
    }
  });
}

// 取消编辑
function cancel() {
  state.dialog.visible = false;
  dataFormRef.value?.resetFields();
}

// 定义事件
const emit = defineEmits(["refresh"]);

// 表单校验规则
const rules = {
  supplierName: [
    { required: true, message: "请输入供应商名称", trigger: "blur" },
  ],
  bankName: [
    { required: true, message: "请输入开户银行", trigger: "blur" },
    { minLength: 2, maxLength: 50, message: "开户银行长度在 2 到 50 个字符", trigger: "blur" },
  ],
  accountName: [
    { required: true, message: "请输入开户名", trigger: "blur" },
    { minLength: 2, maxLength: 50, message: "开户名长度在 2 到 50 个字符", trigger: "blur" },
  ],
  bankAccount: [
    { required: true, message: "请输入银行账号", trigger: "blur" },
    { pattern: /^\d{6,21}$/, message: "银行账号必须是6-21位数字", trigger: "blur" },
  ],
};
</script>

<style lang="scss" scoped>
.supplier-info-card {
  .supplier-info-content {
    padding: 8px 0;
  }

  .info-section {
    padding: 15px;
    border-radius: 4px;
    background-color: var(--el-bg-color-page);
    box-shadow: var(--el-box-shadow-lighter);
  }

  .section-title {
    margin-bottom: 15px;
    padding-bottom: 8px;
    font-size: 16px;
    font-weight: bold;
    color: var(--el-color-primary);
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .info-row {
    display: flex;
    margin-bottom: 15px;
    font-size: 14px;
    line-height: 20px;
  }

  .info-label {
    width: 100px;
    color: var(--el-text-color-secondary);
  }

  .info-value {
    flex: 1;
    color: var(--el-text-color-primary);
    font-weight: 500;
  }

  .license-section {
    height: 100%;
    padding: 15px;
    border-radius: 4px;
    background-color: var(--el-bg-color-page);
    box-shadow: var(--el-box-shadow-lighter);
  }

  .license-content {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 10px;
  }

  .license-image {
    width: 100%;
    max-height: 240px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    cursor: pointer;
  }

  .image-error {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 150px;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }

  .license-upload-container {
    display: flex;
    align-items: center;
  }

  .license-preview {
    margin-left: 10px;
  }

  .license-thumbnail {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    border: 1px solid var(--el-border-color);
  }

  .form-item-help {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .no-data-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 200px;
  }
}
</style>
