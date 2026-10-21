package com.deko.mall.ums.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 资产编码枚举（当前仅积分）
 */
@Schema(description = "资产编码枚举")
public enum AssetCodeEnum {

    /**
     * 积分
     */
    POINT("POINT", "积分");

    @Schema(description = "资产编码")
    private final String code;

    @Schema(description = "资产描述")
    private final String description;

    AssetCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 资产编码
     * @return 枚举实例
     */
    public static AssetCodeEnum of(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (AssetCodeEnum assetCode : values()) {
            if (assetCode.getCode().equals(code)) {
                return assetCode;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
