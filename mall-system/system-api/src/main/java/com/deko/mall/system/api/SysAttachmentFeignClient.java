package com.deko.mall.system.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.system.model.query.SysAttachmentQuery;
import com.deko.mall.system.model.vo.SysAttachmentCategoryVO;
import com.deko.mall.system.model.vo.SysAttachmentPageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 附件服务Feign客户端
 */
@FeignClient(value = "mall-system", contextId = "sysAttachment", configuration = {FeignDecoderConfig.class})
public interface SysAttachmentFeignClient {

    /**
     * 获取分类树
     *
     * @return 分类树列表
     */
    @GetMapping("/feign-api/v1/system/attachments/categories")
    Result<List<SysAttachmentCategoryVO>> getCategoryTree();

    /**
     * 获取附件列表（分页）
     *
     * @param params 查询参数
     * @return 附件列表
     */
    @PostMapping("/feign-api/v1/system/attachments/list")
    Result<SysAttachmentPageVO> getAttachmentList(@RequestBody SysAttachmentQuery params);
}
