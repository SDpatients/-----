-- =============================================================
-- 物料主数据表 修复脚本（兼容 MySQL 5.7+）
-- =============================================================

-- 方案1：先查看当前表结构（确认当前状态）
-- SHOW CREATE TABLE material_info;

-- 方案2：直接重建表（最稳妥）
-- 注意：如果表中已有数据会被清空！
DROP TABLE IF EXISTS material_info;
CREATE TABLE material_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    spec VARCHAR(200),
    unit VARCHAR(20) DEFAULT 'PCS',
    category VARCHAR(50),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_material_code UNIQUE (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主数据表';

-- 插入测试数据
INSERT IGNORE INTO material_info (material_code, material_name, spec, unit, category, status)
VALUES
('MAT001', '不锈钢板', '304 2.0mm', 'KG', '钢材', 1),
('MAT002', '螺丝', 'M6x20', 'PCS', '紧固件', 1),
('MAT003', '螺母', 'M6', 'PCS', '紧固件', 1),
('MAT004', '垫片', 'φ10mm', 'PCS', '紧固件', 1),
('MAT005', '角铁', '30x30x3mm', 'M', '钢材', 1);
