package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysLog;
import com.example.sppt.mapper.SysLogMapper;
import com.example.sppt.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统操作日志业务实现。
 * @author sjy
 * @since 2026-05-30
 */
@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
        implements SysLogService {

    @Override
    public void record(Long operatorId, String operator, String action, String target, String detail) {
        try {
            SysLog logEntity = new SysLog();
            logEntity.setOperatorId(operatorId);
            logEntity.setOperator(operator == null ? "系统" : operator);
            logEntity.setAction(action);
            logEntity.setTarget(target);
            logEntity.setDetail(detail);
            logEntity.setCreateTime(LocalDateTime.now());
            save(logEntity);
        } catch (Exception e) {
            // 记录日志失败不能影响主业务流程，仅打印告警
            log.warn("写入系统日志失败：{}", e.getMessage());
        }
    }

    @Override
    public IPage<SysLog> page(int pageNum, int pageSize, String action, String keyword) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (action != null && !action.isBlank() && !"all".equalsIgnoreCase(action)) {
            wrapper.eq(SysLog::getAction, action);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysLog::getOperator, keyword)
                    .or().like(SysLog::getDetail, keyword));
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
