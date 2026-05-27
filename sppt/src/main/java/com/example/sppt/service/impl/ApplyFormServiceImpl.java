package com.example.sppt.service.impl;

/**
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.mapper.ApplyFormMapper;
import com.example.sppt.service.ApplyFormService;
import org.springframework.stereotype.Service;

@Service
public class ApplyFormServiceImpl extends ServiceImpl<ApplyFormMapper, ApplyForm> implements ApplyFormService {
}