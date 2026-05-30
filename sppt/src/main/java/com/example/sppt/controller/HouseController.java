package com.example.sppt.controller;

<<<<<<< HEAD
import com.example.sppt.dto.Result;
=======
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.service.HouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
<<<<<<< HEAD
    public Result<List<HouseInfo>> list(@RequestParam Long areaId) {
        return Result.success(houseService.listByArea(areaId));
=======
    public List<HouseInfo> list() {
        // 直接查询所有数据，不做任何过滤
        List<HouseInfo> list = houseInfoMapper.selectList(null);
        // 控制台打印，确认数据是否正常
        System.out.println("===== 查到数据条数：" + list.size());
        for (HouseInfo h : list) {
            System.out.println(h.getId() + " | " + h.getHouseCode() + " | " + h.getAddress() + " | " + h.getHouseType() + " | " + h.getStatus());
        }
        return list;
>>>>>>> 5039016ff1150fc0acaa89916f528ff1f5c6b387
    }
}
