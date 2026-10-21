package com.deko.mall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Apple Universal Links 配置文件路由
 * 确保 iOS 系统能通过 HTTPS 访问到 apple-app-site-association 文件
 */
@Configuration
public class AppleAppSiteAssociationConfig {

    @Bean
    public RouterFunction<ServerResponse> appleAppSiteAssociationRoute() {
        return route(GET("/.well-known/apple-app-site-association"), request -> {
            ClassPathResource resource = new ClassPathResource("static/.well-known/apple-app-site-association");
            if (!resource.exists()) {
                return ServerResponse.notFound().build();
            }
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(resource);
        });
    }

    /**
     * Universal Links 兜底页路由
     * 匹配 /ulink 自身及 /ulink/share 等业务路径，返回 ulink.html
     * 不拦截 /ulink/*.txt 等静态文件，留给 Spring 默认 handler 处理
     */
    @Bean
    public RouterFunction<ServerResponse> ulinkFallbackRoute() {
        return route(
            GET("/ulink/**").and(request -> !request.path().matches(".*\\.(txt|json|png|jpg|ico|html)$")),
            this::serveUlinkPage
        );
    }

    private Mono<ServerResponse> serveUlinkPage(ServerRequest request) {
        ClassPathResource resource = new ClassPathResource("static/ulink.html");
        return resource.exists()
            ? ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(resource)
            : ServerResponse.notFound().build();
    }
}
