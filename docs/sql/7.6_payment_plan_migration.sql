-- ============================================================
-- 7.6 付款计划 - 数据库迁移脚本
-- 执行日期: 2026-05-26
-- ============================================================

-- 1. payment 表新增字段：计划付款日期、付款条件、对账单关联ID
ALTER TABLE `payment`
    ADD COLUMN `recon_id` BIGINT NULL COMMENT '对账单ID' AFTER `invoice_no`,
    ADD COLUMN `schedule_date` DATE NULL COMMENT '计划付款日期' AFTER `receive_bank`,
    ADD COLUMN `payment_terms` VARCHAR(100) NULL COMMENT '付款条件，如：月结30天、款到发货' AFTER `schedule_date`;

-- 2. 创建付款审批记录表
CREATE TABLE IF NOT EXISTS `payment_approval` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `payment_id` BIGINT NOT NULL COMMENT '付款单ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '付款单号',
    `supplier_id` BIGINT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(128) NULL COMMENT '供应商名称',
    `payment_amount` DECIMAL(18,2) NULL COMMENT '付款金额',
    `approval_level` INT NOT NULL DEFAULT 1 COMMENT '审批级别：1=一级审批, 2=二级审批, 3=三级审批',
    `approval_status` INT NOT NULL DEFAULT 0 COMMENT '审批状态：0=待审批, 1=已通过, 2=已驳回',
    `approver_id` BIGINT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(64) NULL COMMENT '审批人姓名',
    `approve_remark` VARCHAR(500) NULL COMMENT '审批意见',
    `approve_time` DATETIME NULL COMMENT '审批时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` BIGINT NULL COMMENT '更新人',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除, 1=已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    INDEX `idx_payment_id` (`payment_id`),
    INDEX `idx_supplier_id` (`supplier_id`),
    INDEX `idx_approval_status` (`approval_status`),
    INDEX `idx_approval_level` (`approval_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付款审批记录表';