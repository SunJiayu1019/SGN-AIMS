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
public class ApplyController {

    @Autowired
    private ApplyFormService applyFormService;

    // ==========================
    // 用户端接口（提交申请）
    // ==========================
    @PostMapping("/user/submit")
    public String submit(@RequestBody ApplyForm applyForm) {
        applyForm.setStatus("PENDING");
        applyForm.setCreateTime(LocalDateTime.now());
        applyFormService.save(applyForm);
        return "提交成功";
    }

    // 用户查看自己的申请
    @GetMapping("/user/myList")
    public List<ApplyForm> myList(@RequestParam Long userId) {
        return applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getUserId, userId));
    }

    // ==========================
    // 管理员接口（审批）
    // ==========================
    @GetMapping("/admin/list")
    public List<ApplyForm> list() {
        return applyFormService.list();
    }

    // 审核通过/驳回
    @PostMapping("/admin/audit")
    public String audit(@RequestParam Long id, @RequestParam String status) {
        ApplyForm form = applyFormService.getById(id);
        form.setStatus(status);
        applyFormService.updateById(form);
        return "审核成功";
    }
}