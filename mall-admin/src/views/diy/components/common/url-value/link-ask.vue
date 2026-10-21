<template>
  <!-- 商品问答选择 -->
  <div class="container">
    <div class="flex-row jc-e gap-20 mb-20 align-c">
      <el-select
        v-model="reply_status"
        class="search-w"
        placeholder="回复状态"
        clearable
        @change="handle_search"
      >
        <el-option label="未回" :value="0" />
        <el-option label="已回" :value="1" />
      </el-select>
      <el-input
        v-model="search_value"
        placeholder="请输入问题关键词"
        class="search-w"
        @change="handle_search"
      >
        <template #suffix>
          <icon
            name="search"
            size="16"
            color="9"
            class="c-pointer"
            @click="handle_search"
          />
        </template>
      </el-input>
    </div>
    <div class="content">
      <el-table
        v-loading="loading"
        :data="tableData"
        class="w"
        :header-cell-style="{ background: '#f7f7f7' }"
        row-key="id"
        height="438"
        fixed
        @row-click="row_click"
        @select="handle_select"
        @select-all="handle_select"
      >
        <el-table-column v-if="multiple" type="selection" width="60" />
        <el-table-column v-else label="#" width="60">
          <template #default="scope">
            <el-radio
              v-model="template_selection"
              :label="scope.$index + ''"
              >&nbsp;</el-radio
            >
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="问答" min-width="260">
          <template #default="scope">
            <div class="flex-row align-c gap-10">
              <image-empty
                v-if="scope.row.spuPic"
                v-model="scope.row.spuPic"
                class="img"
              />
              <div class="flex-col gap-3 flex-1 flex-width">
                <div class="text-line-1">{{ scope.row.title }}</div>
                <div v-if="scope.row.spuName" class="spu-name text-line-1">
                  关联:{{ scope.row.spuName }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="回复状态" width="90" align="center">
          <template #default="scope">
            <span :class="scope.row.isReply == 1 ? 'cr-success' : 'cr-danger'">{{
              scope.row.isReply == 1 ? "已回" : "未回"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="浏览" width="80" align="center">
          <template #default="scope">
            {{ scope.row.accessCount ?? 0 }}
          </template>
        </el-table-column>
        <template #empty>
          <no-data :text="empty_text" />
        </template>
      </el-table>
      <div class="mt-10 flex-row jc-e">
        <el-pagination
          :current-page="page"
          background
          :page-size="page_size"
          :pager-count="5"
          layout="prev, pager, next"
          :total="data_total"
          @current-change="get_list"
        />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { getAskPage } from "@/api/cms/ask";
const props = defineProps({
  // 重置
  reset: {
    type: Boolean,
    default: () => false,
  },
  multiple: {
    type: Boolean,
    default: () => false,
  },
  // 判断是否返回链接地址(跳问答详情页)
  selectIsUrl: {
    type: Boolean,
    default: false,
  },
});
watch(
  () => props.reset,
  (val) => {
    if (val) {
      init();
    }
  }
);
onMounted(() => {
  init();
});
const modelValue = defineModel({ type: Object, default: {} });
const tableData = ref<any[]>([]);
const search_value = ref("");
const reply_status = ref<number | undefined>(undefined);
const loading = ref(false);
const init = () => {
  template_selection.value = "";
  reply_status.value = undefined;
  search_value.value = "";
  get_list(1);
};
const handle_search = () => {
  get_list(1);
};
const template_selection = ref("");
//#region 分页 -----------------------------------------------start
// 当前页
const page = ref(1);
// 每页数量
const page_size = ref(30);
// 总数量
const data_total = ref(0);
const empty_text = ref("暂无数据");
// 查询文件(DIY 只展示已确认问答 isEnable=1)
const get_list = (new_page: number) => {
  loading.value = true;
  getAskPage({
    pageNum: new_page,
    pageSize: page_size.value,
    keyword: search_value.value || undefined,
    isReply: reply_status.value,
    isEnable: 1,  })
    .then((res: any) => {
      tableData.value = res.data.list || [];
      if (tableData.value.length === 0) {
        empty_text.value = "暂无数据";
      }
      data_total.value = res.data.total || 0;
      page.value = new_page;
      loading.value = false;
    })
    .catch(() => {
      tableData.value = [];
      data_total.value = 0;
      page.value = 1;
      empty_text.value = "加载失败";
      loading.value = false;
    });
};
//#region 分页 -----------------------------------------------end
const row_click = (row: any) => {
  if (!props.multiple) {
    const new_table_data = JSON.parse(JSON.stringify(tableData.value));
    template_selection.value = new_table_data
      .findIndex((item: any) => item.id == row.id)
      .toString();
    if (props.selectIsUrl) {
      const page_url = "/pages/plugins/ask/detail/detail?id=" + row.id;
      modelValue.value = [
        {
          id: row.id,
          name: row.title || page_url,
          page: page_url,
        },
      ];
    } else {
      modelValue.value = [row];
    }
  }
};
const handle_select = (selection: any) => {
  if (props.selectIsUrl) {
    // 遍历数组selection
    const new_selection = selection.map((item: any) => {
      const page_url = "/pages/plugins/ask/detail/detail?id=" + item.id;
      return {
        id: item.id,
        name: item.title || page_url,
        page: page_url,
      };
    });
    modelValue.value = new_selection;
  } else {
    modelValue.value = selection;
  }
};
</script>
<style lang="scss" scoped>
.container {
  .search-w {
    width: 22.5rem;
  }
  .content {
    :deep(.el-table__inner-wrapper:before) {
      background-color: transparent;
    }
    .img {
      width: 3.6rem;
      height: 3.6rem;
      border-radius: 0.4rem;
    }
    .spu-name {
      font-size: 1.2rem;
      color: #999;
    }
  }
}
</style>
