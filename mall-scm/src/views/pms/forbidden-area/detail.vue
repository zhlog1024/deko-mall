<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <el-button @click="handleCancel">
            <i-ep-back />
            返回列表
          </el-button>
          <template v-if="mode === 'view'">
            <el-button type="primary" @click="switchToEditMode" v-if="id">
              <i-ep-edit />
              编辑
            </el-button>
          </template>
        </div>
      </template>

      <!-- 编辑表单 -->
      <div v-if="mode !== 'view'" class="form-container">
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="100px"
          label-position="right"
          size="default"
        >
          <el-form-item label="区域名称" prop="regionName">
            <el-input
              v-model="form.regionNames"
              disabled
              placeholder="自动生成区域名称"
            />
            <template #tip>
              <div class="tip-text">
                根据选择的区域自动生成名称，无需手动输入
              </div>
            </template>
          </el-form-item>
          <el-form-item label="区域选择" prop="selectedRegions">
            <el-select
              v-model="selectedRegions"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择禁售区域（可多选）"
              :loading="regionLoading"
              @change="handleRegionChange"
              style="width: 100%"
            >
              <el-option
                v-for="region in regionOptions"
                :key="region.id"
                :label="region.name"
                :value="region.id.toString()"
              />
            </el-select>
            <template #tip>
              <div class="tip-text">
                选择禁止销售的地区，可选择多个地区，将阻止这些地区的用户购买商品
              </div>
            </template>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="form.remark"
              type="textarea"
              placeholder="请输入备注信息，非必填"
              maxlength="200"
              rows="4"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleSubmit"
              :loading="submitting"
            >
              <i-ep-check />
              {{ isEdit ? "保存修改" : "创建禁售区域" }}
            </el-button>
            <el-button @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 查看详情 -->
      <div v-if="mode === 'view'" class="detail-container">
        <el-descriptions
          class="detail-descriptions"
          :column="2"
          border
          size="large"
          v-loading="loading"
        >
          <el-descriptions-item label="区域名称">
            <span class="detail-value">{{ detail.regionNames || "-" }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="区域编码">
            <span class="detail-value">{{ detail.regionCodes || "-" }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            <span class="detail-value">{{ detail.remark || "-" }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            <span class="detail-value">{{ detail.createTime || "-" }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            <span class="detail-value">{{ detail.updateTime || "-" }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  createForbiddenArea,
  getForbiddenArea,
  updateForbiddenArea,
} from "@/api/pms/forbidden-area";
import {
  ForbiddenArea,
  ForbiddenAreaForm,
} from "@/api/pms/forbidden-area/types";
import { ElMessage, ElMessageBox } from "element-plus";
import { getRegionList } from "@/api/system/region";
import { RegionDTO } from "@/api/system/region/types";

// 路由相关

const route = useRoute();
const router = useRouter();

// 页面状态
const id = route.query.id as string;
const mode = ref(route.query.mode || (id ? "view" : "create"));
const isEdit = computed(() => mode.value === "edit");
const loading = ref(false);
const submitting = ref(false);

// 表单相关
const formRef = ref();
const detail = ref<ForbiddenArea>({} as ForbiddenArea);
const form = reactive<ForbiddenAreaForm>({
  regionNames: "",
  regionCodes: "",
  remark: "",
});

// 区域数据
const regionOptions = ref<RegionDTO[]>([]);
const regionLoading = ref(false);
const selectedRegions = ref<string[]>([]);

const rules = {
  regionNames: [{ min: 2, max: 500, message: "区域名称过长", trigger: "blur" }],
  selectedRegions: [
    { type: "array", min: 1, message: "请至少选择一个区域", trigger: "change" },
  ],
  remark: [{ max: 200, message: "备注长度不能超过200个字符", trigger: "blur" }],
};

// 生命周期钩子
onMounted(async () => {
  // 先加载区域数据
  await fetchRegionData();

  if (id) {
    loadForbiddenAreaData();
  }
});

/**
 * 加载区域数据
 */
async function fetchRegionData() {
  regionLoading.value = true;
  try {
    // 获取所有省级区域
    const { data } = await getRegionList();
    regionOptions.value = data;
  } catch (error) {
    console.error("获取区域数据失败:", error);
    ElMessage.warning("获取区域数据失败，将无法选择地区");
  } finally {
    regionLoading.value = false;
  }
}

/**
 * 处理区域选择变化
 */
function handleRegionChange(selectedCodes: string[]) {
  try {
    if (!selectedCodes || selectedCodes.length === 0) {
      form.regionNames = "";
      form.regionCodes = "";
      return;
    }

    const regionNames = selectedCodes.map((code) => {
      const region = regionOptions.value.find(
        (r) => r.id && r.id.toString() === code
      );
      return region ? region.name : code;
    });

    if (selectedCodes.length === 1) {
      form.regionCodes = selectedCodes[0];
      form.regionNames = regionNames[0];
    } else {
      form.regionCodes = selectedCodes.join(",");
      form.regionNames = regionNames.join(",");
    }

    console.log("区域选择变化后的表单数据:", {
      regionCodes: form.regionCodes,
      regionNames: form.regionNames,
      selectedRegions: selectedCodes,
    });
  } catch (error) {
    console.error("处理区域选择变化出错:", error);
  }
}

/**
 * 加载禁售区域详情
 */
async function loadForbiddenAreaData() {
  loading.value = true;
  try {
    const { data } = await getForbiddenArea(Number(id));
    detail.value = data;

    if (mode.value === "edit") {
      Object.assign(form, data);

      if (form.regionCodes) {
        selectedRegions.value = form.regionCodes.split(",").filter(Boolean);
        // 确保 regionNames 也同步更新
        handleRegionChange(selectedRegions.value);
      } else {
        selectedRegions.value = [];
        form.regionNames = "";
      }
    }
  } catch (error) {
    console.error("加载禁售区域数据失败:", error);
    ElMessage.error("加载禁售区域数据失败");
  } finally {
    loading.value = false;
  }
}

/**
 * 切换到编辑模式
 */
function switchToEditMode() {
  // 将详情数据复制到表单
  Object.assign(form, detail.value);
  mode.value = "edit";
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!selectedRegions.value || selectedRegions.value.length === 0) {
    ElMessage.warning("请至少选择一个禁售区域");
    return;
  }

  handleRegionChange(selectedRegions.value);

  submitting.value = true;
  try {
    const submitData = {
      regionNames: form.regionNames,
      regionCodes: form.regionCodes,
      remark: form.remark || "",
    };

    if (!submitData.regionCodes || !submitData.regionNames) {
      throw new Error("区域信息不完整，请重新选择区域");
    }

    if (isEdit.value) {
      await updateForbiddenArea(Number(id), submitData);
      ElMessage.success("禁售区域更新成功");
    } else {
      await createForbiddenArea(submitData);
      ElMessage.success("禁售区域创建成功");
    }

    setTimeout(() => {
      router.push("/pms/forbidden-area");
    }, 1000);
  } catch (error) {
    console.error("保存失败:", error);
    ElMessage.error(
      error instanceof Error ? error.message : "保存失败，请重试"
    );
  } finally {
    submitting.value = false;
  }
}

/**
 * 取消编辑，返回列表页
 */
function handleCancel() {
  if (mode.value !== "view" && isDataChanged()) {
    ElMessageBox.confirm("表单数据已修改，置失修改吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        router.push("/pms/forbidden-area");
      })
      .catch(() => {});
  } else {
    router.push("/pms/forbidden-area");
  }
}

/**
 * 判断表单数据是否发生变化
 */
function isDataChanged(): boolean {
  // 新建时，判断任一必填字段是否填写
  if (mode.value === "create") {
    return !!form.regionNames || !!form.regionCodes;
  }

  // 编辑时，需要获取原始数据进行比对
  if (detail.value && detail.value.id) {
    return (
      form.regionNames !== detail.value.regionNames ||
      form.regionCodes !== detail.value.regionCodes ||
      form.remark !== detail.value.remark
    );
  }

  return false;
}
</script>
<style lang="scss" scoped>
.app-container {
  width: 1200px;
  margin: 50px auto;
  border: 1px solid #eee;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.form-container,
.detail-container {
  padding: 20px;
}

.tip-text {
  font-size: 12px;
  color: #909399;
}

.detail-value {
  font-weight: 500;
}

.detail-content {
  min-height: 400px;
}

.detail-region-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .region-tag {
    margin-bottom: 8px;
  }
}
</style>
