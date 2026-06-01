package com.example.sppt.controller;

import com.example.sppt.dto.LoginVO;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.SysUser;
import com.example.sppt.service.SysUserService;
import com.example.sppt.service.SysUserRoleService;
import com.example.sppt.service.SysLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户接口（/api/user/**）
 * 统一后：构造器注入，统一返回 Result。
 * @author sjy
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final SysLogService sysLogService;

    @GetMapping("/admin")
    public Result<List<SysUser>> admin() {
        return Result.success(sysUserService.getAdminUsers());
    }

    /**
     * 用户管理 - 分页查询所有注册用户（含角色名），支持按姓名/手机号关键字过滤。
     * 返回：{ records: [...], total, current, size }，每条记录附带 role 字段。
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(sysUserService.pageUsersWithRole(current, size, keyword));
    }

    /**
     * 用户管理 - 将指定用户设置为管理员（normalAdmin）或取消管理员。
     * body: { userId, admin: true/false }
     */
    @PostMapping("/set-admin")
    public Result<String> setAdmin(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            boolean admin = Boolean.parseBoolean(String.valueOf(body.get("admin")));
            sysUserService.setAdmin(userId, admin);
            sysLogService.record(userId, null, "修改", "sys_user_role",
                    admin ? "将用户(" + userId + ")设置为管理员" : "取消用户(" + userId + ")的管理员身份");
            return Result.success(admin ? "已设为管理员" : "已取消管理员");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 获取个人信息（不含密码）
    @GetMapping("/profile")
    public Result<SysUser> profile(@RequestParam Long userId) {
        SysUser u = sysUserService.getById(userId);
        if (u != null) u.setPassword(null);
        return Result.success(u);
    }

    // 修改密码
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String oldPwd = (String) body.get("oldPassword");
            String newPwd = (String) body.get("newPassword");
            sysUserService.changePassword(userId, oldPwd, newPwd);
            sysLogService.record(userId, null, "修改", "sys_user", "修改了登录密码");
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 编辑个人信息（返回新的 LoginVO 供前端刷新本地登录态）
    @PostMapping("/update-profile")
    public Result<LoginVO> updateProfile(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String realName = (String) body.get("realName");
            String phone = (String) body.get("phone");
            Long areaId = body.get("areaId") == null ? null : Long.valueOf(body.get("areaId").toString());

            SysUser u = sysUserService.updateProfile(userId, realName, phone, areaId);
            sysLogService.record(userId, u.getRealName(), "修改", "sys_user", "编辑了个人信息");

            // 组装最新登录态返回
            List<String> roles = sysUserRoleService.getRoleNamesByUserId(userId);
            String role = roles.contains("coreAdmin") ? "coreAdmin"
                    : roles.contains("normalAdmin") ? "normalAdmin" : "user";
            LoginVO vo = new LoginVO();
            vo.setId(u.getId());
            vo.setPhone(u.getPhone());
            vo.setRealName(u.getRealName());
            vo.setAreaId(u.getAreaId());
            vo.setRole(role);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
