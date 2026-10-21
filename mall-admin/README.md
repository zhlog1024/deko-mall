# Deko Mall · 运营后台（mall-admin）

Vue 3 + TypeScript + Element Plus + Vite + Pinia，对接 `/mall-{模块}/api/v1/`。  
脚手架衍生自 [vue3-element-admin](https://github.com/youlaitech/vue3-element-admin)（MIT）；DIY 装修衍生自 ShopXO（MIT）。详见 [NOTICE](../NOTICE)。

供应商后台见 [mall-scm/README.md](../mall-scm/README.md)。全栈部署见 [deploy/README.md](../deploy/README.md)。

## 启动

```bash
cd mall-admin
cp .env.development.example .env.development
cp .env.production.example .env.production   # 按需
pnpm install
pnpm run dev          # http://localhost:9527
```

需先有网关 `http://127.0.0.1:9999`（及 Nacos）。Compose 部署同端口：http://localhost:9527。

## 环境变量

以 `.env.*.example` 为准，复制为 `.env.development` / `.env.production`（后者勿提交）：

| 变量 | 说明 |
| --- | --- |
| `VITE_APP_PORT` | **9527** |
| `VITE_APP_BASE_API` | 默认 `http://127.0.0.1:9999` |
| `VITE_APP_API_URL` | 同上 |

## 打包

```bash
pnpm run build:prod     # dist/
# Compose：cd ../deploy && docker compose up -d --build --no-deps mall-admin
```

## 约定

- `code === '00000'` 再进业务
- 金额用 `multiplyAmount()`
- 字典 `<Dictionary typeCode="XXX" />`；分页 `pageNo` / `pageSize`
- 检查：`pnpm run lint:eslint` / `lint:prettier` / `lint:stylelint`

```typescript
export function getSpuPage(params: SpuPageQuery) {
  return request({ url: '/mall-pms/api/v1/spu/page', method: 'get', params })
}
```
