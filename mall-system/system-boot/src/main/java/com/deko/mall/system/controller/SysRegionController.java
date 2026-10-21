package com.deko.mall.system.controller;

import cn.hutool.json.JSONUtil;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.dto.*;
import com.deko.mall.system.service.SysRegionService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "07.行政区域地址接口")
@RestController
@RequestMapping("/api/v1/region")
@RequiredArgsConstructor
@Slf4j
public class SysRegionController {

    private final SysRegionService sysRegionService;

    @ApiOperation(value = "省-市-区 3个节点的 公共数据查询")
    @GetMapping("/findAllRegion")
    public Result<List<RegionResDTO>> findAllRegion() {
        log.info("省-市-区 3个节点的 公共数据查询");
        List<RegionResDTO> list = sysRegionService.findAllRegion();
        return Result.success(list);
    }

    @ApiOperation(value = "省行政查询")
    @GetMapping("/findAllProvince")
    public Result<List<ProvinceResDTO>> findAllProvince(
            @RequestParam(required = false) Long countryId
    ) {
        log.info("省行政查询,countryId:{}", countryId);
        List<ProvinceResDTO> list;
        if (countryId != null) {
            list = sysRegionService.findAllProvinceByCountryId(countryId);
        } else {
            list = sysRegionService.getAllProvinceByPid();
        }
        return Result.success(list);
    }


    @ApiOperation(value = "国家查询(level=0)")
    @GetMapping("/findAllCountry")
    public Result<List<ProvinceResDTO>> findAllCountry() {
        log.info("国家查询");
        List<ProvinceResDTO> list = sysRegionService.findAllCountry();
        return Result.success(list);
    }


    @ApiOperation(value = "市行政查询")
    @ApiImplicitParam(name = "pid", value = "地址父级ID", required = true, dataType = "Long")
    @GetMapping("/findAllCityByPid")
    public Result<List<CityResDTO>> findAllCityByPid(@RequestParam Long pid) {
        log.info("省行政查询,pid:{}", pid);
        List<CityResDTO> citys = sysRegionService.findAllCityByPid(pid);
        return Result.success(citys);
    }

    @ApiOperation(value = "市行政查询(批量)")
    @PostMapping("/findAllCityByPidList")
    public Result<List<CityResDTO>> findAllCityByPidList(@RequestBody CityRegionReq req) {
        log.info("市行政查询,req:{}", JSONUtil.toJsonStr(req));
        List<CityResDTO> cityList = sysRegionService.findAllCityByPidList(req);
        return Result.success(cityList);
    }


    @ApiOperation(value = "区行政查询")
    @ApiImplicitParam(name = "pid", value = "地址父级ID", required = true, dataType = "Long")
    @GetMapping("/findAllDistrictByPid")
    public Result<List<DistrictResDTO>> findAllDistrictByPid(@RequestParam Long pid) {
        log.info("区行政查询,pid:{}", pid);
        List<DistrictResDTO> list = sysRegionService.findAllDistrictByPid(pid);
        return Result.success(list);
    }

    @ApiOperation(value = "区行政查询(批量)")
    @PostMapping("/findAllDistrictByPidList")
    public Result<List<DistrictResDTO>> findAllDistrictByPidList(@RequestBody DistrictRegionReq req) {
        log.info("区行政查询,req:{}", JSONUtil.toJsonStr(req));
        List<DistrictResDTO> list = sysRegionService.findAllDistrictByPidList(req);
        return Result.success(list);
    }


    @ApiOperation(value = "镇行政查询")
    @GetMapping("/findAllTownByPid")
    @ApiImplicitParam(name = "pid", value = "地址父级ID", required = true, dataType = "Long")
    public Result<List<TownResDTO>> findAllTownByPid(@RequestParam Long pid) {
        log.info("镇行政查询,pid:{}", pid);
        List<TownResDTO> list = sysRegionService.findAllTownByPid(pid);
        return Result.success(list);
    }

    @ApiOperation(value = "镇行政查询(批量)")
    @PostMapping("/findAllTownByPidList")
    public Result<List<TownResDTO>> findAllTownByPidList(@RequestBody TownRegionReq req) {
        log.info("镇行政查询,req:{}", JSONUtil.toJsonStr(req));
        List<TownResDTO> list = sysRegionService.findAllTownByPidList(req);
        return Result.success(list);
    }


    @ApiOperation(value = "省市行政查询")
    @GetMapping("/findAllProvinceCitys")
    public Result<List<ProvinceResDTO>> findAllProvinceCitys() {
        log.info("省市行政查询");
        List<ProvinceResDTO> list = sysRegionService.findAllProvinceCitys();
        return Result.success(list);
    }

}
