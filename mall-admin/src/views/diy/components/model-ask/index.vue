<template>
  <div class="oh" :style="style_container">
    <div :style="style_img_container">
      <div :class="outer_class" :style="outer_style">
        <template v-if="!['2'].includes(theme)">
          <div
            v-for="(item, index) in list"
            :key="index"
            class="re oh"
            :class="layout_type"
            :style="layout_style"
          >
            <template v-if="theme == 0">
              <div class="oh w h flex-col jc-sb gap-10" :style="layout_img_style">
                <div class="flex-row gap-10 align-b">
                  <div
                    v-if="is_show('ranking')"
                    :class="`top-style one${index + 1}`"
                    >{{ index + 1 }}</div
                  >
                  <div
                    :class="get_title_line"
                    :style="trends_config('title') + 'word-break: break-all;'"
                    >{{ item.title }}</div
                  >
                </div>
                <div
                  v-if="
                    is_show('reply_status') ||
                    is_show('time') ||
                    is_show('page_view')
                  "
                  class="flex-row gap-10 align-c"
                  :style="is_show('ranking') ? 'margin-left: 2.7rem;' : ''"
                >
                  <div class="flex-row">
                    <div
                      v-if="is_show('reply_status')"
                      :style="button_style(item.is_reply == 0)"
                    >
                      <div :style="button_img_style(item.is_reply == 0)">
                        {{ item.is_reply == 0 ? "未回" : "已回" }}
                      </div>
                    </div>
                  </div>
                  <span v-if="is_show('time')" :style="trends_config('time')">{{
                    item.add_time_date
                  }}</span>
                  <span
                    v-if="is_show('page_view')"
                    :style="trends_config('page_view')"
                    >共有{{ item.access_count }}浏览</span
                  >
                </div>
              </div>
            </template>
            <template v-else>
              <div
                class="oh flex-col gap-10 jc-sb"
                :style="layout_img_style"
              >
                <div class="flex-row gap-10 align-b">
                  <div
                    :class="get_title_line"
                    :style="trends_config('title') + 'word-break: break-all;'"
                    >{{ item.title }}</div
                  >
                </div>
                <div
                  v-if="is_show('reply_status') || is_show('time')"
                  class="flex-col gap-10"
                >
                  <span v-if="is_show('time')" :style="trends_config('time')">{{
                    item.add_time_date
                  }}</span>
                  <div class="flex-row">
                    <div
                      v-if="is_show('reply_status')"
                      class="flex-row"
                      :style="button_style(item.is_reply == 0)"
                    >
                      <div :style="button_img_style(item.is_reply == 0)">
                        {{ item.is_reply == 0 ? "未回" : "已回" }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </template>
        <template v-else>
          <swiper
            :key="carousel_key"
            class="w flex"
            direction="horizontal"
            :loop="true"
            :autoplay="autoplay"
            :slides-per-view="form.carousel_col"
            :slides-per-group="slides_per_group"
            :allow-touch-move="false"
            :space-between="content_outer_spacing"
            :pause-on-mouse-enter="true"
            :modules="modules"
          >
            <swiper-slide v-for="(item, index) in list" :key="index">
              <div :class="layout_type" :style="layout_style">
                <div
                  class="oh w h flex-col gap-10 jc-sb"
                  :style="layout_img_style"
                >
                  <div class="flex-row gap-10 align-b">
                    <div
                      :class="get_title_line"
                      :style="trends_config('title') + 'word-break: break-all;'"
                      >{{ item.title }}</div
                    >
                  </div>
                  <div
                    v-if="is_show('reply_status') || is_show('time')"
                    class="flex-col gap-10"
                  >
                    <span v-if="is_show('time')" :style="trends_config('time')">{{
                      item.add_time_date
                    }}</span>
                    <div class="flex-row">
                      <div
                        v-if="is_show('reply_status')"
                        class="flex-row"
                        :style="button_style(item.is_reply == 0)"
                      >
                        <div :style="button_img_style(item.is_reply == 0)">
                          {{ item.is_reply == 0 ? "未回" : "已回" }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </swiper-slide>
          </swiper>
        </template>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import {
  common_styles_computer,
  common_img_computer,
  get_math,
  gradient_handle,
  margin_computer,
  border_computer,
  box_shadow_computer,
  radius_computer,
  background_computer,
  padding_computer,
  gradient_computer,
} from "../../utils";
import { old_margin } from "../../utils/common";
import { isEmpty, cloneDeep } from "lodash";
import { ask_data_normalize, ask_list_normalize, AskRowData } from "../../utils/ask";
import CommonAPI from "../../api/common";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Autoplay } from "swiper/modules";
const modules = [Autoplay];
/**
 * @description: 商品问答列表（渲染）
 * @param value{Object} 样式数据
 * @param isCommonStyle{Object} 是否为通用样式
 */
const props = defineProps({
  value: {
    type: Object,
    default: () => ({}),
  },
  isCommonStyle: {
    type: Boolean,
    default: true,
  },
});
const form = computed(() => props.value?.content || {});
const new_style = computed(() => props.value?.style || {});
// 选择的风格
const theme = computed(() => form.value.theme);
const get_title_line = computed(() => {
  const line = form.value.title_display_method;
  if (line == "0") {
    return "";
  } else {
    return line == "1" ? "text-line-1" : "text-line-2";
  }
});
// 最外层不同风格下的显示
const outer_class = computed(() => {
  const flex = ["0", "2"].includes(theme.value) ? "flex-col " : "container ";
  const wrap = theme.value == "2" ? "" : "flex-wrap ";
  return flex + wrap + "oh";
});
const outer_style = computed(() => {
  const radius = ["0", "2"].includes(theme.value)
    ? `gap: ${new_style.value.content_outer_spacing + "px"};`
    : `column-gap: ${new_style.value.content_outer_spacing + "px"}`;
  return `${radius}`;
});

// 公共样式
const style_container = computed(() =>
  props.isCommonStyle ? common_styles_computer(new_style.value.common_style) : ""
);
const style_img_container = computed(() =>
  props.isCommonStyle ? common_img_computer(new_style.value.common_style) : ""
);
//#region 列表数据
const default_list = {
  title: "测试问答标题",
  access_count: 0,
  add_time_date: "2023-01-01 10:00:00",
  is_reply: 0,
};
const list = ref<AskRowData[]>([]);
// 包裹行 → 渲染裸行(标题取 new_title 覆盖)
const wrapped_to_flat = (rows: any[]): AskRowData[] => {
  return ask_list_normalize(rows).map((item) => {
    const flat = { ...(item.data || {}) };
    if (!isEmpty(item.new_title)) {
      flat.title = item.new_title;
    }
    return flat;
  });
};
// 初始化的时候执行
onMounted(() => {
  // 指定问答并且指定数组不为空
  if (!isEmpty(form.value.data_list) && form.value.data_type == "0") {
    list.value = wrapped_to_flat(cloneDeep(form.value.data_list));
  } else if (!isEmpty(form.value.data_auto_list) && form.value.data_type == "1") {
    // 筛选问答并且筛选数组不为空(快照裸行归一)
    list.value = (form.value.data_auto_list as any[]).map((item: any) =>
      ask_data_normalize(item)
    );
  } else {
    list.value = Array(4).fill(default_list);
  }
});

const get_ask_list = async () => {
  const { keywords, is_reply, number, order_by_type, order_by_rule } =
    form.value;
  const params = {
    ask_keywords: keywords,
    ask_is_reply: is_reply,
    ask_number: number,
    ask_order_by_type: order_by_type,
    ask_order_by_rule: order_by_rule,
  };
  // 获取问答列表
  try {
    const res: any = await CommonAPI.getAskAutoList(params);
    if (!isEmpty(res.data)) {
      list.value = [];
      const rows = res.data.map((child: any) => ask_data_normalize(child));
      // 快照裸行写入 content.data_auto_list,与 App 渲染器读取契约一致
      form.value.data_auto_list = rows;
      list.value = rows;
    } else {
      list.value = Array(4).fill(default_list);
    }
  } catch (error) {
    list.value = Array(4).fill(default_list);
  }
};
// 取出监听的数据
const watch_data = computed(() => {
  const {
    keywords,
    is_reply,
    number,
    order_by_type,
    order_by_rule,
    data_type,
    data_list,
  } = form.value;
  return {
    keywords,
    is_reply,
    number,
    order_by_type,
    order_by_rule,
    data_type,
    data_list,
  };
});
// 初始化的时候不执行, 监听数据变化
watch(
  () => watch_data.value,
  (val, old_val) => {
    // 使用JSON.stringify()进行判断 新值和旧值是否一样 不一样就重新获取数据
    if (
      JSON.stringify(val) !== JSON.stringify(old_val) ||
      props.isCommonStyle
    ) {
      if (val.data_type == "0") {
        if (!isEmpty(val.data_list)) {
          list.value = wrapped_to_flat(cloneDeep(val.data_list));
        } else {
          list.value = Array(4).fill(default_list);
        }
      } else {
        get_ask_list();
      }
    }
  },
  { deep: true }
);
//#endregion
// 问答间距
const content_outer_spacing = computed(
  () => new_style.value.content_outer_spacing
);
// 圆角设置
const content_radius = computed(() => radius_computer(new_style.value.ask_radius));
// 内边距设置
const content_padding = computed(() => padding_computer(new_style.value.ask_padding));
const ask_left_right_width_margin = computed(() => {
  const { ask_margin = old_margin } = new_style.value;
  return ask_margin.margin_left + ask_margin.margin_right;
});
// 两列风格
const two_columns = computed(
  () => content_outer_spacing.value + ask_left_right_width_margin.value * 2 + "px"
);
const button_style = (flag: boolean) => {
  const style = flag
    ? new_style.value.not_replied_yet_style
    : new_style.value.returned_style;
  return `${gradient_computer(style)}; ${border_computer(style)} ${radius_computer(
    style.font_radius
  )}`;
};
const button_img_style = (flag: boolean) => {
  const style = flag
    ? new_style.value.not_replied_yet_style
    : new_style.value.returned_style;
  return `${padding_computer(style.font_padding)} ${background_computer(
    style
  )}font-weight:${style.font_typeface}; font-size: ${style.font_size}px;color: ${
    style.font_color
  };`;
};
// 根据传递的参数，从对象中取值
const trends_config = (key: string, type?: string) => {
  return style_config(
    new_style.value[`ask_${key}_typeface`],
    new_style.value[`ask_${key}_size`],
    new_style.value[`ask_${key}_color`],
    type
  );
};
// 根据传递的值，显示不同的内容
const style_config = (
  typeface: string,
  size: number,
  color: string | object,
  type?: string
) => {
  let style = `font-weight:${typeface}; font-size: ${size}px;color: ${color};line-height: 1.5;`;
  return style;
};
// 不同风格下的样式
const layout_type = computed(() => {
  let class_type = "";
  switch (theme.value) {
    case "0":
      class_type = `oh`;
      break;
    case "1":
      class_type = `oh`;
      break;
    case "2":
      class_type = `multicolumn-columns oh`;
      break;
    default:
      break;
  }
  return class_type;
});
// 容器样式
const layout_style = computed(() => {
  const radius = content_radius.value;
  const width =
    theme.value == "0"
      ? `width: calc(100% - ${ask_left_right_width_margin.value}px);`
      : "";
  const margin =
    theme.value == "1"
      ? {
          ...new_style.value.ask_margin,
          margin_bottom:
            (new_style.value.ask_margin?.margin_bottom || 0) +
            new_style.value.content_outer_spacing,
        }
      : new_style.value.ask_margin;
  const gradient =
    gradient_handle(new_style.value.ask_color_list, new_style.value.ask_direction) +
    margin_computer(margin) +
    border_computer(new_style.value) +
    box_shadow_computer(new_style.value);
  return `${radius} ${gradient} ${width}`;
});
// 容器图片样式
const layout_img_style = computed(() => {
  const padding = content_padding.value;
  const data = {
    background_img_style: new_style.value.ask_background_img_style,
    background_img: new_style.value.ask_background_img,
  };
  return padding + background_computer(data);
});
// 判断是否显示对应的内容
const is_show = (index: string) => {
  return form.value.is_show.includes(index);
};
//#region 轮播设置
// 轮播图key值
const carousel_key = ref("0");
const autoplay = ref<boolean | object>(false);
const slides_per_group = ref(1);
// 内容参数的集合
watchEffect(() => {
  // 是否滚动
  if (new_style.value.is_roll == "1" && list.value.length > 0) {
    autoplay.value = {
      delay: (new_style.value.interval_time || 2) * 1000,
      pauseOnMouseEnter: true,
    };
  } else {
    autoplay.value = false;
  }
  // 判断是平移还是整屏滚动
  slides_per_group.value =
    new_style.value.rolling_fashion == "translation"
      ? 1
      : form.value.carousel_col;
  // 更新轮播图的key，确保更换时能重新更新轮播图
  carousel_key.value = get_math();
});
//#endregion
const content_outer_height = computed(
  () => new_style.value.content_outer_height + "px"
);
</script>
<style lang="scss" scoped>
.two-columns {
  width: calc((100% - v-bind(two_columns)) / 2);
}
.multicolumn-columns {
  height: v-bind(content_outer_height);
}
.one1 {
  color: #fff !important;
  background: #FF6565 !important;
}
.one2 {
  color: #fff !important;
  background: #FF9F2F !important;
}
.one3 {
  color: #fff !important;
  background: #FFC889 !important;
}
.top-style {
  color: #EBAB2A;
  background: #fff;
  padding: 0 0.5rem;
  width: 1.7rem;
  height: 1.7rem;
  font-weight: 400;
  font-size: 1.2rem;
  line-height: 1.7rem;
  text-align: right;
  font-style: normal;
  border-radius: 0.4rem;
}
.container {
  column-count: 2;
}
</style>
