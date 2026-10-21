<template>
    <view class="iconfont-container" :class="propClass" :style="'display:'+propContainerDisplay+';'">
        <text class="iconfont" :class="name" :style="'color:'+ color + ';font-size:'+ size + ';' + propStyle" @tap="$emit('click', $event)"></text>
    </view>
</template>

<script>
    export default {
        props: {
            name: {
                type: String,
                default: '',
            },
            color: {
                type: String,
                default: '',
            },
            size: {
                type: String,
                default: '28rpx',
            },
            propClass: {
                type: String,
                default: '',
            },
            propContainerDisplay: {
                type: String,
                default: 'inline-block',
            },
            propStyle: {
                type: String,
                default: '',
            },
        },
    };
</script>

<style scoped>
    /*
     * 字体与 .icon-* 图标类由 App.vue 全局引入（@/static/app/warm/common/iconfont/iconfont.css），
     * 会编译进 common/main.wxss 全局生效，此处禁止再 @import 一份：
     * 重复内嵌会把 200KB+ 的 base64 字体打进主包（曾导致主包超 2MB 无法上传）。
     * 组件内只保留自身布局样式。
     */
    .iconfont {
        display: flex;
        font-size: inherit;
        overflow: hidden;
        /* 因icon大小被设置为和字体大小一致，而span等标签的下边缘会和字体的基线对齐，故需设置一个往下的偏移比例，来纠正视觉上的未对齐效果 */
        vertical-align: -0.15em;
        outline: none;
        /* 定义元素的颜色，currentColor是一个变量，这个变量的值就表示当前元素的color值，如果当前元素未设置color值，则从父元素继承 */
        fill: currentcolor;
    }
</style>
