package com.example.sppt.dto;

import lombok.Data;

/**
 * 登录成功后返回给前端的用户信息（不含密码）。
 * role 取该用户的最高身份：coreAdmin > normalAdmin > user。
 * @author sjy
 */
@Data
public class LoginVO {
    private Long id;
    private String phone;
    private String realName;
    private Long areaId;
    private String role;        // coreAdmin / normalAdmin / user
}
