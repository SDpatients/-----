-- =============================================================
-- quote_item 表新增 remark 字段：支持报价明细行备注
-- =============================================================

ALTER TABLE `quote_item`
    ADD COLUMN `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `payment_terms`;
