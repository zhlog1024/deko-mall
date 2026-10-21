<template>
  <div class="freight-template-detail">
    <el-card>
      <template #header>
        <div class="header-actions">
          <el-button @click="handleCancel">
            <i-ep-back />
            返回列表
          </el-button>
          <template v-if="mode === 'view'">
            <el-button type="primary" @click="switchToEditMode" v-if="id">
              <i-ep-edit />
              编辑
            </el-button>
          </template>
        </div>
      </template>
      <el-form
        :model="form"
        :rules="dynamicRules"
        ref="formRef"
        label-width="100px"
        :disabled="mode === 'view'"
        style="max-width: 700px; margin: 0 auto"
      >
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="是否包邮" prop="isFreeShipping">
          <el-radio-group v-model="form.isFreeShipping">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认模板">
          <el-radio-group v-model="form.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="国家" v-if="enableInternational">
          <el-select v-model="selectedCountryId" @change="handleCountryChange" placeholder="请选择国家" style="width:260px">
            <el-option v-for="c in countryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <template v-if="form.isFreeShipping === 0">
          <el-form-item label="运费规则" prop="freightRules">
            <div class="freight-rules-container">
              <div
                v-for="(rule, index) in freightRules.filter(
                  (r) => !r.isDefault
                )"
                :key="index"
                class="freight-rule-item"
              >
                <el-card>
                  <div class="freight-rule-header">
                    <span>规则 {{ index + 1 }}</span>
                    <el-button
                      v-if="mode !== 'view'"
                      type="danger"
                      size="small"
                      @click="removeRule(index)"
                    >
                      删除
                    </el-button>
                  </div>
                  <el-form-item
                    label="区域"
                    :prop="`rules.${index}.regionNames`"
                  >
                    <el-select
                      multiple
                      filterable
                      v-model="rule.regionCodesArray"
                      @change="handleRegionChange(rule, index)"
                      placeholder="请选择省份"
                    >
                      <el-option
                        v-for="region in getAvailableRegions(index)"
                        :key="region.id"
                        :label="region.name"
                        :value="region.id.toString()"
                      />
                    </el-select>
                  </el-form-item>

                  <el-form-item
                    label="首件(个)"
                    :prop="`rules.${index}.firstItem`"
                  >
                    <el-input-number
                      v-model="rule.firstItemDisplay"
                      :min="1"
                      controls-position="right"
                      @change="(val) => (rule.firstItem = val ?? 1)"
                    />
                  </el-form-item>
                  <el-form-item
                    label="首费(元)"
                    :prop="`rules.${index}.firstFee`"
                  >
                    <el-input-number
                      v-model="rule.firstFeeDisplay"
                      :min="0"
                      :precision="2"
                      :step="0.5"
                      controls-position="right"
                      @change="
                        (val) => handleFeeChange(val ?? 0, rule, 'first')
                      "
                    />
                  </el-form-item>
                  <el-form-item
                    label="续件(个)"
                    :prop="`rules.${index}.continueItem`"
                  >
                    <el-input-number
                      v-model="rule.continueItemDisplay"
                      :min="1"
                      controls-position="right"
                      @change="(val) => (rule.continueItem = val ?? 1)"
                    />
                  </el-form-item>
                  <el-form-item
                    label="续费(元)"
                    :prop="`rules.${index}.continueFee`"
                  >
                    <el-input-number
                      v-model="rule.continueFeeDisplay"
                      :min="0"
                      :precision="2"
                      :step="0.5"
                      controls-position="right"
                      @change="
                        (val) => handleFeeChange(val ?? 0, rule, 'continue')
                      "
                    />
                  </el-form-item>
                </el-card>
              </div>

              <!-- 默认规则区域 -->
              <el-divider content-position="center"
                >默认规则（适用于未指定区域）
              </el-divider>

              <div class="freight-rule-item default-rule">
                <el-card>
                  <div class="freight-rule-header">
                    <span>默认规则</span>
                  </div>

                  <el-form-item label="首件(个)" prop="defaultRule.firstItem">
                    <el-input-number
                      v-model="defaultRule.firstItemDisplay"
                      :min="1"
                      controls-position="right"
                      @change="(val) => (defaultRule.firstItem = val ?? 1)"
                    />
                  </el-form-item>

                  <el-form-item label="首费(元)" prop="defaultRule.firstFee">
                    <el-input-number
                      v-model="defaultRule.firstFeeDisplay"
                      :min="0"
                      :precision="2"
                      :step="0.5"
                      controls-position="right"
                      @change="
                        (val) => handleFeeChange(val ?? 0, defaultRule, 'first')
                      "
                    />
                  </el-form-item>

                  <el-form-item
                    label="续件(个)"
                    prop="defaultRule.continueItem"
                  >
                    <el-input-number
                      v-model="defaultRule.continueItemDisplay"
                      :min="1"
                      controls-position="right"
                      @change="(val) => (defaultRule.continueItem = val ?? 1)"
                    />
                  </el-form-item>

                  <el-form-item label="续费(元)" prop="defaultRule.continueFee">
                    <el-input-number
                      v-model="defaultRule.continueFeeDisplay"
                      :min="0"
                      :precision="2"
                      :step="0.5"
                      controls-position="right"
                      @change="
                        (val) =>
                          handleFeeChange(val ?? 0, defaultRule, 'continue')
                      "
                    />
                  </el-form-item>
                </el-card>
              </div>

              <div v-if="mode !== 'view'" class="add-rule-btn">
                <el-button type="primary" @click="addRule">添加规则</el-button>
              </div>
            </div>
          </el-form-item>
        </template>

        <el-form-item v-if="mode !== 'view'">
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  createFreightTemplate,
  getFreightTemplate,
  updateFreightTemplate,
} from "@/api/pms/freight-template";
import {
  FreightRuleVO,
  FreightTemplate,
  FreightTemplateForm,
} from "@/api/pms/freight-template/types";
import { ElMessage } from "element-plus";
import { getRegionList, getCountryList, getAppConfig } from "@/api/system/region";
import { RegionDTO } from "@/api/system/region/types";

