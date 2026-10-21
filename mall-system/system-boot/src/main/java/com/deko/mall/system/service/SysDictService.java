package com.deko.mall.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.system.model.entity.SysDict;
import com.deko.mall.system.model.form.DictForm;
import com.deko.mall.system.model.query.DictPageQuery;
import com.deko.mall.system.model.vo.DictPageVO;

import java.util.List;

/**
 * 字典接口
 */
public interface SysDictService extends IService<SysDict> {
    /**
     * 字典数据项分页列表
     */
    Page<DictPageVO> getDictPage(DictPageQuery queryParams);

    /**
     * 字典数据项表单
     *
     * @param id 字典数据项ID
     */
    DictForm getDictForm(Long id);

    /**
     * 新增字典数据项
     *
     * @param dictForm 字典数据项表单
     */
    boolean saveDict(DictForm dictForm);

    /**
     * 修改字典数据项
     *
     * @param id       字典数据项ID
     * @param dictForm 字典数据项表单
     */
    boolean updateDict(Long id, DictForm dictForm);

    /**
     * 删除字典数据项
     *
     * @param idsStr 字典数据项ID，多个以英文逗号(,)分割
     */
    boolean deleteDict(String idsStr);

    /**
     * 获取字典下拉列表
     */
    List<Option> listDictOptions(String typeCode);

}
