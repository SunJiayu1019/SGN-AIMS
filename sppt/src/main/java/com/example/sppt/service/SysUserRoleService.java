package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysUserRole;

import java.util.List;

/**
 * 用户-角色关联 Service
 * @author sjy
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    // 给用户绑定角色
    void bindRole(Long userId, Long roleId);

    // 查询某用户的所有角色名
    List<String> getRoleNamesByUserId(Long userId);

    // 判断某用户是否拥有指定角色名
    boolean hasRole(Long userId, String roleName);
}
