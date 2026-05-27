package com.example.sppt.entity;

/**
 * 用户角色关联表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private Long userId;        // 用户ID
    private Long roleId;        // 角色ID
}