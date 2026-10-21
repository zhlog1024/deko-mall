<template>
  <div class="dashboard-container">
    <!-- 供应商信息与资产统计 -->
    <el-row :gutter="10">
      <el-col :xs="24" :sm="24" :lg="12">
        <supplier-info-card
          :loading="loading.supplier"
          :supplier-info="supplierInfo"
          @refresh="loadSupplierInfo"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "Dashboard",
  inheritAttrs: false,
});

import { useUserStore } from "@/store/modules/user";
import { useRouter } from "vue-router";
import { getSupplier } from "@/api/pms/supplier";

// 引入自定义组件
import SupplierInfoCard from "./components/SupplierInfoCard.vue";

const router = useRouter();
const userStore = useUserStore();
const date = new Date();

// 加载状态
const loading = reactive({
  supplier: true,
  todo: true,
  productRanking: true,
  salesTrend: true,
});

// 供应商基本信息
const supplierInfo = ref({
  id: 0,
  supplierName: "",
  supplierCode: "",
  contactName: "",
  contactPhone: "",
  auditStatus: 0,
  auditStatusLabel: "",
  createTime: "",
  settlement: {
    bankName: "",
    accountName: "",
    bankAccount: "",
  },
});


// 页面初始化时加载所有数据
onMounted(() => {
  loadSupplierInfo();
});

// 加载供应商基本信息
function loadSupplierInfo() {
  loading.supplier = true;
  getSupplier()
    .then(({ data }) => {
      if (data) {
        supplierInfo.value = data;
      }
      loading.supplier = false;
    })
    .catch((error) => {
      console.error("加载供应商信息失败:", error);
      loading.supplier = false;
    });
}

</script>

<style lang="scss" scoped>
.dashboard-container {
  position: relative;
  padding: 24px;

  .user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
  }

  .github-corner {
    position: absolute;
    top: 0;
    right: 0;
    z-index: 1;
    border: 0;
  }

  .data-box {
    display: flex;
    justify-content: space-between;
    padding: 20px;
    font-weight: bold;
    color: var(--el-text-color-regular);
    background: var(--el-bg-color-overlay);
    border-color: var(--el-border-color);
    box-shadow: var(--el-box-shadow-dark);
  }

  .svg-icon {
    fill: currentcolor !important;
  }
}
</style>
