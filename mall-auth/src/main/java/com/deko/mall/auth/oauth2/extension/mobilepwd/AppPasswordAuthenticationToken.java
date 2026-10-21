package com.deko.mall.auth.oauth2.extension.mobilepwd;

import jakarta.annotation.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * app手机号及密码身份验证令牌
 */
public class AppPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 授权类型(app手机号及密码: app_password)
     */
    public static final AuthorizationGrantType APP_PASSWORD = new AuthorizationGrantType("app_password");
    /**
     * 令牌申请访问范围
     */
    private final Set<String> scopes;


    protected AppPasswordAuthenticationToken(
            Authentication clientPrincipal,
            Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters
    ) {
        super(AppPasswordAuthenticationToken.APP_PASSWORD, clientPrincipal, additionalParameters);
        this.scopes = Collections.unmodifiableSet(scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }


    /**
     * 用户凭证(微信小程序 Code)
     */
    @Override
    public Object getCredentials() {
        return this.getAdditionalParameters().get(OAuth2ParameterNames.CODE);
    }

    public Set<String> getScopes() {
        return scopes;
    }

}
