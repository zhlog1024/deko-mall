<template>
    <view :class="theme_view">
        <!-- 分类 -->
        <scroll-view v-if="(category_list || null) != null && category_list.length > 0" class="nav-base scroll-view-horizontal bg-white oh" scroll-x="true">
            <view :class="'item cr-grey dis-inline-block padding-horizontal-main ' + (nav_active_value == 0 ? 'cr-main' : '')" @tap="nav_event" data-value="0">{{$t('common.all')}}</view>
            <block v-for="(item, index) in category_list" :key="index">
                <view :class="'item cr-grey dis-inline-block padding-horizontal-main ' + (nav_active_value == item.id ? 'cr-main' : '')" @tap="nav_event" :data-value="item.id">{{ item.name }}</view>
            </block>
        </scroll-view>

        <!-- 列表 -->
        <scroll-view :scroll-y="true" class="scroll-box scroll-box-ece-nav" @scrolltolower="scroll_lower" lower-threshold="60">
            <view v-if="(data_list || null) != null && data_list.length > 0" class="data-list padding-horizontal-main padding-top-main oh">
                <block v-for="(item, index) in data_list" :key="index">
                    <view :data-value="item.url" @tap="url_event" class="item padding-main border-radius-main bg-white oh cp spacing-mb">
                        <view v-if="(item.cover || null) != null" class="oh pr item-cover">
                            <image :src="item.cover" mode="aspectFit" class="radius fl cover"></image>
                            <view class="base-right fr">
                                <view class="fw-b single-text text-size" :style="(item.title_color || null) != null ? 'color:' + item.title_color + ' !important;' : ''">{{ item.title }}</view>
                                <view v-if="(item.describe || null) != null" class="cr-base margin-top-sm multi-text text-size-sm">{{item.describe}}</view>
                                <view class="pa right-0 bottom-0 base-right-bottom cr-grey text-size-xs">
                                    <text class="fl">{{ item.add_time }}</text>
                                    <text class="fr">{{$t('article-category.article-category.gxra15')}}{{ item.access_count }}</text>
                                </view>
                            </view>
                        </view>
                        <block v-else>
                            <view class="fw-b single-text text-size" :style="(item.title_color || null) != null ? 'color:' + item.title_color + ' !important;' : ''">{{ item.title }}</view>
                            <view class="cr-grey oh text-size-xs margin-top-sm">
                                <text class="fl">{{ item.add_time }}</text>
                                <text class="fr">{{$t('article-category.article-category.gxra15')}}{{ item.access_count }}</text>
                            </view>
                        </block>
                    </view>
                </block>
            </view>
            <view v-else>
                <!-- 提示信息 -->
                <component-no-data :propStatus="data_list_loding_status" :propMsg="data_list_loding_msg"></component-no-data>
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
    import { time_stamp } from '@/common/js/common/common.js';
    import { getArticleCategoryTree, listArticles } from '@/api/diy/article.js';

    // 常量（一次最多加载条数，与 mall-cms 默认分页一致）
    const data_page_size = 10;

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                data_list_loding_status: 1,
                data_list_loding_msg: "",
                data_bottom_line_status: false,
                data_is_loading: 0,
                data_list: [],
                data_total: 0,
                data_page_total: 0,
                data_page: 1,
                params: null,
                category_list: [],
                nav_active_value: 0,
                // 自定义分享信息
                share_info: {},
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentBottomLine,
        },

        onLoad(params) {
            // 调用公共事件方法
            app.globalData.page_event_onload_handle(params);

            var nav_active_value = 0;
            if (params != null && (params.id || null) != null) {
                nav_active_value = parseInt(params.id) || 0;
            }
            this.setData({
                params: params,
                nav_active_value: nav_active_value,
            });

            // 数据加载
            this.get_data();
        },

        onShow() {
            // 调用公共事件方法
            app.globalData.page_event_onshow_handle();

            // 公共onshow事件
            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        // 下拉刷新
        onPullDownRefresh() {
            this.setData({
                data_page: 1,
            });
            this.get_data_list(1);
        },

        methods: {
            // 时间格式化（createTime → add_time）
            format_time_handle(time) {
                return time_stamp(time);
            },

            // 分类树扁平化（tree → [{id,name}]，节点含 children 时递归展开）
            category_tree_flatten(tree, list) {
                var new_list = list || [];
                if ((tree || null) != null && tree.length > 0) {
                    for (var i in tree) {
                        var item = tree[i];
                        if ((item || null) == null) {
                            continue;
                        }
                        new_list.push({
                            id: item.id,
                            name: item.name,
                        });
                        if ((item.children || null) != null && item.children.length > 0) {
                            this.category_tree_flatten(item.children, new_list);
                        }
                    }
                }
                return new_list;
            },

            // 将商品 DTO 转换为页面渲染字段。
            data_item_handle(item) {
                var url = (item.jumpUrl || null) != null && item.jumpUrl != '' ? item.jumpUrl : '/pages/article-detail/article-detail?id=' + item.id;
                return {
                    id: item.id,
                    title: item.title || '',
                    title_color: item.title_color || null,
                    cover: item.cover || null,
                    describe: item.description || '',
                    add_time: this.format_time_handle(item.createTime),
                    access_count: item.accessCount || 0,
                    url: url,
                };
            },

            // 初始化
            get_data() {
                getArticleCategoryTree()
                    .then((data) => {
                        uni.stopPullDownRefresh();
                        // 分类树扁平化处理（mall 分类树可能带 children）
                        this.setData({
                            category_list: this.category_tree_flatten(data.data || [], []),
                        });

                        // 基础自定义分享
                        this.setData({
                            share_info: {
                                path: "/pages/article-category/article-category",
                                query: "id=" + this.nav_active_value,
                            },
                        });

                        // 获取列表数据
                        this.get_data_list(1);

                        // 分享菜单处理
                        app.globalData.page_share_handle(this.share_info);
                    })
                    .catch(() => {
                        uni.stopPullDownRefresh();
                        this.setData({
                            data_list_loding_status: 2,
                        });
                        app.globalData.showToast(this.$t('common.internet_error_tips'));
                    });
            },

            // 获取数据列表
            get_data_list(is_mandatory) {
                // 分页是否还有数据
                if ((is_mandatory || 0) == 0) {
                    if (this.data_bottom_line_status == true) {
                        uni.stopPullDownRefresh();
                        return false;
                    }
                }

                // 是否加载中
                if (this.data_is_loading == 1) {
                    return false;
                }
                this.setData({ data_is_loading: 1 });

                // 加载loding
                if(this.data_page > 1) {
                    uni.showLoading({
                        title: this.$t('common.loading_in_text'),
                    });
                }

                // 请求参数（全部类目不传 categoryId，避免误筛）
                var request_params = {
                    pageNum: this.data_page,
                    pageSize: data_page_size,
                };
                if (this.nav_active_value != null && this.nav_active_value != 0) {
                    request_params['categoryId'] = parseInt(this.nav_active_value);
                }

                // 获取数据
                listArticles(request_params)
                    .then((data) => {
                        if(this.data_page > 1) {
                            uni.hideLoading();
                        }
                        uni.stopPullDownRefresh();

                        // 分页数据
                        var page_data = data.data || null;
                        var page_list = (page_data != null && page_data.list != null) ? page_data.list : [];
                        var page_total = (page_data != null && page_data.total != null) ? page_data.total : 0;
                        var temp_page_total = Math.ceil(page_total / data_page_size);

                        // 字段适配（mall DTO → 渲染字段）
                        var data_list = [];
                        for (var i in page_list) {
                            data_list.push(this.data_item_handle(page_list[i]));
                        }

                        if (data_list.length > 0) {
                            if (this.data_page <= 1) {
                                var temp_data_list = data_list;
                            } else {
                                var temp_data_list = this.data_list || [];
                                var temp_data = data_list;
                                for (var j in temp_data) {
                                    temp_data_list.push(temp_data[j]);
                                }
                            }
                            this.setData({
                                data_list: temp_data_list,
                                data_total: page_total,
                                data_page_total: temp_page_total,
                                data_list_loding_status: 3,
                                data_page: this.data_page + 1,
                                data_is_loading: 0,
                            });

                            // 是否还有数据
                            this.setData({
                                data_bottom_line_status: this.data_list.length > 0 && this.data_page > 1 && this.data_page > this.data_page_total,
                            });
                        } else {
                            this.setData({
                                data_list_loding_status: 0,
                                data_is_loading: 0,
                            });
                            if (this.data_page <= 1) {
                                this.setData({
                                    data_list: [],
                                    data_page_total: temp_page_total,
                                    data_bottom_line_status: false,
                                });
                            } else {
                                // 已经到底，避免重复请求
                                this.setData({
                                    data_page_total: temp_page_total,
                                    data_bottom_line_status: this.data_page > temp_page_total,
                                });
                            }
                        }
                    })
                    .catch(() => {
                        if(this.data_page > 1) {
                            uni.hideLoading();
                        }
                        uni.stopPullDownRefresh();
                        this.setData({
                            data_list_loding_status: 2,
                            data_is_loading: 0,
                        });
                        app.globalData.showToast(this.$t('common.internet_error_tips'));
                    });
            },

            // 滚动加载
            scroll_lower(e) {
                this.get_data_list();
            },

            // 导航事件
            nav_event(e) {
                this.setData({
                    nav_active_value: e.currentTarget.dataset.value || 0,
                    data_page: 1,
                    data_list: [],
                    data_list_loding_status: 1,
                    data_bottom_line_status: false
                });
                this.get_data_list(1);
            },

            // url事件
            url_event(e) {
                app.globalData.url_event(e);
            }
        },
    };
</script>
<style>
    @import "./article-category.css";
</style>
