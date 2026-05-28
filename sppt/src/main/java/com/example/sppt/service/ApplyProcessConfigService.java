package com.example.sppt.service;

/**
 * @author sjy
 * @since 2026-05-28
 */
import com.example.sppt.dto.ApplyProcessConfigDTO;
import com.example.sppt.dto.Result;

public interface ApplyProcessConfigService {

    // 按区域获取审批流程
    Result<?> getProcessConfigByArea(Integer areaId);

    // 按区域保存审批流程
    Result<?> saveProcessConfigByArea(ApplyProcessConfigDTO dto);
    // 获取审批流程配置
//    Result<?> getProcessConfig(String applyType);

    // 保存审批流程配置
//    Result<?> saveProcessConfig(ApplyProcessConfigDTO dto);
}