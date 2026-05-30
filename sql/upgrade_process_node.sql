-- =====================================================================
-- 升级脚本：审批流程配置改为"按申请类型"配置
-- 为 apply_process_node 增加 apply_type 列，并把唯一键改为 (area_id, apply_type, node_level)
-- 在你现有的 approval_db 上执行即可（只改结构，不删数据）
-- =====================================================================
-- 说明：
--   原表唯一键是 uk_area_level (area_id, node_level)，只能按区域配一套流程。
--   现在系统要求"门牌申请(new)"与"门牌补发(reissue)"各自独立配置级数与审核人，
--   因此新增 apply_type 列，并把唯一键扩展为 (area_id, apply_type, node_level)。
--   本系统该配置为全局生效，area_id 固定使用 0（总站）。
-- =====================================================================

ALTER TABLE apply_process_node
    ADD COLUMN apply_type VARCHAR(20) NOT NULL DEFAULT 'new'
    COMMENT '申请类型 new/reissue' AFTER area_id;

-- 调整唯一键：先删旧的，再建新的
-- （若你的库里 uk_area_level 不存在或已删过，下面这行会报错，忽略即可）
ALTER TABLE apply_process_node DROP INDEX uk_area_level;

ALTER TABLE apply_process_node
    ADD UNIQUE KEY uk_area_type_level (area_id, apply_type, node_level);
