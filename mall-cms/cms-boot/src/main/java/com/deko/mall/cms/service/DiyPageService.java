package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.model.dto.DiyPageHomeDTO;
import com.deko.mall.cms.model.dto.DiyPagePublishDTO;
import com.deko.mall.cms.model.dto.DiyPageSaveDTO;
import com.deko.mall.cms.model.entity.DiyPage;
import com.deko.mall.cms.model.query.DiyPageQuery;
import com.deko.mall.cms.model.vo.DiyPageAppVO;
import com.deko.mall.cms.model.vo.DiyPageVO;

import java.util.Map;

public interface DiyPageService extends IService<DiyPage> {

    Long saveOrUpdatePage(DiyPageSaveDTO dto);

    void publish(DiyPagePublishDTO dto);

    void setHome(DiyPageHomeDTO dto);

    IPage<DiyPageVO> page(DiyPageQuery query);

    DiyPageVO getDetail(Long id);

    DiyPageVO getHome();

    void disable(Long id);

    void setEnabled(Long id, Boolean isEnabled);

    // App 端专用方法（返回已解析的数据结构）
    DiyPageAppVO getAppHome();

    DiyPageAppVO getAppHome(String clientType);

    DiyPageAppVO getAppDetail(Long id);

    void delete(Long id);

    Long copy(Long id);

    /**
     * 读取首页 DIY 配置（按端：diy_id + 定时区间）
     *
     * @return {weixin:{diy_id,time_start,time_end}, app:{...}, h5:{...}}
     */
    Map<String, Object> getHomeModeConfig();

    /**
     * 保存首页 DIY 配置（覆盖全量，写入 cms_diy_home_mode 表，每端一行 upsert）
     *
     * @param config 全量配置 {weixin:{diy_id,...}, app:{...}, h5:{...}}
     */
    void saveHomeModeConfig(Map<String, Object> config);
}

