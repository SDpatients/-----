-- 财务对账模块：为 delivery_notice 表添加财务相关字段
ALTER TABLE delivery_notice
ADD COLUMN total_amount DECIMAL(18,2) COMMENT '发货总金额',
ADD COLUMN currency VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
ADD COLUMN tax_amount DECIMAL(18,2) COMMENT '税额',
ADD COLUMN net_amount DECIMAL(18,2) COMMENT '净额',
ADD COLUMN payment_status INT DEFAULT 0 COMMENT '付款状态: 0-未付款 1-部分付款 2-已付款',
ADD COLUMN reconciliation_status INT DEFAULT 0 COMMENT '对账状态: 0-待对账 1-对账中 2-已对账 3-有差异';

-- 为已有数据设置默认值（从关联采购订单取金额）
UPDATE delivery_notice dn
INNER JOIN purchase_order po ON dn.order_id = po.id
SET dn.total_amount = COALESCE(po.total_amount, 0),
    dn.currency = COALESCE(po.currency, 'CNY'),
    dn.tax_amount = COALESCE(po.tax_amount, 0),
    dn.net_amount = COALESCE(po.total_amount, 0) - COALESCE(po.tax_amount, 0),
    dn.payment_status = 0,
    dn.reconciliation_status = 0
WHERE dn.total_amount IS NULL;
