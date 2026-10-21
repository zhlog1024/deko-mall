<template>
  <el-card class="config-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span><i-ep-money class="header-icon" /> 银行账户信息</span>
      </div>
    </template>

    <el-form-item label="银行名称">
      <el-input v-model="localExtConfig.bankName" placeholder="如：中国工商银行" />
    </el-form-item>

    <el-form-item label="收款户名">
      <el-input v-model="localExtConfig.accountName" placeholder="如：示例商城科技有限公司" />
    </el-form-item>

    <el-form-item label="收款卡号">
      <el-input v-model="localExtConfig.accountNo" placeholder="如：6222000011112222333" />
    </el-form-item>

    <el-form-item label="收款码图片">
      <single-upload v-model="localExtConfig.qrcode" :width="120" :height="120" />
      <div class="form-tip">上传银行收款二维码</div>
    </el-form-item>

    <el-form-item label="转账提示语">
      <el-input
        v-model="localExtConfig.tips"
        type="textarea"
        :rows="2"
        placeholder="如：转账时请备注订单号，到账后管理员将尽快审核"
      />
    </el-form-item>
  </el-card>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import SingleUpload from "@/components/Upload/SingleUpload.vue";

const props = defineProps<{
  extConfig: Record<string, any>;
}>();

const emit = defineEmits<{
  "update:extConfig": [value: Record<string, any>];
}>();

const localExtConfig = computed({
  get: () => props.extConfig || {},
  set: (val) => emit("update:extConfig", val),
});

// 初始化默认值
watch(
  () => props.extConfig,
  (val) => {
    if (!val || Object.keys(val).length === 0) {
      emit("update:extConfig", {
        bankName: "",
        accountName: "",
        accountNo: "",
        qrcode: "",
        tips: "转账时请备注订单号，到账后管理员将尽快审核",
      });
    }
  },
  { immediate: true }
);
</script>

<style lang="scss" scoped>
.config-card {
  margin-bottom: 20px;
  border-radius: 8px;
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
.form-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}
</style>
