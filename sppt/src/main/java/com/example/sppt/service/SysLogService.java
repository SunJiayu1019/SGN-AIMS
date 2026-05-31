package com.example.sppt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.SysLog;

/**
 * 系统操作日志 Service
 * 约定：业务代码可调用 record(...) 在任意操作成功后写一条日志；
 *      管理端日志页通过 page(...) 分页查询。
 * @author sjy
 * @since 2026-05-30
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 记录一条操作日志（异常不外抛，避免影响主流程）。
     */
    void record(Long operatorId, String operator, String action, String target, String detail);

    /**
     * 分页查询日志，支持按 操作类型 / 关键字（操作人或说明）过滤。
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页条数
     * @param action   操作类型，空表示不限
     * @param keyword  关键字，匹配 operator / detail，空表示不限
     */
    IPage<SysLog> page(int pageNum, int pageSize, String action, String keyword);
}
