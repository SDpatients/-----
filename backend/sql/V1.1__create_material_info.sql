-- =============================================================
-- 物料主数据表
-- =============================================================
CREATE TABLE IF NOT EXISTS material_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    spec VARCHAR(200),
    unit VARCHAR(20) DEFAULT 'PCS',
    category VARCHAR(50),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_material_code UNIQUE (material_code)
);