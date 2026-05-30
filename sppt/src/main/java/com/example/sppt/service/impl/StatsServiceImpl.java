package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.entity.SysArea;
import com.example.sppt.service.ApplyFormService;
import com.example.sppt.service.HouseService;
import com.example.sppt.service.StatsService;
import com.example.sppt.service.SysAreaService;
import com.example.sppt.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    // 只依赖其它 Service，不直接依赖 Mapper
    private final ApplyFormService applyFormService;
    private final HouseService houseService;
    private final SysAreaService sysAreaService;
    private final SysUserService sysUserService;

    // 房屋类型编码 -> 中文
    private String houseTypeName(String code) {
        if (code == null || code.isEmpty()) return "未分类";
        return switch (code) {
            case "house" -> "住宅";
            case "shop" -> "商铺";
            case "factory" -> "厂房";
            default -> code; // 兼容历史脏数据，原样展示
        };
    }

    // 申请状态编码 -> 中文
    private String applyStatusName(String code) {
        if (code == null || code.isEmpty()) return "未知";
        return switch (code) {
            case "PENDING" -> "待审批";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            default -> code;
        };
    }

    @Override
    public Map<String, Object> overview() {
        long pending = applyFormService.count(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getStatus, "PENDING"));
        long approved = applyFormService.count(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getStatus, "APPROVED"));
        long rejected = applyFormService.count(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getStatus, "REJECTED"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("applyTotal", applyFormService.count());
        map.put("applyPending", pending);
        map.put("applyApproved", approved);
        map.put("applyRejected", rejected);
        map.put("houseTotal", houseService.count());
        map.put("areaTotal", sysAreaService.count());
        map.put("userTotal", sysUserService.count());
        return map;
    }

    @Override
    public List<Map<String, Object>> houseCountByType() {
        List<HouseInfo> houses = houseService.list();

        Map<String, Long> grouped = houses.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getHouseType() == null ? "" : h.getHouseType(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<Map<String, Object>> list = new ArrayList<>();
        grouped.forEach((type, count) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("type", type);
            row.put("typeName", houseTypeName(type));
            row.put("count", count);
            list.add(row);
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> houseCountByArea() {
        List<HouseInfo> houses = houseService.list();

        Map<Long, String> areaNameMap = sysAreaService.list().stream()
                .collect(Collectors.toMap(SysArea::getId, SysArea::getName, (a, b) -> a));

        Map<Long, Long> grouped = houses.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getAreaId() == null ? 0L : h.getAreaId(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<Map<String, Object>> list = new ArrayList<>();
        grouped.forEach((areaId, count) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("areaId", areaId);
            row.put("areaName", areaNameMap.getOrDefault(areaId, "未知区域(" + areaId + ")"));
            row.put("count", count);
            list.add(row);
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> applyCountByStatus() {
        List<ApplyForm> applyList = applyFormService.list();

        Map<String, Long> grouped = applyList.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatus() == null ? "" : a.getStatus(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<Map<String, Object>> list = new ArrayList<>();
        grouped.forEach((status, count) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("status", status);
            row.put("statusName", applyStatusName(status));
            row.put("count", count);
            list.add(row);
        });
        return list;
    }
}
