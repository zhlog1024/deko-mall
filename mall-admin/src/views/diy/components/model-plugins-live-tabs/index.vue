<template>
  <div :style="style_container">
    <div class="flex-col" :style="style_img_container">
      <div class="oh" :style="tabs_container">
        <div class="oh" :style="tabs_img_container">
          <tabs-view
            ref="tabs"
            :value="plugins_live_tabs"
            :active-index="tabs_active_index"
            :tabs-sliding-fixed-bg="tabs_sliding_fixed_bg"
          />
        </div>
      </div>
      <div class="oh" :style="plugins_live_container">
        <div class="oh" :style="plugins_live_img_container">
          <model-plugins-live
            :value="plugins_live_tabs"
            :is-common-style="false"
          />
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
/**
 * @description: 直播选项卡（渲染）
 * @param value{Object} 传过来的数据，用于数据渲染
 *
 * 与 app 渲染器同构：
 *   mall-app/pages/diy/components/diy/plugins-live-tabs.vue
 *   ├─ modules/tabs-view.vue          → 本组件 <tabs-view>
 *   └─ plugins-live-list.vue          → 本组件 <model-plugins-live>
 * 结构照 model-plugins-video-tabs/index.vue（同项目已同构的 ShopXO 迁移件）。
 */
import { ref, watch } from "vue";
import {
  background_computer,
  border_computer,
  box_shadow_computer,
  common_img_computer,
  common_styles_computer,
  gradient_computer,
  margin_computer,
  padding_computer,
  radius_computer,
} from "../../utils";
import { cloneDeep } from "lodash";

const props = defineProps({
  value: {
    type: Object,
    default: () => ({}),
  },
});

const style_container = ref("");
const style_img_container = ref("");
const plugins_live_tabs = ref<any>({});
const tabs_active_index = ref(0);
// 选项卡独立样式
const tabs_container = ref("");
const tabs_img_container = ref("");
// 直播区域样式设置
const plugins_live_container = ref("");
const plugins_live_img_container = ref("");
// 打开滑动固定开关之后，显示的样式
const tabs_sliding_fixed_bg = ref("");

watch(
  () => props.value,
  (val) => {
    const new_data = cloneDeep(val);
    const new_style = new_data?.style || {};
    const new_content = new_data?.content || {};
    tabs_active_index.value = new_content.tabs_active_index || 0;
    // 选项卡背景设置
    const tabs_data = {
      color_list: new_style.tabs_bg_color_list,
      direction: new_style.tabs_bg_direction,
      background_img_style: new_style.tabs_bg_background_img_style,
      background_img: new_style.tabs_bg_background_img,
    };
    tabs_sliding_fixed_bg.value = gradient_computer(tabs_data);
    tabs_container.value =
      gradient_computer(tabs_data) +
      radius_computer(new_style.tabs_radius) +
      margin_computer(new_style.tabs_margin) +
      border_computer(new_style.tabs_content) +
      box_shadow_computer(new_style.tabs_content);
    tabs_img_container.value =
      background_computer(tabs_data) + padding_computer(new_style.tabs_padding);
    // 直播区域背景设置
    const plugins_live_content_data = {
      color_list: new_style.plugins_live_content_color_list,
      direction: new_style.plugins_live_content_direction,
      background_img_style: new_style.plugins_live_content_background_img_style,
      background_img: new_style.plugins_live_content_background_img,
    };
    plugins_live_container.value =
      gradient_computer(plugins_live_content_data) +
      margin_computer(new_style.plugins_live_content_margin) +
      radius_computer(new_style.plugins_live_content_radius) +
      border_computer(new_style.plugins_live_content) +
      box_shadow_computer(new_style.plugins_live_content);
    plugins_live_img_container.value =
      background_computer(plugins_live_content_data) +
      padding_computer(new_style.plugins_live_content_padding);
    // 当前分类对应的数据（直播区风格/轮播列数取 content 顶层，数据取当前 tab）
    const new_tabs_data =
      (new_content.tabs_list || [])[tabs_active_index.value] || {};
    new_data.content.theme = new_content.plugins_live_theme;
    new_data.content.carousel_col = new_content.plugins_live_carousel_col;
    new_data.content.data_type = new_tabs_data.data_type;
    new_data.content.data_list = new_tabs_data.data_list;
    new_data.content.data_auto_list = new_tabs_data.data_auto_list;
    new_data.content.data_ids = new_tabs_data.data_ids;
    new_data.content.keywords = new_tabs_data.keywords;
    // 直播专有：状态筛选
    new_data.content.live_status = new_tabs_data.live_status || [];
    new_data.content.number = new_tabs_data.number;
    new_data.content.order_by_type = new_tabs_data.order_by_type;
    new_data.content.order_by_rule = new_tabs_data.order_by_rule;
    new_data.content.is_recommended = new_tabs_data.is_recommended;
    new_data.content.is_cover = new_tabs_data.is_cover;
    plugins_live_tabs.value = new_data;
    // 公共样式
    style_container.value = common_styles_computer(new_style.common_style);
    style_img_container.value =
      common_img_computer(new_style.common_style) +
      `gap: ${new_style.plugins_live_content_spacing}px;`;
  },
  { immediate: true, deep: true }
);
</script>
<style lang="scss" scoped></style>
