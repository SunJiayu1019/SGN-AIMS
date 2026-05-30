package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计分析接口（管理系统首页默认内容）
 * 统一后：聚合逻辑全部下沉到 StatsService，本类只做转发 + Result 包装；
 *        不再直接依赖任何 Mapper，构造器注入。
 * @author sjy
 * @since 2026-05-29
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    // 顶部概览数字卡片
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.overview());
    }

    // 门牌类别统计
    @GetMapping("/house-by-type")
    public Result<List<Map<String, Object>>> houseByType() {
        return Result.success(statsService.houseCountByType());
    }

    // 门牌区域统计
    @GetMapping("/house-by-area")
    public Result<List<Map<String, Object>>> houseByArea() {
        return Result.success(statsService.houseCountByArea());
    }

    // 申请状态统计
    @GetMapping("/apply-by-status")
    public Result<List<Map<String, Object>>> applyByStatus() {
        return Result.success(statsService.applyCountByStatus());
    }
}
