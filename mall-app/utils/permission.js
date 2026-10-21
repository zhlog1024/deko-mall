/**
 * Android 权限前置说明弹窗
 * 在调用 uni.chooseImage / uni.getLocation / uni.saveImageToPhotosAlbum 等
 * 会触发系统权限弹窗的 API 之前，先展示用途说明，满足 Android 应用市场合规要求。
 * 同一 cacheKey 仅弹一次，授权成功后下次不再弹。
 * iOS 和 H5/小程序平台自动跳过（iOS 系统弹窗自带用途描述，小程序有 scope 机制）。
 *
 * @param {Object}  options
 * @param {string}  options.cacheKey  权限唯一标识，用于记忆已授权状态（必传）
 * @param {string}  options.title     弹窗标题
 * @param {string}  options.content   弹窗内容
 */
export function showPermissionRationale(options = {}) {
  return new Promise((resolve) => {
    // #ifdef APP-PLUS
    var cacheKey = options.cacheKey ? 'perm_rt_' + options.cacheKey : null
    // 已授权过，直接放行
    if (cacheKey && uni.getStorageSync(cacheKey)) {
      resolve(true)
      return
    }
    uni.showModal({
      title: options.title || '',
      content: options.content || '',
      confirmText: options.confirmText || '我知道了',
      cancelText: options.cancelText || '暂不',
      success: (res) => {
        if (res.confirm && cacheKey) {
          uni.setStorageSync(cacheKey, 1)
        }
        resolve(res.confirm)
      }
    })
    // #endif
    // #ifndef APP-PLUS
    resolve(true)
    // #endif
  })
}

/**
 * 标记某个权限已授权（供 uni.xxx API 调用成功后调用）
 * 确保下次使用同一 API 时不再弹说明
 *
 * @param {string} cacheKey 权限唯一标识
 */
export function markPermissionAuthorized(cacheKey) {
  if (cacheKey) {
    uni.setStorageSync('perm_rt_' + cacheKey, 1)
  }
}

/**
 * 失败时引导用户前往系统设置页开启权限
 * Android 跳转应用详情设置页，iOS 跳转系统设置
 */
export function showPermissionDeniedGuide(options = {}) {
  // #ifdef APP-PLUS
  uni.showModal({
    title: options.title || '',
    content: options.content || '',
    confirmText: options.confirmText || '去设置',
    cancelText: options.cancelText || '取消',
    success: (res) => {
      if (res.confirm) {
        uni.openAppAuthorizeSetting()
      }
    }
  })
  // #endif
}
