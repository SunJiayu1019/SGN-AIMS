package com.example.sppt.dto;

import lombok.Data;

/**
 * 审批请求参数
 * @author sjy
 */
@Data
public class AuditDTO {
    private Long applyId;       // 被审批的申请ID
    private Long auditUserId;   // 审批人（当前登录管理员）ID
    private String status;      // 审批结果 APPROVED / REJECTED
    private String remark;      // 审批意见（选填）
}
