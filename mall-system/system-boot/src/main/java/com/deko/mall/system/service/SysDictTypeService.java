package com.deko.mall.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.system.model.entity.SysDictType;
import com.deko.mall.system.model.form.DictTypeForm;
import com.deko.mall.system.model.query.DictTypePageQuery;
import com.deko.mall.system.model.vo.DictTypePageVO;

import java.util.List;

/**
 * 数据字典类型业务接口
 */
public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 字典类型分页列表
     *
     * @param queryParams 分页查询对象
     */
    Page<DictTypePageVO> getDictTypePage(DictTypePageQuery queryParams);


    /**
     * 获取字典类型表单详情
     *
     * @param id 字典类型ID
     */
    DictTypeForm getDictTypeForm(Long id);


    /**
     * 新增字典类型
     *
     * @param dictTypeForm 字典类型表单
     */
    boolean saveDictType(DictTypeForm dictTypeForm);


    /**
     * 修改字典类型
     *
     * @param dictTypeForm 字典类型表单
     */
    boolean updateDictType(Long id, DictTypeForm dictTypeForm);

    /**
     * 删除字典类型
     *
     * @param idsStr 字典类型ID，多个以英文逗号(,)分割
     */
    boolean deleteDictTypes(String idsStr);


    /**
     * 获取字典类型的数据项
     */
    List<Option> listDictItemsByTypeCode(String typeCode);
}
