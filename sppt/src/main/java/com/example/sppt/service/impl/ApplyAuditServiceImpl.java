package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.dto.AuditDTO;
import com.example.sppt.entity.*;
import com.example.sppt.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 门牌申请审批业务实现（真正的「逐级审批」）。
 *
 * 多级审批模型：
 *   - 审批流程由 apply_process_node 按 applyType 逐级（nodeLevel 1..N）配置，
 *     每级有审核人名单(auditUserIds) 和 通过策略(auditType：ONE 任一通过 / ALL 全部通过)；
 *   - apply_form.status 只有三态 PENDING / APPROVED / REJECTED，
 *     “当前处于第几级”由 apply_approval 中该申请已通过(APPROVE)的记录推导，避免改表结构；
 *   - 某级有人 REJECT -> 申请直接 REJECTED（终审驳回）；
 *   - 某级满足通过策略 -> 进入下一级；最后一级通过 -> APPROVED 并门牌入库。
 *
 * 这样 apply_approval 会逐级累积多条记录，nodeLevel 真实反映每条记录所在层级。
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
    private final SysLogService sysLogService;
    private final SysAreaService sysAreaService;

    private static final List<String> APPLY_TYPES = Arrays.asList("new", "reissue");

    @Override
    public List<ApplyForm> listPending(Long adminId) {
        List<ApplyForm> all = applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .eq(ApplyForm::getStatus, "PENDING")
                .orderByDesc(ApplyForm::getCreateTime));
        return filterByResponsibility(all, adminId, true);
    }

    @Override
    public List<ApplyForm> listHandled(Long adminId) {
        List<ApplyForm> all = applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                .in(ApplyForm::getStatus, "APPROVED", "REJECTED")
                .orderByDesc(ApplyForm::getCreateTime));
        return filterByResponsibility(all, adminId, false);
    }

    /**
     * 责任过滤。
     *  - 核心管理员：全部可见；
     *  - 普通管理员：只看自己被指派的申请类型；
     *  - 待审批列表(onlyCurrentLevel=true)进一步只展示“轮到该管理员所在级别”的申请。
     */
    private List<ApplyForm> filterByResponsibility(List<ApplyForm> list, Long adminId, boolean onlyCurrentLevel) {
        boolean core = adminId != null && sysUserRoleService.hasRole(adminId, "coreAdmin");
        List<String> responsibleTypes = core ? APPLY_TYPES : responsibleTypes(adminId);

        List<ApplyForm> result = new ArrayList<>();
        for (ApplyForm f : list) {
            if (!responsibleTypes.contains(f.getApplyType())) {
                continue;
            }
            if (onlyCurrentLevel && !core) {
                // 普通管理员的待审批：必须正好轮到他负责的那一级，且他还没在该级投过票
                Integer myLevel = operatorLevel(f.getApplyType(), adminId);
                if (myLevel == null || !myLevel.equals(currentLevel(f))) {
                    continue;
                }
                if (hasVotedAtLevel(f.getId(), adminId, myLevel)) {
                    continue;
                }
            }
            result.add(f);
        }
        return result;
    }

    private List<String> responsibleTypes(Long adminId) {
        List<String> types = new ArrayList<>();
        if (adminId == null) return types;
        String idStr = adminId.toString();
        for (String type : APPLY_TYPES) {
            List<ApplyProcessNode> nodes = applyProcessConfigService.getProcessConfigByType(type);
            if (nodes.stream().anyMatch(n -> containsId(n.getAuditUserIds(), idStr))) {
                types.add(type);
            }
        }
        return types;
    }

    private Integer operatorLevel(String applyType, Long adminId) {
        if (adminId == null) return null;
        String idStr = adminId.toString();
        for (ApplyProcessNode n : applyProcessConfigService.getProcessConfigByType(applyType)) {
            if (containsId(n.getAuditUserIds(), idStr)) {
                return n.getNodeLevel();
            }
        }
        return null;
    }

    private boolean containsId(String csv, String idStr) {
        if (csv == null || csv.isEmpty()) return false;
        for (String part : csv.split(",")) {
            if (part.trim().equals(idStr)) return true;
        }
        return false;
    }

    // ---- 多级流转的核心：根据 apply_approval 已通过记录推导“当前级别” ----

    /** 当前正在审批的级别 = 已通过(APPROVE)的最高级别 + 1（从 1 开始）。 */
    private int currentLevel(ApplyForm form) {
        List<ApplyApproval> approvals = approvalsOf(form.getId());
        int maxPassed = 0;
        for (ApplyApproval a : approvals) {
            if ("APPROVE".equals(a.getResult()) && a.getNodeLevel() != null) {
                maxPassed = Math.max(maxPassed, a.getNodeLevel());
            }
        }
        return maxPassed + 1;
    }

    private List<ApplyApproval> approvalsOf(Long applyId) {
        return applyApprovalService.list(new LambdaQueryWrapper<ApplyApproval>()
                .eq(ApplyApproval::getApplyId, applyId));
    }

    private boolean hasVotedAtLevel(Long applyId, Long userId, Integer level) {
        return applyApprovalService.count(new LambdaQueryWrapper<ApplyApproval>()
                .eq(ApplyApproval::getApplyId, applyId)
                .eq(ApplyApproval::getAuditUserId, userId)
                .eq(ApplyApproval::getNodeLevel, level)) > 0;
    }

    private ApplyProcessNode nodeOfLevel(String applyType, int level) {
        for (ApplyProcessNode n : applyProcessConfigService.getProcessConfigByType(applyType)) {
            if (n.getNodeLevel() != null && n.getNodeLevel() == level) {
                return n;
            }
        }
        return null;
    }

    private int totalLevels(String applyType) {
        int max = 0;
        for (ApplyProcessNode n : applyProcessConfigService.getProcessConfigByType(applyType)) {
            if (n.getNodeLevel() != null) max = Math.max(max, n.getNodeLevel());
        }
        return Math.max(max, 1);
    }

    @Override
    @Transactional
    public void audit(AuditDTO dto) {
        if (dto.getApplyId() == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("审批参数不完整");
        }
        // 前端传 APPROVED/REJECTED 表示这一票的意见；这里转成记录用的 APPROVE/REJECT
        String vote = dto.getStatus();
        if (!"APPROVED".equals(vote) && !"REJECTED".equals(vote)) {
            throw new IllegalArgumentException("非法的审批状态：" + vote);
        }

        ApplyForm form = applyFormService.getById(dto.getApplyId());
        if (form == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!"PENDING".equals(form.getStatus())) {
            throw new IllegalArgumentException("该申请已审结，无法再次审批");
        }

        Long auditUserId = dto.getAuditUserId();
        boolean core = auditUserId != null && sysUserRoleService.hasRole(auditUserId, "coreAdmin");

        int curLevel = currentLevel(form);
        int total = totalLevels(form.getApplyType());
        ApplyProcessNode node = nodeOfLevel(form.getApplyType(), curLevel);

        // 权限校验：核心管理员可代任意级别审批；普通管理员必须正好负责当前级别
        if (!core) {
            Integer myLevel = operatorLevel(form.getApplyType(), auditUserId);
            if (myLevel == null || myLevel != curLevel) {
                throw new IllegalArgumentException("当前不该由您审批（未轮到您负责的级别）");
            }
            if (hasVotedAtLevel(form.getId(), auditUserId, curLevel)) {
                throw new IllegalArgumentException("您已在本级别审批过，请勿重复操作");
            }
        }

        // 1. 写入本级一条审批记录（无论通过/驳回都记录，nodeLevel 真实落级）
        ApplyApproval approval = new ApplyApproval();
        approval.setApplyId(form.getId());
        approval.setAuditUserId(auditUserId);
        approval.setNodeLevel(curLevel);
        approval.setResult("APPROVED".equals(vote) ? "APPROVE" : "REJECT");
        approval.setRemark(dto.getRemark());
        approval.setCreateTime(LocalDateTime.now());
        applyApprovalService.save(approval);

        // 2. 驳回 -> 直接终审驳回
        if ("REJECTED".equals(vote)) {
            form.setStatus("REJECTED");
            applyFormService.updateById(form);
            sysLogService.record(auditUserId, null, "审批", "apply_form",
                    "第" + curLevel + "级驳回申请单 " + form.getApplyNo());
            return;
        }

        // 3. 通过：判断本级是否“整体通过”（ONE：任一通过即可；ALL：名单内全部通过）
        boolean levelPassed = isLevelPassed(form, node, curLevel);

        if (!levelPassed) {
            // ALL 策略下还差其他审核人，本级未完成，申请保持 PENDING 等待同级其他人
            sysLogService.record(auditUserId, null, "审批", "apply_form",
                    "第" + curLevel + "级通过（本级需全部审核人通过，等待中）申请单 " + form.getApplyNo());
            return;
        }

        // 4. 本级已通过：若是最后一级 -> 终审通过并门牌入库；否则进入下一级
        if (curLevel >= total) {
            form.setStatus("APPROVED");
            applyFormService.updateById(form);
            enterHouse(form);
            sysLogService.record(auditUserId, null, "审批", "apply_form",
                    "终审（第" + curLevel + "级）通过申请单 " + form.getApplyNo() + "，门牌已入库");
        } else {
            // 仍为 PENDING，currentLevel() 下次会自动算成 curLevel+1
            sysLogService.record(auditUserId, null, "审批", "apply_form",
                    "第" + curLevel + "级通过，流转至第" + (curLevel + 1) + "级，申请单 " + form.getApplyNo());
        }
    }

    /**
     * 判断某级是否整体通过：
     *  - ONE：本级出现任意一条 APPROVE 即通过；
     *  - ALL：本级名单内每个审核人都已 APPROVE 才通过。
     */
    private boolean isLevelPassed(ApplyForm form, ApplyProcessNode node, int level) {
        String auditType = node == null || node.getAuditType() == null ? "ONE" : node.getAuditType();
        List<ApplyApproval> approvals = approvalsOf(form.getId());

        if ("ALL".equalsIgnoreCase(auditType) && node != null) {
            String csv = node.getAuditUserIds() == null ? "" : node.getAuditUserIds();
            if (csv.isBlank()) return true;
            for (String idStr : csv.split(",")) {
                String want = idStr.trim();
                if (want.isEmpty()) continue;
                boolean approved = approvals.stream().anyMatch(a ->
                        "APPROVE".equals(a.getResult())
                                && a.getNodeLevel() != null && a.getNodeLevel() == level
                                && a.getAuditUserId() != null
                                && a.getAuditUserId().toString().equals(want));
                if (!approved) return false;
            }
            return true;
        }

        // ONE：本级存在任意一条 APPROVE
        return approvals.stream().anyMatch(a ->
                "APPROVE".equals(a.getResult())
                        && a.getNodeLevel() != null && a.getNodeLevel() == level);
    }

    /**
     * 审批进度：当前级别 / 总级别 / 状态 / 各级审批记录。
     */
    @Override
    public Map<String, Object> progress(Long applyId) {
        Map<String, Object> map = new LinkedHashMap<>();
        ApplyForm form = applyFormService.getById(applyId);
        if (form == null) {
            map.put("exists", false);
            return map;
        }
        int total = totalLevels(form.getApplyType());
        int cur = currentLevel(form);
        map.put("exists", true);
        map.put("status", form.getStatus());
        map.put("totalLevels", total);
        // 审结后 currentLevel 可能超过 total，这里夹取展示
        map.put("currentLevel", Math.min(cur, total));

        List<ApplyApproval> approvals = applyApprovalService.list(
                new LambdaQueryWrapper<ApplyApproval>()
                        .eq(ApplyApproval::getApplyId, applyId)
                        .orderByAsc(ApplyApproval::getNodeLevel)
                        .orderByAsc(ApplyApproval::getCreateTime));
        List<Map<String, Object>> records = new ArrayList<>();
        for (ApplyApproval a : approvals) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("nodeLevel", a.getNodeLevel());
            r.put("auditUserId", a.getAuditUserId());
            r.put("result", a.getResult());
            r.put("remark", a.getRemark());
            r.put("createTime", a.getCreateTime());
            records.add(r);
        }
        map.put("records", records);
        return map;
    }

    /**
     * 将通过的申请落到门牌表（核心业务规则）：
     *
     *  新申请(new)：
     *    1. 在申请人所在街道(areaId, level=4)内生成下一个三位门牌号 NNN（001..999）；
     *    2. house_code = "{街道areaId}-{NNN}"，如 "10086-001"；
     *    3. 根据 area_id 查询对应街道的地理坐标（lng/lat），自动注入 house_info；
     *    4. 新增 house_info（apply_no=申请号、house_code、area_id=街道id、
     *       lng/lat/geometry 由街道自动提供、status=1）；
     *    5. 回写 apply_form.house_id = NNN（三位门牌号对应的整数）。
     *
     *  补发(reissue)：
     *    1. 以 apply_form.original_house_code 在 house_info.house_code 中定位原门牌；
     *    2. 复用原门牌号，不生成新号；将原 house_info.status 置为 1（有效/已补发）；
     *    3. 不修改 house_code（门牌号保持原样）。
     */
    private void enterHouse(ApplyForm form) {
        boolean isReissue = "reissue".equalsIgnoreCase(form.getApplyType());

        if (isReissue) {
            // —— 补发：复用原门牌号，仅更新状态 ——
            String originalCode = form.getOriginalHouseCode();
            if (originalCode == null || originalCode.trim().isEmpty()) {
                throw new IllegalStateException("补发申请缺少原门牌号(original_house_code)");
            }
            HouseInfo origin = houseService.getByHouseCode(originalCode.trim());
            if (origin == null) {
                throw new IllegalStateException("未找到原门牌：" + originalCode);
            }
            origin.setStatus(1);
            houseService.updateById(origin);
            return;
        }

        // —— 新申请：在街道内分配新的三位门牌号 ——
        Long streetAreaId = form.getAreaId();
        if (streetAreaId == null || streetAreaId <= 0) {
            throw new IllegalStateException("新申请缺少所属街道(area_id, level=4)");
        }

        int number = houseService.nextHouseNumber(streetAreaId);        // 1..999
        String houseCode = streetAreaId + "-" + String.format("%03d", number); // 如 "10086-001"

        // ========== 新增：根据 area_id 自动查询街道坐标 ==========
        SysArea streetArea = sysAreaService.getById(streetAreaId);
        if (streetArea == null) {
            throw new IllegalStateException("街道不存在：area_id=" + streetAreaId);
        }
        // =======================================================

        HouseInfo house = new HouseInfo();
        house.setApplyNo(form.getApplyNo());
        house.setHouseCode(houseCode);
        String address = (form.getDetailAddress() != null && !form.getDetailAddress().trim().isEmpty())
                ? form.getDetailAddress()
                : ("待补充地址-" + form.getApplyNo());
        house.setAddress(address);
        house.setHouseType(form.getHouseType());
        house.setAreaId(streetAreaId);

        // ========== 新增：自动从街道坐标赋值 ==========
        house.setLng(streetArea.getLng());
        house.setLat(streetArea.getLat());
        house.setGeometry(streetArea.getGeometry());  // 如果有 geometry 字段
        // =============================================

        house.setCreateTime(LocalDateTime.now());
        house.setStatus(1);
        houseService.save(house);

        // 回写三位门牌号到 apply_form.house_id
        form.setHouseId(number);
        applyFormService.updateById(form);
    }
}
