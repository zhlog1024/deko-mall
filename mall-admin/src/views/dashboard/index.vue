<template>
  <div class="dashboard-container">
    <el-card shadow="never">
      <div class="flex items-center">
        <img
          v-if="avatarUrl"
          class="w-16 h-16 mr-4 rounded-full"
          :src="avatarUrl"
          alt="avatar"
        />
        <div>
          <p class="text-lg font-medium">{{ greetings }}</p>
          <p class="text-sm text-[var(--el-text-color-secondary)] mt-1">
            Deko Mall 运营后台。常用入口见左侧菜单：商品、订单、会员、页面装修、APP 配置。
          </p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12" class="mt-4">
      <el-col v-for="item in highlights" :key="item.title" :xs="24" :sm="12" :md="8">
        <el-card shadow="never" class="highlight-card">
          <div class="highlight-title">{{ item.title }}</div>
          <div class="highlight-desc">{{ item.desc }}</div>
        </el-card>
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

const userStore = useUserStore();

const avatarUrl = computed(() => {
  const avatar = userStore.user.avatar || "";
  return avatar ? `${avatar}?imageView2/1/w/80/h/80` : "";
});

const greetings = computed(() => {
  const hours = new Date().getHours();
  const name = userStore.user.nickname || "管理员";
  if (hours >= 6 && hours < 12) return `上午好，${name}`;
  if (hours >= 12 && hours < 18) return `下午好，${name}`;
  if (hours >= 18 && hours < 24) return `晚上好，${name}`;
  return `夜深了，${name}`;
});

const highlights = [
  { title: "APP 配置", desc: "站点品牌、主题、底栏、登录方式等，改完即对用户端生效。" },
  { title: "页面装修", desc: "DIY 拖拽首页与底部导航，支持多端预览。" },
  { title: "交易履约", desc: "商品、订单、支付、售后与会员资产一条链路跑通。" },
];
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 24px;
}

.highlight-card {
  margin-bottom: 12px;
  min-height: 110px;
}

.highlight-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.highlight-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
