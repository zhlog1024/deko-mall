<template>
    <!-- 用户信息 -->
    <view :style="style_container">
        <view :style="style_img_container">
            <view class="pr padding-xxl" :style="style">
                <view class="flex-row jc-sb align-c margin-bottom-xxl">
                    <view class="flex-1 flex-row align-c gap-12">
                        <image :src="(user_info.user || null) !== null ? user_info.user.avatar : user.avatar" class="circle" mode="widthFix" :style="'width:' + base_data.user_avatar_size * 2 + 'rpx;height:' + base_data.user_avatar_size * 2 + 'rpx;'" data-value="/pages/personal/personal" @tap="url_event" />
                        <view class="flex-col gap-8" data-value="/pages/personal/personal" @tap="url_event">
                            <view class="text-size fw-b" :style="user_name_style">{{ (user_info.user || null) !== null ? user_info.user.user_name_view : user.user_name_view }}</view>
                            <view v-if="id_bool && (user_info.user || null) !== null" class="padding-horizontal-sm padding-vertical-xsss border-radius-sm" :style="number_code_style">ID:{{ user_info.user.number_code }}</view>
                        </view>
                    </view>
                    <view class="flex-row align-c" :style="'gap:' + base_data.img_space * 2 + 'rpx;'">
                        <view v-for="(item, index) in icon_setting" :key="index" :style="{ width: base_data.img_size + 'px', height: base_data.img_size + 'px' }" :data-value="item.link.page || ''" @tap="url_event">
                            <image v-if="item.img.length > 0" :src="item.img[0].url" class="border-radius-sm" mode="scaleToFill" :style="{ width: base_data.img_size + 'px', height: base_data.img_size + 'px' }" />
                            <iconfont v-else :name="'icon-' + item.icon" :size="base_data.img_size * 2 + 'rpx'" color="#666" propContainerDisplay="flex"></iconfont>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    const app = getApp();
    import { common_styles_computer, common_img_computer, gradient_computer } from '@/common/js/common/common.js';
    export default {
        props: {
            propValue: {
                type: Object,
                default: () => ({}),
            },
            propKey: {
                type: [String,Number],
                default: '',
            },
            // 组件渲染的下标
            propIndex: {
                type: Number,
                default: 1000000,
            },
        },
        data() {
            return {
                style_container: '',
                style_img_container: '',
                style: '',
                id_bool: true,
                icon_setting: [
                    { id: '1', img: [], icon: '', link: {} },
                    { id: '2', img: [], icon: '', link: {} },
                ],
                base_data: {},
                // 样式
                user_name_style: '',
                number_code_style: '',
                // 用户信息
                user_info: {
                    user: null,
                    order_count: '0',
                    goods_favor_count: '0',
                    goods_browse_count: '0',
                    message_unread_count: '0',
                },
                user: {
                    avatar: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                    user_name_view: '用户名',
                    number_code: '',
                },
            };
        },
        watch: {
            propKey(val) {
                // 初始化
                this.init();
            },
        },
        created() {
            this.init();
        },
        methods: {
            // 初始化数据
            init() {
                const new_content = this.propValue.content || {};
                const new_style = this.propValue.style || {};
                // mall-app: 获取实时用户数据（消息数等）
                this.fetch_user_data(new_content);
                const temp_base_data = {
                    // 头像
                    user_avatar_size: new_style.user_avatar_size,
                    // 人物
                    user_name_color: new_style.user_name_color,
                    user_name_weight: new_style.user_name_weight,
                    user_name_size: new_style.user_name_size,
                    // id
                    number_code_color_list: new_style.number_code_color_list,
                    number_code_color: new_style.number_code_color,
                    number_code_direction: new_style.number_code_direction,
                    number_code_weight: new_style.number_code_weight,
                    number_code_size: new_style.number_code_size,
                    // 图标设置
                    img_size: new_style.img_size,
                    img_space: new_style.img_space,
                    stats_name_color: new_style.stats_name_color,
                    stats_name_weight: new_style.stats_name_weight,
                };
                // id样式
                const new_gradient_obj = {
                    color_list: temp_base_data.number_code_color_list,
                    direction: temp_base_data.number_code_direction,
                };
                this.setData({
                    user_info: new_content.data || this.user_info,
                    icon_setting: new_content.icon_setting,
                    base_data: temp_base_data,
                    id_bool: new_content.config ? new_content.config.includes('number_code') : true,
                    // 人物名称样式
                    user_name_style: 'color:' + temp_base_data.user_name_color + ';' + 'font-size:' + temp_base_data.user_name_size * 2 + 'rpx;' + 'font-weight:' + temp_base_data.user_name_weight + ';',
                    number_code_style: gradient_computer(new_gradient_obj) + 'color:' + temp_base_data.number_code_color + ';' + 'font-size:' + temp_base_data.number_code_size * 2 + 'rpx;' + 'font-weight:' + temp_base_data.number_code_weight + ';',
                    style_container: common_styles_computer(new_style.common_style),
                    style_img_container: common_img_computer(new_style.common_style, this.propIndex),
                });
            },
            // mall-app: 获取实时用户数据
            fetch_user_data(content) {
                const store = uni.getStorageSync('user') || {};
                const userInfo = store.userInfo || null;
                if (userInfo) {
                    const user_obj = {
                        avatar: userInfo.avatarUrl || this.user.avatar,
                        user_name_view: userInfo.nickName || this.user.user_name_view,
                        number_code: String(userInfo.id || ''),
                    };
                    const data = content.data || {};
                    data['user'] = user_obj;
                    if (userInfo.messageTotal != null) {
                        data['message_unread_count'] = String(userInfo.messageTotal);
                    }
                    this.setData({ user_info: Object.assign({}, this.user_info, data, { user: user_obj }) });
                }
            },

            // 跳转链接
            url_event(e) {
                app.globalData.url_event(e);
            },
        },
    };
</script>
<style></style>
