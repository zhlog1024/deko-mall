const fs = require('fs')
const path = require('path')

let appEnvMode = 'dev'
if (process.env.NODE_ENV !== 'production') {
    try {
        const modeFile = path.resolve(__dirname, '.env_mode')
        appEnvMode = fs.readFileSync(modeFile, 'utf8').trim()
        console.log('[env_mode] 读取到环境模式:', appEnvMode)
    } catch (e) {
        console.log('[env_mode] .env_mode 文件不存在，使用默认值: dev')
    }
} else {
    appEnvMode = process.env.VUE_APP_ENV || 'prod'
    console.log('[env_mode] 生产构建，环境模式:', appEnvMode)
}

module.exports = {
    // webpack配置
    configureWebpack: {
        // 关闭 webpack 的性能提示
        performance: {
            hints: false
        }
    },
    chainWebpack(config) {
        config.plugin('define').tap(args => {
            args[0]['process.env.APP_ENV_MODE'] = JSON.stringify(appEnvMode)
            return args
        })
    }
}