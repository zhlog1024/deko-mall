import { createApp } from "vue";
import App from "./App.vue";
import router from "@/router";
import { setupStore } from "@/store";
import { setupDirective } from "@/directive";
import { setupElIcons, setupI18n, setupPermission } from "@/plugins";
import { VueDraggable } from "vue-draggable-plus";
// 全局弹窗工具（$common.message_box / $common.alert）
import popUp from "@/utils/popUp";
// 装修画布的拖拽缩放组件。
import Vue3DraggableResizable from "vue3-draggable-resizable";
import "vue3-draggable-resizable/dist/Vue3DraggableResizable.css";

// 本地SVG图标
import "virtual:svg-icons-register";

// 样式
import "element-plus/theme-chalk/dark/css-vars.css";
import "@/styles/index.scss";
import "uno.css";
import "animate.css";

const app = createApp(App);
app.component("VueDraggable", VueDraggable);
// 注册画布拖拽缩放组件和输入聚焦指令。
app.use(Vue3DraggableResizable);
app.directive("focus", {
  mounted(el: any) {
    const input = el.querySelector && el.querySelector("input");
    if (input && input.focus) {
      input.focus();
    }
  },
});
// 挂载装修器工具到全局，供 $common.message_box / $common.alert 使用
// 仅在装修相关页面会用到，不影响其他模块
(app.config.globalProperties as any).$common = popUp;
// 全局注册 自定义指令(directive)
setupDirective(app);
// 全局注册 状态管理(store)
setupStore(app);
// 全局注册Element-plus图标
setupElIcons(app);
// 国际化
setupI18n(app);
// 注册动态路由
setupPermission();
app.use(router).mount("#app");
