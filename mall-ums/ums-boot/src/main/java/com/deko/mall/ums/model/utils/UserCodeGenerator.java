package com.deko.mall.ums.model.utils;

import java.security.SecureRandom;

public class UserCodeGenerator {

    // 定义字符池（排除0）
    private static final String FIRST_CHAR = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String OTHER_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom random = new SecureRandom();

    public static String generateUserCode() {
        StringBuilder code = new StringBuilder(9);

        // 生成第一个字符（非0开头）
        code.append(FIRST_CHAR.charAt(random.nextInt(FIRST_CHAR.length())));

        // 生成剩余8位
        for (int i = 1; i < 9; i++) {
            code.append(OTHER_CHARS.charAt(random.nextInt(OTHER_CHARS.length())));
        }

        return code.toString();
    }
}
