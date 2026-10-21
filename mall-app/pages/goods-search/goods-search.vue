<template>
  <view :class="theme_view">
    <view class="bg-white">
      <!-- 搜索关键字 -->
      <view class="padding-horizontal-main padding-top-main padding-bottom-sm pr">
        <view class="top-search-input">
          <component-search
            :propDefaultValue="post_data.keywords || ''"
            propBrColor="#ddd"
            @onsearch="search_input_confirm_event"
            :propIsOnEvent="true"
            @oninput="search_input_value_event"
            :propIsOnInputEvent="true"
            @onfocus="search_input_focus_event"
            :propIsOnFocusEvent="true"
            @onblur="search_input_blur_event"
            :propIsOnBlurEvent="true"
            :propIsRequired="false"
            :propPlaceholder="$t('goods-search-page.goods-search-page.search_placeholder')"
          ></component-search>
        </view>
        <view class="pa top-search-right">
          <view v-if="top_search_right_type_status" class="pr cp" @tap="search_submit_confirm_event">
            <uni-icons type="search" size="60rpx" color="#bbb"></uni-icons>
          </view>
          <view v-if="!top_search_right_type_status" class="pr cp" data-value="/pages/cart/cart" @tap="url_event">
            <uni-icons type="cart" size="60rpx" color="#bbb"></uni-icons>
            <view v-if="top_right_cart_total != 0" class="badge-icon pa">
              <component-badge :propNumber="top_right_cart_total"></component-badge>
            </view>
          </view>
        </view>
      </view>

      <!-- 排序 -->
      <view class="nav-sort oh pr">
        <view class="nav-sort-content">
          <block v-for="(item, index) in search_nav_sort_list" :key="index">
            <view class="item tc fl cp" :data-index="index" @tap="nav_sort_event">
              <text class="cr-base va-m text-size-sm">{{ item.name }}</text>
              <image
                v-if="(item.icon || null) != null"
                class="icon va-m"
                :src="common_static_url + 'sort-' + item.icon + '-icon.png'"
                mode="aspectFill"
              ></image>
            </view>
          </block>
        </view>
        <image class="show-type-submit pa cp" :src="common_static_url+'show-'+(data_show_type_value == 0 ? 'list' : 'grid')+'-icon.png'" mode="aspectFill" @tap="data_show_type_event"></image>
        <image class="screening-submit pa cp" :src="common_static_url+'search-submit-icon.png'" mode="aspectFill" @tap="popup_form_event_show"></image>
      </view>
    </view>

    <!-- 列表 -->
    <scroll-view
      :scroll-y="true"
      class="scroll-box"
      @scrolltolower="scroll_lower"
      lower-threshold="60"
    >
      <view v-if="data_list.length > 0" class="padding-horizontal-main padding-top-main oh">
        <component-goods-list
          :propData="{
            style_type: data_show_type_value == 0 ? 1 : 0,
            goods_list: data_list,
            random: random_value
          }"
          :propCurrencySymbol="currency_symbol"
        ></component-goods-list>
      </view>
      <view v-else>
        <component-no-data
          :propStatus="data_list_loding_status"
          :propMsg="data_list_loding_msg"
        ></component-no-data>
      </view>
      <component-bottom-line :propStatus="data_bottom_line_status"></component-bottom-line>
    </scroll-view>

    <!-- 筛选条件 popup -->
    <component-popup :propShow="is_show_popup_form" propPosition="left" @onclose="popup_form_event_close">
      <form @submit="form_submit_event" class="popup-form oh">
        <view class="search-map padding-main bg-base">
          <view class="padding-main border-radius-main bg-white">
            <view class="map-item map-base br-b">
              <text>{{ $t('goods-search-page.goods-search-page.filter') }}</text>
              <text class="cr-main"> {{ data_total }} </text>
              <text>{{ $t('goods-search-page.goods-search-page.item_count') }}</text>
              <text class="fr cr-red cp" @tap="map_remove_event">{{ $t('goods-search-page.goods-search-page.clear') }}</text>
            </view>
            <input
              type="text"
              confirm-type="search"
              :placeholder="$t('goods-search-page.goods-search-page.search_placeholder')"
              name="keywords"
              :value="post_data.keywords || ''"
              class="map-keywords wh-auto round bg-base margin-top-lg"
              placeholder-class="cr-grey"
            />
          </view>

          <!-- 品牌 -->
          <view
            v-if="(search_map_list.brand_list || null) != null && search_map_list.brand_list.length > 0"
            class="map-item padding-horizontal-main padding-top-main border-radius-main bg-white spacing-mt"
          >
            <view class="map-nav br-b pr">
              <text>{{ $t('goods-search-page.goods-search-page.brand') }}</text>
              <text
                class="arrow-bottom pa cr-grey cp"
                v-if="search_map_list.brand_list.length > 3"
                @tap="more_event"
                data-value="brand_list"
              >{{ $t('goods-search-page.goods-search-page.more') }}</text>
            </view>
            <view
              class="map-content map-images-text-item map-brand-container oh margin-top-lg"
              :style="'height:' + map_fields_list.brand_list.height + ';'"
            >
              <block v-for="(item, index) in search_map_list.brand_list" :key="index">
                <view
                  :class="'item fl cr-base radius cp single-text tc ' + (index > 0 ? 'margin-left-sm ' : '') + (item.active == 1 ? 'cr-main br-main' : '')"
                  @tap="map_item_event"
                  :data-index="index"
                  data-field="brand_list"
                >
                  <image v-if="(item.logoUrl || null) != null" :src="item.logoUrl" mode="aspectFit"></image>
                  <text v-else>{{ item.name }}</text>
                </view>
              </block>
            </view>
          </view>

          <!-- 分类 -->
          <view
            v-if="(search_map_list.category_list || null) != null && search_map_list.category_list.length > 0"
            class="map-item padding-horizontal-main padding-top-main border-radius-main bg-white spacing-mt"
          >
            <view class="map-nav br-b pr">
              <text>{{ $t('goods-search-page.goods-search-page.category') }}</text>
              <text
                class="arrow-bottom pa cr-grey cp"
                v-if="search_map_list.category_list.length > 3"
                @tap="more_event"
                data-value="category_list"
              >{{ $t('goods-search-page.goods-search-page.more') }}</text>
            </view>
            <view
              class="map-content map-text-item map-category-container oh margin-top-lg"
              :style="'height:' + map_fields_list.category_list.height + ';'"
            >
              <block v-for="(item, index) in search_map_list.category_list" :key="index">
                <view
                  :class="'item fl cr-base radius cp margin-right-sm ' + (item.active == 1 ? 'cr-main br-main' : '')"
                  @tap="map_item_event"
                  :data-index="index"
                  data-field="category_list"
                >{{ item.name }}</view>
              </block>
            </view>
          </view>

          <!-- 价格 -->
          <view
            v-if="(search_map_list.screening_price_list || null) != null && search_map_list.screening_price_list.length > 0"
            class="map-item padding-horizontal-main padding-top-main border-radius-main bg-white spacing-mt"
          >
            <view class="map-nav br-b pr">
              <text>{{ $t('goods-search-page.goods-search-page.price') }}</text>
              <text
                class="arrow-bottom pa cr-grey cp"
                v-if="search_map_list.screening_price_list.length > 3"
                @tap="more_event"
                data-value="screening_price_list"
              >{{ $t('goods-search-page.goods-search-page.more') }}</text>
            </view>
            <view
              class="map-content map-text-item screening-price-container oh margin-top-lg"
              :style="'height:' + map_fields_list.screening_price_list.height + ';'"
            >
              <block v-for="(item, index) in search_map_list.screening_price_list" :key="index">
                <view
                  :class="'item fl cr-base radius cp margin-right-sm ' + (item.active == 1 ? 'cr-main br-main' : '')"
                  @tap="map_item_event"
                  :data-index="index"
                  data-field="screening_price_list"
                >{{ item.name }}</view>
              </block>
            </view>
          </view>

          <view class="search-submit padding-main pa">
            <button
              form-type="submit"
              class="bg-main cr-white text-size wh-auto round"
              :disabled="popup_form_loading_status"
              hover-class="none"
            >{{ $t('goods-search-page.goods-search-page.confirm') }}</button>
          </view>
        </view>
      </form>
    </component-popup>

    <!-- 快捷导航 -->
    <component-quick-nav></component-quick-nav>

    <!-- 公共 -->
    <component-common ref="common"></component-common>
  </view>
