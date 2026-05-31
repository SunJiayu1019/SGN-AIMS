package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志 Mapper
 * @author sjy
 * @since 2026-05-30
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}
