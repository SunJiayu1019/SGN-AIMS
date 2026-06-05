package com.example.sppt.util;

import java.util.regex.Pattern;

/**
 * 公共校验工具：手机号、密码。
 *
 * 规则（与前端 utils/validators.js 保持一致）：
 *   1) 手机号：必须是 11 位数字（纯数字，长度恰好 11）。
 *   2) 密码：必须同时包含数字和字母，且只能由数字+字母组成（不含符号/空格/中文）。
 *
 * 说明：前端校验可被绕过（直接调接口/改请求），所以服务端必须再校验一次。
 * @author sjy
 * @since 2026-06-01
 */
public final class ValidatorUtil {

    /** 11 位纯数字。 */
    private static final Pattern PHONE = Pattern.compile("^\\d{11}$");

    /** 只含字母和数字，且至少各有一个。 */
    private static final Pattern PASSWORD =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$");

    private ValidatorUtil() { }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }

    /** 手机号不合规则抛出 IllegalArgumentException。 */
    public static void requireValidPhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号必须是 11 位数字");
        }
    }

    /** 密码不合规则抛出 IllegalArgumentException。 */
    public static void requireValidPassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("密码必须由数字和字母组成（需同时包含数字和字母，不能含符号）");
        }
    }
}
