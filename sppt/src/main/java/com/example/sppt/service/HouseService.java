package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.HouseInfo;

import java.util.List;

/**
 * 门牌信息 Service
 * 新增此 Service 是为了让 Controller 统一“只依赖 Service、不直接依赖 Mapper”。
 * @author sjy
 */
public interface HouseService extends IService<HouseInfo> {

    // 门牌列表（区域 / 类型 / 关键字 均可选）
    List<HouseInfo> listByCondition(Long areaId, String houseType, String keyword);

    // 按区域查询（门牌排查页用）
    List<HouseInfo> listByArea(Long areaId);

    /**
     * 生成街道内下一个可用的三位门牌号（1..999）。
     * 规则：查询该街道(areaId, level=4)已有门牌中 "{areaId}-NNN" 的最大 NNN，+1 返回。
     * @return 下一个门牌号（整数，如 1、2、...）；超过 999 抛异常。
     */
    int nextHouseNumber(Long streetAreaId);

    /**
     * 按门牌编号查询门牌（补发时用 original_house_code 定位原门牌）。
     */
    HouseInfo getByHouseCode(String houseCode);
}
