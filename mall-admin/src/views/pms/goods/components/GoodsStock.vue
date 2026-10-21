<template>
  <div class="component-container">
    <div class="component-container__main">
      <el-card class="box-card">
        <template #header>
          <span>商品规格</span>
          <el-button
            type="success"
            size="small"
            class="float-right"
            @click="handleSpecAdd"
          >
            <i-ep-plus /> 添加规格项
          </el-button>
        </template>

        <el-form
          ref="specFormRef"
          :model="specForm"
          :inline="true"
          size="small"
        >
          <el-table
            ref="specTableRef"
            :data="specForm.specList"
            row-key="id"
            size="small"
          >
            <el-table-column align="center" width="50">
              <template #default>
                <svg-icon class="drag-handler" icon-class="drag" />
              </template>
            </el-table-column>
            <el-table-column label="规格名" width="200">
              <template #default="scope">
                <el-form-item
                  :prop="'specList[' + scope.$index + '].name'"
                  :rules="rules.spec.name"
                >
                  <el-input
                    v-model="scope.row.name"
                    type="text"
                    size="small"
                    @input="handleSpecChange()"
                  />
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column>
              <template #header>
                规格值
                <el-link
                  type="danger"
                  style="font-size: 12px"
                  :underline="false"
                >（默认第一条规格包含图片）</el-link
                >
              </template>

              <template #default="scope">
                <div
                  v-for="item in scope.row.values"
                  :key="item.id"
                  style="display: inline-block; margin-right: 15px"
                >
                  <el-tag
                    size="small"
                    closable
                    :type="colors[scope.$index % colors.length] as any"
                    @close="handleSpecValueRemove(scope.$index, item.id)"
                  >
                    {{ item.value }}
                  </el-tag>
                  <single-upload
                    v-if="scope.$index == 0"
                    v-model="item.picUrl"
                    style="margin-top: 5px"
                  />
                </div>

                <el-input
                  v-if="tagInputs.length > 0 && tagInputs[scope.$index].visible"
                  v-model="tagInputs[scope.$index].value"
                  style="width: 80px; vertical-align: top"
                  size="small"
                  @keyup.enter="handleSpecValueInput(scope.$index)"
                  @blur="handleSpecValueInput(scope.$index)"
                />
                <el-button
                  v-else
                  style="vertical-align: top"
                  size="small"
                  @click="handleSpecValueAdd(scope.$index)"
                >
                  <i-ep-plus /> 添加规格值
                </el-button>
              </template>
            </el-table-column>

            <el-table-column width="60" label="操作">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  circle
                  plain
                  @click.stop="handleSpecRemove(scope.$index)"
                ><i-ep-minus />
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form>
      </el-card>

      <el-card class="box-card">
        <template #header >
          <span>商品库存</span>
        </template>
        <el-form ref="skuFormRef" :model="skuForm" size="small" :inline="true">
          <el-table
            :data="skuForm.skuList"
            :span-method="objectSpanMethod as any"
            highlight-current-row
            size="small"
            border
          >
            <!-- 规格列：只有当存在规格时才显示 -->
            <el-table-column label="SKU ID" align="center" width="80">
              <template #default="scope">
                <span>{{ scope.row.id || '-' }}</span>
              </template>
            </el-table-column>

            <!-- 规格列：只有当存在规格时才显示 -->
            <el-table-column
              v-for="(title, index) in specTitles"
              :key="index"
              align="center"
              :prop="'specValue' + (index + 1)"
              :label="title"
            />

            <!-- 规格编码：对应 pms_sku.sku_sn，选填 -->
            <el-table-column label="规格编码" align="center" width="150">
              <template #default="scope">
                <el-input
                  v-model="scope.row.skuSn"
                  size="small"
                  maxlength="64"
                  placeholder="选填"
                />
              </template>
            </el-table-column>

            <!-- <el-table-column label="价格" align="center">
              <template #default="scope">
                <el-form-item
                  :prop="'skuList[' + scope.$index + '].price'"
                  :rules="rules.sku.price"
                >
                  <el-input v-model="scope.row.price" />
                </el-form-item>
              </template>
            </el-table-column> -->
            <el-table-column label="供货价" align="center" width="100">
              <template #header >
                <span>供货价<i style="color: #f56c6c">*</i></span>
                <el-input
                  v-model="setPrices.supplyPrice"
                  @change="setPrice('supplyPrice')"
                  size="small"
                  placeholder="批量设置供货价"
                />
              </template>
              <template #default="scope">
                <el-form-item
                  :prop="'skuList[' + scope.$index + '].supplyPrice'"
                  :rules="rules.sku.supplyPrice"
                >
                  <el-input v-model="scope.row.supplyPrice" class="price-input" @change="calculatePricesAndType('supplyPrice')" />
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column label="采购价" align="center" width="100">
              <template #header >
                <span>采购价</span>
                <el-input
                  v-model="setPrices.purchasePrice"
                  @change="setPrice('purchasePrice')"
                  size="small"
                  placeholder="批量设置采购价"
                />
              </template>
              <template #default="scope">
                <el-form-item
                  :prop="'skuList[' + scope.$index + '].purchasePrice'"
                >
                  <el-input v-model="scope.row.purchasePrice" class="price-input" />
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column label="零售价毛利率" align="center" width="110">
              <template #default="scope">
                <span :style="{ color: (scope.row.retailPriceMargin || 0) < 5 ? '#F56C6C' : '#67C23A' }">
                  {{ scope.row.retailPriceMargin !== undefined ? scope.row.retailPriceMargin + '%' : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="零售价" align="center" width="100">
              <template #header >
                <span>零售价<i style="color: #f56c6c">*</i></span>
                <el-input
                  v-model="setPrices.retailPrice"
                  @change="setPrice('retailPrice')"
                  size="small"
                  placeholder="批量设置零售价"
                />
              </template>
              <template #default="scope">
                <el-form-item
                  :prop="'skuList[' + scope.$index + '].retailPrice'"
                  :rules="rules.sku.retailPrice"
                >
                  <el-input v-model="scope.row.retailPrice" class="price-input" @change="calculatePricesAndType('retailPrice')" />
                </el-form-item>
              </template>
            </el-table-column>

            <el-table-column label="划线价" align="center" width="100">
              <template #header >
                <span>划线价<i style="color: #f56c6c">*</i></span>
                <el-input
                  v-model="setPrices.marketPrice"
                  @change="setPrice('marketPrice')"
                  size="small"
                  placeholder="批量设置划线价"
                />
              </template>
              <template #default="scope">
                <el-form-item
                  :prop="'skuList[' + scope.$index + '].marketPrice'"
                  :rules="rules.sku.marketPrice"
                >
                  <el-input v-model="scope.row.marketPrice" class="price-input" />
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column label="库存" align="center" width="120">
              <template #header >
                <span>库存<i style="color: #f56c6c">*</i></span>
                <el-input-number
                  v-model="setPrices.stock"
                  :min="0"
                  :precision="0"
                  :step="1"
                  :controls="false"
                  size="small"
                  style="width: 100px"
                  placeholder="批量设置库存"
                  @change="setPrice('stock')"
                />
              </template>
              <template #default="scope">
                <el-form-item
                  :prop="'skuList[' + scope.$index + '].stock'"
                  :rules="rules.sku.stock"
                >
                  <el-input-number
                    v-model="scope.row.stock"
                    :min="0"
                    :precision="0"
                    :step="1"
                    :controls="false"
                    style="width: 100px"
                  />
                </el-form-item>
              </template>
            </el-table-column>
          </el-table>
        </el-form>
      </el-card>
    </div>
    <div class="component-container__footer" v-if="showButtons">
      <el-button @click="handlePrev">上一步，设置商品属性</el-button>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getAttributeList } from "@/api/pms/attribute";
import { addSpu, updateSpu, calculateSpuPricing } from "@/api/pms/goods";

const emit = defineEmits(["prev", "next", "update:modelValue"]);
import { useRouter } from "vue-router";

const router = useRouter();

const specFormRef = ref(ElForm);
const skuFormRef = ref(ElForm);

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => {
      return {};
    },
  },
  showButtons: {
    type: Boolean,
    default: true
  }
});

