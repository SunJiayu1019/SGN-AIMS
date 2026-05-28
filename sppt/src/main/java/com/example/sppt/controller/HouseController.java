package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.mapper.HouseInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/house")
public class HouseController {

    @Autowired
    private HouseInfoMapper houseInfoMapper;

    // 按区域ID查询门牌列表（用于门牌排查页面）
    @GetMapping("/list")
    public List<HouseInfo> list(@RequestParam Long areaId) {
        LambdaQueryWrapper<HouseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseInfo::getAreaId, areaId);
        return houseInfoMapper.selectList(wrapper);
    }
}