package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 * 唯一的“查询管理员”入口（多表联查，统一用 @Select 注解 SQL）。
 * @author sjy
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // 三表联查 -> 只返回管理员用户（coreAdmin / normalAdmin）
    @Select("SELECT DISTINCT u.* "
            + "FROM sys_user u "
            + "INNER JOIN sys_user_role ur ON u.id = ur.user_id "
            + "INNER JOIN sys_role r ON ur.role_id = r.id "
            + "WHERE r.role_name IN ('coreAdmin', 'normalAdmin')")
    List<SysUser> selectAdminUsers();
}
