<!--
 * @Description  : 
 * @Version      : 1.0
 * @Author       : 
 * @Date         : 2025-02-21 13:05:16
 * @LastEditors  : zhlog1024
 * @LastEditTime : 2025-06-05 11:24:25
-->
<script>
/**
 * 使用 uni.Storage 管理登录状态。
 */
import config from '@/config'
import { showPermissionRationale } from '@/utils/permission'
import i18n from '@/locale/index'
export default {
  // 全局数据与方法。
  globalData: {
    // 组件通过 app.globalData.data 读取配置。
    data: {
      // 系统类型
      system_type: 'default',

      // 基础信息
      application_title: 'Deko Mall',
      application_describe: 'Deko Mall - 好的生活，值得甄选',
      // 默认logo，替换 static/common/images/logo.png 即可
      application_logo: '/static/app/warm/common/images/logo.png',

      // 版本信息（自动读取 manifest.json 的版本号）
      app_version_info: config.version,

      // 货币符号
      currency_symbol: '￥',

      // 默认主题
      default_theme: 'warm',

      // 系统tabbar
      system_tabbar: [
        '/pages/index/index',
        '/pages/goods-category/goods-category',
        '/pages/cart/cart',
        '/pages/user/user'
      ],

      // 是否使用原生菜单（0否, 1是）
      is_use_native_tabbar: 0,

      // 商品列表是否展示购物车（0否, 1是）
      is_goods_list_show_cart_opt: 0,

      // 开启浮动客服（0否, 1是）
      is_online_service_fixed: 1,

      // 是否开启微信隐私弹窗授权提示（0否, 1是）
      is_weixin_privacy_setting: 0,

      // 加载是否使用骨架屏（0否, 1是）
      is_loading_use_skeleton: 0,

      // 加载动画类型（0logo, 1名称）
      loading_content_type: 0,

      // 用户中心菜单展示模式（0九宫格, 1列表）
      user_center_nav_show_model_type: 0,

      // 分类页面商品列表模式一级分类使用图标类型（0 实景图, 1 icon图标, 2 大图片）
      category_goods_model_icon_type: 0,

      // 商品分类页面搜索进入独立搜索页面（0否, 1是）
      category_goods_is_search_alone: 0,

      // 商品分类页面开启购物车导航（0否, 1是）
      category_goods_is_show_cart_nav: 0,

      // 公共数据是否已初始化成功
      common_data_init_status: 0,

      // 配置加载状态（0-未加载, 1-加载中, 2-已完成）
      common_data_loading_status: 0,

      // 配置回调队列
      common_data_init_config_back_list: [],

      // 缓存key
      cache_user_info_key: 'user', // 用户信息缓存 key（Storage 为唯一数据源）
      cache_system_info_key: 'cache_system_info_key',
      cache_buy_user_address_select_key: 'cache_buy_user_address_select_key', // 订单确认页地址选择缓存 key
      cache_search_history_key: 'cache_search_history_key', // 搜索历史缓存 key
      cache_app_update_tips_interval_time_key: 'cache_app_update_tips_interval_time_key',
      cache_app_star_tips_interval_time_key: 'cache_app_star_tips_interval_time_key',
      cache_user_base_personal_interval_time_key: 'cache_user_base_personal_interval_time_key',
      cache_diy_data_key: 'cache_diy_data_key',
      cache_diy_page_data_key: 'cache_diy_page_data_key',
      cache_tabbar_badge_key: 'cache_tabbar_badge_key',
      cache_app_diy_tabbar_height_key: 'cache_app_diy_tabbar_height_key',
      cache_app_system_tabbar_height_key: 'cache_app_system_tabbar_height_key',

      // 配置缓存 key
      cache_config_info_key: 'cache_config_info_key',

      // 用户登录缓存key
      cache_user_login_key: 'cache_user_login_key',

      // 上一页地址缓存key
      cache_prev_page_key: 'cache_prev_page_key',

      // tab页面切换参数
      cache_page_tabbar_switch_params: 'cache_page_tabbar_switch_params_key',

      // 未登录页面缓存记录key
      cache_user_no_login_page_status_data_key: 'cache_user_no_login_page_status_data_key',

      // 首页数据缓存key
      cache_index_data_key: 'cache_index_data_key',

      // 启动参数缓存key
      cache_launch_info_key: 'cache_shop_launch_info_key',

      // 用户位置缓存key
      cache_userlocation_key: 'cache_userlocation_key',

      // 是否获取用户位置（0关闭，1开启）
      get_user_location_status: 0,

      // 默认用户头像
      default_user_head_src: '/static/app/warm/common/user.png',
  },

  /**
   * 获取底部导航badge
   */
  get_tab_bar_badge(type) {
    if ((type || null) != null) {
      return uni.getStorageSync(this.data.cache_tabbar_badge_key + '-' + type) || null
    }
    return null
  },

  /**
   * 设置底部导航badge
   */
  set_tab_bar_badge(type, value = 0) {
      // 是否使用原生菜单
      if(this.data.is_use_native_tabbar == 1) {
          if(type == 'cart') {
              if ((value || 0) == 0) {
                  uni.removeTabBarBadge({
                      index: 2
                  });
              } else {
                  uni.setTabBarBadge({
                      index: 2,
                      text: value.toString()
                  });
              }
          }
      } else {
          // 数据处理
          if ((type || null) != null) {
              var key = this.data.cache_tabbar_badge_key+'-'+type;
              if ((value || null) == null) {
                  uni.removeStorageSync(key);
              } else {
                  uni.setStorageSync(key, value);
              }
          }
          // 更新底部菜单数据
          var obj = this.get_page_object() || null;
          if(obj != null && (obj.$vm || null) != null && (obj.$vm.$refs || null) != null && (obj.$vm.$refs.common || null) != null) {
              obj.$vm.$refs.common.footer_init();
          }
      }
  },

  /**
   * 获取当前时间戳（秒）
     */
    get_timestamp() {
      return parseInt(new Date().getTime() / 1000)
    },

    /**
     * 生成 UUID（直播 WS 等插件协议使用）
     */
    uuid() {
      var d = new Date().getTime()
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random() * 16) % 16 | 0
        d = Math.floor(d / 16)
        return (c == 'x' ? r : (r & 0x3) | 0x8).toString(16)
      })
    },

    /**
     * 获取当前 uuid（本地缓存，直播 WS 协议使用）
     */
    request_uuid() {
      var uuid = uni.getStorageSync('cache_user_uuid_key') || null
      if (uuid == null) {
        uuid = this.uuid()
        uni.setStorage({
          key: 'cache_user_uuid_key',
          data: uuid,
          fail: () => {}
        })
      }
      return uuid
    },

    /**
     * 获取设备品牌（直播 WS 协议使用，小写）
     */
    application_client_brand() {
      var value = this.get_system_info('brand', null, true)
      return (value === null) ? '' : value.toLowerCase()
    },

    /**
     * 获取设备信息
     */
    get_system_info(key, dv, is_real) {
      try {
        const cache_key = 'cache_system_info_key'
        let info = null

        if (is_real === true) {
          info = this.set_system_info() || null
        } else {
          info = uni.getStorageSync(cache_key) || null
        }

        if (info == null || key == null) {
          return info
        }

        return info[key] === undefined ? (dv === undefined ? null : dv) : info[key]
      } catch (e) {
        console.error('get_system_info error:', e)
        return dv === undefined ? null : dv
      }
    },

    /**
     * 设置设备信息
     */
    set_system_info() {
      try {
        const system_info = uni.getSystemInfoSync()
        const cache_key = 'cache_system_info_key'
        uni.setStorageSync(cache_key, system_info)
        return system_info
      } catch (e) {
        console.error('set_system_info error:', e)
        return null
      }
    },

    /**
     * 窗口宽度处理
     */
    window_width_handle(width) {
      // #ifdef H5 || APP
      if (width > 960) {
        width = 800
      }
      // #endif
      return width
    },

    /**
     * 窗口高度处理
     */
    window_height_handle(system) {
      var height = system.windowHeight
      if (system.statusBarHeight > 0) {
        height += system.statusBarHeight
      }
      if (system.windowTop > 0) {
        height += system.windowTop
      }
      if (system.windowBottom > 0) {
        height += system.windowBottom
      }
      return height
    },

    /**
     * URL事件处理
     */
    url_event(e) {
      if ((e.currentTarget || null) != null && (e.currentTarget.dataset || null) != null) {
        var value = e.currentTarget.dataset.value || null
        var is_redirect = parseInt(e.currentTarget.dataset.redirect || 0) == 1
        this.url_open(value, is_redirect)
      }
    },

    /**
     * 处理页面跳转、地图和拨号等操作。
     * event_type：0 网页、1 内部页面、2 外部小程序、3 地图、4 电话。
     */
    operation_event(e) {
      var value = e.currentTarget.dataset.value || null
      var type = parseInt(e.currentTarget.dataset.type)
      if (value == null) return
      switch (type) {
        case 0:
          this.open_web_view(value)
          break
        case 1:
          if (this.is_system_tabbar_pages(value)) {
            var temp = value.split('?')
            if (temp.length > 1 && (temp[1] || null) != null) {
              value = temp[0]
              uni.setStorageSync(this.data.cache_page_tabbar_switch_params, this.url_params_to_json(temp[1]))
            }
            uni.switchTab({ url: value })
            this.system_hide_tabbar()
          } else {
            uni.navigateTo({ url: value })
          }
          break
        case 2:
          var arr = value.split('|')
          uni.navigateToMiniProgram({
            appId: arr[0],
            path: arr[1] || '',
            extraData: arr[2] || '',
            envVersion: arr[3] || 'release'
          })
          break
        case 3:
          var loc = value.split('|')
          if (loc.length != 4) {
            this.showToast('位置信息不完整')
            return
          }
          this.open_location(loc[2], loc[3], loc[0], loc[1])
          break
        case 4:
          this.call_tel(value)
          break
      }
    },

    /**
     * URL打开
     */
    url_open(value, is_redirect = false) {
      if ((value || null) != null) {
        // #ifdef MP
        var pages = getCurrentPages()
        if (pages.length >= 10) {
          is_redirect = true
        }
        // #endif

        if (this.is_url(value)) {
          // web地址，打开webview
          uni.navigateTo({
            url: '/pages/web-view/web-view?url=' + encodeURIComponent(value)
          })
        } else if (value.substr(0, 6) === 'tel://') {
          // 电话协议
          uni.makePhoneCall({
            phoneNumber: value.substr(6).toString()
          })
        } else if (value.substr(0, 7) === 'scan://') {
          // 扫码协议
          this.scan_handle()
        } else {
          // 默认页面跳转
          if (this.is_page(value)) {
            // TabBar 页面使用 switchTab 切换
            if (this.is_system_tabbar_pages(value)) {
              var temp = value.split('?')
              if (temp.length > 1 && (temp[1] || null) != null) {
                value = temp[0]
                var query = this.url_params_to_json(temp[1])
                uni.setStorageSync(this.data.cache_page_tabbar_switch_params, query)
              }
              uni.switchTab({
                url: value
              })
              // 隐藏系统 tabbar
              this.system_hide_tabbar()
            } else {
              if (is_redirect) {
                uni.redirectTo({ url: value })
              } else {
                uni.navigateTo({ url: value })
              }
            }
          } else {
            // 搜索关键词
            // 修改为跳转到现有的商品列表页
            uni.navigateTo({
              url: '/pages/goods-search-start/goods-search-start'
            })
          }
        }
      }
    },

    /**
     * 是否为URL地址
     */
    is_url(value) {
      var arr = ['http:/', 'https:']
      return arr.indexOf(value.substr(0, 6)) != -1
    },

    /**
     * 是否为page页面地址（含分包路径）
     */
    is_page(value) {
      var prefix = value.substr(0, 6)
      // 主包 + 分包路径
      var arr = ['/pages', '/pageU', '/pageO', '/pageP', 'pages/', 'pageUs', 'pageOr', 'pagePr']
      return arr.indexOf(prefix) != -1
    },

    /**
     * 货币符号
     */
    currency_symbol() {
      return this.get_config('currency_symbol', '￥')
    },

    /**
     * rpx转px
     */
    rpx_to_px(value) {
      return (value || 0) == 0 ? 0 : uni.upx2px(value * 2)
    },

    /**
     * 读取用户缓存，以 uni.getStorageSync('user') 为唯一数据源。
     */
    get_user_cache_info(key, default_value) {
      var raw = uni.getStorageSync(this.data.cache_user_info_key) || null
      if (raw == null) return default_value === undefined ? null : default_value
      var user = raw.userInfo
      if (user == null) return default_value === undefined ? null : default_value
      if ((key || null) == null) return user
      if (user[key] !== undefined) return user[key]
      if (raw[key] !== undefined) return raw[key]
      return default_value === undefined ? null : default_value
    },

    /**
     * 底部菜单页面数、tabbar
     */
    app_tabbar_pages() {
      var temp_tabbar = this.data.system_tabbar
      if (this.get_config('is_use_native_tabbar', 0) == 0) {
        var app_tabbar = this.get_config('app_tabbar') || null
        if (
          app_tabbar != null &&
          (app_tabbar.content || null) != null &&
          (app_tabbar.content.nav_content || null) != null
        ) {
          temp_tabbar = app_tabbar.content.nav_content.map(function (v) {
            if ((v.link || null) != null && (v.link.page || null) != null) {
              return v.link.page
            }
          })
        }
      }
      return temp_tabbar
    },

    /**
     * diy底部菜单高度数据存储
     */
    app_diy_tabbar_height_save(value) {
      uni.setStorageSync('cache_app_diy_tabbar_height_key', value)
    },

    /**
     * diy底部菜单高度数据读取
     */
    app_diy_tabbar_height_value() {
      return parseInt(uni.getStorageSync('cache_app_diy_tabbar_height_key') || 0)
    },

    /**
     * 系统底部菜单高度数据存储
     * value  高度
     */
    app_system_tabbar_height_save(value) {
      uni.setStorageSync('cache_app_system_tabbar_height_key', value)
    },

    /**
     * 系统底部菜单高度数据读取
     */
    app_system_tabbar_height_value() {
      return parseInt(uni.getStorageSync('cache_app_system_tabbar_height_key') || 0)
    },

    /**
     * 获取tab页面切换参数
     */
    get_page_tabbar_switch_params() {
      return uni.getStorageSync(this.data.cache_page_tabbar_switch_params) || null
    },

    /**
     * 删除tab页面切换参数
     */
    remove_page_tabbar_switch_params() {
      uni.removeStorageSync(this.data.cache_page_tabbar_switch_params)
    },

    /**
     * 是否需要登录、是否需要绑定手机号码
     */
    user_is_bind_mobile(user, object, method, params, is_to_login = false) {
      if (
        (user || null) != null &&
        (user.mobile || null) == null &&
        parseInt(user.is_mandatory_bind_mobile || 0) == 1
      ) {
        if (is_to_login) {
          this.login_confirm_tips_modal(this, object, method, params)
        }
        return true
      }
      return false
    },

    /**
     * 获取用户信息,信息不存在则唤醒授权
     */
    get_user_info(object, method, params) {
      var user = this.get_user_cache_info()
      if (user == null) {
        var self = this
        uni.getNetworkType({
          success: function (res) {
            if (res.networkType != 'none') {
              // #ifdef MP
              self.user_login(object, method, params)
              // #endif
              // #ifdef APP
              self.app_login_handle(self, object, method, params)
              // #endif
              // #ifdef H5
              self.login_confirm_tips_modal(self, object, method, params)
              // #endif
            }
          }
        })
        return false
      } else {
        if (this.user_is_bind_mobile(user, object, method, params, true)) {
          return false
        }
      }
      return user
    },

    /**
     * app登录处理（一键登录暂未对接，统一兜底到登录页）
     */
    app_login_handle(self, object, method, params) {
      var config = self.get_config('plugins_base.thirdpartylogin.data') || null
      if (config != null && parseInt(config.apponekeyusermobile_is_enable || 0) == 1) {
        uni.getProvider({
          service: 'oauth',
          success: function (res) {
            if (res.provider.includes('univerify')) {
              uni.preLogin({
                provider: 'univerify',
                success: function () {
                  uni.login({
                    provider: 'univerify',
                    univerifyStyle: {
                      authButton: {
                        normalColor: self.get_theme_color(),
                        highlightColor: self.get_theme_color(null, true)
                      },
                      backButton: {
                        visible: false
                      }
                    },
                    success: function (res) {
                      uni.getPhoneNumber({
                        provider: 'univerify',
                        success: function (phoneRes) {
                          // TODO: 一键登录获取手机号后对接后端登录接口
                          self.login_confirm_tips_modal(self, object, method, params)
                        },
                        fail: function () {
                          self.login_confirm_tips_modal(self, object, method, params)
                        }
                      })
                    },
                    fail: function () {
                      self.login_confirm_tips_modal(self, object, method, params)
                    }
                  })
                },
                fail: function () {
                  self.login_confirm_tips_modal(self, object, method, params)
                }
              })
            } else {
              self.login_confirm_tips_modal(self, object, method, params)
            }
          },
          fail: function () {
            self.login_confirm_tips_modal(self, object, method, params)
          }
        })
      } else {
        self.login_confirm_tips_modal(self, object, method, params)
      }
    },

    // 小程序用户登录。
    user_login(object, method, params) {
      this.login_confirm_tips_modal(this, object, method, params)
    },

    // 委托登录页处理用户登录。
    user_login_handle(object, method, params, is_to_auth) {
      this.login_confirm_tips_modal(this, object, method, params)
    },

    /**
     * method: 'init' 页面初始化时触发, 其他值表示 API 响应后触发
     */
    login_confirm_tips_modal(self, object, method, params) {
      var page = self.current_page(false)
      var is_tabbar = self.is_system_tabbar_pages('/' + page)
      if (method == 'init' && !is_tabbar) {
        uni.redirectTo({
          url: '/pages/login/login'
        })
      } else if (method == 'init' && is_tabbar) {
        var key = self.data.cache_user_no_login_page_status_data_key
        var data = uni.getStorageSync(key) || []
        if (data.indexOf(page) == -1) {
          data.push(page)
          uni.setStorageSync(key, data)
          uni.navigateTo({
            url: '/pages/login/login'
          })
        }
      } else if (method != 'init') {
        uni.navigateTo({
          url: '/pages/login/login'
        })
      }
    },

    /**
     * 用户自动登录处理
     */
    user_auto_login_handle() {
      // #ifdef H5
      var user = this.get_user_cache_info()
      if (user == null) {
        var params = this.get_launch_cache_info() || {}
        var config = this.get_config('plugins_base.thirdpartylogin.data') || null
        var data = this.get_config('plugins_thirdpartylogin_data') || null
        var url = null
        if (
          (params.thirdpartylogin || null) == null &&
          config != null &&
          data != null &&
          this.is_weixin_env()
        ) {
          var is_auto = config.weixin_is_env_auto_login || 0
          var weixin = data.weixin || null
          if (is_auto != 0 && weixin != null) {
            url = weixin.login_url
          }
        }
        if (url != null) {
          var page = this.current_page()
          if (page != null) {
            uni.setStorageSync(this.data.cache_prev_page_key, page)
          }
          window.location.href = url
        }
      }
      // #endif
    },

    /**
     * 是否微信环境
     */
    is_weixin_env() {
      return navigator.userAgent.toLowerCase().indexOf('micromessenger') !== -1
    },

    /**
     * 底部菜单设置
     */
    set_tabbar(theme) {
      if ((theme || null) == null) {
        theme = this.get_theme_value()
      }
      uni.setTabBarStyle({
        selectedColor: this.get_theme_color(theme),
        color: '#333333',
        backgroundColor: '#ffffff',
        borderStyle: 'black'
      })
    },

    /**
     * 获取主题值
     */
    get_theme_value() {
      // 主题类型        主题颜色
      // 红色 red       #ff0036
      // 黄色 yellow    #f6c133
      // 黑色 black     #333333
      // 绿色 green     #20a53a
      // 橙色 orange    #fe6f04
      // 蓝色 blue      #1677ff
      // 棕色 brown     #8B4513
      // 紫色 purple    #623cec
      // 暖色 warm      #5D4037 (mall-app 特有)
      return uni.getStorageSync('theme') || this.get_config('default_theme') || this.data.default_theme
    },

    /**
     * 设置主题值
     * @param {string} value - 主题标识（red, yellow, black, blue, green, orange, brown, purple, warm）
     */
    set_theme_value(value) {
      // 设置主题缓存
      uni.setStorageSync('theme', value || this.data.default_theme)
    },

    /**
     * 获取启动参数
     */
    get_launch_cache_info() {
      return uni.getStorageSync(this.data.cache_launch_info_key) || null
    },

    set_launch_cache_info(params) {
      params = this.launch_params_handle(params)
      uni.setStorageSync(this.data.cache_launch_info_key, params)
      return params
    },

    /**
     * 终端类型值
     */
    application_client_type() {
      // #ifdef MP-WEIXIN
      return 'weixin'
      // #endif
      // #ifdef H5
      return 'h5'
      // #endif
      // #ifdef APP
      return 'app'
      // #endif
    },

    /**
     * 拨打电话
     */
    call_tel(data) {
      var value = typeof data == 'object' ? data.currentTarget.dataset.value || null : data || null
      if (value != null) {
        uni.makePhoneCall({
          phoneNumber: value.toString()
        })
      }
    },

    /**
     * 隐藏系统底部菜单。
     */
    system_hide_tabbar() {
      if (this.get_config('is_use_native_tabbar', 0) != 1 && this.is_tabbar_pages()) {
        uni.hideTabBar()
      }
    },

    /**
     * 默认弱提示方法
     * @param {string} msg - 提示信息
     * @param {string} status - 状态 默认error [正确success, 错误error]
     */
    showToast(msg, status) {
      if ((msg || null) != null) {
        if ((status || 'error') == 'success') {
          uni.showToast({
            icon: 'success',
            title: msg,
            duration: 3000
          })
        } else {
          uni.showToast({
            icon: 'none',
            title: msg,
            duration: 3000
          })
        }
      }
    },

    /**
     * 当前地址是否存在底部菜单tabbar中
     */
    is_tabbar_pages(url = null) {
      var pages =
        this.get_config('is_use_native_tabbar', 0) == 1 ? this.data.system_tabbar : this.app_tabbar_pages()
      var result = this.is_tabbar_pages_handle(pages, url)
      return result
    },

    /**
     * 当前地址是否存在tabbar中
     * pages  tabbar页面
     * url    url地址
     */
    is_tabbar_pages_handle(pages, url = null) {
      var value = url == null ? '/' + this.current_page() : url
      if ((value || null) != null) {
        if (pages.indexOf(value) != -1) {
          return true
        } else {
          // 存在参数，去掉参数再检索是否存在页面的tabbar
          if (value.indexOf('?') != -1) {
            var temp = value.split('?')
            if (pages.indexOf(temp[0]) != -1) {
              return true
            }
          }
        }
      }
      return false
    },

    /**
     * 上一页页面地址
     */
    prev_page() {
      var value = null
      var pages = getCurrentPages()
      var length = pages.length
      if (length > 1) {
        value = pages[length - 2].route
      }
      return value
    },

    /**
     * 返回上一页
     */
    page_back_prev_event(url = null) {
      var prev_page = this.prev_page()
      if (prev_page == null) {
        this.url_open(url || this.app_tabbar_pages()[0])
      } else {
        uni.navigateBack()
      }
    },

    /**
     * 设置导航栏标题
     */
    set_pages_navigation_bar_title(title) {
      if ((title || null) != null) {
        uni.setNavigationBarTitle({ title: title })
      }
    },

    /**
     * 打开地图
     */
    open_location(lng, lat, name, address, scale) {
      if (lng == undefined || lat == undefined || lng == '' || lat == '') {
        uni.showToast({ title: '位置信息不完整', icon: 'none' })
        return false
      }
      uni.openLocation({
        name: name || '位置',
        address: address || '',
        scale: scale || 18,
        longitude: parseFloat(lng),
        latitude: parseFloat(lat)
      })
    },

    /**
     * 商品数据缓存处理
     */
    goods_data_cache_handle(goods_id, data) {
      // mall-app 中的简化实现
      return data
    },

    /**
     * 登录校验
     * 集中式请求拦截器已处理 A0230，此方法用于仍直接使用 uni.request 的页面
     */
    is_login_check(res, object, method, params) {
      if (res.code == -400) {
        uni.clearStorage()
        this.get_user_info(object, method, params)
        return false
      }
      return true
    },

    /**
     * 页面分享处理
     */
    page_share_handle(share = null) {
      var pages = getCurrentPages()
      var obj = pages[pages.length - 1] || null
      // 从当前页面读取 share_info
      if ((share || null) == null) {
        share = {}
      }
      if (obj != null && Object.keys(share).length <= 0) {
        share = obj.share_info || {}
      }
      // 统一处理
      share = this.share_content_handle(share)
      // #ifdef MP-WEIXIN
      uni.showShareMenu({
        withShareTicket: true,
        title: share.title,
        desc: share.desc,
        path: share.path + share.query,
        imageUrl: share.img,
        menus: ['shareAppMessage']
      })
      // #endif
    },

    /**
     * 是否当前单页模式
     */
    is_current_single_page() {
      return 0
    },

    /**
     * 是否PC端
     */
    // 是否pc
    is_pc() {
      var arr = ['macos', 'windows']
      return arr.indexOf(uni.getSystemInfoSync().platform) != -1
    },

    /**
     * 获取配置信息、可指定key和默认值
     * key              数据key（支持多级读取、以 . 分割key名称）
     * default_value    默认值
     */
    get_config(key, default_value) {
      // 获取全部缓存
      var config = uni.getStorageSync(this.data.cache_config_info_key) || null
      if ((key || null) == null) {
        return config
      }

      // key缓存获取
      var value = null
      if (config != null) {
        // 数据读取
        var arr = key.split('.')
        if (arr.length == 1) {
          value = config[key] == undefined ? null : config[key]
        } else {
          value = config
          for (var i = 0; i < arr.length; i++) {
            if (value[arr[i]] != undefined) {
              value = value[arr[i]]
            } else {
              value = null
              break
            }
          }
        }
      }
      // 默认值（空字符串也视为无效值，走 fallback）
      if ((value == null || value === '') && default_value != undefined) {
        value = default_value
      }
      // 无数据则处理自定义字段定义的数据
      if (value == null) {
        switch (key) {
          // 货币符号
          case 'currency_symbol':
            value = this.data.currency_symbol
            break
        }
      }
      return value
    },

    /**
     * 初始化配置
     */
    init_config(num = 0, object, method, params) {
      var self = this

      // 防重入：如果已初始化完成且不在加载中，直接执行回调，不再发起请求
      if (
        self.data.common_data_init_status == 1 &&
        self.data.common_data_loading_status == 0 &&
        num == 0
      ) {
        if (typeof object === 'object' && (method || null) != null) {
          object[method](params)
        }
        return
      }

      // 如果正在加载中，只加入回调队列，不重复发起请求
      if (self.data.common_data_loading_status == 1 && num == 0) {
        self.data.common_data_init_config_back_list.push({
          object: object,
          method: method,
          params: params
        })
        return
      }

      if (num == 0) {
        self.data.common_data_init_config_back_list.push({
          object: object,
          method: method,
          params: params
        })
      }

      uni.getNetworkType({
        success: function (res) {
          if (res.networkType != 'none') {
            // 获取配置本地缓存
            var config = self.get_config()
            if (config != null) {
              // 公共配置初始化返回处理
              self.init_config_result_handle(config, self)
            }

            // 读取远程配置
            if (self.data.common_data_loading_status == 0) {
              // 赋值在加载状态
              self.data.common_data_loading_status = 1

              // 数据自治：App 启动并行拉取两份数据合并缓存——
              // ① mall-system config/common（App 基础配置：站点/客服/开关）
              // ② mall-diy app-tabbar（底部导航装修数据，DIY 域自治，不再依赖 system 下发）
              const { getCommonConfig } = require('@/api/system/config.js')
              const { getAppTabbar } = require('@/api/diy/index.js')
              Promise.all([
                getCommonConfig(),
                getAppTabbar().catch(function () {
                  // tabbar 拉取失败不阻塞启动（保留 system 侧旧值/空，footer_init 有 null 重试）
                  return null
                })
              ])
                .then(function (results) {
                  self.data.common_data_loading_status = 0
                  var res = results[0]
                  var tabbarRes = results[1]
                  if (res.code == 0 && res.data) {
                    // 合并 DIY tabbar（覆盖 system 侧历史 app_tabbar，保证 DIY 域数据源唯一）
                    var data = res.data
                    if (tabbarRes && tabbarRes.code == '00000') {
                      data.app_tabbar = tabbarRes.data || null
                    }
                    // 配置存储
                    uni.setStorageSync(self.data.cache_config_info_key, data)

                    // 公共配置初始化返回处理
                    self.init_config_result_handle(data, self)

                    // 回调
                    if (self.data.common_data_init_config_back_list.length > 0) {
                      self.data.common_data_init_config_back_list.forEach(function (item) {
                        if (typeof item.object === 'object' && (item.method || null) != null) {
                          item.object[item.method](item.params)
                        }
                      })
                      self.data.common_data_init_config_back_list = []
                    }
                  } else {
                    // 请求失败，重试
                    if (parseInt(num || 0) <= 20) {
                      self.init_config(num + 1, object, method, params)
                    }
                  }
                })
                .catch(function () {
                  self.data.common_data_loading_status = 0
                  if (parseInt(num || 0) <= 20) {
                    self.init_config(num + 1, object, method, params)
                  }
                })
            }
          }
        }
      })
    },

    // 公共配置初始化返回处理
    init_config_result_handle(data, self) {
      // 记录已初始化公共数据状态
      self.data.common_data_init_status = 1

      // 将接口返回的 default_theme 写入 Storage。
      var theme = data.default_theme || null
      if (theme) {
        self.set_theme_value(theme)
      }

      // 设置底部菜单、存在开屏广告则延迟加载
      if (self.data.is_use_native_tabbar == 1) {
        if (self.is_init_config_success_pages_begin(self)) {
          setTimeout(function () {
            self.set_tabbar(data.plugins_themestyle_data)
          }, 500)
        } else {
          self.set_tabbar(data.plugins_themestyle_data)
        }
      }

      // 用户自动登录处理
      self.user_auto_login_handle()
    },

    // 是否初始化配置成功后，页面前处理，返回true则页面需要延迟加载
    is_init_config_success_pages_begin(self = this) {
      // 是否存在开屏广告插件
      return this.is_plugins_startad_status(self)
    },

    // 是否存在开屏广告插件
    is_plugins_startad_status(self = this) {
      // 是否有开屏广告
      var pages = self.app_tabbar_pages() || []
      var current_page = '/' + self.current_page(false)
      if (pages.length > 0 && pages[0] == current_page) {
        var base = self.get_config('plugins_base.startad.data') || {}
        var data = self.get_config('plugins_startad_list') || []
        if (data.length > 0 && parseInt(base.is_status || 0) == 1) {
          return true
        }
      }
      return false
    },

    /**
     * 配置是否有效
     */
    is_config(object, method, params) {
      var self = this
      var count = 0
      var timer = setInterval(function () {
        if (self.data.common_data_init_status == 1) {
          clearInterval(timer)
          if (typeof object === 'object' && (method || null) != null) {
            object[method](true, params)
          }
        }
        count++
        if (count >= 2300) {
          clearInterval(timer)
        }
      }, 100)
    },

    /**
     * 显示提示
     */
    show(msg, status) {
      if ((msg || null) != null) {
        uni.showToast({
          icon: status === 'success' ? 'success' : 'none',
          title: msg,
          duration: 3000
        })
      }
    },

    /**
     * 生成包内静态资源地址：/static/app/{theme}/{type}/ 或 /static/plugins/{type}/images/。
     */
    get_static_url(type, is_plugins) {
      if ((type || null) == null) {
        type = 'common'
      }
      if ((is_plugins || false) == true) {
        return '/static/plugins/' + type + '/images/'
      }
      return '/static/app/' + this.get_theme_value() + '/' + type + '/'
    },

    /**
     * 获取主题颜色视图
     */
    get_theme_value_view() {
      return 'theme-' + this.get_theme_value()
    },

    /**
     * 获取语言值
     */
    get_language_value() {
      // mall-app 暂不支持多语言,默认返回中文
      return 'zh'
    },

    /**
     * 位置信息初始化
     * status: 0-未选择，1-已选择，2-选择中，3-选择失败
     */
    choice_user_location_init() {
      var cache_key = this.data.cache_userlocation_key || 'cache_userlocation_key'
      var result = uni.getStorageSync(cache_key) || null
      var user_location = { status: 0 }
      if (result != null) {
        user_location = {
          ...user_location,
          ...{
            name: result.name || null,
            address: result.address || null,
            lat: result.lat || null,
            lng: result.lng || null,
            status: result.status || 1
          }
        }
      }
      // 默认名称
      var default_name = '请选择位置'
      // 位置选择失败的状态，名称和默认名称不一致则认为是成功的
      if (user_location.status == 3 && user_location.name != default_name) {
        user_location.status = 1
      }
      user_location['text'] = user_location.name || user_location.address || default_name
      return user_location
    },

    /**
     * 清除位置缓存信息
     */
    choice_user_location_remove() {
      var cache_key = this.data.cache_userlocation_key || 'cache_userlocation_key'
      uni.removeStorageSync(cache_key)
    },

    /**
     * 进入位置选择事件
     */
    choose_user_location_event() {
      // 存在数据则改变状态值（选择中）
      var key = this.data.cache_userlocation_key
      var result = uni.getStorageSync(key) || null
      if (result != null) {
        result['status'] = 2
        uni.setStorageSync(key, result)
      }
      // 跳转到位置授权选择页面
      uni.navigateTo({
        url: '/pages/common/open-setting-location/open-setting-location'
      })
    },

    /**
     * 位置权限校验
     */
    get_location_check(type, object, method) {
      // #ifdef MP-WEIXIN || MP-BAIDU || MP-TOUTIAO || MP-QQ
      var self = this
      uni.getSetting({
        success(res) {
          if (!res.authSetting[type]) {
            uni.authorize({
              scope: type,
              success(res) {
                if (typeof object === 'object' && (method || null) != null) {
                  object[method](1)
                }
              },
              fail: res => {
                if (typeof object === 'object' && (method || null) != null) {
                  object[method](0)
                }
              }
            })
          } else {
            if (typeof object === 'object' && (method || null) != null) {
              object[method](1)
            }
          }
        },
        fail: res => {
          self.showToast('获取位置权限失败')
          if (typeof object === 'object' && (method || null) != null) {
            object[method](0)
          }
        }
      })
      // #endif
      // #ifdef MP-ALIPAY || H5 || APP
      if (typeof object === 'object' && (method || null) != null) {
        object[method](1)
      }
      // #endif
    },

    /**
     * 获取用户当前位置
     * object    回调对象
     * method    回调方法
     * is_force  强制获取位置
     */
    get_user_location(object, method, is_force) {
      if (this.get_config('get_user_location_status', 0) == 1 || is_force) {
        var cache_key = this.data.cache_userlocation_key
        var result = uni.getStorageSync(cache_key) || null
        if (result == null) {
          var self = this
          var client_value = self.application_client_type()
          var is_weixin_privacy = false
          var is_network = false
          // APP 端：先弹说明再获取位置（Android 合规要求）
          var permission_rationale_shown = false
          self.data.get_user_location_timer = setInterval(function () {
            // #ifdef MP-WEIXIN
            uni.getPrivacySetting({
              success: res => {
                if (!res.needAuthorization) {
                  is_weixin_privacy = true
                }
              }
            })
            // #endif

            // #ifdef APP
            uni.getNetworkType({
              success: function (res) {
                if (res.networkType != 'none') {
                  is_network = true
                }
              }
            })
            // #endif

            var status = true
            if (client_value == 'weixin' && !is_weixin_privacy) {
              status = false
            }
            if ((client_value == 'ios' || client_value == 'android') && !is_network) {
              status = false
            }
            if (status) {
              // APP 端首次打开时弹一次权限说明
              if (client_value == 'android' && !permission_rationale_shown) {
                permission_rationale_shown = true
                clearInterval(self.data.get_user_location_timer)
                showPermissionRationale({
                  cacheKey: 'location',
                  title: i18n.t('common.need_location_power_usage_text'),
                  content: i18n.t('common.location_get_access_usage_text'),
                  confirmText: i18n.t('common.confirm'),
                  cancelText: i18n.t('common.not_yet')
                }).then(function(granted) {
                  if (granted) {
                    self.do_get_location(cache_key, object, method)
                  }
                })
                return
              }
              self.do_get_location(cache_key, object, method)
              clearInterval(self.data.get_user_location_timer)
            }
          }, 500)
        }
      }
    },

    do_get_location(cache_key, object, method) {
      uni.getLocation({
        type: 'wgs84',
        success: function (res) {
          var address = {
            name: '当前位置',
            address: '',
            lat: res.latitude || null,
            lng: res.longitude || null,
            status: 1
          }
          uni.setStorageSync(cache_key, address)
          if (typeof object === 'object' && (method || null) != null) {
            object[method](address)
          }
        },
        fail: function () {
          // 获取位置失败，静默处理
        }
      })
    },

    /**
     * URL参数转JSON
     */
    url_params_to_json(params) {
      if ((params || null) == null) {
        return {}
      }
      var arr = params.split('&')
      var obj = {}
      for (var i in arr) {
        var temp = arr[i].split('=')
        obj[temp[0]] = temp[1] || ''
      }
      return obj
    },

    /**
     * JSON转URL参数
     */
    json_to_url_params(data) {
      var str = ''
      for (var i in data) {
        if (str != '') {
          str += '&'
        }
        str += i + '=' + data[i]
      }
      return str
    },

    /**
     * 是否系统tabbar页面
     */
    is_system_tabbar_pages(url) {
      var pages = this.data.system_tabbar || []
      return pages.indexOf(url) != -1
    },

    /**
     * 调用页面方法
     */
    get_page_object(page = null) {
      var pages = getCurrentPages()
      if (page == null) {
        return pages[pages.length - 1]
      } else {
        var result = []
        for (var i = 0; i < pages.length; i++) {
          if (pages[i]['route'] == page) {
            result.push(pages[i])
          }
        }
        return result
      }
    },

    /**
     * 底部浮动按钮样式处理
     */
    bottom_fixed_style_handle() {
      var obj = this.get_page_object()
      if (obj != null) {
        var temp =
          obj.bottom_fixed_style == undefined
            ? (obj.data || null) == null
              ? undefined
              : obj.data.bottom_fixed_style
            : ''
        if (temp !== undefined && this.is_tabbar_pages()) {
          obj.$vm.setData({
            bottom_fixed_style:
              'bottom:' + ((this.app_system_tabbar_height_value() - 8) * 2 + 20) + 'rpx'
          })
        }
      }
    },

    /**
     * 页面加载事件处理
     */
    page_event_onload_handle(params) {
      // 内部页面跳转不触发 onShow，在此统一补写分享人
      if (params) {
        if (params.r) {
          var store = uni.getStorageSync('user') || {}
          store.referrer = params.r
          uni.setStorageSync('user', store)
        }
      }
      // 是否存在开屏广告插件
      if (this.is_plugins_startad_status()) {
        uni.removeStorageSync('plugins_startad_cache_key')
      }
    },

    /**
     * 页面展示事件处理
     */
    page_event_onshow_handle() {
      // 隐藏系统tabbar
      this.system_hide_tabbar()

      // 底部浮动按钮样式处理
      this.bottom_fixed_style_handle()

      // 页面顶部导航标题设置
      this.set_pages_navigation_bar_title()

      // 设置导航背景色和颜色
      this.set_navigation_bar_color()
    },

    /**
     * 获取主题色值
     * @param theme 主题标识
     * @param is_light 是否获取浅主色
     */
    get_theme_color(theme, is_light = false) {
      var color_obj = {
        // 主色
        red: '#ff0036',
        yellow: '#f6c133',
        black: '#333333',
        blue: '#1677ff',
        green: '#20a53a',
        orange: '#fe6f04',
        brown: '#8B4513',
        purple: '#623cec',
        warm: '#5D4037',
        // 浅主色
        red_light: '#ffdbe2',
        yellow_light: '#ffebd2',
        black_light: '#dcdcdc',
        blue_light: '#d1e4ff',
        green_light: '#cce8d2',
        orange_light: '#fde4d1',
        brown_light: '#eadcd2',
        purple_light: '#d6cbfb',
        warm_light: '#E8D5D0'
      }
      if ((theme || null) == null) {
        theme = this.get_theme_value()
      }
      if (is_light) {
        theme += '_light'
      }
      return color_obj[theme] || '#ff0036'
    },

    /**
     * 颜色转rgba
     * @param hexValue 色值
     * @param alpha 透明度
     */
    hex_rgba(hexValue, alpha) {
      const rgx = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
      const hex = hexValue.replace(rgx, (m, r, g, b) => r + r + g + g + b + b);
      const rgb = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      if (!rgb) {
        return hexValue;
      }
      const r = parseInt(rgb[1], 16),
        g = parseInt(rgb[2], 16),
        b = parseInt(rgb[3], 16);
      return `rgba(${r},${g},${b},${alpha})`;
    },

    /**
     * 获取正方形logo
     */
    get_application_logo_square() {
      return this.get_config('home_site_logo_square')
    },

    /**
     * 是否启用哀悼模式（返回 false，暂不启用）
     */
    is_app_mourning() {
      return false
    },

    /**
     * 获取应用标题
     */
    get_application_title() {
      // mall-app 中返回配置的标题或默认值
      return this.get_config('home_site_name', this.data.application_title)
    },

    /**
     * 获取应用描述
     */
    get_application_describe() {
      return this.get_config('home_site_describe', this.data.application_describe)
    },

    /**
     * 获取应用logo
     */
    get_application_logo() {
      return this.get_config('home_site_logo', this.data.application_logo)
    },

    /**
     * 打开 webview 页面
     * value       [string]  url地址
     * is_redirect [boolean] 是否关闭当前页面
     */
    open_web_view(value, is_redirect = false) {
      console.log('[open_web_view] url:', value)
      var url = '/pages/web-view/web-view?url=' + encodeURIComponent(value)
      if (is_redirect) {
        uni.redirectTo({ url: url })
      } else {
        uni.navigateTo({
          url: url,
          fail: function (err) {
            console.log('[open_web_view] fail:', JSON.stringify(err))
          }
        })
      }
    },

    /**
     * weburl地址id值匹配
     */
    web_url_value_mate(url, rules) {
      var status = 0
      var value = null
      for (var i in rules) {
        if (url.indexOf(rules[i]) != -1) {
          var temp = url.split(rules[i])
          if (temp.length > 1) {
            temp = temp[1].split('.')
            if (temp.length > 0 && (temp[0] || null) != null) {
              value = temp[0]
            }
          }
          status = 1
          break
        }
      }
      return { status: status, value: value }
    },

    /**
     * 扫码解析处理
     */
    scan_handle() {
      // #ifdef H5
      this.showToast(this.$t('common.not_supported_scan_tips'))
      // #endif
      // #ifndef H5
      var that = this
      showPermissionRationale({
        cacheKey: 'scan_album',
        title: i18n.t('common.scan_code_need_power_usage_text'),
        content: i18n.t('common.scan_code_get_access_usage_text'),
        confirmText: i18n.t('common.confirm'),
        cancelText: i18n.t('common.not_yet')
      }).then(function(granted) {
        if (!granted) return
        uni.scanCode({
        success: function(res) {
          if (res.result !== '') {
            var value = res.result
            // 提取分享人ID写入Storage（与 onShow 逻辑一致）
            var ref_match = value.match(/[?&]r=(\d+)/)
            if (ref_match) {
              var store = uni.getStorageSync('user') || {}
              store.referrer = ref_match[1]
              uni.setStorageSync('user', store)
            }
            if (that.is_url(value)) {
              // 通用 H5 App 页面地址（如 https://域名/#/pages/goods-detail/goods-detail?id=100）
              var hash_index = value.indexOf('/#/pages/')
              if (hash_index != -1) {
                var app_path = value.substring(hash_index + 2) // 提取 /pages/xxx/xxx?id=xxx
                that.url_open(app_path)
                return
              }

              // 是否为商品地址
              var goods_arr = ['/goods-', '/goods/index/id/', '=goods/index/id/']
              var goods_ret = that.web_url_value_mate(value, goods_arr)
              if (goods_ret.status == 1 && goods_ret.value != null) {
                uni.navigateTo({
                  url: '/pages/goods-detail/goods-detail?id=' + goods_ret.value
                })
                return
              }

              // 识别店铺详情地址。
              var shop_arr = ['/shop-index-detail-', '/plugins/index/pluginsname/shop/pluginscontrol/index/pluginsaction/detail/id/', '=plugins/index/pluginsname/shop/pluginscontrol/index/pluginsaction/detail/id/']
              var shop_ret = that.web_url_value_mate(value, shop_arr)
              if (shop_ret.status == 1 && shop_ret.value != null) {
                uni.navigateTo({
                  url: '/pages/plugins/shop/detail/detail?id=' + shop_ret.value
                })
                return
              }

              // 扫码收款通过 WebView 打开。
              var scanpay_arr = ['/scanpay-index-index', 'plugins/index/pluginsname/scanpay/pluginscontrol/index/pluginsaction/index', 'plugins/index/pluginsname/scanpay', '/scanpay']
              var scanpay_ret = that.web_url_value_mate(value, scanpay_arr)
              if (scanpay_ret.status == 1) {
                that.open_web_view(value)
                return
              }

              // 分享链接 — 跳转首页并保留推荐参数
              var share_match = value.match(/\/ulink\/share\?.*c=([^&]+)/)
              if (share_match) {
                var ref_match2 = value.match(/[?&]r=(\d+)/)
                var shareUrl = '/pages/index/index'
                if (ref_match2) shareUrl += '&r=' + ref_match2[1]
                uni.navigateTo({ url: shareUrl })
                return
              }

              // 扫码登录通过 WebView 打开。
              var thirdpartylogin_arr = ['/thirdpartylogin-scan-index-', 'plugins/index/pluginsname/thirdpartylogin/pluginscontrol/scan/pluginsaction/index/id/']
              var thirdpartylogin_ret = that.web_url_value_mate(value, thirdpartylogin_arr)
              if (thirdpartylogin_ret.status == 1 && thirdpartylogin_ret.value != null) {
                that.open_web_view(value)
                return
              }

              // 默认打开url
              that.open_web_view(value)
            } else {
              // 非URL：尝试页面跳转
              that.url_open(value)
            }
          } else {
            console.warn('[scanCode] 扫码结果为空，原始res:', JSON.stringify(res))
            that.showToast('二维码无法识别')
          }
        },
        fail: function(err) {
          console.error('[scanCode] 扫码失败:', JSON.stringify(err))
          that.showToast('扫码失败，请重试')
        }
      })
    })
      // #endif
    },

    /**
     * 启动参数处理
     */
    launch_params_handle(params) {
      var cache_params = this.get_launch_cache_info()
      if ((params.query || null) != null) {
        params = params.query
      }
      if ((params.scene || null) != null) {
        params = this.url_params_to_json(decodeURIComponent(params.scene))
        console.log('[分享人] 解析scene:', params)
      }
      // 缩略参数名统一还原：r→referrer（scene / 直链 / 二维码 通用）
      if (params.r) { params.referrer = params.r; delete params.r; }
      // 微信扫普通链接二维码：URL 通过 q 参数传入，解析 referrer
      if ((params.q || null) != null) {
        var qUrl = decodeURIComponent(params.q)
        var urlParams = this.url_params_to_json(qUrl.split('?')[1] || '')
        if (urlParams.r) { params.referrer = urlParams.r; }
        console.log('[分享人] 解析二维码q参数:', qUrl, '→', urlParams)
      }
      if (
        (params['referrer'] || null) == null &&
        cache_params != null &&
        (cache_params.referrer || null) != null
      ) {
        params['referrer'] = cache_params.referrer
      }
      return params
    },

    /**
     * 提示弹窗（支持回调）
     * e.msg              [string]  提示信息
     * e.title            [string]  标题
     * e.is_show_cancel   [int]     是否显示取消按钮（0否, 1是）
     * e.cancel_text      [string]  取消按钮文字
     * e.confirm_text     [string]  确认按钮文字
     * e.confirm_color    [string]  确认按钮颜色
     * e.object           [object]  回调操作对象
     * e.method           [string]  回调操作对象的函数名
     * e.params           [object]  携带的参数
     */
    alert(e) {
      var msg = e.msg || null
      if (msg != null) {
        var title = e.title || ''
        var is_show_cancel = e.is_show_cancel == 0 ? false : true
        var cancel_text = e.cancel_text || i18n.t('common.cancel')
        var confirm_text = e.confirm_text || i18n.t('common.confirm')
        var cancel_color = e.cancel_color || '#000000'
        var confirm_color = e.confirm_color || '#576B95'
        var params = e.params || {}
        uni.showModal({
          title: title,
          content: msg,
          showCancel: is_show_cancel,
          cancelText: cancel_text,
          cancelColor: cancel_color,
          confirmText: confirm_text,
          confirmColor: confirm_color,
          success(res) {
            if (
              (e.object || null) != null &&
              typeof e.object === 'object' &&
              (e.method || null) != null
            ) {
              params['alert_status'] = res.confirm ? 1 : 0
              e.object[e.method](params)
            }
          }
        })
      } else {
        this.showToast('提示信息不能为空')
      }
    },

    /**
     * 清除 Storage 中的用户缓存。
     */
    remove_user_cache_event(is_remove_user = true) {
      if (is_remove_user) {
        uni.removeStorageSync('token')
        uni.removeStorageSync(this.data.cache_user_info_key)
      }
      uni.removeStorageSync(this.data.cache_user_no_login_page_status_data_key)
      uni.removeStorageSync(this.data.cache_user_base_personal_interval_time_key)
      uni.removeStorageSync(this.data.cache_app_update_tips_interval_time_key)
      uni.removeStorageSync(this.data.cache_app_star_tips_interval_time_key)
      this.showToast('清除成功', 'success')
    },

    /**
     * 打开权限管理中心
     */
    open_setting_event() {
      // #ifdef MP
      uni.openSetting()
      // #endif
      // #ifdef APP
      uni.openAppAuthorizeSetting()
      // #endif
    },

    /**
     * 将 action/controller 请求参数映射到 REST API。
     */
    get_request_url(a, c, plugins, params, group) {
      var map = {
        // 登录注册
        login_user: '/mall-auth/oauth2/token',
        reg_user: '/mall-ums/app-api/v1/members/register',
        forgetpwd_user: '/mall-ums/app-api/v1/members/forget-password',
        // 验证码
        loginverifysend_user: '/mall-system/api/v1/msg/login/sms_code',
        regverifysend_user: '/mall-system/api/v1/msg/register/sms_code',
        forgetpwdverifysend_user: '/mall-system/api/v1/msg/login/sms_code',
        bindverifysend_user: '/mall-system/api/v1/msg/register/sms_code',
        userverifyentry_user: '/mall-system/api/v1/msg/register/sms_code',
        // 绑定
        appmobilebind_user: '/mall-ums/app-api/v1/members/bind-mobile',
        appemailbind_user: '/mall-ums/app-api/v1/members/bind-email',
        appemailbindverifysend_user: '/mall-system/api/v1/msg/register/sms_code',
        appmobilebindverifysend_user: '/mall-system/api/v1/msg/register/sms_code',
        onekeyusermobilebind_user: '/mall-ums/app-api/v1/members/onekey-bind-mobile',
        // 第三方登录
        bind_index_thirdpartylogin: '/mall-ums/app-api/v1/members/thirdparty-bind',
        // 安全
        loginpwdupdate_safety: '/mall-ums/app-api/v1/members/update-password',
        // 协议（内容服务）
        index_agreement: '/mall-cms/app-api/v1/agreement',
        // 用户信息
        tokenuserinfo_user: '/mall-ums/app-api/v1/members/me',
        // 商品海报
        goodsposter_goods_poster: '/mall-pms/app-api/v1/goods/poster',
        // 个人资料
        index_personal: '/mall-ums/app-api/v1/members/me',
        save_personal: '/mall-ums/app-api/v1/members/update',
        useravatarupload_personal: '/mall-ums/app-api/v1/members/avatar',
        // 注销
        logout_safety: '/mall-ums/app-api/v1/members/del',
        // 配置
        common_base: '/mall-system/app-api/v1/config/common',
        // 文章
        index_article: '/mall-system/app-api/v1/article/categories',
        datalist_article: '/mall-system/app-api/v1/article/list',
        detail_article: '/mall-system/app-api/v1/article/detail',
        // 自定义页面
        index_customview: '/mall-system/app-api/v1/customview'
      }
      var key = a + '_' + c
      if (plugins) key = a + '_' + c + '_' + plugins
      return map[key] || ''
    },

    /**
     * 设置导航栏颜色
     * @param {Boolean} is_white - 是否为白色文字
     */
    set_navigation_bar_color(is_white = null) {
      var color = '#000000'
      var bg_color = '#ffffff'
      var arr = ['pages/index/index']
      var page = this.current_page(false)
      if (is_white === true || (is_white === null && arr.indexOf(page) != -1)) {
        color = '#ffffff'
        bg_color = '#000000'
      }
      uni.setNavigationBarColor({
        frontColor: color,
        backgroundColor: bg_color
      })
    },

    /**
     * 获取对象中指定 key 的值（支持多级嵌套）
     * @param {Object} data - 数据对象
     * @param {String} key - 键名（支持 'a.b.c' 格式）
     * @param {*} default_value - 默认值
     */
    get_key_data(data, key, default_value = null) {
      var value = default_value
      if ((data || null) != null) {
        var arr = key.split('.')
        if (arr.length == 1) {
          if (data[key] != undefined) {
            value = data[key]
          }
        } else {
          value = data
          for (var i in arr) {
            if (value[arr[i]] != undefined) {
              value = value[arr[i]]
            } else {
              value = default_value
              break
            }
          }
        }
      }
      return value
    },

    /**
     * 获取当前页面地址
     * @param {Boolean} is_whole - 是否包含参数
     */
    current_page(is_whole = true) {
      // 来源地址、拼接当前小程序页面
      var pages = getCurrentPages()
      var page = pages[pages.length - 1]
      var url = this.page_url_handle(page)
      if (is_whole == false) {
        var temp = url.split('?')
        url = temp[0]
      }
      return url
    },

    /**
     * 页面地址处理
     * @param {Object} page - 页面对象
     */
    page_url_handle(page) {
      if ((page || null) == null) {
        return ''
      }

      // 直接获取页面全的路径地址
      if ((page.$page || null) != null && (page.$page.fullPath || null) != null) {
        var path = page.$page.fullPath
        return path.substr(0, 1) == '/' ? path.substr(1) : path
      }

      // 拼接地址和参数
      var route = page.route
      var options = page.options || {}
      var query = ''
      if (JSON.stringify(options) != '{}') {
        for (var i in options) {
          query += '&' + i + '=' + options[i]
        }
      }
      if ((query || null) != null) {
        route += '?' + query.substr(1)
      }
      return route
    },

    // 分享方法。

    /**
     * 获取当前页面完整 URL（跨平台）
     */
    get_page_url(is_whole = true) {
      var url = this.current_page()
      // #ifdef APP
      url = this.page_url_protocol(url)
      // #endif
      // #ifdef H5
      url = window.location.href
      // #endif
      if (is_whole == false) {
        var temp = url.split('?')
        url = temp[0]
      }
      return url
    },

    /**
     * URL 协议处理，拼接 H5 域名
     */
    page_url_protocol(url) {
      if ((url || null) != null && !this.is_url(url)) {
        var host = this.get_config('common_app_h5_url', '')
        // #ifdef H5
        if (host == '') {
          host = window.location.href.split('#')[0] + '#/'
        }
        // #endif
        url = (host + url).replace('#//', '#/')
      }
      return url
    },

    /**
     * 分享内容标准化处理
     * 合并页面传入的数据和全局配置默认值
     */
    share_content_handle(data) {
      var share_config = this.get_config('plugins_base.share.data') || {}
      var result = {
        title: data.title || share_config.title || this.get_application_title(),
        desc: data.desc || share_config.desc || this.get_application_describe(),
        path: data.path || this.app_tabbar_pages()[0],
        query: this.share_query_handle(data.query || ''),
        img: data.img || share_config.pic || this.get_application_logo_square()
      }
      result['url'] = this.get_page_url()
      // #ifdef H5 || APP
      if (this.is_url(result['url'])) {
        result['url'] = this.page_url_protocol(
          result.url.split('#')[0] +
            '#' +
            ((result.path || null) != null && result.path.substr(0, 1) == '/' ? '' : '/') +
            (result.path || '') +
            (result.query || '')
        )
      }
      // #endif
      return result
    },

    /**
     * 分享参数处理，自动追加 referrer
     */
    share_query_handle(query) {
      if ((query || null) == null || !/(?:^|&)r=/.test(query)) {
        var user_id =
          parseInt(this.get_user_cache_info('id', 0)) ||
          parseInt(this.get_user_cache_info('memberId', 0)) ||
          0
        if (user_id > 0) {
          var join = (query || null) == null ? '' : '&'
          query += join + 'r=' + user_id
        }
      }
      return (query || null) == null ? '' : '?' + query
    },

    /**
     * 文本事件处理（复制、拨号等）
     */
    text_event_handle(e) {
        var event = e.currentTarget.dataset.event || null;
        if (event != null) {
            var value = e.currentTarget.dataset.value;
            switch (event) {
                case 'tel':
                    this.call_tel(value);
                    break;
                case 'copy':
                    this.text_copy_event(value);
                    break;
            }
        }
    },

    /**
     * 复制文本到剪贴板
     */
    text_copy_event(data) {
      var value =
        typeof data == 'object'
          ? (data.currentTarget ? data.currentTarget.dataset.value : null) || null
          : data || null
      if (value != null) {
        var self = this
        uni.setClipboardData({
          data: value,
          success() {
            uni.getClipboardData({
              success() {
                self.showToast('复制成功', 'success')
              }
            })
          }
        })
      }
    },

    /**
     * 是否当前为单页模式（朋友圈进入的单个页面）
     */
    is_single_page_check() {
      if (this.is_current_single_page() == 1) {
        this.showToast('单页模式下无法分享')
        return false
      }
      return true
    }
  },

  methods: {
    // 小程序版本更新自检
    checkMiniProgramUpdate() {
      // #ifdef MP-WEIXIN
      if (!uni.getUpdateManager) {
        return
      }
      var updateManager = uni.getUpdateManager()
      updateManager.onCheckForUpdate(function (res) {
        console.log('[版本更新] 检查结果 hasUpdate:', res.hasUpdate)
      })
      updateManager.onUpdateReady(function () {
        uni.showModal({
          title: '更新提示',
          content: '新版本已准备好，是否重启应用？',
          showCancel: false,
          confirmText: '立即重启',
          success: function (modalRes) {
            if (modalRes.confirm) {
              updateManager.applyUpdate()
            }
          }
        })
      })
      updateManager.onUpdateFailed(function () {
        console.log('[版本更新] 下载失败')
      })
      // #endif
    },

    // 服务端版本检测（全平台，兜底微信 UpdateManager 不触发的问题）
    checkServerVersion() {
      // 防抖：3秒内不重复请求（微信 onShow 可能连续触发两次）
      var now = Date.now()
      if (this._lastVersionCheck && now - this._lastVersionCheck < 3000) {
        return
      }
      this._lastVersionCheck = now

      // #ifdef MP-WEIXIN
      var platform = 'mini_program'
      // #endif
      // #ifdef APP
      var platform = uni.getSystemInfoSync().platform || 'android'
      // #endif
      // #ifdef H5
      var platform = 'h5'
      // #endif

      console.log(
        '[服务端版本检测] 发起检测, appVersion:',
        this.globalData.data.app_version_info,
        'platform:',
        platform
      )

      uni.request({
        url: config.url + '/mall-system/app-api/v1/app-version/check',
        method: 'POST',
        data: {
          appVersion: this.globalData.data.app_version_info,
          platform: platform
        },
        dataType: 'json',
        success: function (res) {
          console.log('[服务端版本检测] 响应:', JSON.stringify(res.data))
          if (res.data.code === '00000' && res.data.data) {
            var data = res.data.data
            if (data.isUpdateStatus === 1 && data.updateData) {
              var upd = data.updateData
              var content = (upd.content || []).join('\n')
              uni.showModal({
                title: '发现新版本 v' + upd.versionNew,
                content: content || upd.name || '新版本已发布，建议立即更新',
                showCancel: upd.isForceUpdate !== 1,
                confirmText: '立即更新',
                cancelText: '稍后再说',
                success: function (modalRes) {
                  if (modalRes.confirm) {
                    // #ifdef MP-WEIXIN
                    wx.restartMiniProgram()
                    // #endif
                    // #ifdef APP
                    if (upd.updateUrl) {
                      plus.runtime.openURL(upd.updateUrl)
                    }
                    // #endif
                  }
                }
              })
            }
          }
        },
        fail: function () {
          console.log('[服务端版本检测] 请求失败')
        }
      })
    }
  },

  onLaunch: function (params) {
    // 隐藏系统tabbar
    this.globalData.system_hide_tabbar()

    // 微信 UpdateManager 需尽早注册
    // #ifdef MP-WEIXIN
    this.checkMiniProgramUpdate()
    // #endif
  },
  /**
   * 分享参数统一入口
   *
   * 外部 URL 使用短名 r → launch_params_handle 转为内部长名 referrer
   * → launchCache（持久化）+ storage.user.referrer（登录/下单用）
   *
   * 消费方：goods-detail.vue、buy.vue（订单）、login.vue
   */
  onShow: function (params) {
    // 热启动时检查版本更新（onLaunch 只触发一次，onShow 每次切回前台都触发）
    // #ifdef MP-WEIXIN
    this.checkMiniProgramUpdate()
    this.checkServerVersion()
    // #endif

    // #ifdef H5
    // H5 下 onShow 无 URL 参数，从 location 提取注入，和小程序统一走 launch_params_handle
    if ((params.query || null) == null && window && window.location && window.location.href) {
      var hash = window.location.href.split('#')[1] || ''
      var queryStr = hash.split('?')[1] || ''
      if (queryStr) {
      var queryObj = {}
      try {
        queryStr.split('&').forEach(function(p) {
          var kv = p.split('=')
          if (kv[0]) queryObj[decodeURIComponent(kv[0])] = decodeURIComponent(kv[1] || '')
        })
      } catch (e) {
        console.warn('[分享人] H5 query 解析失败:', e)
      }
        params.query = queryObj
      }
    }
    // #endif

    // 隐藏系统tabbar
    this.globalData.system_hide_tabbar()

    // 公共数据初始化
    this.globalData.init_config()

    // 设置设备信息
    this.globalData.set_system_info()

    // 启动参数持久化（含 referrer 缓存恢复）
    var launchParams = this.globalData.set_launch_cache_info(params)

    // 全局捕获分享人ID，写入 storage 供后续登录/下单使用（末人归因，每次覆盖）
    console.log('[分享人] onShow launchParams:', JSON.stringify(launchParams))
    if ((launchParams.referrer || null) != null) {
      var store = uni.getStorageSync('user') || {}
      store.referrer = launchParams.referrer
      uni.setStorageSync('user', store)
      console.log('[分享人] 已写入 referrer:', launchParams.referrer)
    } else {
      console.log('[分享人] 本次启动无 referrer')
    }
     // URL Scheme 深链处理（demo://page/...?r=xxx）
     // #ifdef APP-PLUS
     var deepLinkPath = launchParams.path || (launchParams.__path__ || null);
     if (deepLinkPath) {
       var url = '/pages/index/index';
       var queryParts = [];
       if (launchParams.referrer) queryParts.push('r=' + encodeURIComponent(launchParams.referrer));
      if (queryParts.length > 0) url += '?' + queryParts.join('&');
      console.log('[DeepLink] 跳转到:', url);
      // 延迟跳转，确保首页初始化完成
      setTimeout(function() {
        getApp().globalData.url_open(url);
      }, 500);
    }
    // #endif
  },
  onHide: function () {
    // 从前台进入后台
  }
}
</script>

<style>
/*每个页面公共css */
/* 引入应用基础样式。 */
@import '@/common/css/page.css';
@import '@/common/css/business.css';
@import '@/common/css/plugins.css';
@import '@/common/css/lib.css';
@import '@/common/css/theme.css';
@import '@/common/css/animation.css';

/* mall-app 特有样式 */
@import '@/static/app/warm/common/iconfont/iconfont.css';
</style>
