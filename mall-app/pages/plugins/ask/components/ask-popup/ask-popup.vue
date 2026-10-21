<template>
    <!-- 商品问答-我要提问弹窗（登录会员提问；提交后运营确认/回复后展示） -->
    <uni-popup ref="popup" type="center" :mask-click="false">
        <view class="ask-popup-container">
            <view class="ask-popup-head flex-row jc-sb align-c">
                <text class="ask-popup-title">我要提问</text>
                <iconfont name="icon-close-round" size="32rpx" color="#999" @tap="popup_close_event"></iconfont>
            </view>
            <view v-if="(propSpuName || null) != null && propSpuName != ''" class="ask-popup-goods single-text">
                <text class="ask-popup-goods-label">商品：</text>
                <text class="ask-popup-goods-name">{{ propSpuName }}</text>
            </view>
            <view class="ask-popup-form">
                <input class="ask-popup-input" v-model="ask_title" maxlength="60" placeholder="请输入问题标题（必填）" placeholder-class="ask-popup-placeholder" />
                <textarea class="ask-popup-textarea" v-model="ask_content" maxlength="500" placeholder="请详细描述您的问题（选填）" placeholder-class="ask-popup-placeholder"></textarea>
            </view>
            <view class="ask-popup-foot flex-row jc-e align-c">
                <button class="ask-popup-btn ask-popup-btn-cancel" type="default" size="mini" @tap="popup_close_event">取消</button>
                <button class="ask-popup-btn ask-popup-btn-submit bg-main cr-white br-main" type="default" size="mini" :loading="ask_submit_loading" @tap="ask_submit_event">提交问题</button>
            </view>
        </view>
    </uni-popup>
</template>
<script>
    const app = getApp();
    import { submitAsk } from '@/api/cms/ask.js';
    export default {
        props: {
            // 商品 SPU ID（提问必传）
            propSpuId: {
                type: [Number, String],
                default: '',
            },
            // 商品名称快照（提交方携带）
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
        data() {
            return {
                ask_title: '',
                ask_content: '',
                ask_submit_loading: false,
            };
        },
        methods: {
            // 打开弹窗
            open() {
                if ((this.$refs.popup || null) != null) {
                    this.$refs.popup.open();
                }
            },
            // 关闭弹窗
            close() {
                if ((this.$refs.popup || null) != null) {
                    this.$refs.popup.close();
                }
            },
            // 弹窗关闭事件
            popup_close_event() {
                this.close();
            },
            // 重置表单
            reset_form() {
                this.setData({
                    ask_title: '',
                    ask_content: '',
                });
            },
            // 登录状态判断
            is_login_status() {
                var token = uni.getStorageSync('token') || '';
                if (token == '') {
                    return false;
                }
                var member_id = app.globalData.get_user_cache_info('memberId', 0);
                return member_id != null && member_id !== '' && parseInt(member_id) > 0;
            },
            // 提交提问
            ask_submit_event() {
                if (this.ask_submit_loading) {
                    return false;
                }
                // 未登录 → 跳登录（登录返回后弹窗保留草稿，可再次提交）
                if (!this.is_login_status()) {
                    app.globalData.get_user_info(this, 'ask_popup_submit_event', null);
                    return false;
                }
                var title = (this.ask_title || '').trim();
                if (title == '') {
                    app.globalData.showToast('请输入问题标题');
                    return false;
                }
                var spu_id = parseInt(this.propSpuId || 0);
                if (spu_id <= 0) {
                    app.globalData.showToast('商品信息有误，暂无法提问');
                    return false;
                }
                var self = this;
                this.setData({
                    ask_submit_loading: true,
                });
                submitAsk({
                    spuId: spu_id,
                    spuName: this.propSpuName || '',
                    spuPic: this.propSpuPic || '',
                    title: title,
                    content: (this.ask_content || '').trim(),
                }).then(function(res) {
                    self.setData({
                        ask_submit_loading: false,
                    });
                    if (res.code === '00000') {
                        app.globalData.showToast('提问成功，运营审核后展示', 'success');
                        self.reset_form();
                        self.close();
                        self.$emit('onAskSuccess');
                    }
                }).catch(function() {
                    self.setData({
                        ask_submit_loading: false,
                    });
                });
            },
        },
    };
</script>
<style scoped>
    .ask-popup-container {
        width: 600rpx;
        background: #fff;
        border-radius: 16rpx;
        padding: 30rpx;
        box-sizing: border-box;
    }
    .ask-popup-head {
        margin-bottom: 20rpx;
    }
    .ask-popup-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
    }
    .ask-popup-goods {
        background: #f6f6f6;
        border-radius: 8rpx;
        padding: 12rpx 16rpx;
        margin-bottom: 20rpx;
        font-size: 24rpx;
        color: #666;
    }
    .ask-popup-goods-label {
        color: #999;
    }
    .ask-popup-goods-name {
        color: #333;
    }
    .ask-popup-form {
        width: 100%;
    }
    .ask-popup-input {
        width: 100%;
        box-sizing: border-box;
        height: 80rpx;
        line-height: 80rpx;
        border: 2rpx solid #e5e5e5;
        border-radius: 8rpx;
        padding: 0 20rpx;
        margin-bottom: 20rpx;
        font-size: 28rpx;
        color: #333;
    }
    .ask-popup-textarea {
        width: 100%;
        box-sizing: border-box;
        height: 180rpx;
        border: 2rpx solid #e5e5e5;
        border-radius: 8rpx;
        padding: 16rpx 20rpx;
        font-size: 28rpx;
        color: #333;
    }
    .ask-popup-placeholder {
        color: #999;
        font-size: 26rpx;
    }
    .ask-popup-foot {
        margin-top: 30rpx;
        width: 100%;
        justify-content: flex-end;
    }
    .ask-popup-btn {
        margin-left: 20rpx;
        padding: 0 40rpx;
    }
    .ask-popup-btn-cancel {
        background: #f2f2f2;
        color: #666;
    }
</style>
