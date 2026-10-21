const js = require('@eslint/js');
const vue = require('eslint-plugin-vue');
const globals = require('globals');

/**
 * mall-app ESLint 扁平配置（ESLint 9 + uni-app + Vue2）
 * 目的：拦住「静态可检查」的错误，尤其是**移植时漏 import / 变量未定义**这类
 * 只在运行期才暴露的静默失败。
 * 背景：直播模块出现过 `goods.vue` 调用 `getLiveRoomGoods` 却忘记 import，
 * mounted 抛 ReferenceError，而 uni.showLoading 已在其之前调用 →
 * hideLoading 永不执行，「加载中」永久卡死。`no-undef` 可静态拦住这一类。
 * 定位：**只做正确性检查，不做代码风格检查**（420 个历史文件，风格规则只会产生噪音）。
 * 不挂钩构建（HBuilderX 项目无 webpack 钩子），仅作手动 / CI 命令使用。
 */
module.exports = [
    // 忽略：只 lint「我们写的源码」
    {
        ignores: [
            'node_modules/**',
            'unpackage/**',
            'dist/**',
            'static/**',          // 静态资源
            'remote-assets/**',   // CDN 上传源，不参与构建
            'uni_modules/**',     // 插件市场第三方组件，非本项目维护
            'common/js/lib/**',   // 第三方 UMD 库（js-base64 等压缩产物），非本项目维护
            '.hbuilderx/**',
        ],
    },

    // ===== 基础：ESLint 推荐规则 =====
    js.configs.recommended,

    // ===== Vue2 规则集（eslint-plugin-vue 为 Vue2 提供的 flat 配置）=====
    ...vue.configs['flat/vue2-essential'],

    // 本项目规则与全局变量
    {
        languageOptions: {
            ecmaVersion: 2021,
            sourceType: 'module',
            globals: {
                ...globals.browser,
                ...globals.node,
                // uni-app 运行时
                uni: 'readonly',
                getApp: 'readonly',
                getCurrentPages: 'readonly',
                getCurrentInstance: 'readonly',
                uniCloud: 'readonly',
                UniApp: 'readonly',
                UniServiceJSBridge: 'readonly',
                __uniConfig: 'readonly',
                __uniRoutes: 'readonly',
                __channel: 'readonly',
                // 各家小程序宿主
                wx: 'readonly',
                my: 'readonly',
                swan: 'readonly',
                tt: 'readonly',
                ks: 'readonly',
                jd: 'readonly',
                qapp: 'readonly',
                // 小程序 / 快应用全局构造器
                App: 'readonly',
                Page: 'readonly',
                Component: 'readonly',
                Behavior: 'readonly',
                getRegExp: 'readonly',
                // 5+ App（APP-PLUS / nvue）
                plus: 'readonly',
                weex: 'readonly',
                WeixinJSBridge: 'readonly',
                // Weex / nvue 运行时注入的环境对象（scrollMixins.js 等用它区分 nvue 与 H5 分支）
                WXEnvironment: 'readonly',
            },
        },
        rules: {
            // ★★★ 核心规则：未定义变量 ★★★
            // 移植时漏 import 会在这里暴露，而非等到运行期。整个配置的存在意义就是它。
            'no-undef': 'error',

            // ===== 因 uni-app 条件编译而必须关闭的规则（框架事实，非代码缺陷）=====
            // uni-app 用 `// #ifdef MP` / `// #endif` 注释在**构建期**做分支裁剪，
            // 但 ESLint 只能看到裁剪前的完整源码：同一标识符会在多个分支里各声明一次，
            // 看起来像重复定义，实际每个平台只保留一份。这类报错在本次接入中共 120 处
            // （no-dupe-keys 63 + vue/no-dupe-keys 57），全部为假阳性。
            // 代价：真正的对象字面量重复键不再被检出。若将来要在某个文件恢复该检查，
            // 可在该文件用 `/* eslint no-dupe-keys: "error" */` 局部开启。
            'no-dupe-keys': 'off',
            'vue/no-dupe-keys': 'off',

            // vue/no-parsing-error 的 x-invalid-end-tag 同源            // 开标签在 `#ifdef APP-NVUE` 分支内、闭标签在 `#ifndef APP-NVUE` 分支内
            // （如 goods.vue / live-content.vue 的 scroll-view），
            // 裁剪前看必然标签不配平。只关这一项，其余解析错误照旧拦截——
            // 本次就在 buy.vue / goods-list.vue / u-share-popup.vue 靠它揪出 3 个真 markup 错误。
            'vue/no-parsing-error': ['error', { 'x-invalid-end-tag': false }],

            // uni-app 页面即路由（pages/xxx/yyy.vue），文件名天然是单词，
            // 改多词会破坏 pages.json 路由与目录约定。
            'vue/multi-word-component-names': 'off',

            // 历史遗留降为 warn：保留可见性，但不阻塞
            // 未使用变量：历史代码含大量平台条件编译分支（某分支专用变量在别的平台看似未用）
            'no-unused-vars': ['warn', { args: 'none', ignoreRestSiblings: true }],

            // 重复声明：主要是 `var` 在 switch 各 case / if-else 分支里重复声明
            // （如 cart-para-curve.vue 的 cart_top），以及 #ifdef 分支。合法 JS，非缺陷。
            'no-redeclare': 'warn',

            // 多余转义：71 处里 64 处来自 goods-detail.vue:772 的一个 DIY 表单配置 JSON 字面量，
            // `\/` 在其中的 JSON 语义下是合法的，属数据而非代码。
            'no-useless-escape': 'warn',

            // 注册但未使用的组件：多为装修器动态/历史残留注册（如 diy.vue 的 componentBottomLine）
            'vue/no-unused-components': 'warn',

            // 对象/数组 prop 的默认值应为工厂函数：历史 Vue2 写法，改动会变更运行时行为，暂不修
            'vue/require-valid-default-prop': 'warn',

            // 自我赋值：经逐处核对，6 处均为无害 no-op，且 article-tabs.vue 等的写法
            'no-self-assign': 'warn',

            // 空块降为 warn（各平台占位回调常见）
            'no-empty': ['warn', { allowEmptyCatch: true }],

            // ===== 同源假阳性：uni-app 条件编译导致的「不可达 / 多根 / v-else 脱节」=====
            // 典型形态（App.vue / config/request.js / u-icon.vue / goods.vue）            //   // #ifdef MP-WEIXIN
            //   return 'weixin'      ← 该分支之后的 return 在裁剪前看永远不可达
            //   // #endif
            //   // #ifdef H5
            //   return 'h5'
            //   // #endif
            // 裁剪后每个平台只剩一个 return / 一个根节点，Vue 模板也是单根。
            // 这三项保留为 warn：真出现问题时仍能在输出里看到。
            'no-unreachable': 'warn',
            'vue/no-multiple-template-root': 'warn',
            'vue/valid-v-else': 'warn',

            // 空的 case 直落到下一个 case（多个 case 共用同一段逻辑）是有意为之的常见写法，
            // 如 goods-list.vue / goods-detail.vue 的多个页面路径共用分支。
            // 允许空 case 落空，但**非空** case 漏 break 仍然报错。
            'no-fallthrough': ['error', { allowEmptyCase: true }],

            // 其余历史风格债降为 warn
            // 模板内 v-for="(item, index)" 中未使用 index（11 处，无害）
            'vue/no-unused-vars': 'warn',
            // <template> 上的 class（模板不渲染元素，class 无效果）
            'vue/no-useless-template-attributes': 'warn',
            // case 块内的词法声明（未加花括号，历史写法）
            'no-case-declarations': 'warn',
            // obj.hasOwnProperty(...) 直接调用原型方法
            'no-prototype-builtins': 'warn',
            // computed 内产生副作用（历史写法，功能正常）
            'vue/no-side-effects-in-computed-properties': 'warn',
            // v-if 与 v-for 同用（历史写法，功能正常）
            'vue/no-use-v-if-with-v-for': 'warn',

            // 以下为「风格类」规则，本项目**不启用**，避免对历史文件产生无意义 diff            // indent / quotes / semi / comma-dangle / space-before-function-paren / max-len ...
        },
    },

    // 配置脚本本身放宽
    {
        files: ['*.config.js', 'eslint.config.js'],
        languageOptions: { sourceType: 'commonjs' },
    },
];
