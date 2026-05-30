package com.example.sppt.entity;

/**
 * 审批流程节点表
 * 说明：新增 applyType 字段，使审批流程可按"申请类型"分别配置
 *      （new=门牌申请、reissue=门牌补发）。需配套执行 upgrade_process_node.sql。
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
    private Integer id;            // 主键ID
    private Integer areaId;        // 区域ID 0总站（本配置为全局，固定用 0）
    private String applyType;      // 申请类型 new/reissue（新增）
    private Integer nodeLevel;     // 审批节点级数 1~5级
    private String auditUserIds;   // 审核人ID,多个（逗号分隔）
    private String auditType;      // 审核类型 ONE通过/ALL通过
}
