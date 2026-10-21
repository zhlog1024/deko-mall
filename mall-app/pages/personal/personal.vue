<template>
    <view :class="theme_view">
        <view class="page-bottom-fixed">
            <block v-if="data_list_loding_status == 3">
                <form @submit="form_submit" class="form-container">
                    <view class="padding-main page-bottom-fixed">
                        <view class="bg-white border-radius-main oh">
                            <view class="form-gorup oh flex-row jc-sb align-c">
                                <view>{{$t('personal.personal.cw1d8p')}}</view>
                                <view class="flex-row align-c">
                                    <button class="bg-white br-0 lh-0 padding-horizontal-sm" hover-class="none" open-type="chooseAvatar" @chooseavatar="choose_avatar_event" @tap="choose_avatar_event">
                                        <image :src="user_data.avatarUrl || default_avatar" mode="widthFix" class="circle br user-avatar flex-1 flex-width"></image>
                                    </button>
                                    <iconfont name="icon-arrow-right" size="34rpx" color="#ccc"></iconfont>
                                </view>
                            </view>

                            <view class="form-gorup oh flex-row jc-sb align-c">
                                <view class="form-gorup-title">{{$t('personal.personal.gw8br3')}}<text class="form-group-tips-must">*</text></view>
                                <view class="flex-row align-c flex-1 flex-width">
                                    <input :type="application_client_type == 'weixin' ? 'nickname' : 'text'" name="nickName" :value="user_data.nickName || ''" maxlength="16" placeholder-class="cr-grey-9 tr" class="cr-base tr margin-right-sm" :placeholder="$t('personal.personal.44112i')" />
                                </view>
                            </view>

                            <view class="form-gorup oh flex-row jc-sb align-c">
                                <view class="form-gorup-title">{{$t('personal.personal.jibx42')}}</view>
                                <view class="flex-1 flex-width flex-row jc-e align-c">
                                    <picker class="margin-right-sm wh-auto tr" name="birthday" mode="date" :value="user_birthday" data-field="birthday" @change="select_change_event">
                                        <view :class="'picker ' + ((user_birthday || null) == null ? 'cr-grey' : '')">{{ user_birthday || $t('personal.personal.85404s') }}</view>
                                    </picker>
                                    <iconfont name="icon-arrow-right" size="34rpx" color="#ccc"></iconfont>
                                </view>
                            </view>

                            <view class="form-gorup oh flex-row jc-sb align-c">
                                <view class="form-gorup-title">{{$t('personal.personal.x2fofv')}}</view>
                                <view class="flex-row jc-e align-c flex-1 flex-width">
                                    <picker @change="select_change_event" :value="user_gender" :range="gender_list" range-key="name" name="gender" data-field="gender" class="margin-right-sm wh-auto tr">
                                        <view class="uni-input cr-base picker">{{ gender_list[user_gender] ? gender_list[user_gender].name : '未知' }}</view>
                                    </picker>
                                    <iconfont name="icon-arrow-right" size="34rpx" color="#ccc"></iconfont>
                                </view>
                            </view>
                        </view>

                        <view class="bottom-fixed" :style="bottom_fixed_style">
                            <view class="bottom-line-exclude">
                                <button class="item bg-main br-main cr-white round text-size" type="default" form-type="submit" hover-class="none" :disabled="form_submit_disabled_status">{{$t('common.save')}}</button>
                            </view>
                        </view>
                    </view>
                </form>
            </block>

            <!-- 错误提示 -->
            <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
        </view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import config from '@/config';
    import { getUserInfo, updateMembers } from '@/api/user';
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                application_client_type: app.globalData.application_client_type(),
                data_list_loding_status: 1,
                data_list_loding_msg: '',
                bottom_fixed_style: '',
                form_submit_disabled_status: false,
                default_avatar: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                user_data: {},
                user_gender: 0,
                user_birthday: '',
                gender_list: [
                    { name: '未知' },
                    { name: '男' },
                    { name: '女' }
                ]
            };
        },

        components: {
            componentCommon,
            componentNoData
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
        },

        onShow() {
            app.globalData.page_event_onshow_handle();

            this.init();

            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        methods: {
            init() {
                if (app.globalData.get_user_cache_info()) {
                    this.get_data();
                } else {
                    this.setData({
                        data_list_loding_status: 0,
                        data_list_loding_msg: this.$t('setup.setup.nwt4o1'),
                    });
                }
            },

            get_data() {
                getUserInfo().then(res => {
                    if (res.code == '00000') {
                        var data = res.data || {};
                        this.setData({
                            data_list_loding_status: 3,
                            user_data: data,
                            user_gender: parseInt(data.gender) || 0,
                            user_birthday: data.birthday || '',
                        });
                    } else {
                        this.setData({
                            data_list_loding_status: 0,
                            data_list_loding_msg: res.msg,
                        });
                    }
                }).catch(() => {
                    this.setData({
                        data_list_loding_status: 2,
                        data_list_loding_msg: this.$t('common.internet_error_tips'),
                    });
                    app.globalData.showToast(this.$t('common.internet_error_tips'));
                });
            },

            url_event(e) {
                app.globalData.url_event(e);
            },

            select_change_event(e) {
                var field = e.currentTarget.dataset.field;
                var value = e.detail.value;
                var temp = this.user_data;
                temp[field] = value;
                var update = { user_data: temp };
                if (field === 'gender') {
                    update.user_gender = parseInt(value);
                }
                if (field === 'birthday') {
                    update.user_birthday = value;
                }
                this.setData(update);
            },

            choose_avatar_event(e = null) {
                let self = this;

                // #ifndef APP
                let arr = ['weixin', 'alipay'];
                if (arr.indexOf(this.application_client_type) != -1) {
                    if(e !== null && e.detail && e.detail.avatarUrl) {
                        let temp_url = e.detail.avatarUrl;
                        self.upload_handle(temp_url, self);
                    }
                } else {
                    uni.chooseImage({
                        count: 1,
                        success(res) {
                            if (res.tempFilePaths.length > 0) {
                                self.upload_handle(res.tempFilePaths[0], self);
                            }
                        },
                    });
                }
                // #endif

                // #ifdef APP
                uni.showActionSheet({
                    itemList: [self.$t('common.camera_text'), self.$t('common.album_choice_text')],
                    cancelText: self.$t('common.cancel'),
                    success: (res) => {
                        if(res.tapIndex == 0) {
                            var type = 'camera';
                            var permKey = 'perm_rt_camera';
                            var title = self.$t('common.need_camera_power_text');
                            var msg = self.$t('common.camera_get_access_text');
                        } else {
                            var type = 'album';
                            var permKey = 'perm_rt_album';
                            var title = self.$t('common.need_album_power_text');
                            var msg = self.$t('common.album_get_access_text');
                        }
                        // 已授权过，直接走选择
                        if (uni.getStorageSync(permKey)) {
                            self.choose_image_handle(type, self);
                            return;
                        }
                        uni.showModal({
                            title: title,
                            content: msg,
                            cancelText: self.$t('common.cancel'),
                            confirmText: self.$t('common.confirm'),
                            success: function (res) {
                                if (res.confirm) {
                                    uni.setStorageSync(permKey, 1);
                                    self.choose_image_handle(type, self);
                                }
                            }
                        });
                    }
                });
                // #endif
            },

            choose_image_handle(type, self) {
                uni.chooseImage({
                    sourceType: [type],
                    count: 1,
                    success(res) {
                        if (res.tempFilePaths.length > 0) {
                            self.upload_handle(res.tempFilePaths[0], self);
                        }
                    },
                    fail(err) {
                        if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
                            uni.showModal({
                                title: self.$t('common.power_refuse_text'),
                                content: (type == 'camera') ? self.$t('common.camera_error_text') : self.$t('common.album_error_text'),
                                showCancel: false
                            });
                        }
                    }
                });
            },

            upload_handle(image, self) {
                var token = uni.getStorageSync('token');
                uni.showLoading({ title: '上传中...' });
                uni.uploadFile({
                    url: config.url + '/mall-system/api/v1/files/upload',
                    filePath: image,
                    name: 'file',
                    header: { Authorization: token },
                    formData: {},
                    success: function (res) {
                        uni.hideLoading();
                        var data = typeof res.data == 'object' ? res.data : JSON.parse(res.data);
                        if (data.code == '00000') {
                            var temp = self.user_data;
                            temp['avatarUrl'] = data.data.url || data.data;
                            self.setData({ user_data: temp });
                            getApp().globalData.showToast('头像已更新', 'success');
                        } else {
                            getApp().globalData.showToast(data.msg || '上传失败');
                        }
                    },
                    fail: function () {
                        uni.hideLoading();
                        getApp().globalData.showToast('上传失败');
                    }
                });
            },

            form_submit(e) {
                var form_data = {
                    nickName: this.user_data.nickName || '',
                    avatarUrl: this.user_data.avatarUrl || '',
                    birthday: this.user_birthday || null,
                    gender: this.user_gender
                };

                this.setData({
                    form_submit_disabled_status: true,
                });
                uni.showLoading({
                    title: this.$t('common.processing_in_text'),
                });

                updateMembers(form_data).then(res => {
                    uni.hideLoading();
                    this.setData({
                        form_submit_disabled_status: false,
                    });
                    if (res.code == '00000') {
                        // 更新 Storage（数据源）
                        var store = uni.getStorageSync('user') || {};
                        var userInfo = store.userInfo || {};
                        Object.assign(userInfo, res.data || form_data);
                        store.userInfo = userInfo;
                        uni.setStorageSync('user', store);
                        app.globalData.showToast(res.msg || '保存成功', 'success');
                        setTimeout(function () {
                            uni.navigateBack();
                        }, 1000);
                    } else {
                        app.globalData.showToast(res.msg || this.$t('common.sub_error_retry_tips'));
                    }
                }).catch(() => {
                    uni.hideLoading();
                    this.setData({
                        form_submit_disabled_status: false,
                    });
                    app.globalData.showToast(this.$t('common.internet_error_tips'));
                });
            },
        },
    };
</script>
<style>
    @import './personal.css';
</style>
