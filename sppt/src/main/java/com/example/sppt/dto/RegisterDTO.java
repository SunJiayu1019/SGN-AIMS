package com.example.sppt.dto;

import lombok.Data;

/**
 * 注册请求参数
 * 说明：注册一律成为普通用户（user）；两次密码一致性由前端校验，
 *      后端只接收最终密码，并负责校验手机号是否已被注册。
 * @author sjy
 */
@Data
public class RegisterDTO {
    private String phone;       // 手机号
    private String password;    // 密码
    private String realName;    // 真实姓名
    private Long areaId;        // 所在区域ID
}
