
<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item>
          <el-input
            v-model="queryParams.nickName"
            placeholder="会员昵称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"
            ><i-ep-search />搜索</el-button
          >
          <el-button @click="resetQuery"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      v-loading="loading"
      :data="memberList"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="expand" width="120" label="会员地址">
        <template #default="scope">
          <el-table :data="scope.row.addressList" size="small" border>
            <el-table-column
              type="index"
              label="序号"
              width="100"
              align="center"
            />
            <el-table-column
              align="center"
              label="收货人"
              prop="consigneeName"
            />
            <el-table-column
              align="center"
              label="联系方式"
              prop="consigneeMobile"
            />
            <el-table-column align="center" label="收货地址">
              <template #default="scope">
                {{
                  scope.row.provinceName +
                  scope.row.cityName +
                  scope.row.areaName +
                  scope.row.townName +
                  scope.row.detailAddress
                }}
              </template>
            </el-table-column>
            <el-table-column align="center" label="是否默认">
              <template #default>
                <el-tag v-if="scope.row.defaulted == 1" type="success"
                  >是</el-tag
                >
                <el-tag v-if="scope.row.defaulted == 0" type="info">否</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-table-column>

      <el-table-column label="会员ID" prop="id" width="100" align="center" />
      <el-table-column prop="nickName" label="昵称" />
      <el-table-column label="头像" width="100">
        <template #default="scope">
          <el-popover placement="right" :width="400" trigger="hover">
            <img :src="scope.row.avatarUrl" width="400" height="400" />
            <template #reference>
              <img
                :src="scope.row.avatarUrl"
                style="max-width: 60px; max-height: 60px"
              />
            </template>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="mobile" label="手机号码" />
      <el-table-column prop="status" width="80" label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="注册时间" />
    </el-table>

    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />
  </div>
</template>
<script setup lang="ts">
defineOptions({
  name: "Member",
  inheritAttrs: false,
});

import { getMemberPage } from "@/api/ums/member";
import { Member, MemberQuery } from "@/api/ums/member/types";

const state = reactive({
  loading: true,
  ids: [],
  single: true,
  multiple: true,
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
  getMemberPage(state.queryParams).then(({ data }) => {
    state.memberList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
    nickName: "",
  };
  handleQuery();
}

function handleSelectionChange(selection: any) {
  state.ids = selection.map((item: { id: any }) => item.id);
  state.single = selection.length != 1;
  state.multiple = !selection.length;
}

onMounted(() => {
  handleQuery();
});
</script>
