package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.SysRolePerm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-权限关联 Mapper
 * @author sjy
 */
@Mapper
public interface SysRolePermMapper extends BaseMapper<SysRolePerm> {

    // 查询某角色已分配的权限ID
    @Select("SELECT perm_id FROM sys_role_perm WHERE role_id = #{roleId}")
    List<Long> selectPermIdsByRoleId(Long roleId);
}
