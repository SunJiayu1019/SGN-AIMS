-- =====================================================================
-- 系统操作日志：建表 + 权限项 + 演示数据
-- 在 approval_db 库执行一次即可。可重复执行（建表用 IF NOT EXISTS）。
-- =====================================================================

-- 1. 建表 ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_log (
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    operator_id INT          DEFAULT NULL COMMENT '操作人ID',
    operator    VARCHAR(30)  DEFAULT '系统' COMMENT '操作人名称',
    action      VARCHAR(30)  COMMENT '操作类型 登录/注册/新增/修改/删除/审批',
    target      VARCHAR(50)  COMMENT '操作对象 如 apply_form/house_info',
    detail      VARCHAR(500) COMMENT '变更说明',
    ip          VARCHAR(50)  DEFAULT NULL COMMENT '操作来源IP',
    create_time DATETIME     DEFAULT NOW() COMMENT '创建时间',
    INDEX idx_action (action),
    INDEX idx_time (create_time)
) COMMENT='系统操作日志';

-- 2. 注册「系统日志」菜单权限（rbac_seed.sql 里若已建可忽略，这里用 IGNORE 去重） --
INSERT IGNORE INTO sys_permission (perm_name, perm_code, parent_id, type)
VALUES ('系统日志', 'log:view', 0, 'menu');

-- 3. 演示日志数据 --------------------------------------------------------
INSERT INTO sys_log (operator_id, operator, action, target, detail, ip, create_time) VALUES
  (1, '核心管理员', '登录', 'sys_user',   '用户登录，角色：coreAdmin',                '127.0.0.1', NOW() - INTERVAL 2 DAY),
  (2, '管理员A',   '审批', 'apply_form', '通过申请单 AP202605280001（新装门牌）',     '127.0.0.1', NOW() - INTERVAL 2 DAY),
  (2, '管理员A',   '审批', 'apply_form', '驳回申请单 AP202605280002（材料不全）',     '127.0.0.1', NOW() - INTERVAL 1 DAY),
  (1, '核心管理员', '新增', 'house_info', '新增门牌 太原市迎泽区某街道001号',          '127.0.0.1', NOW() - INTERVAL 1 DAY),
  (1, '核心管理员', '修改', 'house_info', '修改门牌 编号 H10023 的地址信息',           '127.0.0.1', NOW() - INTERVAL 20 HOUR),
  (3, '管理员B',   '删除', 'house_info', '删除作废门牌 编号 H10099',                  '127.0.0.1', NOW() - INTERVAL 6 HOUR),
  (1, '核心管理员', '修改', 'sys_role',   '为 normalAdmin 角色调整权限分配',           '127.0.0.1', NOW() - INTERVAL 3 HOUR),
  (2, '管理员A',   '登录', 'sys_user',   '用户登录，角色：normalAdmin',               '127.0.0.1', NOW() - INTERVAL 1 HOUR),
  (1, '核心管理员', '修改', 'site_config','更新审批网站站点配置（页脚信息）',          '127.0.0.1', NOW() - INTERVAL 30 MINUTE),
  (1, '核心管理员', '审批', 'apply_form', '通过申请单 AP202605300003（补办门牌）',     '127.0.0.1', NOW());
