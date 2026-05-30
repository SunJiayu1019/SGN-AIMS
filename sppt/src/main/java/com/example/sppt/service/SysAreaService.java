package com.example.sppt.service;

/**
 * @author sjy
 * @since 2026-05-28
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysArea;

import java.util.List;

public interface SysAreaService extends IService<SysArea> {

    /**
     * 获取指定区域「自身 + 全部子孙」的 ID 列表。
     * 例如传入山西省的 id，会返回 [山西省, 太原市, 吕梁市…, 杏花岭区, 迎泽区…] 的全部 id。
     * 用于“点击上级区域时，连同下级区域的内容一起查出”。
     *
     * @param areaId 区域ID；传 null 或 0 表示“全部区域”，返回 null（调用方据此不加区域过滤）
     * @return 自身及所有后代的 id 列表；当 areaId 为 null/0 时返回 null
     */
    List<Integer> listSelfAndDescendantIds(Integer areaId);
}
