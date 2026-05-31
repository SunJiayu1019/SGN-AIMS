package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.entity.SysBannedWord;
import com.example.sppt.service.SysBannedWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 禁用词条管理接口（/api/banned/**）
 * 供「审批网站管理」页维护禁用词；发布内容时由 NewsService 调用 findHit 校验。
 * @author sjy
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/api/banned")
@RequiredArgsConstructor
public class SysBannedWordController {

    private final SysBannedWordService sysBannedWordService;

    // 列表
    @GetMapping("/list")
    public Result<List<SysBannedWord>> list() {
        return Result.success(sysBannedWordService.listAll());
    }

    // 新增禁用词
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysBannedWord body) {
        if (body.getWord() == null || body.getWord().trim().isEmpty()) {
            return Result.fail("禁用词不能为空");
        }
        body.setWord(body.getWord().trim());
        long exists = sysBannedWordService.lambdaQuery()
                .eq(SysBannedWord::getWord, body.getWord()).count();
        if (exists > 0) {
            return Result.fail("该禁用词已存在");
        }
        body.setCreateTime(LocalDateTime.now());
        sysBannedWordService.save(body);
        return Result.success("添加成功");
    }

    // 删除禁用词
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        sysBannedWordService.removeById(id);
        return Result.success("删除成功");
    }
}