defineOptions({
  name: "FreightTemplateDetail",
  inheritAttrs: false,
});

const formRef = ref();
const route = useRoute();
const router = useRouter();

// 优化ID处理逻辑，避免字符串和空值问题
const id = ref(route.query.id ? String(route.query.id) : undefined);
const mode = ref(
  route.query.mode ? String(route.query.mode) : id.value ? "view" : "create"
);

// 调试信息
console.log("路由参数:", route.query);
console.log("解析的ID值:", id.value, "类型:", typeof id.value);
console.log("页面模式:", mode.value);

// 国家选项
const enableInternational = ref(false); // 国际化开关
const countryOptions = ref<RegionDTO[]>([]);
const selectedCountryId = ref<number>(778937); // 默认中国

// 区域选项
const regionOptions = ref<RegionDTO[]>([]);
// 运费规则数据 - 初始化时暂时不添加默认规则
const freightRules = reactive<
  (FreightRuleVO & {
    regionCodesArray?: string[];
    firstItemDisplay?: number;
    firstFeeDisplay?: number;
    continueItemDisplay?: number;
    continueFeeDisplay?: number;
  })[]
>([]);

const form = reactive<FreightTemplateForm & { countryId?: number }>({
  templateName: "",
  chargeType: "BY_PIECE", // 默认固定为按件计费
  isFreeShipping: 0,
  isDefault: 0,
  rules: [],
});

// 初始化默认规则
const createDefaultRule = () => ({
  id: undefined,
  regionCodes: "DEFAULT",
  regionNames: "默认区域",
  firstItem: 1,
  firstItemDisplay: 1,
  firstFee: 10, // 10元
  firstFeeDisplay: 10,
  continueItem: 1,
  continueItemDisplay: 1,
  continueFee: 5, // 5元
  continueFeeDisplay: 5,
  isDefault: 1,
});

const defaultRule = reactive(createDefaultRule());

const detail = ref<FreightTemplate>({} as FreightTemplate);

const rules = {
  templateName: [
    { required: true, message: "请输入模板名称", trigger: "blur" },
  ],
  isFreeShipping: [
    { required: true, message: "请选择是否包邮", trigger: "change" },
  ],
  rules: [{ required: true, message: "运费规则不能为空", trigger: "blur" }],
};

// 动态验证规则
const dynamicRules = computed(() => {
  const allRules = { ...rules };
  return allRules;
});

