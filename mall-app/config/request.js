import config from '@/config'

var qsStringify = function(obj) {
  return Object.keys(obj)
    .filter(function(key) { return obj[key] !== undefined })
    .map(function(key) { return key + '=' + encodeURIComponent(obj[key]) })
    .join('&')
}

let redirecting = false  // 防重入：多个 A0230 只跳一次

/**
 * 客户端类型标识
 * 通过 uni-app 条件编译区分编译目标端，随每个请求 query 参数传递，
 * 供后端（如 DIY 首页按端配置）识别当前端。
 */
var getClientType = function () {
  // #ifdef MP-WEIXIN
  return 'weixin'
  // #endif
  // #ifdef H5
  return 'h5'
  // #endif
  // #ifdef APP
  return 'app'
  // #endif
  // #ifndef MP-WEIXIN
  // #ifndef H5
  // #ifndef APP
  return 'weixin'
  // #endif
  // #endif
  // #endif
}

export const request = option=>{
  return new Promise((resolve,reject)=>{
    let header = {
      'content-type': 'application/json',
      ...option.headers
    }
    // 在开发环境中加入 ngrok 跳过浏览器警告，用于调试隧道
    if (process.env.NODE_ENV === 'development') {
      header['ngrok-skip-browser-warning'] = '1'
    }
    
    // 自动添加 token（如果存在且不是登录接口）
    const isLoginEndpoint = option.url && option.url.includes('/oauth2/token');
    if (!isLoginEndpoint) {
      const token = uni.getStorageSync('token')
      if (token) {
        header['Authorization'] = token;
      }
    }
    // 自动添加语言 header（配合后端国际化），映射 uni 简写到 BCP 47 格式
    var localeMap = { 'en': 'en-US', 'zh': 'zh-CN', 'zh-Hans': 'zh-CN' };
    var rawLocale = uni.getLocale();
    header['Accept-Language'] = localeMap[rawLocale] || rawLocale || 'zh-CN';
    let url = config.url + option.url
    // 自动附加客户端类型标识
    const clientType = getClientType()
    if (clientType) {
      url += (url.indexOf('?') == -1 ? '?' : '&') + 'application_client_type=' + encodeURIComponent(clientType)
    }
    if(option.params){
      if(url.indexOf('?')==-1){
        url+='?'+qsStringify(option.params)
      }else{
        url+='&'+qsStringify(option.params)
      }
    }
    
    console.log(url);
    
    uni.request({
      ...option,
      url,
      header,
      method: option.method || 'GET',
      success:res=>{
        const data = res.data
        if(data.code=='00000'){
          resolve(data)

        }else
        if (data.code === 'A0230') {
          if (!redirecting) {
            redirecting = true
            var app = getApp()
            if (app && app.globalData && app.globalData.remove_user_cache_event) {
              app.globalData.remove_user_cache_event()
            } else {
              uni.removeStorageSync('user')
              uni.removeStorageSync('token')
            }
            uni.showToast({ title: '会话已过期，请重新登录' })
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/login/login' })
              redirecting = false
            }, 1000)
          }
          reject(data)
        } else {
          // 统一使用弹窗显示错误信息
          uni.showModal({
            title: '提示',
            content: data.msg,
            showCancel: false
          });
          reject(new Error(data.msg || 'Errors'))
        }
      }
    })
  })

}