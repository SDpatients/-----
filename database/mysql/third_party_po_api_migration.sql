-- ============================================
-- 第三方采购订单接口配置表 & 初始数据
-- ============================================

DROP TABLE IF EXISTS third_party_po_api_config;

CREATE TABLE third_party_po_api_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    api_type VARCHAR(30) NOT NULL COMMENT '接口类型(CREATE-新增采购订单,GET_LATEST-获取最新采购订单)',
    base_url VARCHAR(500) NOT NULL COMMENT '第三方接口地址',
    http_method VARCHAR(10) NOT NULL DEFAULT 'POST' COMMENT 'HTTP方法(GET,POST,PUT)',
    auth_type VARCHAR(30) NOT NULL DEFAULT 'NONE' COMMENT '鉴权方式(NONE,BASIC,BEARER,API_KEY)',
    auth_credentials TEXT DEFAULT NULL COMMENT '鉴权凭证(JSON)',
    request_headers TEXT DEFAULT NULL COMMENT '自定义请求头(JSON)',
    request_body_template TEXT DEFAULT NULL COMMENT '请求体模板(JSON,支持占位符)',
    timeout_seconds INT NOT NULL DEFAULT 30 COMMENT '超时时间(秒)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    enabled TINYINT NOT NULL DEFAULT 0 COMMENT '启用状态(0停用,1启用)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_api_type (api_type),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='第三方采购订单接口配置表';