<template>
    <el-form-item label="角标">
        <el-row class="w">
            <el-col :span="24"><el-switch v-model="form.seckill_subscript_show" active-value="1" inactive-value="0"></el-switch></el-col>
        </el-row>
        <el-row v-if="form.seckill_subscript_show == '1'" class="mt-10 w">
            <el-col :span="24">
                <div class="flex-col gap-10">
                    <el-form-item>
                        <el-radio-group v-model="form.subscript_type">
                            <el-radio value="text">文本</el-radio>
                            <el-radio value="img-icon">图片或图标</el-radio>
                            <el-radio value="dynamic-text">动态文本</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="form.subscript_type === 'img-icon'">
                        <upload v-model="form.subscript_img_src" v-model:icon-value="form.subscript_icon_class" is-icon :limit="1" size="50"></upload>
                    </el-form-item>
                    <el-form-item v-if="form.subscript_type === 'text'">
                        <el-input v-model="form.subscript_text" placeholder="请输入固定文本" clearable></el-input>
                    </el-form-item>
                    <el-form-item v-if="form.subscript_type === 'dynamic-text'">
                        <el-tag type="warning" effect="plain" style="width: 100%;">文案由后端按商品自动计算，无需手动填写</el-tag>
                    </el-form-item>
                </div>
            </el-col>
        </el-row>
    </el-form-item>
</template>
<script lang="ts" setup>
import Upload from '../upload/index.vue';
const props = defineProps({
    value: {
        type: Object,
        default: () => ({}),
    }
});
const form = ref(props.value);
watch(() => props.value, (val) => {
    form.value = val;
}, {immediate: true, deep: true});

</script>