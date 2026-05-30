package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.service.HouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 用户端 - 门牌查询接口（/user/house/**）
 * 统一后：只依赖 HouseService，统一返回 Result，构造器注入。
 * @author sjy
 */
@RestController
@RequestMapping("/user/house")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    @GetMapping("/list")
    public Result<List<HouseInfo>> list(@RequestParam Long areaId) {
        return Result.success(houseService.listByArea(areaId));
    }
}