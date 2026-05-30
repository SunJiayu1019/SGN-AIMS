package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.dto.ApplyProcessConfigDTO;
import com.example.sppt.entity.ApplyProcessNode;
import com.example.sppt.entity.SysUser;
import com.example.sppt.mapper.ApplyProcessNodeMapper;
import com.example.sppt.service.ApplyProcessConfigService;
import com.example.sppt.service.SysUserRoleService;
import com.example.sppt.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 审批流程配置 Service 实现（按申请类型）。
 * 统一后：继承 ServiceImpl 复用 BaseMapper；跨 Service 依赖走构造器注入；
 *        Result 包装交给 Controller。
 * @author sjy
 * @since 2026-05-28
 */
@Service
@RequiredArgsConstructor
public class ApplyProcessConfigServiceImpl
        extends ServiceImpl<ApplyProcessNodeMapper, ApplyProcessNode>
        implements ApplyProcessConfigService {

    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;

    // 本配置为全局，区域固定用 0（总站）
    private static final int GLOBAL_AREA = 0;

    // 各申请类型的默认级数
    private int defaultLevel(String applyType) {
        return "new".equals(applyType) ? 3 : 1;   // 门牌申请 3 级，门牌补发 1 级
    }

    @Override
    public List<ApplyProcessNode> getProcessConfigByType(String applyType) {
        // 1. 查询已保存的配置（按级别升序）
        List<ApplyProcessNode> configList = list(new LambdaQueryWrapper<ApplyProcessNode>()
                .eq(ApplyProcessNode::getAreaId, GLOBAL_AREA)
                .eq(ApplyProcessNode::getApplyType, applyType)
                .orderByAsc(ApplyProcessNode::getNodeLevel));

        if (configList != null && !configList.isEmpty()) {
            return configList;
        }

        // 2. 没有配置 -> 生成默认流程（new=3 级 / reissue=1 级），
        //    从用户表的管理员里自动分配，每级分配一个（管理员不足时循环复用）。
        int levels = defaultLevel(applyType);
        List<SysUser> admins = sysUserService.getAdminUsers();

        List<ApplyProcessNode> result = new ArrayList<>();
        for (int i = 1; i <= levels; i++) {
            ApplyProcessNode node = new ApplyProcessNode();
            node.setAreaId(GLOBAL_AREA);
            node.setApplyType(applyType);
            node.setNodeLevel(i);
            node.setAuditType("ONE");
            if (admins != null && !admins.isEmpty()) {
                SysUser admin = admins.get((i - 1) % admins.size());
                node.setAuditUserIds(admin.getId().toString());
            } else {
                node.setAuditUserIds("");
            }
            result.add(node);
        }
        return result;
    }

    @Override
    @Transactional
    public void saveProcessConfigByType(ApplyProcessConfigDTO dto) {
        // 0. 仅核心管理员可配置
        if (dto.getOperatorId() == null
                || !sysUserRoleService.hasRole(dto.getOperatorId(), "coreAdmin")) {
            throw new IllegalArgumentException("只有核心管理员可以配置审批流程");
        }

        String applyType = dto.getApplyType();
        Integer maxLevel = dto.getNodeLevel();
        List<List<String>> adminList = dto.getAuditUserIdsList();

        if (applyType == null || maxLevel == null || maxLevel < 1
                || adminList == null || adminList.size() < maxLevel) {
            throw new IllegalArgumentException("审批流程配置参数不完整");
        }

        // 1. 删除该申请类型原有配置
        remove(new LambdaQueryWrapper<ApplyProcessNode>()
                .eq(ApplyProcessNode::getAreaId, GLOBAL_AREA)
                .eq(ApplyProcessNode::getApplyType, applyType));

        // 2. 逐级保存
        for (int i = 0; i < maxLevel; i++) {
            List<String> userIds = adminList.get(i);

            ApplyProcessNode node = new ApplyProcessNode();
            node.setAreaId(GLOBAL_AREA);
            node.setApplyType(applyType);
            node.setNodeLevel(i + 1);
            node.setAuditUserIds(userIds == null ? "" : String.join(",", userIds));
            node.setAuditType("ONE");
            save(node);
        }
    }
}
