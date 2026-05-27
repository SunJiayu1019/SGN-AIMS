package com.example.sppt.entity;

/**
 * 用户表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;            // 用户ID
    private String phone;       // 手机号
    private String password;    // 密码
    private String realName;    // 姓名
    private Long areaId;        // 所属区域ID
}