<template>
  <el-dialog v-model="props.dialogVisible" title="选择商品" width="1000px" class="select-goods-dialog" @close="resetshow">
    <!-- 搜索区 -->
    <div class="search-bar">
      <el-form :model="queryParams" :inline="true" size="default">
        <el-form-item>
          <el-input v-model="queryParams.spuId" placeholder="商品ID" clearable style="width: 130px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.name" placeholder="商品名称" clearable style="width: 180px" prefix-icon="Search" />
        </el-form-item>
        <el-form-item>
          <el-cascader v-model="queryParams.categoryId" placeholder="商品分类" :props="{ emitPath: false }" :options="categoryOptions" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.spuType" placeholder="商品类型" clearable style="width: 140px" :disabled="!!lockedSpuType">
            <el-option v-for="item in goodsTypeList" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><i-ep-search />搜索</el-button>
          <el-button @click="resetQuery"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="goodsList" border stripe highlight-current-row :row-class-name="rowClassName" @row-click="handleRowClick">
        <el-table-column label="选择" width="60" align="center">
          <template #default="scope">
            <el-checkbox :model-value="selIds.has(scope.row.id)" @change="toggle(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="商品ID" prop="id" width="80" align="center" />
        <el-table-column label="商品名称" min-width="200">
          <template #default="scope">
            <div class="goods-cell">
              <el-image v-if="scope.row.picUrl" :src="scope.row.picUrl" class="goods-thumb" :preview-src-list="[scope.row.picUrl]" />
              <div class="goods-thumb-placeholder" v-else>
                <i-ep-picture />
              </div>
              <span class="goods-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品类型" prop="spuTypeDesc" width="100" align="center" />
        <el-table-column label="零售价" width="120" align="center">
          <template #default="scope">
            <span class="price">¥{{ scope.row.price ?? "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="划线价" width="120" align="center">
          <template #default="scope">
            <span
              class="origin-price"
              v-if="scope.row.originPrice != null && scope.row.originPrice !== ''"
            >
              ¥{{ scope.row.originPrice }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-hint" v-if="!loading && goodsList.length === 0">
        <el-empty description="暂无数据" :image-size="100" />
      </div>
    </el-card>

    <div class="pagination-bar" v-if="total > 0">
      <span class="pagination-info">共 {{ total }} 条 · 已选 {{ selIds.size }} 个</span>
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="sizes, prev, pager, next" background small @current-change="handleQuery" @size-change="handleQuery" />
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetshow">取消</el-button>
        <el-button type="primary" :disabled="selIds.size === 0" @click="handleSubmit">
          确定（{{ selIds.size }}）
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ElMessage } from "element-plus";
import { onMounted, reactive, ref, toRefs, watch } from "vue";
import type { PropType } from "vue";
import { getSpuPage } from "@/api/pms/goods";
import { getCategoryOptions } from "@/api/pms/category";
import type { GoodsQuery } from "@/api/pms/goods/types";

/**
 * 商品选择弹窗（系统统一组件）
 *
 * 语义：多选、跨页保持；初始 selectedIds 为已选并可直接取消；含价格/分类/类型筛选。
 *
 * 事件：
 * - confirm(rows, payload)：rows 为本次会话中见过的行对象
 *   （含 id/name/picUrl/spuTypeDesc/price(零售价)/originPrice(划线价)）；
 *   payload.ids 为最终完整商品 ID 集合（全量替换类场景请用 ids）
 * - close
 */
const props = defineProps({
  dialogVisible: { type: Boolean, default: false },
  /** false=单选（选中即替换为当前行） */
  multiple: { type: Boolean, default: true },
  /** 初始已选商品 ID（每次打开以此为准） */
  selectedIds: { type: Array as PropType<number[]>, default: () => [] },
  /** 锁定商品类型筛选（如只选普通商品 NORMAL） */
  lockedSpuType: { type: String as PropType<string | null>, default: null },
});
const Emit = defineEmits(["confirm", "close"]);

const state = reactive({
  loading: false,
  total: 0,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    spuType: null,
    status: 1,
  } as GoodsQuery,
  goodsList: [] as any[],
  categoryOptions: [] as any[],
  goodsTypeList: [
    { key: null, value: "全部" },
    { key: "NORMAL", value: "普通商品" },
  ],
});

const { loading, goodsList, categoryOptions, total, queryParams, goodsTypeList } = toRefs(state);

// 已选 id（跨页保持）
const selIds = ref<Set<number>>(new Set());
// 已见过行对象缓存（确认时组装 rows）
const rowCache = ref<Map<number, any>>(new Map());

function rowClassName({ row }: any): string {
  return selIds.value.has(row.id) ? "selected-row" : "";
}

function toggle(row: any) {
  const next = new Set(selIds.value);
  if (!props.multiple) {
    next.clear();
  }
  if (next.has(row.id)) {
    next.delete(row.id);
  } else {
    next.add(row.id);
  }
  selIds.value = next;
}

function handleRowClick(row: any) {
  toggle(row);
}

function handleQuery() {
  state.loading = true;
  const params: any = { pageNum: state.queryParams.pageNum, pageSize: state.queryParams.pageSize, status: 1 };
  if (state.queryParams.name) params.name = state.queryParams.name;
  if (state.queryParams.categoryId) params.categoryId = state.queryParams.categoryId;
  if (state.queryParams.spuType) params.spuType = state.queryParams.spuType;
  if (state.queryParams.spuId) params.spuId = state.queryParams.spuId;

  getSpuPage(params)
    .then(({ data }) => {
      const list = data.list || [];
      state.goodsList = list;
      state.total = data.total || 0;
      // 缓存行对象，供确认时组装
      const cache = new Map(rowCache.value);
      list.forEach((row: any) => cache.set(row.id, row));
      rowCache.value = cache;
    })
    .finally(() => {
      state.loading = false;
    });
}

function resetQuery() {
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    categoryId: undefined,
    spuType: props.lockedSpuType || null,
    spuId: undefined,
    status: 1,
  };
  handleQuery();
}

