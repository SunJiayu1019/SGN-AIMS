package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysPermission;
import com.example.sppt.mapper.SysPermissionMapper;
import com.example.sppt.service.SysPermissionService;
import org.springframework.stereotype.Service;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
        implements SysPermissionService {
}