const goodsInfo: any = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit("update:modelValue", value);
  },
});

const state = reactive({
  specForm: {
    specList: [] as any[],
  },
  skuForm: {
    skuList: [] as any[],
  },
  // 商品类型建议
  suggestedSpuType: '',
  suggestedSpuTypeDesc: '',
  // 规格项表格标题
  specTitles: [] as any[],
  rules: {
    spec: {
      name: [{ required: true, message: "请输入规格名称", trigger: "blur" }],
      value: [{ required: true, message: "请输入规格值", trigger: "blur" }],
    },
    sku: {
      //  price: [{ required: true, message: "请输入商品价格", trigger: "blur" }],
      supplyPrice: [{ required: true, message: "请输入商品供货价", trigger: "blur" }],
      retailPrice: [{ required: true, message: "请输入商品零售价", trigger: "blur" }],
      marketPrice: [{ required: true, message: "请输入商品划线价", trigger: "blur" }],
      stock: [{ required: true, message: "请输入商品库存", trigger: "blur" }],
    },
  },
  colors: ["", "success", "warning", "danger"],
  tagInputs: [{ value: undefined, visible: false }], // 规格值标签临时值和显隐控制
});

const { specForm, skuForm, specTitles, rules, colors, tagInputs, suggestedSpuType, suggestedSpuTypeDesc } =
  toRefs(state);

