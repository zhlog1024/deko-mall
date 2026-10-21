/**
 * 区域基础数据类型
 */
export interface RegionDTO {
  /** 区域ID */
  id: number;
  /** 区域父ID */
  pid: number;
  /** 区域名称 */
  name: string;
  /** 区域编码 */
  code: string;
  /** 区域级别 (0-省,1-市,2-区) */
  level: number;
}

/**
 * 省级区域DTO
 */
export interface ProvinceDTO extends RegionDTO {
  /** 城市列表 */
  children?: CityDTO[];
}

/**
 * 市级区域DTO
 */
export interface CityDTO extends RegionDTO {
  /** 区县列表 */
  children?: DistrictDTO[];
}

/**
 * 区县DTO
 */
export interface DistrictDTO extends RegionDTO {
  /** 街道列表 */
  children?: TownDTO[];
}

/**
 * 街道DTO
 */
export interface TownDTO extends RegionDTO {}
