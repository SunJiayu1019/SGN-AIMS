package com.example.sppt.controller;

import com.example.sppt.entity.HouseInfo;
import com.example.sppt.mapper.HouseInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/house")
public class HouseController {

    @Autowired
    private HouseInfoMapper houseInfoMapper;

    @GetMapping("/list")
    public List<HouseInfo> list() {
        // 直接查询所有数据，不做任何过滤
        List<HouseInfo> list = houseInfoMapper.selectList(null);
        // 控制台打印，确认数据是否正常
        System.out.println("===== 查到数据条数：" + list.size());
        for (HouseInfo h : list) {
            System.out.println(h.getId() + " | " + h.getHouseCode() + " | " + h.getAddress() + " | " + h.getHouseType() + " | " + h.getStatus());
        }
        return list;
    }
}