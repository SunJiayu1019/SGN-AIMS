package com.example.sppt.service.impl;

/**
 * @author sjy
 * @since 2026-05-28
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysArea;
import com.example.sppt.mapper.SysAreaMapper;
import com.example.sppt.service.SysAreaService;
import org.springframework.stereotype.Service;

@Service
public class SysAreaServiceImpl extends ServiceImpl<SysAreaMapper, SysArea> implements SysAreaService {
}