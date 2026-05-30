package com.example.sppt.service;

import java.util.List;
import java.util.Map;

/**
 * 统计分析 Service（跨多张表聚合，故为普通 Service，不绑定单一实体、不继承 IService）。
 * 统一后：聚合逻辑从 StatsController 下沉到这里；本 Service 只依赖其它实体 Service，
 *        不直接依赖任何 Mapper。
 * @author sjy
 * @since 2026-05-29
 */
public interface StatsService {

    // 顶部概览数字卡片
    Map<String, Object> overview();

    // 门牌类别统计
    List<Map<String, Object>> houseCountByType();

    // 门牌区域统计
    List<Map<String, Object>> houseCountByArea();

    // 申请状态统计
    List<Map<String, Object>> applyCountByStatus();
}
