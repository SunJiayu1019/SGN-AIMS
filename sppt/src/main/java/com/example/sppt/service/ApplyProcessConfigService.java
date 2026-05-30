package com.example.sppt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.dto.ApplyProcessConfigDTO;
import com.example.sppt.entity.ApplyProcessNode;

import java.util.List;

/**
 * 审批流程配置 Service（按"申请类型"配置）。
 * 统一后：继承 IService<ApplyProcessNode>，方法只返回领域数据 / 抛异常。
 * 业务约定：
 *   - 配置以 applyType 为维度，全局生效（区域固定用 0 总站）；
 *   - 未配置时返回默认流程：new=3 级、reissue=1 级，
 *     审核人从用户表中的管理员里自动分配（每级一个）；
 *   - 仅"核心管理员 coreAdmin"可保存配置。
 * @author sjy
 * @since 2026-05-28
 */
public interface ApplyProcessConfigService extends IService<ApplyProcessNode> {

    // 按申请类型获取流程配置（无配置时返回自动分配的默认流程，不落库）
    List<ApplyProcessNode> getProcessConfigByType(String applyType);

    // 按申请类型保存流程配置（仅核心管理员）；失败时抛异常以触发事务回滚
    void saveProcessConfigByType(ApplyProcessConfigDTO dto);
}
