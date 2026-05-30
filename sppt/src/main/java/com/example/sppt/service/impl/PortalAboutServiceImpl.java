package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.PortalAbout;
import com.example.sppt.mapper.PortalAboutMapper;
import com.example.sppt.service.PortalAboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortalAboutServiceImpl extends ServiceImpl<PortalAboutMapper, PortalAbout> implements PortalAboutService {


        @Override
        public PortalAbout getByAreaId(String areaId) {
            // 1. 优先查询当前选中的区域
            LambdaQueryWrapper<PortalAbout> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PortalAbout::getAreaId, areaId);
            PortalAbout about = this.getOne(wrapper);

            // 2. 如果当前区域没有配置，则返回省级默认（140000）
            if (about == null) {
                wrapper.clear();
                wrapper.eq(PortalAbout::getAreaId, "140000");
                about = this.getOne(wrapper);
            }
            return about;
        }
    }