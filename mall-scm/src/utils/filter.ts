/**
 * Show plural label if time is plural number
 * @param {number} time
 * @param {string} label
 * @return {string}
 */
function pluralize(time: number, label: string) {
  if (time === 1) {
    return time + label;
  }
  return time + label + "s";
}

/**
 * @param {number} time
 */
export function timeAgo(time: number) {
  const between = Date.now() / 1000 - Number(time);
  if (between < 3600) {
    return pluralize(~~(between / 60), " minute");
  } else if (between < 86400) {
    return pluralize(~~(between / 3600), " hour");
  } else {
    return pluralize(~~(between / 86400), " day");
  }
}

/**
 * Number formatting
 * like 10000 => 10k
 * @param {number} num
 * @param {number} digits
 */
export function numberFormatter(num: number, digits: number) {
  const si = [
    { value: 1e18, symbol: "E" },
    { value: 1e15, symbol: "P" },
    { value: 1e12, symbol: "T" },
    { value: 1e9, symbol: "G" },
    { value: 1e6, symbol: "M" },
    { value: 1e3, symbol: "k" },
  ];
  for (let i = 0; i < si.length; i++) {
    if (num >= si[i].value) {
      return (
        (num / si[i].value)
          .toFixed(digits)
          .replace(/\.0+$|(\.[0-9]*[1-9])0+$/, "$1") + si[i].symbol
      );
    }
  }
  return num.toString();
}

/**
 * 10000 => "10,000"
 * @param {number} num
 */
export function toThousandFilter(num: number) {
  return (+num || 0)
    .toString()
    .replace(/^-?\d+/g, (m) => m.replace(/(?=(?!\b)(\d{3})+$)/g, ","));
}

/**
 * Upper case first char
 * @param {String} string
 */
export function uppercaseFirst(string: string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}


/**
 * 金额格式化，处理精度问题并返回数值
 * @param {string|number} value - 需要格式化的金额值
 * @returns {number} - 返回两位小数的浮点数
 */
export function moneyFormatter(value: string) {
  // 处理null、undefined等情况
  if (value === null || value === undefined) {
    return 0.0;
  }

  // 如果是字符串，先尝试清理非法字符
  if (typeof value === "string") {
    value = value.replace(/[^\d.-]/g, "");
    if (value === "" || value === ".") {
      return 0.0;
    }
  }

  // 转换为数值并保留两位小数
  const num = parseFloat(value);
  return isNaN(num) ? 0.0 : parseFloat(num.toFixed(2));
}

/**
 * 将金额格式化为固定两位小数的字符串
 * @param {string|number} value - 需要格式化的金额值
 * @param {boolean} addSymbol - 是否添加货币符号¥
 * @returns {string} - 返回格式化后的字符串
 */
export function formatMoneyString(value: string, addSymbol = false) {
  const num = moneyFormatter(value);
  return addSymbol ? `¥${num.toFixed(2)}` : num.toFixed(2);
}

/**
 * 安全比较两个金额的大小
 * @param {string|number} value1 - 第一个金额
 * @param {string|number} value2 - 第二个金额
 * @returns {number} - 返回比较结果：-1(小于)、0(等于)、1(大于)
 */
export function compareAmount(value1: string, value2: string) {
  const num1 = moneyFormatter(value1);
  const num2 = moneyFormatter(value2);

  if (num1 < num2) return -1;
  if (num1 > num2) return 1;
  return 0;
}

/**
 * 安全加法：解决JavaScript浮点数计算精度问题
 * @param {...(string|number)} amounts - 需要相加的金额列表
 * @returns {number} - 返回相加后的结果
 */
export function addAmount(...amounts: (string | number)[]): number {
  return amounts.reduce((sum: number, amount: string | number): number => {
    return moneyFormatter(sum.toString()) + moneyFormatter(amount.toString());
  }, 0);
}

/**
 * 安全减法：解决JavaScript浮点数计算精度问题
 * @param {string|number} minuend - 被减数
 * @param {string|number} subtrahend - 减数
 * @returns {number} - 返回相减后的结果
 */
export function subtractAmount(
  minuend: string | number,
  subtrahend: string | number
): number {
  return moneyFormatter(
    String(
      moneyFormatter(minuend.toString()) - moneyFormatter(subtrahend.toString())
    )
  );
}

/**
 * 安全乘法：用于计算数量*单价等场景
 * @param {string|number} amount - 金额
 * @param {string|number} multiplier - 乘数
 * @returns {number} - 返回相乘后的结果
 */
export function multiplyAmount(
  minuend: string | number,
  subtrahend: string | number
): number {
  return moneyFormatter(
    String(
      moneyFormatter(minuend.toString()) * moneyFormatter(subtrahend.toString())
    )
  );
}
