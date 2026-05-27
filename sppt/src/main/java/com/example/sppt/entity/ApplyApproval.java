package com.example.sppt.entity;

/**
 * 审批记录表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("apply_approval")
public class ApplyApproval {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private Long applyId;       // 申请表ID
    private Long auditUserId;   // 审核人ID
    private Integer nodeLevel;  // 审批节点级数
    private String result;      // 审批结果 APPROVE/REJECT
    private String remark;      // 审批备注
    private LocalDateTime createTime; // 创建时间
}