<template>
  <div class="oh" :style="style_container">
    <div class="oh" :style="style_img_container">
      <div class="pr oh" :style="style">
        <!-- 非左右滑动：普通列表 -->
        <div
          v-if="!['4'].includes(plugins_live_theme)"
          class="flex-wrap"
          :class="plugins_live_theme_class"
          :style="plugins_live_theme !== '3' ? plugins_live_spacing : ''"
        >
          <div
            v-for="(item, index) in data_list"
            :key="index"
            class="item oh"
            :style="plugins_live_style"
          >
            <!-- 无图模式 -->
            <template v-if="plugins_live_theme == '3'">
              <div
                v-if="
                  field_show.includes('0') ||
                  field_show.includes('1') ||
                  field_show.includes('2') ||
                  field_show.includes('3')
                "
                class="jc-sb flex-1 flex-row align-c"
                :style="content_padding"
              >
                <div class="live-noimg-row flex-row align-c flex-1 flex-width">
                  <div
                    v-if="field_show.includes('0')"
                    class="title text-line-1 live-noimg-title"
                    :style="plugins_live_name"
                  >
                    {{ live_item_title(item) }}
                  </div>
                  <div
                    v-if="
                      field_show.includes('3') && should_show_live_status(item.data)
                    "
                    class="live-status-tag live-status-tag--inline"
                    :class="'status-' + get_live_room_status(item.data)"
                  >
                    <div
                      v-if="get_live_room_status(item.data) === 1"
                      class="live-status-dots"
                    >
                      <span class="dot"></span><span class="dot"></span
                      ><span class="dot"></span>
                    </div>
                    <span>{{ get_live_room_status_name(item.data) }}</span>
                  </div>
                </div>
                <div class="flex-row jc-sb gap-8 margin-left">
                  <div class="text-line-1" :style="plugins_live_seller_name">
                    {{ field_show.includes('1') ? live_seller_name(item) : '' }}
                  </div>
                  <div
                    v-show="field_show.includes('2')"
                    class="flex-row align-c gap-3 live-page-view"
                    :style="plugins_live_page_view"
                  >
                    <icon name="eye" :size="plugins_live_page_view_icon_size" />
                    <div>{{ live_viewer_count(item) }}</div>
                  </div>
                </div>
              </div>
            </template>
            <!-- 有图：单列 / 两列 / 大图 -->
            <div
              v-else
              :class="plugins_live_theme == '0' ? 'flex-row oh' : 'flex-col oh h'"
              :style="plugins_live_img_style"
            >
              <div class="oh pr flex-row">
                <template v-if="item.new_cover && item.new_cover.length > 0">
                  <image-empty
                    v-model="item.new_cover[0].url"
                    class="img"
                    :style="img_radius + img_size"
                    :error-img-style="error_img"
                  />
                </template>
                <template v-else>
                  <image-empty
                    v-model="item.data.cover"
                    class="img"
                    :style="img_radius + img_size"
                    :error-img-style="error_img"
                  />
                </template>
                <!-- 直播状态角标（对齐 app：四态 + 直播中跳动竖条） -->
                <div
                  v-if="
                    field_show.includes('3') && should_show_live_status(item.data)
                  "
                  class="live-status-tag"
                  :class="'status-' + get_live_room_status(item.data)"
                >
                  <div
                    v-if="get_live_room_status(item.data) === 1"
                    class="live-status-dots"
                  >
                    <span class="dot"></span><span class="dot"></span
                    ><span class="dot"></span>
                  </div>
                  <span>{{ get_live_room_status_name(item.data) }}</span>
                </div>
              </div>
              <div
                v-if="
                  field_show.includes('0') ||
                  field_show.includes('1') ||
                  field_show.includes('2') ||
                  field_show.includes('3')
                "
                class="jc-sb flex-1 flex-col"
                :style="plugins_live_theme !== '0' ? content_padding : ''"
              >
                <div class="flex-col">
                  <div
                    v-if="field_show.includes('0')"
                    class="title text-line-2"
                    :style="plugins_live_name"
                  >
                    {{ live_item_title(item) }}
                  </div>
                </div>
                <div class="flex-row jc-sb gap-8 align-e margin-top">
                  <div class="text-line-1" :style="plugins_live_seller_name">
                    {{ field_show.includes('1') ? live_seller_name(item) : '' }}
                  </div>
                  <div
                    v-show="field_show.includes('2')"
                    class="flex-row align-c gap-3 live-page-view"
                    :style="plugins_live_page_view"
                  >
                    <icon name="eye" :size="plugins_live_page_view_icon_size" />
                    <div>{{ live_viewer_count(item) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 左右滑动：轮播 -->
        <div v-else class="oh" :class="plugins_live_theme_class">
          <swiper
            :key="carousel_key"
            class="w flex"
            direction="horizontal"
            :loop="true"
            :autoplay="autoplay"
            :slides-per-view="Number(carousel_col) + 1"
            :slides-per-group="slides_per_group"
            :allow-touch-move="false"
            :space-between="Number(new_style.plugins_live_spacing) || 0"
            :pause-on-mouse-enter="true"
            :modules="modules"
          >
            <swiper-slide v-for="(item, index) in data_list" :key="index">
              <div class="item oh" :style="plugins_live_style">
                <div class="h oh flex-col" :style="plugins_live_img_style">
                  <div class="oh pr h">
                    <template v-if="item.new_cover && item.new_cover.length > 0">
                      <image-empty
                        v-model="item.new_cover[0].url"
                        class="img"
                        :style="img_radius + 'height:100%;'"
                        :error-img-style="error_img"
                      />
                    </template>
                    <template v-else>
                      <image-empty
                        v-model="item.data.cover"
                        class="img"
                        :style="img_radius + 'height:100%;'"
                        :error-img-style="error_img"
                      />
                    </template>
                    <div
                      v-if="field_show.includes('0') && name_float == '1'"
                      class="text-line-1"
                      :style="plugins_live_name + float_name_style"
                    >
                      {{ live_item_title(item) }}
                    </div>
                    <div
                      v-if="
                        field_show.includes('3') &&
                        should_show_live_status(item.data)
                      "
                      class="live-status-tag"
                      :class="'status-' + get_live_room_status(item.data)"
                    >
                      <div
                        v-if="get_live_room_status(item.data) === 1"
                        class="live-status-dots"
                      >
                        <span class="dot"></span><span class="dot"></span
                        ><span class="dot"></span>
                      </div>
                      <span>{{ get_live_room_status_name(item.data) }}</span>
                    </div>
                  </div>
                  <div
                    v-if="
                      field_show.includes('1') ||
                      field_show.includes('2') ||
                      (field_show.includes('0') && name_float == '0')
                    "
                    class="jc-sb flex-1 flex-col"
                    :style="plugins_live_theme !== '0' ? content_padding : ''"
                  >
                    <div class="flex-col">
                      <div
                        v-if="field_show.includes('0') && name_float == '0'"
                        class="title text-line-2"
                        :style="
                          plugins_live_name + plugins_live_name_height_computer
                        "
                      >
                        {{ live_item_title(item) }}
                      </div>
                    </div>
                    <div
                      :class="
                        'flex-row jc-sb gap-8 align-e' +
                        (field_show.includes('0') && name_float == '0'
                          ? ' margin-top'
                          : '')
                      "
                    >
                      <div class="text-line-1" :style="plugins_live_seller_name">
                        {{
                          field_show.includes('1') ? live_seller_name(item) : ''
                        }}
                      </div>
                      <div
                        v-show="field_show.includes('2')"
                        class="flex-row align-c gap-3 live-page-view"
                        :style="plugins_live_page_view"
                      >
                        <icon
                          name="eye"
                          :size="plugins_live_page_view_icon_size"
                        />
                        <div>{{ live_viewer_count(item) }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </swiper-slide>
          </swiper>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
/**
 * @description: 直播列表（渲染）
 * @param value{Object} 样式数据
 * @param isCommonStyle{Boolean} 是否使用通用样式
 *
 * 与 app 渲染器同构：mall-app/pages/diy/components/diy/plugins-live-list.vue
 * 装修器画布不请求接口，使用占位数据（与 video 侧一致）；占位数据只影响内容，
 * 结构/间距/字号/圆角/状态样式全部按真实字段计算。
 */
import { ref, computed, watch, onMounted } from "vue";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Autoplay } from "swiper/modules";
import {
  common_styles_computer,
  common_img_computer,
  padding_computer,
  radius_computer,
  background_computer,
  gradient_computer,
  gradient_handle,
  margin_computer,
  box_shadow_computer,
  border_computer,
} from "../../utils";
import { old_radius, old_padding, old_margin } from "../../utils/common";
import CommonAPI from "../../api/common";
import { live_list_normalize } from "../../utils/live";

const modules = [Autoplay];

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

interface LiveRowData {
  id?: number | string;
  title?: string;
  cover?: string;
  url?: string;
  seller_name?: string;
  status?: number | string;
  status_name?: string;
  online_count?: number | string;
}
interface LiveRow {
  id: number | string;
  data: LiveRowData;
  new_title: string;
  new_cover: any[];
}

const error_img = ref("width:50px;padding:10px;");
const style = ref("");
const style_container = ref("");
const style_img_container = ref("");
// 数据
const data_list = ref<LiveRow[]>([]);
// 风格
const plugins_live_theme = ref("0");
// 是否显示(0标题 1主播 2观看数 3直播状态)
const field_show = ref(["0", "1", "2", "3"]);
// 直播标题
const plugins_live_name = ref("");
// 主播昵称
const plugins_live_seller_name = ref("");
// 观看数
const plugins_live_page_view = ref("");
const plugins_live_page_view_icon_size = ref(12);
// 内容圆角 / 图片圆角 / 内间距 / 内容间距 / 直播间距
const content_radius = ref("");
const img_radius = ref("");
const content_padding = ref("");
const content_spacing = ref("");
const plugins_live_spacing = ref("");
const plugins_live_name_height_computer = ref("");
const float_name_style = ref("");
const name_float = ref("0");
const img_size = ref("");
const carousel_height_computer = ref("");
const carousel_col = ref("2");
const carousel_key = ref("");
const autoplay = ref<boolean | object>(false);
const slides_per_group = ref(1);

const new_content = computed(() => props.value?.content || {});
const new_style = computed(() => props.value?.style || {});

// 占位数据：覆盖三种状态，便于在画布上核对状态角标样式
const placeholder_list = (): LiveRow[] => [
  {
    id: 1,
    new_title: "",
    new_cover: [],
    data: {
      title: "直播标题预览一",
      seller_name: "主播昵称",
      status: 1,
      status_name: "直播中",
      online_count: 128,
    },
  },
  {
    id: 2,
    new_title: "",
    new_cover: [],
    data: {
      title: "健康好物专场",
      seller_name: "张老师",
      status: 0,
      status_name: "未开播",
      online_count: 86,
    },
  },
  {
    id: 3,
    new_title: "",
    new_cover: [],
    data: {
      title: "新品首发预告",
      seller_name: "李店长",
      status: 2,
      status_name: "离开",
      online_count: 45,
    },
  },
];

// 统一列表数据结构（与 app normalize_list_item 一致）
const normalize_list_item = (item: any): LiveRow => {
  if (item && item.data) return item;
  return { id: 0, new_title: "", new_cover: [], data: item || {} };
};

const resolve_data_list = () => {
  const content = new_content.value;
  if (content.data_type == "0") {
    const rows = content.data_list || [];
    return rows.length > 0
      ? rows.map((item: any) => normalize_list_item(item))
      : placeholder_list();
  }
  const auto_rows = content.data_auto_list || [];
  return auto_rows.length > 0
    ? auto_rows.map((item: any) => normalize_list_item(item))
    : placeholder_list();
};

/**
 * 自动获取模式：实时拉取真实直播间（装修器所见即所得）
 *
 * 与视频侧刻意不同：**只渲染，不写回 content.data_auto_list**。
 * 原因：直播间状态分秒变化（未开播/直播中/离开），一旦把取数结果落成快照存进装修配置，
 * app 端 data_type=='1' 时会优先用快照、快照非空就不再实时拉，导致线上一直显示
 * 编辑器里那一刻的旧状态。所以直播预览取数结果只进本地 data_list。
 */
const get_auto_data_list = async () => {
  const content = new_content.value as any;
  const status_list: string[] = Array.isArray(content.live_status)
    ? content.live_status.filter((v: any) => v !== "" && v != null)
    : [];
  const recommended_raw = String(content.is_recommended ?? "");
  try {
    const res: any = await CommonAPI.getLiveAutoList({
      keywords: content.keywords || "",
      status: status_list.join(","),
      is_recommended: recommended_raw === "" ? null : Number(recommended_raw),
      number: content.number || 10,
    });
    const rows = res?.data;
    const list = live_list_normalize(rows);
    data_list.value = list.length > 0 ? list : placeholder_list();
  } catch (e) {
    // 取数失败（服务未起/网络异常）回退占位数据，不阻断装修编辑
    data_list.value = placeholder_list();
  }
};

/** 按数据来源刷新画布数据：手动=用已存快照，自动=实时取数 */
const refresh_data_list = () => {
  if (new_content.value.data_type == "0") {
    data_list.value = resolve_data_list();
  } else {
    get_auto_data_list();
  }
};

onMounted(() => {
  refresh_data_list();
});

// -------- 直播状态（对齐 app modules/plugins-live-status.js） --------
const LIVE_ROOM_STATUS_LIST = [
  { type: "0", name: "离线" },
  { type: "1", name: "直播中" },
  { type: "2", name: "离开" },
  { type: "3", name: "封禁" },
];
const get_live_room_status = (data?: LiveRowData | null) => {
  if (!data) return null;
  const raw =
    data.status !== undefined && data.status !== null && data.status !== ""
      ? data.status
      : (data as any).live_status;
  if (raw === "" || raw === undefined || raw === null) return null;
  const n = Number(raw);
  return Number.isNaN(n) ? null : n;
};
const should_show_live_status = (data?: LiveRowData | null) =>
  get_live_room_status(data) !== null;
const get_live_room_status_name = (data?: LiveRowData | null) => {
  if (data && data.status_name) return String(data.status_name);
  const s = get_live_room_status(data);
  if (s === null) return "";
  const item = LIVE_ROOM_STATUS_LIST.find((it) => String(it.type) === String(s));
  return item ? item.name : "";
};
const live_item_title = (item: LiveRow) =>
  item.new_title ? item.new_title : item.data?.title || "";
const live_seller_name = (item: LiveRow) => {
  const name = item.data?.seller_name;
  return name != null && String(name).trim() !== "" ? String(name) : "";
};
const live_viewer_count = (item: LiveRow) =>
  item.data?.online_count ?? (item.data as any)?.viewer_count ?? "0";

// -------- 样式计算（逐行对齐 app apply_theme_styles） --------
const theme_list = [
  { name: "单列展示", value: "0", width: 110, height: 83 },
  { name: "两列展示（纵向）", value: "1", width: 180, height: 180 },
  { name: "大图展示", value: "2", width: 0, height: 180 },
  { name: "无图模式", value: "3", width: 0, height: 0 },
  { name: "左右滑动展示", value: "4", width: 0, height: 0 },
];

const plugins_live_theme_class = computed(() => {
  switch (plugins_live_theme.value) {
    case "0":
      return "style1 flex-col";
    case "1":
      return "style2 flex-row flex-wrap";
    case "2":
      return "style3 flex-col";
    case "3":
      return "style4 flex-col";
    default:
      return "style5";
  }
});

// 图片宽高（装修器画布按 px 预览，app 为 rpx/2，数值等价）
const plugins_live_img_width = computed(() => {
  const s = new_style.value as any;
  if (typeof s.content_img_width === "number") return s.content_img_width + "px";
  const list = theme_list.filter((item) => item.value == new_content.value.theme);
  return list.length > 0 ? list[0].width + "px" : "auto";
});
const plugins_live_img_height = computed(() => {
  const s = new_style.value as any;
  if (typeof s.content_img_height === "number")
    return s.content_img_height + "px";
  const list = theme_list.filter((item) => item.value == new_content.value.theme);
  return list.length > 0 ? list[0].height + "px" : "auto";
});

watch(
  () => props.value,
  (newVal) => {
    const content = newVal?.content || {};
    const s = newVal?.style || {};
    refresh_data_list();

    plugins_live_theme.value = content.theme;
    field_show.value = Array.isArray(content.field_show)
      ? content.field_show.map((v: any) => String(v))
      : ["0", "1", "2", "3"];
    name_float.value =
      content.name_float !== undefined && content.name_float !== ""
        ? content.name_float
        : "0";

    // 字体（对齐 app：rpx 值 = 画布 px 值 × 2，此处取等价 px）
    plugins_live_name.value =
      "font-size:" +
      s.name_size +
      "px;font-weight:" +
      s.name_weight +
      ";color:" +
      s.name_color +
      ";";
    plugins_live_seller_name.value =
      "font-size:" +
      s.time_size +
      "px;font-weight:" +
      s.time_weight +
      ";color:" +
      s.time_color +
      ";";
    plugins_live_page_view.value =
      "font-size:" +
      s.page_view_size +
      "px;" +
      "line-height:1;" +
      "font-weight:" +
      s.page_view_weight +
      ";color:" +
      s.page_view_color +
      ";";
    plugins_live_page_view_icon_size.value = s.page_view_size;

    content_radius.value = radius_computer(s.content_radius);
    img_radius.value = radius_computer(s.img_radius);
    content_padding.value = padding_computer(s.padding);
    content_spacing.value = `gap: ${s.content_spacing}px;`;
    plugins_live_spacing.value = `gap: ${s.plugins_live_spacing}px;`;
    plugins_live_name_height_computer.value = `height:${
      s.name_size * 2.4
    }px;line-height:${s.name_size * 1.2}px;`;
    carousel_height_computer.value = `${s.plugins_live_height}px;`;

    // 标题浮起背景
    const {
      name_bg_color_list = [],
      name_bg_direction = "180deg",
      name_bg_radius = old_radius,
      name_bg_padding = old_padding,
      name_bg_margin = old_margin,
    } = s as any;
    float_name_style.value =
      gradient_computer({
        color_list: name_bg_color_list,
        direction: name_bg_direction,
      }) +
      radius_computer(name_bg_radius) +
      padding_computer(name_bg_padding) +
      margin_computer(name_bg_margin) +
      "position:absolute;bottom:0;left:0;right:0;";

    // 背景与整体样式
    const plugins_live_data = {
      background_img_style: s.plugins_live_background_img_style || "",
      background_img: s.plugins_live_background_img || [],
    };
    const plugins_live_margin = s.margin || old_margin;
    const margin_width =
      (plugins_live_margin.margin_left || 0) +
      (plugins_live_margin.margin_right || 0);
    const all_style =
      gradient_handle(s.plugins_live_color_list || [], s.plugins_live_direction || "") +
      margin_computer(plugins_live_margin) +
      box_shadow_computer(s) +
      border_computer(s);

    style.value = "";
    img_size.value = `width:${plugins_live_img_width.value};height:${plugins_live_img_height.value};`;

    if (plugins_live_theme.value == "0") {
      style.value = content_radius.value + all_style;
    } else if (plugins_live_theme.value == "1") {
      style.value =
        `width: calc(50% - ${
          (Number(s.plugins_live_spacing) + margin_width * 2) / 2
        }px);` +
        content_radius.value +
        all_style;
    } else if (plugins_live_theme.value == "2") {
      style.value = content_radius.value + all_style;
    } else if (plugins_live_theme.value == "3") {
      // 无图模式：外层仅背景+圆角（对齐 app）
      style.value = `background:#fff;` + content_radius.value;
    } else if (plugins_live_theme.value == "4") {
      carousel_col.value = content.carousel_col || "1";
      carousel_key.value =
        s.interval_time + s.is_roll + s.rolling_fashion;
      if (Number(s.is_roll) === 1 && data_list.value.length > 0) {
        autoplay.value = {
          delay: (s.interval_time || 3) * 1000,
          pauseOnMouseEnter: true,
        };
      } else {
        autoplay.value = false;
      }
      slides_per_group.value =
        s.rolling_fashion == "translation"
          ? 1
          : Number(content.carousel_col) + 1;
      style.value = content_radius.value + all_style;
    }

    if (s.common_style && props.isCommonStyle) {
      style_container.value = common_styles_computer(s.common_style);
      style_img_container.value = common_img_computer(s.common_style);
    }
  },
  { deep: true, immediate: true }
);
</script>
<style lang="scss" scoped>
.style1 {
  .item {
    max-width: 100%;
    .img {
      height: v-bind(plugins_live_img_height);
      width: v-bind(plugins_live_img_width);
    }
  }
}
.style2 {
  .item {
    .img {
      height: v-bind(plugins_live_img_height);
    }
  }
}
.style3 {
  .item {
    width: 100%;
    .img {
      width: 100%;
      height: v-bind(plugins_live_img_height);
    }
  }
}
.style4 {
  .item {
    width: 100%;
    &:not(:last-child) {
      border-bottom: 1rpx solid #eee;
    }
  }
}
.style5 {
  .item {
    height: v-bind(carousel_height_computer);
    .img {
      width: 100%;
      height: 100%;
    }
    .title {
      height: v-bind(plugins_live_name_height_computer);
    }
  }
}
.live-page-view {
  line-height: 1;
  span {
    line-height: 1;
  }
}
/* 直播状态标签（与 app pages/diy plugins-live-list 一致） */
.live-status-tag {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  z-index: 2;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 28rpx;
  color: #fff;
  background-color: #999;
  border-radius: 4rpx;
}
/* 无图模式：标题左对齐，状态在标题右侧 */
.live-noimg-row {
  gap: 16rpx;
  min-width: 0;
}
.live-status-tag.live-status-tag--inline {
  position: static;
  top: auto;
  left: auto;
  z-index: auto;
  flex-shrink: 0;
  align-self: center;
}
.style4 .live-noimg-row .live-noimg-title {
  flex: 0 1 auto;
  min-width: 0;
  text-align: left;
}
/* status-1 直播中 / status-2 离开 / status-3 封禁，默认灰为离线 */
.live-status-tag.status-1 {
  background-color: #e93633;
}
.live-status-tag.status-2 {
  background-color: #f5c242;
}
.live-status-tag.status-3 {
  background-color: #666;
}
/* 直播中竖条跳动动画 */
.live-status-dots {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  height: 16rpx;
  gap: 4rpx;
}
.live-status-dots .dot {
  width: 4rpx;
  background-color: #fff;
  border-radius: 2rpx;
  transform-origin: bottom;
}
.live-status-dots .dot:nth-child(1) {
  animation: live-bar1-random 1.1s infinite ease-in-out both;
}
.live-status-dots .dot:nth-child(2) {
  animation: live-bar2-random 1.4s infinite ease-in-out both;
}
.live-status-dots .dot:nth-child(3) {
  animation: live-bar3-random 1.2s infinite ease-in-out both;
}
@keyframes live-bar1-random {
  0% {
    height: 4rpx;
  }
  50% {
    height: 14rpx;
  }
  100% {
    height: 4rpx;
  }
}
@keyframes live-bar2-random {
  0% {
    height: 6rpx;
  }
  50% {
    height: 12rpx;
  }
  100% {
    height: 6rpx;
  }
}
@keyframes live-bar3-random {
  0% {
    height: 5rpx;
  }
  50% {
    height: 13rpx;
  }
  100% {
    height: 5rpx;
  }
}
</style>
