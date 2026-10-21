<template>
	<view :class="theme_view">
		<view class="container">
			<block v-if="loadStatus == 1">
				<!-- DIY 数据已加载 -->
				<block v-if="diyData && diyData.config">
					<component-diy
						:propValue="diyData.config"
						:propDataId="diyData.id"
						:propKey="randomValue"
						@onLocationBack="handleLocationBack">
						<!-- diy组件异步加载、默认先加载骨架屏展示 -->
						<component-no-data propStatus="1" :propLoadingUseSkeleton="true" propPage="home"></component-no-data>
						<!-- 底部内容 -->
						<template slot="diy-bottom-content">
							<component-bottom-line :propStatus="showBottomLine"></component-bottom-line>
						</template>
						<!-- 底部公共 -->
						<template slot="diy-bottom-common">
							<component-common ref="common_footer" :propIsModalBusiness="false"></component-common>
						</template>
					</component-diy>
				</block>
				<!-- DIY 数据为空 -->
				<component-no-data v-else propStatus="0" propPage="home"></component-no-data>
			</block>
			<!-- 加载中 -->
			<component-no-data v-else propStatus="1" propPage="home" :propIsHeader="true"></component-no-data>

			<!-- 底部公共组件 -->
			<component-common ref="common" :propIsFooter="false"></component-common>

			<!-- 快捷导航 -->
			<component-quick-nav :propIsNav="true" :propIsBar="true"></component-quick-nav>
		</view>
	</view>
</template>

<script>
const app = getApp();
import componentDiy from '@/pages/diy/components/diy/diy';
import componentBottomLine from '@/components/bottom-line/bottom-line';
import componentCommon from '@/components/common/common';
import componentNoData from '@/components/no-data/no-data';
import componentQuickNav from '@/components/quick-nav/quick-nav';
import { getHomeDiyPage } from '@/api/diy/index.js';

export default {
	components: {
		componentDiy,
		componentBottomLine,
		componentCommon,
		componentNoData,
		componentQuickNav
	},
	data() {
		return {
			theme_view: getApp().globalData.get_theme_value_view(),
			diyData: null,
			loadStatus: 0, // 0-加载中(骨架屏) 1-完成
			showBottomLine: false,
			randomValue: 0,
			share_info: {},
		};
	},
	onPullDownRefresh() {
		this.loadDiyData({ is_refresh: true });
		setTimeout(() => {
			uni.stopPullDownRefresh();
		}, 1000);
	},
	onLoad(options) {
		if (app.globalData.page_event_onload_handle) {
			app.globalData.page_event_onload_handle(options);
		}
	},
	onShow() {
		app.globalData.page_event_onshow_handle();

		if (!this.diyData && app.globalData.is_init_config_success_pages_begin()) {
			let self = this;
			setTimeout(function () {
				self.loadDiyData();
			}, 500);
		} else {
			this.loadDiyData();
		}

		if (app.globalData.get_config('status') == 1) {
			app.globalData.init_config(0, this, 'init_config', true);
		} else {
			app.globalData.is_config(this, 'init_config');
		}

		if ((this.$refs.common || null) != null) {
			this.$refs.common.on_show({object: this, method: 'init'});
		}
		if ((this.$refs.common_footer || null) != null) {
			this.$refs.common_footer.on_show({object: this, method: 'init'});
		}
	},
	methods: {
		init_config(status) {
		},

		// 根据 DIY 配置设置导航栏颜色
		setNavBarColor() {
			if (this.diyData && this.diyData.config) {
				var functionButtonsType = parseInt(app.globalData.get_key_data(
					this.diyData.config,
					'header.com_data.style.function_buttons_type',
					0
				));
				app.globalData.set_navigation_bar_color(functionButtonsType == 1);
			}
		},

		loadDiyData(params = {}) {
			var self = this;
			var cache_key = app.globalData.data.cache_index_data_key;

			if (!this.diyData && params.is_refresh !== true) {
				var cache_data = uni.getStorageSync(cache_key) || null;
				if (cache_data != null) {
					this.diyData = cache_data;
					this.loadStatus = 1;
					this.showBottomLine = true;
					this.randomValue = Math.random();
					if (this.diyData.name) {
						uni.setNavigationBarTitle({ title: this.diyData.name });
					}
					// 设置导航栏颜色
					this.setNavBarColor();
					// 分享菜单处理
					if (app.globalData && app.globalData.page_share_handle) {
						app.globalData.page_share_handle(self.share_info);
					}
				}
			}

			// 首页配置按客户端、启用状态和时间范围选择。
			getHomeDiyPage().then(res => {
				uni.stopPullDownRefresh();
				if (res.code === '00000' && res.data?.data) {
					this.diyData = res.data.data;
					this.loadStatus = 1;
					this.showBottomLine = true;
					this.randomValue = Math.random();
					uni.setStorageSync(cache_key, res.data.data);
					if (this.diyData.name) {
						uni.setNavigationBarTitle({ title: this.diyData.name });
					}
					// 设置导航栏颜色
					this.setNavBarColor();
					// 分享菜单处理、延时执行，确保基础数据已加载完成
					setTimeout(function() {
						app.globalData.page_share_handle();
					}, 3000);
				} else {
					// 后端未返回 DIY 首页（未配置 diy_home_mode / 定时外 / 页面停用）：
					// 清掉旧缓存与本页旧数据，避免继续展示过期首页
					uni.removeStorageSync(cache_key);
					this.diyData = null;
					this.loadStatus = 1;
				}
			}).catch(err => {
				console.error('加载 DIY 首页数据失败:', err);
				uni.stopPullDownRefresh();
			});
		},

		handleLocationBack() {
			this.loadDiyData({ is_refresh: true });
		}
	}
};
</script>

<style lang="scss" scoped>
page {
	background: #FAF8F5;
}

.container {
	min-height: 100vh;
}
</style>
