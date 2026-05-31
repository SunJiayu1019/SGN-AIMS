package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.SysLog;
import com.example.sppt.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统操作日志接口（/api/log/**）
 * 统一约定：只依赖 Service，构造器注入，统一返回 Result。
 * @author sjy
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    /**
     * 分页查询日志（管理端日志页使用）。
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param action   操作类型（可空）
     * @param keyword  关键字，匹配操作人/说明（可空）
     */
    @GetMapping("/page")
    public Result<IPage<SysLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword) {
        return Result.success(sysLogService.page(pageNum, pageSize, action, keyword));
    }

    /**
     * 主动记录一条日志（供前端在关键操作后调用）。
     * 入参示例：{ "operatorId":1, "operator":"核心管理员", "action":"审批",
     *            "target":"apply_form", "detail":"通过申请单 AP202605300001" }
     */
    @PostMapping("/record")
    public Result<String> record(@RequestBody Map<String, Object> body) {
        Long operatorId = body.get("operatorId") == null
                ? null : Long.valueOf(body.get("operatorId").toString());
        String operator = (String) body.get("operator");
        String action   = (String) body.get("action");
        String target   = (String) body.get("target");
        String detail   = (String) body.get("detail");
        sysLogService.record(operatorId, operator, action, target, detail);
        return Result.success("已记录");
    }
}
