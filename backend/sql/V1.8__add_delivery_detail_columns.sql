-- 为 delivery_detail 表添加缺失字段
-- 用于支持发货通知单的完整信息存储

ALTER TABLE `delivery_detail`
    ADD COLUMN `case_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '箱号' AFTER `box_count`,
    ADD COLUMN `qty_per_case` int NULL DEFAULT NULL COMMENT '每箱数量' AFTER `case_no`,
    ADD COLUMN `barcode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条码' AFTER `qty_per_case`;
