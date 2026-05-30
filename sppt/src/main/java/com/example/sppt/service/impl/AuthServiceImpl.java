package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.dto.LoginDTO;
import com.example.sppt.dto.LoginVO;
import com.example.sppt.dto.RegisterDTO;
import com.example.sppt.entity.SysRole;
import com.example.sppt.entity.SysUser;
import com.example.sppt.service.AuthService;
import com.example.sppt.service.SysRoleService;
import com.example.sppt.service.SysUserRoleService;
import com.example.sppt.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 登录 / 注册业务实现。
 * 统一约定：跨 Service 依赖走构造器注入；本类不直接依赖 Mapper；
 *          方法返回领域数据 / 抛异常，Result 由 Controller 包装。
 * @author sjy
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;

    private static final String ROLE_USER = "user";

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        // 1. 基本校验
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 2. 手机号唯一性校验（库表未建唯一约束，这里在业务层兜底）
        long exists = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, dto.getPhone().trim()));
        if (exists > 0) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        // 3. 写入用户表（注册一律为普通用户 user）
        SysUser user = new SysUser();
        user.setPhone(dto.getPhone().trim());
        user.setPassword(dto.getPassword());
        user.setRealName(dto.getRealName());
        user.setAreaId(dto.getAreaId() == null ? 0L : dto.getAreaId());
        sysUserService.save(user);

        // 4. 绑定 user 角色（角色不存在则自动创建）
        SysRole userRole = sysRoleService.getOrCreateByName(ROLE_USER);
        sysUserRoleService.bindRole(user.getId(), userRole.getId());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        if (dto.getPhone() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("请输入手机号和密码");
        }

        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, dto.getPhone().trim())
                .eq(SysUser::getPassword, dto.getPassword())
                .last("LIMIT 1"));
        if (user == null) {
            throw new IllegalArgumentException("手机号或密码错误");
        }

        // 取该用户的最高角色：coreAdmin > normalAdmin > user
        List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(user.getId());
        String role = pickHighestRole(roleNames);

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setAreaId(user.getAreaId());
        vo.setRole(role);
        return vo;
    }

    private String pickHighestRole(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return ROLE_USER;
        }
        if (roleNames.contains("coreAdmin")) {
            return "coreAdmin";
        }
        if (roleNames.contains("normalAdmin")) {
            return "normalAdmin";
        }
        return ROLE_USER;
    }
}
