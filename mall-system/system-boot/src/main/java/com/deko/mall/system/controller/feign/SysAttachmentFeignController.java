package com.deko.mall.system.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.api.SysAttachmentFeignClient;
import com.deko.mall.system.model.query.SysAttachmentQuery;
import com.deko.mall.system.model.vo.SysAttachmentCategoryVO;
import com.deko.mall.system.model.vo.SysAttachmentPageVO;
import com.deko.mall.system.service.SysAttachmentCategoryService;
import com.deko.mall.system.service.SysAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 附件服务Feign控制器
 */
@RestController
@RequestMapping("/feign-api/v1/system/attachments")
@RequiredArgsConstructor
public class SysAttachmentFeignController implements SysAttachmentFeignClient {

    private final SysAttachmentCategoryService categoryService;
    private final SysAttachmentService attachmentService;

    @Override
    @GetMapping("/categories")
    public Result<List<SysAttachmentCategoryVO>> getCategoryTree() {
        List<SysAttachmentCategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Override
    @PostMapping("/list")
    public Result<SysAttachmentPageVO> getAttachmentList(@RequestBody SysAttachmentQuery query) {
        var page = attachmentService.getAttachmentList(query);
        SysAttachmentPageVO pageVO = new SysAttachmentPageVO();
        pageVO.setRecords(page.getRecords());
        pageVO.setTotal(page.getTotal());
        pageVO.setSize(page.getSize());
        pageVO.setCurrent(page.getCurrent());
        pageVO.setPages(page.getPages());
        return Result.success(pageVO);
    }
}
