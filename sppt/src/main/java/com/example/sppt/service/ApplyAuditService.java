package com.example.sppt.service;

import com.example.sppt.dto.AuditDTO;
import com.example.sppt.entity.ApplyForm;

import java.util.List;

/**
 * 门牌申请审批 Service（跨多张表：apply_form + apply_process_node + apply_approval + house_info），
 * 故为普通 @Service，不绑定单一实体；只依赖其它实体 Service。
 *
 * 业务约定：
 *   - "谁该审批"由审批流程配置决定：管理员在某申请类型的审核人名单里，就能看到该类型的申请；
 *     核心管理员 coreAdmin 拥有最高权限，可看到并审批全部申请；
 *   - 审批会修改 apply_form 状态、写入 apply_approval 审批记录（含意见）；
 *   - 审批"通过"的申请，会把门牌信息写入 house_info 门牌表。
 * @author sjy
 */
public interface ApplyAuditService {

    // 当前管理员负责的"待审批"申请
    List<ApplyForm> listPending(Long adminId);

    // 当前管理员负责的"已审批"（已通过 / 已驳回）申请
    List<ApplyForm> listHandled(Long adminId);

    // 审批某条申请：改状态 + 记录审批意见 +（通过时）门牌入库；失败抛异常以回滚
    void audit(AuditDTO dto);

    // 查询某申请的审批进度（当前级别 / 总级别 / 各级记录），用于前端展示多级流转
    java.util.Map<String, Object> progress(Long applyId);
}
