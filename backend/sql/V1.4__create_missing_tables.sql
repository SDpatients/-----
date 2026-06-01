-- =============================================================
-- 缺失表的建表脚本
-- 修复内容：为 5 个已有 Java Entity 但无对应数据库表的情况建表
-- =============================================================

-- 1. purchase_order_erp_mapping（ERP订单号映射表）
DROP TABLE IF EXISTS `purchase_order_erp_mapping`;
CREATE TABLE `purchase_order_erp_mapping` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `order_id` bigint NOT NULL COMMENT '采购订单ID',
    `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
    `erp_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ERP订单号',
    `erp_system_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ERP系统类型',
    `sync_time` datetime NULL DEFAULT NULL COMMENT '同步时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
    INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
    INDEX `idx_erp_order_no`(`erp_order_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ERP订单号映射表' ROW_FORMAT = Dynamic;

-- 2. order_detail_version（订单明细版本表）
DROP TABLE IF EXISTS `order_detail_version`;
CREATE TABLE `order_detail_version` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `order_id` bigint NOT NULL COMMENT '采购订单ID',
    `order_detail_id` bigint NOT NULL COMMENT '订单明细ID',
    `version_no` int NULL DEFAULT NULL COMMENT '版本号',
    `change_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更类型',
    `quantity` decimal(18,4) NULL DEFAULT NULL COMMENT '数量',
    `unit_price` decimal(18,2) NULL DEFAULT NULL COMMENT '单价',
    `amount` decimal(18,2) NULL DEFAULT NULL COMMENT '金额',
    `delivery_date` date NULL DEFAULT NULL COMMENT '交期',
    `snapshot_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '快照JSON',
    `change_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更原因',
    `change_by` bigint NULL DEFAULT NULL COMMENT '变更人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_order_detail_id`(`order_detail_id` ASC) USING BTREE,
    INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细版本表' ROW_FORMAT = Dynamic;

-- 3. delivery_feedback（交货反馈主表）
DROP TABLE IF EXISTS `delivery_feedback`;
CREATE TABLE `delivery_feedback` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `order_id` bigint NOT NULL COMMENT '采购订单ID',
    `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
    `supplier_id` bigint NOT NULL COMMENT '供应商ID',
    `feedback_status` int NULL DEFAULT NULL COMMENT '反馈状态',
    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `buyer_confirm_by` bigint NULL DEFAULT NULL COMMENT '采购方确认人ID',
    `buyer_confirm_time` datetime NULL DEFAULT NULL COMMENT '采购方确认时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
    INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '交货反馈主表' ROW_FORMAT = Dynamic;

-- 4. delivery_feedback_line（交货反馈行表）
DROP TABLE IF EXISTS `delivery_feedback_line`;
CREATE TABLE `delivery_feedback_line` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `feedback_id` bigint NOT NULL COMMENT '反馈主表ID',
    `order_detail_id` bigint NULL DEFAULT NULL COMMENT '订单明细ID',
    `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料编码',
    `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
    `promised_delivery_date` date NULL DEFAULT NULL COMMENT '承诺交期',
    `planned_quantity` decimal(18,4) NULL DEFAULT NULL COMMENT '计划数量',
    `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '批号',
    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_feedback_id`(`feedback_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '交货反馈行表' ROW_FORMAT = Dynamic;

-- 5. three_way_match（三单匹配表）
DROP TABLE IF EXISTS `three_way_match`;
CREATE TABLE `three_way_match` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `recon_id` bigint NULL DEFAULT NULL COMMENT '对账单ID',
    `order_id` bigint NULL DEFAULT NULL COMMENT '采购订单ID',
    `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
    `order_amount` decimal(18,2) NULL DEFAULT NULL COMMENT '订单金额',
    `receipt_id` bigint NULL DEFAULT NULL COMMENT '收货记录ID',
    `receipt_amount` decimal(18,2) NULL DEFAULT NULL COMMENT '收货金额',
    `invoice_id` bigint NULL DEFAULT NULL COMMENT '发票ID',
    `invoice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发票号码',
    `invoice_amount` decimal(18,2) NULL DEFAULT NULL COMMENT '发票金额',
    `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
    `match_result` int NULL DEFAULT NULL COMMENT '匹配结果: 0=完全匹配 1=部分匹配 2=不匹配',
    `diff_amount` decimal(18,2) NULL DEFAULT NULL COMMENT '差异金额',
    `diff_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '差异原因',
    `match_time` datetime NULL DEFAULT NULL COMMENT '匹配时间',
    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_recon_id`(`recon_id` ASC) USING BTREE,
    INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
    INDEX `idx_invoice_id`(`invoice_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '三单匹配表' ROW_FORMAT = Dynamic;