<template>
    <!-- 顶部导航栏 -->
    <div class="navbar">
        <div class="nav-left">
            <!-- <icon name="arrow-left" color="f">返回</icon> -->
            <div class="flex-row align-c">
                <div class="name">
                    <div :class="[{'c-pointer': common_store_config?.diy_config_operate?.is_base_data == 1}, 'flex-row align-c gap-10']" @click="common_store_config?.diy_config_operate?.is_base_data == 1 ? dialog_visible = true : ''">
                        <image-empty v-if="modelValue.logo" :src="modelValue.logo" class="round img" error-img-style="width: 2.2rem;height: 2.2rem;" />
                        <div>{{ modelValue.name }}</div>
                        <icon v-if="common_store_config?.diy_config_operate?.is_base_data == 1" name="edit" color="#7DBEFF"></icon>
                    </div>
                </div>
            </div>
        </div>
        <div class="nav-right">
            <el-button
                v-if="common_store_config?.preview_url !== ''"
                class="btn-plain"
                :class="saveDisabled ? 'disabled' : ''"
                :disabled="saveDisabled"
                plain
                @click="preview_event"
            >
                预览
            </el-button>
            <el-button
                v-if="common_store_config?.diy_config_operate?.is_save_button == 1"
                :class="[
                    common_store_config?.diy_config_operate?.is_save_close_button == 1 ? 'btn-plain' : 'btn-white',
                    saveDisabled ? 'disabled' : '',
                ]"
                :disabled="saveDisabled"
                @click="save_event"
            >
                保存
            </el-button>
            <el-button
                v-if="common_store_config?.diy_config_operate?.is_save_close_button == 1"
                class="btn-white"
                :class="saveDisabled ? 'disabled' : ''"
                :disabled="saveDisabled"
                @click="save_close_event"
            >
                保存关闭
            </el-button>
        </div>
    </div>
    <el-dialog v-model="dialog_visible" class="radius-lg" width="650" draggable :close-on-click-modal="false" append-to-body>
        <template #header>
            <div class="title re">
                <div class="middle size-16 fw-b">基础信息</div>
            </div>
        </template>
        <div class="content pa-20">
            <el-form ref="ruleFormRef" :model="form" :rules="rules" label-width="50" status-icon @submit.prevent>
                <el-form-item label="封面">
                    <upload v-model="form.logo" :limit="1"></upload>
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入名称" clearable @keyup.enter="confirm_event(ruleFormRef)" />
                </el-form-item>
                <el-form-item label="描述">
                    <el-input v-model="form.describe" placeholder="请输入描述" :rows="4" type="textarea" clearable @keyup.enter="confirm_event(ruleFormRef)" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-switch v-model="form.is_enable" active-value="1" inactive-value="0"></el-switch>
                </el-form-item>
            </el-form>
        </div>
        <template #footer>
            <span class="dialog-footer">
                <el-button class="plr-28 ptb-10" @click="dialog_visible = false">取消</el-button>
                <el-button class="plr-28 ptb-10" type="primary" @click="confirm_event(ruleFormRef)">确定</el-button>
            </span>
        </template>
    </el-dialog>
</template>
<script setup lang="ts">
import { FormInstance, FormRules } from 'element-plus';
import { commonStore } from '../../../store';
import Upload from '../../../components/common/upload/index.vue';

const common_store = commonStore();
const props = defineProps({
    saveDisabled: {
        type: Boolean,
        default: false,
    },
});
const modelValue = defineModel({ type: Object, default: {} });
// 公共配置项
const common_store_config = computed(() => common_store.common.config);
// #region 变量 --------------------start
const dialog_visible = ref(false);
watch(dialog_visible, (val) => {
    if (val) {
        form.value = {
            logo: modelValue.value.logo.length > 0 ? [{ id: '', url: modelValue.value.logo, original: '', ext: '', size: 0, type: 'img' }] : [],
            name: modelValue.value.name,
            describe: modelValue.value.describe,
            is_enable: modelValue.value.is_enable.toString(),
        };
    }
});
const form = ref({
    logo: [] as any[],
    name: '',
    describe: '',
    is_enable: '',
});
const ruleFormRef = ref<FormInstance>();
const rules = reactive<FormRules>({
    name: [
        { required: true, trigger: 'blur', message: '请输入模版名称' },
        { min: 1, max: 60, message: '名称长度1~60个字符', trigger: 'blur' },
    ],
});
// #endregion 变量 --------------------end

