<template>
  <div class="spu-group-page">
    <!-- 左侧分组列表 -->
    <div class="left-panel">
      <div class="panel-header">
        <span class="panel-title">商品分组</span>
        <el-button type="primary" size="small" @click="handleCreate">
          <i-ep-plus /> 新建
        </el-button>
      </div>
      <el-input
        v-model="searchKey"
        placeholder="搜索分组名称"
        clearable
        class="search-input"
      >
        <template #prefix><i-ep-search /></template>
      </el-input>
      <div class="group-list" v-loading="listLoading">
        <div
          v-for="group in filteredGroups"
          :key="group.id"
          :class="['group-item', { active: currentGroup?.id === group.id }]"
          @click="selectGroup(group)"
        >
          <img :src="group.iconUrl || defaultIcon" class="group-icon" />
          <div class="group-info">
            <span class="group-name">{{ group.name }}</span>
            <span class="group-count">{{ group.spuCount || 0 }} 个商品</span>
            <span class="group-id-text">ID: {{ group.id }}</span>
          </div>
          <el-switch
            :model-value="group.status === 1"
            size="small"
            @change="(val: any) => toggleStatus(group, val)"
            @click.stop
          />
        </div>
        <el-empty
          v-if="!listLoading && filteredGroups.length === 0"
          :description="searchKey ? '无匹配分组' : '暂无分组，点击新建'"
        />
      </div>
    </div>

    <!-- 右侧详情面板 -->
    <div class="right-panel" v-loading="detailLoading">
      <template v-if="currentGroup">
        <div class="detail-header">
          <span class="detail-title">{{ isNew ? '新建分组' : '编辑分组' }}</span>
          <span v-if="!isNew" class="detail-id">ID: {{ currentGroup.id }}</span>
        </div>
        <div class="detail-body">
          <!-- 基本信息 -->
          <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" class="info-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="分组名称" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入" maxlength="20" show-word-limit />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="展示排序">
                  <el-input-number v-model="formData.sort" :min="0" :max="9999" controls-position="right" @change="markDirty" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-switch v-model="formData.statusBool" active-text="启用" inactive-text="禁用" @change="markDirty" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="描述">
                  <el-input v-model="formData.description" placeholder="备注说明" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="分组图标">
                  <SingleUpload
                    v-model="formData.iconUrl"
                    :width="100"
                    :height="100"
                    @update:modelValue="markDirty"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <!-- 商品列表 -->
          <div class="product-section">
            <div class="product-header">
              <span class="section-label">商品列表（{{ productList.length }} 个）</span>
              <div class="product-header-actions">
                <el-button
                  v-if="checkedCount > 0"
                  type="danger"
                  size="small"
                  plain
                  @click="batchRemoveProducts"
                >
                  移除选中（{{ checkedCount }}）
                </el-button>
                <el-button type="primary" size="small" @click="showProductDialog">
                  <i-ep-plus /> 搜索添加
                </el-button>
                <el-button size="small" @click="showImportDialog">
                  批量导入
                </el-button>
              </div>
            </div>
            <table class="product-table">
              <thead>
                <tr>
                  <th style="width:45px"><el-checkbox v-model="allSelected" :indeterminate="isIndeterminate" @change="(val: any) => toggleAll(val)" /></th>
                  <th style="width:80px">商品ID</th>
                  <th style="width:70px">图片</th>
                  <th>商品名称</th>
                  <th style="width:80px">类型</th>
                  <th style="width:100px">零售价</th>
                  <th style="width:48px">排序</th>
                  <th style="width:70px">操作</th>
                </tr>
              </thead>
              <VueDraggable
                v-model="productList"
                tag="tbody"
                :animation="300"
                ghost-class="product-ghost"
                handle=".drag-handle"
                @sort="onSort"
              >
                <tr v-for="(item, index) in productList" :key="item.id" class="product-row">
                  <td align="center">
                    <el-checkbox v-model="item._checked" @change="onItemCheckChange" />
                  </td>
                  <td align="center" class="product-id">{{ item.id }}</td>
                  <td align="center">
                    <el-popover placement="right" :width="300" trigger="hover">
                      <img :src="item.picUrl" style="width:300px;height:300px;object-fit:contain" />
                      <template #reference>
                        <img :src="item.picUrl" class="product-thumb" />
                      </template>
                    </el-popover>
                  </td>
                  <td><span class="product-name" :title="item.name">{{ item.name }}</span></td>
                  <td align="center" class="product-meta">{{ item.spuTypeDesc }}</td>
                  <td align="center">{{ '¥' + item.price }}</td>
                  <td align="center">
                    <div class="drag-handle"><i-ep-rank /></div>
                  </td>
                  <td align="center">
                    <el-button size="small" type="danger" circle @click="removeProduct(index)">
                      <i-ep-delete />
                    </el-button>
                  </td>
                </tr>
              </VueDraggable>
            </table>
          </div>
        </div>
        <div class="detail-footer">
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button v-if="!isNew" type="danger" plain @click="handleDelete">删除分组</el-button>
        </div>
      </template>
      <el-empty v-else description="选择左侧分组或点击新建" class="empty-state" />
    </div>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importVisible" title="批量导入商品" width="480px" :close-on-click-modal="false">
      <div class="import-dialog">
        <p class="import-tip">粘贴商品ID，支持换行、逗号、分号分隔：</p>
        <el-input
          v-model="importText"
          type="textarea"
          :rows="10"
          placeholder="1001, 1002, 1003&#10;或每行一个&#10;1001&#10;1002&#10;1003"
        />
        <p v-if="importResult" class="import-feedback">
          识别到 <b>{{ importResult.matched }}</b> 个有效商品<template v-if="importResult.invalid">，{{ importResult.invalid }} 个 ID 未匹配</template>
        </p>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- 商品选择弹窗 -->
    <SelectContentList
      v-if="productDialogVisible"
      :dialogVisible="productDialogVisible"
      :multiple="true"
      :selectedIds="productList.map((item: any) => item.id)"
      @close="productDialogVisible = false"
      @confirm="handleProductConfirm"
    />
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'SpuGroup' })

