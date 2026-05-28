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

    @Autowired  // 换成这个，Spring 自带，不用额外依赖
    private ApplyFormMapper applyFormMapper;

    @PostMapping("/submit")
    public String submit(@RequestBody ApplyForm form) {
        form.setCreateTime(LocalDateTime.now());
        form.setStatus("PENDING");
        applyFormMapper.insert(form);
        return "提交成功";
    }

    @GetMapping("/myList")
    public List<ApplyForm> myList(
            @RequestParam Long userId,
            @RequestParam String applyType
    ) {
        LambdaQueryWrapper<ApplyForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplyForm::getUserId, userId);
        wrapper.eq(ApplyForm::getApplyType, applyType);
        wrapper.orderByDesc(ApplyForm::getCreateTime);
        return applyFormMapper.selectList(wrapper);
    }
}