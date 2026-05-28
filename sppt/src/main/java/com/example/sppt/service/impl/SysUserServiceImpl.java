package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysUser;
import com.example.sppt.mapper.SysUserMapper;
import com.example.sppt.service.SysUserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public List<SysUser> getAdminUsers() {
        return baseMapper.selectAdminUsers();
    }
}