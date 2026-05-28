package com.example.sppt.controller;

/**
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.service.ApplyFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/apply")
public class ApplyAdminController {

    @Autowired
    private ApplyFormService applyFormService;
    // ==========================
    // 管理员接口（审批）
    // ==========================
    @GetMapping("/admin/list")
    public List<ApplyForm> list() {
        return applyFormService.list();
    }

    // 审批接口（改状态）
    @PutMapping("/admin/audit/{id}/{status}")
    public boolean audit(
            @PathVariable Long id,
            @PathVariable String status) {
        return applyFormService.audit(id, status, "");
    }
}