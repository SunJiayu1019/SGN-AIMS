package com.example.sppt.service;

/**
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.ApplyForm;

public interface ApplyFormService extends IService<ApplyForm> {
    boolean audit(Long id, String status, String remark);
}