<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="query" :inline="true">
        <el-form-item prop="name" label="模板名称">
          <el-input
            v-model="query.templateName"
            placeholder="请输入模板名称"
            clearable
            @keyup.enter="fetchList"
          />
        </el-form-item>
        <el-form-item prop="isDefault" label="默认模板" style="width: 150px">
          <el-select v-model="query.isDefault" placeholder="全部" clearable>
            <el-option :value="1" label="是" />
            <el-option :value="0" label="否" />
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
          新建模板
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
          prop="templateName"
          label="模板名称"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column label="默认模板" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault === 1" type="success"
              >默认
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="是否包邮" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isFreeShipping === 1">包邮</el-tag>
            <el-tag v-else type="info">不包邮</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计费类型" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">按件计费</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="运费规则" min-width="200">
          <template #default="scope">
            <div v-if="scope.row.rules && scope.row.rules.length > 0">
              <!-- 如果规则数量过多，只展示部分 -->
              <el-popover
                placement="right"
                :width="400"
                trigger="hover"
                v-if="scope.row.rules.length > 2"
              >
                <template #reference>
                  <div class="fee-brief">
                    <div class="fee-info">
                      <div class="fee-item">
                        <span class="fee-label">首规则：</span>
                        <span class="fee-value"
                          >首{{ scope.row.rules[0].firstItem }}件 ¥{{scope.row.rules[0].firstFee}}</span
                        >
                      </div>
                      <div class="rules-count">
                        共 {{ scope.row.rules.length }} 条规则，悬停查看全部
                      </div>
                    </div>
                  </div>
                </template>
                <!-- 弹出的完整规则列表 -->
                <div class="rules-popover">
                  <div
                    v-for="(rule, index) in scope.row.rules"
                    :key="index"
                    class="rule-item"
                  >
                    <div class="rule-header">规则 {{ index + 1 }}</div>
                    <div class="rule-region">
                      适用区域：{{ rule.regionNames || "未指定" }}
                    </div>
                    <div class="rule-fees">
                      <span
                        >首{{ rule.firstItem }}件：¥{{rule.firstFee}}</span
                      >
                      <span class="divider">|</span>
                      <span
                        >续{{ rule.continueItem }}件：¥{{rule.continueFee}}</span
                      >
                    </div>
                  </div>
                </div>
              </el-popover>

              <!-- 如果规则数量少，直接显示 -->
              <div class="fee-list" v-else>
                <div
                  class="fee-info"
                  v-for="(rule, index) in scope.row.rules"
                  :key="index"
                >
                  <div class="fee-region">
                    区域 {{ index + 1 }}：{{ rule.regionNames || "未指定" }}
                  </div>
                  <div class="fee-details">
                    <span
                      >首{{ rule.firstItem }}件：¥{{rule.firstFee}}</span
                    >
                    <span class="divider">|</span>
                    <span
                      >续{{ rule.continueItem }}件：¥{{rule.continueFee}}</span
                    >
                  </div>
                </div>
              </div>
            </div>
            <div class="empty-info" v-else>暂无运费规则</div>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="160"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="success"
              link
              @click.stop="handleDetail(scope.row.id)"
              >详情
            </el-button>

            <el-button link type="primary" @click="handleEdit(scope.row.id)">
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.isDefault !== 1"
            >
              删除
            </el-button>
            <el-button
              link
              type="success"
              @click="handleSetDefault(scope.row.id)"
              v-if="scope.row.isDefault !== 1"
            >
              设为默认
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
  deleteFreightTemplate,
  getFreightTemplatePages,
  setDefaultFreightTemplate,
} from "@/api/pms/freight-template";
import {
  FreightTemplate,
  FreightTemplatePageQuery,
} from "@/api/pms/freight-template/types";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";

defineOptions({
  name: "FreightTemplate",
  inheritAttrs: false,
});

const router = useRouter();
const queryFormRef = ref();
const loading = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const list = ref<FreightTemplate[]>([]);
const total = ref(0);
const query = reactive<FreightTemplatePageQuery>({
  pageNum: 1,
  pageSize: 10,
  templateName: "",
  chargeType: "BY_PIECE", // 固定为按件计费
  isDefault: undefined,
});

/**
 * 获取运费模板列表
 */
function fetchList() {
  loading.value = true;
  getFreightTemplatePages(query)
    .then(({ data }) => {
      // 处理数据，确保数据一致性
      list.value = data.list.map((item) => ({
        ...item,
        chargeType: "BY_PIECE", // 确保所有数据的计费类型为按件计费
      }));
      total.value = data.total;
      loading.value = false;
    })
    .catch(() => {
      loading.value = false;
    });
}

/**
 * 重置查询条件
 */
function resetQuery() {
  queryFormRef.value?.resetFields();
  query.pageNum = 1;
  query.chargeType = "BY_PIECE"; // 重置时确保计费类型固定为按件计费
  fetchList();
}

/**
 * 表格选择变化
 */
function handleSelectionChange(selection: any) {
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

  ElMessageBox.confirm("确定要删除选中的运费模板吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      // 实际需要实现批量删除API
      const promises = ids.value.map((id) => deleteFreightTemplate(String(id)));
      Promise.all(promises).then(() => {
        ElMessage.success("批量删除成功");
        fetchList();
      });
    })
    .catch(() => {});
}

/**
 * 新建运费模板
 */
function handleCreate() {
  router.push({ path: "freight-template-detail", query: { mode: "create" } });
}

/**
 * 编辑运费模板
 */
function handleEdit(id: number) {
  router.push({ path: "freight-template-detail", query: { id, mode: "edit" } });
}

/**
 * 查看运费模板详情
 */
function handleDetail(id: number) {
  router.push({ path: "freight-template-detail", query: { id, mode: "view" } });
}

/**
 * 删除运费模板
 */
function handleDelete(id: number) {
  deleteFreightTemplate(String(id))
    .then(() => {
      ElMessage.success("删除成功");
      fetchList();
    })
    .catch(() => {
      ElMessage.error("删除失败");
    });
}

/**
 * 设置为默认运费模板
 */
function handleSetDefault(id: number) {
  setDefaultFreightTemplate(id)
    .then(() => {
      ElMessage.success("设置默认成功");
      fetchList();
    })
    .catch(() => {
      ElMessage.error("设置默认失败");
    });
}

onMounted(fetchList);
</script>
<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  display: flex;
  flex-direction: column;
}

.page-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.search-card {
  margin-bottom: 16px;
}

.list-card {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.left-actions {
  display: flex;
  align-items: center;
}

.selection-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #606266;
}

.fee-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.fee-item {
  display: flex;
  align-items: center;
}

.fee-label {
  color: #606266;
  margin-right: 4px;
}

.fee-value {
  color: #303133;
  font-weight: 500;
}

.fee-brief {
  cursor: pointer;
}

.fee-brief:hover {
  text-decoration: underline;
  color: #409eff;
}

.rules-count {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.rule-item {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.rule-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.rule-header {
  font-weight: 600;
  margin-bottom: 6px;
  color: #303133;
}

.rule-region {
  margin-bottom: 6px;
  color: #606266;
}

.rule-fees {
  color: #303133;
}

.divider {
  margin: 0 6px;
  color: #dcdfe6;
}

.fee-region {
  font-size: 13px;
  margin-bottom: 4px;
  color: #606266;
}

.fee-details {
  font-size: 12px;
}

.empty-info {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
