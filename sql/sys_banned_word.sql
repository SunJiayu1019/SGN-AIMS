-- =====================================================================
-- 禁用词条 sys_banned_word：演示数据
-- 表结构见「数据库建表.md」第 8 节。含这些词的政策/公告将无法上传。
-- =====================================================================

CREATE TABLE IF NOT EXISTS sys_banned_word (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE COMMENT '禁用词',
    create_time DATETIME DEFAULT NOW()
) COMMENT='禁用词条表';

INSERT IGNORE INTO sys_banned_word (word) VALUES
  ('赌博'),
  ('暴力'),
  ('诈骗'),
  ('违法'),
  ('测试敏感词');
