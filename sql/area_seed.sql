-- =====================================================================
-- 行政区划种子数据（山西省 → 市 → 区/县）
-- 说明：
--   1. level：1=省 2=市 3=区/县 4=街道
--   2. parent_id：上级区域 id；省的 parent_id = 0
--   3. code：本系统约定用「拼音」作为编码，方便前端 city 入参与之对应
--           （山西省=shanxi，太原市=taiyuan，吕梁市=lvliang，晋中市=jinzhong）
--   4. 用 INSERT ... ON DUPLICATE KEY 兜底，code 唯一，可重复执行
--   5. 这里显式写死 id，方便后续 mock 数据（house_info/apply_form/portal_news）
--      用 area_id=1/2/3 时仍然对得上（1=太原 2=吕梁 3=晋中，沿用旧约定）。
-- =====================================================================

-- 省（顶级，id 用 100，避免和旧的 1/2/3 冲突）
INSERT INTO sys_area (id, name, code, level, parent_id, parent_path, sort) VALUES
(100, '山西省', 'shanxi', 1, 0, '', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), level=VALUES(level), parent_id=VALUES(parent_id);

-- 市（parent_id = 100 山西省）。沿用旧约定：太原=1、吕梁=2、晋中=3
INSERT INTO sys_area (id, name, code, level, parent_id, parent_path, sort) VALUES
(1, '太原市', 'taiyuan',  2, 100, '0,100', 1),
(2, '吕梁市', 'lvliang',  2, 100, '0,100', 2),
(3, '晋中市', 'jinzhong', 2, 100, '0,100', 3)
ON DUPLICATE KEY UPDATE name=VALUES(name), level=VALUES(level), parent_id=VALUES(parent_id);

-- 区/县（parent_id = 所属市 id）
INSERT INTO sys_area (id, name, code, level, parent_id, parent_path, sort) VALUES
-- 太原市(1) 下属
(11, '杏花岭区', 'xinghualing', 3, 1, '0,100,1', 1),
(12, '迎泽区',   'yingze',      3, 1, '0,100,1', 2),
(13, '小店区',   'xiaodian',    3, 1, '0,100,1', 3),
(14, '尖草坪区', 'jiancaoping', 3, 1, '0,100,1', 4),
(15, '万柏林区', 'wanbailin',   3, 1, '0,100,1', 5),
-- 吕梁市(2) 下属
(21, '离石区', 'lishi',   3, 2, '0,100,2', 1),
(22, '孝义市', 'xiaoyi',  3, 2, '0,100,2', 2),
(23, '汾阳市', 'fenyang', 3, 2, '0,100,2', 3),
-- 晋中市(3) 下属
(31, '榆次区', 'yuci',     3, 3, '0,100,3', 1),
(32, '介休市', 'jiexiu',   3, 3, '0,100,3', 2),
(33, '太谷区', 'taigu',    3, 3, '0,100,3', 3)
ON DUPLICATE KEY UPDATE name=VALUES(name), level=VALUES(level), parent_id=VALUES(parent_id);
