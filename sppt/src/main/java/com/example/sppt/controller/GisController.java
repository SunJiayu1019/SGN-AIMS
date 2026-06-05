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
     * 中心缓冲查询（圆形缓冲区）。
     *
     * 思路：给定一个中心点(lng/lat)和半径 radius(米)，
     *      返回落在「以中心为圆心、radius 为半径的圆」内的所有门牌。
     *      门牌坐标存的是经纬度文本，用 Haversine 公式算球面距离，
     *      距离 <= radius 即命中。无需 GIS 扩展，纯 Java 计算。
     *
     * @param lng    中心点经度
     * @param lat    中心点纬度
     * @param radius 缓冲半径（米）
     * @param areaId 可选：限定区域
     */
    @GetMapping("/buffer/center")
    public Result<List<Map<String, Object>>> centerBuffer(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam double radius,
            @RequestParam(required = false) Long areaId) {

        LambdaQueryWrapper<HouseInfo> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null && areaId > 0) {
            wrapper.eq(HouseInfo::getAreaId, areaId);
        }
        List<HouseInfo> houses = houseService.list(wrapper);

        Map<Long, String> areaName = buildAreaNameMap();
        List<Map<String, Object>> hit = new ArrayList<>();
        for (HouseInfo h : houses) {
            double[] ll = resolveLngLat(h);
            if (ll == null) continue;
            double dist = haversine(lat, lng, ll[1], ll[0]); // 注意 haversine 入参是(纬,经)
            if (dist <= radius) {
                Map<String, Object> p = toPoint(h, ll, areaName);
                p.put("distance", Math.round(dist * 100) / 100.0); // 距中心多少米
                hit.add(p);
            }
        }
        // 按距离从近到远排序，方便前端展示
        hit.sort((a, b) -> Double.compare(
                ((Number) a.get("distance")).doubleValue(),
                ((Number) b.get("distance")).doubleValue()));
        return Result.success(hit);
    }

    /**
     * 矩形缓冲查询（矩形/包围盒缓冲区）。
     *
     * 思路：给定矩形对角的两个经纬度(minLng,minLat) 与 (maxLng,maxLat)，
     *      返回落在该矩形范围内的所有门牌（minLng<=lng<=maxLng 且 minLat<=lat<=maxLat）。
     *      为容错，内部对 min/max 做一次规范化（谁大谁当 max）。
     */
    @GetMapping("/buffer/rect")
    public Result<List<Map<String, Object>>> rectBuffer(
            @RequestParam double minLng,
            @RequestParam double minLat,
            @RequestParam double maxLng,
            @RequestParam double maxLat,
            @RequestParam(required = false) Long areaId) {

        double west  = Math.min(minLng, maxLng);
        double east  = Math.max(minLng, maxLng);
        double south = Math.min(minLat, maxLat);
        double north = Math.max(minLat, maxLat);

        LambdaQueryWrapper<HouseInfo> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null && areaId > 0) {
            wrapper.eq(HouseInfo::getAreaId, areaId);
        }
        List<HouseInfo> houses = houseService.list(wrapper);

        Map<Long, String> areaName = buildAreaNameMap();
        List<Map<String, Object>> hit = new ArrayList<>();
        for (HouseInfo h : houses) {
            double[] ll = resolveLngLat(h);
            if (ll == null) continue;
            double x = ll[0], y = ll[1];
            if (x >= west && x <= east && y >= south && y <= north) {
                hit.add(toPoint(h, ll, areaName));
            }
        }
        return Result.success(hit);
    }

    /** 区域 id -> 名称 映射。 */
    private Map<Long, String> buildAreaNameMap() {
        Map<Long, String> areaName = new LinkedHashMap<>();
        for (SysArea a : sysAreaService.list()) {
            areaName.put(a.getId(), a.getName());
        }
        return areaName;
    }

    /** 把门牌 + 已解析坐标 组装成前端点位 Map。 */
    private Map<String, Object> toPoint(HouseInfo h, double[] ll, Map<Long, String> areaName) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", h.getId());
        p.put("houseCode", h.getHouseCode());
        p.put("address", h.getAddress());
        p.put("houseType", h.getHouseType());
        p.put("areaId", h.getAreaId());
        p.put("areaName", areaName.getOrDefault(h.getAreaId(), "未知区域"));
        p.put("lng", ll[0]);
        p.put("lat", ll[1]);
        return p;
    }

    /**
     * Haversine 球面距离（米）。入参均为十进制度，顺序：(纬度1,经度1,纬度2,经度2)。
     */
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000.0; // 地球平均半径，米
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
