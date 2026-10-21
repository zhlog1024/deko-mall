<template>
  <el-card class="config-card ext-config-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span><i-ep-money class="header-icon" /> 微信支付扩展配置</span>
      </div>
    </template>

    <el-form-item label="API V3密钥" prop="parsedExtConfig.apiV3Key">
      <el-input v-model="config.apiV3Key" placeholder="请输入API V3密钥" />
    </el-form-item>

    <el-form-item label="商户证书序列号" prop="parsedExtConfig.merchantSerialNo">
      <el-input v-model="config.merchantSerialNo" placeholder="请输入商户证书序列号" />
    </el-form-item>

    <el-form-item label="商户私钥" prop="parsedExtConfig.merchantPrivateKey">
      <el-input v-model="config.merchantPrivateKey" type="textarea" :rows="4" placeholder="请输入商户私钥（PEM格式）" />
    </el-form-item>

    <el-form-item label="平台证书序列号" prop="parsedExtConfig.platformCertSerialNo">
      <el-input v-model="config.platformCertSerialNo" placeholder="请输入平台证书序列号" />
    </el-form-item>

    <el-form-item label="平台证书" prop="parsedExtConfig.platformCert">
      <el-input v-model="config.platformCert" type="textarea" :rows="4" placeholder="请输入平台证书内容（PEM格式）" />
    </el-form-item>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';

interface WechatExtConfig {
  apiV3Key: "",
  merchantPrivateKey: "",
  merchantSerialNo: string;
  platformCert: string;
  platformCertSerialNo: string;
}

const props = defineProps<{ extConfig?: string }>();
const emit = defineEmits<{ (e: 'update:extConfig', val: object): void }>();

const config = reactive<WechatExtConfig>({
  apiV3Key: '', merchantPrivateKey: '', merchantSerialNo: '', platformCert: '', platformCertSerialNo: ''
});

const parse = (val?: string | object) => {
  if (!val) return;
  try {
    const obj = typeof val === 'string' ? JSON.parse(val) : val;
    Object.keys(config).forEach(k => {
      if (obj[k] !== undefined) (config as any)[k] = obj[k];
    });
  } catch {}
};

watch(() => props.extConfig, parse, { immediate: true });
watch(config, () => emit('update:extConfig', { ...config }), { deep: true });
</script>

<style scoped>
.config-card { margin-bottom: 20px; border-radius: 8px; }
.ext-config-card { margin-top: 25px; border-left: 4px solid #67c23a; }
.card-header { display: flex; align-items: center; font-weight: bold; }
.header-icon { margin-right: 8px; }
</style>
