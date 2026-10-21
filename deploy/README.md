# 部署说明

Docker Compose 本机部署。默认拉起中间件、9 个后端、运营 / 供应商后台；用户端 H5 需额外发行后再 `--profile app`。

| 类型 | 默认 | 内容 |
| --- | --- | --- |
| 中间件 | 是 | MySQL、Redis、RabbitMQ、MinIO、Nacos |
| 后端 | 是 | 网关 + auth / system / ums / pms / oms / pay / sms / cms |
| 管理前端 | 是 | `mall-admin`、`mall-scm` |
| 配置发布 | 是 | `nacos-config-publisher`（写入后退出） |
| 用户端 H5 | 否 | 见 [mall-app/README](../mall-app/README.md) |

初始化库 `db/deko_mall.sql`；Nacos 命名空间 `deko-mall-docker`。

## 架构

```text
浏览器
  ├─ :9527  mall-admin ──/api──┐
  ├─ :9528  mall-scm   ──/api──┤
  └─ :8080  mall-app H5（可选）─┼──► gateway :9999 ──► 微服务
     MySQL Redis RabbitMQ MinIO Nacos
```

## 环境

| 依赖 | 说明 |
| --- | --- |
| Docker Compose v2 | 内存建议 ≥ 8 GB |
| JDK 17 · Maven 3.9+ | 后端镜像依赖本机 `mvn package`，不可跳过 |
| 磁盘 / 网络 | 约 15 GB；需 Docker Hub、Maven 中央仓库 |

Apple Silicon 需为 Nacos（AMD64）开启仿真；Windows 建议在 WSL2 Linux 文件系统中操作。

## 启动

```bash
git clone https://github.com/zhlog1024/deko-mall.git   # 或 gitee.com/zhlog1024/deko-mall
cd deko-mall && mvn clean package -DskipTests
cd deploy
cp .env.example .env    # 可选
docker compose up -d --build
```

确认：

```bash
docker compose ps --all
docker compose logs --tail=80 nacos-config-publisher
curl -f http://localhost:9527/healthz
curl -f http://localhost:9527/api/mall-auth/api/v1/auth/captcha
```

常驻服务应为 `running`；`nacos-config-publisher` 为 `Exited (0)`；验证码接口 `code` 为 `00000`。页面可能先于 API 就绪。

已构建过：`docker compose up -d`。改后端后先 `mvn clean package -DskipTests`，再 `docker compose up -d --build`。

## 访问

| 服务 | 地址 | 账号 |
| --- | --- | --- |
| 运营后台 | http://localhost:9527 | `admin` / `123456` |
| 供应商后台 | http://localhost:9528 | `supplier` / `123456` |
| 用户端 H5 | http://localhost:8080 | `--profile app` |
| 网关 | http://localhost:9999 | — |
| Nacos | http://localhost:8848/nacos | `nacos` / `nacos` |
| RabbitMQ | http://localhost:15672 | `deko` / `deko_mq_2024` |
| MinIO | http://localhost:9001 · API :9002 | `deko` / `deko_minio_2024` |
| MySQL | `127.0.0.1:3306` / `deko_mall` | `root` / `deko_root_2024` |
| Redis | `127.0.0.1:6379` | `deko_redis_2024` |

端口仅绑定 `127.0.0.1`。前端端口与本地开发一致：9527 / 9528 / 8080。

底栏 / Logo 等种子资源在 MinIO，重置后执行：`./deploy/minio/seed-minio-assets.sh`。

## 配置

```bash
cd deploy && cp .env.example .env
```

常用：`ADMIN_PORT`/`SCM_PORT`/`APP_PORT`（9527/9528/8080）、`GATEWAY_PORT`、`NACOS_NAMESPACE`、中间件密码（见 `.env.example`）。改中间件密码须同时改服务端账号与客户端配置。

Nacos 配置（`nacos/config/*.yaml`）改完后：

```bash
docker compose run --rm nacos-config-publisher
```

用户端 H5：HBuilderX 打开 `mall-app/` → 发行网站-H5 → `docker compose --profile app up -d`。细节见 [mall-app/README.md](../mall-app/README.md)。

## 运维

```bash
cd deploy
docker compose up -d --build --no-deps mall-admin mall-scm   # 只重建后台
docker compose logs -f mall-gateway
docker compose down       # 保留数据
docker compose down -v    # 清空数据卷
```

再次 `up` 不会用新 SQL 覆盖已有库。

## 常见问题

| 现象 | 处理 |
| --- | --- |
| 502 / 验证码失败 | 查 config-publisher、gateway、auth、Nacos |
| 端口占用 | `.env` 改 `*_PORT` 后重新 `up` |
| 退出码 137 | Docker 内存不足 |
| 刷新 404 | 确认使用本目录 `nginx-web.conf` |
| Nacos 无配置 | 须跑发布器，不会自动挂载目录 |

上线请更换默认密码，配置域名与 HTTPS，并自行备份。
