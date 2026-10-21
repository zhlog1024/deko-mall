import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { 
  ForbiddenAreaForm, 
  ForbiddenAreaPageQuery, 
  ForbiddenArea,
  ForbiddenAreaPageResult 
} from './types';

/**
 * 分页查询禁售区域列表
 */
export function getForbiddenAreaPages(
  queryParams: ForbiddenAreaPageQuery
): AxiosPromise<ForbiddenAreaPageResult> {
  return request({
    url: '/mall-pms/api/forbidden/area/page',
    method: 'get',
    params: queryParams
  });
}

/**
 * 获取禁售区域详情
 */
export function getForbiddenArea(id: number): AxiosPromise<ForbiddenArea> {
  return request({
    url: `/mall-pms/api/forbidden/area/${id}`,
    method: 'get'
  });
}

/**
 * 创建禁售区域
 */
export function createForbiddenArea(data: ForbiddenAreaForm): AxiosPromise<boolean> {
  return request({
    url: '/mall-pms/api/forbidden/area',
    method: 'post',
    data
  });
}

/**
 * 更新禁售区域
 */
export function updateForbiddenArea(id: number, data: ForbiddenAreaForm): AxiosPromise<boolean> {
  return request({
    url: `/mall-pms/api/forbidden/area/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除禁售区域
 */
export function deleteForbiddenArea(ids: string): AxiosPromise<boolean> {
  return request({
    url: `/mall-pms/api/forbidden/area/${ids}`,
    method: 'delete'
  });
}

/**
 * 获取禁售区域列表(不分页)
 */
export function getForbiddenAreaList(): AxiosPromise<ForbiddenArea[]> {
  return request({
    url: '/mall-pms/api/forbidden/area/list',
    method: 'get'
  });
}

/**
 * 检查区域是否在禁售区域内
 */
export function isInForbiddenArea(regionCode: string, supplierId: string): AxiosPromise<boolean> {
  return request({
    url: '/mall-pms/api/forbidden/area/check',
    method: 'get',
    params: {
      regionCode,
      supplierId
    }
  });
}

/**
 * 获取供应商的所有禁售区域
 */
export function getSupplierForbiddenAreas(supplierId: string): AxiosPromise<ForbiddenArea[]> {
  return request({
    url: `/mall-pms/api/forbidden/area/supplier/${supplierId}/list`,
    method: 'get'
  });
}