const setPrices=ref({
  supplyPrice: "",
  purchasePrice: "",
  retailPrice: "",
  marketPrice: "",
  stock: undefined,
})
const setPrice = (key:string)=>{
  const value = (setPrices.value as any)[key];
  if (value === '' || value === null || value === undefined) {
    return ElMessage.error("批量设置值不能为空");
  }

  skuForm.value.skuList.forEach((item) => {
    (item as any)[key] = value;
  });
  (setPrices.value as any)[key] = undefined

  // 批量设置供货价、零售价后，触发价格重新计算
  if (key === 'supplyPrice' || key === 'retailPrice') {
    calculatePricesAndType();
  }
}

// 计算价格和商品类型（统一调用新接口）
// 标记 spuType 是否被用户手动改过，改过后不再自动覆盖
let spuTypeDirty = false;

const calculatePricesAndType = async (triggerField?: string) => {
  if (!skuForm.value.skuList || skuForm.value.skuList.length === 0) return;

  try {
    const res = await calculateSpuPricing({
      spuId: goodsInfo.value.id,
      spuType: spuTypeDirty ? goodsInfo.value.spuType : undefined,
      skuList: skuForm.value.skuList.map(sku => ({
        id: sku.id,
        retailPrice: Number(sku.retailPrice),
        supplyPrice: Number(sku.supplyPrice),
      })),
    });

    if (res.data) {
      res.data.skuList.forEach((calculatedSku: any, index: number) => {
        if (skuForm.value.skuList[index]) {
          skuForm.value.skuList[index].retailPriceMargin = calculatedSku.retailPriceMargin;
        }
      });

      // 仅当未被手动改过时才自动覆盖类型
      if (!spuTypeDirty && res.data.suggestedSpuType && goodsInfo.value.spuType !== res.data.suggestedSpuType) {
        goodsInfo.value.spuType = res.data.suggestedSpuType;
      }
    }
  } catch (error) {
    console.error('计算定价信息失败:', error);
  }
};

// 手动切换商品类型时触发
const recalculateForTypeChange = () => {
  spuTypeDirty = true;
  calculatePricesAndType('spuType');
};

watch(
  () => goodsInfo.value.categoryId,
  (newVal) => {
    // 商品编辑不加载分类下的规格
    const goodsId = goodsInfo.value.id;
    if (goodsId) {
      return false;
    }
    if (newVal) {
      // type=1 商品分类下的规格
      getAttributeList({ categoryId: newVal, type: 1 }).then((response) => {
        const specList = response.data;
        if (specList && specList.length > 0) {
          specList.forEach((item: any) => {
            state.specForm.specList.push({
              name: item.name,
              values: [],
            });
          });
          loadData();
        }
        
        // 无论分类是否有规格模板，都为新增商品创建默认单规格
        // 这样用户可以直接保存，也可以删除默认规格添加自定义规格
        if (!goodsInfo.value.id) {
          initDefaultSpec();
        }
      });
    }
  },
  {
    immediate: true,
    deep: true,
  }
);

watch(state.specForm.specList, () => {
  generateSkuList();
});

/**
 * 初始化默认规格（用于新增商品时没有规格模板的情况）
 */
function initDefaultSpec() {
  console.log('初始化默认规格，用于新增单规格商品');
  
  // 检查是否已经有规格数据（避免重复初始化）
  if (state.specForm.specList.length > 0) {
    return;
  }
  
  // 创建默认规格
  state.specForm.specList.push({
    name: "默认规格",
    values: [
      {
        id: "tid_1_1", // 使用正确的临时ID前缀
        value: "默认",
        picUrl: null
      }
    ],
  });
  
  // 添加规格值输入控制
  state.tagInputs.push({ value: undefined, visible: false });
  
  // 生成SKU列表
  generateSkuList();
}

