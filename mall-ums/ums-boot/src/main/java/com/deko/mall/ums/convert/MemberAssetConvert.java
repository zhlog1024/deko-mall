package com.deko.mall.ums.convert;

import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.dto.MemberAssetsVO;
import com.deko.mall.ums.model.domain.UmsMemberAssets;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 会员对象转换器
 */
@Mapper(componentModel = "spring")
public interface MemberAssetConvert {

    MemberAssetsDTO dto2Entity(UmsMemberAssets assets);

    List<MemberAssetsDTO> dto2EntityList(List<UmsMemberAssets> assetsList);

    MemberAssetsVO dto2Vo(MemberAssetsDTO memberAsset);

    List<MemberAssetsVO> dtoList2VoList(List<MemberAssetsDTO> memberAssets);
}
