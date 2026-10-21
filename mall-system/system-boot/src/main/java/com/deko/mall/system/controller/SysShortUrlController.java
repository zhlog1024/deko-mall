package com.deko.mall.system.controller;

import com.deko.mall.common.result.Result;
import com.deko.mall.system.service.ShortUrlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "短链接接口")
@RestController
@RequestMapping("/api/v1/shortUrl")
@RequiredArgsConstructor
public class SysShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping
    public Result<String> generateShortUrl(@RequestParam("url") String longUrl, @RequestParam("type") String type) throws IOException {
        return Result.success(shortUrlService.generateShortUrl(longUrl, type));
    }

}
