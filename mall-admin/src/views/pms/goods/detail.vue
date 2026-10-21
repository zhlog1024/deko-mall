<script setup lang="ts">
import GoodsCategory from "./components/GoodsCategory.vue";
import GoodsInfo from "./components/GoodsInfo.vue";
import GoodsAttribute from "./components/GoodsAttribute.vue";
import GoodsStock from "./components/GoodsStock.vue";
import GoodsDelivery from "./components/GoodsDelivery.vue";

import { addSpu, auditSpu, getSpuDetail, updateSpu } from "@/api/pms/goods";
import { useRoute, useRouter } from "vue-router";
import { GoodsDetail } from "@/api/pms/goods/types";
import { onMounted, reactive, ref } from "vue";

import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();

// 添加对GoodsStock组件的引用
const goodsStockRef = ref<InstanceType<typeof GoodsStock> | null>(null);
const state = reactive({
  loaded: false,
  isEdit: false, // 标识是否是编辑模式
  goodsInfo: {
    spuType: "NORMAL",
    album: [],
    attrList: [],
    specList: [],
    skuList: [],
    auditStatus: 0,
    rejectReason: "",
    submitTime: "",
    auditTime: "",
    freightTemplateId: null,
    forbiddenAreaId: null,
    subPicUrls: [], // 增加商品轮播图默认空数组值
    needDelivery: true, // 新增商品默认需要发货
  } as GoodsDetail,
  auditDialog: {
    visible: false,
    title: "商品审核",
    spuId: "",
    auditStatus: 0, // 1-通过 2-拒绝
    remark: "",
  },

  submitting: false,
  saveLoading: false,
});

const { loaded, isEdit, goodsInfo, auditDialog, submitting, saveLoading } =
  toRefs(state);

function loadData() {
  const goodsId = route.query.goodsId as string;

  if (goodsId) {
    state.isEdit = true; // 设置为编辑模式
    getSpuDetail(goodsId).then((response) => {
      state.goodsInfo = response.data;
      state.loaded = true;
    });
  } else {
    state.isEdit = false; // 设置为新增模式
    state.loaded = true;
  }
}

type AuditStatusType = "success" | "warning" | "danger" | "info";
// 获取审核状态对应的类型
// 获取审核状态对应的类型（修改参数类型为number）
function getAuditStatusType(status: any): AuditStatusType {
  const map: Record<number, AuditStatusType> = {
    0: "warning",
    1: "success",
    2: "danger",
  };
  return map[status] || "info";
}

// 同步修改获取状态文本的方法（参数类型改为number）
function getAuditStatusText(status: any) {
  const map: Record<number, string> = {
    0: "待审核",
    1: "已通过",
    2: "已拒绝",
  };
  return map[status] || "未知";
}

// 处理审核操作
function handleAudit() {
  state.auditDialog.spuId = goodsInfo.value.id;
  state.auditDialog.auditStatus = 1; // 默认选择通过
  state.auditDialog.remark = "";
  state.auditDialog.visible = true;
}

function submitAudit() {
  if (!state.auditDialog.remark && state.auditDialog.auditStatus === 2) {
    ElMessage.warning("请填写备注");
    return;
  }

  // 校验商品类型是否为空
  if (!goodsInfo.value.spuType) {
    ElMessage.warning("商品类型不能为空，请先在商品信息中设置商品类型");
    return;
  }

  state.submitting = true;
  auditSpu(state.auditDialog.spuId.toString(), {
    auditStatus: state.auditDialog.auditStatus,
    remark: state.auditDialog.remark,
  })
    .then(() => {
      ElMessage.success("审核提交成功");
      state.auditDialog.visible = false;

      // 审核成功后返回列表页
      router.back();
    })
    .catch((error) => {
      console.error("审核失败:", error);
      ElMessage.error(error.message || "审核失败");
    })
    .finally(() => {
      state.submitting = false;
    });
}

/**
 * 保存商品
 */
function handleSave() {
  // 校验必要的字段
  if (!goodsInfo.value.name) {
    ElMessage.warning("请填写商品名称");
    return;
  }

  // 校验商品类型
  if (!goodsInfo.value.spuType) {
    ElMessage.warning("请选择商品类型");
    return;
  }

  // 校验商品分类（未选分类直接保存会因数据库 NOT NULL 约束导致保存失败）
  if (!goodsInfo.value.categoryId) {
    ElMessage.warning("请选择商品分类");
    return;
  }

  // 检查商品状态，上架商品不允许修改
  if (isEdit.value && goodsInfo.value.status === 1) {
    ElMessage.warning("商品已上架，不能修改商品信息");
    return;
  }

  state.saveLoading = true;

  // 调用GoodsStock组件的submitForm方法处理所有数据
  if (goodsStockRef.value) {
    console.log("开始调用GoodsStock组件的submitForm方法");
    const formData = goodsStockRef.value.submitForm();

    // 如果返回false表示验证失败
    if (formData === false) {
      state.saveLoading = false;
      return;
    }

    console.log("使用GoodsStock组件处理后的数据:", formData);

    // 根据是否编辑模式选择不同的API
    const savePromise = isEdit.value
      ? updateSpu(formData.id, formData)
      : addSpu(formData);

    savePromise
      .then(() => {
        ElMessage.success(isEdit.value ? "更新成功" : "新增成功");
        router.back();
      })
      .catch((error) => {
        console.error("保存失败:", error);
        ElMessage.error(error.message || "保存失败");
      })
      .finally(() => {
        state.saveLoading = false;
      });
  } else {
    // 如果无法获取组件引用，显示错误
    ElMessage.error("GoodsStock组件引用获取失败");
    state.saveLoading = false;
  }
}

