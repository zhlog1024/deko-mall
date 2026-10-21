package com.deko.mall.sms.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.sms.model.vo.BannerVO;
import com.deko.mall.sms.service.AdvertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "App-营销广告")
@RestController
@RequestMapping("/app-api/v1/adverts")
@Slf4j
@AllArgsConstructor
public class AdvertController {

    private AdvertService advertService;

    @Operation(summary = "APP首页广告横幅列表")
    @GetMapping("/banners")
    public Result<List<BannerVO>> getBannerList() {
        List<BannerVO> list = advertService.getBannerList();
        return Result.success(list);
    }
}
