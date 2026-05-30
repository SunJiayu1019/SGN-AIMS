package com.example.sppt.service;

import com.example.sppt.dto.LoginDTO;
import com.example.sppt.dto.LoginVO;
import com.example.sppt.dto.RegisterDTO;

/**
 * 登录 / 注册 Service（跨多张表：sys_user + sys_role + sys_user_role），
 * 故为普通 @Service，不绑定单一实体；只依赖其它实体 Service。
 * @author sjy
 */
public interface AuthService {

    // 注册：一律成为普通用户 user；手机号已存在时抛异常
    void register(RegisterDTO dto);

    // 登录：校验手机号 + 密码，成功返回用户信息（含最高角色），失败抛异常
    LoginVO login(LoginDTO dto);
}
