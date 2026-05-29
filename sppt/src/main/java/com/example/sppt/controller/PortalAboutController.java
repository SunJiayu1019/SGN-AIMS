package com.example.sppt.controller;

import com.example.sppt.entity.PortalAbout;
import com.example.sppt.service.PortalAboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/about")
@RequiredArgsConstructor
public class PortalAboutController {

    private final PortalAboutService portalAboutService;

    @GetMapping("/list")
    public List<PortalAbout> list() {
        return portalAboutService.list();
    }

    @GetMapping("/{id}")
    public PortalAbout getById(@PathVariable Long id) {
        return portalAboutService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody PortalAbout portalAbout) {
        return portalAboutService.saveOrUpdate(portalAbout);
    }

    @DeleteMapping("/{id}")
    public boolean remove(@PathVariable Long id) {
        return portalAboutService.removeById(id);
    }

    // 关键：按区域查询（给前端用）
    @GetMapping("/area/{areaId}")
    public PortalAbout getByArea(@PathVariable String areaId) {
        return portalAboutService.getByAreaId(areaId);
    }
}