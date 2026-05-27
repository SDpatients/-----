-- =====================================================
-- 7.5 质量异常流程 - DDL 变更迁移脚本
-- 日期: 2026-05-26
-- =====================================================

-- 质检表: 增加检验标准ID字段
ALTER TABLE quality_inspection
    ADD COLUMN standard_id BIGINT DEFAULT NULL COMMENT '检验标准ID' AFTER supplier_id;

-- 检验标准表: 增加检验策略和抽检比例字段
ALTER TABLE inspection_standard
    ADD COLUMN inspection_strategy TINYINT DEFAULT 1 COMMENT '检验策略(0免检,1抽检,2全检)' AFTER sample_rule,
    ADD COLUMN sample_rate DECIMAL(5,2) DEFAULT NULL COMMENT '抽检比例' AFTER inspection_strategy;

-- NCR表: 增加处理详情和处理备注字段
ALTER TABLE nonconformance_report
    ADD COLUMN handle_detail TEXT DEFAULT NULL COMMENT '处理详情' AFTER handle_method,
    ADD COLUMN handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注' AFTER handle_detail;

-- 8D报告表: 增加当前阶段和阶段截止日期字段
ALTER TABLE eight_d_report
    ADD COLUMN current_step TINYINT DEFAULT 1 COMMENT '当前阶段(1D1,2D2,3D3,4D4,5D5,6D6,7D7,8D8)' AFTER due_date,
    ADD COLUMN step_due_date DATE DEFAULT NULL COMMENT '当前阶段截止日期' AFTER current_step;