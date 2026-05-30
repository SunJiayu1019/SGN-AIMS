package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysRole;
import com.example.sppt.mapper.SysRoleMapper;
import com.example.sppt.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    @Override
    public SysRole getOrCreateByName(String roleName) {
        SysRole role = getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleName, roleName)
                .last("LIMIT 1"));
        if (role != null) {
            return role;
        }
        // 不存在则创建（主要用于首次注册时自动补 'user' 角色）
        role = new SysRole();
        role.setRoleName(roleName);
        save(role);
        return role;
    }
}