// 监听包邮状态变化
watch(
  () => form.isFreeShipping,
  (newValue) => {
    console.log("包邮状态变更为:", newValue);
    try {
      if (newValue === 1) {
        // 如果是包邮，清空运费规则
        form.rules = [];
        freightRules.splice(0);
      } else {
        // 如果不是包邮
        // 确保计费类型有默认值
        if (!form.chargeType) {
          form.chargeType = "BY_PIECE";
        }

        // 确保有至少一个运费规则
        if (freightRules.length === 0) {
          console.log("添加默认规则");
          addRule();
        }
      }
    } catch (error) {
      console.error("监听包邮状态变化异常:", error);
    }
  },
  { immediate: true }
);

/**
 * 切换到编辑模式
 */
function switchToEditMode() {
  // 将详情数据复制到表单
  mode.value = "edit";
}

// 不再需要监听计费类型变化，因为已固定为按件计费

// 添加规则
function addRule() {
  try {
    // 初始化规则对象
    const newRule: FreightRuleVO & {
      regionCodesArray: string[];
      firstItemDisplay: number;
      firstFeeDisplay: number;
      continueItemDisplay: number;
      continueFeeDisplay: number;
    } = {
      regionCodes: "",
      regionNames: "",
      regionCodesArray: [],
      firstItem: 10, // 10件
      firstFee: 0,
      continueItem: 10, // 10件
      continueFee: 0,
      firstFeeDisplay: 0, // 0元
      continueFeeDisplay: 0, // 0元
      firstItemDisplay: 0,
      continueItemDisplay: 0,
    };

    // 设置显示值
    newRule.firstItemDisplay = newRule.firstItem;
    newRule.continueItemDisplay = newRule.continueItem;

    // 如果regionOptions存在且为数组，尝试设置默认区域
    if (
      regionOptions &&
      Array.isArray(regionOptions) &&
      regionOptions.length > 0
    ) {
      // 检查已有规则，找出已使用的省份代码
      const usedRegionCodes = new Set();
      freightRules.forEach((rule) => {
        if (rule.regionCodesArray && Array.isArray(rule.regionCodesArray)) {
          rule.regionCodesArray.forEach(
            (code) => code && usedRegionCodes.add(code)
          );
        }
      });

      // 找出未使用的省份
      const unusedRegions = regionOptions.filter(
        (region) =>
          region && region.id && !usedRegionCodes.has(region.id.toString())
      );

      if (unusedRegions.length > 0) {
        // 默认选择前5个未使用的省份
        const defaultRegions = unusedRegions.slice(0, 5);
        const defaultRegionCodes = defaultRegions.map((r) => r.id.toString());
        const defaultRegionNames = defaultRegions.map((r) => r.name).join(",");

        // 设置区域相关字段
        newRule.regionCodes = defaultRegionCodes.join(",");
        newRule.regionNames = defaultRegionNames;
        newRule.regionCodesArray = defaultRegionCodes;
      }
    }

    // 添加到规则列表
    freightRules.push(newRule);
    // 同步到表单
    syncFreightRulesToForm();
  } catch (error) {
    console.error("添加规则出错:", error);
    ElMessage.error("添加规则失败，请重试");
  }
}

// 获取省份区域数据
async function fetchRegionData() {
  try {
    const { data } = await getRegionList(selectedCountryId.value);
    regionOptions.value = data || [];
  } catch (error) {
    console.error("获取区域数据失败:", error);
    ElMessage.warning("获取区域数据失败，将使用本地缓存数据");
  }
}

// 国家变更
function handleCountryChange() {
  fetchRegionData();
}

