package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.dto.RolePermDTO;
import com.example.sppt.entity.SysPermission;
import com.example.sppt.entity.SysRole;
import com.example.sppt.service.SysPermissionService;
import com.example.sppt.service.SysRolePermService;
import com.example.sppt.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色权限管理接口（/api/rbac/**）
 * 读取角色 / 权限，并为角色分配权限。
 * 统一约定：只依赖 Service，构造器注入，统一返回 Result。
 * @author sjy
 * @since 2026-05-29
 */
@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;
    private final SysRolePermService sysRolePermService;

    // 全部角色
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.success(sysRoleService.list());
    }

    // 全部权限
    @GetMapping("/permissions")
    public Result<List<SysPermission>> permissions() {
        return Result.success(sysPermissionService.list());
    }

    // 某角色已分配的权限ID
    @GetMapping("/role/{roleId}/perms")
    public Result<List<Long>> rolePerms(@PathVariable Long roleId) {
        return Result.success(sysRolePermService.getPermIdsByRoleId(roleId));
    }

    // 为角色分配权限
    @PostMapping("/role/perms")
    public Result<String> assign(@RequestBody RolePermDTO dto) {
        try {
            sysRolePermService.assignPerms(dto.getRoleId(), dto.getPermIds());
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