import SelectContentList from '@/components/SelectContentList/index.vue'
import SingleUpload from '@/components/Upload/SingleUpload.vue'
import { VueDraggable } from 'vue-draggable-plus'
import {
  getSpuGroups,
  getSpuGroupDetail,
  getSpuGroupSpus,
  createSpuGroup,
  updateSpuGroup,
  deleteSpuGroup,
  batchGetSpuInfo,
} from '@/api/pms/spu-group'
import { ElMessage, ElMessageBox } from 'element-plus'
import { nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const defaultIcon = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHJ4PSI0IiBmaWxsPSIjZTVlN2ViIi8+PHBhdGggZD0iTTEwIDE2TDE0IDIwTDIyIDEyIiBzdHJva2U9IiM5Y2EzYWYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIi8+PC9zdmc+'

// state
const listLoading = ref(false)
const saving = ref(false)
const detailLoading = ref(false)
const dirty = ref(false)
const skipDirtyWatch = ref(false)
const searchKey = ref('')
const groupList = ref<any[]>([])
const currentGroup = ref<any>(null)
const isNew = ref(false)
const productList = ref<any[]>([])
const productDialogVisible = ref(false)
const importVisible = ref(false)
const importText = ref('')
const importLoading = ref(false)
const importResult = ref<{ matched: number; invalid: number } | null>(null)
const formRef = ref<FormInstance>()
let selectRequestId = 0

const formData = reactive({
  name: '',
  description: '',
  iconUrl: '',
  sort: 0,
  statusBool: true,
  isSystem: 0,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
}

const filteredGroups = computed(() => {
  if (!searchKey.value) return groupList.value
  const kw = searchKey.value.toLowerCase()
  return groupList.value.filter((g: any) => g.name?.toLowerCase().includes(kw))
})

// dirty tracking
function markDirty() {
  dirty.value = true
}

watch(
  () => ({ ...formData }),
  () => { if (!skipDirtyWatch.value) dirty.value = true },
  { deep: true },
)

async function confirmDiscard(): Promise<boolean> {
  if (!dirty.value) return true
  try {
    await ElMessageBox.confirm('当前内容未保存，是否放弃？', '提示', {
      confirmButtonText: '放弃',
      cancelButtonText: '继续编辑',
      type: 'warning',
    })
    dirty.value = false
    return true
  } catch {
    return false
  }
}

// data loading
function loadGroupList() {
  listLoading.value = true
  getSpuGroups({ pageNum: 1, pageSize: 100 }).then((res) => {
    groupList.value = res.data.records || []
    listLoading.value = false
  })
}

async function selectGroup(group: any) {
  if (!(await confirmDiscard())) return

  isNew.value = false
  detailLoading.value = true
  currentGroup.value = group

  const requestId = ++selectRequestId

  try {
    const [detailRes, spuRes] = await Promise.all([
      getSpuGroupDetail(group.id),
      getSpuGroupSpus(group.id),
    ])
    if (requestId !== selectRequestId) return

    skipDirtyWatch.value = true
    formData.name = detailRes.data.name || ''
    formData.description = detailRes.data.description || ''
    formData.iconUrl = detailRes.data.iconUrl || ''
    formData.sort = detailRes.data.sort ?? 0
    formData.statusBool = detailRes.data.status !== 0
    formData.isSystem = detailRes.data.isSystem ?? 0
    productList.value = (spuRes.data || []).map((item: any) => ({ ...item, _checked: false }))
    await nextTick()
    skipDirtyWatch.value = false
    dirty.value = false
  } finally {
    if (requestId === selectRequestId) {
      detailLoading.value = false
    }
  }
}

// create
async function handleCreate() {
  if (!(await confirmDiscard())) return

  skipDirtyWatch.value = true
  isNew.value = true
  currentGroup.value = { id: null }
  formData.name = ''
  formData.description = ''
  formData.iconUrl = ''
  formData.sort = 0
  formData.statusBool = true
  formData.isSystem = 0
  productList.value = []

  await nextTick()
  skipDirtyWatch.value = false
  dirty.value = false
}

// status toggle
function toggleStatus(group: any, val: boolean) {
  const prevStatus = group.status
  group.status = val ? 1 : 0
  updateSpuGroup(group.id, {
    spuGroup: { id: group.id, status: val ? 1 : 0 },
    spuList: [],
  }).catch(() => {
    group.status = prevStatus
    ElMessage.error('状态切换失败')
  })
}


// product management
function showProductDialog() {
  productDialogVisible.value = true
}

/** 统一选品组件确认：全量替换分组商品（保留未取消的旧行，含排序等本地状态） */
function handleProductConfirm(rows: any[], payload: { ids: number[] }) {
  const keepIds = new Set((payload && payload.ids) || [])
  const kept = productList.value.filter((item: any) => keepIds.has(item.id))
  const keptIds = new Set(kept.map((item: any) => item.id))
  const added = rows
    .filter((item: any) => !keptIds.has(item.id))
    .map((item: any) => ({ ...item, _checked: false }))
  productList.value = [...kept, ...added]
  dirty.value = true
  productDialogVisible.value = false
}

// checkbox helpers
const checkedCount = computed(() => productList.value.filter((p: any) => p._checked).length)
const allSelected = computed(() => productList.value.length > 0 && productList.value.every((p: any) => p._checked))
const isIndeterminate = computed(() => productList.value.some((p: any) => p._checked) && !allSelected.value)

function toggleAll(val: boolean) {
  productList.value.forEach((p: any) => (p._checked = val))
}

function onItemCheckChange() {
  // _checked is already reactive; checkedCount updates automatically
}

// drag sort
function onSort() {
  dirty.value = true
}

function removeProduct(index: number) {
  productList.value.splice(index, 1)
  dirty.value = true
}

// import
function showImportDialog() {
  importText.value = ''
  importResult.value = null
  importVisible.value = true
}

async function handleImport() {
  const raw = importText.value.trim()
  if (!raw) {
    ElMessage.warning('请粘贴 SPU ID')
    return
  }
  const ids = raw
    .split(/[\n,;\s]+/)
    .map((s: string) => Number(s.trim()))
    .filter((n: number) => Number.isFinite(n) && n > 0)

  if (ids.length === 0) {
    ElMessage.warning('未识别到有效 ID')
    return
  }

  importLoading.value = true
  try {
    const res = await batchGetSpuInfo(ids)
    const matched: any[] = res.data || []
    const existingIds = new Set(productList.value.map((item: any) => item.id))
    const newItems = matched.filter((item: any) => !existingIds.has(item.id))
    newItems.forEach((item: any) => (item._checked = false))

    importResult.value = {
      matched: matched.length,
      invalid: ids.length - matched.length,
    }

    if (newItems.length === 0) {
      ElMessage.warning('所有商品已在列表中或 ID 无效')
      return
    }

    productList.value = [...productList.value, ...newItems]
    dirty.value = true
    ElMessage.success(`成功导入 ${newItems.length} 个商品`)
    importVisible.value = false
  } finally {
    importLoading.value = false
  }
}

function batchRemoveProducts() {
  productList.value = productList.value.filter((item: any) => !item._checked)

  dirty.value = true
}

// save / delete
async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    saving.value = true
    const spuGroup = {
      name: formData.name,
      description: formData.description || undefined,
      iconUrl: formData.iconUrl || undefined,
      sort: formData.sort,
      status: formData.statusBool ? 1 : 0,
    }
    const spuList = productList.value.map((item: any, idx: number) => ({
      spuId: item.id,
      sort: idx,
    }))
    const params = { spuGroup, spuList }

    const request = isNew.value
      ? createSpuGroup(params)
      : updateSpuGroup(currentGroup.value.id, params)

    request.then((res: any) => {
      if (res.code === '00000') {
        ElMessage.success(isNew.value ? '创建成功' : '保存成功')
        dirty.value = false
        if (isNew.value) {
          loadGroupList()
          setTimeout(() => {
            const newGroup = groupList.value.find((g: any) => g.name === formData.name)
            if (newGroup) selectGroup(newGroup)
          }, 300)
        } else {
          currentGroup.value.name = formData.name
          currentGroup.value.spuCount = productList.value.length
          loadGroupList()
        }
      }
    }).finally(() => {
      saving.value = false
    })
  })
}

