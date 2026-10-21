<template>
    <!-- 商品 -->
    <div class="container">
        <div class="flex-row jc-e gap-20 mb-20 align-c">
            <el-cascader v-model="category_ids" :options="category_list" :props="cascader_config" placeholder="请选择分类" :show-all-levels="false" filterable clearable @change="cascader_change" />
            <el-select v-model="spu_type" class="search-w" placeholder="商品类型" clearable @change="handle_search">
                <el-option value="NORMAL" label="普通商品" />
                <el-option value="DECLARE" label="报单商品" />
                <el-option value="CLOUD" label="云仓商品" />
            </el-select>
            <el-select v-model="brand_ids" class="search-w" placeholder="品牌" filterable clearable @change="handle_search">
                <el-option v-for="item in brand_list" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-input v-model="search_value" placeholder="请输入搜索内容" class="search-w" @change="handle_search">
                <template #suffix>
                    <icon name="search" size="16" color="9" class="c-pointer" @click="handle_search"></icon>
                </template>
            </el-input>
        </div>
        <div class="content">
            <el-table v-loading="loading" :data="tableData" class="w" :header-cell-style="{ background: '#f7f7f7' }" row-key="id" height="438" fixed @row-click="row_click" @select="handle_select" @select-all="handle_select">
                <el-table-column v-if="multiple" type="selection" width="60" />
                <el-table-column v-else label="#" width="60" type="">
                    <template #default="scope">
                        <el-radio v-model="template_selection" :label="scope.$index + ''">&nbsp;</el-radio>
                    </template>
                </el-table-column>
                <el-table-column prop="id" label="ID" width="80" type="" />
                <el-table-column prop="picUrl" label="商品">
                    <template #default="scope">
                        <div class="flex-row align-c gap-10">
                            <image-empty v-if="scope.row.picUrl" :model-value="scope.row.picUrl" class="img"></image-empty>
                            <div class="flex-1">{{ scope.row.name }}</div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="spuTypeDesc" width="100" label="商品类型" />
                <el-table-column label="零售价" width="100">
                    <template #default="scope">
                        <span>¥{{ scope.row.price || '0.00' }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="categoryName" width="100" label="分类" />
                <el-table-column prop="brandName" width="100" label="品牌" />
                <el-table-column prop="sales" width="80" label="销量" />
                <template #empty>
                    <no-data :text="empty_text"></no-data>
                </template>
            </el-table>
            <div class="mt-10 flex-row jc-e">
                <el-pagination :current-page="page" background :page-size="page_size" :pager-count="5" layout="prev, pager, next" :total="data_total" @current-change="get_list" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import commonApi from '../../../api/common';
import { commonStore } from '../../../store';
import { isEmpty } from 'lodash';
const common_store = commonStore();
const props = defineProps({
    // 重置
    reset: {
        type: Boolean,
        default: () => false,
    },
    multiple: {
        type: Boolean,
        default: () => false,
    },
    linkUrl: {
        type: String,
        default: '',
    },
    // 判断是否返回链接地址
    selectIsUrl: {
        type: Boolean,
        default: false,
    },
});

watch(
    () => props.reset,
    (val) => {
        if (val) {
            init();
        }
    }
);

onMounted(() => {
    init();
});

const modelValue = defineModel({ type: Object, default: {} });
const tableData = ref<any[]>([]);
const search_value = ref('');
const loading = ref(false);
const cascader_config = {
    // ✅ 改为单选模式，更符合商品选择的业务场景
    value: 'id',
    label: 'name',
    children: 'items',
    multiple: false,         // ← 单选
    checkStrictly: true,     // 可选择任意级别
    emitPath: false,         // 只返回ID
};

const init = () => {
    template_selection.value = '';
    category_ids.value = null;
    spu_type.value = '';
    brand_ids.value = '';
    search_value.value = '';
    category_list.value = common_store.common.goods_category || [];
    brand_list.value = common_store.common.brand_list || [];
    if (props.linkUrl) {
        get_list(1);
    }
};

const handle_search = () => {
    // 🔧 添加防抖，避免频繁请求
    if (searchTimer) {
        clearTimeout(searchTimer);
    }
    searchTimer = setTimeout(() => {
        get_list(1);
    }, 300);
};

// 🔧 递归查找分类的 level
const findCategoryLevel = (categories: any[], targetId: number): number | null => {
    for (const cat of categories) {
        if (cat.id === targetId) {
            return cat.level || null;
        }
        if (cat.items && cat.items.length > 0) {
            const level = findCategoryLevel(cat.items, targetId);
            if (level !== null) return level;
        }
    }
    return null;
};

const cascader_change = (val: any) => {
    category_ids.value = val ? Number(val) : null;
    // 🔧 查找分类级别
    category_level.value = val ? findCategoryLevel(category_list.value, Number(val)) : null;
    get_list(1);
};

const category_ids = ref<number | null>(null);
const category_level = ref<number | null>(null);  // 🔧 记录分类级别
const spu_type = ref('');
const brand_ids = ref('');
const category_list = ref<any[]>([]);
const brand_list = ref<any[]>([]);
const template_selection = ref('');

const page = ref(1);
const page_size = ref(30);
const data_total = ref(0);
const empty_text = ref('暂无数据');

// 🔧 防抖定时器
let searchTimer: any = null;

// ✅ 使用动态接口获取商品列表
const get_list = (new_page: number) => {
    let new_data = {
        page: new_page,
        keywords: search_value.value || undefined,
        brand_ids: brand_ids.value || undefined,
        page_size: page_size.value,
        category_id: category_ids.value || undefined,
        category_level: category_level.value || undefined,  // 🔧 传递分类级别
        spu_type: spu_type.value || undefined,
    };
    
    loading.value = true;
    commonApi.getDynamicApi(props.linkUrl, new_data).then((res: any) => {
        tableData.value = res.data.data_list || [];
        if (isEmpty(res.data.data_list)) {
            empty_text.value = '暂无数据';
        }
        data_total.value = res.data.data_total || 0;
        page.value = res.data.page || new_page;
        setTimeout(() => {
            loading.value = false;
        }, 300);
    }).catch((err: any) => {
        tableData.value = [];
        data_total.value = 0;
        page.value = 1;
        empty_text.value = err?.message || err || '请求失败';
        loading.value = false;
    });
};

const row_click = (row: any) => {
    if (!props.multiple) {
        const new_table_data = JSON.parse(JSON.stringify(tableData.value));
        template_selection.value = new_table_data.findIndex((item: any) => item.id == row.id).toString();
        if (props.selectIsUrl) {
            const pagePath = '/pages/product/product?id=' + row.id;  // ✅ 修正为 mall-app 实际路径
            const new_row = {
                id: row.id,
                name: row.name || pagePath,
                page: pagePath,
            };
            modelValue.value = [new_row];
        } else {
            modelValue.value = [{
                id: row.id,
                name: row.name,
                url: row.picUrl,
                title: row.name,
                price: row.price,
                originPrice: row.originPrice,
                sales: row.sales,
                simpleDesc: row.simpleDesc || row.description || '', // 🔧 添加商品简述字段
            }];
        }
    }
};

const handle_select = (selection: any) => {
    if (props.selectIsUrl) {
        const new_selection = selection.map((item: any) => {
            const pagePath = '/pages/product/product?id=' + item.id;  // ✅ 修正为 mall-app 实际路径
            return {
                id: item.id,
                name: item.name || pagePath,
                page: pagePath,
            };
        });
        modelValue.value = new_selection;
    } else {
        modelValue.value = selection.map((item: any) => ({
            id: item.id,
            name: item.name,
            url: item.picUrl,
            title: item.name,
            price: item.price,
            originPrice: item.originPrice,
            sales: item.sales,
            simpleDesc: item.simpleDesc || item.description || '', // 🔧 添加商品简述字段
        }));
    }
};
</script>
<style lang="scss" scoped>
.container {
    .search-w {
        width: 22.5rem;
    }
    .content {
        :deep(.el-table__inner-wrapper:before) {
            background-color: transparent;
        }
        .img {
            width: 3.6rem;
        }
    }
}
:deep(.el-cascader) {
    height: 3.2rem;
    .el-input {
        height: 3.2rem;
    }
    .el-cascader__tags {
        flex-wrap: nowrap !important;
    }
    .el-tag {
        max-width: 10rem;
    }
}
</style>
