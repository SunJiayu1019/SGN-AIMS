package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.entity.SysArea;
import com.example.sppt.service.HouseService;
import com.example.sppt.service.SysAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GIS 专题地图接口（/api/gis/**）
 *
 * 设计：house_info 表已有 lng/lat/geometry 字段，本接口把门牌按行政区划过滤后，
 *      解析出经纬度返回给前端 Leaflet 渲染为点位；并提供「各区域门牌数量」用于
 *      生成专题图（按区域着色/聚合）。
 * @author sjy
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/api/gis")
@RequiredArgsConstructor
public class GisController {

    private final HouseService houseService;
    private final SysAreaService sysAreaService;

    /**
     * 门牌点位：可按 areaId 过滤（不传或 0 表示全部）。
     * 返回每个点的 经纬度 / 地址 / 类型 / 区域，前端据此打点。
     */
    @GetMapping("/house-points")
    public Result<List<Map<String, Object>>> housePoints(@RequestParam(required = false) Long areaId) {
        LambdaQueryWrapper<HouseInfo> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null && areaId > 0) {
            wrapper.eq(HouseInfo::getAreaId, areaId);
        }
        List<HouseInfo> houses = houseService.list(wrapper);

        // 区域 id -> 名称
        Map<Long, String> areaName = new LinkedHashMap<>();
        for (SysArea a : sysAreaService.list()) {
            areaName.put(a.getId(), a.getName());
        }

        List<Map<String, Object>> points = new ArrayList<>();
        for (HouseInfo h : houses) {
            double[] ll = resolveLngLat(h);
            if (ll == null) continue;   // 无有效坐标的跳过
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", h.getId());
            p.put("houseCode", h.getHouseCode());
            p.put("address", h.getAddress());
            p.put("houseType", h.getHouseType());
            p.put("areaId", h.getAreaId());
            p.put("areaName", areaName.getOrDefault(h.getAreaId(), "未知区域"));
            p.put("lng", ll[0]);
            p.put("lat", ll[1]);
            points.add(p);
        }
        return Result.success(points);
    }

    /**
     * 专题统计：各区域门牌数量（用于专题图按区域着色 / 图例）。
     */
    @GetMapping("/area-summary")
    public Result<List<Map<String, Object>>> areaSummary() {
        Map<Long, String> areaName = new LinkedHashMap<>();
        for (SysArea a : sysAreaService.list()) {
            areaName.put(a.getId(), a.getName());
        }
        Map<Long, Integer> counter = new LinkedHashMap<>();
        for (HouseInfo h : houseService.list()) {
            Long aid = h.getAreaId() == null ? 0L : h.getAreaId();
            counter.merge(aid, 1, Integer::sum);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        counter.forEach((aid, cnt) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("areaId", aid);
            row.put("areaName", areaName.getOrDefault(aid, "未知区域(" + aid + ")"));
            row.put("count", cnt);
            list.add(row);
        });
        return Result.success(list);
    }

    /**
     * 解析门牌的经纬度：优先取 lng/lat 字段；否则尝试解析 geometry "POINT(lng lat)"。
     * @return [lng, lat]，无法解析返回 null
     */
    private double[] resolveLngLat(HouseInfo h) {
        try {
            if (h.getLng() != null && !h.getLng().isBlank()
                    && h.getLat() != null && !h.getLat().isBlank()) {
                return new double[]{Double.parseDouble(h.getLng().trim()),
                                    Double.parseDouble(h.getLat().trim())};
            }
            String g = h.getGeometry();
            if (g != null && g.toUpperCase().contains("POINT")) {
                String inner = g.substring(g.indexOf('(') + 1, g.indexOf(')')).trim();
                String[] parts = inner.split("\\s+");
                if (parts.length >= 2) {
                    return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
                }
            }
        } catch (Exception ignore) {
            // 坐标脏数据：跳过
        }
        return null;
    }
}
