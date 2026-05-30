-- =====================================================================
-- 可选：登录 / 审批功能的演示数据（角色 + 演示管理员账号）
-- 建议在"空库或尚无这些角色/账号时"执行一次。
-- 密码均为明文 123456，仅供本地联调；生产环境请改为加密存储。
-- =====================================================================

-- 1. 三种角色（普通注册用户会自动绑定 user；管理员需手动绑定）
INSERT INTO sys_role (role_name) VALUES ('coreAdmin'), ('normalAdmin'), ('user');

-- 2. 演示账号：1 个核心管理员 + 3 个普通管理员
INSERT INTO sys_user (phone, password, real_name, area_id) VALUES
  ('13900000000', '123456', '核心管理员', 0),
  ('13900000001', '123456', '管理员A',   1),
  ('13900000002', '123456', '管理员B',   2),
  ('13900000003', '123456', '管理员C',   3);

-- 3. 绑定角色（用子查询按手机号 / 角色名匹配，避免手写 id 出错）
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_name = 'coreAdmin'
WHERE u.phone = '13900000000';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON r.role_name = 'normalAdmin'
WHERE u.phone IN ('13900000001', '13900000002', '13900000003');

-- 说明：
--   - 登录后，前端根据返回的 role 决定跳转：coreAdmin/normalAdmin -> 后台；user -> 审批网站首页。
--   - "审批流程配置"仅核心管理员(13900000000)可保存；其余管理员只能查看。
--   - 审批门牌申请页：核心管理员可见全部申请；普通管理员仅见自己被指派的申请类型。