// 加载数据
onMounted(async () => {
  try {
    console.log("开始加载页面数据, 模板ID:", id.value, "模式:", mode.value);

    // 读取国际化配置
    try {
      const { data: config } = await getAppConfig();
      enableInternational.value = parseInt(config?.enable_international_phone) === 1;
    } catch (e) { /* ignore */ }

    // 先加载国家列表
    try {
      const { data: countries } = await getCountryList();
      countryOptions.value = countries || [];
    } catch (e) { /* ignore */ }

    // 如果是编辑模式，先加载模板详情（获取保存的国家），再加载区域
    if (id.value) {
      console.log("开始请求运费模板ID:", id.value);
      await fetchTemplateDetail();
      console.log("模板详情加载完成, selectedCountryId:", selectedCountryId.value);
    }

    // 加载区域数据（此时 selectedCountryId 已经是正确的国家）
    console.log("正在请求区域数据...");
    await fetchRegionData();
    console.log("区域数据加载完成, 数量:", regionOptions.value?.length);

    // 新建模板模式 + 编辑模式后续处理
    if (!id.value) {
      console.log("新建模板模式，无需加载模板数据");
      // 如果没有ID，创建一个默认装填规则
      if (form.isFreeShipping === 0) {
        addRule();
        // 确保默认规则初始化
        defaultRule.isDefault = 1;
        defaultRule.regionCodes = "DEFAULT";
        defaultRule.regionNames = "默认区域";
        defaultRule.firstItem = 1;
        defaultRule.firstItemDisplay = 1;
        defaultRule.firstFee = 10; // 10元
        defaultRule.firstFeeDisplay = 10;
        defaultRule.continueItem = 1;
        defaultRule.continueItemDisplay = 1;
        defaultRule.continueFee = 5; // 5元
        defaultRule.continueFeeDisplay = 5;
      }
    }

    console.log("页面初始化完成");
  } catch (error) {
    console.error("页面初始化错误:", error);
    ElMessage.error("页面初始化失败，请刷新重试");
  }
});

