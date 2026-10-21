<template>
    <div class="w">
        <el-form :model="form" label-width="auto">
            <card-container class="card-container">
                <div class="mb-12">直播设置</div>
                <el-form-item label="展示样式">
                    <el-select v-model="form.theme" style="width: 220px">
                        <el-option v-for="t in theme_list" :key="t.id" :label="t.name" :value="t.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="关键词">
                    <el-input v-model="form.keywords" placeholder="按标题/主播筛选直播（留空显示全部）" clearable />
                </el-form-item>
                <el-form-item label="状态筛选">
                    <el-checkbox-group v-model="form.live_status">
                        <el-checkbox label="1">直播中</el-checkbox>
                        <el-checkbox label="2">离开</el-checkbox>
                        <el-checkbox label="0">未开播</el-checkbox>
                    </el-checkbox-group>
                </el-form-item>
                <el-form-item label="显示条数">
                    <slider v-model="form.number" :min="1" :max="20" />
                </el-form-item>
                <el-form-item label="显示字段">
                    <el-checkbox-group v-model="form.field_show">
                        <el-checkbox label="0">标题</el-checkbox>
                        <el-checkbox label="1">主播</el-checkbox>
                        <el-checkbox label="2">观看数</el-checkbox>
                        <el-checkbox label="3">直播状态</el-checkbox>
                    </el-checkbox-group>
                </el-form-item>
                <!-- 左右滑动展示专属（对齐 app：carousel_col / name_float） -->
                <template v-if="form.theme == '4'">
                    <el-form-item label="轮播列数">
                        <slider v-model="form.carousel_col" :min="1" :max="5" />
                    </el-form-item>
                    <el-form-item label="标题浮起">
                        <el-switch
                            v-model="form.name_float"
                            active-value="1"
                            inactive-value="0"
                            @change="name_float_change"
                        />
                    </el-form-item>
                </template>
            </card-container>
        </el-form>
    </div>
</template>
<script setup lang="ts">
/**
 * @description: 直播列表组件内容设置
 */
import { onMounted, reactive } from 'vue';

const props = defineProps({
    value: {
        type: Object,
        default: () => ({}),
    },
    defaultConfig: {
        type: Object,
        default: () => ({}),
    },
});

const form = reactive(props.value);

const theme_list = [
    { id: '0', name: '单列展示' },
    { id: '1', name: '两列展示（纵向）' },
    { id: '2', name: '大图展示' },
    { id: '3', name: '无图模式' },
    { id: '4', name: '左右滑动展示' },
];

/**
 * 标题浮起切换：对齐 app 的显示效果 —— 浮起时标题叠加在封面上，需用浅色字
 */
const name_float_change = (val: string | number | boolean) => {
    if (val == '1') {
        form.name_color = '#fff';
        form.name_weight = '400';
    } else {
        form.name_color = '#333';
        form.name_weight = 'bold';
    }
};

/**
 * 历史数据兼容：早期版本显示字段存在 product_show_list，渲染器（含 app 端）
 * 统一消费 field_show。打开编辑器时回填并清理旧字段，保存后即转为新契约；
 * 未重新保存的页面由 app 侧 resolve_live_field_show 兼容读取。
 */
onMounted(() => {
    if (!Array.isArray(form.field_show)) {
        form.field_show = Array.isArray(form.product_show_list) && form.product_show_list.length > 0
            ? form.product_show_list.slice()
            : ['0', '1', '2', '3'];
    }
    delete form.product_show_list;
});
</script>
