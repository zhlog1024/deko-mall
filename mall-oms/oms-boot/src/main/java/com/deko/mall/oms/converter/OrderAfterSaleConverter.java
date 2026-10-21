package com.deko.mall.oms.converter;

import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.oms.enums.AfterSaleStatusEnum;
import com.deko.mall.oms.enums.AfterSaleTypeEnum;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSale;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSaleItem;
import com.deko.mall.oms.model.form.OrderAfterSaleForm;
import com.deko.mall.oms.model.form.OrderAfterSaleItemForm;
import com.deko.mall.oms.model.vo.AfterSalePageVO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 售后对象转换器
 */
@Mapper(componentModel = "spring", imports = {StringUtils.class, Arrays.class, Collections.class, IBaseEnum.class, AfterSaleTypeEnum.class, AfterSaleStatusEnum.class})
public interface OrderAfterSaleConverter {
    /**
     * 申请表单转实体
     */
    OmsOrderAfterSale form2Entity(OrderAfterSaleForm form);

    /**
     * 实体转分页视图对象
     */
    @Mapping(target = "afterSaleTypeLabel", expression = "java(IBaseEnum.getLabelByValue(entity.getAfterSaleType(), AfterSaleTypeEnum.class))")
    @Mapping(target = "statusLabel", expression = "java(IBaseEnum.getLabelByValue(entity.getStatus(), AfterSaleStatusEnum.class))")
    @Mapping(target = "items", ignore = true)
    AfterSalePageVO entity2PageVO(OmsOrderAfterSale entity);

    /**
     * 实体转详情视图对象
     */
    @Mapping(target = "afterSaleTypeLabel", expression = "java(IBaseEnum.getLabelByValue(entity.getAfterSaleType(), AfterSaleTypeEnum.class))")
    @Mapping(target = "statusLabel", expression = "java(IBaseEnum.getLabelByValue(entity.getStatus(), AfterSaleStatusEnum.class))")
    @Mapping(target = "items", ignore = true)
    AfterSalePageVO entity2DetailVO(OmsOrderAfterSale entity);

    /**
     * 售后商品表单转实体
     */
    OmsOrderAfterSaleItem itemForm2Entity(OrderAfterSaleItemForm form);

    /**
     * 批量转换售后商品表单为实体
     */
    List<OmsOrderAfterSaleItem> itemForms2Entities(List<OrderAfterSaleItemForm> forms);
}
