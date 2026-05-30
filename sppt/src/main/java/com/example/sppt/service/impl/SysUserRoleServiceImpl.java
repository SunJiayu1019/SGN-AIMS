package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysUserRole;
import com.example.sppt.mapper.SysUserRoleMapper;
import com.example.sppt.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements SysUserRoleService {

    @Override
    public void bindRole(Long userId, Long roleId) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        save(ur);
    }

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        return baseMapper.selectRoleNamesByUserId(userId);
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        List<String> names = getRoleNamesByUserId(userId);
        return names != null && names.contains(roleName);
    }
}
