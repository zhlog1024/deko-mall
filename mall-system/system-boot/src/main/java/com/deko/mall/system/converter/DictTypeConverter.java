package com.deko.mall.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.system.model.entity.SysDictType;
import com.deko.mall.system.model.form.DictTypeForm;
import com.deko.mall.system.model.vo.DictTypePageVO;
import org.mapstruct.Mapper;

/**
 * 字典类型对象转换器
 */
@Mapper(componentModel = "spring")
public interface DictTypeConverter {

    Page<DictTypePageVO> entity2Page(Page<SysDictType> page);

    DictTypeForm entity2Form(SysDictType entity);

    SysDictType form2Entity(DictTypeForm entity);
}
