package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-角色关联 Mapper
 * @author sjy
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    // 查询某个用户拥有的全部角色名（用于登录后判定身份）
    @Select("SELECT r.role_name "
            + "FROM sys_user_role ur "
            + "INNER JOIN sys_role r ON ur.role_id = r.id "
            + "WHERE ur.user_id = #{userId}")
    List<String> selectRoleNamesByUserId(Long userId);
}
