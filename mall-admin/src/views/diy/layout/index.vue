<template>
    <div v-loading.fullscreen.lock="loading" class="app-wrapper no-copy" element-loading-background="rgba(255,255,255,1)" element-loading-custom-class="loading-custom">
        <template v-if="!loading_content">
            <template v-if="!is_empty">
                <navbar v-model="form.model" :save-disabled="save_disabled" @preview="preview_event" @save="save_event" @save-close="save_close_event" />
                <div class="app-wrapper-content flex-row">
                    <app-main
                        :diy-data="form.diy_data"
                        :tabs-data="form.tabs_data"
                        :header="form.header"
                        :footer="form.footer"
                        :on-right-update="right_update"
                        @clear="clear_data_event"
                    ></app-main>
                    <settings :key="key" :value="diy_data_item"></settings>
                </div>
            </template>
            <template v-else>
                <no-data height="100vh" img-width="260px" size="16px" :text="empty_data"></no-data>
            </template>
        </template>
        <preview v-model="preview_dialog" :data-id="diy_id"></preview>
    </div>
</template>

<script setup lang="ts">
import { is_obj, set_cookie, get_cookie, get_math } from '../utils';
import { Settings, AppMain } from './components';
import defaultSettings from './components/main/index';
import defaultConfigSetting from '../config/setting';
import defaultConfigConst from '../config/const';
import { clone, cloneDeep, isEmpty, omit } from 'lodash';
import { diyData, headerAndFooter, diyConfig } from '../api/diy';
import CommonAPI from '../api/common';
import { commonStore } from '../store';
import { magic_config } from '../config/const/tabs-magic';
import { get_id, get_type } from '../utils/common';
const common_store = commonStore();
interface diy_data_item {
    id: string;
    model: {
        logo: string;
        name: string;
        is_enable: string;
        describe: string;
    };
    header: headerAndFooter;
    footer: headerAndFooter;
    tabs_data: Array<any>;
    diy_data: Array<any>;
}
const temp_form = ref<diy_data_item>({
    id: '',
    model: {
        logo: '',
        name: 'DIY模版',
        is_enable: '1',
        describe: '',
    },
    header: {
        name: '页面设置',
        show_tabs: '1',
        key: 'page-settings',
        com_data: defaultSettings.header_nav,
    },
    footer: {
        name: '底部导航',
        show_tabs: '0',
        key: 'footer-nav',
        com_data: defaultSettings.footer_nav,
    },
    tabs_data: [],
    diy_data: [],
});
const form = ref<diy_data_item>({
    id: '',
    model: {
        logo: '',
        name: '',
        is_enable: '1',
        describe: '',
    },
    header: {
        name: '页面设置',
        show_tabs: '1',
        key: 'page-settings',
        com_data: defaultSettings.header_nav,
    },
    footer: {
        name: '底部导航',
        show_tabs: '0',
        key: 'footer-nav',
        com_data: defaultSettings.footer_nav,
    },
    tabs_data: [],
    diy_data: [],
});
const diy_data_item = ref({});

const key = ref('');

const right_update = (item: any, diy: [Array<any>], header: headerAndFooter, footer: headerAndFooter, tabs_data: [Array<any>]) => {
    console.log('right_update called, item:', item);
    console.log('right_update item.com_data:', item?.com_data);
    console.log('right_update item.com_data.content:', item?.com_data?.content);
    console.log('right_update item.com_data.style:', item?.com_data?.style);
    diy_data_item.value = item;
    form.value.diy_data = diy;
    form.value.header = header;
    form.value.footer = footer;
    form.value.tabs_data = tabs_data;
    // 生成随机id
    key.value = Math.random().toString(36).substring(2);
};
// 清空数据
const clear_data_event = () => {
    let new_tem_form = cloneDeep(temp_form.value);
    form.value.header.show_tabs = '1';
    form.value.footer.show_tabs = '0';
    form.value.header.com_data = new_tem_form.header.com_data;
    form.value.footer.com_data = new_tem_form.footer.com_data;
    form.value.tabs_data = [];
    form.value.diy_data = [];
    // 数据清空之后，将公共沉浸模式判断为false
    common_store.set_is_immersion_model(false);
    common_store.set_is_general_safe_distance(false);
    diy_data_item.value = new_tem_form.header;
};
//#region 页面初始化数据 ---------------------start
// 页面加载
onMounted(() => {
    common_init();
});

