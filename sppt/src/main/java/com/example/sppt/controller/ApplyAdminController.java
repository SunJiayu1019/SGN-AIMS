package com.example.sppt.controller;

import com.example.sppt.dto.AuditDTO;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.service.ApplyAuditService;
import com.example.sppt.service.ApplyFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端 - 申请审批接口（/apply/admin/**）
 * 统一后：依赖 Service（审批编排走 ApplyAuditService），构造器注入，统一返回 Result。
 * @author sjy
 * @since 2026-05-27
 */
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyAdminController {

    private final ApplyAuditService applyAuditService;
    private final ApplyFormService applyFormService;

    // 全部申请（兜底/调试用，不做责任过滤）
    @GetMapping("/admin/list")
    public Result<List<ApplyForm>> list() {
        return Result.success(applyFormService.list());
    }

    // 当前管理员负责的"待审批"申请
    @GetMapping("/admin/pending")
    public Result<List<ApplyForm>> pending(@RequestParam Long adminId) {
        return Result.success(applyAuditService.listPending(adminId));
    }

    // 当前管理员负责的"已审批"申请（已通过 / 已驳回）
    @GetMapping("/admin/handled")
    public Result<List<ApplyForm>> handled(@RequestParam Long adminId) {
        return Result.success(applyAuditService.listHandled(adminId));
    }

    // 审批：改状态 + 记录意见 +（通过时）门牌入库
    @PostMapping("/admin/audit")
    public Result<String> audit(@RequestBody AuditDTO dto) {
        try {
            applyAuditService.audit(dto);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 审批进度：当前级别 / 总级别 / 各级审批记录
    @GetMapping("/admin/progress")
    public Result<java.util.Map<String, Object>> progress(@RequestParam Long applyId) {
        return Result.success(applyAuditService.progress(applyId));
    }
}
