<template>
    <model-goods-list :value="props.value" :new-list="goods_magic_list" :is-use-auto="false" :is-common-style="isCommonStyle"></model-goods-list>
</template>
<script setup lang="ts">
import { isEmpty } from "lodash";
const props = defineProps({
    value: {
        type: Object,
        default: () => {
            return {};
        },
    },
    isCommonStyle: {
        type: Boolean,
        default: true,
    },
});

const form = computed(() => props.value?.content || {});

// 与 mall model-goods-list 渲染器消费字段一致的默认占位
const default_list = {
    title: "测试商品标题",
    min_original_price: "41.2",
    show_original_price_symbol: "￥",
    show_original_price_unit: "/ 台",
    min_price: "51",
    show_price_symbol: "￥",
    show_price_unit: "/ 台",
    sales_count: "1000",
    images: "",
    new_cover: [],
    is_error: 0,
    plugins_view_icon_data: [
        {
            name: "满减活动",
            bg_color: "#EA3323",
            br_color: "",
            color: "#fff",
            url: "",
        },
        {
            name: "包邮",
            bg_color: "",
            br_color: "#EA3323",
            color: "#EA3323",
            url: "",
        },
        {
            name: "领劵",
            bg_color: "",
            br_color: "#EA9223",
            color: "#EA9223",
            url: "",
        },
    ],
};
// 数据来源的内容：mall 扁平 content 结构下组装快照列表（指定商品 data_list / 筛选 data_auto_list）
const goods_magic_list = computed(() => {
    // 指定商品并且指定商品数组不为空
    if (form.value.data_type == "0" && !isEmpty(form.value.data_list)) {
        const list = form.value.data_list || [];
        return list.map((item: any) => {
            return {
                ...item.data,
                title: !isEmpty(item.new_title) ? item.new_title : item.data?.title,
                simple_desc: item.data?.simpleDesc || item.data?.description || item.data?.simple_desc || '',
                new_cover: item.new_cover,
                images: item.data?.url || item.data?.images || item.data?.picUrl || '',
                min_price: item.data?.price,
                min_original_price: item.data?.originPrice,
                sales_count: item.data?.sales,
                show_price_symbol: "¥",
                show_original_price_symbol: "¥",
                is_error: 0,
                plugins_view_icon_data: [],
            };
        });
    } else if (!isEmpty(form.value.data_auto_list)) {
        return form.value.data_auto_list;
    } else {
        return Array(4).fill(default_list);
    }
});
</script>
<style lang="scss" scoped>
</style>
