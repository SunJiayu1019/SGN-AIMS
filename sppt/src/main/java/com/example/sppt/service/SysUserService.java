package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysUser;
import java.util.List;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {
    List<SysUser> getAdminUsers();

    /**
     * 分页查询所有注册用户（含角色名 role：coreAdmin/normalAdmin/user），
     * keyword 可按姓名或手机号模糊匹配。
     * @return { records, total, current, size }
     */
    Map<String, Object> pageUsersWithRole(long current, long size, String keyword);

    /**
     * 将用户设置为管理员(normalAdmin)或取消管理员。
     * 核心管理员(coreAdmin)不允许被改动。
     */
    void setAdmin(Long userId, boolean admin);

    /**
     * 修改密码：校验原密码后更新。失败抛异常。
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 编辑个人信息：更新姓名 / 手机号 / 所属区域（手机号唯一校验）。
     * 返回更新后的用户（不含密码处理由调用方决定）。
     */
    SysUser updateProfile(Long userId, String realName, String phone, Long areaId);
}