/**
 * 取消编辑，返回列表页
 */
function onSpuTypeChanged() {
  if (goodsStockRef.value) {
    goodsStockRef.value.recalculateForTypeChange();
  }
}

function handleCancel() {
  router.back();
}

onMounted(() => {
  loadData();
});
</script>
<template>
  <div class="app-container">
    <div class="page-title">
      <h2>{{ isEdit ? "编辑商品" : "新增商品" }}</h2>
    </div>

    <el-row :gutter="20" v-if="loaded">
      <el-col :span="24">
        <el-card class="goods-section-card" shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>商品分类</h3>
            </div>
          </template>
          <GoodsCategory v-model="goodsInfo" :showButtons="false" />
        </el-card>
      </el-col>

      <el-col :span="24">
        <el-card class="goods-section-card" shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>商品信息</h3>
            </div>
          </template>
          <GoodsInfo v-model="goodsInfo" :showButtons="false" @spuTypeChanged="onSpuTypeChanged" />
        </el-card>
      </el-col>

      <el-col :span="24">
        <el-card class="goods-section-card" shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>商品属性</h3>
            </div>
          </template>
          <GoodsAttribute v-model="goodsInfo" :showButtons="false" />
        </el-card>
      </el-col>

      <el-col :span="24">
        <el-card class="goods-section-card" shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>商品库存</h3>
            </div>
          </template>
          <GoodsStock
            ref="goodsStockRef"
            v-model="goodsInfo"
            :showButtons="false"
          />
        </el-card>
      </el-col>

      <el-col :span="24">
        <el-card class="goods-section-card" shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>配送设置</h3>
            </div>
          </template>
          <GoodsDelivery v-model="goodsInfo" :showButtons="false" />
        </el-card>
      </el-col>

      <!-- 审核状态和操作区域，只在编辑模式显示 -->
      <el-col :span="24" v-if="isEdit">
        <el-card class="goods-section-card audit-info-card" shadow="hover">
          <template #header>
            <div class="section-header audit-header">
              <h3>审核状态</h3>
              <el-tag
                :type="getAuditStatusType(goodsInfo.auditStatus)"
                size="large"
              >
                {{ getAuditStatusText(goodsInfo.auditStatus) }}
              </el-tag>
            </div>
          </template>
          <!-- 审核信息内容区域 -->
          <div v-if="goodsInfo.auditStatus === 0" class="audit-notice">
            <el-alert
              title="商品待审核"
              type="warning"
              :closable="false"
              show-icon
            >
              <template #default>
                商品待审核，请返回列表页，通过列表中的【审核】按钮进行审核操作
              </template>
            </el-alert>
          </div>
          <div v-else-if="goodsInfo.auditStatus === 2" class="audit-reason">
            <p><strong>拒绝原因：</strong>{{ goodsInfo.remark || "-" }}</p>
            <p><strong>审核人：</strong>{{ goodsInfo.auditUser || "-" }}</p>
            <p><strong>审核时间：</strong>{{ goodsInfo.auditTime || "-" }}</p>
          </div>
          <div v-else-if="goodsInfo.auditStatus === 1" class="audit-info">
            <p><strong>备注：</strong>{{ goodsInfo.remark || "-" }}</p>
            <p><strong>审核人：</strong>{{ goodsInfo.auditUser || "-" }}</p>
            <p><strong>审核时间：</strong>{{ goodsInfo.auditTime || "-" }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部操作按钮，统一处理所有操作 -->
    <div class="bottom-action-buttons" v-if="loaded">
      <el-button @click="handleCancel">返回</el-button>

      <!-- 保存按钮 -->
      <el-button type="primary" @click="handleSave" :loading="saveLoading">
        <i-ep-check />
        保存
      </el-button>

      <!-- 审核按钮已移除，审核操作在列表页完成 -->
    </div>
    
  <!-- 审核表单弹窗 -->

  <!-- 拒绝原因弹窗 -->
  <el-dialog
    v-model="auditDialog.visible"
    :title="auditDialog.title"
    width="500px"
  >
    <el-form label-width="80px">
      <el-form-item label="审核结果">
        <el-radio-group v-model="auditDialog.auditStatus">
          <el-radio :label="1">通过</el-radio>
          <el-radio :label="2">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="auditDialog.auditStatus === 2" label="拒绝原因">
        <el-input
          v-model="auditDialog.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入拒绝原因"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="auditDialog.visible = false">取 消</el-button>
      <el-button type="primary" @click="submitAudit">提 交</el-button>
    </template>
  </el-dialog>
  </div>

</template>

<style lang="scss" scoped>
.app-container {
  width: 1200px;
  margin: 30px auto;
  padding: 0 20px 50px;
  border: 1px solid #eee;
}

.page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.audit-info-card {
  margin-bottom: 20px;
}

.audit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.audit-action-buttons {
  display: flex;
  gap: 10px;
}

.audit-reason,
.audit-info {
  line-height: 1.8;
}

.bottom-action-buttons {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 100;
  display: flex;
  gap: 10px;
  background-color: #fff;
  padding: 10px 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.goods-section-card {
  margin-bottom: 20px;

  .section-header {
    display: flex;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }
  }
}
</style>
