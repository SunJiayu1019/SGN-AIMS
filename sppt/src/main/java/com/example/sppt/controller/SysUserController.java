package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.entity.SysUser;
import com.example.sppt.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户接口
 * 统一后：构造器注入统一用 Lombok @RequiredArgsConstructor，统一返回 Result。
 * @author sjy
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping("/admin")
    public Result<List<SysUser>> admin() {
        return Result.success(sysUserService.getAdminUsers());
    }
}
