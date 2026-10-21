<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="query" :inline="true">
        <el-form-item
          prop="selectedRegions"
          label="区域选择"
          style="width: 350px"
        >
          <el-select
            v-model="selectedRegions"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择区域（可多选）"
            @change="handleRegionChange"
            :loading="regionLoading"
            style="width: 100%"
          >
            <el-option
              v-for="region in regionOptions"
              :key="region.id"
              :label="region.name"
              :value="region.id.toString()"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="fetchList">
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
        <el-button type="success" @click="handleCreate">
          <i-ep-plus />
          新建禁售区域
        </el-button>
        <el-button
          type="danger"
          :disabled="multiple"
          @click="handleBatchDelete"
        >
          <i-ep-delete />
          删除
        </el-button>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="list"
        @selection-change="handleSelectionChange"
        border
        stripe
        size="large"
        highlight-current-row
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column
          prop="regionNames"
          label="区域名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="regionCodes"
          label="区域编码"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="remark"
          label="备注"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="success" @click="handleDetail(scope.row.id)">
              详情
            </el-button>
            <el-button link type="primary" @click="handleEdit(scope.row.id)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页工具条 -->
    <pagination
      v-if="total > 0"
      :total="total"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      @pagination="fetchList"
    />
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  deleteForbiddenArea,
  getForbiddenAreaPages,
} from "@/api/pms/forbidden-area";
import {
  ForbiddenArea,
  ForbiddenAreaPageQuery,
} from "@/api/pms/forbidden-area/types";
import { getRegionList } from "@/api/system/region";
import { RegionDTO } from "@/api/system/region/types";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";

defineOptions({
  name: "ForbiddenArea",
  inheritAttrs: false,
});

const router = useRouter();
const queryFormRef = ref();
const loading = ref(false);
const ids = ref<number[]>([]);
const single = ref(true);
const multiple = ref(true);
const list = ref<ForbiddenArea[]>([]);
const total = ref(0);

// 区域选择相关
const regionOptions = ref<RegionDTO[]>([]);
const regionLoading = ref(false);
const selectedRegions = ref<string[]>([]);

// 查询参数
const query = reactive<ForbiddenAreaPageQuery>({
  pageNum: 1,
  pageSize: 10,
  regionCodes: "",
  regionNames: "",
});

/**
 * 获取禁售区域列表
 */
function fetchList() {
  loading.value = true;
  getForbiddenAreaPages(query)
    .then(({ data }) => {
      list.value = data.list;
      total.value = data.total;
      loading.value = false;
    })
    .catch(() => {
      loading.value = false;
    });
}

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
  if (!selectedCodes || selectedCodes.length === 0) {
    query.regionCodes = "";
    return;
  }

  // 将区域代码转换为逗号分隔的字符串
  query.regionCodes = selectedCodes.join(",");
}

/**
 * 重置查询条件
 */
function resetQuery() {
  queryFormRef.value?.resetFields();
  selectedRegions.value = [];
  query.regionCodes = "";
  query.pageNum = 1;
  fetchList();
}

/**
 * 表格选择变化
 */
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/**
 * 批量删除
 */
function handleBatchDelete() {
  if (ids.value.length === 0) {
    ElMessage.warning("请至少选择一条记录");
    return;
  }

  ElMessageBox.confirm("确定要删除选中的禁售区域吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      // 实际需要实现批量删除API
      const promises = ids.value.map((id) => deleteForbiddenArea(String(id)));
      Promise.all(promises).then(() => {
        ElMessage.success("批量删除成功");
        fetchList();
      });
    })
    .catch(() => {});
}

/**
 * 新建禁售区域
 */
function handleCreate() {
  router.push({ path: "forbidden-area-detail", query: { mode: "create" } });
}

/**
 * 编辑禁售区域
 */
function handleEdit(id: number) {
  router.push({ path: "forbidden-area-detail", query: { id, mode: "edit" } });
}

/**
 * 查看禁售区域详情
 */
function handleDetail(id: number) {
  router.push({ path: "forbidden-area-detail", query: { id, mode: "view" } });
}

/**
 * 删除禁售区域
 */
function handleDelete(id: number) {
  ElMessageBox.confirm("确定要删除该禁售区域吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      deleteForbiddenArea(String(id))
        .then(() => {
          ElMessage.success("删除成功");
          fetchList();
        })
        .catch(() => {
          ElMessage.error("删除失败");
        });
    })
    .catch(() => {});
}

onMounted(async () => {
  await fetchRegionData();
  fetchList();
});
</script>
<style lang="scss" scoped>
.search-container {
  margin-bottom: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
