package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysHelp;

import java.util.List;

/**
 * 帮助文档 Service
 * @author sjy
 * @since 2026-05-30
 */
public interface SysHelpService extends IService<SysHelp> {

    // 按 sort 升序返回全部帮助条目（前端帮助页渲染用）
    List<SysHelp> listAllSorted();
}
