#!/usr/bin/env bash
# 将 App 默认远程资源（底栏图标、Logo、快捷入口等）预置到 MinIO，
# 供 sys_app_config / cms_diy_app_tabbar 等配置直接引用。
#
# 用法（在仓库根或 deploy/ 下）:
#   ./deploy/minio/seed-minio-assets.sh
#
# 环境变量:
#   MINIO_CONTAINER   默认 deko-minio
#   MINIO_ROOT_USER   默认 deko
#   MINIO_ROOT_PASSWORD 默认 deko_minio_2024
#   MINIO_BUCKET      默认 deko-mall
#   MINIO_PUBLIC_BASE 对外访问前缀，默认 http://127.0.0.1:9002/deko-mall
#                     （本机 auth 占 9000 时，compose 常把 MinIO API 映到 9002）

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
APP_STATIC="${ROOT}/mall-app/static"
CONTAINER="${MINIO_CONTAINER:-deko-minio}"
ACCESS_KEY="${MINIO_ROOT_USER:-deko}"
SECRET_KEY="${MINIO_ROOT_PASSWORD:-deko_minio_2024}"
BUCKET="${MINIO_BUCKET:-deko-mall}"
PUBLIC_BASE="${MINIO_PUBLIC_BASE:-http://127.0.0.1:9002/${BUCKET}}"
STAGING="$(mktemp -d)"
trap 'rm -rf "${STAGING}"' EXIT

if ! docker inspect "${CONTAINER}" >/dev/null 2>&1; then
  echo "MinIO 容器未运行: ${CONTAINER}" >&2
  exit 1
fi

# object_key(相对 seed/) -> 本地源文件
declare -a MAP=(
  "app/common/tabbar/home.png|${APP_STATIC}/app/common/tabbar/home.png"
  "app/common/tabbar/home-current.png|${APP_STATIC}/app/common/tabbar/home-current.png"
  "app/common/tabbar/category.png|${APP_STATIC}/app/common/tabbar/category.png"
  "app/common/tabbar/category-current.png|${APP_STATIC}/app/common/tabbar/category-current.png"
  "app/common/tabbar/cart.png|${APP_STATIC}/app/common/tabbar/cart.png"
  "app/common/tabbar/cart-current.png|${APP_STATIC}/app/common/tabbar/cart-current.png"
  "app/common/tabbar/user.png|${APP_STATIC}/app/common/tabbar/user.png"
  "app/common/tabbar/user-current.png|${APP_STATIC}/app/common/tabbar/user-current.png"
  "app/common/price.png|${APP_STATIC}/app/common/price.png"
  "app/warm/common/images/logo.png|${APP_STATIC}/app/warm/common/images/logo.png"
  "app/warm/common/user.png|${APP_STATIC}/app/warm/common/user.png"
  "app/warm/common/online-service-icon.png|${APP_STATIC}/app/warm/common/online-service-icon.png"
  # quick_nav 历史路径 /static/images/common/* —— 用 warm 主题图标填充
  "images/common/coupon-icon.png|${APP_STATIC}/app/warm/common/quick-icon.png"
  "images/common/favor-icon.png|${APP_STATIC}/app/warm/common/favor-icon.png"
  "images/common/customer-service-icon.png|${APP_STATIC}/app/warm/common/customer-service-icon.png"
)

echo "==> stage files under seed/"
for entry in "${MAP[@]}"; do
  key="${entry%%|*}"
  src="${entry#*|}"
  if [[ ! -f "${src}" ]]; then
    echo "missing source: ${src}" >&2
    exit 1
  fi
  dest="${STAGING}/seed/${key}"
  mkdir -p "$(dirname "${dest}")"
  cp -f "${src}" "${dest}"
done

echo "==> upload to ${CONTAINER}:${BUCKET}/seed/"
docker cp "${STAGING}/seed" "${CONTAINER}:/tmp/deko-seed"
docker exec "${CONTAINER}" sh -c "
  mc alias set local http://127.0.0.1:9000 '${ACCESS_KEY}' '${SECRET_KEY}' >/dev/null
  mc mb -p 'local/${BUCKET}' 2>/dev/null || true
  mc anonymous set download 'local/${BUCKET}' >/dev/null
  mc cp -r /tmp/deko-seed/ 'local/${BUCKET}/seed/'
  rm -rf /tmp/deko-seed
"

echo "==> public URLs (MINIO_PUBLIC_BASE=${PUBLIC_BASE})"
while IFS= read -r -d '' f; do
  rel="${f#${STAGING}/}"
  echo "  ${PUBLIC_BASE}/${rel}"
done < <(find "${STAGING}" -type f -print0 | sort -z)

echo "done."
