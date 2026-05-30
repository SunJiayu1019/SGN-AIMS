-- =====================================================================
-- 角色权限管理 演示数据：权限清单（菜单 + 按钮）+ 默认的角色权限分配
-- 可重复执行：perm_code / (role_id,perm_id) 均有唯一键，配合 INSERT IGNORE 去重。
-- 依赖：请先执行 auth_seed.sql（保证 coreAdmin / normalAdmin / user 三个角色已存在）。
-- =====================================================================

-- 1. 菜单级权限（parent_id 先置 0，稍后用子查询回填）
INSERT IGNORE INTO sys_permission (perm_name, perm_code, parent_id, type) VALUES
  ('统计分析',     'stat:view',    0, 'menu'),
  ('审批申请',     'audit:view',   0, 'menu'),
  ('门牌管理',     'house:view',   0, 'menu'),
  ('行政区划管理', 'area:view',    0, 'menu'),
  ('街道查询',     'street:view',  0, 'menu'),
  ('审批流程配置', 'process:view', 0, 'menu'),
  ('角色权限管理', 'role:view',    0, 'menu'),
  ('审批网站管理', 'website:view', 0, 'menu'),
  ('系统日志',     'log:view',     0, 'menu');

-- 2. 按钮级权限（parent_id 先置 0）
INSERT IGNORE INTO sys_permission (perm_name, perm_code, parent_id, type) VALUES
  ('门牌新增',     'house:add',    0, 'button'),
  ('门牌编辑',     'house:edit',   0, 'button'),
  ('门牌删除',     'house:delete', 0, 'button'),
  ('门牌导出PDF',  'house:export', 0, 'button'),
  ('审批操作',     'audit:do',     0, 'button'),
  ('流程配置保存', 'process:save', 0, 'button'),
  ('权限分配',     'role:assign',  0, 'button'),
  ('网站内容编辑', 'website:edit', 0, 'button');

-- 3. 回填按钮的 parent_id（用派生表自联，规避 MySQL 同表更新限制）
UPDATE sys_permission c
  JOIN (SELECT id, perm_code FROM sys_permission) p ON p.perm_code = 'house:view'
  SET c.parent_id = p.id
  WHERE c.perm_code IN ('house:add', 'house:edit', 'house:delete', 'house:export');

UPDATE sys_permission c
  JOIN (SELECT id, perm_code FROM sys_permission) p ON p.perm_code = 'audit:view'
  SET c.parent_id = p.id
  WHERE c.perm_code IN ('audit:do');

UPDATE sys_permission c
  JOIN (SELECT id, perm_code FROM sys_permission) p ON p.perm_code = 'process:view'
  SET c.parent_id = p.id
  WHERE c.perm_code IN ('process:save');

UPDATE sys_permission c
  JOIN (SELECT id, perm_code FROM sys_permission) p ON p.perm_code = 'role:view'
  SET c.parent_id = p.id
  WHERE c.perm_code IN ('role:assign');

UPDATE sys_permission c
  JOIN (SELECT id, perm_code FROM sys_permission) p ON p.perm_code = 'website:view'
  SET c.parent_id = p.id
  WHERE c.perm_code IN ('website:edit');

-- 4. 默认分配：核心管理员拥有全部权限
INSERT IGNORE INTO sys_role_perm (role_id, perm_id)
SELECT r.id, p.id
FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_name = 'coreAdmin';

-- 5. 默认分配：普通管理员拥有部分权限（无删除 / 流程配置 / 角色权限 / 日志）
INSERT IGNORE INTO sys_role_perm (role_id, perm_id)
SELECT r.id, p.id
FROM sys_role r JOIN sys_permission p
  ON p.perm_code IN (
    'stat:view', 'audit:view', 'audit:do',
    'house:view', 'house:add', 'house:edit', 'house:export',
    'area:view', 'street:view',
    'website:view', 'website:edit'
  )
WHERE r.role_name = 'normalAdmin';

-- user 角色默认不分配后台权限（普通用户只用前台审批网站）。