const is_empty = ref(false);
const empty_data = ref('编辑数据有误');
const init = () => {
    // 如果 URL 中带有 diy_id，则认为是“编辑已有装修页”；否则视为“新建装修页”
    const diyId = get_id();
    if (diyId) {
        // 详情接口使用 GET 方法，id 作为路径参数
        CommonAPI.getDiyDetail(diyId).then((res: any) => {
            // request.ts 已经把 axios 响应 data 解包为 { code, data, msg }
            const new_data = res.data || undefined;
            if (new_data) {
                let data = form_data_transfor_diy_data(new_data);
                // 默认数据合并
                data.header.com_data = default_merge(data.header.com_data, 'header_nav');
                data.footer.com_data = default_merge(data.footer.com_data, 'footer_nav');
                
                data.diy_data = data_merge(data.diy_data);
                data.tabs_data = data_merge(data.tabs_data);
                // 判断默认数据是否开启沉浸式
                if (data.header.com_data.style.immersive_style === '1') {
                    common_store.set_is_immersion_model(true);
                    // 判断默认数据是否开启沉浸式,并且开启了安全距离
                    if (data.header.com_data.style.general_safe_distance_value == '1') {
                        common_store.set_is_general_safe_distance(true);
                    }
                }
                form.value = data;
            } else {
                is_empty.value = true;
                empty_data.value = '编辑数据有误';
            }
            loading_event();
        }).catch((err) => {
            is_empty.value = true;
            empty_data.value = err;
            loading_event();
        });
    } else {
        // 新建装修页：直接使用默认模板数据初始化（不再依赖 VITE_APP_BASE_API 判断）
        temp_form.value.header.com_data = defaultSettings.header_nav;
        temp_form.value.footer.com_data = defaultSettings.footer_nav;
        form.value = cloneDeep(temp_form.value);
        is_empty.value = false;
        loading_event();
    }
};
// 数据合并
const data_merge = (list: string[]) => {
    list.forEach((item: any) => {
        item.com_data = default_merge(item.com_data, item.key);
    });
    return list;
};
// 浅层数据合并
const default_merge = (data: any, key: string) => {
    if (data.style) {
        data.style = Object.assign({}, cloneDeep((defaultSettings as any)[key.replace(/-/g, '_')]).style, data.style);
        // 历史的值，赋值给新的值
        if (key == 'header_nav' && !isEmpty(data.style.position_color)) {
            data.style.location_color = data.style.position_color;
        }
    } else {
        data.style = cloneDeep((defaultSettings as any)[key.replace(/-/g, '_')]).style;
    }
    // 兼融老数据
    if (!isEmpty(data.style.common_style)) {
        data.style.common_style = Object.assign({}, cloneDeep(defaultConfigConst), data.style.common_style);
    }
    if (data.content) {
        data.content = Object.assign({}, cloneDeep((defaultSettings as any)[key.replace(/-/g, '_')]).content, data.content);
    } else {
        data.content = cloneDeep((defaultSettings as any)[key.replace(/-/g, '_')]).content;
    }
    // 选项卡魔方数据补全
    if (key == 'tabs-magic') {
        if (data.content.home_data) {
            data.content.home_data = Object.assign({}, magic_config, data.content.home_data);
        }
        if (data.content.tabs_list.length > 0) {
            data.content.tabs_list = data.content.tabs_list.map((item: any) => {
                return Object.assign({}, magic_config, item);
            })
        }
    }
    return data;
};

// 初始化公共数据
const token = ref('');
const common_init = async () => {
    if (get_cookie('attachment_host') || get_cookie('attachment_host') !== 'null' || get_cookie('attachment_host') !== null) {
        CommonAPI.getInit().then((res: any) => {
            common_store.set_common(res.data);
            set_cookie('attachment_host', res.data.config.attachment_host);
            init();
        });
    } else {
        // 将localStorage中的数据读取出,判断localStorage中是否有数据
        if (localStorage.getItem('diy_init_common')) {
            const data = JSON.parse(localStorage.getItem('diy_init_common') || '');
            common_store.set_common(data);
            // 清除缓存
            localStorage.removeItem('diy_init_common');
            init();
        }
    }
    // 获取用户id
    if (import.meta.env.VITE_APP_BASE_API == '/dev-admin') {
        let temp_data = await import(import.meta.env.VITE_APP_BASE_API == '/dev-admin' ? '../utils/temp.d' : '../utils/temp_pro.d.ts');
        token.value = '&token=' + temp_data.default.temp_token;
    } else {
        // 获取token
        const cookie = get_cookie('admin_info');
        if (cookie && cookie !== null && cookie !== 'null') {
            token.value = '&token=' + JSON.parse(cookie).token;
        }
    }
};
// 加载动画
const loading = ref(true);
const loading_content = ref(true);
const loading_event = () => {
    loading_content.value = false;
    setTimeout(() => {
        loading.value = false;
    }, 1000);
};
//#endregion 页面初始化数据 ---------------------end

