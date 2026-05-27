package com.example.sppt.entity;

/**
 * 审批流程节点表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("apply_process_node")
public class ApplyProcessNode {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private Long areaId;        // 区域ID 0总站
    private Integer nodeLevel;  // 审批节点级数 1~5级
    private String auditUserIds; // 审核人ID,多个
    private String auditType;   // 审核类型 ONE通过/ALL通过
}