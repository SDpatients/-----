-- ============================================================
-- 数据库对接修复 — 第三部分：寻源与供应商模块 迁移脚本
-- 对照基准：supplier_collaboration.sql
-- 执行日期：2026-05-29
-- ============================================================

-- 1. rfq_supplier（询价邀请供应商表）：添加缺失列
ALTER TABLE rfq_supplier
    ADD COLUMN supplier_name VARCHAR(100) DEFAULT NULL COMMENT '供应商名称' AFTER supplier_id,
    ADD COLUMN quote_id BIGINT DEFAULT NULL COMMENT '报价单ID' AFTER invite_status;

-- 2. quote（报价单表）：valid_until 类型从 DATE 改为 DATETIME
ALTER TABLE quote MODIFY COLUMN valid_until DATETIME DEFAULT NULL COMMENT '报价有效期';

-- 3. supplier_category（供应商分类表）：添加缺失的 remark 列
ALTER TABLE supplier_category
    ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER status;

-- ============================================================
-- 4. 创建缺失的数据库表
-- ============================================================

-- 4.1 quote_award（报价授标表）
DROP TABLE IF EXISTS `quote_award`;
CREATE TABLE `quote_award` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `quote_id` bigint NOT NULL COMMENT '报价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `award_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '授标金额',
  `award_tax_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税额',
  `award_currency` varchar(10) DEFAULT 'CNY' COMMENT '币种',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `order_no` varchar(50) NULL DEFAULT NULL COMMENT '关联订单号',
  `award_by` bigint NULL DEFAULT NULL COMMENT '授标人ID',
  `award_by_name` varchar(50) NULL DEFAULT NULL COMMENT '授标人姓名',
  `award_time` datetime NULL DEFAULT NULL COMMENT '授标时间',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rfq_id`(`rfq_id` ASC) USING BTREE,
  INDEX `idx_quote_id`(`quote_id` ASC) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报价授标表' ROW_FORMAT = Dynamic;

-- 4.2 quote_negotiation（报价谈判表）
DROP TABLE IF EXISTS `quote_negotiation`;
CREATE TABLE `quote_negotiation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_id` bigint NOT NULL COMMENT '报价单ID',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `round` int NOT NULL DEFAULT 1 COMMENT '谈判轮次',
  `initiator` varchar(20) DEFAULT NULL COMMENT '发起方(buyer/supplier)',
  `target_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '目标价格',
  `supplier_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '供应商报价',
  `buyer_remark` varchar(500) NULL DEFAULT NULL COMMENT '采购方备注',
  `supplier_remark` varchar(500) NULL DEFAULT NULL COMMENT '供应商备注',
  `negotiation_time` datetime NULL DEFAULT NULL COMMENT '谈判时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_quote_id`(`quote_id` ASC) USING BTREE,
  INDEX `idx_rfq_id`(`rfq_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报价谈判表' ROW_FORMAT = Dynamic;

-- 4.3 exchange_rate（汇率表）
DROP TABLE IF EXISTS `exchange_rate`;
CREATE TABLE `exchange_rate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_currency` varchar(10) NOT NULL COMMENT '源币种',
  `to_currency` varchar(10) NOT NULL COMMENT '目标币种',
  `rate` decimal(18, 6) NOT NULL COMMENT '汇率',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `source` varchar(50) NULL DEFAULT NULL COMMENT '数据来源',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_currency_date`(`from_currency` ASC, `to_currency` ASC, `effective_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '汇率表' ROW_FORMAT = Dynamic;