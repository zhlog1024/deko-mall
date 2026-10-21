package com.deko.mall.pay.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.model.form.PayConfigForm;
import com.deko.mall.pay.model.vo.PayConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * 支付配置对象转换器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PayConfigConverter {

    /**
     * 实体转视图对象
     *
     * @param entity 支付配置实体
     * @return 支付配置视图对象
     */
    PayConfigVO entity2Vo(PayConfig entity);

    /**
     * 表单转实体
     *
     * @param form 支付配置表单
     * @return 支付配置实体
     */
    PayConfig form2Entity(PayConfigForm form);

    /**
     * 实体转表单
     *
     * @param entity 支付配置实体
     * @return 支付配置表单
     */
    PayConfigForm entity2Form(PayConfig entity);

    /**
     * 分页实体转分页视图对象
     *
     * @param payConfigPage 支付配置分页
     * @return 支付配置分页视图对象
     */
    Page<PayConfigVO> toVoPage(Page<PayConfig> payConfigPage);
}
