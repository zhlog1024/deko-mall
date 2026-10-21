<template>
    <div class="setting-content">
        <template v-if="type == '1'">
            <model-plugins-live-tabs-content :value="form.content" :default-config="data_config"></model-plugins-live-tabs-content>
        </template>
        <template v-else-if="type == '2'">
            <model-plugins-live-tabs-styles :value="form.style" :content="form.content" :default-config="data_config"></model-plugins-live-tabs-styles>
        </template>
    </div>
</template>
<script setup lang="ts">
/**
 * @description: 直播选项卡组件（设置入口：内容/样式）
 */
import { reactive } from 'vue';

const props = defineProps({
    type: {
        type: String,
        default: '1',
    },
    value: {
        type: Object,
        default: () => ({}),
    },
});

const data_config = reactive({
    img_radius_0: 4,
    img_radius_1: 0,
});
const form = ref(props.value);
</script>
