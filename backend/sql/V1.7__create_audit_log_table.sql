-- =============================================================
-- sys_audit_log（系统审计日志表）正式 DDL
-- 对应实体：com.supplier.system.entity.SysAuditLog
-- 父类字段：com.supplier.common.entity.BaseEntity
-- 说明：AOP @AuditLog 注解写入的唯一正式表
-- 索引策略：
--   1) business_id  : 反查"某笔业务的所有操作记录"（实时监控按业务主键关联）
--   2) business_no  : 按业务单号反查（如订单号、ASN 号）
--   3) user_id+operate_time : 按用户复盘一段时间内的操作
--   4) result_status+operate_time : 异常审计扫描
--   5) module_name+business_type+operate_time : 模块维度分页
-- =============================================================
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统审计日志表' ROW_FORMAT = Dynamic;
