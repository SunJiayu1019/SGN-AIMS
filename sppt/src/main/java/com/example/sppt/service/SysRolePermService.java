package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysRolePerm;

import java.util.List;

/**
 * 角色-权限关联 Service
 * @author sjy
 */
public interface SysRolePermService extends IService<SysRolePerm> {

    // 查询某角色已分配的权限ID
    List<Long> getPermIdsByRoleId(Long roleId);

    // 重新分配某角色的权限（先清空再写入）
    void assignPerms(Long roleId, List<Long> permIds);
}
