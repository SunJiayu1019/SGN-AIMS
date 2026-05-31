package com.example.sppt.entity;

/**
 * 系统操作日志
 * 记录管理端每一次关键操作（登录 / 新增 / 修改 / 删除 / 审批等）。
 * @author sjy
 * @since 2026-05-30
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;               // 日志ID
    private Long operatorId;       // 操作人ID（可空）
    private String operator;       // 操作人名称（冗余存储，便于展示）
    private String action;         // 操作类型 登录/新增/修改/删除/审批
    private String target;         // 操作对象 如 apply_form / house_info
    private String detail;         // 变更说明
    private String ip;             // 操作来源IP
    private LocalDateTime createTime; // 创建时间
}
