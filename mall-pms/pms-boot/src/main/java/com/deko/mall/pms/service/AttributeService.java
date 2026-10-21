package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.entity.PmsCategoryAttribute;
import com.deko.mall.pms.model.form.PmsCategoryAttributeForm;

public interface AttributeService extends IService<PmsCategoryAttribute> {

    /**
     * 批量保存商品属性
     *
     * @param formData 属性表单数据
     */
    boolean saveBatch(PmsCategoryAttributeForm formData);
}
