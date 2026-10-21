<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="商品ID">
          <el-input
            v-model="queryParams.spuId"
            placeholder="请输入商品ID"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="规格ID">
          <el-input
            v-model="queryParams.skuId"
            placeholder="请输入规格ID"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-cascader
            v-model="queryParams.categoryId"
            placeholder="全部"
            :props="{ emitPath: false }"
            :options="categoryOptions"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="queryParams.auditStatus"
            placeholder="请选择审核状态"
            style="width: 170px"
          >
            <el-option
              v-for="item in auditStatusList"
              :key="item.key"
              :label="item.value"
              :value="item.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <i-ep-search />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <i-ep-refresh />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card>
      <template #header>
        <el-button type="success" @click="handleAdd">
          <i-ep-plus />
          新增
        </el-button>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleDelete"
        >
          <i-ep-delete />
          删除
        </el-button>
      </template>

      <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="goodsList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="商品ID" prop="id" />
        <el-table-column type="expand" width="120" label="商品库存">
          <template #default="props">
            <el-table :data="props.row.skuList" border>
              <el-table-column align="center" label="SKU ID" prop="id" />
              <el-table-column
                align="center"
                label="规格编码"
                prop="skuSn"
                width="130"
              >
                <template #default="scope">
                  <span>{{ scope.row.skuSn || "-" }}</span>
                </template>
              </el-table-column>
              <el-table-column
                align="left"
                label="规格名称"
                prop="name"
                width="400"
              />
              <el-table-column label="图片" prop="picUrl">
                <template #default="scope">
                  <img :src="scope.row.picUrl" width="40" />
                </template>
              </el-table-column>

              <el-table-column align="center" label="供货价" prop="supplyPrice">
                <template #default="scope"
                  >{{scope.row.supplyPrice }}
                </template>
              </el-table-column>
              <el-table-column align="center" label="采购价" prop="purchasePrice">
                <template #default="scope"
                  >{{scope.row.purchasePrice }}
                </template>
              </el-table-column>
              <el-table-column align="center" label="零售价" prop="retailPrice">
                <template #default="scope"
                  >{{scope.row.retailPrice }}
                </template>
              </el-table-column>

              <el-table-column align="center" label="划线价" prop="marketPrice">
                <template #default="scope"
                  >{{ scope.row.marketPrice }}
                </template>
              </el-table-column>

              <el-table-column align="center" label="库存" prop="stock" />
            </el-table>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" />
        <el-table-column label="图片">
          <template #default="scope">
            <el-popover placement="right" :width="400" trigger="hover">
              <img :src="scope.row.picUrl" width="400" height="400" />
              <template #reference>
                <img
                  :src="scope.row.picUrl"
                  style="max-width: 60px; max-height: 60px"
                />
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="商品类目" prop="categoryName" min-width="100" />
        <el-table-column label="商品类型" prop="spuTypeDesc" min-width="100" />
        <el-table-column label="商品品牌" prop="brandName" min-width="100" />
        <el-table-column label="审核状态" prop="auditStatus" width="100">
          <template #default="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="上架状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          align="center"
          label="提审时间"
          prop="submitTime"
          width="100"
        />
        <el-table-column
          align="center"
          label="审核时间"
          prop="auditTime"
          width="100"
        />
        <el-table-column
          align="center"
          label="审核人员"
          prop="auditUser"
          width="100"
        />
        <el-table-column
          align="center"
          label="备注"
          prop="remark"
          width="100"
        />

        <el-table-column align="center" label="零售价" prop="originalPrice">
          <template #default="scope"
            >{{ scope.row.price}}
          </template>
        </el-table-column>

        <el-table-column label="销量" prop="sales" width="100" />
        <el-table-column label="操作" width="340">
          <template #default="scope">
            <el-button
              type="success"
              link
              @click.stop="handleGoodsView(scope.row.detail)"
              >详情
            </el-button>
            <el-button
              type="primary"
              link
              @click.stop="handleUpdate(scope.row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页工具条 -->
    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />
    <el-dialog v-model="dialogVisible" title="商品详情">
      <div class="goods-detail-box" v-html="goodDetail"></div>
    </el-dialog>

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
<!--商品列表-->
<script setup lang="ts">
import {
  auditSpu,
  deleteSpu,
  getSpuPage,
  publishSpu,
  unpublishSpu,
} from "@/api/pms/goods";
import { getCategoryOptions } from "@/api/pms/category";
import { Goods, GoodsQuery } from "@/api/pms/goods/types";
import { useRouter } from "vue-router";
// 添加路由导航守卫和生命周期钩子
import { onActivated } from "vue";

defineOptions({
  name: "Goods",
  inheritAttrs: false,
});

const dataTableRef = ref();

const router = useRouter();

