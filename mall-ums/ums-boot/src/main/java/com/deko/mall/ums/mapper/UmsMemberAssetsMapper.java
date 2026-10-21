package com.deko.mall.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.ums.model.domain.UmsMemberAssets;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【ums_asset】的数据库操作Mapper
 * @Entity generator.domain.UmsAsset
 */
@Mapper
public interface UmsMemberAssetsMapper extends BaseMapper<UmsMemberAssets> {

    UmsMemberAssets selectByMemberAndCode(@Param("memberId") Long memberId, @Param("assetCode") String code);

    List<Long> getNeedInitUser();
}




