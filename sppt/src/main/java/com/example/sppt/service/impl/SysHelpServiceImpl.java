package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysHelp;
import com.example.sppt.mapper.SysHelpMapper;
import com.example.sppt.service.SysHelpService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帮助文档 Service 实现
 * @author sjy
 * @since 2026-05-30
 */
@Service
public class SysHelpServiceImpl extends ServiceImpl<SysHelpMapper, SysHelp>
        implements SysHelpService {

    @Override
    public List<SysHelp> listAllSorted() {
        return list(new LambdaQueryWrapper<SysHelp>()
                .orderByAsc(SysHelp::getSort)
                .orderByDesc(SysHelp::getCreateTime));
    }
}
