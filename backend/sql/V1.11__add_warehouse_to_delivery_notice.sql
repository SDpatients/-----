-- 为 delivery_notice 表添加收货仓库字段
ALTER TABLE delivery_notice
ADD COLUMN warehouse VARCHAR(100) COMMENT '收货仓库';
