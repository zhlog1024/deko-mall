package com.deko.mall.ums.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.ums.model.domain.UmsMemberAssetsFlow;
import com.deko.mall.ums.model.vo.UmsMemberAssetsFlowVO;
import org.mapstruct.Mapper;

/**
 * 会员对象转换器
 */
@Mapper(componentModel = "spring")
public interface MemberAssetsFlowConvert {

    Page<UmsMemberAssetsFlowVO> toVoPage(Page<UmsMemberAssetsFlow> assetsFlow);

    UmsMemberAssetsFlowVO entity2Vo(UmsMemberAssetsFlow assetsFlow);

}
