package com.deko.mall.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.system.model.entity.SysAppVersion;
import com.deko.mall.system.model.form.AppVersionForm;
import com.deko.mall.system.model.query.AppVersionPageQuery;
import com.deko.mall.system.model.dto.AppVersionCheckDTO;
import com.deko.mall.system.model.vo.AppVersionVO;

/**
 * APP版本管理业务接口
 */
public interface SysAppVersionService extends IService<SysAppVersion> {

    /**
     * APP版本分页列表
     */
    Page<AppVersionVO> getPage(AppVersionPageQuery queryParams);

    /**
     * APP版本详情
     */
    AppVersionVO getDetail(Long id);

    /**
     * 新增APP版本
     */
    boolean add(AppVersionForm form);

    /**
     * 修改APP版本
     */
    boolean update(Long id, AppVersionForm form);

    /**
     * 删除APP版本
     */
    boolean delete(String idsStr);

    /**
     * 检测APP版本更新
     */
    AppVersionCheckDTO checkUpdate(String appVersion, String platform);
}