// 获取模板数据
async function fetchTemplateDetail() {
  if (!id.value) return;

  try {
    console.log("开始获取运费模板数据, ID:", id.value);

    // API调用增加错误处理
    let response;
    try {
      response = await getFreightTemplate(Number(id.value));
      console.log("模板API返回原始数据:", response);
    } catch (apiError) {
      console.error("调用运费模板API失败:", apiError);
      ElMessage.error("获取模板数据失败");
      throw apiError;
    }

    // 在加载前先重置默认规则为初始状态
    console.log("重置默认运费规则到初始状态");
    Object.assign(defaultRule, createDefaultRule());

    // 处理API返回结果，处理不同格式的返回
    // 有可能是 {data: 模板对象} 或者 {code: 200, data: 模板对象, msg: 'success'}
    let data;
    if (response && response.data !== undefined) {
      // 先检查是否是标准响应格式 {code, data, msg}
      if (response.code !== undefined && response.data !== undefined) {
        data = response.data; // 标准响应格式
        console.log("解析标准响应格式");
      } else {
        data = response.data; // 简化响应格式 {data: {...}}
        console.log("解析简化响应格式");
      }
    } else if (response && typeof response === "object") {
      data = response; // 如果直接返回模板对象
      console.log("解析直接返回对象格式");
    } else {
      console.error("运费模板数据格式不完整:", response);
      throw new Error("运费模板数据格式不完整");
    }

    console.log("处理后的模板数据:", data);

    // 保存模板数据
    detail.value = data;

    // 填充表单基本字段
    form.templateName = data.templateName || "";
    if ((data as any).countryId) selectedCountryId.value = (data as any).countryId;
    form.chargeType = data.chargeType || "BY_PIECE";
    form.isFreeShipping = data.isFreeShipping ?? 0;
    form.isDefault = data.isDefault ?? 0;

    // 如果不是包邮，处理运费规则
    if (form.isFreeShipping === 0) {
      if (data.rules && Array.isArray(data.rules) && data.rules.length > 0) {
        console.log("模板包含规则数量:", data.rules.length);

        // 首先分离默认规则和普通规则
        const defaultRuleData = data.rules.find((rule) => rule.isDefault === 1);
        const normalRules = data.rules.filter((rule) => rule.isDefault !== 1);

        console.log("默认规则:", defaultRuleData);
        console.log("普通规则数量:", normalRules.length);

        // 处理普通规则
        if (normalRules && normalRules.length > 0) {
          // 先清空在添加
          freightRules.splice(0, freightRules.length);

          // 遍历添加所有普通规则
          normalRules.forEach((rule) => {
            freightRules.push({
              id: rule.id,
              regionCodes: rule.regionCodes,
              regionNames: rule.regionNames,
              regionCodesArray: rule.regionCodes
                ? rule.regionCodes.split(",")
                : [],
              firstItem: rule.firstItem,
              firstItemDisplay: rule.firstItem,
              firstFee: rule.firstFee,
              firstFeeDisplay: rule.firstFee ,
              continueItem: rule.continueItem,
              continueItemDisplay: rule.continueItem,
              continueFee: rule.continueFee,
              continueFeeDisplay: rule.continueFee ,
              isDefault: 0,
            });
          });
        } else {
          // 如果没有普通规则，添加一个空的普通规则
          console.log("没有普通规则，创建空规则");
          addRule();
        }

        // 处理默认规则
        if (defaultRuleData) {
          console.log("冲现有默认规则数据:", defaultRuleData);
          // 先清除默认规则中的值，确保它不会保留上次的值
          Object.assign(defaultRule, {
            id: defaultRuleData.id,
            regionCodes: "DEFAULT",
            regionNames: "默认区域",
            isDefault: 1,
          });

          // 处理数字值
          // 首件数量
          if (
            defaultRuleData.firstItem !== undefined &&
            defaultRuleData.firstItem !== null
          ) {
            defaultRule.firstItem = defaultRuleData.firstItem;
            defaultRule.firstItemDisplay = defaultRuleData.firstItem;
          } else {
            defaultRule.firstItem = 1;
            defaultRule.firstItemDisplay = 1;
          }

          // 首费
          if (
            defaultRuleData.firstFee !== undefined &&
            defaultRuleData.firstFee !== null
          ) {
            defaultRule.firstFee = defaultRuleData.firstFee;
            defaultRule.firstFeeDisplay = defaultRuleData.firstFee;
          } else {
            defaultRule.firstFee = 10; // 10元
            defaultRule.firstFeeDisplay = 10;
          }

          // 续件数量
          if (
            defaultRuleData.continueItem !== undefined &&
            defaultRuleData.continueItem !== null
          ) {
            defaultRule.continueItem = defaultRuleData.continueItem;
            defaultRule.continueItemDisplay = defaultRuleData.continueItem;
          } else {
            defaultRule.continueItem = 1;
            defaultRule.continueItemDisplay = 1;
          }

          // 续费
          if (
            defaultRuleData.continueFee !== undefined &&
            defaultRuleData.continueFee !== null
          ) {
            defaultRule.continueFee = defaultRuleData.continueFee;
            defaultRule.continueFeeDisplay = defaultRuleData.continueFee;
          } else {
            defaultRule.continueFee = 5; // 5元
            defaultRule.continueFeeDisplay = 5;
          }

          console.log("初始化后的默认规则:", defaultRule);
        } else {
          console.log("没有默认规则，创建新的默认规则");
          // 完全重置默认规则
          Object.assign(defaultRule, {
            id: undefined,
            regionCodes: "DEFAULT",
            regionNames: "默认区域",
            firstItem: 1,
            firstItemDisplay: 1,
            firstFee: 10, // 10元
            firstFeeDisplay: 10,
            continueItem: 1,
            continueItemDisplay: 1,
            continueFee: 5, // 5元
            continueFeeDisplay: 5,
            isDefault: 1,
          });
        }
      } else {
        // 如果没有规则，添加空规则和默认规则
        console.log("模板没有规则，添加空规则");
        // 添加空的普通规则
        addRule();

        // 初始化默认规则
        defaultRule.id = undefined;
        defaultRule.regionCodes = "DEFAULT";
        defaultRule.regionNames = "默认区域";
        defaultRule.firstItem = 1;
        defaultRule.firstItemDisplay = 1;
        defaultRule.firstFee = 10; // 10元
        defaultRule.firstFeeDisplay = 10;
        defaultRule.continueItem = 1;
        defaultRule.continueItemDisplay = 1;
        defaultRule.continueFee = 5; // 5元
        defaultRule.continueFeeDisplay = 5;
        defaultRule.isDefault = 1;
      }

      // 刷新表单数据
      syncFreightRulesToForm();
    } else {
      // 如果是包邮，清空所有规则
      freightRules.splice(0, freightRules.length);
      form.rules = [];
    }
  } catch (error) {
    console.error("获取运费模板详情失败:", error);
    ElMessage.error("获取运费模板详情失败");
  }
}

