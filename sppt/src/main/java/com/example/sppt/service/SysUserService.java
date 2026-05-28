package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysUser;
import java.util.List;

public interface SysUserService extends IService<SysUser> {
    List<SysUser> getAdminUsers();
}