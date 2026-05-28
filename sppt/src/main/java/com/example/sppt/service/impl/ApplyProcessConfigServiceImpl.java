package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.dto.ApplyProcessConfigDTO;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.ApplyProcessNode;
import com.example.sppt.entity.SysUser;
import com.example.sppt.mapper.ApplyProcessNodeMapper;
import com.example.sppt.service.ApplyProcessConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplyProcessConfigServiceImpl implements ApplyProcessConfigService {

    private static final Logger log = LoggerFactory.getLogger(ApplyProcessConfigServiceImpl.class);
    private final ApplyProcessNodeMapper applyProcessNodeMapper;

    public ApplyProcessConfigServiceImpl(ApplyProcessNodeMapper applyProcessNodeMapper) {
        this.applyProcessNodeMapper = applyProcessNodeMapper;
    }

    /**
     * 根据 区域ID 查询审批流程配置
     */
    public Result<?> getProcessConfigByArea(Integer areaId) {
        // 1. 根据 areaId 查询该区域所有审批级别（1~5级）
        LambdaQueryWrapper<ApplyProcessNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplyProcessNode::getAreaId, areaId);
        wrapper.orderByAsc(ApplyProcessNode::getNodeLevel);
        List<ApplyProcessNode> configList = applyProcessNodeMapper.selectList(wrapper);

        // 2. 没有配置 → 自动生成默认 1级流程
        if (configList == null || configList.isEmpty()) {
            List<SysUser> adminUsers = applyProcessNodeMapper.selectAdminUsers();
            ApplyProcessNode node = new ApplyProcessNode();
            node.setAreaId(areaId);
            node.setNodeLevel(1);
            node.setAuditType("ONE");

            // 默认给第一个管理员
            if (!adminUsers.isEmpty()) {
                String userIds = adminUsers.get(0).getId().toString();
                node.setAuditUserIds(userIds);
            } else {
                node.setAuditUserIds("");
            }

            List<ApplyProcessNode> result = new ArrayList<>();
            result.add(node);
            return Result.success(result);
        }

        return Result.success(configList);
    }

    /**
     * 按 区域ID 保存审批流程
     * 支持 1~5级
     */
    @Transactional
    public Result<?> saveProcessConfigByArea(ApplyProcessConfigDTO dto) {
        try {
            Integer areaId = dto.getAreaId();
            Integer maxLevel = dto.getNodeLevel();
            List<List<String>> adminList = dto.getAuditUserIdsList();

            // 1. 删除该区域原有配置
            LambdaQueryWrapper<ApplyProcessNode> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(ApplyProcessNode::getAreaId, areaId);
            applyProcessNodeMapper.delete(delWrapper);

            // 2. 逐级保存
            for (int i = 0; i < maxLevel; i++) {
                int level = i + 1;
                List<String> userIds = adminList.get(i);

                ApplyProcessNode node = new ApplyProcessNode();
                node.setAreaId(areaId);
                node.setNodeLevel(level);
                node.setAuditUserIds(String.join(",", userIds));
                node.setAuditType("ONE");

                applyProcessNodeMapper.insert(node);
            }

            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存区域审批流程失败", e);
            return Result.fail("保存失败");
        }
    }

//    @Override
//    public Result<?> getProcessConfig(String applyType) {
//        return Result.success(new ArrayList<>());
//    }
}