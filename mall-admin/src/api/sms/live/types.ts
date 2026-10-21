/**
 * 直播管理类型声明
 */

/**
 * 直播间查询参数
 */
export interface LiveRoomQuery extends PageQuery {
  keywords?: string;
  status?: number;
}

/**
 * 直播间列表项/详情
 */
export interface LiveRoom {
  id: number;
  roomNo: string;
  title: string;
  cover?: string;
  streamName: string;
  pushUrl?: string;
  pullUrl?: string;
  status: number;
  statusName?: string;
  anchorName?: string;
  anchorAvatar?: string;
  onlineCount?: number;
  likeCount?: number;
  liveStartTime?: string;
  liveEndTime?: string;
  isRecommended?: number;
  sort: number;
  createTime?: string;
}

/**
 * 直播间分页结果
 */
export type LiveRoomPageResult = PageResult<LiveRoom[]>;

/**
 * 直播间表单
 */
export interface LiveRoomForm {
  id?: number;
  title: string;
  cover?: string;
  streamName?: string;
  anchorName?: string;
  anchorAvatar?: string;
  isLiveChat?: number;
  isLiveGoodsBuy?: number;
  isLiveLike?: number;
  liveTips?: string;
  liveReconnectNumber?: number;
  liveReconnectIntervalTime?: number;
  liveGoodsExplainAutoCloseTime?: number;
  isRecommended?: number;
  sort?: number;
  remark?: string;
}

/**
 * 直播商品挂载项
 * <p>
 * 后端 `LiveGoodsVO` 带 @JsonNaming(SnakeCase)（App 契约），出参是 **snake_case**，
 * 后台 axios 未做 snake→camel 转换，故此处必须用 snake_case 字段名；
 * 写成 camelCase（spuId/goodsUrl/liveGoodsIsExplain）会恒为 undefined，
 * 导致「讲解中」列永远显示「否」、「设讲解」按钮永远显示 —— 表现即"点设讲解没反应"。
 */
export interface LiveGoods {
  id: number;
  spu_id: number;
  images?: string;
  title: string;
  price?: number;
  inventory?: number;
  goods_url?: string;
  live_goods_is_explain?: number;
}

/**
 * 直播商品绑定表单
 */
export interface LiveRoomGoodsForm {
  roomId: number;
  spuId: number;
  sort?: number;
}

/**
 * 全局直播设置
 */
export interface LiveSettings {
  tencentSecretId?: string;
  tencentSecretKey?: string;
  pushDomain?: string;
  playDomain?: string;
  pushAuthKey?: string;
  playUseHttps?: boolean;
  wsHost?: string;
  wsPort?: number;
  wsIsWss?: number;
  applicationName?: string;
}