</template>
<script>
const app = getApp()
import componentCommon from '@/components/common/common'
import componentQuickNav from '@/components/quick-nav/quick-nav'
import componentNoData from '@/components/no-data/no-data'
import componentBottomLine from '@/components/bottom-line/bottom-line'
import componentGoodsList from '@/components/goods-list/goods-list'
import componentSearch from '@/components/search/search'
import componentBadge from '@/components/badge/badge'
import componentPopup from '@/components/popup/popup'
import iconfont from '@/components/iconfont/iconfont'
import { listSpuPages } from '@/api/pms/goods.js'
import { getCategoryList } from '@/api/pms/category.js'
import { request } from '@/config/request'

export default {
  data() {
    return {
      theme_view: app.globalData.get_theme_value_view(),
      common_static_url: app.globalData.get_static_url('common'),
      data_list_loding_status: 1,
      data_list_loding_msg: '',
      data_bottom_line_status: false,
      data_is_loading: 0,
      data_list: [],
      data_total: 0,
      data_page_total: 0,
      data_page: 1,
      top_right_cart_total: 0,
      params: null,
      post_data: {},
      load_status: 0,
      random_value: 0,
      top_search_right_type_status: false,
      share_info: {},
      // 排序
      search_nav_sort_index: 0,
      search_nav_sort_list: [
        { name: '综合', field: 'default', sort: 'asc', icon: null },
        { name: '销量', field: 'sales', sort: 'asc', icon: 'default' },
        { name: '价格', field: 'price', sort: 'asc', icon: 'default' },
        { name: '新品', field: 'id', sort: 'asc', icon: 'default' }
      ],
      // 展示样式（0九方格、1图文）
      data_show_type_value: 0,
      currency_symbol: app.globalData.currency_symbol(),
      // 筛选弹窗
      filter_data_loaded: 0,
      is_show_popup_form: false,
      popup_form_loading_status: false,
      // 筛选条件
      search_map_list: {
        brand_list: [],
        category_list: [],
        screening_price_list: []
      },
      map_fields_list: {
        brand_list: { height: '96rpx', default: '96rpx', form_key: 'brand_ids' },
        category_list: { height: '82rpx', default: '82rpx', form_key: 'category_ids' },
        screening_price_list: { height: '82rpx', default: '82rpx', form_key: 'screening_price_values' }
      }
    }
  },

  components: {
    componentCommon,
    componentQuickNav,
    componentNoData,
    componentBottomLine,
    componentGoodsList,
    componentSearch,
    componentBadge,
    componentPopup,
    iconfont
  },

  onLoad(params) {
    app.globalData.page_event_onload_handle(params)
    params = app.globalData.launch_params_handle(params)
    this.setData({
      params: params,
      post_data: {
        keywords: params.keywords || params.keyword || ''
      }
    })
  },

  onShow() {
    app.globalData.page_event_onshow_handle()
    this.init()
    this.get_cart_count()
    if ((this.$refs.common || null) != null) {
      this.$refs.common.on_show()
    }
  },

  onPullDownRefresh() {
    this.setData({ data_page: 1 })
    this.get_data_list(1)
  },

  methods: {
    init() {
      if (this.load_status == 0) {
        this.setData({ load_status: 1 })
        this.get_data_list(1)
      }
    },

    // 获取数据列表
    get_data_list(is_mandatory) {
      if ((is_mandatory || 0) == 0) {
        if (this.data_bottom_line_status == true) {
          uni.stopPullDownRefresh()
          return false
        }
      }
      if (this.data_is_loading == 1) return false
      this.setData({ data_is_loading: 1 })

      // 使用筛选参数构建方法
      var queryParams = this.request_map_handle()

      // URL参数中的分类id（后端自动识别层级）
      if (this.params && this.params.category_id) {
        queryParams.categoryId = parseInt(this.params.category_id)
      }

      // URL参数中的品牌id
      if (this.params && this.params.brand) {
        queryParams.brandId = parseInt(this.params.brand)
      }

      listSpuPages(queryParams)
        .then(response => {
          this.setData({ data_is_loading: 0 })
          uni.stopPullDownRefresh()

          if (response.code === '00000' || response.data) {
            var data = response.data
            var list = data.list || []
            var total = data.total || 0

            // 首次加载时填充筛选选项（后续搜索保持筛选态，不复写）
            if (this.data_page <= 1 && this.filter_data_loaded !== 1) {
              var temp_search_map = this.search_map_list
              if (data.brandList) {
                var brandList = []
                data.brandList.forEach(function (b) {
                  brandList.push({ id: b.id, name: b.name, logoUrl: b.logoUrl || null, active: 0 })
                })
                temp_search_map.brand_list = brandList
              }
              if (data.screeningPriceList) {
                var priceList = []
                data.screeningPriceList.forEach(function (p) {
                  priceList.push({ name: p.name, min_price: p.minPrice, max_price: p.maxPrice, active: 0 })
                })
                temp_search_map.screening_price_list = priceList
              }
              this.setData({ search_map_list: temp_search_map, filter_data_loaded: 1 })
            }

            // 转换为 component-goods-list 期望的格式
            var goodsList = list.map(function (item) {
              return {
                id: item.id,
                goods_url: '/pages/goods-detail/goods-detail?id=' + item.id,
                title: item.name,
                images: item.picUrl,
                min_price: item.price || item.retailPrice || 0,
                original_price: item.marketPrice || item.originalPrice || 0,
                show_field_price_status: 1,
                show_price_symbol: '¥',
                sales_count: item.sales || 0,
                is_more_price: 0,
                default_sku_id: item.defaultSkuId || null,
                is_exist_many_spec: item.isExistManySpec || 0
              }
            })

            if (this.data_page <= 1) {
              var temp_data_list = goodsList
            } else {
              var temp_data_list = this.data_list.concat(goodsList)
            }

            var pageTotal = Math.ceil(total / 10)
            this.setData({
              data_list: temp_data_list,
              random_value: Math.random(),
              data_total: total,
              data_page_total: pageTotal,
              data_list_loding_status: 3,
              data_page: this.data_page + 1,
              data_bottom_line_status:
                temp_data_list.length > 0 && this.data_page > 1 && this.data_page > pageTotal
            })

            if (goodsList.length == 0 && this.data_page <= 1) {
              this.setData({
                data_list: [],
                data_bottom_line_status: false
              })
            }
          } else {
            this.setData({
              data_list_loding_status: 0,
              data_list_loding_msg: response.msg || '加载失败'
            })
          }
        })
        .catch(() => {
          uni.stopPullDownRefresh()
          this.setData({
            data_is_loading: 0,
            data_list_loding_status: 2,
            data_list_loding_msg: '网络错误'
          })
        })
    },

    scroll_lower(e) {
      this.get_data_list()
    },

    // 排序事件
    nav_sort_event(e) {
      var index = e.currentTarget.dataset.index || 0
      var temp_search_nav_sort = this.search_nav_sort_list
      var temp_sort = temp_search_nav_sort[index]['sort'] == 'desc' ? 'asc' : 'desc'
      for (var i in temp_search_nav_sort) {
        if (i != index) {
          if (temp_search_nav_sort[i]['icon'] != null) {
            temp_search_nav_sort[i]['icon'] = 'default'
          }
          temp_search_nav_sort[i]['sort'] = 'desc'
        }
      }
      temp_search_nav_sort[index]['sort'] = temp_sort
      if (temp_search_nav_sort[index]['icon'] != null) {
        temp_search_nav_sort[index]['icon'] = temp_sort
      }

      this.setData({
        search_nav_sort_index: index,
        search_nav_sort_list: temp_search_nav_sort,
        data_page: 1,
        data_list: [],
        data_list_loding_status: 1,
        data_bottom_line_status: false
      })
      this.get_data_list(1)
    },

    // 数据展示类型切换
    data_show_type_event(e) {
      this.setData({
        data_show_type_value: this.data_show_type_value == 0 ? 1 : 0
      })
    },

    // 搜索输入事件
    search_input_value_event(value) {
      var temp_post = this.post_data
      temp_post['keywords'] = value
      this.setData({ post_data: temp_post })
    },

    search_input_blur_event(value) {
      if (value == '') {
        this.setData({ top_search_right_type_status: false })
      }
    },

    search_input_focus_event() {
      this.setData({ top_search_right_type_status: true })
    },

    search_input_confirm_event(value) {
      var temp_post = this.post_data
      temp_post['keywords'] = value
      this.setData({
        top_search_right_type_status: false,
        post_data: temp_post,
        data_page: 1,
        data_list: [],
        data_list_loding_status: 1,
        data_bottom_line_status: false
      })
      this.get_data_list(1)
    },

    search_submit_confirm_event() {
      this.setData({
        top_search_right_type_status: false,
        data_page: 1,
        data_list: [],
        data_list_loding_status: 1,
        data_bottom_line_status: false
      })
      this.get_data_list(1)
    },

    // 筛选条件开启
    popup_form_event_show() {
      this.setData({ is_show_popup_form: true })
      if (this.search_map_list.category_list.length === 0) {
        this.get_filter_data()
      }
    },

    // 筛选条件关闭
    popup_form_event_close() {
      this.setData({ is_show_popup_form: false })
    },

    // 获取筛选选项数据（品牌和价格从搜索接口直接返回，此处仅补充分类列表）
    get_filter_data() {
      var self = this
      var currentCategoryId = (this.params && this.params.category_id) ? parseInt(this.params.category_id) : 0
      getCategoryList(currentCategoryId).then(function (res) {
        var data = res.data || []
        var categoryList = []
        data.forEach(function (item) {
          if (item && item.id && item.name) {
            categoryList.push({ id: item.id, name: item.name, active: 0 })
          }
        })
        var searchMapList = self.search_map_list
        searchMapList.category_list = categoryList
        self.setData({ search_map_list: searchMapList })
      })
    },

    // 条件-更多展示
    more_event(e) {
      var value = e.currentTarget.dataset.value || null
      var tempMore = this.map_fields_list
      if (value != null && (tempMore[value] || null) != null) {
        tempMore[value]['height'] = tempMore[value]['height'] == 'auto' ? tempMore[value]['default'] : 'auto'
        this.setData({ map_fields_list: tempMore })
      }
    },

    // 条件-选择（价格区间单选，其他多选）
    map_item_event(e) {
      var index = e.currentTarget.dataset.index
      var field = e.currentTarget.dataset.field
      var tempList = this.search_map_list
      if ((tempList[field] || null) != null && (tempList[field][index] || null) != null) {
        var isActive = (tempList[field][index]['active'] || 0) == 0 ? 1 : 0
        // 价格区间：单选，先清空其他选中
        if (field === 'screening_price_list') {
          for (var k in tempList[field]) {
            tempList[field][k]['active'] = 0
          }
        }
        tempList[field][index]['active'] = isActive
        this.setData({ search_map_list: tempList })
      }
    },

    // 条件-清空
    map_remove_event() {
      var tempList = this.search_map_list
      var tempPost = this.post_data
      tempPost['keywords'] = ''
      for (var i in tempList) {
        if ((tempList[i] || null) != null && tempList[i].length > 0) {
          for (var k in tempList[i]) {
            tempList[i][k]['active'] = 0
          }
        }
      }
      var tempSort = this.search_nav_sort_list
      for (var j in tempSort) {
        tempSort[j]['sort'] = 'asc'
        tempSort[j]['icon'] = (tempSort[j]['field'] == 'default') ? null : 'default'
      }
      this.setData({
        search_map_list: tempList,
        post_data: tempPost,
        is_show_popup_form: false,
        search_nav_sort_list: tempSort,
        search_nav_sort_index: 0,
        data_page: 1,
        data_list: [],
        data_list_loding_status: 1,
        data_bottom_line_status: false
      })
      this.get_data_list(1)
    },

    // 筛选确认
    form_submit_event(e) {
      var tempPost = this.post_data
      tempPost['keywords'] = e.detail.value.keywords || ''
      this.setData({
        post_data: tempPost,
        is_show_popup_form: false,
        data_page: 1,
        data_list: [],
        data_list_loding_status: 1,
        data_bottom_line_status: false
      })
      this.get_data_list(1)
    },

    // 构建筛选参数
    request_map_handle() {
      var post_data = {}
      post_data['keywords'] = this.post_data.keywords || ''
      post_data['pageNum'] = this.data_page
      post_data['pageSize'] = 10

      // 品牌筛选
      if (this.search_map_list.brand_list && this.search_map_list.brand_list.length > 0) {
        var bids = []
        this.search_map_list.brand_list.forEach(function (item) {
          if (item.active == 1) bids.push(item.id)
        })
        if (bids.length > 0) {
          post_data['brandId'] = bids[0]
        }
      }

      // 分类筛选
      if (this.search_map_list.category_list && this.search_map_list.category_list.length > 0) {
        var cids = []
        this.search_map_list.category_list.forEach(function (item) {
          if (item.active == 1) cids.push(item.id)
        })
        if (cids.length > 0) {
          post_data['categoryId'] = cids[0]
        }
      }

      // 价格区间筛选
      if (this.search_map_list.screening_price_list && this.search_map_list.screening_price_list.length > 0) {
        this.search_map_list.screening_price_list.forEach(function (item) {
          if (item.active == 1) {
            if (item.min_price != null && item.min_price >= 0) {
              post_data['minPrice'] = item.min_price
            }
            if (item.max_price != null && item.max_price > 0) {
              post_data['maxPrice'] = item.max_price
            }
          }
        })
      }

      // 排序
      var sortItem = this.search_nav_sort_list[this.search_nav_sort_index]
      if (sortItem.field !== 'default') {
        post_data['sortField'] = sortItem.field
        post_data['sort'] = sortItem.sort
      }

      return post_data
    },

    url_event(e) {
      app.globalData.url_event(e)
    },

    // 获取购物车初始数量。
    get_cart_count() {
      var self = this
      request({
        url: '/mall-oms/app-api/v1/carts',
        method: 'GET',
        headers: { auth: true }
      }).then(function(res) {
        if (res.code === '00000') {
          var list = res.data || []
          var total = 0
          for (var i = 0; i < list.length; i++) {
            total += list[i].count || 0
          }
          self.setData({ top_right_cart_total: total })
          self.set_tab_bar_badge_handle()
        }
      }).catch(function() {})
    },

    goods_cart_count_handle(cart_number) {
        this.setData({
            top_right_cart_total: cart_number
        });
        this.set_tab_bar_badge_handle();
    },

    set_tab_bar_badge_handle() {
        var cart_total = this.top_right_cart_total || 0;
        app.globalData.set_tab_bar_badge('cart', cart_total);
    }
  }
}
</script>
<style>
@import './goods-search.css';
</style>
