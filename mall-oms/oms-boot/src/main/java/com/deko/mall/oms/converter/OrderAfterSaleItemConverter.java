package com.deko.mall.oms.converter;

import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSaleItem;
import com.deko.mall.oms.model.form.OrderAfterSaleItemForm;
import com.deko.mall.oms.model.vo.OmsOrderAfterSaleItemVO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 售后对象转换器
 */
@Mapper(componentModel = "spring", imports = {StringUtils.class, Arrays.class, Collections.class, IBaseEnum.class})
public interface OrderAfterSaleItemConverter {
    /**
     * 申请表单转实体
     */
    OmsOrderAfterSaleItem form2Entity(OrderAfterSaleItemForm form);

    OmsOrderAfterSaleItemVO entity2VO(OmsOrderAfterSaleItem entity);

    List<OmsOrderAfterSaleItemVO> entity2ListVO(List<OmsOrderAfterSaleItem> entity);
}
