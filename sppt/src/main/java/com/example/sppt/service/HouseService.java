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
}
