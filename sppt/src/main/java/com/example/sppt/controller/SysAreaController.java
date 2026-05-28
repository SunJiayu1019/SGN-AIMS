package com.example.sppt.controller;

/**
 * @author sjy
 * @since 2026-05-28
 */
import java.util.List;
import com.example.sppt.entity.SysArea;
import com.example.sppt.service.SysAreaService;
import com.example.sppt.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/area")
public class SysAreaController {

    private final SysAreaService sysAreaService;

    public SysAreaController(SysAreaService sysAreaService) {
        this.sysAreaService = sysAreaService;
    }

    @GetMapping("/list") // 接口路径
    public Result<List<SysArea>> list() {
        return Result.success(sysAreaService.list());
    }
}