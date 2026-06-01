-- =============================================================
-- 订单变更表 & 对账明细表 修复脚本
-- 修复内容：添加缺失的数据库列
-- =============================================================

-- 1. order_change 表添加缺失列
ALTER TABLE order_change
    ADD COLUMN apply_by BIGINT DEFAULT NULL COMMENT '申请人' AFTER change_reason,
    ADD COLUMN apply_time DATETIME DEFAULT NULL COMMENT '申请时间' AFTER apply_by;

-- 2. reconciliation_detail 表添加缺失列
ALTER TABLE reconciliation_detail
    ADD COLUMN confirm_remark VARCHAR(500) DEFAULT NULL COMMENT '确认备注' AFTER confirm_time;