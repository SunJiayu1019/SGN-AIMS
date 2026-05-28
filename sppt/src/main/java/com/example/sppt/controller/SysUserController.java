package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.entity.SysUser;
import com.example.sppt.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/admin")
    public Result<List<SysUser>> admin() {
        return Result.success(sysUserService.getAdminUsers());
    }
}