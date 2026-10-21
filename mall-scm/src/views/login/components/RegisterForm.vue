<template>
  <div class="register-form">
    <el-form
      ref="registerFormRef"
      :model="registerData"
      :rules="registerRules"
      label-width="120px"
    >
      <!-- 登录账号 -->
      <el-form-item label="登录账号" prop="loginName">
        <el-input
          v-model="registerData.loginName"
          placeholder="请输入登录账号"
          autocomplete="off"
        />
      </el-form-item>

      <!-- 供应商名称 -->
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input
          v-model="registerData.supplierName"
          placeholder="请输入供应商名称"
          autocomplete="off"
        />
      </el-form-item>

      <!-- 密码 -->
      <el-form-item label="登录密码" prop="password">
        <el-input
          v-model="registerData.password"
          type="password"
          placeholder="请输入登录密码"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="registerData.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <!-- 营业执照 -->
      <el-form-item label="营业执照" prop="businessLicense">
        <div class="license-upload-container">
          <single-upload v-model="registerData.businessLicense" />
          <div v-if="registerData.businessLicense" class="license-preview">
            <el-image
              :src="registerData.businessLicense"
              fit="cover"
              class="license-thumbnail"
              :preview-src-list="[registerData.businessLicense]"
            />
          </div>
        </div>
        <div class="form-item-help">上传营业执照图片，支持JPG、PNG格式</div>
      </el-form-item>

      <!-- 开户银行 -->
      <el-form-item label="开户银行" prop="settlementForm.bankName">
        <el-input
          v-model="registerData.settlementForm.bankName"
          placeholder="请输入开户银行"
          autocomplete="off"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 开户名 -->
      <el-form-item label="开户名" prop="settlementForm.accountName">
        <el-input
          v-model="registerData.settlementForm.accountName"
          placeholder="请输入开户名"
          autocomplete="off"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 银行账号 -->
      <el-form-item label="银行账号" prop="settlementForm.bankAccount">
        <el-input
          v-model="registerData.settlementForm.bankAccount"
          placeholder="请输入银行账号"
          autocomplete="off"
          maxlength="21"
          show-word-limit
        />
      </el-form-item>

      <!-- 店铺信息 -->
      <el-divider content-position="left">店铺信息</el-divider>
      <el-alert
        title="店铺信息将在审核通过后展示在用户端 App 店铺页面，请提前完善，避免店铺信息为空"
        type="info"
        :closable="false"
        class="shop-tip"
      />

      <!-- 店铺Logo -->
      <el-form-item label="店铺Logo" prop="shopLogo">
        <single-upload
          v-model="registerData.shopLogo"
          :style="{ width: '100px', height: '100px' }"
        />
        <div class="form-item-help">建议尺寸 200×200，将展示在用户端店铺页头部</div>
      </el-form-item>

      <!-- 店铺名称 -->
      <el-form-item label="店铺名称" prop="shopName">
        <el-input
          v-model="registerData.shopName"
          placeholder="默认与供应商名称一致，可修改"
          autocomplete="off"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 店铺简介 -->
      <el-form-item label="店铺简介" prop="shopDescription">
        <el-input
          v-model="registerData.shopDescription"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="选填，介绍店铺主营与特色"
        />
      </el-form-item>

      <!-- 注册按钮 -->
      <el-form-item>
        <el-button
          :loading="loading"
          type="primary"
          class="w-full"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form-item>

      <!-- 返回登录 -->
      <el-form-item>
        <div class="text-center">
          <span class="text-sm text-gray-500">已有账号？</span>
          <el-button type="text" @click="switchToLogin">返回登录</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "RegisterForm",
  inheritAttrs: false,
});

import { addSupplier } from "@/api/pms/supplier";
import { getCaptchaApi } from "@/api/auth";
import SingleUpload from "@/components/Upload/SingleUpload.vue";
import { ElMessage } from "element-plus";

const registerFormRef = ref<any>(null);
const emit = defineEmits(["register-success", "switch-to-login"]);

// 响应式状态
const loading = ref(false);
const captchaBase64 = ref("");

// 注册表单数据
const registerData = ref({
  loginName: "",
  supplierName: "",
  password: "",
  confirmPassword: "",
  businessLicense: "",
  shopName: "",
  shopLogo: "",
  shopDescription: "",
  settlementForm: {
    bankName: "",
    accountName: "",
    bankAccount: "",
  },
});

// 表单验证规则
const registerRules = ref({
  loginName: [
    { required: true, message: "请输入登录账号", trigger: "blur" },
    { min: 4, max: 20, message: "长度在 4 到 20 个字符之间", trigger: "blur" },
  ],
  supplierName: [
    { required: true, message: "请输入供应商名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符之间", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符之间", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入密码", trigger: "blur" },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== registerData.value.password) {
          callback(new Error("两次输入密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  businessLicense: [
    { required: true, message: "请上传营业执照", trigger: "blur" },
  ],
  shopLogo: [
    { required: true, message: "请上传店铺Logo", trigger: "change" },
  ],
  "settlementForm.bankName": [
    { required: true, message: "请输入开户银行", trigger: "blur" },
    { minLength: 2, maxLength: 50, message: "开户银行长度在 2 到 50 个字符", trigger: "blur" },
  ],
  "settlementForm.accountName": [
    { required: true, message: "请输入开户名", trigger: "blur" },
    { minLength: 2, maxLength: 50, message: "开户名长度在 2 到 50 个字符", trigger: "blur" },
  ],
  "settlementForm.bankAccount": [
    { required: true, message: "请输入银行账号", trigger: "blur" },
    { pattern: /^\d{6,21}$/, message: "银行账号必须是6-21位数字", trigger: "blur" },
  ],
});

// 注册处理
function handleRegister() {
  registerFormRef.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true;

      // 构建提交数据
      const submitData = {
        loginName: registerData.value.loginName,
        supplierName: registerData.value.supplierName,
        password: registerData.value.password,
        confirmPassword: registerData.value.confirmPassword,
        businessLicense: registerData.value.businessLicense,
        // 店铺名称未填写时默认取供应商名称
        shopName: registerData.value.shopName || registerData.value.supplierName,
        shopLogo: registerData.value.shopLogo,
        shopDescription: registerData.value.shopDescription,
        settlementForm: {
          bankName: registerData.value.settlementForm.bankName,
          accountName: registerData.value.settlementForm.accountName,
          bankAccount: registerData.value.settlementForm.bankAccount,
        },
      };

      addSupplier(submitData)
        .then(() => {
          ElMessage.success("注册成功，请等待管理员审核");
          emit("register-success");
          emit("switch-to-login"); // 切换回登录页
        })
        .catch(() => {
          // 获取新验证码
        })
        .finally(() => {
          loading.value = false;
        });
    }
  });
}

// 切换到登录页
function switchToLogin() {
  emit("switch-to-login");
}
</script>

<style lang="scss" scoped>
.register-form {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 30px;

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  :deep(.el-input__wrapper) {
    height: 40px;
  }

  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  .captcha-container {
    display: flex;
    align-items: center;

    .el-input {
      flex: 1;
      margin-right: 15px;
    }

    .captcha-image {
      height: 40px;
      cursor: pointer;
      border-radius: 4px;
    }
  }

  .license-upload-container {
    display: flex;
    align-items: center;
  }

  .license-preview {
    margin-left: 15px;
  }

  .license-thumbnail {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    border: 1px solid var(--el-border-color);
    box-shadow: var(--el-box-shadow-lighter);
  }

  .form-item-help {
    margin-top: 6px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .shop-tip {
    margin-bottom: 20px;
  }
}
</style>
