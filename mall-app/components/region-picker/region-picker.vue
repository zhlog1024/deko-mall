<template>
    <view :class="theme_view">
        <component-popup :propShow="value" propPosition="bottom" @onclose="popup_close_event">
            <view class="flex-row jc-sb align-c padding-main">
                <text class="cr-grey" @tap="popup_close_event">{{ $t('common.cancel') }}</text>
                <text class="cr-blue" @tap="sub_region_event">{{ $t('common.confirm') }}</text>
            </view>
            <view class="g-dp-ctt-wrapper">
                <picker-view class="picker-view" :style="col_style" :indicator-style="indicatorStyle" :value="columns_index[0]" data-column="0" @change="changeHandler">
                    <picker-view-column>
                        <view class="g-dp-ctt-wp-item" v-for="(item, a) in columns[0]" :key="item.id">{{ item.name }}</view>
                    </picker-view-column>
                </picker-view>

                <picker-view v-if="columns[1].length > 0" class="picker-view" :style="col_style" :indicator-style="indicatorStyle" :value="columns_index[1]" data-column="1" @change="changeHandler">
                    <picker-view-column>
                        <view class="g-dp-ctt-wp-item" v-for="(item, b) in columns[1]" :key="item.id">{{ item.name }}</view>
                    </picker-view-column>
                </picker-view>

                <picker-view v-if="columns[2].length > 0" class="picker-view" :style="col_style" :indicator-style="indicatorStyle" :value="columns_index[2]" data-column="2" @change="changeHandler">
                    <picker-view-column>
                        <view class="g-dp-ctt-wp-item" v-for="(item, c) in columns[2]" :key="item.id">{{ item.name }}</view>
                    </picker-view-column>
                </picker-view>
            </view>
        </component-popup>
    </view>
</template>

<script>
const app = getApp();
import componentPopup from "@/components/popup/popup";
import { findAllProvince, findAllCityByPid, findAllDistrictByPid } from '@/api/user';

export default {
    data() {
        return {
            theme_view: app.globalData.get_theme_value_view(),
            columns: [[], [], []],
            columns_index: [[0], [0], [0]],
            indicatorStyle: `height: ${uni.upx2px(88)}px;`,
            active_cols: 1,
        };
    },
    computed: {
        col_style() {
            var cols = Math.max(this.active_cols, 1);
            return `width: ${Math.floor(100 / cols)}%;`;
        },
    },
    components: { componentPopup },
    props: {
        value: { type: Boolean, default: false },
        propCountryId: { type: [String, Number], default: "778937" },
        propProvinceId: { type: [String, Number], default: "" },
        propCityId: { type: [String, Number], default: "" },
        propCountyId: { type: [String, Number], default: "" },
    },
    watch: {
        value(new_val) {
            if (new_val) {
                this.$nextTick(() => { this.loadProvinces(); });
            }
        },
    },
    methods: {
        get_region_value(index, id) {
            var data = this.columns[index];
            var list_index = [0];
            data.forEach((d, i) => {
                if (d.id == id) { list_index = [i]; return false; }
            });
            this.$nextTick(() => { this.$set(this.columns_index, index, list_index); });
        },
        changeHandler(e) {
            const { dataset, value } = e.target;
            var col = parseInt(dataset.column);
            if (!value || value.length === 0) return;
            var item = this.columns[col][value[0]];
            if (!item || !item.id) return;
            this.$set(this.columns_index, col, value);
            for (var c = col + 1; c <= 2; c++) {
                this.$set(this.columns, c, []);
                this.$set(this.columns_index, c, [0]);
            }
            if (col === 0) {
                this.loadCities(item.id, true);
            } else if (col === 1) {
                this.loadDistricts(item.id, true);
            }
        },

        loadProvinces() {
            var countryId = this.propCountryId || "778937";
            findAllProvince({ countryId: countryId }).then(res => {
                var data = res.data || [];
                this.$set(this.columns, 0, data);
                this.$set(this.columns, 1, []);
                this.$set(this.columns, 2, []);
                this.active_cols = 1;
                var id = this.propProvinceId || (data[0] && data[0].id);
                if (id) {
                    if (this.propProvinceId) this.get_region_value(0, this.propProvinceId);
                    this.loadCities(id, true);
                }
            }).catch(() => {});
        },
        loadCities(pid, init) {
            if (!pid) return;
            findAllCityByPid({ pid: pid }).then(res => {
                var data = res.data || [];
                this.$set(this.columns, 1, data);
                this.$set(this.columns, 2, []);
                this.active_cols = data.length > 0 ? 2 : 1;
                if (data.length > 0) {
                    var id = init ? (data[0] && data[0].id) : (this.propCityId || (data[0] && data[0].id));
                    if (id) {
                        if (!init && this.propCityId) this.get_region_value(1, this.propCityId);
                        this.loadDistricts(id, init);
                    }
                }
            }).catch(() => {});
        },
        loadDistricts(pid, init) {
            if (!pid) return;
            findAllDistrictByPid({ pid: pid }).then(res => {
                var data = res.data || [];
                this.$set(this.columns, 2, data);
                if (data.length > 0) this.active_cols = 3;
                if (!init && this.propCountyId) this.get_region_value(2, this.propCountyId);
            }).catch(() => {});
        },
        popup_close_event() { this.$emit('input', false); },
        sub_region_event() {
            var ids = [], names = [];
            for (var i = 0; i < 3; i++) {
                if (this.columns[i].length > 0) {
                    var item = this.columns[i][this.columns_index[i]];
                    if (item) { ids.push(item.id); names.push(item.name); }
                }
            }
            if (ids.length === 0) { app.globalData.showToast('请选择地区'); return; }
            while (ids.length < 3) { ids.push(null); names.push(''); }
            this.$emit('change', { ids: ids, names: names, mergeName: names.join('') });
            this.$emit('input', false);
        }
    },
};
</script>
<style scoped>
    ::v-deep .popup-bottom { border-radius: 0; }
    .picker-view { height: 480rpx; }
    .picker-view-column { height: 480rpx !important; }
    .g-dp-ctt-wrapper {
        height: 480upx; width: 100%; background-color: #ffffff;
        display: flex; align-items: center; justify-content: center;
    }
    .g-dp-ctt-wp-item {
        width: 100%; height: 88upx; line-height: 88upx;
        text-align: center; text-overflow: ellipsis; white-space: nowrap; font-size: 30upx;
    }
    .flex-row { display: flex; flex-direction: row; }
    .jc-sb { justify-content: space-between; }
    .align-c { align-items: center; }
    .padding-main { padding: 20rpx 30rpx; }
    .cr-grey { color: #999; }
    .cr-blue { color: #1677ff; }
</style>