const emit = defineEmits(['preview', 'save', 'saveClose']);
//  * 点击预览时的事件处理函数。
const preview_event = () => {
    emit('preview', true);
};
// 点击仅保存时的事件处理函数。
const save_event = () => {
    emit('save', true);
};
// 点击保存并关闭时的事件处理函数。
const save_close_event = () => {
    emit('saveClose', true);
};

const confirm_event = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate((valid, fields) => {
        if (valid) {
            dialog_visible.value = false;
            modelValue.value = {
                ...form.value,
                logo: form.value.logo.length > 0 ? form.value.logo[0].url : '',
            };
        }
        //  else {
        //     console.log('error submit!', fields);
        // }
    });
};
</script>
<style lang="scss" scoped>
@use '../../../styles/variables' as *;
.navbar {
    height: 6rem;
    padding: 0 3rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: $cr-primary;
    .nav-left {
        display: flex;
        align-items: center;
        .name {
            // padding-left: 2rem;
            // margin: 0 2rem;
            color: #fff;
            position: relative;
            // &::before {
            //     content: '';
            //     display: inline-block;
            //     width: 0.1rem;
            //     height: 65%;
            //     background-color: #fff;
            //     position: absolute;
            //     left: 0;
            //     top: 50%;
            //     transform: translateY(-50%);
            // }
            .img {
                width: 2.2rem;
                height: 2.2rem;
            }
        }
    }
    .nav-right {
        display: flex;
        gap: 1.2rem;
        align-items: center;
        
        // 使用深度选择器确保样式生效，覆盖 Element Plus plain 按钮的默认样式
        :deep(.el-button.btn-plain[plain]) {
            background-color: transparent !important;
            border: 1px solid #fff !important;
            border-color: #fff !important;
            color: #fff !important;
            padding: 0.8rem 1.6rem !important;
            font-size: 1.4rem !important;
            height: auto !important;
            min-height: auto !important;
            line-height: 1.5 !important;
            border-radius: 0.4rem !important;
            
            // 重置 Element Plus plain 按钮的默认样式
            &::before {
                display: none !important;
            }
            
            &:hover:not(.disabled):not(:disabled) {
                background-color: #fff !important;
                border-color: #fff !important;
                color: $cr-primary !important;
            }
            
            &:focus:not(.disabled):not(:disabled) {
                background-color: #fff !important;
                border-color: #fff !important;
                color: $cr-primary !important;
            }
            
            &:active:not(.disabled):not(:disabled) {
                background-color: #fff !important;
                border-color: #fff !important;
                color: $cr-primary !important;
            }
            
            &.disabled,
            &:disabled {
                opacity: 0.5 !important;
                cursor: not-allowed !important;
                background-color: transparent !important;
                border-color: #fff !important;
                color: #fff !important;
                
                &:hover,
                &:focus,
                &:active {
                    background-color: transparent !important;
                    border-color: #fff !important;
                    color: #fff !important;
                }
            }
        }
        
        :deep(.el-button.btn-white:not([type="primary"]):not([type="success"]):not([type="warning"]):not([type="danger"]):not([type="info"])) {
            background-color: #fff !important;
            border: 1px solid #fff !important;
            border-color: #fff !important;
            color: $cr-primary !important;
            padding: 0.8rem 1.6rem !important;
            font-size: 1.4rem !important;
            height: auto !important;
            min-height: auto !important;
            line-height: 1.5 !important;
            border-radius: 0.4rem !important;
            
            // 重置 Element Plus 默认样式
            &::before {
                display: none !important;
            }
            
            &:hover:not(.disabled):not(:disabled) {
                background-color: #1e7ede !important;
                border-color: #1e7ede !important;
                color: #fff !important;
            }
            
            &:focus:not(.disabled):not(:disabled) {
                background-color: #1e7ede !important;
                border-color: #1e7ede !important;
                color: #fff !important;
            }
            
            &:active:not(.disabled):not(:disabled) {
                background-color: #1e7ede !important;
                border-color: #1e7ede !important;
                color: #fff !important;
            }
            
            &.disabled,
            &:disabled {
                opacity: 0.5 !important;
                cursor: not-allowed !important;
                background-color: #fff !important;
                border-color: #fff !important;
                color: $cr-primary !important;
                
                &:hover,
                &:focus,
                &:active {
                    background-color: #fff !important;
                    border-color: #fff !important;
                    color: $cr-primary !important;
                }
            }
        }
    }
}
</style>
