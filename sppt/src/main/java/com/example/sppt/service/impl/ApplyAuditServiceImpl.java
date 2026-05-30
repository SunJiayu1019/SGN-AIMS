package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.dto.AuditDTO;
import com.example.sppt.entity.ApplyApproval;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.entity.ApplyProcessNode;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.service.ApplyApprovalService;
import com.example.sppt.service.ApplyAuditService;
import com.example.sppt.service.ApplyFormService;
import com.example.sppt.service.ApplyProcessConfigService;
import com.example.sppt.service.HouseService;
import com.example.sppt.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 门牌申请审批业务实现。
 * 统一约定：跨 Service 依赖走构造器注入；不直接依赖 Mapper；Result 由 Controller 包装。
 * @author sjy
 */
@Service
@RequiredArgsConstructor
public class ApplyAuditServiceImpl implements ApplyAuditService {

    private final ApplyFormService applyFormService;
    private final ApplyProcessConfigService applyProcessConfigService;
    private final SysUserRoleService sysUserRoleService;
    private final HouseService houseService;
    private final ApplyApprovalService applyApprovalService;

    // 本系统的两种申请类型
    private static final List<String> APPLY_TYPES = Arrays.asList("new", "reissue");

    @Override
    public List<ApplyForm> listPending(Long adminId) {
        List<ApplyForm> all = applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getStatus, "PENDING")
                .orderByDesc(ApplyForm::getCreateTime));
        return filterByResponsibility(all, adminId);
    }

    @Override
    public List<ApplyForm> listHandled(Long adminId) {
        List<ApplyForm> all = applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .in(ApplyForm::getStatus, "APPROVED", "REJECTED")
                .orderByDesc(ApplyForm::getCreateTime));
        return filterByResponsibility(all, adminId);
    }

    /**
     * 按"谁该负责"过滤：核心管理员看全部；其余管理员只看自己被指派的申请类型。
     */
    private List<ApplyForm> filterByResponsibility(List<ApplyForm> list, Long adminId) {
        if (adminId != null && sysUserRoleService.hasRole(adminId, "coreAdmin")) {
            return list;   // 核心管理员：全部
        }
        List<String> responsibleTypes = responsibleTypes(adminId);
        List<ApplyForm> result = new ArrayList<>();
        for (ApplyForm f : list) {
            if (responsibleTypes.contains(f.getApplyType())) {
                result.add(f);
            }
        }
        return result;
    }

    // 该管理员被指派到的申请类型集合（按"已保存或默认"的有效配置计算）
    private List<String> responsibleTypes(Long adminId) {
        List<String> types = new ArrayList<>();
        if (adminId == null) {
            return types;
        }
        String adminIdStr = adminId.toString();
        for (String type : APPLY_TYPES) {
            List<ApplyProcessNode> nodes = applyProcessConfigService.getProcessConfigByType(type);
            boolean assigned = nodes.stream().anyMatch(n -> containsId(n.getAuditUserIds(), adminIdStr));
            if (assigned) {
                types.add(type);
            }
        }
        return types;
    }

    // 找出该管理员在某申请类型里负责的级别（用于审批记录），找不到返回 null
    private Integer operatorLevel(String applyType, Long adminId) {
        if (adminId == null) {
            return null;
        }
        String adminIdStr = adminId.toString();
        List<ApplyProcessNode> nodes = applyProcessConfigService.getProcessConfigByType(applyType);
        for (ApplyProcessNode n : nodes) {
            if (containsId(n.getAuditUserIds(), adminIdStr)) {
                return n.getNodeLevel();
            }
        }
        return null;
    }

    private boolean containsId(String csv, String idStr) {
        if (csv == null || csv.isEmpty()) {
            return false;
        }
        for (String part : csv.split(",")) {
            if (part.trim().equals(idStr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void audit(AuditDTO dto) {
        if (dto.getApplyId() == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("审批参数不完整");
        }
        String status = dto.getStatus();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("非法的审批状态：" + status);
        }

        ApplyForm form = applyFormService.getById(dto.getApplyId());
        if (form == null) {
            throw new IllegalArgumentException("申请不存在");
        }

        // 1. 修改申请状态
        form.setStatus(status);
        applyFormService.updateById(form);

        // 2. 写入审批记录（含审批意见）
        ApplyApproval approval = new ApplyApproval();
        approval.setApplyId(form.getId());
        approval.setAuditUserId(dto.getAuditUserId());
        approval.setNodeLevel(operatorLevel(form.getApplyType(), dto.getAuditUserId()));
        approval.setResult("APPROVED".equals(status) ? "APPROVE" : "REJECT");
        approval.setRemark(dto.getRemark());
        approval.setCreateTime(LocalDateTime.now());
        applyApprovalService.save(approval);

        // 3. 审批通过 -> 门牌信息入库
        if ("APPROVED".equals(status)) {
            enterHouse(form);
        }
    }

    /**
     * 将通过的申请落到门牌表：
     *   - 新申请(new)：门牌此前不存在，新增一条 house_info，并回写 apply_form.house_id 建立关联；
     *   - 补发(reissue)：门牌已存在，置为有效状态。
     */
    private void enterHouse(ApplyForm form) {
        if (form.getHouseId() != null) {
            // 补发 / 已关联门牌：确保有效
            HouseInfo house = houseService.getById(form.getHouseId());
            if (house != null) {
                house.setStatus(1);
                houseService.updateById(house);
            }
            return;
        }

        // 新申请：新增门牌
        HouseInfo house = new HouseInfo();
        house.setHouseCode("MP-" + form.getApplyNo());   // 用唯一申请单号派生唯一门牌编号
        String address = (form.getDetailAddress() != null && !form.getDetailAddress().trim().isEmpty())
                ? form.getDetailAddress()
                : ("待补充地址-" + form.getApplyNo());
        house.setAddress(address);
        house.setHouseType(form.getHouseType());
        house.setAreaId(form.getAreaId());
        house.setStatus(1);
        houseService.save(house);

        // 回写关联
        form.setHouseId(house.getId());
        applyFormService.updateById(form);
    }
}
