-- =====================================================================
-- 多级审批演示配置：为「门牌申请(new)」配置 3 级审批流程
-- 依赖：
--   1) 已执行 upgrade_process_node.sql（apply_process_node 含 apply_type 列）
--   2) auth_seed.sql 已插入 4 个管理员（按插入顺序：1=核心管理员, 2=管理员A, 3=管理员B, 4=管理员C）
-- 说明：
--   - new 申请走 3 级：第1级=管理员A(2)，第2级=管理员B(3)，第3级=管理员C(4)；
--   - 每级 audit_type=ONE（任一审核人通过即进入下一级）；
--   - reissue 申请配 1 级：管理员A(2)。
--   - area_id 固定 0（全局/总站）。
--   如管理员实际 id 不同，请按你库中的真实 id 调整 audit_user_ids。
-- =====================================================================

-- 先清掉旧的 new / reissue 配置，避免重复
DELETE FROM apply_process_node WHERE area_id = 0 AND apply_type IN ('new', 'reissue');

-- 门牌申请 new：3 级
INSERT INTO apply_process_node (area_id, apply_type, node_level, audit_user_ids, audit_type) VALUES
  (0, 'new', 1, '2', 'ONE'),
  (0, 'new', 2, '3', 'ONE'),
  (0, 'new', 3, '4', 'ONE');

-- 门牌补发 reissue：1 级
INSERT INTO apply_process_node (area_id, apply_type, node_level, audit_user_ids, audit_type) VALUES
  (0, 'reissue', 1, '2', 'ONE');

-- 验证：查看配置
-- SELECT * FROM apply_process_node WHERE area_id = 0 ORDER BY apply_type, node_level;
