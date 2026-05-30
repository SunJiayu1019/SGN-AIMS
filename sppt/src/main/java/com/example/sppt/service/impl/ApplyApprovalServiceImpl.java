package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.ApplyApproval;
import com.example.sppt.mapper.ApplyApprovalMapper;
import com.example.sppt.service.ApplyApprovalService;
import org.springframework.stereotype.Service;

@Service
public class ApplyApprovalServiceImpl extends ServiceImpl<ApplyApprovalMapper, ApplyApproval>
        implements ApplyApprovalService {
}
