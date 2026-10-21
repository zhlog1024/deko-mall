<script setup lang="ts">
defineOptions({
  name: "Supplier",
  inheritAttrs: false,
});

import {
  addSupplier,
  auditSupplier,
  getSupplier,
  getSupplierPages,
  updateSupplier,
} from "@/api/pms/supplier";

import {
  DialogType,
  Supplier,
  SupplierForm,
  SupplierPageQuery,
} from "@/api/pms/supplier/types";

const queryFormRef = ref(ElForm); // 属性名必须和元素的ref属性值一致
const dataFormRef = ref(ElForm); // 属性名必须和元素的ref属性值一致

const state = reactive({
  loading: true,
  // 选中ID数组
  ids: [],
  // 非单个禁用
  single: true,
  // 非多个禁用
  multiple: true,
  queryParams: {
    supplierId: "",
    keywords: "",
    pageNum: 1,
    pageSize: 10,
  } as SupplierPageQuery,
  supplierList: [] as Supplier[],
  total: 0,
  dialog: {} as DialogType,
  formData: {} as SupplierForm,
  isAdd: false, // 新增标识
  auditDialog: {
    visible: false,
    title: "供应商审核",
    auditStatus: 1, // 1-通过 2-拒绝
    remark: "",
    spuId: "",
  },
});

// 表单校验规则（结算相关规则随结算体系剥离已移除）
const rules = {};

const {
  loading,
  multiple,
  queryParams,
  supplierList,
  total,
  dialog,
  formData,
  auditDialog,
} = toRefs(state);

function handleQuery() {
  state.loading = true;
  getSupplierPages(state.queryParams).then(({ data }) => {
    state.supplierList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  queryFormRef.value.resetFields();
  handleQuery();
}

function handleSelectionChange(selection: any) {
  state.ids = selection.map((item: any) => item.id);
  state.single = selection.length !== 1;
  state.multiple = !selection.length;
}

function handleAdd() {
  state.dialog = { title: "添加商家信息", visible: true };
  state.isAdd = true;

  state.formData = {
  } as SupplierForm;
}

function handleUpdate(row: any) {
  state.dialog = { title: "修改商家信息", visible: true };
  state.isAdd = false;
  const supplierId = row.id || state.ids;
  getSupplier(supplierId).then(({ data }) => {
    state.formData = {
      ...data,
    };
  });
}

/**
 * 表单提交
 */
function submitForm() {
  dataFormRef.value.validate((isValid: boolean) => {
    if (isValid) {
      if (state.formData.id) {
        updateSupplier(state.formData.id, state.formData).then(() => {
          ElMessage.success("修改成功");
          cancel();
          handleQuery();
        });
      } else {
        addSupplier(state.formData).then(() => {
          ElMessage.success("新增成功");
          cancel();
          handleQuery();
        });
      }
    }
  });
}

/**
 * 取消
 */
function cancel() {
  state.dialog.visible = false;
  dataFormRef.value.resetFields();
}

/**
 * 审核
 */
function handleAudit(row: any) {
  state.auditDialog = {
    visible: true,
    title: `供应商审核 - ${row.supplierName}`,
    auditStatus: 1,
    remark: "",
    spuId: row.id,
  };

  state.formData = {
    ...row,
  };
}

function submitAudit() {
  if (!state.auditDialog.remark && state.auditDialog.auditStatus === 2) {
    ElMessage.warning("请填写备注");
    return;
  }

  auditSupplier(state.formData.id.toString(), {
    auditStatus: state.auditDialog.auditStatus,
    remark: state.auditDialog.remark,
  }).then(() => {
    ElMessage.success("审核提交成功");
    state.auditDialog.visible = false;
    handleQuery(); // 刷新列表
  });
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="keywords" label="供应商ID">
          <el-input v-model="queryParams.supplierId" placeholder="供应商ID" />
        </el-form-item>

        <el-form-item prop="keywords" label="关键字">
          <el-input v-model="queryParams.keywords" placeholder="账号/名称" />
        </el-form-item>

        <el-form-item label="审核状态">
          <el-select
            v-model="queryParams.auditStatus"
            class="filter-item"
            placeholder="请选择"
            clearable
            style="width: 200px"
          >
            <el-option label="待审核" value="0" />
            <el-option label="审核通过" value="1" />
            <el-option label="审核拒绝" value="2" />
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
      </template>
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="supplierList"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="id" label="供应商ID" />
        <el-table-column prop="loginName" label="登录账号" />
        <el-table-column prop="supplierName" label="供应商名称" />

        <el-table-column
          prop="businessLicense"
          label="营业执照"
          min-width="150"
        >
          <template #default="scope">
            <el-popover placement="right" :width="400" trigger="hover">
              <img
                :src="scope.row.businessLicense"
                width="400"
                height="400"
                alt=""
              />
              <template #reference>
                <img
                  :src="scope.row.businessLicense"
                  style="max-width: 60px; max-height: 60px"
                  alt=""
                />
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatusLabel" label="审核状态" />
        <el-table-column prop="remark" label="备注" />

        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="success" link @click="handleUpdate(scope.row)">
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleAudit(scope.row)"
              v-if="scope.row.auditStatus === 0"
            >
              审核
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

    <!-- 表单弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      top="5vh"
      width="600px"
    >
      <el-form ref="dataFormRef" :model="formData" label-width="100px">
        <el-form-item label="登录账号" prop="name">
          <el-input
            v-model="formData.loginName"
            auto-complete="off"
            :disabled="!state.isAdd"
          />
        </el-form-item>

        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="formData.supplierName" auto-complete="off" />
        </el-form-item>

        <el-form-item label="营业执照" prop="businessLicense">
          <single-upload v-model="formData.businessLicense" />
        </el-form-item>

        <el-form-item label="退货人">
          <el-input v-model="formData.returnContact" placeholder="退货时收件人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="退货电话">
          <el-input v-model="formData.returnPhone" placeholder="退货时联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="退货地址">
          <el-input v-model="formData.returnAddress" placeholder="退货时收货详细地址" maxlength="200" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
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

        <!-- 供应商填写的店铺信息 -->
        <el-divider content-position="left">店铺信息（入驻时填写）</el-divider>
        <el-form-item label="店铺Logo">
          <el-image
            v-if="formData.shopLogo"
            :src="formData.shopLogo"
            style="width: 80px; height: 80px"
            fit="cover"
            :preview-src-list="[formData.shopLogo]"
          />
          <span v-else class="text-muted">未上传</span>
        </el-form-item>
        <el-form-item label="店铺名称">
          <span>{{ formData.shopName || formData.supplierName || '-' }}</span>
        </el-form-item>
        <el-form-item label="店铺简介">
          <span>{{ formData.shopDescription || '-' }}</span>
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
