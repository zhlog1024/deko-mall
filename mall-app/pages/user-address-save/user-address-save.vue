<template>
    <view :class="theme_view">
        <view v-if="data_list_loding_status === 3">
            <view class="form-container padding-main oh">
                <!-- 联系人信息 -->
                <view class="border-radius-main bg-white spacing-mb oh">
                    <view class="form-gorup">
                        <view class="flex-row jc-sb align-c br-b-f5 padding-bottom-sm">
                            <view class="form-gorup-title">{{ $t('user-address-save-page.user-address-save-page.contact') }}<text class="form-group-tips-must">*</text></view>
                            <input type="text" v-model="address_data.consigneeName" maxlength="16" placeholder-class="cr-grey-9" class="cr-base flex-1 flex-width" :placeholder="$t('user-address-save-page.user-address-save-page.contact_placeholder')" />
                        </view>
                        <view class="flex-row jc-sb align-c padding-top-sm">
                            <view class="form-gorup-title">{{ $t('user-address-save-page.user-address-save-page.phone') }}<text class="form-group-tips-must">*</text></view>
                            <input type="text" v-model="address_data.consigneeMobile" :maxlength="(!enable_international_phone || country_id === 778937) ? 11 : 20" placeholder-class="cr-grey-9" class="cr-base flex-1 flex-width" :placeholder="$t('user-address-save-page.user-address-save-page.phone_placeholder')" />
                        </view>
                    </view>
                </view>

                <!-- 地区 -->
                <view class="border-radius-main bg-white spacing-mb oh">
                    <view class="form-gorup">
                        <!-- 国家（国际化开启时显示） -->
                        <view v-if="enable_international_phone == 1" class="flex-row jc-sb align-c br-b-f5 padding-vertical-sm">
                            <view class="form-gorup-title">{{ $t('user-address-save.user-address-save.country') }}<text class="form-group-tips-must">*</text></view>
                            <view class="flex-1 flex-width">
                                <picker mode="selector" :range="country_list" range-key="name" :value="country_index" @change="country_change">
                                    <view class="single-text">
                                        <text v-if="country_name">{{ country_name }}</text>
                                        <text v-else class="cr-grey-9">{{ $t('user-address-save.user-address-save.select_country') }}</text>
                                    </view>
                                </picker>
                            </view>
                        </view>
                        <!-- 省市区 -->
                        <block v-if="is_user_address_forbid_choice_region == 0">
                        <view class="flex-row jc-sb align-c br-b-f5 padding-vertical-sm">
                            <view class="form-gorup-title">{{$t('user-address-save.user-address-save.h8f148')}}<text class="form-group-tips-must">*</text></view>
                            <view class="flex-1 flex-width">
                                <view @tap="open_region" class="single-text">
                                    <text v-if="province_name">{{ province_name }}{{ city_name ? '-' + city_name : '' }}{{ county_name ? '-' + county_name : '' }}</text>
                                    <text v-else class="cr-grey-9">{{ $t('user-address-save-page.user-address-save-page.select_region') }}</text>
                                </view>
                                <region-picker
                                    v-model="region_picker_show"
                                    :propCountryId="country_id || ''"
                                    :propProvinceId="province_id || ''"
                                    :propCityId="city_id || ''"
                                    :propCountyId="county_id || ''"
                                    @change="region_event">
                                </region-picker>
                            </view>
                        </view>
                        </block>
                        <!-- 禁止手动选择省市区时，仅展示地址。 -->
                        <block v-else>
                        <view class="flex-row jc-sb align-c br-b-f5 padding-vertical-sm">
                            <view class="form-gorup-title">{{$t('user-address-save.user-address-save.h8f148')}}<text class="form-group-tips-must">*</text></view>
                            <view class="flex-1 flex-width">
                                <view class="single-text">
                                    <text v-if="province_name">{{ province_name }}{{ city_name ? '-' + city_name : '' }}{{ county_name ? '-' + county_name : '' }}</text>
                                    <text v-else class="cr-grey-9">{{ $t('user-address-save-page.user-address-save-page.select_region') }}</text>
                                </view>
                            </view>
                        </view>
                        </block>
                        <view class="flex-row jc-sb align-start br-b-f5 padding-vertical-sm">
                            <view class="form-gorup-title">{{$t('user-address-save.user-address-save.4rc269')}}<text class="form-group-tips-must">*</text></view>
                            <view class="flex-1 flex-width flex-row align-start">
                                <textarea v-model="address_data.detailAddress" maxlength="80" placeholder-class="cr-grey-9" class="cr-base bs-bb flex-1 address-textarea" :placeholder="$t('user-address-save-page.user-address-save-page.detail_placeholder')" auto-height />
                                <view v-if="home_user_address_map_status == 1" class="padding-left-sm padding-top-sm" @tap="choose_user_location_event">
                                    <iconfont name="icon-location" size="36rpx" color="#ADADAD"></iconfont>
                                </view>
                            </view>
                        </view>
                        <!-- 智能地址识别，由 is_user_address_discern 控制。 -->
                        <view v-if="is_user_address_discern == 1" class="pr padding-top-sm">
                            <view class="intelligent-identification oh" :style="identfication_show ? 'height:254rpx;' : 'height:0;'">
                                <view class="bg-base border-radius-main padding-main oh">
                                    <textarea @input="address_discern_value_event" :value="address_discern_value" class="margin-0" :placeholder="$t('user-address-save-page.user-address-save-page.paste_hint')" placeholder-class="cr-grey" />
                                    <view class="padding-top-sm tr" :class="identfication_show ? '' : 'dis-none'">
                                        <button type="default" size="mini" class="btn round cr-base text-size-xs margin-right-sm padding-0 tc" @tap="clear_discern_submit_event">{{ $t('user-address-save-page.user-address-save-page.clear') }}</button>
                                        <button type="default" size="mini" class="btn round bg-main cr-white text-size-xs padding-0 tc" @tap="address_discern_submit_event">{{ $t('user-address-save-page.user-address-save-page.recognize') }}</button>
                                    </view>
                                </view>
                            </view>
                            <view class="tc padding-top-main" @tap="identfication_change">
                                <text class="padding-right-sm">{{ $t('user-address-save-page.user-address-save-page.address_clipboard') }}</text>
                                <iconfont :name="identfication_show ? 'icon-arrow-top' : 'icon-arrow-bottom'" size="28rpx" color="#666"></iconfont>
                            </view>
                        </view>
                    </view>
                </view>

                <!-- 地址别名 -->
                <view class="border-radius-main bg-white spacing-mb oh">
                    <view class="form-gorup">
                        <view class="flex-row br-b-f5 padding-top-sm padding-bottom-main">
                            <view class="form-gorup-title padding-top-sm alias-title">{{$t('user-address-save.user-address-save.231435')}}</view>
                            <view class="flex-1 flex-width">
                                <view class="flex-row">
                                    <view v-for="(item, index) in alias_list" :key="index" class="alias-btn" :class="item === (address_data.alias || null) ? 'bg-main cr-white br-main' : 'bg-white cr-black br-grey-d'" :data-value="item" @tap="alias_change">{{ item }}</view>
                                </view>
                                <view class="padding-top-lg label-edit-right">
                                    <view v-if="alias_eidt_bool" class="flex-row align-c alias-add pr">
                                        <input type="text" v-model="alias_eidt_text" maxlength="16" placeholder-class="cr-grey-9" class="input-alias cr-base dis-inline-block bg-base text-size-xs padding-left-sm va-m" :placeholder="$t('user-address-save-page.user-address-save-page.custom_tag_hint')" />
                                        <button type="default" size="mini" class="btn round cr-white text-size-xs pa" :class="(alias_eidt_text || null) !== null ? 'br-main bg-main' : 'bg-grey br-grey'" @tap="alias_add_submit_event">{{ ('user-address-save-page.user-address-save-page.save') }}</button>
                                    </view>
                                    <view v-else>
                                        <view v-if="alias_eidt_text">
                                            <view class="flex-row align-c alias-edit">
                                                <button type="default" size="mini" class="btn text-size-xs" :class="alias_eidt_text === (address_data.alias || '') ? 'bg-main cr-white br-main' : 'bg-white cr-black br-grey-d'" @tap="alias_checked_change">{{ alias_eidt_text }}</button>
                                                <button type="default" size="mini" class="btn bg-main-pair cr-white br-main-pair text-size-xs" @tap="alias_edit_submit_event">{{ $t('user-address-save-page.user-address-save-page.edit_tag') }}</button>
                                            </view>
                                        </view>
                                        <view v-else class="alias-btn bg-white cr-black br-grey-d" @tap="alias_add">
                                            <iconfont name="icon-add-wide" size="28rpx"></iconfont>
                                        </view>
                                    </view>
                                    <input type="hidden" name="alias" :value="address_data.alias" class="dis-none" />
                                </view>
                            </view>
                        </view>
                        <view class="flex-row jc-sb align-c padding-top-main">
                            <view class="form-gorup-title default-title">{{$t('user-address-save.user-address-save.g43klm')}}</view>
                            <switch :color="theme_color" :checked="(address_data.defaulted || 0) == 1 ? true : false" @change="default_ads_change_event" />
                        </view>
                    </view>
                </view>

                <!-- 操作按钮 -->
                <view class="padding-vertical-main margin-top-sm spacing-mb">
                    <view class="bottom-line-exclude oh">
                        <button class="btn cr-white round text-size" type="default" hover-class="none" :disabled="form_submit_disabled_status" :style="btn_bg_color" @tap="form_submit">{{ $t('user-address-save-page.user-address-save-page.save') }}</button>
                        <button v-if="params && (params.id || null) !== null && params.id !== '0'" class="btn round bg-white cr-main br-main spacing-mt" type="default" @tap="address_delete_event" hover-class="none">{{ $t('user-address-save-page.user-address-save-page.delete_addr') }}</button>
                    </view>
                </view>
            </view>
        </view>
        <view v-else>
            <component-no-data :propStatus="data_list_loding_status"></component-no-data>
        </view>

        <!-- 位置选择 -->
        <component-choice-location ref="choice_location" :propIsShowAddressChoice="false" @onBack="user_back_choice_location"></component-choice-location>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>

