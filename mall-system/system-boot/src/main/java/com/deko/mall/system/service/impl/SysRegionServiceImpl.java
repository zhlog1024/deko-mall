package com.deko.mall.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.system.converter.RegionConvert;
import com.deko.mall.system.dto.*;
import com.deko.mall.system.enums.BaseConstant;
import com.deko.mall.system.enums.RegionLevelEnum;
import com.deko.mall.system.mapper.RegionMapper;
import com.deko.mall.system.model.entity.Region;
import com.deko.mall.system.service.SysRegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 部门业务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements SysRegionService {


    public List<Region> getAllChinaRegion() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getHidden, 0)
                .ge(Region::getLevel, 1)
                .le(Region::getLevel, 3);

        List<Region> regions = baseMapper.selectList(queryWrapper);
        return regions;
    }

    public List<Region> getAllProvinceByPid(Long pid) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.PROVINCE_LEVEL.getValue());
        queryWrapper.eq(Region::getParentId, pid);
        List<Region> regions = baseMapper.selectList(queryWrapper);
        return regions;
    }

    public List<Region> findAllCityByPid1(Long pid) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.CITY_LEVEL.getValue());
        queryWrapper.eq(Region::getParentId, pid);
        List<Region> regions = baseMapper.selectList(queryWrapper);
        return regions;
    }

    @Override
    public List<ProvinceResDTO> getAllProvinceByPid() {
        List<Region> allProvince = this.getAllProvinceByPid(BaseConstant.CHINA_REGION_ID);
        return RegionConvert.INSTANCE.convert2ProvinceResDTOList(allProvince);
    }

    @Override
    public List<RegionResDTO> findAllRegion() {
        List<Region> allProvince = this.getAllChinaRegion();
        List<RegionResDTO> regionResDTOS = RegionConvert.INSTANCE.convert2RegionResDTOList(allProvince);
        return recursionForRegion(BaseConstant.CHINA_REGION_ID, regionResDTOS);
    }

    /**
     * 递归查询省市区子节点
     */
    private List<RegionResDTO> recursionForRegion(Long parentId, List<RegionResDTO> regions) {
        List<RegionResDTO> list = new ArrayList<>();
        Optional.ofNullable(regions).orElse(new ArrayList<>())
                .stream().filter(region -> region.getParentId().equals(parentId))
                .forEach(region -> {
                    RegionResDTO regionResDTO = RegionConvert.INSTANCE.convert2SameRegionResDTO(region);
                    List<RegionResDTO> children = recursionForRegion(region.getId(), regions);
                    regionResDTO.setChildren(children);
                    list.add(regionResDTO);
                });
        return CollectionUtil.isNotEmpty(list) ? list : null;
    }

    @Override
    public List<TownResDTO> findAllTownByPidList(TownRegionReq req) {
        List<Region> townList = this.findAllTownByPidList(req.getPidList());
        return RegionConvert.INSTANCE.convert2TownResDTOList(townList);
    }

    @Override
    public List<ProvinceResDTO> findAllProvinceCitys() {
        return this.findAllProvinceCitys1(BaseConstant.CHINA_REGION_ID);
    }

    public List<ProvinceResDTO> findAllProvinceCitys1(Long contryId) {
        List<Region> allProvince = getAllProvinceByPid(contryId);
        List<ProvinceResDTO> provinceVOS = RegionConvert.INSTANCE.convertTownResDTOList(allProvince);
        provinceVOS.stream().forEach(provinceVO -> {
            provinceVO.setCheck(false);
            provinceVO.setDisabled(false);
            List<Region> cityList = findAllCityByPid1(provinceVO.getId());
            List<CityResDTO> cityVOS = RegionConvert.INSTANCE.convert1TownResDTOList(cityList);
            cityVOS.stream().forEach(cityVO -> {
                cityVO.setCheck(false);
                cityVO.setDisabled(false);
            });
            provinceVO.setCitys(cityVOS);
        });
        return provinceVOS;
    }

    @Override
    public List<TownResDTO> findAllTownByPid(Long pid) {
        List<Region> townList = this.findAllTownByPid1(pid);
        return RegionConvert.INSTANCE.convert2TownResDTOList(townList);
    }


    @Override
    public List<DistrictResDTO> findAllDistrictByPidList(DistrictRegionReq req) {
        List<Region> districtList = this.findAllDistrictByPidList(req.getPidList());
        return RegionConvert.INSTANCE.convert2DistrictResDTOList(districtList);
    }

    @Override
    public List<DistrictResDTO> findAllDistrictByPid(Long pid) {
        List<Region> districtList = this.findAllDistrictByPid1(pid);
        return RegionConvert.INSTANCE.convert2DistrictResDTOList(districtList);
    }

    public List<Region> findAllDistrictByPid1(Long pid) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.DISTRICT_LEVEL.getValue());
        queryWrapper.eq(Region::getParentId, pid);
        List<Region> regions = baseMapper.selectList(queryWrapper);
        return regions;
    }

    @Override
    public List<CityResDTO> findAllCityByPidList(CityRegionReq req) {
        List<Region> cityList = this.findAllCityByPidList(req.getPidList());
        return RegionConvert.INSTANCE.convert2CityResDTOList(cityList);
    }

    @Override
    public List<CityResDTO> findAllCityByPid(Long pid) {
        List<Region> cityList = this.findAllCityByPid1(pid);
        return RegionConvert.INSTANCE.convert2CityResDTOList(cityList);
    }

    public List<Region> findAllTownByPid1(Long pid) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.TOWN_LEVEL.getValue());
        queryWrapper.eq(Region::getParentId, pid);
        List<Region> regions = baseMapper.selectList(queryWrapper);
        return regions;
    }


    public List<Region> findAllCityByPidList(List<Long> pidList) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.CITY_LEVEL.getValue());
        queryWrapper.in(Region::getParentId, pidList);
        return baseMapper.selectList(queryWrapper);
    }

    public List<Region> findAllDistrictByPidList(List<Long> pidList) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.DISTRICT_LEVEL.getValue());
        queryWrapper.in(Region::getParentId, pidList);
        return baseMapper.selectList(queryWrapper);
    }

    public List<Region> findAllTownByPidList(List<Long> pidList) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.TOWN_LEVEL.getValue());
        queryWrapper.in(Region::getParentId, pidList);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<ProvinceResDTO> findAllCountry() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, RegionLevelEnum.COUNTRY_LEVEL.getValue());
        queryWrapper.eq(Region::getParentId, 0);
        queryWrapper.eq(Region::getHidden, 0);
        queryWrapper.orderByAsc(Region::getSort);
        List<Region> countries = baseMapper.selectList(queryWrapper);
        return RegionConvert.INSTANCE.convert2ProvinceResDTOList(countries);
    }

    @Override
    public List<ProvinceResDTO> findAllProvinceByCountryId(Long countryId) {
        if (countryId == null) {
            countryId = BaseConstant.CHINA_REGION_ID;
        }
        List<Region> provinces = this.getAllProvinceByPid(countryId);
        return RegionConvert.INSTANCE.convert2ProvinceResDTOList(provinces);
    }
}
