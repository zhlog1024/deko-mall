<template>
    <movable-area class="float-window-movable-container">
        <movable-view :x="x" :y="y" direction="all" class="float-window-spread flex-row align-c jc-c" @tap="btn_event" :style="spread_style">
            <block v-if="style.float_style == 'diffuse'">
                <view class="ring" :style="ring_style"></view>
                <view class="ring" :style="ring_style"></view>
            </block>
            <view class="img oh" :style="img_style">
                <block v-if="(form || null) != null && form.button_jump == 'customer_service'">
                    <component-online-service :propChatImage="img_url" :propIsSpread="false" :propIsMovable="false" :propIsDiy="true"></component-online-service>
                </block>
                <block v-else>
                    <image-empty :propImageSrc="img_url" propImgFit="aspectFill" propErrorStyle="width: 60rpx;height: 60rpx;"></image-empty>
                </block>
            </view>
        </movable-view>
        <component-quick-nav ref="quick_nav" :propIsBtn="false"></component-quick-nav>
    </movable-area>
</template>

<script>
    const app = getApp();
    import { isEmpty } from '@/common/js/common/common.js';
    import imageEmpty from '@/pages/diy/components/diy/modules/image-empty.vue';
    import componentOnlineService from '@/components/online-service/online-service';
    import componentQuickNav from '@/components/quick-nav/quick-nav';
    export default {
        components: {
            imageEmpty,
            componentOnlineService,
            componentQuickNav
        },
        props: {
            propValue: {
                type: Object,
                default: () => {
                    return {};
                },
            },
            propIsHalf: {
                type: Boolean,
                default: false,
            },
            propKey: {
                type: [String,Number],
                default: '',
            }
        },
        data() {
            return {
                form: {},
                style: {},
                img_url: '',
                x: 0,
                y: 0,
                img_style: '',
                spread_style: '',
                ring_style: '',
            };
        },
        watch: {
            propKey(val) {
                // 初始化
                this.init();
            }
        },
        created() {
            this.init();
        },
        methods: {
            init() {
                // 获取内容
                let form = this.propValue.content || {};
                // 获取图片
                let img_url = (form.button_img || null) != null ? (form.button_img[0] || null) : null;
                if (img_url != null) {
                    img_url = img_url.url || null;
                }
                const { float_style, float_style_color, display_location, offset_number_percentage, horizontal_margin } = this.propValue.style;
                // 按钮大小（rpx），默认 90
                const button_size = this.propValue.style.button_size || 90;
                const spread_size = button_size + 10;           // 容器内容区比图片大 10rpx
                const ring_size = button_size + 10;             // 扩散圈同容器
                // 获取当前手机的宽度和高度
                const { windowWidth, windowHeight } = uni.getSystemInfoSync();
                // rpx → px：1rpx = screenWidth / 750
                const total_width_rpx = spread_size + 20;       // + CSS padding 10rpx × 2
                const total_width_px = Math.ceil(total_width_rpx * windowWidth / 750);
                // 内置间隙 15rpx（padding 10rpx + 图片居中偏移 5rpx），扣掉后 margin 直接控制视觉间距
                const inner_gap_rpx = 15;
                const raw_margin = horizontal_margin || 0;
                const effective_margin = Math.max(0, raw_margin - inner_gap_rpx);
                const margin_px = Math.ceil(effective_margin * windowWidth / 750);
                // 计算出距离左边的距离
                let x = display_location == 'left' ? margin_px : windowWidth - total_width_px - margin_px;
                // 计算出距离顶部的距离
                const y = Math.ceil(windowHeight * (1 - Number(offset_number_percentage)) - 20);
                // 背景色/阴影样式
                const bg_or_shadow = float_style == 'shadow'
                    ? `box-shadow: 0 0 40rpx ${float_style_color};border-radius: 50%;`
                    : `background-color: ${float_style_color};border-radius: 50%;`;
                this.setData({
                    form: form,
                    style: this.propValue.style,
                    img_url: img_url,
                    img_style: `width: ${button_size}rpx; height: ${button_size}rpx; border-radius: 50%; z-index: 2; ${bg_or_shadow}`,
                    spread_style: `width: ${spread_size}rpx; height: ${spread_size}rpx;`,
                    ring_style: `width: ${ring_size}rpx; height: ${ring_size}rpx; background-color: ${float_style_color};`,
                    x: x,
                    y: y
                });
            },
            // 按钮事件
            btn_event() {
                const { button_jump, button_link } = this.form;
                console.log('[float-window] btn_event clicked', button_jump, button_link);
                switch(button_jump) {
                    // 链接
                    case 'link' :
                        if (!isEmpty(button_link)) {
                            console.log('[float-window] calling url_open', button_link.page);
                            app.globalData.url_open && app.globalData.url_open(button_link.page);
                        }
                        break;
                    // 快捷导航
                    case 'quick_nav' :
                        this.$emit('btn_event', 'quick_nav');
                        break;
                    // 多语言
                    case 'lang' :
                        this.$emit('btn_event', 'lang');
                        break;
                    // 分享页面
                    case 'share' :
                        this.$emit('btn_event', 'share');
                        break;
                    // 回到顶部
                    case 'gotop' :
                        this.$emit('btn_event', 'gotop');
                        break;
                }
            },
        },
    };
</script>

<style scoped lang="scss">
    .img {
        // 尺寸和样式由 img_style 动态控制
    }
    .float-window-movable-container {
        position: fixed;
        width: 100%;
        max-width: 1600rpx;
        height: 100%;
        top: 0;
        background: transparent;
        pointer-events: none;
        z-index: 99;
    }
    /**
    * 呼吸灯
    */
    .float-window-spread {
        position: relative;
        pointer-events: auto;
        z-index: 1;
        padding: 10rpx;
        border-radius: 50%;
        // 尺寸由 spread_style 动态控制
    }
    .float-window-spread .ring {
        /* 速度为1.5 * 层数 = 实际运行速度，速度修改则 animation-delay 属性也修改相同速度 */
        animation: pulsing 1.5s ease-out infinite;
        border-radius: 100%;
        position: absolute;
        // 尺寸和颜色由 ring_style 动态控制
    }
    /* 速度为1*层数 */
    .float-window-spread .ring:nth-of-type(1) {
        -webkit-animation-delay: -1.5s;
        animation-delay: -1.5s;
    }

    /* 速度为1*层数 */
    .float-window-spread .ring:nth-of-type(2) {
        -webkit-animation-delay: -2s;
        animation-delay: -2s;
    }
    @keyframes pulsing {
        100% {
            transform: scale(1.35);
            opacity: 0;
        }
    }
</style>
