-- =============================================================
-- 物料主数据表 修复脚本
-- 使用场景：如果已存在旧表但缺少字段，或者表结构不完整
-- =============================================================

-- 1. 如果表已存在但字段不全，先尝试加字段（安全操作）
ALTER TABLE material_info 
  ADD COLUMN IF NOT EXISTS spec VARCHAR(200) AFTER material_name,
  ADD COLUMN IF NOT EXISTS unit VARCHAR(20) DEFAULT 'PCS' AFTER spec,
  ADD COLUMN IF NOT EXISTS category VARCHAR(50) AFTER unit,
  ADD COLUMN IF NOT EXISTS status TINYINT DEFAULT 1 AFTER category,
  ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 AFTER status,
  ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER deleted,
  ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER create_time,
  ADD COLUMN IF NOT EXISTS create_by BIGINT AFTER update_time,
  ADD COLUMN IF NOT EXISTS update_by BIGINT AFTER create_by,
  ADD COLUMN IF NOT EXISTS version INT DEFAULT 0 AFTER update_by;

-- 2. 如果表不存在或者完全重建
-- DROP TABLE IF EXISTS material_info;
-- CREATE TABLE material_info (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     material_code VARCHAR(50) NOT NULL,
--     material_name VARCHAR(100) NOT NULL,
--     spec VARCHAR(200),
--     unit VARCHAR(20) DEFAULT 'PCS',
--     category VARCHAR(50),
--     status TINYINT DEFAULT 1,
--     deleted TINYINT DEFAULT 0,
--     create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
--     update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
--     create_by BIGINT,
--     update_by BIGINT,
--     version INT DEFAULT 0,
--     CONSTRAINT uk_material_code UNIQUE (material_code)
-- );

-- 3. 插入测试数据（可选）
-- INSERT IGNORE INTO material_info (material_code, material_name, spec, unit, category, status)
-- VALUES
-- ('MAT001', '不锈钢板', '304 2.0mm', 'KG', '钢材', 1),
-- ('MAT002', '螺丝', 'M6x20', 'PCS', '紧固件', 1),
-- ('MAT003', '螺母', 'M6', 'PCS', '紧固件', 1);
