<script setup lang="ts">
defineOptions({
  name: "Shop",
  inheritAttrs: false,
});

import { getShopPages, updateShop } from "@/api/pms/shop";
import { DialogType, Shop, ShopPageQuery } from "@/api/pms/shop/types";

const queryFormRef = ref(ElForm); // 属性名必须和元素的ref属性值一致
const dataFormRef = ref(ElForm); // 属性名必须和元素的ref属性值一致

const state = reactive({
  loading: true,
  queryParams: {
    shopId: undefined,
    keywords: "",
    pageNum: 1,
    pageSize: 10,
  } as ShopPageQuery,
  shopList: [] as Shop[],
  total: 0,
  dialog: {} as DialogType,
  formData: {} as Shop,
});

const { loading, queryParams, shopList, total, dialog, formData } = toRefs(state);

function handleQuery() {
  state.loading = true;
  getShopPages(state.queryParams).then(({ data }) => {
    state.shopList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  queryFormRef.value.resetFields();
  handleQuery();
}

function handleUpdate(row: any) {
  state.dialog = { title: "修改店铺信息", visible: true };
  state.formData = { ...row };
}

function handleStatusChange(row: any) {
  const newStatus = row.status === 1 ? 0 : 1;
  updateShop(row.id, { status: newStatus }).then(() => {
    ElMessage.success(newStatus === 1 ? "店铺已开启" : "店铺已关闭");
    handleQuery();
  });
}

onMounted(() => {
  handleQuery();
});

function submitForm() {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) return;
    updateShop(state.formData.id, {
      shopName: state.formData.shopName,
      logo: state.formData.logo,
      logoLong: state.formData.logoLong,
      description: state.formData.description,
      sort: state.formData.sort,
      score: state.formData.score,
    }).then(() => {
      ElMessage.success("修改成功");
      state.dialog.visible = false;
      handleQuery();
    });
  });
}
</script>

<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="true">
      <el-form-item label="店铺ID" prop="shopId">
        <el-input
          v-model="queryParams.shopId"
          placeholder="请输入店铺ID"
          clearable
          style="width: 150px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="店铺名称" prop="keywords">
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入店铺名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="shopList" @selection-change="handleSelectionChange">
      <el-table-column label="店铺ID" prop="id" width="80" align="center" />
      <el-table-column label="店铺名称" prop="shopName" min-width="180" />
      <el-table-column label="店铺logo" prop="logo" width="100" align="center">
        <template #default="{ row }">
          <el-image v-if="row.logo" :src="row.logo" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[row.logo]" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="店铺简介" prop="description" min-width="220" show-overflow-tooltip />
      <el-table-column label="归属类型" prop="ownerType" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.ownerType === 'SUPPLIER'">供应商</el-tag>
          <el-tag v-else type="warning">会员</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商品数" prop="goodsCount" width="90" align="center" />
      <el-table-column label="收藏数" prop="favorCount" width="90" align="center" />
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="评分" prop="score" width="80" align="center" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            :active-value="true"
            :inactive-value="false"
            active-text="开"
            inactive-text="关"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="handleQuery"
    />

    <!-- 修改店铺信息弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" append-to-body>
      <el-form ref="dataFormRef" :model="formData" label-width="100px">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="formData.shopName" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="店铺logo" prop="logo">
          <single-upload v-model="formData.logo" :width="120" :height="120" />
        </el-form-item>
        <el-form-item label="店铺长图" prop="logoLong">
          <single-upload v-model="formData.logoLong" :width="300" :height="120" />
        </el-form-item>
        <el-form-item label="店铺简介" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入店铺简介" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <div class="flex-row align-c gap-10">
            <el-rate v-model="formData.score" :max="5" allow-half :colors="['#f7ba2a', '#f7ba2a', '#f7ba2a']" />
            <el-input-number v-model="formData.score" :min="0" :max="5" :step="0.1" :precision="1" :controls="true" style="width: 120px" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="dialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
