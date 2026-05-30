package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysRolePerm;
import com.example.sppt.mapper.SysRolePermMapper;
import com.example.sppt.service.SysRolePermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysRolePermServiceImpl extends ServiceImpl<SysRolePermMapper, SysRolePerm>
        implements SysRolePermService {

    @Override
    public List<Long> getPermIdsByRoleId(Long roleId) {
        return baseMapper.selectPermIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPerms(Long roleId, List<Long> permIds) {
        if (roleId == null) {
            throw new IllegalArgumentException("缺少角色ID");
        }
        // 1. 清空该角色原有权限
        remove(new LambdaQueryWrapper<SysRolePerm>()
                .eq(SysRolePerm::getRoleId, roleId));

        // 2. 写入新选择的权限
        if (permIds == null || permIds.isEmpty()) {
            return;
        }
        List<SysRolePerm> rows = new ArrayList<>();
        for (Long permId : permIds) {
            if (permId == null) continue;
            SysRolePerm rp = new SysRolePerm();
            rp.setRoleId(roleId);
            rp.setPermId(permId);
            rows.add(rp);
        }
        saveBatch(rows);
    }
}
