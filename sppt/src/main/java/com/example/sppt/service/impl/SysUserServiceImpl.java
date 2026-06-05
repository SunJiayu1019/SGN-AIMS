package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysRole;
import com.example.sppt.entity.SysUser;
import com.example.sppt.entity.SysUserRole;
import com.example.sppt.mapper.SysUserMapper;
import com.example.sppt.service.SysRoleService;
import com.example.sppt.service.SysUserRoleService;
import com.example.sppt.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired @Lazy
    private SysUserRoleService sysUserRoleService;
    @Autowired @Lazy
    private SysRoleService sysRoleService;

    @Override
    public List<SysUser> getAdminUsers() {
        return baseMapper.selectAdminUsers();
    }

    @Override
    public Map<String, Object> pageUsersWithRole(long current, long size, String keyword) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            w.and(q -> q.like(SysUser::getRealName, k).or().like(SysUser::getPhone, k));
        }
        w.orderByAsc(SysUser::getId);
        Page<SysUser> page = page(new Page<>(current, size), w);

        // 批量取每个用户的角色名，附加 role 字段（密码不返回）
        List<Map<String, Object>> records = new ArrayList<>();
        for (SysUser u : page.getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("phone", u.getPhone());
            m.put("realName", u.getRealName());
            m.put("areaId", u.getAreaId());
            List<String> roles = sysUserRoleService.getRoleNamesByUserId(u.getId());
            String role = roles.contains("coreAdmin") ? "coreAdmin"
                    : roles.contains("normalAdmin") ? "normalAdmin" : "user";
            m.put("role", role);
            records.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return result;
    }

    @Override
    public void setAdmin(Long userId, boolean admin) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 核心管理员不允许通过此入口改动
        if (sysUserRoleService.hasRole(userId, "coreAdmin")) {
            throw new IllegalArgumentException("核心管理员身份不可在此修改");
        }
        Long adminRoleId = roleIdByName("normalAdmin");
        Long userRoleId = roleIdByName("user");

        if (admin) {
            // 设为管理员：清掉 user 角色，绑定 normalAdmin（避免重复绑定）
            sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(adminRoleId);
            sysUserRoleService.save(ur);
        } else {
            // 取消管理员：清掉所有角色，回退为普通 user
            sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(userRoleId);
            sysUserRoleService.save(ur);
        }
    }

    private Long roleIdByName(String name) {
        SysRole r = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleName, name).last("LIMIT 1"));
        if (r == null) {
            throw new IllegalStateException("角色不存在：" + name + "（请先执行 auth_seed.sql 初始化角色）");
        }
        return r.getId();
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        // 新密码格式：数字+字母，不含符号（服务端兜底）
        com.example.sppt.util.ValidatorUtil.requireValidPassword(newPassword);
        SysUser user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (oldPassword == null || !oldPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        user.setPassword(newPassword);
        updateById(user);
    }

    @Override
    public SysUser updateProfile(Long userId, String realName, String phone, Long areaId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        SysUser user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 手机号唯一校验（排除自己）
        if (phone != null && !phone.trim().isEmpty()) {
            // 格式校验：11 位数字（服务端兜底）
            com.example.sppt.util.ValidatorUtil.requireValidPhone(phone);
            long exists = count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, phone.trim())
                    .ne(SysUser::getId, userId));
            if (exists > 0) {
                throw new IllegalArgumentException("该手机号已被其他用户使用");
            }
            user.setPhone(phone.trim());
        }
        if (realName != null) {
            user.setRealName(realName.trim());
        }
        if (areaId != null) {
            user.setAreaId(areaId);
        }
        updateById(user);
        return user;
    }
}
