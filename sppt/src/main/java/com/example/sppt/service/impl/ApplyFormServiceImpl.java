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
    @Override
    public boolean audit(Long id, String status,String remark) {
        // 1. 先查出来
        ApplyForm form = this.getById(id);
        if (form == null) {
            return false;
        }
        // 2. 改状态
        form.setStatus(status); // "APPROVED" 或 "REJECTED"
        // 3. 保存到数据库
        return this.updateById(form);

//        // 插入审批记录（含意见）
//        ApplyApproval approval = new ApplyApproval();
//        approval.setApplyId(id);           // 关联申请单
//        approval.setAuditUserId(1L); // 随便写一个固定值
//        approval.setResult(status);        // 审批结果
//        approval.setRemark(remark);        // ✅审批意见
//        approval.setCreateTime(new Date());
//
//        applyApprovalMapper.insert(approval); // 插入数据库
//        return true;
    }
}