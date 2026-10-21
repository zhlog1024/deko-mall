import request from '@/utils/request'
import { AxiosPromise } from 'axios'
import { RegionDTO } from './types'

/**
 * 获取所有省份
 * @param countryId 国家ID,默认中国
 */
export function getRegionList(countryId?: number): AxiosPromise<RegionDTO[]> {
  return request({
    url: '/mall-system/api/v1/region/findAllProvince',
    method: 'get',
    params: countryId ? { countryId } : undefined
  })
}

/**
 * 获取所有国家(level=0)
 */
export function getCountryList(): AxiosPromise<RegionDTO[]> {
  return request({
    url: '/mall-system/api/v1/region/findAllCountry',
    method: 'get'
  })
}

/**
 * 获取 APP 全局配置(含 enable_international_phone)
 */
export function getAppConfig(): AxiosPromise<Record<string, any>> {
  return request({
    url: '/mall-system/app-api/v1/config/common',
    method: 'get'
  })
}

/**
 * 获取省市区三级联动数据
 */
export function getAllRegions(): AxiosPromise<any[]> {
  return request({
    url: '/mall-system/api/v1/region/findAllRegion',
    method: 'get'
  })
}

/**
 * 根据父ID获取城市列表
 * @param pid 父ID
 */
export function getCitiesByProvinceId(pid: number): AxiosPromise<any[]> {
  return request({
    url: '/mall-system/api/v1/region/findAllCityByPid',
    method: 'get',
    params: { pid }
  })
}

/**
 * 根据父ID获取区县列表
 * @param pid 父ID
 */
export function getDistrictsByCityId(pid: number): AxiosPromise<any[]> {
  return request({
    url: '/mall-system/api/v1/region/findAllDistrictByPid',
    method: 'get',
    params: { pid }
  })
}
