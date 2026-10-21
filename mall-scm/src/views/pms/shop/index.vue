<script setup lang="ts">
defineOptions({
  name: "Shop",
  inheritAttrs: false,
});

import { getMyShop, updateMyShop } from "@/api/pms/shop";
import { Shop, ShopUpdateForm } from "@/api/pms/shop/types";
import SingleUpload from "@/components/Upload/SingleUpload.vue";
import { ElMessage } from "element-plus";

const formRef = ref(ElForm);
const loading = ref(false);
const saving = ref(false);

const formData = reactive<ShopUpdateForm>({
  shopName: "",
  logo: "",
  logoLong: "",
  description: "",
});

// 表单校验规则
const rules = {
  shopName: [
    { required: true, message: "请输入店铺名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符之间", trigger: "blur" },
  ],
  logo: [{ required: true, message: "请上传店铺Logo", trigger: "change" }],
};

// 加载我的店铺信息
function fetchShop() {
  loading.value = true;
  getMyShop()
    .then(({ data }) => {
      const shop: Shop = data;
      formData.shopName = shop.shopName || "";
      formData.logo = shop.logo || "";
      formData.logoLong = shop.logoLong || "";
      formData.description = shop.description || "";
    })
    .catch(() => {
      ElMessage.error("获取店铺信息失败");
    })
    .finally(() => {
      loading.value = false;
    });
}

// 保存
function handleSave() {
  formRef.value.validate((valid: boolean) => {
    if (!valid) return;
    saving.value = true;
    updateMyShop({
      shopName: formData.shopName,
      logo: formData.logo,
      logoLong: formData.logoLong,
      description: formData.description,
    })
      .then(() => {
        ElMessage.success("店铺信息保存成功");
        fetchShop();
      })
      .catch(() => {
        ElMessage.error("保存失败，请重试");
      })
      .finally(() => {
        saving.value = false;
      });
  });
}

onMounted(() => {
  fetchShop();
});
</script>

<template>
  <div class="app-container">
    <el-card v-loading="loading" class="shop-card">
      <template #header>
        <div class="card-header">
          <span>店铺设置</span>
          <span class="header-tip">店铺信息将在用户端 App 店铺页面展示，请及时完善，避免店铺信息为空</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="shop-form"
      >
        <el-form-item label="店铺名称" prop="shopName">
          <el-input
            v-model="formData.shopName"
            placeholder="请输入店铺名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="店铺Logo" prop="logo">
          <single-upload v-model="formData.logo" :width="120" :height="120" />
          <div class="form-item-help">建议尺寸 200×200，将展示在用户端店铺页头部</div>
        </el-form-item>

        <el-form-item label="店铺横幅" prop="logoLong">
          <single-upload v-model="formData.logoLong" :width="300" :height="100" />
          <div class="form-item-help">选填，建议尺寸 750×250</div>
        </el-form-item>

        <el-form-item label="店铺简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入店铺简介，介绍店铺主营与特色"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.shop-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-tip {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .shop-form {
    max-width: 640px;
  }

  .form-item-help {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}
</style>
