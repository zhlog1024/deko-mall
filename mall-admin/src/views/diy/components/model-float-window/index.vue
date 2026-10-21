<template>
    <div class="flex align-c jc-c suspension" :style="style">
        <div class="spread flex align-c jc-c" :style="spreadMarginStyle">
            <template v-if="new_style.float_style == 'diffuse'">
                <div class="ring"></div>
                <div class="ring"></div>
            </template>
            <image-empty v-model="form.button_img[0]" :class="{'shadow': new_style.float_style == 'shadow'}" />
        </div>
    </div>
</template>
<script setup lang="ts">
import { percentage_count } from '../../utils';

const props = defineProps({
    value: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
const style = ref('');
// 用于页面判断显示
const state = reactive({
    form: props.value?.content || {},
    new_style: props.value?.style || {},
});
// 如果需要解构，确保使用toRefs
const { form, new_style } = toRefs(state);
const color = computed(() => new_style.value.float_style_color)
// 按钮大小：默认 90（对应 rpx），预览按比例缩放为 rem
// 原始比例：img=4.5rem, spread=6rem, ring=5rem, inner_img=3rem @ button_size=90
const btn_size = computed(() => new_style.value.button_size || 90)
const button_size_rem = computed(() => btn_size.value / 20)            // 4.5rem @90
const button_inner_img_rem = computed(() => btn_size.value / 30)       // 3rem @90
const spread_size_rem = computed(() => btn_size.value / 15)            // 6rem @90
const ring_size_rem = computed(() => btn_size.value / 18)              // 5rem @90
const horizontal_margin = computed(() => new_style.value.horizontal_margin)
const marginRem = computed(() => (horizontal_margin.value || 0) / 20)   // rpx→rem
const spreadMarginStyle = computed(() => {
    const m = marginRem.value;
    if (new_style.value.display_location === 'left') {
        return `justify-content: flex-start; margin-left: ${m}rem;`;
    }
    return `justify-content: flex-end; margin-right: ${m}rem;`;
})

const emits = defineEmits(['change']);
// 内容参数的集合
watch(props.value, (val) => {
    // 内容
    form.value = val?.content || {};
    // 样式
    new_style.value = val?.style || {};
    // 默认是靠右
    let location = `justify-content: flex-end;`;
    if (new_style.value.display_location == 'left') {
        location = `justify-content: flex-start;`;
    }
    style.value = location;
    // 手机端要做悬浮效果，需要抛给父组件
    emits('change', { bottom: new_style.value.offset_number, location: new_style.value.display_location });
}, {immediate: true, deep: true});
</script>
<style lang="scss" scoped>
:deep(.el-image) {
    width: calc(v-bind(button_size_rem) * 1rem);
    height: calc(v-bind(button_size_rem) * 1rem);
    border-radius: 50%;
    .image-slot img {
        width: calc(v-bind(button_inner_img_rem) * 1rem);
        height: calc(v-bind(button_inner_img_rem) * 1rem);
    }
}
.shadow {
    box-shadow: 0 0 20px v-bind(color);
}
/**
* 呼吸灯
*/
.spread {
    position: relative;
    z-index: 1;
    width: calc(v-bind(spread_size_rem) * 1rem);
    height: calc(v-bind(spread_size_rem) * 1rem);
}
.spread .ring {
    /* 速度为1.5 * 层数 = 实际运行速度，速度修改则 animation-delay 属性也修改相同速度 */
    animation: pulsing 1.5s ease-out infinite;
    background-color: v-bind(color);
    border-radius: 100%;
    width: calc(v-bind(ring_size_rem) * 1rem);
    height: calc(v-bind(ring_size_rem) * 1rem);
    position: absolute;
}
/* 速度为1*层数 */
.spread .ring:nth-of-type(1) {
    -webkit-animation-delay: -1.5s;
    animation-delay: -1.5s;
}

/* 速度为1*层数 */
.spread .ring:nth-of-type(2) {
    -webkit-animation-delay: -2s;
    animation-delay: -2s;
}
@keyframes pulsing {
    100% {
        transform: scale(1.35);
        opacity: 0
    }
}
</style>
