package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.entity.SysHelp;
import com.example.sppt.service.SysHelpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帮助信息接口（/api/help/**）
 * 统一约定：只依赖 Service，构造器注入，统一返回 Result。
 * @author sjy
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class SysHelpController {

    private final SysHelpService sysHelpService;

    // 帮助条目列表（按 sort 排序）
    @GetMapping("/list")
    public Result<List<SysHelp>> list() {
        return Result.success(sysHelpService.listAllSorted());
    }

    // 单条详情
    @GetMapping("/{id}")
    public Result<SysHelp> detail(@PathVariable Long id) {
        return Result.success(sysHelpService.getById(id));
    }
}