// 处理规则数据的显示值转换（后端存储单位与前端显示单位不同）
function prepareRuleDisplayValues(rule: FreightRuleVO): FreightRuleVO & {
  regionCodesArray: string[];
  firstItemDisplay: number;
  firstFeeDisplay: number;
  continueItemDisplay: number;
  continueFeeDisplay: number;
} {
  try {
    const processedRule = { ...rule } as any;

    // 处理区域代码数组
    processedRule.regionCodesArray = rule.regionCodes
      ? rule.regionCodes.split(",").filter(Boolean)
      : [];

    // 按件计费直接显示件数
    processedRule.firstItemDisplay = rule.firstItem;
    processedRule.continueItemDisplay = rule.continueItem;

    // 金额处理（分转元）
    processedRule.firstFeeDisplay = rule.firstFee;
    processedRule.continueFeeDisplay = rule.continueFee;

    return processedRule;
  } catch (error) {
    console.error("处理规则显示值出错:", error);
    // 返回原始规则作为兼底，添加缺失的显示属性
    return {
      ...rule,
      regionCodesArray: rule.regionCodes ? rule.regionCodes.split(",") : [],
      firstItemDisplay: rule.firstItem,
      firstFeeDisplay: rule.firstFee ,
      continueItemDisplay: rule.continueItem,
      continueFeeDisplay: rule.continueFee ,
    };
  }
}

// 将freightRules同步到form.rules
function syncFreightRulesToForm() {
  try {
    console.log("同步运费规则到表单");

    // 先处理普通规则
    const normalRules = freightRules
      .filter((rule) => !rule.isDefault)
      .map((rule) => {
        // 过滤展示属性
        const {
          regionCodesArray,
          firstItemDisplay,
          firstFeeDisplay,
          continueItemDisplay,
          continueFeeDisplay,
          ...rest
        } = rule;

        return {
          ...rest,
          isDefault: 0,
        };
      });

    // 添加默认规则
    const defaultRuleData = {
      id: defaultRule.id,
      regionCodes: defaultRule.regionCodes,
      regionNames: defaultRule.regionNames,
      firstItem: defaultRule.firstItem,
      firstFee: defaultRule.firstFee,
      continueItem: defaultRule.continueItem,
      continueFee: defaultRule.continueFee,
      isDefault: 1,
    };

    // 合并所有规则并保存到表单
    form.rules = [...normalRules, defaultRuleData];
    console.log("同步后的表单规则:", form.rules);
  } catch (error) {
    console.error("同步规则到表单失败:", error);
  }
}

// 获取可用于当前规则的区域选项
function getAvailableRegions(currentIndex: number) {
  try {
    // 如果区域数据不可用，返回空数组
    if (
      !regionOptions.value ||
      !Array.isArray(regionOptions.value) ||
      regionOptions.value.length === 0
    ) {
      return [];
    }

    // 收集其他规则已使用的区域代码
    const usedRegionCodes = new Set<string>();

    freightRules.forEach((rule, index) => {
      // 跳过当前规则
      if (
        index !== currentIndex &&
        rule.regionCodesArray &&
        Array.isArray(rule.regionCodesArray)
      ) {
        rule.regionCodesArray.forEach(
          (code: string) => code && usedRegionCodes.add(code)
        );
      }
    });

    // 过滤出未被其他规则使用的区域
    return regionOptions.value.filter(
      (region: any) =>
        region && region.id && !usedRegionCodes.has(region.id.toString())
    );
  } catch (error) {
    console.error("获取可用区域出错:", error);
    return [];
  }
}

// 处理区域选择变化
function handleRegionChange(rule: any, index: number) {
  try {
    // 将选中的区域代码和名称转换为逗号分隔的字符串
    if (!rule.regionCodesArray || !Array.isArray(rule.regionCodesArray)) {
      rule.regionCodesArray = [];
    }

    rule.regionCodes = rule.regionCodesArray.join(",");
    rule.regionNames = rule.regionCodesArray
      .map((code: string) => {
        const region = regionOptions.value.find(
          (r: any) => r.id && r.id.toString() === code
        );
        return region ? region.name : code;
      })
      .join(",");
    console.log("区域选择变化:", rule.regionCodes, rule.regionNames);
  } catch (error) {
    console.error("处理区域选择变化出错:", error);
  }
}

