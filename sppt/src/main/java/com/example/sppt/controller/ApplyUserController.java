package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.service.ApplyFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端申请接口（/user/apply/**）
 * 统一后：不再直接注入 Mapper，改为依赖 ApplyFormService，并返回 Result。
 * @author sjy
 */
@RestController
@RequestMapping("/user/apply")
@RequiredArgsConstructor
public class ApplyUserController {

    private final ApplyFormService applyFormService;

    // 提交申请
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody ApplyForm form) {
        form.setCreateTime(LocalDateTime.now());
        form.setStatus("PENDING");
        applyFormService.save(form);
        return Result.success("提交成功");
    }

    // 我的申请（按 用户 + 申请类型）
    @GetMapping("/myList")
    public Result<List<ApplyForm>> myList(@RequestParam Long userId,
                                          @RequestParam String applyType) {
        return Result.success(applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getUserId, userId)
                .eq(ApplyForm::getApplyType, applyType)
                .orderByDesc(ApplyForm::getCreateTime)));
    }

    // 我的申请（仅按用户，不区分类型）—— 供“我的申请”列表页使用
    @GetMapping("/list")
    public Result<List<ApplyForm>> list(@RequestParam Long userId) {
        return Result.success(applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getUserId, userId)
                .orderByDesc(ApplyForm::getCreateTime)));
    }
}
