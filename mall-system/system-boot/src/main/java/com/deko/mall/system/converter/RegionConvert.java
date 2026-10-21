package com.deko.mall.system.converter;


import com.deko.mall.system.dto.*;
import com.deko.mall.system.model.entity.Region;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RegionConvert {

    RegionConvert INSTANCE = Mappers.getMapper(RegionConvert.class);

    RegionResDTO convert2SameRegionResDTO(RegionResDTO regionResDTO);

    List<ProvinceResDTO> convert2ProvinceResDTOList(List<Region> Regions);

    List<CityResDTO> convert2CityResDTOList(List<Region> Regions);

    List<DistrictResDTO> convert2DistrictResDTOList(List<Region> Regions);

    List<TownResDTO> convert2TownResDTOList(List<Region> Regions);

    List<ProvinceResDTO> convertTownResDTOList(List<Region> Regions);

    List<CityResDTO> convert1TownResDTOList(List<Region> Regions);

    List<RegionResDTO> convert2RegionResDTOList(List<Region> Regions);

}
