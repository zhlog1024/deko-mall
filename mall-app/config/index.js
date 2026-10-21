
const systemInfo = uni.getSystemInfoSync()
const inApp = systemInfo.uniPlatform === 'app'
// 优先使用环境变量，其次使用硬编码默认值
const appEnv = process.env.VUE_APP_ENV || 'dev'
const appTerminal = getAppTerminal(systemInfo)
// 获取manifest.json中的版本号
const version = systemInfo.appVersion

const base_url = {
  // IDE 本机网关
  dev: 'http://127.0.0.1:9999',
  // HBuilderX「发行」走 prod；对接本地 Compose 网关（宿主机 9999）
  prod: 'http://127.0.0.1:9999'
}


export const getEnv = () => {
  // 第一优先级：.env_mode 文件注入（最可靠，用于本地环境切换）
  if (process.env.APP_ENV_MODE) {
    console.log('[ENV] 使用 .env_mode 注入环境:', process.env.APP_ENV_MODE)
    return process.env.APP_ENV_MODE
  }

  // 第二优先级：编译时注入的环境变量
  if (process.env.VUE_APP_ENV) {
    console.log('[ENV] 使用编译时环境变量:', process.env.VUE_APP_ENV)
    return process.env.VUE_APP_ENV
  }

  // 第二优先级：根据 NODE_ENV 判断（HBuilderX 发行时会设置）
  if (process.env.NODE_ENV === 'production') {
    console.log('[ENV] 使用 NODE_ENV=production，返回 prod')
    return 'prod'
  }

  // APP 环境优先使用 appEnv
  if (inApp) {
    console.log('[ENV] APP 环境，使用默认值:', appEnv)
    return appEnv
  }

  console.log('[ENV] 使用默认值: dev')
  return 'dev'
}

function getAppTerminal({ uniPlatform, osName }) {

  if (uniPlatform === 'mp-weixin') {
    return 'APPLET'
  } else if (osName === 'android') {
    return 'ANDROID'
  } else if (osName === 'ios') {
    return 'IOS'
  }
  return 'APPLET'
}

export const gatewayConfig = () => {
  const env = getEnv()
  const url = base_url[env]
  return {
    url,
    // iOS 应用商店 ID —— 中性占位（原值指向真实上架应用，已移除）
    iOSAppStoreId: '0000000000',
    appId: 'wx0000000000000000',
    // 腾讯位置服务 key —— 中性占位（占位值刻意不用 `XXXXX-XXXXX-...` 形态：
    // 避免写成真实 key 形状的占位串）。
    // 原值曾是真实 key 并随仓库公开，导致配额被第三方耗尽；
    // 请到 https://lbs.qq.com/ 申请自己的 key 后替换。
    qqmapKey: 'YOUR_QQMAP_KEY',
    version: version,
    appTerminal
  }
}

export default gatewayConfig()