//#region 顶部导航回调方法 ---------------------start
const preview_dialog = ref(false);
const diy_id = ref('');
const preview_event = (bool: boolean) => {
    let url = common_store.common.config.preview_url;
    if (isEmpty(url)) {
        ElMessage.error('请先配置预览地址');
    } else {
        save_disabled.value = bool;
        save_formmat_form_data(form.value, false, true);
    }
};
const save_disabled = ref(false);
const save_event = (bool: boolean) => {
    save_disabled.value = bool;
    save_formmat_form_data(form.value);
};
const save_close_event = (bool: boolean) => {
    save_disabled.value = bool;
    save_formmat_form_data(form.value, true);
};
// save_formmat_form_data: 保存数据， data： 数据， close： 是否关闭， is_preview： 是否预览
const save_formmat_form_data = (data: diy_data_item, close: boolean = false, is_preview: boolean = false) => {
    ElMessage({
        message: '保存中',
        type: 'success',
        duration: 0,
        icon: 'Loading',
        customClass: 'message-box-custom',
    })
    const clone_form = cloneDeep(data);
    clone_form.header.show_tabs = '1';
    // 去除位置颜色
    clone_form.header.com_data.style = omit(cloneDeep(clone_form.header.com_data.style), ['position_color']);

    clone_form.footer.show_tabs = '0';
    // 组件类型
    const new_array_1 = ['goods-list', 'goods-magic'];
    // 数据比正常list多一级
    const new_array_2 = ['goods-tabs'];
    // 层级更深
    const new_array_4 = ['data-magic'];
    // 商品列表
    const new_array_6 = ['nav-group'];
    // 选项卡数据更新
    clone_form.tabs_data = clone_form.tabs_data.map((item: any) => { 
        if (['tabs-magic'].includes(item.key)) {
            item.com_data.content.tabs_active_index = 0;
            // 获取所有组件名称
            const all_type = ['hot_zone', 'img_magic', 'goods_list', 'nav_group', 'video', 'carousel'];
            // 将数据信息合并起来
            const new_data_list = cloneDeep([item.com_data.content.home_data, ...item.com_data.content.tabs_list]);
            // 对整个数据进行处理
            const data_list_handle: any[] = [];
            new_data_list.forEach((item1: any) => {
                if (['goods_list'].includes(item1.magic_type)) {
                    // 处理商品数据
                    goods_or_article_data_processing(item1[item1.magic_type].content, item1.magic_type == new_array_1[0], item1.magic_type);
                }
                data_list_handle.push(Object.keys(item1)
                    .filter(key => !(all_type.filter((item2: string) => !isEmpty(item1.magic_type) ? (item2 !== item1.magic_type) : item2).includes(key)))
                    .reduce((acc: Record<string, any>, key: string) => { 
                        acc[key] = item1[key];
                        return acc;
                    }, {}));
            });
            // 处理完成之后拆分开
            item.com_data.content.home_data = data_list_handle.length > 0 ? data_list_handle[0] : null;
            item.com_data.content.tabs_list = data_list_handle.slice(1, data_list_handle.length);
        } 
        return {
            ...item,
            show_tabs: '0',
        }
    });
    // 拖拽区域更新
    console.log('diy_data 所有组件 keys:', clone_form.diy_data.map((item: any) => item.key));
    clone_form.diy_data = clone_form.diy_data.map((item: any) => {
        console.log('处理组件, key:', item.key, 'new_array_1.includes:', new_array_1.includes(item.key), 'new_array_2.includes:', new_array_2.includes(item.key));
        if (new_array_1.includes(item.key)) {
            // 商品数据处理
            goods_or_article_data_processing(item.com_data.content, item.key == new_array_1[0], item.key);
        } else if (new_array_2.includes(item.key)) {
            console.log('处理 goods-tabs, key:', item.key);
            item.com_data.content.tabs_active_index = 0;
            item.com_data.content.tabs_list.forEach((item0: any) => {
                console.log('处理 tabs_list item, data_type:', item0.data_type, 'data_list 长度:', item0.data_list?.length);
                // 商品数据处理
                goods_or_article_data_processing(item0, item.key == new_array_1[0], item.key);
            });
        } else if (new_array_4.includes(item.key)) {
            item.com_data.content.data_magic_list.forEach((item1: any) => {
                if (item1.data_content.data_type == 'goods') {
                    item1.data_content.goods_ids = item1.data_content.goods_list.map((item2: any) => item2.data.id).join(',') || '';
                    item1.data_content.goods_list = item1.data_content.goods_list.map((item3: any) => {
                        return {
                            ...item3,
                            data: [],
                            data_id: item3.data.id,
                        };
                    });
                    // 判断值是否存在
                    if (!isEmpty(item1.data_content.data_source_content)) {
                        // 清除自定义里的数据
                        item1.data_content.data_source_content.data_ids = [];
                        item1.data_content.data_source_content.data_list = [];
                        item1.data_content.data_source_content.data_auto_list = [];
                        item1.data_content.data_source_content.data_type = 0;
                    } else {
                        item1.data_content.data_source_content = { data_type: 0, data_ids: '', data_list: [], data_auto_list: []}
                    }
                } else if (item1.data_content.data_type == 'video') {
                    // 视频数据处理
                    goods_or_article_data_processing(item1.data_content, false, 'video');
                } else {
                    // 清除商品里的数据
                    item1.data_content.goods_ids = '';
                    item1.data_content.goods_list= [];
                    // 判断值是否存在
                    if (!isEmpty(item1.data_content.data_source_content)) {
                        // 清除自定义里的数据
                        item1.data_content.data_source_content.data_type = 0;
                        item1.data_content.data_source_content.data_ids = '';
                        item1.data_content.data_source_content.data_list = [];
                        item1.data_content.data_source_content.data_auto_list = [];
                    } else {
                        item1.data_content.data_source_content = { data_type: 0, data_ids: '', data_list: [], data_auto_list: []}
                    }
                }
            });
        } else if (new_array_6.includes(item.key)) {
            item.com_data.content.nav_content_list = item.com_data.content.nav_content_list.map((item0: any) => ({...omit(item0, 'tabs_name') }));
        } else if (['data-tabs'].includes(item.key)) {
            item.com_data.content.tabs_active_index = 0;
            const new_tabs_list = item?.com_data?.content?.tabs_list || [];
            new_tabs_list.forEach((item: any) => {
                item.tabs_name = 'content';
                if (item.tabs_data_type == 'goods') {
                    const new_com_data = item.goods_config;
                    // 商品的数据处理
                    goods_or_article_data_processing(new_com_data.content, true);
                    // 清除其他数据
                    delete item.article_config;
                }
            });
        } else if (['salerecords'].includes(item.key)) {
            item.com_data.content.data_auto_list = [];
            item.com_data.content.is_left = '0';
        } else if (['seckill'].includes(item.key)) {
            item.com_data.content.data = [];
            item.com_data.content.is_left = '0';
        }
        return {
            ...item,
            show_tabs: '0',
        };
    });
    // 数据改造
    const new_data = diy_data_transfor_form_data(clone_form);
    CommonAPI.getDynamicApi(common_store.common.config.diy_save_url, new_data)
        .then((res) => {
            setTimeout(() => {
                save_disabled.value = false;
            }, 500);
            ElMessage.closeAll();
            setTimeout(() => {
                ElMessage.success('保存成功');
            }, 100);
            if (close) {
                ElMessageBox.confirm('您确定要关闭本页吗？', '提示')
                    .then(() => {
                        // 关闭页面
                        window.close();
                    })
                    .catch(() => {});
            }
            if (is_preview) {
                preview_dialog.value = true;
                diy_id.value = String(res.data);
            }
            if (res.data != null && res.data != '') {
                form.value.id = String(res.data);
            }
            // 本地的时候会补id参数
            if (import.meta.env.VITE_APP_BASE_API == '/dev-admin') {
                history.pushState({}, '', '?s=diy/saveinfo/id/' + res.data + '.html');
            }
        })
        .catch((err) => {
            // 失败的时候关闭弹出框
            ElMessage.closeAll();
            if (err == 'canceled') {
                console.log('请求已取消');
            } else {
                ElMessage.error(err || '系统出错');
            }
            save_disabled.value = false;
        });
};
/**
 * 商品数据处理函数
 * 该函数根据提供的数据类型和是否为商品的标志，处理数据内容的结构
 * 主要目的是为了标准化数据处理逻辑，使得数据格式符合预期的要求
 * 
 * @param new_com_data_content 传入的新数据内容对象，包含需要处理的数据列表和其他相关信息
 * @param is_goods 是否为商品的标志，用于决定数据处理的具体方式
 */
 const goods_or_article_data_processing = (new_com_data_content: any, is_goods: boolean, type: string = '') => {
    // 判断数据类型，如果为'0'，则进行详细的数据处理
    if (new_com_data_content.data_type == '0') {
        // 提取数据ID列表，用于后续的数据查询或处理
        new_com_data_content.data_ids = new_com_data_content.data_list.map((item: any) => item.data?.id).join(',') || '';

        // 重构数据列表，只添加 data_id 字段，保留原始数据用于前端展示
        // 注意：不再清空 data 字段，确保保存后刷新能正确显示价格、销量等信息
        new_com_data_content.data_list = new_com_data_content.data_list.map((item1: any) => {
            // 保留原始字段，添加 data_id
            return {
                id: item1.id,
                new_cover: item1.new_cover,
                new_title: item1.new_title,
                data: item1.data,  // 保留完整 data 数据
                data_id: item1.data?.id,
            };
        });
        // 设置分组ID、数量等默认值，确保数据的一致性和完整性
        new_com_data_content.keywords = '';
        // 商品分组ID默认为undefined
        new_com_data_content.group_id = undefined;
        new_com_data_content.number = defaultConfigSetting.page_size;

        // 根据是否为商品，设置不同的字段
        if (!is_goods) {
        }
    } else {
        // 如果数据类型不是'0'，清空数据ID列表和数据列表，确保数据处理的一致性
        new_com_data_content.data_ids = '';
        new_com_data_content.data_list = [];
    }

    // 清空自动数据列表，为后续的数据处理或展示做准备
    new_com_data_content.data_auto_list = [];
};
//#endregion 顶部导航回调方法 ---------------------end


