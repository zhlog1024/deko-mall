/**
 * 内容管理-视频 API（mall-cms,运营上传 PGC 视频)
 * 契约对照 mall-cms VideoAdminController(/api/v1/video):
 * page/detail/新增/编辑/enable/DELETE;权限码 cms:video:add/edit/enable/delete
 */
import request from "@/utils/request";

const BASE = "/mall-cms/api/v1/video";

/** 视频分页查询(管理端:keyword/isRecommended/isHot/isEnable 过滤) */
export interface VideoPageQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  isRecommended?: number;
  isHot?: number;
  isEnable?: number;
  /** sort=自定义排序优先(默认);createTime=创建时间倒序;accessCount=浏览数倒序;duration=时长倒序 */
  sortField?: string;
}

/** 视频管理列表 VO(列表不带 videoUrl 大字段) */
export interface VideoPageVO {
  id: number;
  title: string;
  cover?: string;
  description?: string;
  duration?: number;
  accessCount?: number;
  isRecommended?: number;
  isHot?: number;
  sort?: number;
  isEnable?: number;
  createTime?: string;
  updateTime?: string;
}

/** 视频详情(编辑回显,含视频地址) */
export interface VideoDetailVO extends VideoPageVO {
  videoUrl?: string;
}

/** 视频新增/编辑表单 */
export interface VideoForm {
  id?: number;
  title: string;
  cover?: string;
  videoUrl: string;
  /** 时长(秒) */
  duration: number;
  description?: string;
  isRecommended: number;
  isHot: number;
  sort: number;
  isEnable: number;
  spuId?: number;
  spuName?: string;
  spuPic?: string;
  spuPrice?: string;
}

/** 启用/停用表单 */
export interface VideoEnableForm {
  isEnable: number;
}

/** 视频分页列表 */
export function getVideoPage(query: VideoPageQuery) {
  return request<any, PageResult<VideoPageVO>>({
    url: `${BASE}/page`,
    method: "get",
    params: query,
  });
}

/** 视频详情(编辑回显) */
export function getVideoDetail(id: number) {
  return request<any, VideoDetailVO>({
    url: `${BASE}/${id}`,
    method: "get",
  });
}

/** 新增视频 */
export function addVideo(data: VideoForm) {
  return request({
    url: BASE,
    method: "post",
    data,
  });
}

/** 编辑视频 */
export function updateVideo(id: number, data: VideoForm) {
  return request({
    url: `${BASE}/${id}`,
    method: "put",
    data,
  });
}

/** 启用/停用视频 */
export function enableVideo(id: number, data: VideoEnableForm) {
  return request({
    url: `${BASE}/${id}/enable`,
    method: "put",
    data,
  });
}

/** 删除视频(逻辑删除,支持批量) */
export function deleteVideos(ids: number[]) {
  return request({
    url: `${BASE}/${ids.join(",")}`,
    method: "delete",
  });
}

/**
 * 评论/举报运营管理
 */

/** 评论管理分页项 */
export interface VideoCommentAdminVO {
  id: number;
  videoId?: number;
  userId?: number;
  nickname?: string;
  avatar?: string;
  pid?: number;
  content?: string;
  images?: string;
  likeCount?: number;
  replyCount?: number;
  isEnable?: number;
  createTime?: string;
  videoTitle?: string;
}

/** 举报管理分页项 */
export interface VideoReportAdminVO {
  id: number;
  videoId?: number;
  userId?: number;
  type?: string;
  content?: string;
  status?: number;
  createTime?: string;
  videoTitle?: string;
}

const OPERATE_BASE = "/mall-cms/api/v1/video-operate";

/** 评论分页 */
export function getVideoCommentPage(params: Record<string, any>) {
  return request<any, PageResult<VideoCommentAdminVO>>({
    url: `${OPERATE_BASE}/comments`,
    method: "get",
    params,
  });
}

/** 评论删除(运营) */
export function deleteVideoCommentOperate(commentId: number) {
  return request({
    url: `${OPERATE_BASE}/comments/${commentId}`,
    method: "delete",
  });
}

/** 评论启停 */
export function enableVideoCommentOperate(commentId: number, isEnable: number) {
  return request({
    url: `${OPERATE_BASE}/comments/${commentId}/enable`,
    method: "put",
    params: { isEnable },
  });
}

/** 举报分页 */
export function getVideoReportPage(params: Record<string, any>) {
  return request<any, PageResult<VideoReportAdminVO>>({
    url: `${OPERATE_BASE}/reports`,
    method: "get",
    params,
  });
}

/** 举报处理状态更新 */
export function handleVideoReport(reportId: number, status = 1) {
  return request({
    url: `${OPERATE_BASE}/reports/${reportId}/handle`,
    method: "put",
    params: { status },
  });
}
