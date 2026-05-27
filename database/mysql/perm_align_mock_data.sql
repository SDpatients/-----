-- =====================================================
-- 前后端权限码对齐脚本 (supplier_collaboration_mock_data.sql 架构)
-- 将数据库 perm_code 对齐到前端路由所需的权限码
-- 执行前请先备份 sys_permission 和 sys_role_permission 表
-- =====================================================

USE supplier_collaboration;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- Part 1: 更新现有的 perm_code，使其与前端路由对齐
-- =====================================================

-- ID=4: supplier → supplier:view (前端路由 /purchasing/suppliers 需 supplier:view)
UPDATE sys_permission SET perm_code = 'supplier:view' WHERE id = 4 AND perm_code = 'supplier';

-- ID=7: sourcing:rfq → rfq:view (前端路由 /purchasing/rfq 需 rfq:view)
UPDATE sys_permission SET perm_code = 'rfq:view' WHERE id = 7 AND perm_code = 'sourcing:rfq';

-- ID=8: order → order:view (前端路由 /purchasing/orders 需 order:view)
UPDATE sys_permission SET perm_code = 'order:view' WHERE id = 8 AND perm_code = 'order';

-- ID=11: delivery:asn → asn:view (前端路由 /purchasing/delivery 需 asn:view)
UPDATE sys_permission SET perm_code = 'asn:view' WHERE id = 11 AND perm_code = 'delivery:asn';

-- ID=12: quality → quality:view (前端路由 /purchasing/quality 需 quality:view)
UPDATE sys_permission SET perm_code = 'quality:view' WHERE id = 12 AND perm_code = 'quality';

-- ID=14: settlement → settlement:view (前端路由 /purchasing/settlement 需 settlement:view)
UPDATE sys_permission SET perm_code = 'settlement:view' WHERE id = 14 AND perm_code = 'settlement';

-- ID=17: portal:dashboard → supplier:dashboard:view (前端路由 /supplier/dashboard 需 supplier:dashboard:view)
UPDATE sys_permission SET perm_code = 'supplier:dashboard:view' WHERE id = 17 AND perm_code = 'portal:dashboard';

-- ID=19: delivery:create → asn:create (前端路由 /purchasing/delivery/create 需 asn:create)
UPDATE sys_permission SET perm_code = 'asn:create' WHERE id = 19 AND perm_code = 'delivery:create';

-- =====================================================
-- Part 2: 新增前端路由需要的权限（ID 从 21 开始）
-- =====================================================

-- 采购方门户权限
INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, perm_type, path, component, http_method, icon, sort, visible, status, create_by, update_by) VALUES
(21, 0,  '采购工作台',    'dashboard:view',          1, '/purchasing/dashboard',       NULL, NULL, 'data-board',    1, 1, 1, 1, 1),
(22, 0,  '财务管理',      'finance:view',            1, '/purchasing/finance',         NULL, NULL, 'money',         8, 1, 1, 1, 1),
(23, 0,  '报价对比',      'quote:view',              1, '/purchasing/quote-compare',   NULL, NULL, 'trend-charts',  9, 1, 1, 1, 1),
(24, 0,  'VMI需求预测',   'inventory:view',          1, '/purchasing/vmi-forecast',    NULL, NULL, 'box',          10, 1, 1, 1, 1),
(25, 0,  '集成网关',      'integration:view',        1, '/purchasing/integration',     NULL, NULL, 'connection',   11, 1, 1, 1, 1),
(26, 0,  '系统配置',      'config:view',             1, '/purchasing/config',          NULL, NULL, 'setting',      12, 1, 1, 1, 1),
(27, 0,  '消息待办',      'message:view',            1, '/messages',                   NULL, NULL, 'bell',         13, 1, 1, 1, 1),

-- 供应商门户权限
(28, 0,  '供应商订单中心',  'supplier:order:view',     1, '/supplier/orders',           NULL, NULL, 'tickets',       1, 1, 1, 1, 1),
(29, 0,  '供应商发货中心',  'supplier:delivery:view',  1, '/supplier/delivery',         NULL, NULL, 'truck',         2, 1, 1, 1, 1),
(30, 0,  '供应商创建发货',  'supplier:delivery:create', 1, '/supplier/delivery/create',  NULL, NULL, 'plus',          3, 1, 1, 1, 1),
(31, 0,  '供应商质量中心',  'supplier:quality:view',   1, '/supplier/quality',          NULL, NULL, 'medal',         4, 1, 1, 1, 1),
(32, 0,  '供应商财务中心',  'supplier:settlement:view', 1, '/supplier/settlement',       NULL, NULL, 'money',         5, 1, 1, 1, 1),
(33, 0,  '供应商RFQ报价',   'supplier:rfq:view',       1, '/supplier/rfq',              NULL, NULL, 'document',      6, 1, 1, 1, 1),
(34, 0,  '供应商资料中心',  'supplier:profile:view',   1, '/supplier/profile',          NULL, NULL, 'user',          7, 1, 1, 1, 1),