// 数据改造
const diy_data_transfor_form_data = (clone_form: diy_data_item) => {
    return {
        id: clone_form.id,
        name: clone_form.model.name,
        coverUrl: clone_form.model.logo,
        // 兼容字符串/数字/布尔三种形式的启用标记
        isEnabled: ['1', 1, true].includes(clone_form.model.is_enable as any),
        config: JSON.stringify({
            header: clone_form.header,
            footer: clone_form.footer,
            diy_data: clone_form.diy_data,
            tabs_data: clone_form.tabs_data,
        }),
    };
};
const form_data_transfor_diy_data = (clone_form: diyData) => {
    // 仅使用后端新的字段结构：config / coverUrl / isEnabled
    const temp_config: any = (clone_form as any).config;
    const new_tem_form = cloneDeep(temp_form.value);
    try {
        if (!temp_config) {
            throw new Error('empty config');
        }
        const parsed: diyConfig = is_obj(temp_config)
            ? (temp_config as diyConfig)
            : (JSON.parse(temp_config as string) as diyConfig);
        return {
            id: clone_form.id,
            model: {
                logo: (clone_form as any).coverUrl || '',
                name: clone_form.name,
                is_enable: (clone_form as any).isEnabled,
                describe: (clone_form as any).describe,
            },
            header: parsed.header,
            footer: parsed.footer,
            tabs_data: parsed.tabs_data,
            diy_data: parsed.diy_data,
        };
    } catch (error) {
        // 解析失败时退回默认模板
        return {
            id: clone_form.id,
            model: {
                logo: (clone_form as any).coverUrl || '',
                name: clone_form.name,
                is_enable: (clone_form as any).isEnabled,
                describe: (clone_form as any).describe,
            },
            header: new_tem_form.header,
            footer: new_tem_form.footer,
            tabs_data: new_tem_form.tabs_data,
            diy_data: new_tem_form.diy_data,
        };
    }
};
</script>

<style scoped lang="scss">
.app-wrapper {
    position: relative;
    width: 100%;
    height: 100vh; // 使用 100vh 而不是 calc，因为已经脱离了父容器
    background-color: #f0f2f5;
    overflow: hidden;
    .app-wrapper-content {
        height: calc(100vh - 6rem); // navbar 高度 6rem
        overflow: hidden;
    }
}
.no-copy {
    -webkit-touch-callout: none; /* iOS Safari */
    -webkit-user-select: none; /* Safari */
    -khtml-user-select: none; /* Konqueror HTML */
    -moz-user-select: none; /* Firefox */
    -ms-user-select: none; /* Internet Explorer/Edge */
    user-select: none; /* Non-prefixed version, currently supported by Chrome and Opera */
}
</style>
