package com.example.sppt.mapper;

/**
 * @author sjy
 * @since 2026-05-28
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.ApplyProcessNode;
import com.example.sppt.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplyProcessNodeMapper extends BaseMapper<ApplyProcessNode> {

    // 注解版 SQL
    @Select("SELECT * FROM apply_process_node " +
            "WHERE area_id = #{areaId} AND node_level = #{nodeLevel}")
    List<ApplyProcessNode> selectByAreaAndApplyType(
            @Param("areaId") Integer areaId,
            @Param("nodeLevel") Integer nodeLevel
    );

    // 查询所有管理员
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE r.role_name = '管理员'")
    List<SysUser> selectAdminUsers();
}