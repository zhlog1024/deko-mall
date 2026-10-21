<template>
  <div ref="rootRef" class="code-playground">
    <!-- HTML / CSS / JS 编辑器 -->
    <div class="code-panels">
      <div class="editor-card">
        <div class="editor-head">
          <span class="editor-title">HTML</span>
          <span class="editor-tip">整页 HTML,app 端由 mp-html 渲染</span>
        </div>
        <div ref="htmlElRef" class="ace-host"></div>
      </div>
      <div class="editor-card">
        <div class="editor-head">
          <span class="editor-title">CSS</span>
          <span class="editor-tip">预留:仅 web 端生效</span>
        </div>
        <div ref="cssElRef" class="ace-host"></div>
      </div>
      <div class="editor-card">
        <div class="editor-head">
          <span class="editor-title">JS</span>
          <span class="editor-tip">预留:仅 web 端生效</span>
        </div>
        <div ref="jsElRef" class="ace-host"></div>
      </div>
    </div>
    <!-- 实时预览 -->
    <div class="preview-panel">
      <div class="preview-head">
        <span>实时预览</span>
        <el-button link size="small" @click="refreshPreview">
          <i-ep-refresh />刷新
        </el-button>
      </div>
      <iframe
        ref="previewElRef"
        title="页面实时预览"
        sandbox="allow-scripts"
        class="preview-frame"
      ></iframe>
    </div>
  </div>
</template>

<script setup lang="ts">
import ace from "ace-builds";
import "ace-builds/css/ace.css";
// 注册编辑器语言与主题。
import "ace-builds/src-noconflict/mode-html";
import "ace-builds/src-noconflict/mode-css";
import "ace-builds/src-noconflict/mode-javascript";
import "ace-builds/src-noconflict/theme-monokai";
import "ace-builds/src-noconflict/theme-tomorrow_night_eighties";
import "ace-builds/src-noconflict/theme-twilight";

defineOptions({ name: "PageCodeEditor" });

const props = defineProps<{
  htmlContent?: string;
  cssContent?: string;
  jsContent?: string;
}>();

const emit = defineEmits<{
  "update:htmlContent": [value: string];
  "update:cssContent": [value: string];
  "update:jsContent": [value: string];
}>();

const rootRef = ref<HTMLDivElement>();
const htmlElRef = ref<HTMLDivElement>();
const cssElRef = ref<HTMLDivElement>();
const jsElRef = ref<HTMLDivElement>();
const previewElRef = ref<HTMLIFrameElement>();

// 代码编辑器实例
type Editor = ReturnType<typeof ace.edit>;
const htmlEditor = shallowRef<Editor>();
const cssEditor = shallowRef<Editor>();
const jsEditor = shallowRef<Editor>();

// 预览防抖(输入即刷新,连发时合并)
let previewTimer: ReturnType<typeof setTimeout> | undefined;
const pending = reactive({
  html: "",
  css: "",
  js: "",
});

function syncPreview() {
  pending.html = htmlEditor.value?.getValue() ?? "";
  pending.css = cssEditor.value?.getValue() ?? "";
  pending.js = jsEditor.value?.getValue() ?? "";
  clearTimeout(previewTimer);
  previewTimer = setTimeout(renderPreview, 200);
}

/** 在独立沙箱中预览页面代码。 */
function renderPreview() {
  const frame = previewElRef.value;
  if (!frame) return;
  const html = pending.html;
  const css = pending.css;
  const js = pending.js;
  frame.srcdoc =
    '<!DOCTYPE html><html><head><meta charset="utf-8" /><style>' +
    css +
    "</style></head><body>" +
    html +
    "<script>" +
    js +
    "</" +
    "script></body></html>";
}

/** 手动刷新预览(重新按当前编辑器内容渲染) */
function refreshPreview() {
  syncPreview();
  renderPreview();
}

/** 创建编辑器并同步输入。 */
function createEditor(
  el: HTMLDivElement,
  mode: string,
  theme: string,
  initial: string,
  onChange: (value: string) => void
): Editor {
  const editor = ace.edit(el, {
    mode,
    theme,
    value: initial ?? "",
    autoScrollEditorIntoView: true,
    fontSize: 14,
    tabSize: 4,
    readOnly: false,
    highlightActiveLine: true,
    wrap: "free",
  });
  editor.on("input", () => {
    onChange(editor.getValue());
    syncPreview();
  });
  return editor;
}

/** 外部值变化时同步到编辑器(避免覆盖用户正在输入的内容) */
function syncEditorValue(
  editor: Editor | undefined,
  value: string | undefined
) {
  if (editor && value !== undefined && editor.getValue() !== value) {
    editor.setValue(value ?? "", -1);
  }
}

watch(
  () => props.htmlContent,
  (val) => syncEditorValue(htmlEditor.value, val)
);
watch(
  () => props.cssContent,
  (val) => syncEditorValue(cssEditor.value, val)
);
watch(
  () => props.jsContent,
  (val) => syncEditorValue(jsEditor.value, val)
);

// 容器尺寸变化时同步 ace 布局
let resizeObserver: ResizeObserver | undefined;
function refreshEditors() {
  htmlEditor.value?.resize(true);
  cssEditor.value?.resize(true);
  jsEditor.value?.resize(true);
}

onMounted(() => {
  // 关闭 worker(构建环境下避免额外 worker 资源加载)
  ace.config.set("useWorker", false);
  if (htmlElRef.value) {
    htmlEditor.value = createEditor(
      htmlElRef.value,
      "ace/mode/html",
      "ace/theme/monokai",
      props.htmlContent ?? "",
      (value) => emit("update:htmlContent", value)
    );
  }
  if (cssElRef.value) {
    cssEditor.value = createEditor(
      cssElRef.value,
      "ace/mode/css",
      "ace/theme/tomorrow_night_eighties",
      props.cssContent ?? "",
      (value) => emit("update:cssContent", value)
    );
  }
  if (jsElRef.value) {
    jsEditor.value = createEditor(
      jsElRef.value,
      "ace/mode/javascript",
      "ace/theme/twilight",
      props.jsContent ?? "",
      (value) => emit("update:jsContent", value)
    );
  }
  // 初次进入按当前值渲染一次预览
  syncPreview();
  renderPreview();

  if (rootRef.value && typeof ResizeObserver !== "undefined") {
    resizeObserver = new ResizeObserver(refreshEditors);
    resizeObserver.observe(rootRef.value);
  } else {
    window.addEventListener("resize", refreshEditors);
  }
});

onBeforeUnmount(() => {
  clearTimeout(previewTimer);
  resizeObserver?.disconnect();
  window.removeEventListener("resize", refreshEditors);
  [htmlEditor.value, cssEditor.value, jsEditor.value].forEach((editor) => {
    editor?.destroy();
  });
});
</script>

<style scoped lang="scss">
.code-playground {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  height: 100%;
  min-height: 0;
}

/* HTML 编辑区占据剩余高度。 */
.code-panels {
  display: grid;
  grid-template-rows: minmax(0, 1fr) 220px 220px;
  gap: 10px;
  min-height: 0;
}

.editor-card {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.editor-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-light);
}

.editor-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.editor-tip {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.ace-host {
  flex: 1;
  min-height: 0;
}

/* 右列:实时预览 */
.preview-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-light);
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.preview-frame {
  flex: 1;
  width: 100%;
  min-height: 0;
  border: none;
  background: #fff;
}
</style>
