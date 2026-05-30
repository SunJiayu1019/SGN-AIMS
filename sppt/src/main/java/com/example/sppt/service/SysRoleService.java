package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysRole;

/**
 * 角色 Service
 * @author sjy
 */
public interface SysRoleService extends IService<SysRole> {

    // 按角色名获取角色（不存在则自动创建后返回）——用于注册时保证 'user' 角色存在
    SysRole getOrCreateByName(String roleName);
}
