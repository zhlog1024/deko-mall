package com.deko.mall.system.service.msg.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.system.config.SysResultCode;
import com.deko.mall.system.mapper.SysMsgTemplateMapper;
import com.deko.mall.system.model.entity.SysMsgTemplate;
import com.deko.mall.system.model.form.SysMsgTemplateForm;
import com.deko.mall.system.model.vo.SysMsgTemplatePageVO;
import com.deko.mall.system.service.msg.SysMsgTemplateService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息模板服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMsgTemplateServiceImpl extends ServiceImpl<SysMsgTemplateMapper, SysMsgTemplate> implements SysMsgTemplateService {

    private final SysMsgTemplateMapper templateMapper;
    private final Configuration fmConfig;

    @Override
    public SysMsgTemplate getTemplate(String templateCode) {
        LambdaQueryWrapper<SysMsgTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMsgTemplate::getTemplateCode, templateCode)
                .eq(SysMsgTemplate::getStatus, 1);
        SysMsgTemplate template = templateMapper.selectOne(wrapper);
        if (template == null) {
            throw new BizException(SysResultCode.MSG_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    @Override
    public String renderTemplate(SysMsgTemplate template, Map<String, Object> params) {
        try {
            Template fm = new Template(template.getTemplateCode(),
                    new StringReader(template.getTemplateContent()), fmConfig);
            StringWriter out = new StringWriter();
            fm.process(params, out);
            return out.toString();
        } catch (TemplateException e) {
            log.error("模板渲染失败: templateCode={}, error={}", template.getTemplateCode(), e.getMessage());
            throw new BizException(SysResultCode.MSG_TEMPLATE_RENDER_FAILED);
        } catch (Exception e) {
            log.error("模板渲染异常: templateCode={}", template.getTemplateCode(), e);
            throw new BizException(SysResultCode.MSG_TEMPLATE_RENDER_FAILED);
        }
    }

    @Override
    public boolean validateTemplateParams(SysMsgTemplate template, Map<String, Object> params) {
        if (template.getTemplateParams() == null || params == null) {
            return false;
        }

        JSONArray paramArray = JSONUtil.parseArray(template.getTemplateParams());
        Set<String> requiredParams = paramArray.stream()
                .map(obj -> JSONUtil.parseObj(obj).getStr("name"))
                .collect(Collectors.toSet());

        return params.keySet().containsAll(requiredParams);
    }

    @Override
    public Page<SysMsgTemplate> listTemplatesPage(Page<SysMsgTemplate> page, SysMsgTemplatePageVO queryVO) {
        LambdaQueryWrapper<SysMsgTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryVO.getTemplateCode()), SysMsgTemplate::getTemplateCode, queryVO.getTemplateCode())
                .like(StrUtil.isNotBlank(queryVO.getTemplateName()), SysMsgTemplate::getTemplateName, queryVO.getTemplateName())
                .eq(StrUtil.isNotBlank(queryVO.getChannelCode()), SysMsgTemplate::getChannelCode, queryVO.getChannelCode())
                .eq(queryVO.getStatus() != null, SysMsgTemplate::getStatus, queryVO.getStatus())
                .orderByDesc(SysMsgTemplate::getCreateTime);
        return templateMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(SysMsgTemplateForm form) {
        // 校验模板编码唯一性
        checkTemplateCodeUnique(form.getTemplateCode(), null);

        SysMsgTemplate template = new SysMsgTemplate();
        BeanUtil.copyProperties(form, template);
        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "msg:template", key = "#form.templateCode")
    public boolean updateTemplate(SysMsgTemplateForm form) {
        // 校验模板编码唯一性
        checkTemplateCodeUnique(form.getTemplateCode(), form.getId());

        SysMsgTemplate template = templateMapper.selectById(form.getId());
        if (template == null) {
            throw new BizException(SysResultCode.MSG_TEMPLATE_NOT_FOUND);
        }

        BeanUtil.copyProperties(form, template);
        return templateMapper.updateById(template) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "msg:template", allEntries = true)
    public boolean deleteTemplates(Long[] ids) {
        return templateMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private void checkTemplateCodeUnique(String templateCode, Long id) {
        LambdaQueryWrapper<SysMsgTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMsgTemplate::getTemplateCode, templateCode)
                .ne(id != null, SysMsgTemplate::getId, id);
        if (templateMapper.selectCount(wrapper) > 0) {
            throw new BizException(SysResultCode.MSG_TEMPLATE_CODE_EXISTS);
        }
    }
}
