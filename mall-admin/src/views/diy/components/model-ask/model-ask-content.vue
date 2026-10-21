<template>
  <div class="content">
    <el-form :model="form" label-width="70" class="m-h">
      <common-content-top :value="form.content_top" />
      <div class="divider-line"></div>
      <card-container class="card-container">
        <div class="mb-12">展示设置</div>
        <el-form-item label="选择风格">
          <el-radio-group v-model="form.theme">
            <el-radio
              v-for="item in base_list.theme_list"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.theme == '2'" label="轮播列数">
          <el-radio-group v-model="form.carousel_col">
            <el-radio :value="1">单列展示</el-radio>
            <el-radio :value="2">两列展示</el-radio>
            <el-radio :value="3">三列展示</el-radio>
            <el-radio :value="4">四列展示</el-radio>
          </el-radio-group>
        </el-form-item>
      </card-container>
      <div class="divider-line"></div>
      <card-container class="card-container card-container-br">
        <div class="mb-12">问答设置</div>
        <!-- 数据筛选组件, 根据数据源类型显示不同的筛选组件 -->
        <data-filter
          type="ask"
          :value="form"
          :list="form.data_list"
          :base-list="base_list"
          @add="add"
          @data_list_replace="data_list_replace"
          @data_list_remove="data_list_remove"
          @data_list_sort="data_list_sort"
        />
      </card-container>
      <div class="divider-line"></div>
      <card-container class="card-container">
        <div class="mb-12">列表设置</div>
        <el-form-item label="是否显示">
          <el-checkbox-group v-model="form.is_show">
            <el-checkbox
              v-for="item in base_list.show_list.filter((item: any) =>
                item.show.includes(form.theme)
              )"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-checkbox
            >
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="标题显示">
          <el-radio-group v-model="form.title_display_method">
            <el-radio
              v-for="item in base_list.display_method"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
      </card-container>
    </el-form>
    <url-value-dialog
      v-model:dialog-visible="url_value_dialog_visible"
      :type="['ask']"
      :multiple="url_value_multiple_bool"
      @update:model-value="url_value_dialog_call_back"
    />
  </div>
</template>
<script setup lang="ts">
import { get_math } from "../../utils";
import { ask_row_wrap } from "../../utils/ask";
import CommonAPI from "../../api/common";
/**
 * @description: 商品问答（内容）
 * @param value{Object} 传过来的数据(content),用于数据渲染
 * @param styles{Object} 样式
 */
const props = defineProps({
  value: {
    type: Object,
    default: () => ({}),
  },
  styles: {
    type: Object,
    default: () => ({}),
  },
});
// 默认值
const state = reactive({
  form: props.value,
  data: props.styles,
});
// 如果需要解构，确保使用toRefs
const { form, data } = toRefs(state);
const base_list = reactive({
  theme_list: [
    { name: "单列展示", value: "0" },
    { name: "两列展示（纵向）", value: "1" },
    { name: "左右滑动展示", value: "2" },
  ],
  data_type_list: [
    { name: "指定问答", value: "0" },
    { name: "筛选问答", value: "1" },
  ],
  show_list: [
    { name: "回复状态", value: "reply_status", show: ["0", "1", "2"] },
    { name: "日期时间", value: "time", show: ["0", "1", "2"] },
    { name: "浏览量", value: "page_view", show: ["0", "1"] },
    { name: "TOP排名", value: "ranking", show: ["0"] },
  ],
  filter_list: [
    { name: "全部", value: "" },
    { name: "未回", value: "0" },
    { name: "已回", value: "1" },
  ],
  display_method: [
    { name: "自动", value: "0" },
    { name: "一行", value: "1" },
    { name: "两行", value: "2" },
  ],
});
onMounted(() => {
  // 指定问答:已存 data_ids 但快照为空(迁移/复制/旧页面等)时按 id 回显
  hydrate_data_list();
});
// 指定问答快照回显:data_ids → mall-cms ask/list(by-ids)拉取填充 data_list(复用指定行结构)
const hydrate_data_list = async () => {
  const cfg: any = form.value;
  const existList: any[] = cfg.data_list || [];
  const idsText: string = cfg.data_ids || "";
  if (cfg.data_type !== "0" || existList.length > 0 || !idsText) {
    return;
  }
  const ids = String(idsText)
    .split(",")
    .map((s: string) => parseInt(s.trim(), 10))
    .filter((n: number) => !isNaN(n));
  if (ids.length === 0) {
    return;
  }
  try {
    const res: any = await CommonAPI.getAskList({ ids });
    const rows: any[] = res?.data || [];
    if (rows.length > 0) {
      cfg.data_list = rows.map((child: any) => ask_row_wrap(child));
    }
  } catch (error) {
    console.error("问答指定数据回显失败:", error);
  }
};
// 移除
const data_list_remove = (index: number) => {
  form.value.data_list.splice(index, 1);
};
const url_value_multiple_bool = ref(true);
const data_list_replace_index = ref(0);
// 开启关闭链接
const url_value_dialog_visible = ref(false);
// 替换
const data_list_replace = (index: number) => {
  data_list_replace_index.value = index;
  url_value_multiple_bool.value = false;
  url_value_dialog_visible.value = true;
};
// 拖动排序
const data_list_sort = (item: any) => {
  form.value.data_list = item;
};
//  新增
const add = () => {
  url_value_multiple_bool.value = true;
  url_value_dialog_visible.value = true;
};
const url_value_dialog_call_back = (item: any[]) => {
  if (url_value_multiple_bool.value) {
    item.forEach((child: any) => {
      form.value.data_list.push(ask_row_wrap(child));
    });
  } else {
    form.value.data_list[data_list_replace_index.value] = {
      id: get_math(),
      new_title: "",
      new_cover:
        form.value.data_list[data_list_replace_index.value]?.new_cover || [],
      data: item[0],
    };
  }
};
</script>
<style lang="scss" scoped>
.content {
  width: 100%;
}
</style>
