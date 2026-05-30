package com.example.sppt.dto;

import lombok.Data;

/**
 * 登录请求参数
 * @author sjy
 */
@Data
public class LoginDTO {
    private String phone;       // 手机号
    private String password;    // 密码
}
