<template>
  <el-card class="config-card ext-config-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span><i-ep-money class="header-icon" /> 支付宝扩展配置</span>
      </div>
    </template>

    <el-form-item label="应用私钥" prop="parsedExtConfig.appPrivateKey">
      <el-input v-model="config.appPrivateKey" type="textarea" :rows="4" placeholder="请输入应用私钥" />
    </el-form-item>

    <el-form-item label="支付宝公钥" prop="parsedExtConfig.alipayPublicKey">
      <el-input v-model="config.alipayPublicKey" type="textarea" :rows="4" placeholder="请输入支付宝公钥" />
    </el-form-item>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';

interface AlipayExtConfig {
  appPrivateKey: "",
  alipayPublicKey: string;
}

const props = defineProps<{ extConfig?: string }>();
const emit = defineEmits<{ (e: 'update:extConfig', val: object): void }>();

const config = reactive<AlipayExtConfig>({ appPrivateKey: '', alipayPublicKey: '' });

const parse = (val?: string | object) => {
  if (!val) return;
  try {
    const obj = typeof val === 'string' ? JSON.parse(val) : val;
    if (obj.appPrivateKey !== undefined) config.appPrivateKey = obj.appPrivateKey;
    if (obj.alipayPublicKey !== undefined) config.alipayPublicKey = obj.alipayPublicKey;
  } catch {}
};

watch(() => props.extConfig, parse, { immediate: true });
watch(config, () => emit('update:extConfig', { ...config }), { deep: true });
</script>

<style scoped>
.config-card { margin-bottom: 20px; border-radius: 8px; }
.ext-config-card { margin-top: 25px; border-left: 4px solid #409eff; }
.card-header { display: flex; align-items: center; font-weight: bold; }
.header-icon { margin-right: 8px; }
</style>
