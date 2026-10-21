<template>
  <div class="content">
    <el-form :model="form" label-width="74" class="m-h">
      <common-content-top :value="form.content_top" />
      <div class="divider-line"></div>
      <card-container class="card-container">
        <div class="mb-12">展示设置</div>
        <el-form-item label="对齐方式">
          <el-radio-group v-model="form.justification">
            <el-radio value="left">居左</el-radio>
            <el-radio value="center">居中</el-radio>
            <el-radio value="right">居右</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选项卡置顶">
          <div class="flex-row align-c gap-10">
            <el-switch
              v-model="form.tabs_top_up"
              active-value="1"
              inactive-value="0"
            />
            <tooltip
              content="1.滑动置顶仅手机端有效<br/>2.滑动置顶后通用的圆角不生效"
            />
          </div>
        </el-form-item>
        <template v-if="form.tabs_top_up == '1' && is_not_general_safe_distance">
          <el-form-item label="安全距离">
            <div class="flex-row align-c gap-10">
              <el-switch
                v-model="form.is_general_safe_distance"
                active-value="1"
                inactive-value="0"
              />
              <tooltip content="选项卡置顶是否需要安全距离" />
            </div>
          </el-form-item>
        </template>
        <el-form-item label="选项卡风格">
          <el-radio-group
            v-model="form.tabs_theme"
            @change="tabs_theme_change"
          >
            <el-radio
              v-for="item in base_list.tabs_theme_list"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.tabs_theme == '3'" label="选中图标">
          <upload
            v-model="form.tabs_adorn_img"
            v-model:icon-value="form.tabs_adorn_icon"
            is-icon
            :limit="1"
            size="50"
          />
        </el-form-item>
        <el-form-item label="选择风格">
          <el-radio-group
            v-model="form.plugins_video_theme"
            @change="plugins_video_theme_change"
          >
            <el-radio
              v-for="item in base_list.plugins_video_theme_list"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="form.plugins_video_theme == '4'"
          label="轮播列数"
        >
          <el-radio-group v-model="form.plugins_video_carousel_col">
            <el-radio
              v-for="item in base_list.carousel_col_list"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
      </card-container>
      <div class="divider-line"></div>
      <card-container class="card-container">
        <div class="mb-12">选项卡设置</div>
        <div class="nav-list">
          <drag
            :data="form.tabs_list"
            type="card"
            icon-position="top"
            :space-col="20"
            @click="tabs_list_click"
            @remove="tabs_list_remove"
            @on-sort="tabs_list_sort"
          >
            <template #default="{ row, index }">
              <div class="flex-col w">
                <el-form-item label="显示类型" class="w mb-10">
                  <div class="flex-col gap-10 w h">
                    <el-radio-group v-model="row.tabs_type">
                      <el-radio value="0">文本</el-radio>
                      <el-radio value="1">图片/图标</el-radio>
                    </el-radio-group>
                    <template v-if="row.tabs_type == '1'">
                      <upload
                        v-model="row.tabs_img"
                        v-model:icon-value="row.tabs_icon"
                        is-icon
                        :limit="1"
                        size="50"
                      />
                    </template>
                    <template v-else>
                      <el-input
                        v-model="row.title"
                        placeholder="请输入标题文字"
                        clearable
                      />
                    </template>
                  </div>
                </el-form-item>
                <sliding-fixed
                  v-model="row.is_sliding_fixed"
                  @sliding_fixed_change="sliding_fixed_change($event, index)"
                />
                <template v-if="form.tabs_active_index == index">
                  <el-form-item v-if="form.tabs_theme == '1'" label="简介配置">
                    <el-input
                      v-model="row.desc"
                      placeholder="请输入简介"
                      clearable
                    />
                  </el-form-item>
                  <!-- 数据筛选组件, 根据数据源类型显示不同的筛选组件 -->
                  <data-filter
                    type="plugins-video"
                    :value="row"
                    :list="row.data_list"
                    :base-list="base_list"
                    @add="plugins_video_add(index)"
                    @data_list_replace="data_list_replace($event, index)"
                    @data_list_remove="data_list_remove($event, index)"
                    @data_list_sort="data_list_sort($event, index)"
                  />
                </template>
              </div>
            </template>
          </drag>
          <el-button class="mtb-20 w" @click="tabs_add">+添加</el-button>
        </div>
      </card-container>
      <div class="divider-line"></div>
      <card-container class="card-container">
        <div class="mb-12">列表设置</div>
        <el-form-item label="是否显示">
          <el-checkbox-group v-model="form.field_show">
            <el-checkbox
              v-for="item in base_list.field_show_list"
              :key="item.value"
              :value="item.value"
              >{{ item.name }}</el-checkbox
            >
          </el-checkbox-group>
        </el-form-item>
        <template v-if="form.plugins_video_theme == '4'">
          <el-form-item label="标题浮起">
            <el-switch
              v-model="form.name_float"
              active-value="1"
              inactive-value="0"
              @change="switch_chage"
            />
          </el-form-item>
        </template>
      </card-container>
    </el-form>
    <url-value-dialog
      v-model:dialog-visible="url_value_dialog_visible"
      :type="['plugins-video']"
      :multiple="url_value_multiple_bool"
      @update:model-value="url_value_dialog_call_back"
    />
  </div>
