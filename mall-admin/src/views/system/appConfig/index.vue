<template>
  <div class="app-config-container">
    <el-row :gutter="20">
      <!-- 左侧分组导航 -->
      <el-col :span="5">
        <div class="group-sidebar">
          <div
            v-for="group in groups"
            :key="group.groupCode"
            :class="['group-item', { active: activeGroup === group.groupCode }]"
            @click="activeGroup = group.groupCode"
          >
            {{ group.groupName }}
          </div>
        </div>
      </el-col>

      <!-- 右侧配置表单 -->
      <el-col :span="19">
        <div class="config-form" v-if="currentGroup">
          <el-alert
            :title="currentGroup.groupName"
            type="info"
            :closable="false"
            style="margin-bottom: 20px"
          />

          <el-form label-width="200px" ref="formRef">
            <el-card
              v-for="item in currentGroup.items"
              :key="item.configKey"
              shadow="never"
              class="config-card"
            >
              <el-form-item :label="item.configName + '：'">
                <!-- 开关类型：0/1 布尔值 -->
                <template v-if="item.valueType === 'boolean'">
                  <el-switch
                    v-model="formData[item.configKey]"
                    :active-value="1"
                    :inactive-value="0"
                  />
                  <span class="switch-label">
                    {{ formData[item.configKey] === 1 ? '开启' : '关闭' }}
                  </span>
                </template>

                <!-- 下拉选择 -->
                <el-select
                  v-else-if="item.valueType === 'select'"
                  v-model="formData[item.configKey]"
                  style="width: 260px"
                >
                  <el-option
                    v-for="opt in getSelectOptions(item.configKey)"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>

                <!-- 图片类型 -->
                <template v-else-if="item.valueType === 'image'">
                  <div class="image-input-row">
                    <el-input
                      v-model="formData[item.configKey]"
                      placeholder="请输入图片URL"
                      style="width: 400px"
                    />
                    <el-image
                      v-if="formData[item.configKey]"
                      :src="formData[item.configKey]"
                      class="image-preview"
                      fit="cover"
                    />
                  </div>
                </template>

                <!-- JSON 类型 -->
                <el-input
                  v-else-if="item.valueType === 'json'"
                  v-model="formData[item.configKey]"
                  type="textarea"
                  :rows="6"
                  :placeholder="item.remark || '请输入JSON格式数据'"
                  style="width: 500px"
                />

                <!-- 文本类型（多行） -->
                <el-input
                  v-else-if="item.valueType === 'text'"
                  v-model="formData[item.configKey]"
                  type="textarea"
                  :rows="4"
                  :placeholder="item.remark || '请输入内容'"
                  style="width: 500px"
                />

                <!-- 字符串类型 -->
                <el-input
                  v-else
                  v-model="formData[item.configKey]"
                  :placeholder="item.remark || '请输入' + item.configName"
                  style="width: 400px"
                />

                <!-- 配置键名 + 说明 -->
                <template #extra>
                  <span class="config-key-tag">{{ item.configKey }}</span>
                  <span v-if="item.remark" class="config-remark">{{ item.remark }}</span>
                </template>
              </el-form-item>
            </el-card>
          </el-form>

          <div style="margin-top: 20px; text-align: right">
            <el-button type="primary" :loading="saving" @click="handleSave">
              保存配置
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "AppConfig",
  inheritAttrs: false,
});

import { getAppConfigGroups, saveAppConfig } from "@/api/system/appConfig";
import { AppConfigGroup } from "@/api/system/appConfig/types";

const groups = ref<AppConfigGroup[]>([]);
const activeGroup = ref("");
const saving = ref(false);
const formData = reactive<Record<string, any>>({});

const currentGroup = computed(() =>
  groups.value.find((g) => g.groupCode === activeGroup.value)
);

// 下拉选项映射（config_key → 选项列表）
const SELECT_OPTIONS: Record<string, { label: string; value: number | string }[]> = {
  default_theme: [
    { label: "暖色 warm", value: "warm" },
    { label: "红色 red", value: "red" },
    { label: "黄色 yellow", value: "yellow" },
    { label: "黑色 black", value: "black" },
    { label: "蓝色 blue", value: "blue" },
    { label: "绿色 green", value: "green" },
    { label: "橙色 orange", value: "orange" },
    { label: "棕色 brown", value: "brown" },
    { label: "紫色 purple", value: "purple" },
  ],
  loading_content_type: [
    { label: "Logo动画", value: 0 },
    { label: "名称动画", value: 1 },
  ],
  category_show_level: [
    { label: "商品列表模式", value: 0 },
    { label: "一级图标模式", value: 1 },
    { label: "二级分类模式", value: 2 },
    { label: "三级分类模式", value: 3 },
  ],
  user_center_nav_show_model_type: [
    { label: "九宫格", value: 0 },
    { label: "列表", value: 1 },
  ],
  common_goods_cover_size_type: [
    { label: "正方形", value: 0 },
    { label: "竖长图", value: 1 },
  ],
};

function getSelectOptions(configKey: string) {
  return SELECT_OPTIONS[configKey] || [];
}

function fillFormData() {
  groups.value.forEach((group) => {
    group.items.forEach((item) => {
      let val = item.configValue;
      if (item.valueType === "boolean") {
        val = Number(val) || 0;
      } else if (item.valueType === "select") {
        if (val === "" || val === null) {
          val = SELECT_OPTIONS[item.configKey]?.[0]?.value ?? "";
        } else {
          // DB 存的都是字符串，如果选项值是 number 需转换类型，否则 el-select 匹配不上
          const opts = SELECT_OPTIONS[item.configKey];
          if (opts?.length && typeof opts[0].value === "number") {
            val = Number(val);
          }
        }
      } else if (item.valueType === "json") {
        try {
          val = JSON.stringify(JSON.parse(val), null, 2);
        } catch { /* keep original */ }
      }
      formData[item.configKey] = val;
    });
  });
}

function handleSave() {
  saving.value = true;
  const payload: Record<string, string> = {};

  try {
    groups.value.forEach((group) => {
      group.items.forEach((item) => {
        const key = item.configKey;
        const type = item.valueType;
        let val = formData[key] ?? item.configValue ?? "";

        if (type === "boolean") {
          val = String(val);
        } else if (type === "select") {
          val = String(val);
        } else if (type === "json" && typeof val === "string" && val.trim()) {
          JSON.parse(val); // validate
          val = val;
        }
        payload[key] = String(val);
      });
    });
  } catch {
    ElMessage.warning("JSON 格式不正确，请检查后重试");
    saving.value = false;
    return;
  }

  saveAppConfig(payload)
    .then(() => {
      ElMessage.success("配置保存成功");
    })
    .finally(() => {
      saving.value = false;
    });
}

onMounted(() => {
  getAppConfigGroups().then(({ data }) => {
    groups.value = data;
    if (data.length > 0) {
      activeGroup.value = data[0].groupCode;
    }
    fillFormData();
  });
});
</script>

<style scoped>
.app-config-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.group-sidebar {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.group-item {
  padding: 14px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}

.group-item:hover {
  background: #f0f2f5;
  color: #303133;
}

.group-item.active {
  background: #ecf5ff;
  color: #409eff;
  border-left-color: #409eff;
  font-weight: 500;
}

.config-form {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.config-card {
  margin-bottom: 12px;
}

.config-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.switch-label {
  margin-left: 10px;
  font-size: 13px;
  color: #909399;
}

.image-input-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.image-preview {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.config-key-tag {
  display: inline-block;
  padding: 2px 8px;
  margin-right: 8px;
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  border-radius: 3px;
  font-family: monospace;
}

.config-remark {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
