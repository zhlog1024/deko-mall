package com.deko.mall.system.service;

import com.deko.mall.system.dto.*;

import java.util.List;

/**
 * 行政区域地址服务接口层
 */
public interface SysRegionService {


    List<DistrictResDTO> findAllDistrictByPidList(DistrictRegionReq req);

    List<DistrictResDTO> findAllDistrictByPid(Long pid);

    List<CityResDTO> findAllCityByPidList(CityRegionReq req);

    List<CityResDTO> findAllCityByPid(Long pid);

    List<ProvinceResDTO> getAllProvinceByPid();

    List<RegionResDTO> findAllRegion();

    List<TownResDTO> findAllTownByPidList(TownRegionReq req);

    List<ProvinceResDTO> findAllProvinceCitys();

    List<TownResDTO> findAllTownByPid(Long pid);

    /** 查询所有国家(level=0) */
    List<ProvinceResDTO> findAllCountry();

    /** 根据国家ID查询省份(level=1) */
    List<ProvinceResDTO> findAllProvinceByCountryId(Long countryId);
}
