<template>
    <!-- 商品详情页-商品问答区块（挂 spu 的已展示问答，快照接口取数） -->
    <view v-if="propSpuId != null && propSpuId !== '' && parseInt(propSpuId) > 0" class="ask-section spacing-mb">
        <view class="spacing-nav-title flex-row align-c jc-sb text-size-xs">
            <view class="title-left flex-row align-c">
                <text class="text-wrapper title-left-border">商品问答</text>
                <text v-if="ask_total > 0" class="cr-grey text-size-xs margin-left-sm">({{ ask_total }})</text>
            </view>
            <text v-if="ask_list.length > 0" @tap="ask_more_event" class="arrow-right padding-right cr-grey cp">查看全部 ></text>
        </view>
        <view class="border-radius-main padding-main bg-white">
            <!-- 问答列表 -->
            <block v-if="ask_list.length > 0">
                <view v-for="(item, index) in ask_list" :key="index" class="ask-section-item" @tap="ask_item_event" :data-value="item.url">
                    <view class="flex-row jc-sb align-c">
                        <view class="flex-1 flex-width ask-section-title text-line-1">{{ item.title }}</view>
                        <view class="ask-section-tag" :class="item.is_reply == 1 ? 'ask-section-tag-reply' : 'ask-section-tag-wait'">{{ item.is_reply == 1 ? '已回' : '未回' }}</view>
                    </view>
                    <view class="flex-row align-c cr-grey-9 text-size-xs margin-top-xs">
                        <text>{{ item.add_time_date }}</text>
                        <view class="fw-b padding-horizontal-xs">·</view>
                        <text>{{ item.access_count || 0 }}人浏览</text>
                    </view>
                </view>
            </block>
            <view v-else-if="ask_load_status == 0 || ask_load_status == 3" class="ask-section-empty cr-grey tc text-size-xs">
                暂无相关问答，快来提问吧
            </view>
            <!-- 提问入口 -->
            <view class="br-t-e padding-top-main cr-base flex-row jc-c align-c" @tap="ask_open_event">
                <iconfont name="icon-edit" size="28rpx" color="#FF3F3F" propClass="margin-right-sm"></iconfont>
                <text>我要提问</text>
                <iconfont name="icon-arrow-right" size="26rpx" color="#999" propClass="margin-left-sm"></iconfont>
            </view>
        </view>

        <!-- 提问弹窗 -->
        <ask-popup ref="ask_popup" :prop-spu-id="propSpuId" :prop-spu-name="propSpuName" :prop-spu-pic="propSpuPic" @onAskSuccess="ask_load_handle"></ask-popup>
    </view>
</template>
<script>
    import askPopup from '@/pages/plugins/ask/components/ask-popup/ask-popup';
    import { listAsks } from '@/api/cms/ask.js';
    import { time_stamp } from '@/common/js/common/common.js';

    export default {
        // 注：prop 一律以驼峰名声明（propSpuId / propSpuName / propSpuPic），
        // 且**在模板表达式里也必须用驼峰名**——Vue 不会为驼峰 prop 生成下划线别名。
        // 早期版本模板里写成了下划线形式，恒为 undefined：根节点 v-if 永远为 false，
        // 商品详情页的问答区块从不渲染，控制台持续报
        // "Property or method 'prop_spu_id' is not defined"。
        // 注意区分两种位置：属性**名**写 :prop-spu-id 是对的（Vue 会自动 kebab→camel 映射），
        // 只有属性**值表达式**里必须写驼峰。
        props: {
            // 商品 SPU ID
            propSpuId: {
                type: [Number, String],
                default: '',
            },
            // 商品名称快照
            propSpuName: {
                type: String,
                default: '',
            },
            // 商品主图快照
            propSpuPic: {
                type: String,
                default: '',
            },
        },
        components: {
            askPopup,
        },
        data() {
            return {
                ask_list: [],
                ask_total: 0,
                // 0 空/无数据, 1 加载中, 2 错误, 3 加载完成
                ask_load_status: 1,
            };
        },
        watch: {
            propSpuId(new_val) {
                if (new_val != null && new_val !== '' && parseInt(new_val) > 0) {
                    this.ask_load_handle();
                }
            },
        },
        created() {
            this.ask_load_handle();
        },
        methods: {
            // 快照行字段适配（mall VO camelCase → 展示字段）
            ask_item_handle(item) {
                var row = item || {};
                var is_reply = row.isReply != null ? row.isReply : (row.is_reply != null ? row.is_reply : 0);
                var ask_id = row.id != null ? row.id : '';
                return {
                    id: ask_id,
                    url: ask_id != null && ask_id !== '' ? '/pages/plugins/ask/detail/detail?id=' + ask_id : '',
                    title: row.title || '',
                    is_reply: Number(is_reply) || 0,
                    add_time_date: time_stamp(row.createTime),
                    access_count: row.accessCount != null ? row.accessCount : (row.access_count || 0),
                };
            },
            // 加载问答列表
            ask_load_handle() {
                var spu_id = parseInt(this.propSpuId || 0);
                if (spu_id <= 0) {
                    return false;
                }
                var self = this;
                this.setData({
                    ask_load_status: 1,
                });
                listAsks({
                    pageNum: 1,
                    pageSize: 3,
                    spuId: spu_id,
                }).then(function(res) {
                    if (res.code === '00000') {
                        var page_data = res.data || null;
                        var page_list = (page_data != null && page_data.list != null) ? page_data.list : [];
                        var data_list = [];
                        for (var i = 0; i < page_list.length; i++) {
                            data_list.push(self.ask_item_handle(page_list[i]));
                        }
                        self.setData({
                            ask_list: data_list,
                            ask_total: (page_data != null && page_data.total != null) ? page_data.total : data_list.length,
                            ask_load_status: data_list.length > 0 ? 3 : 0,
                        });
                    } else {
                        self.setData({
                            ask_load_status: 2,
                        });
                    }
                }).catch(function() {
                    self.setData({
                        ask_load_status: 2,
                    });
                });
            },
            // 提问入口
            ask_open_event() {
                if ((this.$refs.ask_popup || null) != null) {
                    this.$refs.ask_popup.open();
                }
            },
            // 查看全部
            ask_more_event() {
                uni.navigateTo({
                    url: '/pages/plugins/ask/list/list?spuId=' + this.propSpuId + '&spuName=' + encodeURIComponent(this.propSpuName || '') + '&spuPic=' + encodeURIComponent(this.propSpuPic || ''),
                });
            },
            // 问答详情跳转
            ask_item_event(e) {
                var url = e.currentTarget.dataset.value;
                if (url != null && url != '') {
                    uni.navigateTo({
                        url: url,
                    });
                }
            },
        },
    };
</script>
<style scoped>
    .ask-section-item {
        padding: 20rpx 0;
        border-bottom: 2rpx solid #f5f5f5;
    }
    .ask-section-item:last-of-type {
        border-bottom: 0;
    }
    .ask-section-title {
        font-size: 28rpx;
        color: #333;
        margin-right: 20rpx;
    }
    .ask-section-tag {
        flex-shrink: 0;
        font-size: 22rpx;
        padding: 4rpx 12rpx;
        border-radius: 6rpx;
        line-height: 1.4;
    }
    .ask-section-tag-reply {
        color: #10BB78;
        background: rgba(16, 187, 120, 0.1);
    }
    .ask-section-tag-wait {
        color: #E3A51B;
        background: rgba(227, 165, 27, 0.12);
    }
    .ask-section-empty {
        padding-top: 40rpx;
        padding-bottom: 40rpx;
    }
</style>
