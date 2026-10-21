<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue';
import { ElForm } from 'element-plus';
import { getFreightTemplateList, getSupplierDefaultTemplate, getSupplierTemplates } from "@/api/pms/freight-template/index";
import { getForbiddenAreaList, getSupplierForbiddenAreas } from "@/api/pms/forbidden-area/index";

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  showButtons: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['prev', 'next', 'update:modelValue']);
const dataFormRef = ref(ElForm);

const goodsInfo: any = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value);
  }
});

const state = reactive({
  freightTemplates: [] as Array<any>,
  forbiddenAreas: [] as Array<any>,
  loading: {
    freight: false,
    area: false
  }
});

// 加载运费模板列表
async function loadFreightTemplates() {
  state.loading.freight = true;
  try {
    // 如果有supplierId，直接从后端获取该供应商的运费模板
    if (goodsInfo.value.supplierId) {
      // 获取供应商默认模板
      try {
        const { data: defaultTemplate } = await getSupplierDefaultTemplate(goodsInfo.value.supplierId);
        if (defaultTemplate) {
          console.log('供应商默认运费模板:', defaultTemplate);
          // 自动选择默认模板
          goodsInfo.value.freightTemplateId = defaultTemplate.id;
        }
      } catch (err) {
        console.error("获取供应商默认模板失败", err);
      }
      
      // 直接从后端获取指定供应商的运费模板
      const { data } = await getSupplierTemplates(goodsInfo.value.supplierId);
      console.log('供应商运费模板数据:', data);
      state.freightTemplates = data || [];
    } else {
      console.log('商品没有关联供应商，加载所有运费模板');
      const { data } = await getFreightTemplateList();
      state.freightTemplates = data || [];
    }
  } catch (error) {
    console.error("加载运费模板失败", error);
  } finally {
    state.loading.freight = false;
  }
}

// 加载禁售区域列表
async function loadForbiddenAreas() {
  state.loading.area = true;
  try {
    // 如果有supplierId，直接从后端获取该供应商的禁售区域
    if (goodsInfo.value.supplierId) {
      const { data } = await getSupplierForbiddenAreas(goodsInfo.value.supplierId);
      console.log('供应商禁售区域数据:', data);
      state.forbiddenAreas = data || [];
    } else {
      console.log('商品没有关联供应商，加载所有禁售区域');
      const { data } = await getForbiddenAreaList();
      state.forbiddenAreas = data || [];
    }
  } catch (error) {
    console.error("加载禁售区域失败", error);
  } finally {
    state.loading.area = false;
  }
}

function handlePrev() {
  emit("prev");
}

function handleNext() {
  dataFormRef.value.validate((valid: boolean) => {
    if (valid) {
      emit("next");
    }
  });
}

// 监听商品信息中supplierId的变化，重新加载数据
watch(() => goodsInfo.value.supplierId, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal) {
    loadFreightTemplates();
    loadForbiddenAreas();
  }
});

onMounted(() => {
  loadFreightTemplates();
  loadForbiddenAreas();
});
</script>

<template>
  <div class="component-container">
    <div class="component-container__main">
      <el-form
        ref="dataFormRef"
        :model="goodsInfo"
        label-width="120px"
      >
        <!-- 运费模板选择 -->
        <el-form-item label="运费模板">
          <el-select 
            v-model="goodsInfo.freightTemplateId" 
            placeholder="请选择运费模板"
            style="width: 400px"
            clearable
            :loading="state.loading.freight"
          >
            <el-option
              v-for="item in state.freightTemplates"
              :key="item.id"
              :label="item.templateName"
              :value="item.id"
            >
              <div class="template-option">
                <span>{{ item.templateName }}</span>
                <span v-if="item.isDefault === 1" class="default-tag">默认</span>
              </div>
            </el-option>
          </el-select>

        </el-form-item>

        <!-- 禁售区域选择 -->
        <el-form-item label="禁售区域">
          <el-select 
            v-model="goodsInfo.forbiddenAreaId" 
            placeholder="请选择禁售区域"
            style="width: 400px"
            clearable
            :loading="state.loading.area"
          >
            <el-option
              v-for="item in state.forbiddenAreas"
              :key="item.id"
              :label="item.regionNames"
              :value="item.id"
            >
              <span>{{ item.regionNames }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <!-- 是否需要发货 - 已隐藏 -->
        <!-- <el-form-item label="是否需要发货">
            <el-radio-group v-model="goodsInfo.needDelivery">
              <el-radio :value="true" size="large">需要</el-radio>
              <el-radio :value="false" size="large">不需要</el-radio>
            </el-radio-group>
         
        </el-form-item> -->
      </el-form>
    </div>
    <div class="component-container__footer" v-if="showButtons">
      <el-button @click="handlePrev">上一步，设置商品库存</el-button>
      <el-button type="primary" @click="handleNext"
        >完成，提交商品</el-button
      >
    </div>
  </div>
</template>

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
}

.template-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .default-tag {
    background-color: #67c23a;
    color: white;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
  }
}

.form-item-actions {
  margin-top: 8px;
  display: inline-block;
  margin-left: 10px;
}
</style>
