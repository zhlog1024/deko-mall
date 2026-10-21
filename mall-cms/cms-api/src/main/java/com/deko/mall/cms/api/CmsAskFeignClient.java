package com.deko.mall.cms.api;

import com.deko.mall.cms.api.dto.AskByIdsQuery;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 商品问答服务 Feign 客户端（供装修器选问答等消费）
 */
@FeignClient(value = "mall-cms", contextId = "cmsAsk")
public interface CmsAskFeignClient {

    /**
     * 问答分页（装修器"自动数据/筛选"模式拉问答列表）
     *
     * @param query 分页查询
     * @return 问答数据分页
     */
    @PostMapping("/feign-api/v1/ask/page")
    PageResult<AskItemDTO> page(@RequestBody AskFeignPageQuery query);

    /**
     * 指定问答数据（装修器"指定问答"模式：按 ids 回显）
     *
     * @param query 问答ID列表
     * @return 问答数据项
     */
    @PostMapping("/feign-api/v1/ask/by-ids")
    Result<List<AskItemDTO>> listByIds(@RequestBody AskByIdsQuery query);
}