</template>
<script setup lang="ts">
import { get_math, tabs_style } from "../../utils";
import { video_row_wrap, video_data_normalize } from "../../utils/video";
import CommonAPI from "../../api/common";
import { commonStore } from "../../store";
const common_store = commonStore();
/**
 * @description: 视频选项卡（内容）
 * @param value{Object} 内容数据
 * @param styles{Object} 样式数据
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
  tabs_theme_list: [
    { name: "样式一", value: "0" },
    { name: "样式二", value: "1" },
    { name: "样式三", value: "2" },
    { name: "样式四", value: "3" },
  ],
  plugins_video_theme_list: [
    { name: "单列展示", value: "0", width: 110, height: 83 },
    { name: "两列展示（纵向）", value: "1", width: 180, height: 180 },
    { name: "大图展示", value: "2", width: 0, height: 180 },
    { name: "无图模式", value: "3", width: 0, height: 0 },
    { name: "左右滑动展示", value: "4", width: 0, height: 0 },
  ],
  carousel_col_list: [
    { name: "单列展示", value: "0" },
    { name: "两列展示", value: "1" },
    { name: "三列展示", value: "2" },
    { name: "四列展示", value: "3" },
  ],
  data_type_list: [
    { name: "选择视频", value: "0" },
    { name: "筛选视频", value: "1" },
  ],
  field_show_list: [
    { name: "视频标题", value: "3" },
    { name: "日期时间", value: "0" },
    { name: "浏览量", value: "1" },
  ],
});
const tabs_theme_change = (
  val: string | number | boolean | undefined
): void => {
  data.value.tabs_color_checked = tabs_style(
    data.value.tabs_color_checked,
    val
  );
};
onMounted(() => {
  // 各 Tab 指定视频:已存 data_ids 但快照为空时按 id 回显
  hydrate_tabs_data_list();
});
// 各 Tab 指定视频快照回显(data_ids → data_list,行结构与"指定视频"回调一致)
const hydrate_tabs_data_list = async () => {
  const tabs: any[] = form.value.tabs_list || [];
  for (const cfg of tabs) {
    const existList: any[] = cfg.data_list || [];
    const idsText: string = cfg.data_ids || "";
    if (cfg.data_type !== "0" || existList.length > 0 || !idsText) {
      continue;
    }
    const ids = String(idsText)
      .split(",")
      .map((s: string) => parseInt(s.trim(), 10))
      .filter((n: number) => !isNaN(n));
    if (ids.length === 0) {
      continue;
    }
    try {
      const res: any = await CommonAPI.getVideoList({ ids });
      const rows: any[] = res?.data || [];
      if (rows.length > 0) {
        cfg.data_list = rows.map((child: any) => video_row_wrap(child));
      }
    } catch (error) {
      console.error("视频选项卡指定数据回显失败:", error);
    }
  }
};
// 视频列表风格切换
const plugins_video_theme_change = (val: any) => {
  if (val == "3" || val == "4") {
    form.value.field_show = ["1", "3"];
  } else {
    form.value.field_show = ["0", "1", "3"];
  }
  // 切换风格时，将对应图片的默认值宽度和高度赋值
  const list = base_list.plugins_video_theme_list.filter(
    (item) => item.value == val
  );
  if (list.length > 0) {
    data.value.content_img_width = list[0].width;
    data.value.content_img_height = list[0].height;
  }
};
/**
 * 处理滑动固定状态变化的函数
 * 当某个标签页的滑动固定状态发生变化时，确保同时只有一个标签页被设置为滑动固定
 */
