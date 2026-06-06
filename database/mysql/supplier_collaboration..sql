/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : supplier_collaboration

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 06/06/2026 09:28:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for appeal
-- ----------------------------
DROP TABLE IF EXISTS `appeal`;
CREATE TABLE `appeal`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `appeal_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申诉编号',
  `ncr_id` bigint NULL DEFAULT NULL COMMENT 'NCR ID',
  `inspection_id` bigint NULL DEFAULT NULL COMMENT '检验单ID',
  `deduction_id` bigint NULL DEFAULT NULL COMMENT '扣款单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料名称',
  `appeal_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申诉原因',
  `appeal_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申诉说明',
  `adjust_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '调整金额',
  `appeal_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已提交,2审核通过,3审核驳回)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `reviewer` bigint NULL DEFAULT NULL COMMENT '审核人',
  `reviewer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appeal_no`(`appeal_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_time`(`supplier_id` ASC, `appeal_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_ncr_id`(`ncr_id` ASC) USING BTREE,
  INDEX `idx_deduction_id`(`deduction_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '质检申诉联动扣款表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_status_track
-- ----------------------------
DROP TABLE IF EXISTS `biz_status_track`;
CREATE TABLE `biz_status_track`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型(delivery_notice/quality_inspection/reconciliation/receipt_record)',
  `business_id` bigint NOT NULL COMMENT '业务ID',
  `before_status` tinyint NULL DEFAULT NULL COMMENT '变更前状态',
  `after_status` tinyint NULL DEFAULT NULL COMMENT '变更后状态',
  `track_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轨迹说明',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_operator_time`(`operator` ASC, `operate_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2063051605938356226 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通用业务状态轨迹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for deduction
-- ----------------------------
DROP TABLE IF EXISTS `deduction`;
CREATE TABLE `deduction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `deduction_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '扣款单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `source_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源类型',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源ID',
  `deduction_type` tinyint NOT NULL COMMENT '扣款类型(1质量,2延期,3短交,4其他)',
  `deduction_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '扣款金额',
  `deduction_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '扣款原因',
  `deduction_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已提交,2已确认,3有异议,4已入账)',
  `recon_id` bigint NULL DEFAULT NULL COMMENT '对账单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_deduction_no`(`deduction_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_time`(`supplier_id` ASC, `deduction_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '扣款单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for delivery_barcode
-- ----------------------------
DROP TABLE IF EXISTS `delivery_barcode`;
CREATE TABLE `delivery_barcode`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT 'ASN ID',
  `package_id` bigint NULL DEFAULT NULL COMMENT '包装ID',
  `barcode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '条码',
  `barcode_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条码类型',
  `print_count` int NOT NULL DEFAULT 0 COMMENT '打印次数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
  INDEX `idx_package_id`(`package_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '送货条码表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for delivery_detail
-- ----------------------------
DROP TABLE IF EXISTS `delivery_detail`;
CREATE TABLE `delivery_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT 'ASN ID',
  `order_detail_id` bigint NULL DEFAULT NULL COMMENT '订单明细ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料名称',
  `material_spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `plan_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '计划送货数量',
  `actual_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '实际送货数量',
  `received_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '已收货数量',
  `qualified_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '合格数量',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '批次号',
  `production_date` date NULL DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date NULL DEFAULT NULL COMMENT '过期日期/有效期至',
  `box_count` int NOT NULL DEFAULT 0 COMMENT '箱数',
  `case_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '箱号',
  `qty_per_case` int NULL DEFAULT NULL COMMENT '每箱数量',
  `barcode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条码',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
  INDEX `idx_order_detail_id`(`order_detail_id` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062813794748383234 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ASN送货明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for delivery_feedback
-- ----------------------------
DROP TABLE IF EXISTS `delivery_feedback`;
CREATE TABLE `delivery_feedback`  (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '交货反馈主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for delivery_feedback_line
-- ----------------------------
DROP TABLE IF EXISTS `delivery_feedback_line`;
CREATE TABLE `delivery_feedback_line`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `feedback_id` bigint NOT NULL COMMENT '反馈主表ID',
  `order_detail_id` bigint NULL DEFAULT NULL COMMENT '订单明细ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `promised_delivery_date` date NULL DEFAULT NULL COMMENT '承诺交期',
  `planned_quantity` decimal(18, 4) NULL DEFAULT NULL COMMENT '计划数量',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '交货反馈行表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for delivery_label
-- ----------------------------
DROP TABLE IF EXISTS `delivery_label`;
CREATE TABLE `delivery_label`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `label_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签编号',
  `notice_id` bigint NOT NULL COMMENT 'ASN ID',
  `delivery_detail_id` bigint NULL DEFAULT NULL COMMENT 'ASN明细ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料编码',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '批次号',
  `package_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '箱号',
  `pallet_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '托盘号',
  `package_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '包装数量',
  `qr_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '二维码内容',
  `print_count` int NOT NULL DEFAULT 0 COMMENT '打印次数',
  `last_print_time` datetime NULL DEFAULT NULL COMMENT '最后打印时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_label_no`(`label_no` ASC) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
  INDEX `idx_package_no`(`package_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '送货标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for delivery_notice
-- ----------------------------
DROP TABLE IF EXISTS `delivery_notice`;
CREATE TABLE `delivery_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ASN单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `plan_delivery_date` date NULL DEFAULT NULL COMMENT '计划送货日期',
  `actual_delivery_date` date NULL DEFAULT NULL COMMENT '实际送货日期',
  `delivery_status` tinyint NOT NULL DEFAULT 0 COMMENT 'ASN状态(0草稿,1已提交,2在途,3部分收货,4已收货,5已取消)',
  `delivery_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '送货方式',
  `delivery_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物流公司',
  `delivery_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物流单号',
  `driver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '司机姓名',
  `driver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '司机电话',
  `vehicle_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车牌号',
  `delivery_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '送货地址',
  `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `batch_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '批号',
  `production_date` date NULL DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date NULL DEFAULT NULL COMMENT '有效期至',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `arrive_time` datetime NULL DEFAULT NULL COMMENT '到达时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `total_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '发货总金额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'CNY' COMMENT '币种',
  `tax_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税额',
  `net_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '净额',
  `payment_status` int NULL DEFAULT 0 COMMENT '付款状态: 0-未付款 1-部分付款 2-已付款',
  `reconciliation_status` int NULL DEFAULT 0 COMMENT '对账状态: 0-待对账 1-对账中 2-已对账 3-有差异',
  `warehouse` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货仓库',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_no`(`notice_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_date`(`supplier_id` ASC, `delivery_status` ASC, `plan_delivery_date` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062813794685468675 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ASN送货通知单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for delivery_package
-- ----------------------------
DROP TABLE IF EXISTS `delivery_package`;
CREATE TABLE `delivery_package`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT 'ASN ID',
  `package_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '包装编号',
  `package_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '包装类型',
  `weight` decimal(18, 4) NULL DEFAULT NULL COMMENT '重量(kg)',
  `volume` decimal(18, 4) NULL DEFAULT NULL COMMENT '体积(m³)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_package_no`(`package_no` ASC) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '送货包装表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for delivery_package_detail
-- ----------------------------
DROP TABLE IF EXISTS `delivery_package_detail`;
CREATE TABLE `delivery_package_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `package_id` bigint NOT NULL COMMENT '包装ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_package_id`(`package_id` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '送货包装明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for delivery_plan
-- ----------------------------
DROP TABLE IF EXISTS `delivery_plan`;
CREATE TABLE `delivery_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_detail_id` bigint NOT NULL COMMENT '订单明细ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `plan_date` date NOT NULL COMMENT '计划交付日期',
  `plan_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '计划数量',
  `promise_date` date NULL DEFAULT NULL COMMENT '承诺日期',
  `plan_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待确认,1已确认,2已发货,3已完成,4已取消)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_detail_date`(`order_detail_id` ASC, `plan_date` ASC) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `plan_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '交付计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for delivery_template
-- ----------------------------
DROP TABLE IF EXISTS `delivery_template`;
CREATE TABLE `delivery_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `header_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '页头配置(JSON)',
  `footer_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '页脚配置(JSON)',
  `column_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '列配置(JSON)',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`template_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '送货模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for eight_d_report
-- ----------------------------
DROP TABLE IF EXISTS `eight_d_report`;
CREATE TABLE `eight_d_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '8D报告编号',
  `ncr_id` bigint NOT NULL COMMENT 'NCR ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `d1_team` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D1团队',
  `d2_problem` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D2问题描述',
  `d3_containment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D3遏制措施',
  `d4_root_cause` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D4根因分析',
  `d5_corrective_action` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D5纠正措施',
  `d6_validate_action` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D6验证措施',
  `d7_prevent_action` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D7预防措施',
  `d8_close_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'D8关闭总结',
  `due_date` date NULL DEFAULT NULL COMMENT '截止日期',
  `current_step` tinyint NULL DEFAULT 1 COMMENT '当前阶段(1D1,2D2,3D3,4D4,5D5,6D6,7D7,8D8)',
  `step_due_date` date NULL DEFAULT NULL COMMENT '当前阶段截止日期',
  `report_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已提交,2审核中,3退回,4已关闭)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_report_no`(`report_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_ncr_id`(`ncr_id` ASC) USING BTREE,
  INDEX `idx_supplier_status_due`(`supplier_id` ASC, `report_status` ASC, `due_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '8D整改报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exchange_rate
-- ----------------------------
DROP TABLE IF EXISTS `exchange_rate`;
CREATE TABLE `exchange_rate`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源币种',
  `to_currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标币种',
  `rate` decimal(18, 6) NOT NULL COMMENT '汇率',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据来源',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_currency_date`(`from_currency` ASC, `to_currency` ASC, `effective_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '汇率表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for forecast_demand
-- ----------------------------
DROP TABLE IF EXISTS `forecast_demand`;
CREATE TABLE `forecast_demand`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `demand_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预测编号',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `demand_date` date NOT NULL COMMENT '需求日期',
  `demand_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '需求数量',
  `demand_type` tinyint NOT NULL DEFAULT 1 COMMENT '需求类型(1预测,2JIT,3VMI补货)',
  `demand_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待处理,1已发布,2已响应,3已关闭)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_demand_no`(`demand_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_date`(`supplier_id` ASC, `demand_status` ASC, `demand_date` ASC) USING BTREE,
  INDEX `idx_material_date`(`material_code` ASC, `demand_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '需求预测表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for inspection_standard
-- ----------------------------
DROP TABLE IF EXISTS `inspection_standard`;
CREATE TABLE `inspection_standard`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `standard_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标准编号',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `standard_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '检验标准名称',
  `sample_rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '抽样规则',
  `inspection_strategy` tinyint NULL DEFAULT 1 COMMENT '检验策略(0免检,1抽检,2全检)',
  `sample_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '抽检比例',
  `acceptance_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '合格标准率',
  `version_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0停用,1启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_standard_no`(`standard_no` ASC) USING BTREE,
  INDEX `idx_material_status`(`material_code` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检验标准表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for inspection_standard_item
-- ----------------------------
DROP TABLE IF EXISTS `inspection_standard_item`;
CREATE TABLE `inspection_standard_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `standard_id` bigint NOT NULL COMMENT '标准ID',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '检验项目',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类型(1定性,2定量)',
  `standard_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标准值',
  `upper_limit` decimal(18, 4) NULL DEFAULT NULL COMMENT '上限',
  `lower_limit` decimal(18, 4) NULL DEFAULT NULL COMMENT '下限',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `required` tinyint NOT NULL DEFAULT 1 COMMENT '是否必检',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_standard_sort`(`standard_id` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检验标准项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for integration_endpoint
-- ----------------------------
DROP TABLE IF EXISTS `integration_endpoint`;
CREATE TABLE `integration_endpoint`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `endpoint_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '端点编码',
  `endpoint_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '端点名称',
  `system_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '系统类型(ERP,WMS,MES,FINANCE,OCR)',
  `integration_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '集成方式(REST,MQ,FILE,DB,EDI)',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础地址',
  `auth_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '认证类型',
  `timeout_ms` int NOT NULL DEFAULT 30000 COMMENT '超时时间毫秒',
  `retry_limit` int NOT NULL DEFAULT 3 COMMENT '重试次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_endpoint_code`(`endpoint_code` ASC) USING BTREE,
  INDEX `idx_system_status`(`system_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '集成端点配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for integration_log
-- ----------------------------
DROP TABLE IF EXISTS `integration_log`;
CREATE TABLE `integration_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路ID',
  `endpoint_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '端点编码',
  `system_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '系统类型',
  `interface_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口编码',
  `direction` tinyint NOT NULL COMMENT '方向(1入站,2出站)',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `request_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求摘要',
  `response_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应摘要',
  `result_status` tinyint NOT NULL DEFAULT 0 COMMENT '结果(0失败,1成功)',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `cost_ms` int NULL DEFAULT NULL COMMENT '耗时毫秒',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_interface_status_time`(`interface_code` ASC, `result_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062376293382590466 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '集成调用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for integration_sync_task
-- ----------------------------
DROP TABLE IF EXISTS `integration_sync_task`;
CREATE TABLE `integration_sync_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '同步任务号',
  `system_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '系统类型',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务类型',
  `external_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外部单号',
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '事件类型',
  `payload` json NULL COMMENT '任务内容',
  `task_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待处理,1处理中,2成功,3失败,4忽略)',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `next_retry_time` datetime NULL DEFAULT NULL COMMENT '下次重试时间',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_no`(`task_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_external_event`(`external_no` ASC, `event_type` ASC) USING BTREE,
  INDEX `idx_type_status_time`(`task_type` ASC, `task_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_retry_time`(`next_retry_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '集成同步任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for invoice
-- ----------------------------
DROP TABLE IF EXISTS `invoice`;
CREATE TABLE `invoice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `invoice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发票号码',
  `invoice_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发票代码',
  `invoice_type` tinyint NOT NULL DEFAULT 1 COMMENT '发票类型',
  `recon_id` bigint NULL DEFAULT NULL COMMENT '对账单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `tax_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '税号',
  `invoice_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '发票金额',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税额',
  `tax_rate` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '税率',
  `invoice_date` date NULL DEFAULT NULL COMMENT '开票日期',
  `invoice_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待开票,1已上传,2已验真,3已认证,4已作废)',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '收到时间',
  `certify_time` datetime NULL DEFAULT NULL COMMENT '认证时间',
  `void_time` datetime NULL DEFAULT NULL COMMENT '作废时间',
  `void_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作废原因',
  `file_id` bigint NULL DEFAULT NULL COMMENT '发票附件ID',
  `ocr_status` tinyint NOT NULL DEFAULT 0 COMMENT 'OCR状态(0未识别,1识别中,2成功,3失败)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invoice_tax`(`invoice_no` ASC, `tax_number` ASC) USING BTREE,
  INDEX `idx_recon_id`(`recon_id` ASC) USING BTREE,
  INDEX `idx_supplier_status_date`(`supplier_id` ASC, `invoice_status` ASC, `invoice_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '发票表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for material_info
-- ----------------------------
DROP TABLE IF EXISTS `material_info`;
CREATE TABLE `material_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `spec` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PCS',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `deleted` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `version` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_code`(`material_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2061617341636050947 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物料主数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_event_log
-- ----------------------------
DROP TABLE IF EXISTS `message_event_log`;
CREATE TABLE `message_event_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件ID',
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件类型',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'supplier-collaboration' COMMENT '事件来源',
  `trace_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `business_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务单号',
  `payload` json NULL COMMENT '事件内容',
  `publish_status` tinyint NOT NULL DEFAULT 0 COMMENT '发布状态(0待发布,1成功,2失败)',
  `consume_status` tinyint NOT NULL DEFAULT 0 COMMENT '消费状态(0待消费,1成功,2失败,3忽略)',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `occurred_at` datetime NOT NULL COMMENT '发生时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_event_id`(`event_id` ASC) USING BTREE,
  INDEX `idx_event_status_time`(`event_type` ASC, `publish_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '事件消息日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_notice
-- ----------------------------
DROP TABLE IF EXISTS `message_notice`;
CREATE TABLE `message_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知编号',
  `receiver_user_id` bigint NULL DEFAULT NULL COMMENT '接收用户ID',
  `receiver_supplier_id` bigint NULL DEFAULT NULL COMMENT '接收供应商ID',
  `channel` tinyint NOT NULL DEFAULT 1 COMMENT '通道(1站内信,2邮件,3短信,4企微)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `send_status` tinyint NOT NULL DEFAULT 0 COMMENT '发送状态(0待发送,1成功,2失败)',
  `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '阅读状态(0未读,1已读)',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_no`(`notice_no` ASC) USING BTREE,
  INDEX `idx_user_read_time`(`receiver_user_id` ASC, `read_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_supplier_time`(`receiver_supplier_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2063048222934384643 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_template
-- ----------------------------
DROP TABLE IF EXISTS `message_template`;
CREATE TABLE `message_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `channel` tinyint NOT NULL DEFAULT 1 COMMENT '推送渠道(1站内信,2邮件,3短信,4企业微信)',
  `title_template` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题模板',
  `content_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容模板',
  `variables` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变量列表(逗号分隔)',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0停用,1启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`template_code` ASC) USING BTREE,
  INDEX `idx_channel_status`(`channel` ASC, `status` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for nonconformance_report
-- ----------------------------
DROP TABLE IF EXISTS `nonconformance_report`;
CREATE TABLE `nonconformance_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ncr_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'NCR单号',
  `inspection_id` bigint NULL DEFAULT NULL COMMENT '检验单ID',
  `receipt_id` bigint NULL DEFAULT NULL COMMENT '收货记录ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `unqualified_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '不合格数量',
  `problem_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题描述',
  `severity` tinyint NOT NULL DEFAULT 1 COMMENT '严重等级(1一般,2严重,3重大)',
  `handle_method` tinyint NULL DEFAULT NULL COMMENT '处理方式',
  `handle_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '处理详情',
  `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理备注',
  `ncr_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已发布,2处理中,3待验证,4已关闭,5已取消)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `close_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关闭说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ncr_no`(`ncr_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_time`(`supplier_id` ASC, `ncr_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_inspection_id`(`inspection_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NCR质量异常表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_change
-- ----------------------------
DROP TABLE IF EXISTS `order_change`;
CREATE TABLE `order_change`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `change_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '变更单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_detail_id` bigint NULL DEFAULT NULL COMMENT '订单明细ID',
  `change_type` tinyint NOT NULL COMMENT '变更类型(1数量,2价格,3交期,4取消,5其他)',
  `change_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更内容',
  `before_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更前值',
  `after_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更后值',
  `change_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更原因',
  `apply_by` bigint NULL DEFAULT NULL COMMENT '申请人',
  `apply_time` datetime NULL DEFAULT NULL COMMENT '申请时间',
  `approve_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态(0待审批,1通过,2拒绝)',
  `approve_by` bigint NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_change_no`(`change_no` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_id` ASC, `approve_status` ASC) USING BTREE,
  INDEX `idx_change_type`(`change_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单变更记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_detail_version
-- ----------------------------
DROP TABLE IF EXISTS `order_detail_version`;
CREATE TABLE `order_detail_version`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '采购订单ID',
  `order_detail_id` bigint NOT NULL COMMENT '订单明细ID',
  `version_no` int NULL DEFAULT NULL COMMENT '版本号',
  `change_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更类型',
  `quantity` decimal(18, 4) NULL DEFAULT NULL COMMENT '数量',
  `unit_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '单价',
  `amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '金额',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细版本表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for order_status_log
-- ----------------------------
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `action_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作名称',
  `before_status` tinyint NULL DEFAULT NULL COMMENT '变更前状态',
  `after_status` tinyint NULL DEFAULT NULL COMMENT '变更后状态',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_time`(`order_id` ASC, `operate_time` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单状态轨迹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_track
-- ----------------------------
DROP TABLE IF EXISTS `order_track`;
CREATE TABLE `order_track`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `track_status` tinyint NOT NULL COMMENT '跟踪状态',
  `track_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '跟踪时间',
  `track_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跟踪说明',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_time`(`order_id` ASC, `track_time` ASC) USING BTREE,
  INDEX `idx_track_status`(`track_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062797015972147203 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单跟踪记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '付款单号',
  `invoice_id` bigint NULL DEFAULT NULL COMMENT '发票ID',
  `invoice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发票号码',
  `recon_id` bigint NULL DEFAULT NULL COMMENT '对账单ID',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `payment_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '付款金额',
  `payment_method` tinyint NOT NULL DEFAULT 1 COMMENT '付款方式',
  `payment_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款账号',
  `payment_bank` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款银行',
  `receive_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收款账号',
  `receive_bank` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收款银行',
  `schedule_date` date NULL DEFAULT NULL COMMENT '计划付款日期',
  `payment_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款条件，如：月结30天、款到发货',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '付款时间',
  `payment_status` tinyint NOT NULL DEFAULT 0 COMMENT '付款状态(0待付款,1部分付款,2已付款,3已拒绝)',
  `receipt_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外部付款回执号',
  `approve_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态',
  `voucher_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '凭证号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`payment_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_receipt_no`(`receipt_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_time`(`supplier_id` ASC, `payment_status` ASC, `payment_time` ASC) USING BTREE,
  INDEX `idx_invoice_id`(`invoice_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '付款记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment_approval
-- ----------------------------
DROP TABLE IF EXISTS `payment_approval`;
CREATE TABLE `payment_approval`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `payment_id` bigint NOT NULL COMMENT '付款单ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '付款单号',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `payment_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '付款金额',
  `approval_level` int NOT NULL DEFAULT 1 COMMENT '审批级别：1=一级审批, 2=二级审批, 3=三级审批',
  `approval_status` int NOT NULL DEFAULT 0 COMMENT '审批状态：0=待审批, 1=已通过, 2=已驳回',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批人姓名',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审批意见',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除, 1=已删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_payment_id`(`payment_id` ASC) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_approval_status`(`approval_status` ASC) USING BTREE,
  INDEX `idx_approval_level`(`approval_level` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '付款审批记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for portal_todo
-- ----------------------------
DROP TABLE IF EXISTS `portal_todo`;
CREATE TABLE `portal_todo`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `todo_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '待办类型',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint NOT NULL COMMENT '业务ID',
  `business_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务单号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `todo_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待办,1已办,2忽略)',
  `due_time` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_status_time`(`user_id` ASC, `todo_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_supplier_status_time`(`supplier_id` ASC, `todo_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062813805418692610 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '门户待办表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `org_id` bigint NULL DEFAULT NULL COMMENT '组织ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
  `erp_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ERP订单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `order_date` date NOT NULL COMMENT '订单日期',
  `delivery_date` date NULL DEFAULT NULL COMMENT '要求交货日期',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `exchange_rate` decimal(18, 6) NOT NULL DEFAULT 1.000000 COMMENT '汇率',
  `total_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税额',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态(0待下发,1待确认,2已确认,3部分发货,4已完成,5已取消,6已拒单)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间',
  `confirm_by` bigint NULL DEFAULT NULL COMMENT '确认人',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `reject_time` datetime NULL DEFAULT NULL COMMENT '拒单时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '取消原因',
  `buyer_id` bigint NULL DEFAULT NULL COMMENT '采购员ID',
  `buyer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '采购员姓名',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  `contract_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '合同编号',
  `payment_terms` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款条款',
  `delivery_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '送货地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_date`(`supplier_id` ASC, `order_status` ASC, `order_date` ASC) USING BTREE,
  INDEX `idx_org_status_create_time`(`org_id` ASC, `order_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_erp_order_no`(`erp_order_no` ASC) USING BTREE,
  INDEX `idx_delivery_date`(`delivery_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796885248274434 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '采购订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for purchase_order_detail
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order_detail`;
CREATE TABLE `purchase_order_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `line_no` int NOT NULL DEFAULT 0 COMMENT '行号',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料名称',
  `material_spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料规格',
  `material_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料型号',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '订单数量',
  `price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '未税单价',
  `tax_price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '含税单价',
  `tax_rate` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '税率',
  `amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '未税金额',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税额',
  `delivered_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '已发货数量',
  `received_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '已收货数量',
  `qualified_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '合格数量',
  `delivery_date` date NULL DEFAULT NULL COMMENT '要求交货日期',
  `promise_date` date NULL DEFAULT NULL COMMENT '承诺交期',
  `line_status` tinyint NOT NULL DEFAULT 0 COMMENT '行状态(0待确认,1已确认,2部分发货,3已完成,4已取消)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_line`(`order_id` ASC, `line_no` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE,
  INDEX `idx_delivery_status`(`delivery_date` ASC, `line_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796885441212418 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '采购订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for purchase_order_erp_mapping
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order_erp_mapping`;
CREATE TABLE `purchase_order_erp_mapping`  (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ERP订单号映射表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for quality_appeal
-- ----------------------------
DROP TABLE IF EXISTS `quality_appeal`;
CREATE TABLE `quality_appeal`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `appeal_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申诉编号',
  `ncr_id` bigint NULL DEFAULT NULL COMMENT 'NCR ID',
  `inspection_id` bigint NULL DEFAULT NULL COMMENT '检验单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `appeal_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申诉原因',
  `appeal_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已提交,2审核中,3通过,4驳回)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `audit_by` bigint NULL DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_appeal_no`(`appeal_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_time`(`supplier_id` ASC, `appeal_status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '质量申诉表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quality_inspection
-- ----------------------------
DROP TABLE IF EXISTS `quality_inspection`;
CREATE TABLE `quality_inspection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inspection_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '检验单号',
  `receipt_id` bigint NOT NULL COMMENT '收货记录ID',
  `delivery_id` bigint NULL DEFAULT NULL COMMENT '送货明细ID',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `standard_id` bigint NULL DEFAULT NULL COMMENT '检验标准ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料名称',
  `inspect_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '检验数量',
  `qualified_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '合格数量',
  `unqualified_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '不合格数量',
  `inspect_result` tinyint NOT NULL DEFAULT 0 COMMENT '检验结果(0待检,1合格,2不合格,3部分合格)',
  `inspect_type` tinyint NOT NULL DEFAULT 1 COMMENT '检验类型',
  `inspect_time` datetime NULL DEFAULT NULL COMMENT '检验时间',
  `inspector` bigint NULL DEFAULT NULL COMMENT '检验员',
  `inspector_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '检验员姓名',
  `inspect_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '检验说明',
  `handle_method` tinyint NULL DEFAULT NULL COMMENT '处理方式(1退货,2换货,3特采,4报废,5扣款)',
  `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inspection_no`(`inspection_no` ASC) USING BTREE,
  INDEX `idx_supplier_result_time`(`supplier_id` ASC, `inspect_result` ASC, `inspect_time` ASC) USING BTREE,
  INDEX `idx_receipt_id`(`receipt_id` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '质量检验表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quote
-- ----------------------------
DROP TABLE IF EXISTS `quote`;
CREATE TABLE `quote`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报价单号',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `exchange_rate` decimal(18, 6) NOT NULL DEFAULT 1.000000 COMMENT '汇率',
  `total_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税额',
  `quote_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已提交,2已采纳,3未采纳,4已撤回)',
  `negotiation_round` tinyint NOT NULL DEFAULT 0 COMMENT '谈判轮次',
  `payment_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款条件',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `valid_until` datetime NULL DEFAULT NULL COMMENT '报价有效期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quote_no`(`quote_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_rfq_supplier`(`rfq_id` ASC, `supplier_id` ASC) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `quote_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796699990061058 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报价单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quote_award
-- ----------------------------
DROP TABLE IF EXISTS `quote_award`;
CREATE TABLE `quote_award`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `quote_id` bigint NOT NULL COMMENT '报价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `award_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '授标金额',
  `award_tax_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税额',
  `award_currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'CNY' COMMENT '币种',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联订单号',
  `award_by` bigint NULL DEFAULT NULL COMMENT '授标人ID',
  `award_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '授标人姓名',
  `award_time` datetime NULL DEFAULT NULL COMMENT '授标时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2062796885378297859 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报价授标表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for quote_item
-- ----------------------------
DROP TABLE IF EXISTS `quote_item`;
CREATE TABLE `quote_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_id` bigint NOT NULL COMMENT '报价单ID',
  `rfq_item_id` bigint NOT NULL COMMENT '询价明细ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `spec` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '数量',
  `price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '未税单价',
  `tax_price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '含税单价',
  `tax_rate` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '税率',
  `amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '未税金额',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税额',
  `delivery_date` date NULL DEFAULT NULL COMMENT '交期',
  `payment_terms` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '付款条件',
  `delivery_days` int NULL DEFAULT NULL COMMENT '交货天数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_quote_id`(`quote_id` ASC) USING BTREE,
  INDEX `idx_rfq_item_id`(`rfq_item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796700115890180 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报价明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quote_negotiation
-- ----------------------------
DROP TABLE IF EXISTS `quote_negotiation`;
CREATE TABLE `quote_negotiation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_id` bigint NOT NULL COMMENT '报价单ID',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `round` int NOT NULL DEFAULT 1 COMMENT '谈判轮次',
  `initiator` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起方(buyer/supplier)',
  `target_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '目标价格',
  `supplier_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '供应商报价',
  `buyer_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '采购方备注',
  `supplier_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商备注',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2062355842522542083 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报价谈判表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for receipt_diff
-- ----------------------------
DROP TABLE IF EXISTS `receipt_diff`;
CREATE TABLE `receipt_diff`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '收货记录ID',
  `notice_id` bigint NULL DEFAULT NULL COMMENT 'ASN ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `plan_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '计划数量',
  `receipt_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '收货数量',
  `diff_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '差异数量',
  `diff_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '差异原因',
  `handle_method` tinyint NULL DEFAULT NULL COMMENT '处理方式(1冲销,2退货,3让步接收)',
  `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理备注',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待处理,1已处理)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_id`(`record_id` ASC) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收货差异表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for receipt_record
-- ----------------------------
DROP TABLE IF EXISTS `receipt_record`;
CREATE TABLE `receipt_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货单号',
  `delivery_id` bigint NOT NULL COMMENT '送货明细ID',
  `notice_id` bigint NULL DEFAULT NULL COMMENT 'ASN ID',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料名称',
  `plan_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '计划收货数量',
  `receipt_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '实际收货数量',
  `reject_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '拒收数量',
  `receipt_time` datetime NULL DEFAULT NULL COMMENT '收货时间',
  `receiver` bigint NULL DEFAULT NULL COMMENT '收货人',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仓库名称',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '库位',
  `receipt_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待收货,1已收货,2已拒收,3部分收货)',
  `diff_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '差异原因',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拒收原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_receipt_no`(`receipt_no` ASC) USING BTREE,
  INDEX `idx_supplier_time`(`supplier_id` ASC, `receipt_time` ASC) USING BTREE,
  INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
  INDEX `idx_delivery_id`(`delivery_id` ASC) USING BTREE,
  INDEX `idx_receipt_status`(`receipt_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收货记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reconciliation
-- ----------------------------
DROP TABLE IF EXISTS `reconciliation`;
CREATE TABLE `reconciliation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recon_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对账单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `recon_period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对账周期',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `total_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '对账总金额',
  `confirmed_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '确认金额',
  `diff_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '差异金额',
  `recon_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1待供应商确认,2供应商已确认,3有异议,4已开票,5已付款,6已关闭)',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间',
  `confirm_by` bigint NULL DEFAULT NULL COMMENT '确认人',
  `confirm_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '确认备注',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_recon_no`(`recon_no` ASC) USING BTREE,
  INDEX `idx_supplier_status_period`(`supplier_id` ASC, `recon_status` ASC, `recon_period` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对账单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reconciliation_detail
-- ----------------------------
DROP TABLE IF EXISTS `reconciliation_detail`;
CREATE TABLE `reconciliation_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recon_id` bigint NOT NULL COMMENT '对账单ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `receipt_id` bigint NULL DEFAULT NULL COMMENT '收货ID',
  `receipt_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货单号',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物料名称',
  `quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '数量',
  `unit_price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `order_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
  `deduction_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '扣款金额',
  `confirmed_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '确认金额',
  `diff_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '差异金额',
  `diff_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '差异原因',
  `confirm_status` tinyint NOT NULL DEFAULT 0 COMMENT '确认状态(0待确认,1已确认,2有异议)',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间',
  `confirm_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '确认备注',
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
  INDEX `idx_receipt_id`(`receipt_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对账明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rfq
-- ----------------------------
DROP TABLE IF EXISTS `rfq`;
CREATE TABLE `rfq`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rfq_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '询价单号',
  `rfq_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '询价标题',
  `org_id` bigint NULL DEFAULT NULL COMMENT '组织ID',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `quote_deadline` datetime NOT NULL COMMENT '报价截止时间',
  `rfq_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0草稿,1已发布,2报价中,3已截止,4已定价,5已取消)',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rfq_no`(`rfq_no` ASC) USING BTREE,
  INDEX `idx_org_status_time`(`org_id` ASC, `rfq_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_deadline`(`quote_deadline` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796493055684610 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '询价单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rfq_item
-- ----------------------------
DROP TABLE IF EXISTS `rfq_item`;
CREATE TABLE `rfq_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `line_no` int NOT NULL DEFAULT 0 COMMENT '行号',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料名称',
  `material_spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '数量',
  `target_delivery_date` date NULL DEFAULT NULL COMMENT '目标交期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rfq_id`(`rfq_id` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796493177319427 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '询价明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rfq_supplier
-- ----------------------------
DROP TABLE IF EXISTS `rfq_supplier`;
CREATE TABLE `rfq_supplier`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rfq_id` bigint NOT NULL COMMENT '询价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `invite_status` tinyint NOT NULL DEFAULT 0 COMMENT '邀请状态(0待邀请,1已邀请,2已响应,3拒绝)',
  `quote_id` bigint NULL DEFAULT NULL COMMENT '报价单ID',
  `invite_time` datetime NULL DEFAULT NULL COMMENT '邀请时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rfq_supplier`(`rfq_id` ASC, `supplier_id` ASC) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `invite_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062796493437366277 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '询价邀请供应商表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_bank_account
-- ----------------------------
DROP TABLE IF EXISTS `supplier_bank_account`;
CREATE TABLE `supplier_bank_account`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账户名称',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '开户银行',
  `bank_branch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开户支行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '银行账号',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_bank_account`(`bank_account` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商银行账户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `supplier_blacklist`;
CREATE TABLE `supplier_blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
  `credit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '拉黑原因',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '解除时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0解除,1生效)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_credit_code`(`credit_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2060178716197130242 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商黑名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_category
-- ----------------------------
DROP TABLE IF EXISTS `supplier_category`;
CREATE TABLE `supplier_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code`(`category_code` ASC) USING BTREE,
  INDEX `idx_parent_sort`(`parent_id` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_contact
-- ----------------------------
DROP TABLE IF EXISTS `supplier_contact`;
CREATE TABLE `supplier_contact`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人姓名',
  `contact_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人角色',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `is_primary` tinyint NOT NULL DEFAULT 0 COMMENT '是否主联系人',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_phone`(`contact_phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_info
-- ----------------------------
DROP TABLE IF EXISTS `supplier_info`;
CREATE TABLE `supplier_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `org_id` bigint NULL DEFAULT NULL COMMENT '组织ID',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
  `supplier_short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商简称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `supplier_type` tinyint NOT NULL DEFAULT 1 COMMENT '供应商类型',
  `credit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `legal_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人代表',
  `registered_capital` decimal(18, 2) NULL DEFAULT NULL COMMENT '注册资本',
  `establish_date` date NULL DEFAULT NULL COMMENT '成立日期',
  `business_scope` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '经营范围',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '银行账号',
  `tax_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '税号',
  `invoice_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开票地址',
  `invoice_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开票电话',
  `rating` tinyint NULL DEFAULT NULL COMMENT '供应商评级(1A,2B,3C,4D)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0待审核,1合作中,2暂停,3黑名单,4拒绝)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_by` bigint NULL DEFAULT NULL COMMENT '审核人',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_code`(`supplier_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_credit_code`(`credit_code` ASC) USING BTREE,
  INDEX `idx_category_status`(`category_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_org_status_create_time`(`org_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_name`(`supplier_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2061278470826102787 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商基本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_performance
-- ----------------------------
DROP TABLE IF EXISTS `supplier_performance`;
CREATE TABLE `supplier_performance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `evaluate_period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评估周期',
  `quality_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '质量评分',
  `delivery_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '交付评分',
  `service_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '服务评分',
  `price_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '价格评分',
  `total_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '综合评分',
  `qualified_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '合格率',
  `ontime_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '准交率',
  `evaluate_by` bigint NULL DEFAULT NULL COMMENT '评估人',
  `evaluate_time` datetime NULL DEFAULT NULL COMMENT '评估时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_period`(`supplier_id` ASC, `evaluate_period` ASC) USING BTREE,
  INDEX `idx_period_score`(`evaluate_period` ASC, `total_score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商绩效表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier_qualification
-- ----------------------------
DROP TABLE IF EXISTS `supplier_qualification`;
CREATE TABLE `supplier_qualification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `qual_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资质类型',
  `qual_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资质名称',
  `qual_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资质编号',
  `qual_org` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发证机构',
  `valid_start` date NULL DEFAULT NULL COMMENT '有效期开始',
  `valid_end` date NULL DEFAULT NULL COMMENT '有效期结束',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0无效,1有效,2即将过期,3已过期)',
  `remind_days` int NOT NULL DEFAULT 30 COMMENT '提醒天数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplier_type`(`supplier_id` ASC, `qual_type` ASC) USING BTREE,
  INDEX `idx_valid_end`(`valid_end` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062690566553997315 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商资质表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块名称',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务主键ID',
  `business_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务单号',
  `action_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '动作名称',
  `before_status` int NULL DEFAULT NULL COMMENT '操作前状态',
  `after_status` int NULL DEFAULT NULL COMMENT '操作后状态',
  `request_method` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法签名',
  `request_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求路径',
  `client_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端IP',
  `result_status` int NOT NULL DEFAULT 1 COMMENT '结果状态: 1=成功 0=失败',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息（截断 500）',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business_id`(`business_id` ASC) USING BTREE COMMENT '业务主键索引（实时监控主链路）',
  INDEX `idx_business_no`(`business_no` ASC) USING BTREE COMMENT '业务单号索引',
  INDEX `idx_user_time`(`user_id` ASC, `operate_time` DESC) USING BTREE COMMENT '用户操作复盘',
  INDEX `idx_result_time`(`result_status` ASC, `operate_time` DESC) USING BTREE COMMENT '异常审计扫描',
  INDEX `idx_module_biz_time`(`module_name` ASC, `business_type` ASC, `operate_time` DESC) USING BTREE COMMENT '模块维度分页',
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE COMMENT '链路追踪'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统审计日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参数键',
  `config_value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数值',
  `config_type` tinyint NOT NULL DEFAULT 1 COMMENT '参数类型(1系统,2业务)',
  `encrypted` tinyint NOT NULL DEFAULT 0 COMMENT '是否加密',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`dict_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062812959834423298 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `item_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项标签',
  `item_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项值',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_item`(`dict_id` ASC, `item_value` ASC) USING BTREE,
  INDEX `idx_dict_sort`(`dict_id` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062812959989612547 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_export_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_export_task`;
CREATE TABLE `sys_export_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '导出任务号',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务类型',
  `file_id` bigint NULL DEFAULT NULL COMMENT '文件ID',
  `export_params` json NULL COMMENT '导出参数',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总数量',
  `processed_count` int NOT NULL DEFAULT 0 COMMENT '已处理数量',
  `task_status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态(0待处理,1处理中,2成功,3失败)',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_no`(`task_no` ASC) USING BTREE,
  INDEX `idx_type_status_time`(`task_type` ASC, `task_status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062805399655952386 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '异步导出任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_file_attachment
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_attachment`;
CREATE TABLE `sys_file_attachment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `business_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务单号',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始文件名',
  `file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小',
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储桶',
  `object_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对象Key',
  `file_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件摘要',
  `upload_user_id` bigint NULL DEFAULT NULL COMMENT '上传用户ID',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0禁用,1有效)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_file_hash`(`file_hash` ASC) USING BTREE,
  INDEX `idx_bucket_object`(`bucket_name` ASC, `object_key`(191) ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062805400113131523 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_idempotent_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_idempotent_record`;
CREATE TABLE `sys_idempotent_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `idempotent_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '幂等键',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `request_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求摘要',
  `result_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结果码',
  `result_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结果信息',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0处理中,1成功,2失败)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_idempotent_key`(`idempotent_key` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '幂等记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `login_type` tinyint NOT NULL DEFAULT 1 COMMENT '登录类型(1密码,2刷新令牌)',
  `client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户代理',
  `login_status` tinyint NOT NULL DEFAULT 1 COMMENT '登录状态(0失败,1成功)',
  `fail_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username_time`(`username` ASC, `login_time` ASC) USING BTREE,
  INDEX `idx_ip_time`(`client_ip` ASC, `login_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级ID',
  `perm_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限名称',
  `perm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限编码',
  `perm_type` tinyint NOT NULL COMMENT '权限类型(1菜单,2按钮,3接口)',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由或接口路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `http_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'HTTP方法',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code` ASC) USING BTREE,
  INDEX `idx_parent_sort`(`parent_id` ASC, `sort` ASC) USING BTREE,
  INDEX `idx_type_status`(`perm_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `role_type` tinyint NOT NULL DEFAULT 1 COMMENT '角色类型(1内部,2供应商,3接口)',
  `data_scope` tinyint NOT NULL DEFAULT 1 COMMENT '数据范围(1全部,2组织,3本人,4供应商)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0禁用,1启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_status_sort`(`status` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `perm_id` ASC) USING BTREE,
  INDEX `idx_perm_id`(`perm_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 95 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `org_id` bigint NULL DEFAULT NULL COMMENT '组织ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码哈希',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `user_type` tinyint NOT NULL DEFAULT 1 COMMENT '用户类型(1内部用户,2供应商用户,3接口账号)',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0禁用,1启用,2锁定)',
  `password_update_time` datetime NULL DEFAULT NULL COMMENT '密码更新时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `token_version` int NOT NULL DEFAULT 0 COMMENT '令牌版本',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除,1已删除)',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_supplier_status`(`supplier_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_org_status`(`org_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2062433834506514434 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for third_party_po_api_config
-- ----------------------------
DROP TABLE IF EXISTS `third_party_po_api_config`;
CREATE TABLE `third_party_po_api_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `api_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口类型(CREATE-新增采购订单,GET_LATEST-获取最新采购订单)',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '第三方接口地址',
  `http_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'POST' COMMENT 'HTTP方法(GET,POST,PUT)',
  `auth_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NONE' COMMENT '鉴权方式(NONE,BASIC,BEARER,API_KEY)',
  `auth_credentials` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '鉴权凭证(JSON)',
  `request_headers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '自定义请求头(JSON)',
  `request_body_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求体模板(JSON,支持占位符)',
  `timeout_seconds` int NOT NULL DEFAULT 30 COMMENT '超时时间(秒)',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `enabled` tinyint NOT NULL DEFAULT 0 COMMENT '启用状态(0停用,1启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_type`(`api_type` ASC) USING BTREE,
  INDEX `idx_enabled`(`enabled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '第三方采购订单接口配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for three_way_match
-- ----------------------------
DROP TABLE IF EXISTS `three_way_match`;
CREATE TABLE `three_way_match`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `recon_id` bigint NULL DEFAULT NULL COMMENT '对账单ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '采购订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `order_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '订单金额',
  `receipt_id` bigint NULL DEFAULT NULL COMMENT '收货记录ID',
  `receipt_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '收货金额',
  `invoice_id` bigint NULL DEFAULT NULL COMMENT '发票ID',
  `invoice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发票号码',
  `invoice_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '发票金额',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `match_result` int NULL DEFAULT NULL COMMENT '匹配结果: 0=完全匹配 1=部分匹配 2=不匹配',
  `diff_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '差异金额',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '三单匹配表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for vmi_inventory
-- ----------------------------
DROP TABLE IF EXISTS `vmi_inventory`;
CREATE TABLE `vmi_inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料编码',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仓库名称',
  `onhand_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '现有库存',
  `available_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '可用库存',
  `safety_qty` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '安全库存',
  `max_qty` decimal(18, 4) NULL DEFAULT NULL COMMENT '最高库存',
  `inventory_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(1正常,2预警,3缺料)',
  `last_sync_time` datetime NULL DEFAULT NULL COMMENT '最后同步时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_material_warehouse`(`supplier_id` ASC, `material_code` ASC, `warehouse_id` ASC) USING BTREE,
  INDEX `idx_status`(`inventory_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'VMI库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for write_off
-- ----------------------------
DROP TABLE IF EXISTS `write_off`;
CREATE TABLE `write_off`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `write_off_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冲销单号',
  `notice_id` bigint NULL DEFAULT NULL COMMENT 'ASN通知单ID',
  `asn_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ASN号',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单号',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `write_off_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冲销类型: 退货冲销/价格调整/数量差异调整/破损扣减/质量扣款/其他调整',
  `amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '冲销金额',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '冲销调整表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
