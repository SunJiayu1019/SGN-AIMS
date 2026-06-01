package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.mapper.HouseInfoMapper;
import com.example.sppt.service.HouseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseServiceImpl extends ServiceImpl<HouseInfoMapper, HouseInfo>
        implements HouseService {

    @Override
    public List<HouseInfo> listByCondition(Long areaId, String houseType, String keyword) {
        LambdaQueryWrapper<HouseInfo> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null && areaId > 0) {
            wrapper.eq(HouseInfo::getAreaId, areaId);
        }
        if (houseType != null && !houseType.isEmpty()) {
            wrapper.eq(HouseInfo::getHouseType, houseType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(HouseInfo::getAddress, keyword)
                    .or().like(HouseInfo::getHouseCode, keyword));
        }
        wrapper.orderByDesc(HouseInfo::getId);
        return list(wrapper);
    }

    @Override
    public List<HouseInfo> listByArea(Long areaId) {
        return list(new LambdaQueryWrapper<HouseInfo>()
                .eq(HouseInfo::getAreaId, areaId));
    }

    @Override
    public int nextHouseNumber(Long streetAreaId) {
        if (streetAreaId == null) {
            throw new IllegalArgumentException("街道ID不能为空");
        }
        // 取该街道已有门牌，解析 "{areaId}-NNN" 的 NNN，求最大值
        List<HouseInfo> exists = list(new LambdaQueryWrapper<HouseInfo>()
                .eq(HouseInfo::getAreaId, streetAreaId));
        int max = 0;
        String prefix = streetAreaId + "-";
        for (HouseInfo h : exists) {
            String code = h.getHouseCode();
            if (code == null || !code.startsWith(prefix)) continue;
            String numPart = code.substring(prefix.length());
            try {
                max = Math.max(max, Integer.parseInt(numPart));
            } catch (NumberFormatException ignore) {
                // 非规则编号跳过
            }
        }
        int next = max + 1;
        if (next > 999) {
            throw new IllegalStateException("该街道门牌号已用尽（上限 999）");
        }
        return next;
    }

    @Override
    public HouseInfo getByHouseCode(String houseCode) {
        if (houseCode == null || houseCode.trim().isEmpty()) return null;
        return getOne(new LambdaQueryWrapper<HouseInfo>()
                .eq(HouseInfo::getHouseCode, houseCode.trim())
                .last("LIMIT 1"));
    }
}
