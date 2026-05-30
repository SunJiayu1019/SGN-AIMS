package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.PortalAbout;

public interface PortalAboutService extends IService<PortalAbout> {
    PortalAbout getByAreaId(String areaId);
}