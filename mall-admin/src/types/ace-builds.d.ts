/**
 * ace-builds 本地类型声明
 *
 * 背景: ace-builds@1.44 官方自带的 ace.d.ts 引用了不存在的私有包
 * `ace-builds-internal`(registry 404, 官方发版缺陷), 会污染项目类型解析。
 * 因此通过 tsconfig paths 将 "ace-builds" 指到本文件,
 * 仅声明本工程用到的 API(runtime 仍使用 node_modules 里的真实 ace)。
 */
declare module "ace-builds" {
  export interface AceEditor {
    getValue(): string;
    setValue(value: string, cursorPos?: number): void;
    on(event: string, handler: (...args: unknown[]) => void): void;
    resize(force?: boolean): void;
    destroy(): void;
  }

  export interface AceConfig {
    set(key: string, value: unknown): void;
  }

  export function edit(
    el: HTMLElement | string | null,
    options?: Record<string, unknown>
  ): AceEditor;

  export const config: AceConfig;
}

// 编辑器 mode/theme 均为 side-effect 注册(无需类型),声明避免解析告警
declare module "ace-builds/src-noconflict/mode-html" {}
declare module "ace-builds/src-noconflict/mode-css" {}
declare module "ace-builds/src-noconflict/mode-javascript" {}
declare module "ace-builds/src-noconflict/theme-monokai" {}
declare module "ace-builds/src-noconflict/theme-tomorrow_night_eighties" {}
declare module "ace-builds/src-noconflict/theme-twilight" {}
