<template>
    <view :class="theme_view">
        <scroll-view :scroll-y="true" class="scroll-box" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="data_list.length > 0" class="padding-horizontal-main padding-top-main">
                <view v-for="(item, index) in data_list" :key="index" class="padding-main border-radius-main oh bg-white spacing-mb">
                    <view :data-value="'/pages/plugins/shop/detail/detail?id=' + item.shop_info.id" @tap="url_event" class="cp">
                        <image class="logo fl radius" :src="item.shop_info.logo" mode="aspectFill"></image>
                        <view class="base">
                            <view class="single-text fw-b">{{item.shop_info.name}}</view>
                            <view class="multi-text cr-grey margin-top-sm">{{item.shop_info.describe}}</view>
                        </view>
                    </view>
                    <button class="br-yellow cr-yellow bg-white fr round" type="default" size="mini" @tap="cancel_event" :data-value="item.id" :data-index="index" hover-class="none">{{$t('common.cancel')}}</button>
                </view>
            </view>
            <view v-else>
                <!-- 提示信息 -->
                <component-no-data :propStatus="data_list_loding_status"></component-no-data>
            </view>
            <!-- 结尾 -->
            <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
        </scroll-view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from "@/components/no-data/no-data";
    import componentBottomLine from "@/components/bottom-line/bottom-line";
    import { request } from '@/config/request';

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                data_list_loding_status: 1,
                data_bottom_line_status: false,
                data_is_loading: 0
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentBottomLine
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);
        },

        onShow() {
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 数据加载
            this.init();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }

            // 分享菜单处理
            app.globalData.page_share_handle();
        },

        // 下拉刷新
        onPullDownRefresh() {
            this.setData({
                data_page: 1
            });
            this.get_data_list(1);
        },

        methods: {
            // 获取数据
            init() {
                var user = app.globalData.get_user_info(this, "init");
                if (user != false) {
                    this.get_data_list();
                } else {
                    this.setData({
                        data_list_loding_status: 0,
                        data_bottom_line_status: false
                    });
                }
            },

            // 获取数据
            get_data_list(is_mandatory) {
                // 分页是否还有数据
                if ((is_mandatory || 0) == 0) {
                    if (this.data_bottom_line_status == true) {
                        uni.stopPullDownRefresh();
                        return false;
                    }
                }
                
                // 是否加载中
                if(this.data_is_loading == 1) {
                    return false;
                }
                this.setData({
                    data_is_loading: 1,
                    data_list_loding_status: 1
                });
                
                // 加载loding
                if(this.data_page > 1) {
                    uni.showLoading({
                        title: this.$t('common.loading_in_text'),
                    });
                }
                
                // 获取数据
                const self = this;
                request({
                    url: '/mall-ums/app-api/v1/favor?targetType=SHOP',
                    method: 'POST',
                    data: {
                        pageNum: this.data_page,
                        pageSize: 10,
                    },
                    headers: { auth: true },
                }).then(function(res) {
                    if (self.data_page > 1) {
                        uni.hideLoading();
                    }
                    uni.stopPullDownRefresh();
                    if (res.code === '00000' && res.data != null) {
                        // 数据列表
                        var data = res.data;
                        var list = data.list || [];
                        var total = data.total || 0;
                        // 转换为收藏列表结构。
                        var tempShopList = list.map(function(item) {
                            return {
                                id: item.favorId,
                                shop_info: {
                                    id: item.targetId,
                                    url: '/pages/plugins/shop/detail/detail?id=' + item.targetId,
                                    name: item.name,
                                    logo: item.logo || '',
                                    describe: item.description || '',
                                },
                            };
                        });
                        if (self.data_page <= 1) {
                            var temp_data_list = tempShopList || [];
                        } else {
                            var temp_data_list = self.data_list || [];
                            for (var i in tempShopList) {
                                temp_data_list.push(tempShopList[i]);
                            }
                        }
                    
                        var pageTotal = Math.ceil(total / 10);
                        self.setData({
                            data_list: temp_data_list,
                            data_total: total,
                            data_page_total: pageTotal,
                            data_list_loding_status: temp_data_list.length > 0 ? 3 : 0,
                            data_list_loding_msg: '',
                            data_page: self.data_page + 1,
                            data_is_loading: 0,
                        });
                    
                        // 是否还有数据
                        self.setData({
                            data_bottom_line_status: self.data_list.length > 0 && self.data_page > 1 && self.data_page > pageTotal,
                        });
                    } else {
                        self.setData({
                            data_list_loding_status: 2,
                            data_list_loding_msg: res.msg || '加载失败',
                        });
                    }
                }).catch(function() {
                    if(self.data_page > 1) {
                        uni.hideLoading();
                    }
                    uni.stopPullDownRefresh();
                    self.setData({
                        data_list_loding_status: 2,
                        data_is_loading: 0,
                        data_list_loding_msg: self.$t('common.internet_error_tips'),
                    });
                });
            },

            // 滚动加载
            scroll_lower(e) {
                this.get_data_list();
            },

            // 取消
            cancel_event(e) {
                uni.showModal({
                    title: this.$t('common.warm_tips'),
                    content: this.$t('common.cancel_confirm_tips'),
                    confirmText: this.$t('common.confirm'),
                    cancelText: this.$t('common.no'),
                    success: result => {
                        if (result.confirm) {
                            // 参数
                            var id = e.currentTarget.dataset.value;
                            var index = e.currentTarget.dataset.index;
                            
                            // 加载loding
                            uni.showLoading({
                                title: this.$t('common.processing_in_text')
                            });
                            request({
                                url: '/mall-ums/app-api/v1/favor/' + id,
                                method: 'DELETE',
                                headers: { auth: true },
                            }).then(function(res) {
                                uni.hideLoading();
                                if (res.code === '00000') {
                                    var temp_data_list = this.data_list;
                                    temp_data_list.splice(index, 1);
                                    this.setData({
                                        data_list: temp_data_list
                                    });
                                    if (temp_data_list.length == 0) {
                                        this.setData({
                                            data_list_loding_status: 0,
                                            data_bottom_line_status: false
                                        });
                                    }
                                    app.globalData.showToast('已取消收藏', 'success');
                                } else {
                                    app.globalData.showToast(res.msg || '操作失败');
                                }
                            }.bind(this)).catch(function() {
                                uni.hideLoading();
                                app.globalData.showToast(this.$t('common.internet_error_tips'));
                            }.bind(this));
                        }
                    }
                });
            },

            // url事件
            url_event(e) {
                app.globalData.url_event(e);
            }
        }
    };
</script>
<style>
    @import './favor.css';
</style>