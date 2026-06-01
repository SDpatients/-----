-- =============================================================
-- 测试种子数据
-- =============================================================

-- 管理员用户（密码：test123456）
INSERT INTO sys_user (username, password, real_name, user_type, supplier_id, status) VALUES
('admin_test', '$2a$10$M2C8mVko/b6t/Zp6ih15pOtOb6TqvwBFaXk6WMV5QHFEPl6xul/YG', '管理员', 0, NULL, 1),
('supplier_user_1', '$2a$10$M2C8mVko/b6t/Zp6ih15pOtOb6TqvwBFaXk6WMV5QHFEPl6xul/YG', '供应商A用户', 1, 1, 1),
('supplier_user_2', '$2a$10$M2C8mVko/b6t/Zp6ih15pOtOb6TqvwBFaXk6WMV5QHFEPl6xul/YG', '供应商B用户', 1, 2, 1);

-- 角色
INSERT INTO sys_role (role_name, role_code, status) VALUES
('管理员', 'admin', 1),
('供应商', 'supplier', 1),
('采购', 'purchasing', 1);

-- 权限
INSERT INTO sys_permission (perm_name, perm_code, perm_type, status) VALUES
('订单管理', 'order:view', 0, 1),
('订单创建', 'order:create', 1, 1),
('订单下发', 'order:publish', 1, 1),
('供应商管理', 'supplier:view', 0, 1),
('供应商审核', 'supplier:audit', 1, 1),
('ASN管理', 'delivery:view', 0, 1),
('质检管理', 'quality:view', 0, 1),
('对账管理', 'settlement:view', 0, 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2);

-- 角色权限关联（管理员拥有所有权限）
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8);

-- 测试供应商
INSERT INTO supplier_info (supplier_code, supplier_name, credit_code, contact_name, contact_phone, status) VALUES
('SUP001', '测试供应商A', '91110000MA001TEST1', '张三', '13800000001', 1),
('SUP002', '测试供应商B', '91110000MA002TEST2', '李四', '13800000002', 1),
('SUP003', '黑名单供应商', '91110000MA003TEST3', '王五', '13800000003', 0);

-- 黑名单
INSERT INTO supplier_blacklist (credit_code, company_name, reason, status) VALUES
('91110000MA003TEST3', '黑名单供应商', '质量问题严重', 1);

-- 测试采购订单
INSERT INTO purchase_order (order_no, order_status, supplier_id, order_date, delivery_date, total_amount) VALUES
('PO202605270001', 0, 1, '2026-05-27', '2026-06-15', 100000.00),
('PO202605270002', 1, 1, '2026-05-27', '2026-06-15', 50000.00),
('PO202605270003', 2, 2, '2026-05-27', '2026-06-15', 30000.00);

-- 订单明细
INSERT INTO purchase_order_detail (order_id, material_code, material_name, quantity, unit_price, amount) VALUES
(1, 'MAT001', '螺丝', 10000.0000, 5.00, 50000.00),
(1, 'MAT002', '螺母', 5000.0000, 10.00, 50000.00),
(2, 'MAT003', '轴承', 1000.0000, 50.00, 50000.00);

-- 测试 ASN
INSERT INTO delivery_notice (notice_no, order_id, supplier_id, delivery_status) VALUES
('ASN202605270001', 2, 1, 0),
('ASN202605270002', 3, 2, 1);

-- 测试质检记录
INSERT INTO quality_inspection (inspection_no, supplier_id, inspect_result, inspect_type) VALUES
('IQC202605270001', 1, 0, 1);

-- 测试对账单
INSERT INTO reconciliation (recon_no, supplier_id, supplier_name, recon_status, recon_period, total_amount, start_date, end_date) VALUES
('REC202605270001', 1, '测试供应商A', 0, '2026-05', 100000.00, '2026-05-01', '2026-05-31');