/**
 * 批次效期判定（库存/批次页面共用）
 *
 * 口径：expireDate 距今 < 30 天视为「效期临近」，已过期单独标记。
 */
export const EXPIRE_WARN_DAYS = 30;

/** 距到期天数（含当天，负数=已过期）；无到期日/非法日期返回 null */
export function daysToExpire(expireDate?: string | null): number | null {
  if (!expireDate) return null;
  const target = new Date(`${String(expireDate).slice(0, 10)}T00:00:00`);
  if (Number.isNaN(target.getTime())) return null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Math.round((target.getTime() - today.getTime()) / 86400000);
}

export type ExpireLevel = "expired" | "warning" | "normal" | "unknown";

/** 效期等级：expired-已过期 warning-临近(<30天) normal-正常 unknown-无到期日 */
export function expireLevel(expireDate?: string | null): ExpireLevel {
  const days = daysToExpire(expireDate);
  if (days === null) return "unknown";
  if (days < 0) return "expired";
  if (days < EXPIRE_WARN_DAYS) return "warning";
  return "normal";
}

/** 效期后缀文案，如「(还剩 12 天)」 */
export function expireSuffix(expireDate?: string | null): string {
  const days = daysToExpire(expireDate);
  if (days === null) return "";
  if (days < 0) return `（已过期 ${Math.abs(days)} 天）`;
  if (days === 0) return "（今天到期）";
  return `（还剩 ${days} 天）`;
}
