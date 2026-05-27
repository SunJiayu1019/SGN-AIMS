package com.example.sppt.entity;

/**
 * 角色权限关联表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role_perm")
public class SysRolePerm {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private Long roleId;        // 角色ID
    private Long permId;        // 权限ID
}