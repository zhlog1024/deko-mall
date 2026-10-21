import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { 
  FreightTemplateForm, 
  FreightTemplatePageQuery, 
  FreightTemplate,
  FreightTemplatePageResult 
} from './types';

/**
 * 分页查询运费模板列表
 */
export function getFreightTemplatePages(
  queryParams: FreightTemplatePageQuery
): AxiosPromise<FreightTemplatePageResult> {
  return request({
    url: '/mall-pms/scm-api/freight/template/page',
    method: 'get',
    params: queryParams
  });
}

/**
 * 获取运费模板详情
 */
export function getFreightTemplate(id: number): AxiosPromise<FreightTemplate> {
  return request({
    url: `/mall-pms/scm-api/freight/template/${id}`,
    method: 'get'
  });
}

/**
 * 创建运费模板
 */
export function createFreightTemplate(data: FreightTemplateForm): AxiosPromise<boolean> {
  return request({
    url: '/mall-pms/scm-api/freight/template',
    method: 'post',
    data
  });
}

/**
 * 更新运费模板
 */
export function updateFreightTemplate(id: number, data: FreightTemplateForm): AxiosPromise<boolean> {
  return request({
    url: `/mall-pms/scm-api/freight/template/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除运费模板
 */
export function deleteFreightTemplate(ids: string): AxiosPromise<boolean> {
  return request({
    url: `/mall-pms/scm-api/freight/template/${ids}`,
    method: 'delete'
  });
}

/**
 * 设置默认运费模板
 */
export function setDefaultFreightTemplate(id: number): AxiosPromise<boolean> {
  return request({
    url: `/mall-pms/scm-api/freight/template/${id}/default`,
    method: 'put'
  });
}

/**
 * 获取运费模板列表(不分页)
 */
export function getFreightTemplateList(): AxiosPromise<FreightTemplate[]> {
  return request({
    url: '/mall-pms/scm-api/freight/template/list',
    method: 'get'
  });
}

/**
 * 获取供应商默认运费模板
 */
export function getSupplierDefaultTemplate(supplierId: string): AxiosPromise<FreightTemplate> {
  return request({
    url: `/mall-pms/scm-api/freight/template/supplier/${supplierId}/default`,
    method: 'get'
  });
}
