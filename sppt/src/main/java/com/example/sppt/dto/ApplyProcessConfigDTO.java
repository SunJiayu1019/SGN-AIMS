package com.example.sppt.dto;

import lombok.Data;

import java.util.List;

/**
 * 审批流程配置请求参数（按"申请类型"配置）。
 * 统一后改为以 applyType 为维度：new=门牌申请、reissue=门牌补发。
 * @author sjy
 * @since 2026-05-28
 */
@Data
public class ApplyProcessConfigDTO {
    // 申请类型 new / reissue
    private String applyType;
    // 审批级数
    private Integer nodeLevel;
    // 各层级审核人ID列表（外层=级别，内层=该级别的多个审核人ID）
    private List<List<String>> auditUserIdsList;
    // 操作人ID（用于"仅核心管理员可配置"的后端鉴权）
    private Long operatorId;
    // 区域ID（本配置为全局，默认 0 总站）
    private Integer areaId;
}
