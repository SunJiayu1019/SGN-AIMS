-- =====================================================================
-- 升级脚本：为 apply_form 补充"群众申请 -> 审批 -> 回显"衔接所需字段
-- 在你现有的 approval_db 上直接执行即可（只新增列，不动数据）
-- =====================================================================
-- 说明：
--   新申请(apply_type = 'new')时，门牌还没生成，house_info 里没有这条记录，
--   所以申请人填的"房屋类型 / 详细地址 / 姓名"必须落在 apply_form 上，
--   否则提交后这些信息无处存放，管理员审批页也拿不到，衔接就断了。
--   (area_id 列你的建表语句里已经有了，这里不再重复添加)
-- =====================================================================

ALTER TABLE apply_form
    ADD COLUMN applicant_name VARCHAR(30)  NULL COMMENT '申请人姓名' AFTER user_id,
    ADD COLUMN house_type     VARCHAR(20)  NULL COMMENT '房屋类型 house住宅/shop商铺/factory厂房' AFTER house_id,
    ADD COLUMN detail_address VARCHAR(255) NULL COMMENT '房屋详细地址' AFTER house_type;

-- 如果你之前不小心已经手动加过 area_id 以外的列，导致上面报 "Duplicate column"，
-- 可以把已存在的那几行删掉、只保留缺的那几列再执行。
