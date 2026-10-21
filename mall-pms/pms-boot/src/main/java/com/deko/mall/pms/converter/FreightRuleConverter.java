package com.deko.mall.pms.converter;

import com.deko.mall.pms.model.entity.PmsFreightRule;
import com.deko.mall.pms.model.vo.FreightRuleVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 商品对象转换器
 */
@Mapper(componentModel = "spring")
public interface FreightRuleConverter {

    List<FreightRuleVO> entities2VOs(List<PmsFreightRule> entities);

}
