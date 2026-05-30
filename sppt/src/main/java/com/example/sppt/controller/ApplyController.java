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
 * 用户端申请接口（/apply/user/**）
 * 说明：与 ApplyUserController（/user/apply/**）功能重复，建议后续二选一保留；
 *      本次仅统一技术（走 Service、返回 Result），未删除以免影响已联调的页面。
 * @author sjy
 * @since 2026-05-27
 */
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyFormService applyFormService;

    // 用户端：提交申请
    @PostMapping("/user/submit")
    public Result<String> submit(@RequestBody ApplyForm applyForm) {
        applyForm.setStatus("PENDING");
        applyForm.setCreateTime(LocalDateTime.now());
        applyFormService.save(applyForm);
        return Result.success("提交成功");
    }

    // 用户查看自己的申请
    @GetMapping("/user/myList")
    public Result<List<ApplyForm>> myList(@RequestParam Long userId) {
        return Result.success(applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getUserId, userId)));
    }
}
