package com.example.sppt.controller;

/**
 * @author sjy
 * @since 2026-05-28
 */
import com.example.sppt.dto.ApplyProcessConfigDTO;
import com.example.sppt.dto.Result;
import com.example.sppt.service.ApplyProcessConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process/config")
public class ApplyProcessConfigController {

    @Autowired
    private ApplyProcessConfigService applyProcessConfigService;
    /**
     * 根据 区域ID 查询流程配置
     */
    @GetMapping("/area/{areaId}")
    public Result<?> getByArea(@PathVariable Integer areaId) {
        return applyProcessConfigService.getProcessConfigByArea(areaId);
    }

    /**
     * 根据 区域ID 保存流程配置
     */
    @PostMapping("/save-by-area")
    public Result<?> saveByArea(@RequestBody ApplyProcessConfigDTO dto) {
        return applyProcessConfigService.saveProcessConfigByArea(dto);
    }


    /**
     * 获取审批流程配置
     * @param applyType new:门牌申请 reissue:门牌补发
     */
//    @GetMapping("/{applyType}")
//    public Result<?> getConfig(@PathVariable String applyType) {
//        return applyProcessConfigService.getProcessConfig(applyType);
//    }
    /**
     * 保存审批流程配置
     */
//    @PostMapping("/save")
//    public Result<?> saveConfig(@RequestBody ApplyProcessConfigDTO dto) {
//        return applyProcessConfigService.saveProcessConfig(dto);
//    }
}