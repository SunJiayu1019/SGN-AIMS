package com.example.sppt.entity;

/**
 * 权限表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_permission")
public class SysPermission {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private String permName;    // 权限名称
    private String permCode;    // 权限标识 如 house:add
    private Long parentId;      // 父级权限ID
    private String type;        // 权限类型 menu/button
}