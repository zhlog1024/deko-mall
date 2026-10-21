<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="memberId">
          <el-input
            v-model="queryParams.memberId"
            placeholder="会员ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="nickName">
          <el-input
            v-model="queryParams.nickName"
            placeholder="会员昵称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="mobile">
          <el-input
            v-model="queryParams.mobile"
            placeholder="手机号码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status" style="width: 120px">
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            @change="handleQuery"
            style="width: 100%"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <i-ep-search />搜索
          </el-button>
          <el-button @click="resetQuery">
            <i-ep-refresh />重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table v-loading="loading" :data="memberList" border style="width: 100%">
        <el-table-column label="会员ID" prop="id" width="100" align="center" />
        <el-table-column prop="nickName" label="昵称" min-width="120" align="center" />
        <el-table-column prop="mobile" label="手机号码" width="130" />
        <el-table-column prop="tagName" label="标签" width="120">
          <template #default="scope">
            <el-tag
              v-if="scope.row.tagName && scope.row.tagName !== 0 && scope.row.tagName !== '0'"
              type="success"
            >
              {{ scope.row.tagName }}
            </el-tag>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" width="80" label="状态" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" fixed="right" width="200" align="center">
          <template #default="scope">
            <div class="action-cell">
              <el-button type="primary" link size="small" @click="handleAsset(scope.row)">
                <i-ep-wallet-filled /> 会员资产
              </el-button>
              <el-button type="primary" link size="small" @click="handleTagEdit(scope.row)">
                <i-ep-price-tag /> 标签维护
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />

    <el-dialog v-model="tagDialogVisible" title="会员标签维护" width="30%">
      <el-form :model="currentMember" label-width="80px">
        <el-form-item label="标签名称">
          <el-input
            v-model="currentMember.tagName"
            placeholder="请输入标签名称"
            clearable
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tagDialogVisible = false">
            <i-ep-close class="button-icon" />取 消
          </el-button>
          <el-button type="primary" @click="saveTag">
            <i-ep-check class="button-icon" />确 定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "Member",
  inheritAttrs: false,
});

import { getMemberPage, updateMember } from "@/api/ums/member";
import { Member, MemberQuery } from "@/api/ums/member/types";
import { useRouter } from "vue-router";

const router = useRouter();
const queryFormRef = ref(ElForm);

const state = reactive({
  loading: true,
  total: 0,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  } as MemberQuery,
  memberList: [] as Member[],
});

const { loading, queryParams, memberList, total } = toRefs(state);

function handleQuery() {
  state.loading = true;
  getMemberPage(state.queryParams)
    .then(({ data }) => {
      state.memberList = data.list;
      state.total = data.total;
    })
    .finally(() => {
      state.loading = false;
    });
}

function resetQuery() {
  queryFormRef.value?.resetFields?.();
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
  } as MemberQuery;
  handleQuery();
}

const handleAsset = (row: Member) => {
  router.push({
    path: "/ums/assets",
    query: { userId: row.id },
  });
};

const tagDialogVisible = ref(false);
const currentMember = ref<{ id: string; tagName: string }>({
  id: "",
  tagName: "",
});

const handleTagEdit = (row: Member) => {
  let tagVal: any = row.tagName;
  if (tagVal === 0 || tagVal === "0" || !tagVal) {
    tagVal = "";
  }
  currentMember.value = {
    id: String(row.id),
    tagName: tagVal,
  };
  tagDialogVisible.value = true;
};

const saveTag = async () => {
  if (!currentMember.value.tagName) {
    ElMessage.warning("请输入标签名称");
    return;
  }
  try {
    const res = await updateMember(Number(currentMember.value.id), {
      id: Number(currentMember.value.id),
      tagName: currentMember.value.tagName,
    });
    if (res.code === "00000") {
      ElMessage.success("标签保存成功");
      tagDialogVisible.value = false;
      handleQuery();
    }
  } catch (error) {
    console.error("保存标签失败", error);
    ElMessage.error("标签保存失败");
  }
};

onMounted(() => {
  handleQuery();
});
</script>

<style lang="scss" scoped>
.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;

  .el-button {
    padding: 4px 8px;
    white-space: nowrap;
  }
}
</style>