-- 扩展权限（前端 mock 数据中也用到的）
(35, 0,  '供应商审核',      'supplier:audit',          2, NULL,                         NULL, NULL, NULL,           1, 1, 1, 1, 1),
(36, 0,  '文件上传',        'file:upload',             2, NULL,                         NULL, NULL, NULL,           1, 1, 1, 1, 1),
(37, 0,  '物流查看',        'logistics:view',          1, NULL,                         NULL, NULL, NULL,           1, 1, 1, 1, 1),
(38, 0,  '供应商订单确认',  'supplier:order:confirm',   2, NULL,                         NULL, NULL, NULL,           1, 1, 1, 1, 1),
(39, 0,  '供应商质量整改',  'supplier:quality:rectify', 2, NULL,                         NULL, NULL, NULL,           1, 1, 1, 1, 1);

-- =====================================================
-- Part 3: 更新角色权限关联
-- =====================================================

-- 清除旧的 SYSTEM_ADMIN 角色权限
DELETE FROM sys_role_permission WHERE role_id = 1;

-- SYSTEM_ADMIN(role_id=1) 拥有全部权限 (id 1-39)
INSERT INTO sys_role_permission (role_id, perm_id, create_by, update_by) VALUES
(1, 1, 1, 1), (1, 2, 1, 1), (1, 3, 1, 1), (1, 4, 1, 1), (1, 5, 1, 1),
(1, 6, 1, 1), (1, 7, 1, 1), (1, 8, 1, 1), (1, 9, 1, 1), (1, 10, 1, 1),
(1, 11, 1, 1), (1, 12, 1, 1), (1, 13, 1, 1), (1, 14, 1, 1), (1, 15, 1, 1),
(1, 16, 1, 1), (1, 17, 1, 1), (1, 18, 1, 1), (1, 19, 1, 1), (1, 20, 1, 1),
(1, 21, 1, 1), (1, 22, 1, 1), (1, 23, 1, 1), (1, 24, 1, 1), (1, 25, 1, 1),
(1, 26, 1, 1), (1, 27, 1, 1), (1, 28, 1, 1), (1, 29, 1, 1), (1, 30, 1, 1),
(1, 31, 1, 1), (1, 32, 1, 1), (1, 33, 1, 1), (1, 34, 1, 1), (1, 35, 1, 1),
(1, 36, 1, 1), (1, 37, 1, 1), (1, 38, 1, 1), (1, 39, 1, 1);

-- 清除旧的 SUPPLIER_ADMIN 角色权限
DELETE FROM sys_role_permission WHERE role_id = 6;

-- SUPPLIER_ADMIN(role_id=6) 拥有供应商侧权限
-- 门户工作台(16), 供应商工作台(17→supplier:dashboard:view), 订单确认(18), ASN创建(19→asn:create)
-- 加上新增的供应商权限
INSERT INTO sys_role_permission (role_id, perm_id, create_by, update_by) VALUES
(6, 16, 1, 1), (6, 17, 1, 1), (6, 18, 1, 1), (6, 19, 1, 1),
(6, 27, 1, 1),   -- message:view
(6, 28, 1, 1),   -- supplier:order:view
(6, 29, 1, 1),   -- supplier:delivery:view
(6, 30, 1, 1),   -- supplier:delivery:create
(6, 31, 1, 1),   -- supplier:quality:view
(6, 32, 1, 1),   -- supplier:settlement:view
(6, 33, 1, 1),   -- supplier:rfq:view
(6, 34, 1, 1),   -- supplier:profile:view
(6, 36, 1, 1),   -- file:upload
(6, 38, 1, 1),   -- supplier:order:confirm
(6, 39, 1, 1);   -- supplier:quality:rectify

-- =====================================================
-- Part 4: 验证
-- =====================================================
SELECT '=== 权限码更新结果 ===' AS '';
SELECT id, perm_name, perm_code, perm_type, path 
FROM sys_permission 
ORDER BY id;

SELECT '=== SYSTEM_ADMIN 拥有的权限码 ===' AS '';
SELECT p.perm_code
FROM sys_role_permission rp
JOIN sys_permission p ON p.id = rp.perm_id
WHERE rp.role_id = 1
ORDER BY p.id;

SELECT '=== SUPPLIER_ADMIN 拥有的权限码 ===' AS '';
SELECT p.perm_code
FROM sys_role_permission rp
JOIN sys_permission p ON p.id = rp.perm_id
WHERE rp.role_id = 6
ORDER BY p.id;

SET FOREIGN_KEY_CHECKS = 1;