function loadData() {
  // 处理规格列表数据
  if (goodsInfo.value.specList && goodsInfo.value.specList.length > 0) {
    // 有规格数据，正常处理
    goodsInfo.value.specList.forEach((specItem: any) => {
      const specIndex = state.specForm.specList.findIndex(
        (item: any) => item.name == specItem.name
      );
      if (specIndex > -1) {
        (state.specForm.specList[specIndex] as any).values.push({
          id: specItem.id,
          value: specItem.value,
          picUrl: specItem.picUrl,
        });
      } else {
        state.specForm.specList.push({
          name: specItem.name,
          values: [
            { id: specItem.id, value: specItem.value, picUrl: specItem.picUrl },
          ],
        });
      }
    });
  } else if (goodsInfo.value.skuList && goodsInfo.value.skuList.length > 0) {
    // 没有规格但有SKU，创建默认规格
    console.log('创建默认规格，因为商品没有规格信息');
    state.specForm.specList.push({
      name: "默认规格",
      values: [
        {
          id: "tid_1_1", // 使用正确的临时ID前缀
          value: "默认",
          picUrl: null
        }
      ],
    });
  }

  // 每个规格项追加一个添加规格值按钮
  for (let i = 0; i < state.specForm.specList.length; i++) {
    state.tagInputs.push({ value: undefined, visible: false });
  }

  generateSkuList();

  handleSpecChange();

  handleSpecReorder();
}

/**
 * 生成SKU列表的title
 */
function handleSpecChange() {
  const specList = JSON.parse(JSON.stringify(state.specForm.specList));
  state.specTitles = specList.map((item: any) => item.name);
}

/**
 * 规格列表重排序
 */
function handleSpecReorder() {
  state.specForm.specList.forEach((item, index) => {
    item.index = index;
  });
}

/**
 *  根据商品规格笛卡尔积生成SKU列表
 *
 * 规格列表：
 * [
 *    { 'id':1,'name':'颜色','values':[{id:1,value:'白色'},{id:2,value:'黑色'},{id:3,value:'蓝色'}] },
 *    { 'id':2,'name':'版本','values':[{id:1,value:'6+128G'},{id:2,value:'8+128G'},{id:3,value:'8G+256G'}] }
 * ]
 */
function generateSkuList() {
  console.log('Before generating SKU list:', state.specForm.specList);
  // 如果规格为空，生成SKU列表为空
  if (state.specForm.specList.length == 0) {
    state.skuForm.skuList = [];
    return;
  }

  const specList = JSON.parse(
    JSON.stringify(
      state.specForm.specList.filter(
        (item) => item.values && item.values.length > 0
      )
    )
  ); // 深拷贝，取有属性的规格项，否则笛卡尔积运算得到的SKU列表值为空

  const skuList = specList.reduce(
    (acc: any, curr: any) => {
      let result = [] as any[];
      acc.forEach((item: any) => {
        // curr => { 'id':1,'name':'颜色','values':[{id:1,value:'白色'},{id:2,value:'黑色'},{id:3,value:'蓝色'}] }
        curr.values.forEach((v: any) => {
          // v=>{id:1,value:'白色'}
          let temp = Object.assign({}, item);
          temp.specValues += v.value + "_"; // 规格值拼接
          temp.specIds += v.id + "|"; // 规格ID拼接
          result.push(temp);
        });
      });
      return result;
    },
    [{ specValues: "", specIds: "" }]
  );

  // 按索引位置匹配已有SKU数据
  skuList.forEach((item: any, index: number) => {
    item.specIds = item.specIds.substring(0, item.specIds.length - 1);
    item.name = item.specValues
      .substring(0, item.specValues.length - 1)
      .replaceAll("_", " ");

    // 清空规格ID，规格变化时需要重新匹配
    item.id = undefined;
    item.price = undefined;
    item.supplyPrice = undefined;
    item.purchasePrice = undefined;
    item.retailPrice = undefined;
    item.marketPrice = undefined;
    item.stock = undefined;
    item.skuSn = undefined;

    // 根据索引获取对应的SKU数据（仅在编辑模式下，后端返回了skuList时）
    if (goodsInfo.value.skuList && goodsInfo.value.skuList.length > 0 && goodsInfo.value.skuList[index]) {
      const sku = goodsInfo.value.skuList[index];
      item.id = sku.id;
      item.price = sku.price;
      item.supplyPrice = sku.supplyPrice;
      item.purchasePrice = sku.purchasePrice;
      item.retailPrice = sku.retailPrice;
      item.marketPrice = sku.marketPrice;
      item.stock = sku.stock;
      item.skuSn = sku.skuSn;
      item.retailPriceMargin = sku.retailPriceMargin;
    }
    const specValueArr = item.specValues
      .substring(0, item.specValues.length - 1)
      .split("_"); // ['黑','6+128G','官方标配']
    specValueArr.forEach((v: any, i: any) => {
      const key = "specValue" + (i + 1);
      item[key] = v;
      if (i == 0 && state.specForm.specList.length > 0) {
        const valueIndex = state.specForm.specList[0].values.findIndex(
          (specValue: any) => specValue.value == v
        );
        if (valueIndex > -1) {
          item.picUrl = state.specForm.specList[0].values[valueIndex].picUrl;
        }
      }
    });
  });
  state.skuForm.skuList = JSON.parse(JSON.stringify(skuList));
}

