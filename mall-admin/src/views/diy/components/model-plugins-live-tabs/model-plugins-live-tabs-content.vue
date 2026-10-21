<template>
    <div class="w">
        <el-form :model="form" label-width="auto">
            <card-container class="card-container">
                <div class="mb-12">选项卡设置</div>
                <el-form-item label="对齐方式">
                    <el-radio-group v-model="form.justification">
                        <el-radio value="left">居左</el-radio>
                        <el-radio value="center">居中</el-radio>
                        <el-radio value="right">居右</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="选项卡风格">
                    <el-radio-group v-model="form.tabs_theme">
                        <el-radio v-for="item in tabs_theme_list" :key="item.value" :value="item.value">{{ item.name }}</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item v-if="form.tabs_theme == '3'" label="选中图标">
                    <upload
                        v-model="form.tabs_adorn_img"
                        v-model:icon-value="form.tabs_adorn_icon"
                        is-icon
                        :limit="1"
                        size="50"
                    />
                </el-form-item>
                <el-form-item label="直播区样式">
                    <el-select v-model="form.plugins_live_theme" style="width: 220px">
                        <el-option v-for="t in theme_list" :key="t.id" :label="t.name" :value="t.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="显示字段">
                    <el-checkbox-group v-model="form.field_show">
                        <el-checkbox label="0">标题</el-checkbox>
                        <el-checkbox label="1">主播</el-checkbox>
                        <el-checkbox label="2">观看数</el-checkbox>
                        <el-checkbox label="3">直播状态</el-checkbox>
                    </el-checkbox-group>
                </el-form-item>
                <!-- 左右滑动展示专属（对齐 app：标题浮起叠加在封面上） -->
                <el-form-item v-if="form.plugins_live_theme == '4'" label="标题浮起">
                    <el-switch v-model="form.name_float" active-value="1" inactive-value="0" />
                </el-form-item>
                <el-form-item label="吸顶固定">
                    <el-radio-group v-model="form.tabs_top_up">
                        <el-radio label="0">否</el-radio>
                        <el-radio label="1">是</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item v-if="form.tabs_top_up == '1'" label="安全距离">
                    <el-switch v-model="form.is_general_safe_distance" active-value="1" inactive-value="0" />
                </el-form-item>
            </card-container>

            <card-container class="card-container">
                <div class="flex-row jc-sb align-c mb-12">
                    <div>直播分类 Tabs</div>
                    <el-button type="success" size="small" @click="add_tab"><i-ep-plus />新增分类</el-button>
                </div>
                <div v-for="(tab, index) in form.tabs_list" :key="index" class="tabs-item">
                    <div class="flex-row align-c jc-sb mb-10">
                        <div class="size-14 fw">分类 {{ index + 1 }}</div>
                        <el-button type="danger" size="small" link :disabled="form.tabs_list.length <= 1" @click="remove_tab(index)">删除</el-button>
                    </div>
                    <el-form-item label="显示类型">
                        <div class="flex-col gap-10 w">
                            <el-radio-group v-model="tab.tabs_type">
                                <el-radio value="0">文本</el-radio>
                                <el-radio value="1">图片/图标</el-radio>
                            </el-radio-group>
                            <upload
                                v-if="tab.tabs_type == '1'"
                                v-model="tab.tabs_img"
                                v-model:icon-value="tab.tabs_icon"
                                is-icon
                                :limit="1"
                                size="50"
                            />
                            <el-input v-else v-model="tab.title" placeholder="请输入分类名称" clearable />
                        </div>
                    </el-form-item>
                    <el-form-item v-if="tab.tabs_type != '1'" label="分类名称">
                        <el-input v-model="tab.title" placeholder="如：全部 / 直播中" />
                    </el-form-item>
                    <el-form-item label="关键词">
                        <el-input v-model="tab.keywords" placeholder="按标题/主播筛选（留空全部）" clearable />
                    </el-form-item>
                    <el-form-item label="状态筛选">
                        <el-checkbox-group v-model="tab.live_status">
                            <el-checkbox label="1">直播中</el-checkbox>
                            <el-checkbox label="2">离开</el-checkbox>
                            <el-checkbox label="0">未开播</el-checkbox>
                        </el-checkbox-group>
                    </el-form-item>
                    <el-form-item label="显示条数">
                        <slider v-model="tab.number" :min="1" :max="20" />
                    </el-form-item>
                </div>
            </card-container>
        </el-form>
    </div>
</template>
<script setup lang="ts">
/**
 * @description: 直播选项卡组件内容设置（tabs 分类管理 + 直播区配置）
 */
import { onMounted, reactive } from 'vue';
import { get_math } from '../../utils';

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

// 选项卡风格（对齐 app modules/tabs-view：0-风格1 … 4-风格5）
const tabs_theme_list = [
    { value: '0', name: '风格1' },
    { value: '1', name: '风格2' },
    { value: '2', name: '风格3' },
    { value: '3', name: '风格4' },
    { value: '4', name: '风格5' },
];

const default_tab = () => ({
    id: get_math(),
    title: '新分类',
    desc: '',
    tabs_type: '0',
    tabs_img: [],
    tabs_icon: '',
    is_sliding_fixed: '0',
    keywords: '',
    live_status: [],
    number: 6,
    order_by_type: '0',
    order_by_rule: '0',
    is_recommended: '',
    data_type: '',
    data_list: [],
    data_auto_list: [],
    data_ids: '',
    is_cover: '0',
});

/**
 * 历史数据兼容：早期版本分类名存在 name、且缺 tabs 展示契约字段（渲染器读 title，
 * 缺字段会算出 NaNrpx 导致分类栏塌陷）。打开编辑器时一次性回填并清理旧字段，
 * 保存后即转为新契约；未重新保存的页面由 app 侧 normalize_live_tabs 兜底。
 */
const legacy_migrate = () => {
    if (form.tabs_theme === undefined || form.tabs_theme === null || form.tabs_theme === '') {
        form.tabs_theme = '0';
    }
    if (!Array.isArray(form.tabs_list)) {
        form.tabs_list = [];
    }
    form.tabs_list.forEach((tab: any) => {
        if ((tab.title === undefined || tab.title === null || tab.title === '') && tab.name) {
            tab.title = tab.name;
        }
        if (tab.title === undefined || tab.title === null) tab.title = '';
        if (tab.tabs_type === undefined || tab.tabs_type === null || tab.tabs_type === '') tab.tabs_type = '0';
        if (!Array.isArray(tab.tabs_img)) tab.tabs_img = [];
        if (tab.tabs_icon === undefined || tab.tabs_icon === null) tab.tabs_icon = '';
        if (tab.is_sliding_fixed === undefined || tab.is_sliding_fixed === null || tab.is_sliding_fixed === '') tab.is_sliding_fixed = '0';
        if (tab.desc === undefined || tab.desc === null) tab.desc = '';
        // 旧字段清理，避免 name/title 双份数据再次跑偏
        delete tab.name;
    });
};

onMounted(() => {
    legacy_migrate();
});

const add_tab = () => {
    if (!Array.isArray(form.tabs_list)) {
        form.tabs_list = [];
    }
    form.tabs_list.push(default_tab());
};

const remove_tab = (index: number) => {
    if (form.tabs_list.length <= 1) return;
    form.tabs_list.splice(index, 1);
    if (form.tabs_active_index >= form.tabs_list.length) {
        form.tabs_active_index = 0;
    }
};
</script>
<style scoped>
.tabs-item {
    border: 1px solid #ebeef5;
    border-radius: 6px;
    padding: 10px 12px;
    margin-bottom: 12px;
}
</style>
