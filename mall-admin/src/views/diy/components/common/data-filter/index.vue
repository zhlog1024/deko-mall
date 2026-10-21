<template>
  <el-form-item label="读取方式">
    <el-radio-group v-model="form.data_type">
      <el-radio
        v-for="item in option_list"
        :key="item.value"
        :value="item.value"
        >{{ item.name }}</el-radio
      >
    </el-radio-group>
  </el-form-item>
  <div v-show="form.data_type === '0'" class="nav-list">
    <drag-group
      :list="drag_list"
      :img-params="config.imgParam"
      :title-params="config.titleParam"
      :type="config.typeParam"
      @onsort="data_list_sort"
      @remove="data_list_remove"
      @replace="data_list_replace"
    />
    <el-button class="mt-20 w" @click="add">+添加</el-button>
  </div>
  <div v-show="form.data_type === '1'" class="w h">
    <!-- 商品 -->
    <template v-if="type === 'goods'">
      <el-form-item label="商品分组">
        <div class="flex-row gap-10 w h">
          <el-select
            v-model="form.group_id"
            filterable
            clearable
            placeholder="请选择商品分组"
            class="w h"
            @change="handle_group_change"
          >
            <el-option
              v-for="item in common_store.common.spu_group_list"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="显示数量">
        <el-input-number
          v-model="form.number"
          :min="1"
          :max="50"
          type="number"
          placeholder="请输入显示数量"
          value-on-clear="min"
          class="w number-show"
          controls-position="right"
        />
      </el-form-item>
    </template>
    <!-- 品牌 -->
    <template v-else-if="type === 'brand'">
      <el-form-item label="品牌分类">
        <el-select
          v-model="form.category_ids"
          multiple
          collapse-tags
          filterable
          placeholder="请选择品牌分类"
        >
          <el-option
            v-for="item in common_store.common.brand_list"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="显示数量">
        <el-input-number
          v-model="form.number"
          :min="1"
          :max="50"
          type="number"
          placeholder="请输入显示数量"
          value-on-clear="min"
          class="w number-show"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="排序类型">
        <el-radio-group v-model="form.order_by_type">
          <el-radio
            v-for="item in common_store.common.brand_order_by_type_list"
            :key="item.index"
            :value="item.index"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序规则">
        <el-radio-group v-model="form.order_by_rule">
          <el-radio
            v-for="item in common_store.common.data_order_by_rule_list"
            :key="item.index"
            :value="item.index"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
    </template>
    <!-- 文章 -->
    <template v-else-if="type === 'article'">
      <el-form-item label="文章分类">
        <el-tree-select
          v-model="form.category_ids"
          :data="article_category_tree"
          multiple
          :render-after-expand="false"
          :show-checkbox="true"
          :node-key="'id'"
          :props="{ label: 'name', children: 'children' }"
          check-strictly
          placeholder="请选择文章分类"
          class="w"
        />
      </el-form-item>
      <el-form-item label="显示数量">
        <el-input-number
          v-model="form.number"
          :min="1"
          :max="50"
          type="number"
          placeholder="请输入显示数量"
          value-on-clear="min"
          class="w number-show"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="排序类型">
        <el-radio-group v-model="form.order_by_type">
          <el-radio
            v-for="item in article_order_by_type_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序规则">
        <el-radio-group v-model="form.order_by_rule">
          <el-radio
            v-for="item in article_order_by_rule_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="仅显示有封面">
        <el-switch
          v-model="form.is_cover"
          active-value="1"
          inactive-value="0"
        />
      </el-form-item>
    </template>
    <!-- 问答 -->
    <template v-else-if="type === 'ask'">
      <el-form-item label="关键词">
        <el-input
          v-model="keywords"
          placeholder="请输入问题关键词"
          clearable
          @blur="keyword_blur"
        />
      </el-form-item>
      <el-form-item label="回复状态">
        <el-radio-group v-model="form.is_reply">
          <el-radio
            v-for="item in ask_reply_filter_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="显示数量">
        <el-input-number
          v-model="form.number"
          :min="1"
          :max="50"
          type="number"
          placeholder="请输入显示数量"
          value-on-clear="min"
          class="w number-show"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="排序类型">
        <el-radio-group v-model="form.order_by_type">
          <el-radio
            v-for="item in ask_order_by_type_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序规则">
        <el-radio-group v-model="form.order_by_rule">
          <el-radio
            v-for="item in ask_order_by_rule_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
    </template>
    <!-- 视频(无分类,category 二期;排序一维 order_by_type → mall-cms sortField) -->
    <template v-else-if="type === 'plugins-video'">
      <el-form-item label="关键词">
        <el-input
          v-model="keywords"
          placeholder="请输入视频标题关键词"
          clearable
          @blur="keyword_blur"
        />
      </el-form-item>
      <el-form-item label="是否推荐">
        <el-radio-group v-model="form.is_recommended">
          <el-radio
            v-for="item in video_recommend_filter_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否热门">
        <el-radio-group v-model="form.is_hot">
          <el-radio
            v-for="item in video_hot_filter_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="显示数量">
        <el-input-number
          v-model="form.number"
          :min="1"
          :max="50"
          type="number"
          placeholder="请输入显示数量"
          value-on-clear="min"
          class="w number-show"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="排序类型">
        <el-radio-group v-model="form.order_by_type">
          <el-radio
            v-for="item in video_order_by_type_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序规则">
        <el-radio-group v-model="form.order_by_rule">
          <el-radio
            v-for="item in video_order_by_rule_list"
            :key="item.value"
            :value="item.value"
            >{{ item.name }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
    </template>
    <!-- 店铺（多商户） -->
    <template v-else-if="type === 'merchant'">
      <el-form-item label="显示数量">
        <el-input-number
          v-model="form.number"
          :min="1"
          :max="50"
          type="number"
          placeholder="请输入显示数量"
          value-on-clear="min"
          class="w number-show"
          controls-position="right"
        />
      </el-form-item>
    </template>
    <!-- 其他类型兜底 -->
    <template v-else> </template>
  </div>
</template>

<script lang="ts" setup>
import { commonStore } from "../../../store";
import { get_data_list } from "../../../utils";
import { getSpuGroups } from "@/api/pms/spu-group";
import CommonAPI from "../../../api/common";
import { nextTick } from "vue";
const common_store = commonStore();

// 文章自动筛选选项(语义对齐 mall-cms orderByType:0综合/1人气/2最新;规则 0降序/1升序)
const article_order_by_type_list = [
  { name: "综合", value: 0 },
  { name: "人气", value: 1 },
  { name: "最新", value: 2 },
];
const article_order_by_rule_list = [
  { name: "降序", value: 0 },
  { name: "升序", value: 1 },
];
// 问答自动筛选选项(排序字段为 DIY 契约占位:mall-cms ask 后端当前固定 自定义排序+创建时间倒序)
const ask_order_by_type_list = [
  { name: "综合", value: 0 },
  { name: "最新", value: 1 },
];
const ask_order_by_rule_list = [
  { name: "降序", value: 0 },
  { name: "升序", value: 1 },
];
// 视频推荐/热门过滤(全部/是/否;后端空=不筛选,0=非推荐/非热门)
const video_recommend_filter_list = [
  { name: "全部", value: "" },
  { name: "是", value: "1" },
  { name: "否", value: "0" },
];
const video_hot_filter_list = [
  { name: "全部", value: "" },
  { name: "是", value: "1" },
  { name: "否", value: "0" },
];
// 视频排序类型(0综合=默认 sort+创建时间/1最新=createTime/2浏览量=accessCount/3时长=duration → mall-cms 映射 sortField)
const video_order_by_type_list = [
  { name: "综合", value: 0 },
  { name: "最新", value: 1 },
  { name: "浏览量", value: 2 },
  { name: "时长", value: 3 },
];
// 视频排序规则(0降序/1升序;综合忽略,其余按所选字段方向 → mall-cms 转发 orderByRule)
const video_order_by_rule_list = [
  { name: "降序", value: 0 },
  { name: "升序", value: 1 },
];
// 文章分类(树形,来自 diy-designer/article/category;勾选父分类后端递归含子)
interface ArticleCategoryTreeItem {
  id: number;
  name: string;
  children?: ArticleCategoryTreeItem[];
}
const article_category_tree = ref<ArticleCategoryTreeItem[]>([]);
const loadArticleCategory = async () => {
  try {
    const res = await CommonAPI.getArticleCategory();
    article_category_tree.value = (res?.data ||
      []) as ArticleCategoryTreeItem[];
  } catch (error) {
    console.error("加载文章分类失败:", error);
  }
};
// 定义组件的属性和事件
const props = defineProps({
  value: {
    type: Object as PropType<any>,
    default: () => {},
  },
  list: {
    type: Array,
    default: () => [],
  },
  baseList: {
    type: Object,
    default: () => {},
  },
  type: {
    type: String as PropType<
      | "goods"
      | "brand"
      | "merchant"
      | "article"
      | "ask"
      | "plugins-video"
    >,
    default: "goods",
  },
});
// 问答回复状态过滤(全部/未回/已回,取传入 baseList.filter_list,缺省给源默认)
const ask_reply_filter_list = computed(() => {
  const source = (props.baseList && props.baseList.filter_list) || [];
  if (Array.isArray(source) && source.length > 0) {
    return source;
  }
  return [
    { name: "全部", value: "" },
    { name: "未回", value: "0" },
    { name: "已回", value: "1" },
  ];
});
const keywords = ref(props.value.keywords);
const form = ref(props.value);
const drag_list = ref(props.list);
type option = {
  name: string;
  value: string;
};
const option_list = ref<option[]>([]);
// 不同组件的默认配置
const configMap = {
  goods: {
    optionListKey: "product_list",
    imgParam: "images",
    titleParam: "title",
  },
  brand: {
    optionListKey: "brand_data_type_list",
    imgParam: "logo",
    titleParam: "title",
  },
  article: {
    optionListKey: "data_type_list",
    imgParam: "cover",
    titleParam: "title",
  },
  ask: {
    optionListKey: "data_type_list",
    imgParam: "",
    titleParam: "title",
    typeParam: "custom",
  },
  "plugins-video": {
    optionListKey: "data_type_list",
    imgParam: "cover",
    titleParam: "title",
  },
  merchant: {
    optionListKey: "data_type_list",
    imgParam: "logo",
    titleParam: "name",
  },
  default: {
    optionListKey: "brand_data_type_list",
    imgParam: "logo",
    titleParam: "title",
  },
};
type config_type = {
  optionListKey: string;
  imgParam: string;
  titleParam: string;
  typeParam?: string;
};
const config = ref<config_type>(configMap.default);
// 更新数据,避免子组件数据不刷新
watchEffect(() => {
  // 获取不同数据对应的值
  config.value = configMap[props.type] || configMap.default;
  // 不同数据对应的type类型
  option_list.value = props.baseList[config.value.optionListKey];
  // 关键字复制
  keywords.value = props.value.keywords;
  form.value = props.value;
  // 历史数据转成数字类型
  form.value.order_by_type = Number(props.value?.order_by_type || 0);
  form.value.order_by_rule = Number(props.value?.order_by_rule || 0);
  drag_list.value = props.list;
});

// 加载商品分组列表
const loadSpuGroupList = async () => {
  // 调用接口加载分组列表
  try {
    const res = await getSpuGroups({
      pageNum: 1,
      pageSize: 100,
      groupCode: "",
      name: "",
    });

    // 注意：后端返回的数据结构是 res.data.records，不是 res.data.list
    if (res.data && res.data.records) {
      // 更新store中的分组列表
      common_store.common.spu_group_list = res.data.records;

      // 检查当前的group_id是否在分组列表中存在
      const currentGroupId = form.value.group_id;
      const isValidGroup = res.data.records.some(
        (group) => group.id === currentGroupId
      );

      // 如果当前没有选中分组，或者选中的分组不在列表中，则默认选中第一个
      if (!currentGroupId || !isValidGroup) {
        if (res.data.records.length > 0) {
          form.value.group_id = res.data.records[0].id;
        }
      }
    }
  } catch (error) {
    console.error("加载商品分组列表失败:", error);
  }
};

// 监听读取方式和类型变化，当切换到筛选商品时加载分组列表
watch(
  () => [form.value.data_type, props.type],
  async ([dataType, type]) => {
    if (dataType === "1" && type === "goods") {
      await loadSpuGroupList();
    }
    if (dataType === "1" && type === "article") {
      await loadArticleCategory();
    }
  },
  { immediate: true }
);

// 监听分组列表变化，如果当前没有选中分组且有可用分组，自动选中第一个
watch(
  () => common_store.common.spu_group_list,
  (newList) => {
    if (
      form.value.data_type === "1" &&
      props.type === "goods" &&
      newList &&
      newList.length > 0
    ) {
      // 检查当前的group_id是否在分组列表中存在
      const currentGroupId = form.value.group_id;
      const isValidGroup = newList.some((group) => group.id === currentGroupId);

      // 如果当前没有选中分组，或者选中的分组不在列表中，则默认选中第一个
      if (!currentGroupId || !isValidGroup) {
        // 使用nextTick确保在下一个tick中设置，避免同步触发多次watch
        nextTick(() => {
          form.value.group_id = newList[0].id;
        });
      }
    }
  },
  { deep: true }
);
const keyword_blur = () => {
  form.value.keywords = keywords.value;
};
// 商品分组变化时的处理
const handle_group_change = (value: any) => {
  console.log("商品分组变化:", value);
  // 分组切换时会触发watch监听，自动重新查询商品列表
};
const emits = defineEmits([
  "data_list_sort",
  "data_list_remove",
  "data_list_replace",
  "add",
]);
// 数组顺序更新时触发事件
const data_list_sort = (new_list: any) => {
  emits("data_list_sort", new_list);
};
// 数组数据删除时触发的事件
const data_list_remove = (index: number) => {
  emits("data_list_remove", index);
};
// 数组数据替换时触发的事件
const data_list_replace = (index: number) => {
  emits("data_list_replace", index);
};
// 数组添加数据时触发的事件
const add = () => {
  emits("add");
};
</script>