// 处理金额变化 - 转换单位为分
function handleFeeChange(val: number, rule: any, type: string) {
  try {
    if (type === "first") {
      rule.firstFee = val; // 转换为分位单位
    } else {
      rule.continueFee = val;
    }
  } catch (error) {
    console.error("处理金额变化出错:", error);
  }
}

// 删除规则
function removeRule(index: number) {
  try {
    freightRules.splice(index, 1);
    // 如果删除后没有规则，添加一个默认规则
    if (freightRules.length === 0 && form.isFreeShipping === 0) {
      addRule();
    } else {
      // 同步到表单
      syncFreightRulesToForm();
    }
    console.log("删除规则后的运费规则:", freightRules);
  } catch (error) {
    console.error("删除规则出错:", error);
  }
}

// 表单提交
function handleSubmit() {
  const feeValid = freightRules.every(
    (rule) =>
      rule.firstFee >= 0 &&
      rule.continueFee >= 0 &&
      !isNaN(rule.firstFee) &&
      !isNaN(rule.continueFee)
  );
  if (!feeValid) {
    ElMessage.error("运费金额必须为有效数值");
    return;
  }
  if (mode.value === "view") return;

  // 先同步运费规则到表单
  if (form.isFreeShipping === 0) {
    syncFreightRulesToForm();
  }

  // 检查运费规则有效性
  if (form.isFreeShipping === 0) {
    // 验证规则中的区域选择
    const invalidRules = freightRules.filter((rule) => {
      return (
        !rule.regionCodes ||
        !rule.regionNames ||
        !rule.regionCodesArray ||
        rule.regionCodesArray.length === 0
      );
    });

    if (invalidRules.length > 0) {
      ElMessage.error("请为所有规则选择区域");
      return;
    }
  }

  formRef.value.validate(async (valid: boolean) => {
    if (!valid) return;
    try {
      const formData = { ...form, countryId: selectedCountryId.value };

      // 确保包邮时无运费规则
      if (formData.isFreeShipping === 1) {
        formData.rules = [];
      } else {
        // 检查所有规则是否都有区域选择
        const hasInvalidRule = formData.rules.some((rule) => {
          return !rule.regionCodes || !rule.regionNames;
        });

        if (hasInvalidRule) {
          ElMessage.error("请确保所有规则均有选择区域");
          return;
        }
      }

      // 提交数据前先进行最后检查，确保所有数据有效
      console.log("提交的表单数据:", formData);

      // 确保运费规则中不存在NaN值
      if (formData.rules && formData.rules.length > 0) {
        formData.rules = formData.rules.map((rule) => ({
          ...rule,
          firstItem: rule.firstItem || 1,
          firstFee: !isNaN(rule.firstFee) ? rule.firstFee : 0,
          continueItem: rule.continueItem || 1,
          continueFee: !isNaN(rule.continueFee) ? rule.continueFee : 0,
          isDefault: rule.isDefault === 1 ? 1 : 0,
        }));
      }

      if (mode.value === "edit") {
        // 确保ID是有效数字
        const templateId = id.value ? parseInt(id.value) : 0;
        if (isNaN(templateId) || templateId <= 0) {
          throw new Error("模板ID无效");
        }
        console.log("更新模板，ID:", templateId);
        await updateFreightTemplate(templateId, formData);
        ElMessage.success("修改成功");
      } else {
        console.log("创建新模板");
        await createFreightTemplate(formData);
        ElMessage.success("创建成功");
      }
      router.back();
    } catch (error) {
      console.error("保存失败:", error);
      ElMessage.error("保存失败");
    }
  });
}

// 取消操作
function handleCancel() {
  router.back();
}
</script>

<style scoped>
.freight-template-detail {
  padding: 20px;
}

.freight-rules-container {
  width: 100%;
}

.freight-rule-item {
  margin-bottom: 20px;
}

.freight-rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
}

.add-rule-btn {
  margin-top: 10px;
  text-align: center;
}

/* 默认规则样式 */
.default-rule {
  border: 1px dashed #409eff;
  padding: 5px;
  background-color: #ecf5ff;
  border-radius: 4px;
}

.default-rule .freight-rule-header span {
  color: #409eff;
}
</style>