/**
 * 添加规格
 */
function handleSpecAdd() {
  if (state.specForm.specList.length >= 3) {
    ElMessage.warning("最多支持3组规格");
    return;
  }
  state.specForm.specList.push({ values: [] });
  state.tagInputs.push({ value: undefined, visible: false });
  handleSpecReorder();
}

/**
 * 删除规格
 */
function handleSpecRemove(index: any) {
  state.specForm.specList.splice(index, 1);
  state.tagInputs.splice(index, 1);
  generateSkuList();
  handleSpecReorder();
  handleSpecChange();
}

/**
 * 添加规格值
 */
function handleSpecValueAdd(specIndex: any) {
  state.tagInputs[specIndex].visible = true;
}

/**
 * 删除规格值
 */
function handleSpecValueRemove(rowIndex: any, specValueId: any) {
  const specList = JSON.parse(JSON.stringify(state.specForm.specList));
  const removeIndex = specList[rowIndex].values
    .map((item: any) => item.id)
    .indexOf(specValueId);
  specList[rowIndex].values.splice(removeIndex, 1);
  state.specForm.specList = specList;
  generateSkuList();
  handleSpecChange();
  handleSpecReorder();
}

/**
 * 规格值输入
 */
function handleSpecValueInput(rowIndex: any) {
  const currSpecValue = state.tagInputs[rowIndex].value;
  const specValues = state.specForm.specList[rowIndex].values;
  if (
    specValues &&
    specValues.length > 0 &&
    specValues.map((item: any) => item.value).includes(currSpecValue)
  ) {
    ElMessage.warning("规格值重复，请重新输入");
    return false;
  }
  if (currSpecValue) {
    if (specValues && specValues.length > 0) {
      // 临时规格值ID tid_1_1
      let maxSpecValueIndex = specValues
        .filter((item: any) => item.id && typeof item.id === 'string' && item.id.includes("tid_"))
        .map((item: any) => item.id.split("_")[2])
        .reduce((acc: any, curr: any) => {
          return acc > curr ? acc : curr;
        }, 0);
      state.specForm.specList[rowIndex].values[specValues.length] = {
        value: currSpecValue,
        id: "tid_" + (rowIndex + 1) + "_" + ++maxSpecValueIndex,
      };
    } else {
      state.specForm.specList[rowIndex].values = [
        { value: currSpecValue, id: "tid_" + (rowIndex + 1) + "_1" },
      ];
    }
  }
  state.tagInputs[rowIndex].value = undefined;
  state.tagInputs[rowIndex].visible = false;
  generateSkuList();
}

/**
 * 合并规格单元格
 *
 * @param cellObj 单元格对象
 */

