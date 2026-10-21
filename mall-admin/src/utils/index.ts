/**
 * Check if an element has a class
 * @param {HTMLElement} ele
 * @param {string} cls
 * @returns {boolean}
 */
export function hasClass(ele: HTMLElement, cls: string) {
  return !!ele.className.match(new RegExp("(\\s|^)" + cls + "(\\s|$)"));
}

export function parseTime(time: any, cFormat?: string) {
  if (arguments.length === 0 || !time) {
    return null;
  }
  const format = cFormat || "{y}-{m}-{d} {h}:{i}:{s}";
  let date;
  if (typeof time === "object") {
    date = time;
  } else {
    if (typeof time === "string") {
      if (/^[0-9]+$/.test(time)) {
        // support "1548221490638"
        time = parseInt(time);
      } else {
        // support safari
        // https://stackoverflow.com/questions/4310953/invalid-date-in-safari
        time = time.replace(new RegExp(/-/gm), "/");
      }
    }

    if (typeof time === "number" && time.toString().length === 10) {
      time = time * 1000;
    }
    date = new Date(time);
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
  };
  const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
    const value = formatObj[key];
    // Note: getDay() returns 0 on Sunday
    if (key === "a") {
      return ["日", "一", "二", "三", "四", "五", "六"][value];
    }
    return value.toString().padStart(2, "0");
  });
  console.log(time_str);

  return time_str;
}

/**
 * Add class to element
 * @param {HTMLElement} ele
 * @param {string} cls
 */
export function addClass(ele: HTMLElement, cls: string) {
  if (!hasClass(ele, cls)) ele.className += " " + cls;
}

/**
 * Remove class from element
 * @param {HTMLElement} ele
 * @param {string} cls
 */
export function removeClass(ele: HTMLElement, cls: string) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp("(\\s|^)" + cls + "(\\s|$)");
    ele.className = ele.className.replace(reg, " ");
  }
}

/**
 *  判断是否为外部链接
 *
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path: string) {
  const isExternal = /^(https?:|http?:|mailto:|tel:)/.test(path);
  return isExternal;
}

/**
 * 设置Style属性
 */
export function setStyleProperty(propName: string, value: string) {
  document.documentElement.style.setProperty(propName, value);
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
  amount: string | number,
  multiplier: string | number
): number {
  return moneyFormatter(
    String(
      moneyFormatter(amount.toString()) * moneyFormatter(multiplier.toString())
    )
  );
}