<script>
    const app = getApp();
    import componentCommon from '@/components/common/common';
    import componentNoData from '@/components/no-data/no-data';
    import componentChoiceLocation from '@/components/choice-location/choice-location';
    import regionPicker from '@/components/region-picker/region-picker';
    import iconfont from '@/components/iconfont/iconfont';
    import { add, update, del, parseAddress } from '@/api/ums/address.js';
    import { findAllCountry } from '@/api/user';

    const theme_color = app.globalData.get_theme_color();
    const theme_color_light = app.globalData.get_theme_color(null, true);

    export default {
        data() {
            return {
                theme_view: app.globalData.get_theme_value_view(),
                theme_color: theme_color,
                btn_bg_color: 'background:linear-gradient(107deg, ' + theme_color_light + ' 0%, ' + theme_color + ' 100%)',
                params: {},
                data_list_loding_status: 1,
                enable_international_phone: 0,
                // 地图选址由 home_user_address_map_status 控制。
                home_user_address_map_status: 0,
                is_user_address_location_discern: 0,
                is_user_address_discern: 0,
                is_user_address_forbid_choice_region: 0,
                international_address_countries: [],
                address_data: {
                    countryId: '',
                    countryName: '',
                    consigneeName: '',
                    consigneeMobile: '',
                    provinceId: '',
                    cityId: '',
                    areaId: '',
                    provinceName: '',
                    cityName: '',
                    areaName: '',
                    detailAddress: '',
                    defaulted: 0,
                    alias: '',
                    lng: null,
                    lat: null
                },
                // 省市区（含国家，由 region-picker 统一管理）
                country_id: 778937,  // 默认中国
                country_name: '中国',
                province_id: null,
                city_id: null,
                county_id: null,
                province_name: '',
                city_name: '',
                county_name: '',
                user_location: null,
                address_discern_value: '',
                form_submit_disabled_status: false,
                // 省市区选择器
                region_picker_show: false,
                country_list: [],
                country_index: 0,
                                // 智能识别
                identfication_show: false,
                // 别名
                alias_list: [this.$t('user-address-save.user-address-save.t1loj5'), this.$t('user-address-save.user-address-save.6t396c'), this.$t('user-address-save.user-address-save.34rqp8')],
                alias_eidt_bool: false,
                alias_eidt_text: '',
            };
        },

        components: {
            componentCommon,
            componentNoData,
            componentChoiceLocation,
            regionPicker,
            iconfont,
        },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            this.setData({ params: params || {} });

            uni.setNavigationBarTitle({
                title: '收货地址' + ((this.params.id || null) == null ? '新增' : '编辑'),
            });

            // 清除位置缓存信息
            this.choose_user_location_remove();

            // 配置就绪后渲染表单。
            this.init_config();

            if ((this.$refs.common || null) != null) {
                this.$refs.common.on_show();
            }
        },

        onShow() {
            app.globalData.page_event_onshow_handle();
            // 从地图页返回后恢复位置。
            this.user_location_init();
            app.globalData.page_share_handle();
        },

        methods: {
            // 配置就绪后读取并渲染表单。
            init_config(status) {
                if ((status || false) == true) {
                    this.read_international_config();
                    this.read_address_map_config();
                    this.init();
                } else {
                    app.globalData.is_config(this, 'init_config');
                }
            },

            init() {
                this.get_data();
            },

            get_data() {
                var id = this.params.id || null;
                if (id == null || id == '0') {
                    this.setData({
                        data_list_loding_status: 3,
                    });
                    this.load_country_list();
                    return;
                }

                // 从 list API 获取单个地址数据（编辑模式）
                var memberId = app.globalData.get_user_cache_info('id', 0) || app.globalData.get_user_cache_info('memberId', 0);
                var self = this;
                import('@/api/ums/address.js').then(function(api) {
                    api.list(memberId).then(function(res) {
                        if (res.code == '00000') {
                            var data_list = res.data || [];
                            var ads_data = null;
                            for (var i in data_list) {
                                if (data_list[i].id == id) {
                                    ads_data = data_list[i];
                                    break;
                                }
                            }

                            var alias = '';
                            if (ads_data) {
                                alias = ads_data.alias || '';
                            }

                            self.setData({
                                address_data: ads_data || {},
                                data_list_loding_status: 3,
                                country_id: (ads_data && ads_data.countryId) || 778937,
                                country_name: (ads_data && ads_data.countryName) || 'China',
                            });
                            

                            if (self.alias_list.indexOf(alias) === -1) {
                                self.setData({ alias_eidt_text: alias || '' });
                            }

                            if (ads_data != null) {
                                self.setData({
                                    country_id: (ads_data && ads_data.countryId) || 778937,
                                    country_name: (ads_data && ads_data.countryName) || 'China',
                                    province_id: ads_data.provinceId || null,
                                    city_id: ads_data.cityId || null,
                                    county_id: ads_data.areaId || null,
                                    city_name: ads_data.cityName || '',
                                    province_name: ads_data.provinceName || '',
                                    county_name: ads_data.areaName || '',
                                });

                                // 地理位置
                                var lng = ads_data.lng || 0;
                                var lat = ads_data.lat || 0;
                                if (lng != 0 && lat != 0) {
                                    self.setData({
                                        user_location: {
                                            lng: lng,
                                            lat: lat,
                                            address: ads_data.detailAddress || '',
                                        },
                                    });
                                }
                            }
                        } else {
                            if (app.globalData.is_login_check(res)) {
                                app.globalData.showToast(res.msg);
                            }
                        }
                    }).catch(function() {
                        app.globalData.showToast('获取地址数据失败');
                    });
                });
            },

            // 选择地理位置
            choose_user_location_event(e) {
                if ((this.$refs.choice_location || null) != null) {
                    this.$refs.choice_location.choose_user_location();
                }
            },

            // 清除位置缓存信息
            choose_user_location_remove() {
                app.globalData.choice_user_location_remove();
            },

            // 检测到地图选址结果后，更新地址字段。
            user_back_choice_location(e) {
                if (e && e.status == 1) {
                    this.setData({ user_location: e });
                    this.apply_map_location(e);
                }
            },

            // 地址信息初始化（onShow 时从 Storage 恢复）
            user_location_init() {
                var loc = app.globalData.choice_user_location_init();
                if (loc && loc.status == 1) {
                    this.apply_map_location(loc);
                }
            },

            // 选址后的统一处理：保存坐标 + 解析地址 + 回写省市区和详细地址
            apply_map_location(loc) {
                // name (POI名称) + address (完整地址) 拼接为详细地址
                var parts = [];
                if (loc.name) parts.push(loc.name);
                if (loc.address && loc.address !== loc.name) parts.push(loc.address);
                var full_address = parts.join(' ');

                var update_data = {
                    user_location: loc,
                    'address_data.lng': loc.lng || null,
                    'address_data.lat': loc.lat || null,
                    'address_data.detailAddress': full_address
                };
                this.setData(update_data);

                // 调后端 parseAddress API 解析省市区（受 is_user_address_location_discern 配置控制）
                // 只传地图返回的 address（标准行政区划地址），不传 name（POI名称会干扰解析）
                var self = this;
                if (this.is_user_address_location_discern != 1) {
                    return;
                }
                var raw = loc.address || '';
                if (!raw) return;
                parseAddress(raw).then(function(res) {
                    if (res.code == '00000' && res.data) {
                        var data = res.data;
                        var set_obj = {};
                        if (data.province) {
                            set_obj['province_id'] = data.province;
                            set_obj['province_name'] = data.province_name;
                            self.address_data.provinceId = data.province;
                            self.address_data.provinceName = data.province_name;
                        }
                        if (data.city) {
                            set_obj['city_id'] = data.city;
                            set_obj['city_name'] = data.city_name;
                            self.address_data.cityId = data.city;
                            self.address_data.cityName = data.city_name;
                        }
                        if (data.county) {
                            set_obj['county_id'] = data.county;
                            set_obj['county_name'] = data.county_name;
                            self.address_data.areaId = data.county;
                            self.address_data.areaName = data.county_name;
                        }
                        // 解析后的街道地址在前，地图 POI 名称在后
                        if (data.address) {
                            var parsed = loc.name ? data.address + ' ' + loc.name : data.address;
                            set_obj['address_data.detailAddress'] = parsed;
                            self.address_data.detailAddress = parsed;
                        }
                        if (Object.keys(set_obj).length > 0) {
                            self.setData(set_obj);
                        }
                    }
                }).catch(function() {
                    // 解析失败保留拼接结果
                });
            },

            // 加载国家列表
            // 读取国际化配置
            read_international_config() {
                var raw = app.globalData.get_config('enable_international_phone', 0);
                var val = parseInt(raw) || 0;
                console.log('[addr] enable_international_phone raw:', raw, 'parsed:', val);
                this.setData({
                    enable_international_phone: val,
                    international_address_countries: app.globalData.get_config('international_address_countries', []) || [],
                });
            },

            // 读取地图选址配置
            read_address_map_config() {
                this.setData({
                    home_user_address_map_status: parseInt(app.globalData.get_config('home_user_address_map_status', 1)) || 0,
                    is_user_address_location_discern: parseInt(app.globalData.get_config('is_user_address_location_discern', 1)) || 0,
                    is_user_address_discern: parseInt(app.globalData.get_config('is_user_address_discern', 1)) || 0,
                    is_user_address_forbid_choice_region: parseInt(app.globalData.get_config('is_user_address_forbid_choice_region', 0)) || 0,
                });
            },

            load_country_list() {
                if (!this.enable_international_phone) {
                    // 国际化关闭，默认中国，不加载列表
                    this.setData({ country_id: 778937, country_name: 'China' });
                    return;
                }
                findAllCountry().then(res => {
                    if (res.code === '00000' && res.data) {
                        var all = res.data;
                        var whitelist = this.international_address_countries || [];
                        // 按白名单过滤（未配白名单则显示全部）
                        var list = whitelist.length > 0
                            ? all.filter(function(c) { return whitelist.indexOf(c.name) !== -1; })
                            : all;
                        var idx = 0;
                        list.forEach(function(item, i) {
                            if (item.id == this.country_id) idx = i;
                        }.bind(this));
                        this.setData({ country_list: list, country_index: idx });
                    }
                }).catch(err => {
                    console.log('[load_country_list] error:', err);
                });
            },
            // 国家变更
            country_change(e) {
                var idx = e.detail.value;
                var item = this.country_list[idx];
                if (item) {
                    this.setData({
                        country_id: item.id,
                        country_name: item.name,
                        country_index: idx,
                        province_id: null,
                        city_id: null,
                        county_id: null,
                        province_name: '',
                        city_name: '',
                        county_name: '',
                    });
                }
            },
            // 省市区选择器
            open_region(e) {
                this.setData({ region_picker_show: true });
            },
            region_event(data) {
                if (data && data.ids && data.names) {
                    this.setData({
                        province_id: data.ids[0] || null,
                        city_id: data.ids[1] || null,
                        county_id: data.ids[2] || null,
                        province_name: data.names[0] || '',
                        city_name: data.names[1] || '',
                        county_name: data.names[2] || '',
                    });
                }
            },

            // 默认地址设置改变
            default_ads_change_event(e) {
                var temp_ads = this.address_data;
                temp_ads['defaulted'] = (temp_ads.defaulted || 0) == 1 ? 0 : 1;
                this.setData({ address_data: temp_ads });
            },

            // 地址信息识别输入事件
            address_discern_value_event(e) {
                this.setData({ address_discern_value: e.detail.value });
            },

            // 地址信息识别确认事件
            address_discern_submit_event(e) {
                if ((this.address_discern_value || null) == null) {
                    app.globalData.showToast('请先输入地址信息');
                    return false;
                }
                this.address_discern_handle(this.address_discern_value);
            },
            clear_discern_submit_event() {
                this.setData({ address_discern_value: '' });
            },

            // 地址识别处理
            address_discern_handle(raw_text) {
                uni.showLoading({ title: '处理中...', mask: true });
                parseAddress(raw_text).then((res) => {
                    uni.hideLoading();
                    if (res.code == '00000') {
                        var data = res.data;
                        var temp_ads = this.address_data || {};
                        if (data.name) temp_ads.consigneeName = data.name;
                        if (data.tel) temp_ads.consigneeMobile = data.tel;
                        if (data.province) {
                            temp_ads.provinceId = data.province;
                            temp_ads.provinceName = data.province_name;
                            this.setData({
                                province_id: data.province,
                                province_name: data.province_name,
                            });
                        }
                        if (data.city) {
                            temp_ads.cityId = data.city;
                            temp_ads.cityName = data.city_name;
                            this.setData({
                                city_id: data.city,
                                city_name: data.city_name,
                            });
                        }
                        if (data.county) {
                            temp_ads.areaId = data.county;
                            temp_ads.areaName = data.county_name;
                            this.setData({
                                county_id: data.county,
                                county_name: data.county_name,
                            });
                        }
                        if (data.address) temp_ads.detailAddress = data.address;

                        // 清除位置缓存（识别后坐标可能与地址不符）
                        temp_ads['lng'] = '';
                        temp_ads['lat'] = '';
                        this.setData({
                            address_data: temp_ads,
                            user_location: null,
                        });
                        this.choose_user_location_remove();

                        app.globalData.showToast('识别完成', 'success');
                    } else {
                        app.globalData.showToast(res.msg || '识别失败，请重试');
                    }
                }).catch(() => {
                    uni.hideLoading();
                    app.globalData.showToast('网络异常，请重试');
                });
            },

            // 地址粘贴板展开
            identfication_change() {
                this.setData({ identfication_show: !this.identfication_show });
            },

            // 别名
            alias_change(e) {
                var val = e.currentTarget.dataset.value || null;
                this.$set(this.address_data, 'alias', val);
                this.setData({ alias_eidt_bool: false });
            },
            alias_checked_change() {
                this.$set(this.address_data, 'alias', this.alias_eidt_text);
            },
            alias_add(e) {
                this.setData({ alias_eidt_bool: true });
            },
            alias_add_submit_event() {
                this.setData({ alias_eidt_bool: false });
                this.$set(this.address_data, 'alias', this.alias_eidt_text);
            },
            alias_edit_submit_event() {
                this.setData({ alias_eidt_bool: true });
            },

            // 数据提交
            form_submit(e) {
                var form_data = this.address_data || {};

                // 联系人信息
                form_data.consigneeName = form_data.consigneeName || '';
                form_data.consigneeMobile = form_data.consigneeMobile || '';

                // 国家
                form_data.countryId = this.country_id || 778937;
                form_data.countryName = this.country_name || 'China';
                // 省市区
                form_data.provinceId = this.province_id;
                form_data.cityId = this.city_id;
                form_data.areaId = this.county_id;
                form_data.provinceName = this.province_name;
                form_data.cityName = this.city_name;
                form_data.areaName = this.county_name;

                // 地理位置
                var lng = form_data.lng || 0;
                var lat = form_data.lat || 0;
                if ((this.user_location || null) != null) {
                    lng = this.user_location.lng || 0;
                    lat = this.user_location.lat || 0;
                }
                form_data.lng = lng;
                form_data.lat = lat;

                // 数据校验
                if (!form_data.consigneeName) {
                    app.globalData.showToast('请填写联系人姓名');
                    return false;
                }
                var mobilePattern = (!this.enable_international_phone || (this.country_id === 778937)) ? /^1[3-9]\d{9}$/ : /.+/;
                if (!mobilePattern.test(form_data.consigneeMobile)) {
                    app.globalData.showToast('请输入正确的手机号');
                    return false;
                }
                if (!form_data.provinceId && !form_data.cityId) {
                    app.globalData.showToast('请选择省市区');
                    return false;
                }
                if (!form_data.detailAddress) {
                    app.globalData.showToast('请填写详细地址');
                    return false;
                }

                this.setData({ form_submit_disabled_status: true });
                uni.showLoading({ title: '保存中...' });

                var id = this.params.id || null;
                if (id && id !== '0') {
                    update(id, form_data).then((res) => {
                        uni.hideLoading();
                        this.setData({ form_submit_disabled_status: false });
                        if (res.code == '00000') {
                            app.globalData.showToast('地址修改成功', 'success');
                            this.back_prev_event();
                        } else {
                            app.globalData.showToast(res.msg || '保存失败');
                        }
                    }).catch(() => {
                        uni.hideLoading();
                        this.setData({ form_submit_disabled_status: false });
                        app.globalData.showToast('网络异常，请重试');
                    });
                } else {
                    add(form_data).then((res) => {
                        uni.hideLoading();
                        this.setData({ form_submit_disabled_status: false });
                        if (res.code == '00000') {
                            app.globalData.showToast('地址添加成功', 'success');
                            this.back_prev_event();
                        } else {
                            app.globalData.showToast(res.msg || '保存失败');
                        }
                    }).catch(() => {
                        uni.hideLoading();
                        this.setData({ form_submit_disabled_status: false });
                        app.globalData.showToast('网络异常，请重试');
                    });
                }
            },

            // 删除地址
            address_delete_event() {
                var value = this.params.id || null;
                if (value == null) {
                    app.globalData.showToast('地址ID异常');
                    return false;
                }
                uni.showModal({
                    title: '温馨提示',
                    content: '确定要删除该收货地址吗？',
                    confirmText: '确定',
                    cancelText: '取消',
                    success: (result) => {
                        if (result.confirm) {
                            uni.showLoading({ title: '删除中...' });
                            del(value).then((res) => {
                                uni.hideLoading();
                                if (res.code == '00000') {
                                    app.globalData.showToast('删除成功', 'success');
                                    this.back_prev_event();
                                } else {
                                    app.globalData.showToast(res.msg || '删除失败');
                                }
                            }).catch(() => {
                                uni.hideLoading();
                                app.globalData.showToast('网络异常，请重试');
                            });
                        }
                    },
                });
            },

            back_prev_event() {
                var pages = app.globalData.prev_page();
                if (pages) {
                    setTimeout(function() {
                        uni.navigateBack();
                    }, 1000);
                } else {
                    setTimeout(function() {
                        app.globalData.url_open('/pages/user-address/user-address', true);
                    }, 1000);
                }
            },
        },
    };
</script>
<style>
    @import './user-address-save.css';
</style>