const sliding_fixed_change = (
  val: string | number | boolean,
  index: number
) => {
  // 查找除当前标签页外，其他标签页中是否已有滑动固定的
  const tabs_list_is_sliding_fixed = form.value.tabs_list.findIndex(
    (item: any, index1: number) =>
      item.is_sliding_fixed == "1" && index != index1
  );
  // 如果当前标签页的滑动固定状态为'1'，且已存在其他滑动固定的标签页
  if (val == "1" && tabs_list_is_sliding_fixed != -1) {
    // 遍历所有标签页，将其他标签页的滑动固定状态设置为'0'
    form.value.tabs_list.forEach((item: any, index1: number) => {
      if (index != index1) {
        item.is_sliding_fixed = "0";
      }
    });
  }
};
// 选项卡设置
const tabs_list_remove = (index: number) => {
  if (form.value.tabs_list.length > 1) {
    form.value.tabs_list.splice(index, 1);
    if (form.value.tabs_list.length > index) {
      form.value.tabs_active_index = index;
    } else {
      form.value.tabs_active_index = index - 1;
    }
  } else {
    form.value.tabs_active_index = 0;
    ElMessage.warning("至少保留一个选项卡");
  }
};
const tabs_list_sort = (item: any) => {
  form.value.tabs_list = item;
};
const tabs_add = () => {
  form.value.tabs_list.push({
    id: get_math(),
    tabs_type: "0",
    tabs_img: [],
    tabs_icon: "",
    is_sliding_fixed: "0",
    title: "",
    desc: "",
    data_type: "0",
    keywords: "",
    data_ids: "",
    number: 4,
    order_by_type: 0,
    is_recommended: "",
    is_hot: "",
    data_list: [],
    data_auto_list: [],
  });
  form.value.tabs_active_index = form.value.tabs_list.length - 1;
};
let click_index = 0;
const url_value_multiple_bool = ref(true);
const data_list_replace_index = ref(0);
// 打开弹出框
const url_value_dialog_visible = ref(false);

const data_list_replace = (index: number, row_index: number) => {
  click_index = row_index;
  data_list_replace_index.value = index;
  url_value_multiple_bool.value = false;
  url_value_dialog_visible.value = true;
};
// 移除
const data_list_remove = (index: number, plugins_video_index: number) => {
  form.value.tabs_list[plugins_video_index].data_list.splice(index, 1);
};
// 拖动排序
const data_list_sort = (item: any, index: number) => {
  form.value.tabs_list[index].data_list = item;
};
//  新增
const plugins_video_add = (index: number) => {
  click_index = index;
  url_value_multiple_bool.value = true;
  url_value_dialog_visible.value = true;
};
const tabs_list_click = (item: any, index: number) => {
  form.value.tabs_active_index = index;
};
// 弹出框选择的内容
const url_value_dialog_call_back = (item: any[]) => {
  if (url_value_multiple_bool.value) {
    item.forEach((child: any) => {
      form.value.tabs_list[click_index].data_list.push(video_row_wrap(child));
    });
  } else {
    form.value.tabs_list[click_index].data_list[
      data_list_replace_index.value
    ] = {
      id: get_math(),
      new_cover:
        form.value.tabs_list[click_index].data_list[data_list_replace_index.value]
          ?.new_cover || [],
      new_title: "",
      data: video_data_normalize(item[0]),
    };
  }
};
// 标题浮起之后视频标题的颜色和字体更新
const switch_chage = (val: string | number | boolean) => {
  if (val == "1") {
    data.value.name_color = "#fff";
    data.value.name_weight = "400";
  } else {
    data.value.name_color = "#333";
    data.value.name_weight = "bold";
  }
};
// 是否设置安全距离
const is_not_general_safe_distance = computed(
  () => common_store.is_immersion_model && !common_store.is_general_safe_distance
);
watchEffect(() => {
  if (!is_not_general_safe_distance.value) {
    form.value.is_general_safe_distance = "0";
  }
});
</script>
<style lang="scss" scoped>
.content {
  width: 100%;
}
</style>
