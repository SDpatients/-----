-- =============================================================
-- 冲销调整表
-- =============================================================
DROP TABLE IF EXISTS `write_off`;
CREATE TABLE `write_off` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `write_off_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冲销单号',
    `notice_id` bigint NULL DEFAULT NULL COMMENT 'ASN通知单ID',
    `asn_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ASN号',
    `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
    `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单号',
    `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
    `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
    `write_off_type` varchar(50) NOT NULL COMMENT '冲销类型: 退货冲销/价格调整/数量差异调整/破损扣减/质量扣款/其他调整',
    `amount` decimal(18,2) NULL DEFAULT NULL COMMENT '冲销金额',
    `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冲销原因',
    `status` int NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已审核, 2-已驳回, 3-已取消',
    `write_off_time` datetime NULL DEFAULT NULL COMMENT '冲销时间',
    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_write_off_no`(`write_off_no` ASC) USING BTREE,
    INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
    INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE,
    INDEX `idx_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '冲销调整表' ROW_FORMAT = Dynamic;