function handleDelete() {
  const name = currentGroup.value?.name || '该分组'
  const count = productList.value.length
  ElMessageBox.confirm(
    `确定删除分组「${name}」？分组内 ${count} 个商品的关联也将清除。`,
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' },
  ).then(() => {
    deleteSpuGroup(currentGroup.value.id).then(() => {
      ElMessage.success('删除成功')
      currentGroup.value = null
      productList.value = []
      loadGroupList()
    })
  })
}

onMounted(() => {
  loadGroupList()
})
</script>

<style lang="scss" scoped>
.spu-group-page {
  display: flex;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

// left panel
.left-panel {
  width: 300px;
  min-width: 300px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.search-input {
  margin: 12px;
  width: auto;
}

.group-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #ecf5ff;
  }

  &.active {
    background: #d9ecff;
  }
}

.group-icon {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.group-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.group-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-count {
  font-size: 12px;
  color: #909399;
}

.group-id-text {
  font-size: 12px;
  color: #c0c4cc;
}

// right panel
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.detail-id {
  font-size: 13px;
  color: #909399;
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.info-form {
  margin-bottom: 24px;
}

.product-section {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.product-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.product-header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.product-thumb {
  width: 45px;
  height: 45px;
  object-fit: cover;
  border-radius: 4px;
}

.drag-handle {
  cursor: grab;
  font-size: 18px;
  color: #c0c4cc;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;

  &:hover {
    color: #909399;
    background: #f0f2f5;
  }

  &:active {
    cursor: grabbing;
    color: #409eff;
  }
}

.product-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ebeef5;

  th, td {
    padding: 8px 8px;
    border-bottom: 1px solid #ebeef5;
    font-size: 14px;
  }

  th {
    background: #fafafa;
    color: #909399;
    font-weight: 500;
  }

  .product-row {
    transition: background 0.15s;

    &:hover {
      background: #f5f7fa;
    }
  }

  .product-id {
    font-size: 13px;
    color: #909399;
  }

  .product-name {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .product-meta {
    font-size: 13px;
    color: #606266;
  }
}

.product-ghost {
  opacity: 0.4;
  background: #ecf5ff;
}

.detail-footer {
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
}

.empty-state {
  margin: auto;
}
</style>
