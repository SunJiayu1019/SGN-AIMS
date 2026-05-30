package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.service.HousePdfService;
import com.example.sppt.service.HouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 门牌管理接口（/api/house/**）
 * 增删改查 + 导出某门牌全部信息的 PDF。
 * 统一后：只依赖 Service，统一返回 Result（PDF 接口除外，返回二进制流），构造器注入。
 * @author sjy
 * @since 2026-05-29
 */
@RestController
@RequestMapping("/api/house")
@RequiredArgsConstructor
public class HouseAdminController {

    private final HouseService houseService;
    private final HousePdfService housePdfService;

    // 门牌列表（区域 / 类型 / 关键字均可选）
    @GetMapping("/list")
    public Result<List<HouseInfo>> list(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String keyword) {
        return Result.success(houseService.listByCondition(areaId, houseType, keyword));
    }

    // 单个门牌详情
    @GetMapping("/{id}")
    public Result<HouseInfo> getOne(@PathVariable Long id) {
        return Result.success(houseService.getById(id));
    }

    // 新增门牌
    @PostMapping("/save")
    public Result<HouseInfo> save(@RequestBody HouseInfo house) {
        if (house.getStatus() == null) {
            house.setStatus(1);
        }
        houseService.save(house);
        return Result.success(house);
    }

    // 修改门牌
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody HouseInfo house) {
        if (house.getId() == null) {
            return Result.fail("缺少门牌ID");
        }
        return Result.success(houseService.updateById(house));
    }

    // 删除门牌
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(houseService.removeById(id));
    }

    // 导出某门牌的全部信息为 PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] pdf = housePdfService.exportHousePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // 用 inline 便于浏览器直接预览；前端也可按附件下载
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"house_" + id + ".pdf\"");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
