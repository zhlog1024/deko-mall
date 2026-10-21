<template>
  <el-cascader
    :model-value="modelValue"
    :options="options"
    :loading="loading"
    :props="{ expandTrigger: 'hover' }"
    :placeholder="placeholder"
    clearable
    filterable
    style="width: 100%"
    @update:model-value="handleChange"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getAllRegions } from "@/api/system/region";

/**
 * 省市区级联选择（公共组件，数据源 mall-system sys_region）
 *
 * 口径：值与平台收货地址/订单快照一致 —— 存 sys_region.id（非行政代码）
 *
 * v-model: string[]（省/市/区 3 个 id 的字符串数组，或空数组）
 * @change: { ids: string[]; names: string[] } —— names 与 ids 一一对应（省/市/区名称）
 * 方法：matchByNames(province?, city?, county?) -> string[] | null（供地图逆地理按名称反查 id）
 */
interface RegionOption {
  value: string;
  label: string;
  children?: RegionOption[];
}

const props = defineProps<{
  modelValue?: string[];
  placeholder?: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: string[]): void;
  (e: "change", data: { ids: string[]; names: string[] }): void;
}>();

const options = ref<RegionOption[]>([]);
const loading = ref(false);

// 模块级缓存：同一次会话内多个页面/弹窗共用一次请求
let treeCache: Promise<RegionOption[]> | null = null;

function buildOptions(nodes: any[], depth = 0): RegionOption[] {
  return (nodes || []).map((n: any) => {
    const option: RegionOption = {
      value: String(n.id ?? ""),
      label: n.name || "",
    };
    if (depth < 2 && Array.isArray(n.children) && n.children.length > 0) {
      option.children = buildOptions(n.children, depth + 1);
    }
    return option;
  });
}

function loadOptions(): Promise<RegionOption[]> {
  if (!treeCache) {
    treeCache = getAllRegions()
      .then((res) => buildOptions((res.data || []) as any[]))
      .catch((e) => {
        treeCache = null;
        throw e;
      });
  }
  return treeCache;
}

function findNamesByIds(ids: string[]): string[] {
  if (ids.length !== 3) return [];
  const names: string[] = [];
  let level: RegionOption[] | undefined = options.value;
  for (const id of ids) {
    const node = (level || []).find((o) => o.value === id);
    names.push(node ? node.label : "");
    level = node ? node.children : undefined;
  }
  return names;
}

/** 按名称反查三级 id（地图逆地理回填用；匹配失败返回 null） */
function matchByNames(province?: string, city?: string, county?: string): string[] | null {
  if (!province || options.value.length === 0) return null;
  const prov = options.value.find((o) => o.label === province);
  if (!prov) return null;
  const cityName = city || province;
  const cityNode = (prov.children || []).find((o) => o.label === cityName) || (prov.children || [])[0];
  if (!cityNode) return null;
  const countyName = county || "";
  const countyNode = countyName
    ? (cityNode.children || []).find((o) => o.label === countyName)
    : (cityNode.children || [])[0];
  if (!countyNode) return null;
  return [prov.value, cityNode.value, countyNode.value];
}

function handleChange(value: unknown) {
  const ids = Array.isArray(value) ? value.map(String) : [];
  emit("update:modelValue", ids);
  emit("change", { ids, names: findNamesByIds(ids) });
}

defineExpose({ matchByNames });

onMounted(async () => {
  loading.value = true;
  try {
    options.value = await loadOptions();
  } catch (e) {
    console.error("省市区地址库加载失败", e);
  } finally {
    loading.value = false;
  }
});
</script>
