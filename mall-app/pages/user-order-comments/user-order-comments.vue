<template>
    <view :class="theme_view">
        <view class="padding-main">
            <view v-if="detail != null">
                <form @submit="form_submit" class="form-container">
                    <view v-for="(item, index) in detail.items" :key="index" class="form-gorup oh border-radius-main spacing-mb">
                        <view class="oh">
                            <view :data-value="item.goods_url" @tap="url_event" class="cp">
                                <image class="goods-image fl radius br margin-right-sm" :src="item.images" mode="aspectFill"></image>
                            </view>
                            <view class="item-base fl margin-top-sm">
                                <block v-for="(tv, ti) in [1, 2, 3, 4, 5]" :key="ti">
                                    <image class="star-icon va-m" :src="common_static_url + 'stars' + (form_rating_list[index] != undefined && form_rating_list[index] >= tv ? '-active' : '') + '-icon.png'" mode="aspectFill" @tap="rating_event" :data-index="index" :data-value="tv"></image>
                                </block>
                                <text v-if="form_rating_list[index] != undefined" class="cr-grey va-m margin-left-xs">{{ rating_msg[form_rating_list[index] - 1] }}</text>
                            </view>
                        </view>
                        <view class="margin-top-main br-t">
                            <textarea @input="form_content_event" :data-index="index" placeholder-class="cr-grey" class="cr-base" :placeholder="$t('user-order-comments-page.user-order-comments-page.review_hint') + '？' + $t('user-order-comments-page.user-order-comments-page.review_detail')" maxlength="230"></textarea>
                            <view class="margin-top-sm">
                                <component-upload :propData="form_images_list[index]" :propCallData="index" :propMaxNum="3" :propPathType="editor_path_type" @call-back="upload_image_event"></component-upload>
                            </view>
                        </view>
                    </view>

                    <view class="form-gorup border-radius-main spacing-mb">
                        <switch class="va-m" name="is_anonymous" @change="anonymous_event"></switch>
                        <text class="cr-base va-m">{{ $t('user-order-comments-page.user-order-comments-page.anonymous_review') }}</text>
                        <text class="fr cr-grey margin-top-sm">{{ anonymous_msg_list[anonymous_value] }}</text>
                    </view>

                    <view class="form-gorup form-gorup-submit">
                        <button form-type="submit" class="bg-main br-main cr-white round text-size" type="default" hover-class="none" :disabled="form_button_disabled">{{ $t('user-order-comments-page.user-order-comments-page.submit') }}</button>
                    </view>
                </form>
            </view>
            <view v-else>
                <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
            </view>
        </view>
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from "@/components/no-data/no-data";
    import componentBottomLine from "@/components/bottom-line/bottom-line";
    import componentUpload from '@/components/upload/upload';
    import { getOrderDetail } from '@/api/oms/order';
    import { submitOrderComments } from '@/api/pms/comment';

    var common_static_url = app.globalData.get_static_url("common");
    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                common_static_url: common_static_url,
                data_list_loding_status: 1,
                data_list_loding_msg: "",
                params: null,
                detail: null,
                editor_path_type: "",
                rating_msg: [this.$t('user-order-comments.user-order-comments.si255j'), this.$t('user-order-comments.user-order-comments.42b0x1'), this.$t('user-order-comments.user-order-comments.72mov3'), this.$t('user-order-comments.user-order-comments.16cmv6'), this.$t('user-order-comments.user-order-comments.e4i88f')],
                anonymous_value: 0,
                anonymous_msg_list: [this.$t('user-order-comments.user-order-comments.021i2d'), this.$t('user-order-comments.user-order-comments.h2ci83')],
                form_rating_list: [],
                form_images_list: [],
                form_content_list: [],
                form_button_disabled: false,
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentBottomLine,
            componentUpload
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            this.setData({
                params: params,
            });
        },

        onShow() {
            app.globalData.page_event_onshow_handle();
            this.init();
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
            app.globalData.page_share_handle();
        },

        onPullDownRefresh() {
            this.init();
        },

        methods: {
            init() {
                var self = this;
                self.setData({ data_list_loding_status: 1 });

                // 读取订单详情。
                getOrderDetail(this.params.orderNo).then(function(res) {
                    uni.stopPullDownRefresh();
                    if (res.code === '00000') {
                        var data = res.data;
                        // 将 orderItems 转换为评价表单结构。
                        var items = (data.orderItems || []).map(function(item) {
                            return {
                                order_item_id: item.id,
                                goods_id: item.spuId,
                                sku_id: item.skuId,
                                goods_url: '/pages/goods-detail/goods-detail?id=' + item.spuId,
                                images: item.picUrl,
                                title: item.spuName,
                                spec: item.skuName || ''
                            };
                        });
                        self.setData({
                            editor_path_type: data.editor_path_type || "",
                            detail: {
                                id: data.id,
                                items: items
                            },
                            data_list_loding_status: 3,
                            data_list_loding_msg: "",
                        });
                    } else {
                        self.setData({
                            data_list_loding_status: 2,
                            data_list_loding_msg: res.data.msg || this.$t('common.fail'),
                        });
                    }
                }).catch(function() {
                    uni.stopPullDownRefresh();
                    self.setData({
                        data_list_loding_status: 2,
                        data_list_loding_msg: self.$t('common.internet_error_tips'),
                    });
                });
            },

            upload_image_event(res, index) {
                var temp = this.form_images_list;
                temp[index] = res;
                this.setData({ form_images_list: temp });
            },

            anonymous_event(e) {
                this.setData({
                    anonymous_value: e.detail.value == true ? 1 : 0,
                });
            },

            rating_event(e) {
                var index = parseInt(e.currentTarget.dataset.index);
                var value = parseInt(e.currentTarget.dataset.value);
                var temp_list = [].concat(this.form_rating_list);
                temp_list[index] = value;
                this.setData({ form_rating_list: temp_list });
            },

            form_content_event(e) {
                var index = parseInt(e.currentTarget.dataset.index);
                var value = e.detail.value;
                var temp_list = [].concat(this.form_content_list);
                temp_list[index] = value;
                this.setData({ form_content_list: temp_list });
            },

            form_submit(e) {
                var length = this.detail.items.length;
                var self = this;

                // 评分校验
                var count = this.form_rating_list.length;
                if (count < length) {
                    app.globalData.showToast(self.$t('user-order-comments.user-order-comments.34ne4c'));
                    return false;
                }
                var max = Math.max.apply(null, this.form_rating_list);
                var min = Math.min.apply(null, this.form_rating_list);
                if (min < 1 || max > 5) {
                    app.globalData.showToast(self.$t('user-order-comments.user-order-comments.x6fwbf'));
                    return false;
                }

                // 内容校验
                var count = this.form_content_list.length;
                if (count < length) {
                    app.globalData.showToast(self.$t('user-order-comments.user-order-comments.8f303u'));
                    return false;
                }
                for (var i = 0; i < length; i++) {
                    var c = (this.form_content_list[i] || '').length;
                    if (c < 6 || c > 230) {
                        app.globalData.showToast(self.$t('user-order-comments.user-order-comments.39y6ym'));
                        return false;
                    }
                }

                // 图片校验
                if (this.form_images_list.length > 0) {
                    for (var j = 0; j < this.form_images_list.length; j++) {
                        var imgArr = this.form_images_list[j] || [];
                        if (imgArr.length > 3) {
                            app.globalData.showToast(self.$t('user-order-comments.user-order-comments.488j15'));
                            return false;
                        }
                    }
                }

                // 构建提交数据（适配 PMS 后端格式）
                var items = [];
                for (var i = 0; i < length; i++) {
                    items.push({
                        orderItemId: this.detail.items[i].order_item_id || 0,
                        spuId: this.detail.items[i].goods_id,
                        skuId: this.detail.items[i].sku_id,
                        rating: this.form_rating_list[i],
                        content: this.form_content_list[i],
                        images: this.form_images_list[i] || []
                    });
                }

                var submitData = {
                    items: items,
                    isAnonymous: this.anonymous_value
                };

                uni.showLoading({ title: self.$t('common.submit_in_text') });
                self.setData({ form_button_disabled: true });

                // 提交商品评价。
                submitOrderComments(this.detail.id, submitData).then(function(res) {
                    uni.hideLoading();
                    if (res.code === '00000') {
                        app.globalData.showToast(res.msg, 'success');
                        setTimeout(function() {
                            uni.navigateBack({ delta: 1 });
                        }, 2000);
                    } else {
                        self.setData({ form_button_disabled: false });
                        app.globalData.showToast(res.msg || self.$t('common.fail'));
                    }
                }).catch(function() {
                    uni.hideLoading();
                    self.setData({ form_button_disabled: false });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            url_event(e) {
                app.globalData.url_event(e);
            }
        },
    };
</script>
<style>
    @import "./user-order-comments.css";
</style>
