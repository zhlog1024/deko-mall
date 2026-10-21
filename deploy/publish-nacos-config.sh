#!/usr/bin/env bash
# =============================================================================
# publish-nacos-config.sh — 把 deploy/nacos/config/*.yaml 发布到 Nacos 配置中心
# =============================================================================
# 用法
# ----
#   deploy/publish-nacos-config.sh                     # 用默认值
#   NACOS_ADDR=nacos:8848 deploy/publish-nacos-config.sh
#   NAMESPACE=deko-mall-dev deploy/publish-nacos-config.sh
#
# 环境变量
# -------
#   NACOS_ADDR      Nacos 地址 host:port（默认 localhost:8848）
#   NACOS_USERNAME  用户名（默认 nacos）
#   NACOS_PASSWORD  密码（默认 nacos）
#   NACOS_GROUP     配置分组（默认 DEFAULT_GROUP）
#   NAMESPACE       命名空间 ID（默认空 = public）
#   NACOS_ACCESS_TOKEN  已启用鉴权时可传入访问令牌
#   CONFIG_DIR      配置目录（默认脚本同级 nacos/config）
#   CREATE_NAMESPACE  为 true 时创建缺失的命名空间（默认 false）
#   MAX_WAIT        等待 Nacos 就绪的最长秒数（默认 180）
#
# 退出码: 0 = 全部发布成功, 1 = 有失败, 2 = 用法/环境错误
# =============================================================================
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

NACOS_ADDR="${NACOS_ADDR:-localhost:8848}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NAMESPACE="${NAMESPACE:-}"
[[ "$NAMESPACE" == "public" ]] && NAMESPACE=""
CONFIG_DIR="${CONFIG_DIR:-$SCRIPT_DIR/nacos/config}"
MAX_WAIT="${MAX_WAIT:-180}"

# 颜色（非 TTY 时关闭）
C_RESET=''; C_RED=''; C_GRN=''; C_YEL=''; C_DIM=''; C_BLD=''
if [[ -t 1 ]]; then
  C_RESET=$'\033[0m'; C_RED=$'\033[31m'; C_GRN=$'\033[32m'
  C_YEL=$'\033[33m'; C_DIM=$'\033[2m'; C_BLD=$'\033[1m'
fi

log()  { printf '%s\n' "$*"; }
ok()   { printf '  %s✓%s %s\n' "$C_GRN" "$C_RESET" "$*"; }
warn() { printf '  %s!%s %s\n' "$C_YEL" "$C_RESET" "$*"; }
err()  { printf '  %s✗%s %s\n' "$C_RED" "$C_RESET" "$*" >&2; }

command -v curl >/dev/null 2>&1 || { err "缺少 curl"; exit 2; }
[[ -d "$CONFIG_DIR" ]] || { err "配置目录不存在: $CONFIG_DIR"; exit 2; }

