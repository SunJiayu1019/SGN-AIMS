package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysBannedWord;

import java.util.List;

/**
 * 禁用词条 Service
 * @author sjy
 * @since 2026-05-30
 */
public interface SysBannedWordService extends IService<SysBannedWord> {

    // 全部禁用词
    List<SysBannedWord> listAll();

    /**
     * 检测文本是否命中禁用词。
     * @return 命中的第一个禁用词；未命中返回 null
     */
    String findHit(String... texts);
}
