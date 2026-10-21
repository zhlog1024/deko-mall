package com.deko.mall.pms.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.dto.SupplierDTO;
import com.deko.mall.pms.model.entity.PmsSupplier;
import com.deko.mall.pms.model.form.SupplierForm;
import com.deko.mall.pms.model.vo.SupplierVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * 供应商数据转换器
 */
@Mapper(componentModel = "spring")
public interface SupplierConverter {


    @Mappings({
            @Mapping(target = "auditStatusLabel", expression = "java(com.deko.mall.common.base.IBaseEnum.getLabelByValue(entity.getAuditStatus(), com.deko.mall.pms.enums.SupplierAuditStatusEnum.class))")
    })
    SupplierVO entity2VO(PmsSupplier entity);

    SupplierDTO entity2DTO(PmsSupplier entity);


    Page<SupplierVO> entity2Page(Page<PmsSupplier> page);

    PmsSupplier form2Entity(SupplierForm form);


}
