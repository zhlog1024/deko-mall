import defaultHeaderNav from "../../../config/const/header-nav";
import defaultFooterNav from "../../../config/const/footer-nav";
import defaultVideo from "../../../config/const/video";
import defaultRowLine from "../../../config/const/row-line";
import defaultSearch from "../../../config/const/search";
import defaultNavGroup from "../../../config/const/nav-group";
import defaultAuxiliaryBlank from "../../../config/const/auxiliary-blank";
import defaultRichText from "../../../config/const/rich-text";
import defaultCarousel from "../../../config/const/carousel";
import defaultNotice from "../../../config/const/notice";
import defaultCustom from "../../../config/const/custom";
import defaultTitle from "../../../config/const/title";
import defaultFloatWindow from "../../../config/const/float-window";
import defaultTabs from "../../../config/const/tabs";
import defaultGoodsList from "../../../config/const/goods-list";
import defaultGoodsMagic from "../../../config/const/goods-magic";
import defaultArticleList from "../../../config/const/article-list";
import defaultArticleTabs from "../../../config/const/article-tabs";
import defaultAsk from "../../../config/const/ask";
import defaultAskTabs from "../../../config/const/ask-tabs";
import defaultPluginsVideoList from "../../../config/const/plugins-video-list";
import defaultPluginsVideoTabs from "../../../config/const/plugins-video-tabs";
import defaultGoodsTabs from "../../../config/const/goods-tabs";
import defaultDataTabs from "../../../config/const/data-tabs";
import defaultImgMagic from "../../../config/const/img-magic";
import defaultHotZone from "../../../config/const/hot-zone";
import defaultDataMagic from "../../../config/const/data-magic";
import defaultTabsCarousel from "../../../config/const/tabs-carousel";
import defaultTabsMagic from "../../../config/const/tabs-magic";
import defaultUserInfo from "../../../config/const/user-info";
import defaultShop from "../../../config/const/shop";
import defaultPluginsLive from "../../../config/const/plugins-live";
import defaultPluginsLiveTabs from "../../../config/const/plugins-live-tabs";

// 系统设置
interface DefaultSettings {
  header_nav: object;
  footer_nav: object;
  video: object;
  row_line: object;
  auxiliary_blank: object;
  rich_text: object;
  search: object;
  nav_group: object;
  carousel: object;
  notice: object;
  title: object;
  float_window: object;
  tabs: object;
  goods_list: object;
  goods_magic: object;
  goods_tabs: object;
  article_list: object;
  article_tabs: object;
  ask: object;
  ask_tabs: object;
  plugins_video: object;
  plugins_video_tabs: object;
  data_tabs: object;
  img_magic: object;
  data_magic: object;
  hot_zone: object;
  tabs_carousel: object;
  tabs_magic: object;
  user_info: object;
  shop: object;
  plugins_live: object;
  plugins_live_tabs: object;
}

const defaultSettings: DefaultSettings = {
  header_nav: defaultHeaderNav,
  footer_nav: defaultFooterNav,
  video: defaultVideo,
  row_line: defaultRowLine,
  auxiliary_blank: defaultAuxiliaryBlank,
  rich_text: defaultRichText,
  search: defaultSearch,
  nav_group: defaultNavGroup,
  carousel: defaultCarousel,
  notice: defaultNotice,
  custom: defaultCustom,
  title: defaultTitle,
  float_window: defaultFloatWindow,
  tabs: defaultTabs,
  goods_list: defaultGoodsList,
  goods_magic: defaultGoodsMagic,
  article_list: defaultArticleList,
  article_tabs: defaultArticleTabs,
  ask: defaultAsk,
  ask_tabs: defaultAskTabs,
  plugins_video: defaultPluginsVideoList,
  plugins_video_tabs: defaultPluginsVideoTabs,
  goods_tabs: defaultGoodsTabs,
  data_tabs: defaultDataTabs,
  img_magic: defaultImgMagic,
  data_magic: defaultDataMagic,
  hot_zone: defaultHotZone,
  tabs_carousel: defaultTabsCarousel,
  tabs_magic: defaultTabsMagic,
  user_info: defaultUserInfo,
  shop: defaultShop,
  plugins_live: defaultPluginsLive,
  plugins_live_tabs: defaultPluginsLiveTabs,
};

export default defaultSettings;
