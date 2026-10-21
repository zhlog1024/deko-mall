package com.deko.mall.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.system.model.entity.SysAppVersion;
import com.deko.mall.system.model.form.AppVersionForm;
import com.deko.mall.system.model.vo.AppVersionVO;
import org.mapstruct.Mapper;

/**
 * APP版本对象转换器
 */
@Mapper(componentModel = "spring")
public interface SysAppVersionConverter {

    Page<AppVersionVO> entity2Page(Page<SysAppVersion> page);

    AppVersionVO entity2VO(SysAppVersion entity);

    SysAppVersion form2Entity(AppVersionForm form);
}
