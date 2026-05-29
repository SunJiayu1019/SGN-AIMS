package com.example.sppt.controller;

import com.example.sppt.entity.HouseInfo;
import com.example.sppt.service.HouseInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/house")
@RequiredArgsConstructor
public class HouseCheckController {

    // 注入接口，和你其他Controller写法一致
    private final HouseInfoService houseInfoService;

    // 查所有门牌（排查用）
    @GetMapping("/list")
    public List<HouseInfo> list() {
        return houseInfoService.list();
    }

    // 按id查
    @GetMapping("/{id}")
    public HouseInfo getById(@PathVariable Long id) {
        return houseInfoService.getById(id);
    }

    // 新增/编辑门牌
    @PostMapping("/save")
    public boolean save(@RequestBody HouseInfo houseInfo) {
        return houseInfoService.saveOrUpdate(houseInfo);
    }

    // 删除
    @DeleteMapping("/{id}")
    public boolean remove(@PathVariable Long id) {
        return houseInfoService.removeById(id);
    }
}