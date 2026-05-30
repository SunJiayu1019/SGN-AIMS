package com.example.sppt.controller;

import java.util.List;
import com.example.sppt.entity.SysArea;
import com.example.sppt.service.SysAreaService;
import com.example.sppt.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行政区划接口
 * 统一后：构造器注入统一用 Lombok @RequiredArgsConstructor，统一返回 Result。
 * @author sjy
 * @since 2026-05-28
 */
@RestController
@RequestMapping("/api/sys/area")
@RequiredArgsConstructor
public class SysAreaController {

    private final SysAreaService sysAreaService;

    @GetMapping("/list")
    public Result<List<SysArea>> list() {
        return Result.success(sysAreaService.list());
    }
}
