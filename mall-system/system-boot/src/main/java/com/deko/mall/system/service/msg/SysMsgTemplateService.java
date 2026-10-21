package com.deko.mall.system.service.msg;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.system.model.entity.SysMsgTemplate;
import com.deko.mall.system.model.form.SysMsgTemplateForm;
import com.deko.mall.system.model.vo.SysMsgTemplatePageVO;

import java.util.Map;

/**
 * 消息模板服务接口
 */
public interface SysMsgTemplateService {

    /**
     * 获取模板
     *
     * @param templateCode 模板编码
     * @return 消息模板
     */
    SysMsgTemplate getTemplate(String templateCode);

    /**
     * 渲染模板内容
     *
     * @param template 模板
     * @param params   参数
     * @return 渲染后的内容
     */
    String renderTemplate(SysMsgTemplate template, Map<String, Object> params);

    /**
     * 校验模板参数
     *
     * @param template 模板
     * @param params   参数
     * @return true-校验通过
     */
    boolean validateTemplateParams(SysMsgTemplate template, Map<String, Object> params);

    /**
     * 分页查询
     *
     * @param page    分页对象
     * @param queryVO 查询参数
     * @return 分页结果
     */
    Page<SysMsgTemplate> listTemplatesPage(Page<SysMsgTemplate> page, SysMsgTemplatePageVO queryVO);

    /**
     * 创建模板
     *
     * @param form 模板表单
     * @return 模板ID
     */
    Long createTemplate(SysMsgTemplateForm form);

    /**
     * 修改模板
     *
     * @param form 模板表单
     * @return 是否成功
     */
    boolean updateTemplate(SysMsgTemplateForm form);

    /**
     * 删除模板
     *
     * @param ids 模板ID列表
     * @return 是否成功
     */
    boolean deleteTemplates(Long[] ids);
}