CONFIG_FILES=("$CONFIG_DIR"/*.yaml)
if (( ${#CONFIG_FILES[@]} == 0 )) || [[ ! -f "${CONFIG_FILES[0]}" ]]; then
  err "配置目录内无 .yaml: $CONFIG_DIR"
  exit 2
fi

case "$NACOS_ADDR" in
  http://*|https://*) BASE="${NACOS_ADDR%/}/nacos" ;;
  *) BASE="http://${NACOS_ADDR}/nacos" ;;
esac
WORK_DIR="$(mktemp -d)" || exit 2
trap 'rm -rf "$WORK_DIR"' EXIT
AUTH_ARGS=()

# 1. 等待 Nacos 就绪
# 只等「进程起来了」，不关心配置是否已发；发布动作本身幂等。
log "${C_BLD}等待 Nacos 就绪${C_RESET}  ${C_DIM}${BASE}${C_RESET}"
ready=0
waited=0
while (( waited < MAX_WAIT )); do
  if curl -fsS --max-time 3 \
       "${BASE}/v1/console/health/readiness" >/dev/null 2>&1; then
    ready=1; break
  fi
  sleep 3; waited=$(( waited + 3 ))
  (( waited % 15 == 0 )) && printf '  %s… 已等待 %ss%s\n' "$C_DIM" "$waited" "$C_RESET"
done

if (( ready == 0 )); then
  err "Nacos 在 ${MAX_WAIT}s 内未就绪（${BASE}）"
  err "排查: docker compose logs nacos"
  exit 1
fi
ok "Nacos 已就绪（等待 ${waited}s）"

# Nacos 原生鉴权使用 accessToken；HTTP Basic 和自定义 Namespace 请求头不能替代它。
token="${NACOS_ACCESS_TOKEN:-}"
if [[ -z "$token" ]]; then
  if curl -fsS --max-time 10 -X POST "${BASE}/v1/auth/users/login" \
      --data-urlencode "username=${NACOS_USERNAME}" \
      --data-urlencode "password=${NACOS_PASSWORD}" -o "$WORK_DIR/login.json" 2>/dev/null; then
    token="$(sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$WORK_DIR/login.json")"
  fi
fi
AUTH_ARGS=(--data-urlencode "accessToken=${token}")
# Before any writes, validate access and namespace existence. Empty means public.
if ! curl -fsS --max-time 10 -G "${AUTH_ARGS[@]}" "${BASE}/v1/console/namespaces" \
    -o "$WORK_DIR/namespaces.json"; then
  err "无法读取命名空间；请检查 Nacos 凭据"; exit 1
fi
if [[ -n "$NAMESPACE" ]] && ! tr -d '[:space:]' < "$WORK_DIR/namespaces.json" | grep -Fq "\"namespace\":\"${NAMESPACE}\""; then
  if [[ "${CREATE_NAMESPACE:-false}" != "true" ]]; then
    err "命名空间 ID 不存在：${NAMESPACE}（请先创建）"; exit 1
  fi
  if ! resp="$(curl -fsS --max-time 10 -X POST "${BASE}/v1/console/namespaces" \
      "${AUTH_ARGS[@]}" --data-urlencode "customNamespaceId=${NAMESPACE}" \
      --data-urlencode "namespaceName=${NAMESPACE}" \
      --data-urlencode "namespaceDesc=Deko Mall Docker development")" || [[ "$resp" != "true" ]]; then
    err "创建命名空间失败：${NAMESPACE}"; exit 1
  fi
  ok "已创建命名空间 ${NAMESPACE}"
fi

# 2. 逐个发布
#
# 用 POST /v1/cs/configs 发布（content 走 --data-urlencode，避免特殊字符截断）。
# 该接口是**幂等**的：同 dataId+group 已存在则覆盖，故可重复执行。
log ""
log "${C_BLD}发布配置${C_RESET}  ${C_DIM}group=${NACOS_GROUP}${NAMESPACE:+ namespace=${NAMESPACE}}${C_RESET}"

fail=0
count=0
for f in "${CONFIG_FILES[@]}"; do
  [[ -f "$f" ]] || continue
  data_id="$(basename "$f")"
  count=$(( count + 1 ))

  # --fail-with-body 需要 curl 7.76+；为兼容旧版只用 --fail，失败时单独取错误
  if resp="$(curl -fsS --max-time 15 "${AUTH_ARGS[@]}" -X POST "${BASE}/v1/cs/configs" \
        --data-urlencode "dataId=${data_id}" \
        --data-urlencode "group=${NACOS_GROUP}" \
        --data-urlencode "tenant=${NAMESPACE}" \
        --data-urlencode "type=yaml" \
        --data-urlencode "content@${f}" 2>&1)"; then
    if [[ "$resp" == "true" ]]; then
      ok "${data_id}"
    else
      err "${data_id}  发布返回非 true: ${resp}"
      fail=$(( fail + 1 ))
    fi
  else
    err "${data_id}  请求失败: ${resp}"
    fail=$(( fail + 1 ))
  fi
done

# 3. 校验：逐个回读，确认内容确实落地
#
# 只信「发布接口返回 true」是不够的 —— 认证/命名空间配错时可能写入到别处。
# 回读能证明「服务按 dataId 能取到内容」，这正是服务启动时的真实路径。
#
# 回读必须用 **查询串**，不能用 `-X GET --data-urlencode`：
# 后者把参数放进请求体，而 Nacos 的服务端只从 query string 取参，
# 实测返回 500「Required request parameter 'dataId' ... is not present」。
# 发布接口（POST /v1/cs/configs）则相反，参数放 body 才正确 —— 两者写法不同。
log ""
log "${C_BLD}校验回读${C_RESET}"
missing=0
for f in "${CONFIG_FILES[@]}"; do
  [[ -f "$f" ]] || continue
  data_id="$(basename "$f")"
  verified=0
  # 首次发布后配置缓存可能尚未刷新，有限重试后仍须逐字一致。
  for attempt in {1..10}; do
    if curl -fsS --max-time 10 "${AUTH_ARGS[@]}" -G "${BASE}/v1/cs/configs" \
          --data-urlencode "dataId=${data_id}" \
          --data-urlencode "group=${NACOS_GROUP}" \
          --data-urlencode "tenant=${NAMESPACE}" -o "$WORK_DIR/readback.yaml" 2>/dev/null \
        && cmp -s "$f" "$WORK_DIR/readback.yaml"; then
      verified=1
      break
    fi
    (( attempt < 10 )) && sleep 1
  done
  if (( verified )); then
    ok "${data_id}  ${C_DIM}(内容完全一致)${C_RESET}"
  else
    err "${data_id}  回读失败或内容不一致"; missing=$(( missing + 1 ))
  fi
done

log ""
if (( fail > 0 || missing > 0 )); then
  printf '%s%s  ❌ 发布未全部成功%s  发布失败 %d / 回读缺失 %d（共 %d 个）\n' \
    "$C_RED" "$C_BLD" "$C_RESET" "$fail" "$missing" "$count"
  exit 1
fi

printf '%s%s  ✅ 全部 %d 个配置已发布并回读确认%s\n' "$C_GRN" "$C_BLD" "$count" "$C_RESET"
exit 0
