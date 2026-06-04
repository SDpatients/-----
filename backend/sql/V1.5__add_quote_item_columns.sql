-- =============================================================
-- quote_item 表新增字段：支持报价明细行完整信息
-- =============================================================

ALTER TABLE `quote_item`
    ADD COLUMN `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称' AFTER `material_code`,
    ADD COLUMN `spec` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格' AFTER `material_name`,
    ADD COLUMN `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位' AFTER `spec`,
    ADD COLUMN `delivery_date` date NULL DEFAULT NULL COMMENT '交期' AFTER `tax_amount`,
    ADD COLUMN `payment_terms` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款条件' AFTER `delivery_date`;