package com.deko.mall.cms.controller.app;

import com.deko.mall.cms.model.vo.AgreementVO;
import com.deko.mall.cms.service.AgreementService;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 用户端协议接口，支持直接返回 HTML。
 */
@Tag(name = "App-协议接口")
@RestController
@RequestMapping("/app-api/v1/agreement")
@RequiredArgsConstructor
public class AgreementAppController {

    private final AgreementService agreementService;

    @Operation(summary = "按 document 获取启用协议")
    @GetMapping("/{document}")
    public Result<AgreementVO> getAgreement(
            @Parameter(description = "协议标识(userregister/userprivacy/...)") @PathVariable String document) {
        return Result.success(agreementService.getEnabledByDocument(document));
    }

    @Operation(summary = "协议 HTML 直出(webview 兼容)")
    @GetMapping(value = "/{document}/html", produces = "text/html;charset=UTF-8")
    public void getAgreementHtml(
            @Parameter(description = "协议标识") @PathVariable String document,
            HttpServletResponse response) throws IOException {
        AgreementVO agreement = agreementService.getEnabledByDocument(document);
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            out.println("<title>" + escapeHtml(agreement.getTitle()) + "</title>");
            out.println("<style>body{font-size:15px;line-height:1.7;color:#333;padding:16px;margin:0;"
                    + "word-break:break-word;}img{max-width:100%;height:auto;}</style></head><body>");
            out.println(agreement.getContent() == null ? "" : agreement.getContent());
            out.println("</body></html>");
        }
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
