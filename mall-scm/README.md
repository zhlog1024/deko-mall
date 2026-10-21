# Deko Mall · 供应商后台（mall-scm）

Vue 3 + TypeScript + Element Plus + Vite + Pinia，对接 `/mall-{模块}/scm-api/v1/`。  
脚手架衍生自 [vue3-element-admin](https://github.com/youlaitech/vue3-element-admin)（MIT），详见 [NOTICE](../NOTICE)。

结构同 [mall-admin/README.md](../mall-admin/README.md)，差异主要在 API 前缀与菜单。部署见 [deploy/README.md](../deploy/README.md)。

## 启动

```bash
cd mall-scm
cp .env.development.example .env.development
cp .env.production.example .env.production   # 按需
pnpm install
pnpm run dev          # http://localhost:9528
```

需先有网关 `http://127.0.0.1:9999`。Compose 部署同端口：http://localhost:9528。

## 环境变量

以 `.env.*.example` 为准，复制为 `.env.development` / `.env.production`（后者勿提交）：

| 变量 | 说明 |
| --- | --- |
| `VITE_APP_PORT` | **9528** |
| `VITE_APP_BASE_API` | 默认 `http://127.0.0.1:9999` |
| `VITE_APP_API_URL` | 同上 |

## 打包

```bash
pnpm run build:prod
# Compose：cd ../deploy && docker compose up -d --build --no-deps mall-scm
```

## 约定

与运营后台相同：`code === '00000'`、`multiplyAmount()`、字典与分页组件；`pnpm run lint:eslint` / `lint:prettier` / `lint:stylelint`。

```typescript
export function getSupplierOrderList(params: OrderQuery) {
  return request({ url: '/mall-oms/scm-api/v1/orders/page', method: 'get', params })
}
```
