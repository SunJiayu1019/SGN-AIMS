package com.example.sppt.controller;

import com.example.sppt.dto.ApplyProcessConfigDTO;
import com.example.sppt.dto.Result;
import com.example.sppt.service.ApplyProcessConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审批流程配置接口（按申请类型）。
 * 统一后：只依赖 Service，构造器注入，统一返回 Result；
 *        "仅核心管理员可配置"的鉴权在 Service 内完成（依据 operatorId）。
 * @author sjy
 * @since 2026-05-28
 */
@RestController
@RequestMapping("/api/process/config")
@RequiredArgsConstructor
public class ApplyProcessConfigController {

    private final ApplyProcessConfigService applyProcessConfigService;

    // 按申请类型查询流程配置（new=门牌申请 / reissue=门牌补发）
    @GetMapping("/type/{applyType}")
    public Result<?> getByType(@PathVariable String applyType) {
        return Result.success(applyProcessConfigService.getProcessConfigByType(applyType));
    }

    // 按申请类型保存流程配置（仅核心管理员）
    @PostMapping("/save-by-type")
    public Result<?> saveByType(@RequestBody ApplyProcessConfigDTO dto) {
        try {
            applyProcessConfigService.saveProcessConfigByType(dto);
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
