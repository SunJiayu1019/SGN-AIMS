package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.mapper.HouseInfoMapper;
import com.example.sppt.service.HouseInfoService;
import org.springframework.stereotype.Service;

@Service
public class HouseInfoServiceImpl
        extends ServiceImpl<HouseInfoMapper, HouseInfo>
        implements HouseInfoService {
    // 基本CRUD不用写，ServiceImpl已实现
}