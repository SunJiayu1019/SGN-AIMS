package com.example.sppt.controller;

import com.example.sppt.dto.LoginDTO;
import com.example.sppt.dto.LoginVO;
import com.example.sppt.dto.RegisterDTO;
import com.example.sppt.dto.Result;
import com.example.sppt.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录 / 注册接口（/api/auth/**）
 * 统一约定：只依赖 Service，构造器注入，统一返回 Result。
 * @author sjy
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 注册（一律成为普通用户 user）
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        try {
            authService.register(dto);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 登录
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        try {
            return Result.success(authService.login(dto));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
