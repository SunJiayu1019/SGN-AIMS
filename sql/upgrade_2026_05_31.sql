-- =====================================================================
-- 2026-05-31 结构升级脚本（配合本次后端实体重构执行）
-- 目标：让数据库与「数据库重制版」+ 本次保留字段的约定保持一致。
-- 可重复执行（已尽量使用 IF NOT EXISTS / 兼容写法）。
-- 注意：MySQL 8.0+ 支持 ADD COLUMN IF NOT EXISTS；若版本较低请去掉 IF NOT EXISTS 手动判断。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. apply_form：按你的选择「保留」申请人姓名与房屋类型两列。
--    新版基础表若未包含这两列，这里补回；并补齐 original_house_code / house_id。
-- ---------------------------------------------------------------------
ALTER TABLE apply_form
  ADD COLUMN IF NOT EXISTS applicant_name VARCHAR(30) NULL COMMENT '申请人姓名' AFTER user_id;

ALTER TABLE apply_form
  ADD COLUMN IF NOT EXISTS house_type VARCHAR(20) NULL COMMENT '房屋类型 house/shop/factory' AFTER original_house_code;

-- 原门牌号（补发用），新版应为 VARCHAR；若历史是 INT 请改类型
ALTER TABLE apply_form
  ADD COLUMN IF NOT EXISTS original_house_code VARCHAR(50) NULL COMMENT '补发时的原门牌号';

-- house_id：审批通过后分配的「三位门牌号」（整数 1..999），不再是 house_info 的外键
ALTER TABLE apply_form
  ADD COLUMN IF NOT EXISTS house_id INT NULL COMMENT '通过后分配的三位门牌号';

-- detail_address（新申请时房屋地址落在申请表）
ALTER TABLE apply_form
  ADD COLUMN IF NOT EXISTS detail_address VARCHAR(255) NULL COMMENT '房屋详细地址';

-- 若历史上 apply_form.house_id 存在指向 house_info(id) 的外键，请先删除，避免类型/语义冲突：
-- （下面语句按需手动执行，外键名以你库中实际为准）
-- ALTER TABLE apply_form DROP FOREIGN KEY <旧外键名>;

-- ---------------------------------------------------------------------
-- 2. house_info：补齐实体新增映射的列 apply_no / create_time。
--    apply_no 关联 apply_form.apply_no，门牌入库与「门牌排查」依赖它。
-- ---------------------------------------------------------------------
ALTER TABLE house_info
  ADD COLUMN IF NOT EXISTS apply_no VARCHAR(50) NULL COMMENT '关联的申请号' AFTER id;

ALTER TABLE house_info
  ADD COLUMN IF NOT EXISTS create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 门牌编号同一街道内唯一（与规则一致）：建议加复合唯一键
-- 若已存在同名约束会报错，可忽略或先删后建
ALTER TABLE house_info
  ADD UNIQUE KEY uk_housecode_area (house_code, area_id);

-- ---------------------------------------------------------------------
-- 3. portal_about.area_id：实体改为 INT，确保列类型为 INT。
-- ---------------------------------------------------------------------
ALTER TABLE portal_about
  MODIFY COLUMN area_id INT DEFAULT 0;

-- ---------------------------------------------------------------------
-- 说明：
--  - 角色名沿用代码现状 coreAdmin / normalAdmin / user（见 auth_seed.sql）。
--  - 用户管理「设为管理员」= 绑定 normalAdmin 角色；「取消」= 回退 user。
--  - 门牌编号规则：{街道area_id}-{三位号}，如 10086-001，审批通过时按街道自增生成。
-- =====================================================================
