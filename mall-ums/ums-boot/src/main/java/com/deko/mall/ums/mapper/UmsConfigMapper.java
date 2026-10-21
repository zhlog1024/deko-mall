package com.deko.mall.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.ums.model.entity.UmsConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 针对表【ums_asset】的数据库操作Mapper
 * @Entity generator.domain.UmsAsset
 */
@Mapper
public interface UmsConfigMapper extends BaseMapper<UmsConfig> {


    BigDecimal getConfigValue(@Param("model") String model, @Param("biz") String biz);

    List<UmsConfig> getConfigValueByModel(@Param("model") String mode);
}




