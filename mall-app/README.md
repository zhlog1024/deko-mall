# Deko Mall 用户端（uni-app）

跨端用户端：微信小程序 / H5 / Android / iOS。HBuilderX 工程（非标准 Vite 项目）。

部署与 Compose 见 [deploy/README.md](../deploy/README.md)。

## 技术栈

uni-app · Vue 2 · HBuilderX · Sass（`rpx`）。运行时依赖主要为 `dayjs`、`hls.js`。

## 启动

```bash
npm install
```

1. [HBuilderX](https://www.dcloud.io/hbuilderx.html) → **打开目录** `mall-app/`
2. **运行** → 浏览器 / 微信开发者工具（H5：http://localhost:8080）
3. **发行** → 网站-H5 / 小程序-微信 / 原生 App

> `package.json` 的 `dev:*` 依赖 HBuilderX，不能单独用 npm 启动。

发行前替换：`manifest.json` 小程序 AppID、`config/index.js` 的 `qqmapKey`。

## 环境

| 键 | 网关 |
| --- | --- |
| `dev` / `prod` | `http://127.0.0.1:9999`（上线前改 `prod`） |

H5 端口 **8080**（与 Compose `APP_PORT` 一致）。挂 Compose：发行 H5 后执行  
`docker compose -f deploy/docker-compose.yml --profile app up -d`。

## 说明

用户端 UI 衍生自 ShopXO uni-app（MIT），详见根目录 [NOTICE](../NOTICE)。