const objectSpanMethod = ({ rowIndex, columnIndex }: any) => {
  // 第0列是 SKU ID，不合并，每行独立显示
  if (columnIndex === 0) {
    return [1, 1];
  }
  // 规格列从第1列开始，合并逻辑对应规格1、规格2、规格3
  let mergeRows = [1, 1, 1];
  const specLen = state.specForm.specList.filter(
    (item) => item.values && item.values.length > 0
  ).length;
  if (specLen == 2) {
    const values_len_2 = state.specForm.specList[1].values
      ? state.specForm.specList[1].values.length
      : 1;
    mergeRows = [values_len_2, 1, 1];
  } else if (specLen == 3) {
    const values_len_2 = state.specForm.specList[1].values
      ? state.specForm.specList[1].values.length
      : 1;
    const values_len_3 = state.specForm.specList[2].values
      ? state.specForm.specList[2].values.length
      : 1;
    mergeRows = [values_len_2 * values_len_3, values_len_3, 1];
  }
  // 规格列：columnIndex 1=规格1, 2=规格2, 3=规格3
  if (columnIndex === 1) {
    if (rowIndex % mergeRows[0] === 0) {
      return [mergeRows[0], 1];
    }
    return [0, 0];
  }
  if (columnIndex === 2) {
    if (rowIndex % mergeRows[1] === 0) {
      return [mergeRows[1], 1];
    }
    return [0, 0];
  }
  if (columnIndex === 3) {
    if (rowIndex % mergeRows[2] === 0) {
      return [mergeRows[2], 1];
    }
    return [0, 0];
  }
};

/**
 * 商品表单提交
 * @returns 处理后的商品数据或false
 */
function submitForm() {
  // 判断商品SKU列表是否为空
  if (!state.skuForm.skuList || state.skuForm.skuList.length === 0) {
    ElMessage.warning("未添加商品库存");
    return false;
  }

     // 校验运费模板是否选择
  if (!goodsInfo.value.freightTemplateId) {
    ElMessage.warning("请选择运费模板");
    return false;
  }

  // 异步验证改成同步验证
  if (!specFormRef.value.validate()) {
    return false;
  }

  // 先检查必填字段是否为空，确保即使没有触发 blur 也能检测到空值
  const emptyFields: string[] = [];
  state.skuForm.skuList.forEach((sku: any, index: number) => {
    if (sku.supplyPrice === "" || sku.supplyPrice === null || sku.supplyPrice === undefined) emptyFields.push(`第${index + 1}行：供货价`);
    if (sku.retailPrice === "" || sku.retailPrice === null || sku.retailPrice === undefined) emptyFields.push(`第${index + 1}行：零售价`);
    if (sku.marketPrice === "" || sku.marketPrice === null || sku.marketPrice === undefined) emptyFields.push(`第${index + 1}行：划线价`);
    if (sku.stock === "" || sku.stock === null || sku.stock === undefined) emptyFields.push(`第${index + 1}行：库存`);
  });

  if (emptyFields.length > 0) {
    ElMessage.warning("请填写完整的商品规格信息：" + emptyFields.join("、"));
    return false;
  }

  if (!skuFormRef.value.validate()) {
    return false;
  }

  // 重组商品的规格和SKU列表
  let submitsData = Object.assign({}, goodsInfo.value);
  delete submitsData.specList;
  delete submitsData.skuList;

  let specList = [] as any[];
  state.specForm.specList.forEach((item) => {
    item.values.forEach((value: any) => {
      value.name = item.name;
    });
    specList = specList.concat(item.values);
  });
  submitsData.specList = specList; // 规格列表
  // 处理SKU列表价格字段（元转分）
  let skuList = JSON.parse(JSON.stringify(state.skuForm.skuList));
  skuList = skuList.map((item: any) => {
    return item;
  });

  submitsData.skuList = skuList;
  
  // 确保 subPicUrls 是字符串数组
  if (submitsData.subPicUrls && Array.isArray(submitsData.subPicUrls)) {
    submitsData.subPicUrls = submitsData.subPicUrls.map(item => {
      if (typeof item === 'string') {
        return item;
      }
      return item.url || '';
    }).filter(url => url); // 过滤空值
  }
  
  console.log('处理后的商品数据:', submitsData);
  console.log('subPicUrls 类型:', Array.isArray(submitsData.subPicUrls) ? 'Array' : typeof submitsData.subPicUrls);
  console.log('subPicUrls 内容:', submitsData.subPicUrls);
  return submitsData;
}

function handlePrev() {
  emit("prev");
}

onMounted(() => {
  loadData();
});

// 暴露方法给父组件使用
defineExpose({
  submitForm,
  recalculateForTypeChange,
});
</script>

<style lang="scss" scoped>
.component-container {
  &__main {
    margin: 20px auto;
  }

  &__footer {
    position: fixed;
    right: 20px;
    bottom: 20px;
  }

  .box-card {
    margin-bottom: 20px;
  }
}

.el-form--inline .el-form-item {
  margin-top: 18px;
}

// 价格输入框宽度
.price-input {
  width: 90px;
}

// 毛利率显示颜色
.margin-positive {
  color: #67C23A;
}

.margin-negative {
  color: #F56C6C;
}
</style>
