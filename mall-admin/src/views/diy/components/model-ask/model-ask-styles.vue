<template>
  <div class="styles">
    <el-form :model="form" label-width="70">
      <card-container class="card-container">
        <div class="mb-12">内容样式</div>
        <el-form-item label="内容背景">
          <background-common
            v-model:color_list="form.ask_color_list"
            v-model:direction="form.ask_direction"
            v-model:img_style="form.ask_background_img_style"
            v-model:img="form.ask_background_img"
            @mult_color_picker_event="mult_color_picker_event"
          />
        </el-form-item>
        <el-form-item label="内容标题">
          <color-text-size-group
            v-model:color="form.ask_title_color"
            v-model:typeface="form.ask_title_typeface"
            v-model:size="form.ask_title_size"
            default-color="#000000"
          />
        </el-form-item>
        <el-form-item label="日期时间">
          <color-text-size-group
            v-model:color="form.ask_time_color"
            v-model:typeface="form.ask_time_typeface"
            v-model:size="form.ask_time_size"
          />
        </el-form-item>
        <el-form-item label="浏览量">
          <color-text-size-group
            v-model:color="form.ask_page_view_color"
            v-model:typeface="form.ask_page_view_typeface"
            v-model:size="form.ask_page_view_size"
          />
        </el-form-item>
        <el-form-item label="外间距">
          <margin :value="form.ask_margin" />
        </el-form-item>
        <el-form-item label="内间距">
          <padding :value="form.ask_padding" />
        </el-form-item>
        <el-form-item label="内容圆角">
          <radius :value="form.ask_radius" />
        </el-form-item>
        <el-form-item v-if="data.theme == '0'" label="内容间距">
          <slider v-model="form.content_spacing" :max="100" />
        </el-form-item>
        <el-form-item label="问答间距">
          <slider v-model="form.content_outer_spacing" :max="100" />
        </el-form-item>
        <template v-if="theme == '2'">
          <el-form-item label="内容高度">
            <slider v-model="form.content_outer_height" :max="1000" />
          </el-form-item>
        </template>
        <!-- 边框处理 -->
        <border-config
          v-model:show="form.border_is_show"
          v-model:color="form.border_color"
          v-model:style="form.border_style"
          v-model:size="form.border_size"
        />
        <!-- 阴影配置 -->
        <shadow-config v-model="form" />
      </card-container>
      <div class="divider-line"></div>
      <el-tabs v-model="tabs_icon_name" class="content-tabs">
        <el-tab-pane
          v-for="(tab, index) in tabs"
          :key="index"
          :label="tab.label"
          :name="tab.value"
        >
          <common-button-style :value="form[`${tab.value}_style`]" />
        </el-tab-pane>
      </el-tabs>
      <template v-if="theme == '2'">
        <div class="divider-line"></div>
        <card-container class="card-container">
          <div class="mb-12">轮播设置</div>
          <el-form-item label="自动轮播">
            <el-switch
              v-model="form.is_roll"
              active-value="1"
              inactive-value="0"
            />
          </el-form-item>
          <template v-if="form.is_roll == '1'">
            <el-form-item label="间隔时间">
              <slider v-model="form.interval_time" :min="1" :max="100" />
            </el-form-item>
          </template>
          <el-form-item label="滚动方式">
            <el-radio-group v-model="form.rolling_fashion">
              <el-radio value="translation">平移</el-radio>
              <el-radio value="cut-screen">切屏</el-radio>
            </el-radio-group>
          </el-form-item>
        </card-container>
      </template>
    </el-form>
    <template v-if="isCommonStyle">
      <div class="divider-line"></div>
      <common-styles :value="form.common_style" @update:value="common_style_update" />
    </template>
  </div>
</template>
<script setup lang="ts">
/**
 * @description: 商品问答（样式）
 * @param value{Object} 样式数据
 * @param content{Object} 内容数据
 */
const props = defineProps({
  value: {
    type: Object,
    default: () => ({}),
  },
  content: {
    type: Object,
    default: () => ({}),
  },
  isCommonStyle: {
    type: Boolean,
    default: true,
  },
});
const tabs_icon_name = ref("returned");
// 动态生成 tab 配置
const tabs = [
  { label: "已回样式", value: "returned" },
  { label: "未回样式", value: "not_replied_yet" },
];
// 默认值
const state = reactive({
  form: props.value,
  data: props.content,
});
// 如果需要解构，确保使用toRefs
const { form, data } = toRefs(state);
const theme = computed(() => data.value.theme);

// 问答背景渐变设置
const mult_color_picker_event = (arry: color_list[], type: number) => {
  form.value.ask_color_list = arry;
  form.value.ask_direction = type.toString();
};
const common_style_update = (value: any) => {
  form.value.common_style = value;
};
</script>
<style lang="scss" scoped>
.styles {
  width: 100%;
}
:deep(.el-tabs.content-tabs) {
  .el-tabs__content {
    padding: 1rem 3rem;
  }
}
</style>