function init() {
  selIds.value = new Set(props.selectedIds || []);
  rowCache.value = new Map();
  resetQuery();
}

watch(
  () => props.dialogVisible,
  (val) => {
    if (val) init();
  },
  { immediate: true }
);

function resetshow() {
  Emit("close");
}

function handleSubmit() {
  if (selIds.value.size === 0) {
    ElMessage.warning("请选择商品");
    return;
  }
  const ids = [...selIds.value];
  const rows = ids
    .map((id) => rowCache.value.get(id))
    .filter((row): row is any => !!row);
  Emit("confirm", rows, { ids });
}

onMounted(() => {
  getCategoryOptions().then(({ data }) => {
    categoryOptions.value = data;
  });
});
</script>

<style scoped lang="scss">
.select-goods-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
  }
}

.search-bar {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
  .el-form {
    flex-wrap: wrap;
    gap: 0 8px;
  }
  .el-form-item {
    margin-bottom: 8px;
  }
}

.table-card {
  border-radius: 8px;
  :deep(.el-card__body) {
    padding: 0;
  }
  :deep(.el-table .selected-row) {
    background: #ecf5ff;
  }
}

.goods-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  .goods-thumb {
    width: 48px;
    height: 48px;
    border-radius: 4px;
    flex-shrink: 0;
    object-fit: cover;
    background: #f5f7fa;
  }
  .goods-thumb-placeholder {
    width: 48px;
    height: 48px;
    border-radius: 4px;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
    font-size: 20px;
    flex-shrink: 0;
  }
  .goods-name {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
  }
}

.price {
  font-weight: 600;
  color: #e6a23c;
}

.origin-price {
  color: #909399;
  text-decoration: line-through;
}

.empty-hint {
  padding: 40px 0;
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  .pagination-info {
    color: #909399;
    font-size: 13px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
