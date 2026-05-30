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
}