const state = reactive({
  // 遮罩层
  loading: true,
  // 选中数组
  ids: [],
  total: 0,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    auditStatus: "",
  } as GoodsQuery,
  goodsList: [] as Goods[],
  categoryOptions: [] as OptionType[],
  goodDetail: undefined,
  dialogVisible: false,
  auditDialog: {
    visible: false,
    title: "商品审核",
    spuId: "",
    auditStatus: 0, // 1-通过 2-拒绝
    remark: "",
  },
  auditStatusList: [
    { key: "", value: "全部" },
    { key: 0, value: "待审核" },
    { key: 1, value: "已通过" },
    { key: 2, value: "已拒绝" },
  ],
});

const {
  loading,
  ids,
  queryParams,
  goodsList,
  categoryOptions,
  goodDetail,
  total,
  dialogVisible,
  auditStatusList,
  auditDialog,
} = toRefs(state);

function handleQuery() {
  state.loading = true;

  // 使用统一的商品列表接口，传入审核状态参数
  getSpuPage(state.queryParams).then(({ data }) => {
    state.goodsList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
    spuId: undefined,
    skuId: undefined,
    name: undefined,
    categoryId: undefined,
    auditStatus: "",
  };
  handleQuery();
}

function handleGoodsView(detail: any) {
  state.goodDetail = detail;
  state.dialogVisible = true;
}

function handleAdd() {
  router.push({ path: "goods-detail" });
}

function handleUpdate(row: any) {
  router.push({
    path: "goods-detail",
    query: { goodsId: row.id, categoryId: row.categoryId },
  });
}

function handleDelete(row: any) {
  const ids = row.id || state.ids.join(",");
  ElMessageBox.confirm("是否确认删除选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(function () {
      return deleteSpu(ids);
    })
    .then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    });
}

function handleRowClick(row: any) {
  dataTableRef.value.toggleRowSelection(row);
}

function handleSelectionChange(selection: any) {
  state.ids = selection.map((item: { id: any }) => item.id);
}

type AuditStatusType = "success" | "warning" | "danger" | "info";
// 获取审核状态对应的类型
// 获取审核状态对应的类型（修改参数类型为number）
function getAuditStatusType(status: number): AuditStatusType {
  const map: Record<number, AuditStatusType> = {
    0: "warning",
    1: "success",
    2: "danger",
  };
  return map[status] || "info";
}

// 获取审核状态对应的类型（修改参数类型为number）
function getStatusType(status: number): AuditStatusType {
  const map: Record<number, AuditStatusType> = {
    0: "warning",
    1: "success",
    2: "danger",
  };
  return map[status] || "info";
}

// 同步修改获取状态文本的方法（参数类型改为number）
function getAuditStatusText(status: number) {
  const map: Record<number, string> = {
    0: "待审核",
    1: "已通过",
    2: "已拒绝",
  };
  return map[status] || "未知";
}

// 同步修改获取状态文本的方法（参数类型改为number）
function getStatusText(status: number) {
  const map: Record<number, string> = {
    0: "草稿",
    1: "已上架",
    2: "已下架",
  };
  return map[status] || "未知";
}

// 处理审核操作
function handleAudit(row: any) {
  state.auditDialog.spuId = row.id;
  state.auditDialog.visible = true;
}

function submitAudit() {
  if (!state.auditDialog.remark && state.auditDialog.auditStatus === 2) {
    ElMessage.warning("请填写原因");
    return;
  }

  auditSpu(state.auditDialog.spuId.toString(), {
    auditStatus: state.auditDialog.auditStatus,
    remark: state.auditDialog.remark,
  }).then(() => {
    ElMessage.success("审核提交成功");
    state.auditDialog.visible = false;
    handleQuery(); // 刷新列表
  });
}

/**
 * 处理商品上架
 * @param row 商品行数据
 */
function handlePublish(row: any) {
  // 确保只有审核通过的商品才能上架
  if (row.auditStatus !== 1) {
    ElMessage.warning("只有审核通过的商品才能上架");
    return;
  }

  ElMessageBox.confirm("确定要上架该商品吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      publishSpu(row.id).then(() => {
        ElMessage.success("商品上架成功");
        handleQuery(); // 刷新列表
      });
    })
    .catch(() => {
      // 取消上架
    });
}

/**
 * 处理商品下架
 * @param row 商品行数据
 */
function handleUnpublish(row: any) {
  // 确保只有审核通过的商品才能下架
  if (row.auditStatus !== 1) {
    ElMessage.warning("只有审核通过的商品才能下架");
    return;
  }

  ElMessageBox.confirm("确定要下架该商品吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      unpublishSpu(row.id).then(() => {
        ElMessage.success("商品下架成功");
        handleQuery(); // 刷新列表
      });
    })
    .catch(() => {
      // 取消下架
    });
}

// 当路由激活时重新加载数据（从其他页面返回时触发）
onActivated(() => {
  console.log("商品列表页被激活，重新加载数据");
  handleQuery();
});

// 组件挂载时加载数据
onMounted(() => {
  getCategoryOptions().then(({ data }) => {
    categoryOptions.value = data;
  });
  handleQuery();
});
</script>
<style lang="scss" scoped>
.search-container {
  padding: 18px 16px 10px 16px;

  .el-form-item {
    margin-bottom: 8px;
  }
}

.goods-detail-box {
  img {
    width: 100% !important;
  }
}
</style>
