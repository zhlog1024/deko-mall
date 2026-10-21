package com.deko.mall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.system.model.entity.SysDept;
import com.deko.mall.system.model.form.DeptForm;
import com.deko.mall.system.model.query.DeptQuery;
import com.deko.mall.system.model.vo.DeptVO;

import java.util.List;

/**
 * 部门业务接口
 */
public interface SysDeptService extends IService<SysDept> {
    /**
     * 部门列表
     */
    List<DeptVO> listDepartments(DeptQuery queryParams);

    /**
     * 部门树形下拉选项
     */
    List<Option> listDeptOptions();

    /**
     * 新增部门
     */
    Long saveDept(DeptForm formData);

    /**
     * 修改部门
     */
    Long updateDept(Long deptId, DeptForm formData);

    /**
     * 删除部门
     *
     * @param ids 部门ID，多个以英文逗号,拼接字符串
     */
    boolean deleteByIds(String ids);

    /**
     * 获取部门详情
     */
    DeptForm getDeptForm(Long deptId);
}
