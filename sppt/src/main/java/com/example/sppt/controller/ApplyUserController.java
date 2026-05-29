package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.mapper.ApplyFormMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user/apply")
public class ApplyUserController {

    @Autowired
    private ApplyFormMapper applyFormMapper;

    // 提交申请（修复版）
    @PostMapping("/submit")
    public String submit(@RequestBody ApplyForm form) {
        // 自动生成申请编号
        form.setApplyNo("APPLY" + System.currentTimeMillis());
        // 给houseId一个默认值，避免数据库报错
        if (form.getHouseId() == null) {
            form.setHouseId(1L);
        }
        form.setCreateTime(LocalDateTime.now());
        form.setStatus("PENDING");
        applyFormMapper.insert(form);
        return "提交成功";
    }

    // 查询我的申请列表（修复：接口地址 list，applyType 可选）
    @GetMapping("/list")
    public List<ApplyForm> myList(
            @RequestParam Long userId,
            @RequestParam(required = false) String applyType
    ) {
        LambdaQueryWrapper<ApplyForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplyForm::getUserId, userId);

        // 如果有传类型，才加筛选条件
        if (applyType != null && !applyType.isEmpty()) {
            wrapper.eq(ApplyForm::getApplyType, applyType);
        }

        wrapper.orderByDesc(ApplyForm::getCreateTime);
        return applyFormMapper.selectList(wrapper);
    }
}