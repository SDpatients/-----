-- =====================================================
-- 前后端权限码对齐脚本 (data.sql 架构 / schema.sql 表结构)
-- 将数据库 perm_code 对齐到前端路由所需的权限码
-- 不修改已有权限，仅新增前端需要的权限并分配给 SUPER_ADMIN
-- =====================================================

USE supplier_collaboration;
SET NAMES utf8mb4;

-- =====================================================
-- Part 1: 新增前端路由需要的权限（ID 从 600 开始，避免与现有 1-523 冲突）
-- =====================================================

INSERT INTO sys_permission (id, perm_name, perm_code, perm_type, parent_id, path, component, icon, sort, visible, status, create_by) VALUES

-- 采购方门户权限
(600, '采购工作台',    'dashboard:view',          1, 0, '/purchasing/dashboard',      NULL, 'data-board',     1, 1, 1, 1),
(601, '供应商管理',    'supplier:view',           1, 0, '/purchasing/suppliers',      NULL, 'office-building', 2, 1, 1, 1),
(602, '订单管理',      'order:view',              1, 0, '/purchasing/orders',         NULL, 'tickets',        3, 1, 1, 1),
(603, '物流交付',      'asn:view',                1, 0, '/purchasing/delivery',       NULL, 'truck',          4, 1, 1, 1),
(604, '创建ASN',       'asn:create',              1, 0, '/purchasing/delivery/create', NULL, 'plus',           5, 1, 1, 1),
(605, '质量检验',      'quality:view',            1, 0, '/purchasing/quality',        NULL, 'medal',          6, 1, 1, 1),
(606, '对账管理',      'settlement:view',         1, 0, '/purchasing/settlement',     NULL, 'money',          7, 1, 1, 1),
(607, '财务管理',      'finance:view',            1, 0, '/purchasing/finance',        NULL, 'wallet',         8, 1, 1, 1),
(608, 'RFQ询价',       'rfq:view',                1, 0, '/purchasing/rfq',            NULL, 'document',       9, 1, 1, 1),
(609, '报价对比',      'quote:view',              1, 0, '/purchasing/quote-compare',  NULL, 'trend-charts',  10, 1, 1, 1),
(610, 'VMI需求预测',   'inventory:view',          1, 0, '/purchasing/vmi-forecast',   NULL, 'box',           11, 1, 1, 1),
(611, '集成网关',      'integration:view',        1, 0, '/purchasing/integration',    NULL, 'connection',    12, 1, 1, 1),
(612, '系统配置',      'config:view',             1, 0, '/purchasing/config',         NULL, 'setting',       13, 1, 1, 1),
(613, '消息待办',      'message:view',            1, 0, '/messages',                  NULL, 'bell',          14, 1, 1, 1),

-- 供应商门户权限
(620, '供应商工作台',    'supplier:dashboard:view',   1, 0, '/supplier/dashboard',       NULL, 'data-board',  1, 1, 1, 1),
(621, '供应商订单中心',  'supplier:order:view',       1, 0, '/supplier/orders',          NULL, 'tickets',     2, 1, 1, 1),
(622, '供应商发货中心',  'supplier:delivery:view',    1, 0, '/supplier/delivery',        NULL, 'truck',       3, 1, 1, 1),
(623, '供应商创建发货',  'supplier:delivery:create',  1, 0, '/supplier/delivery/create', NULL, 'plus',        4, 1, 1, 1),
(624, '供应商质量中心',  'supplier:quality:view',     1, 0, '/supplier/quality',         NULL, 'medal',       5, 1, 1, 1),
(625, '供应商财务中心',  'supplier:settlement:view',  1, 0, '/supplier/settlement',      NULL, 'money',       6, 1, 1, 1),
(626, '供应商RFQ报价',   'supplier:rfq:view',         1, 0, '/supplier/rfq',             NULL, 'document',    7, 1, 1, 1),
(627, '供应商资料中心',  'supplier:profile:view',     1, 0, '/supplier/profile',         NULL, 'user',        8, 1, 1, 1),

-- 扩展权限
(630, '供应商审核',      'supplier:audit',          2, 0, NULL, NULL, NULL, 1, 1, 1, 1),
(631, '文件上传',        'file:upload',             2, 0, NULL, NULL, NULL, 1, 1, 1, 1),
(632, '物流查看',        'logistics:view',          1, 0, NULL, NULL, NULL, 1, 1, 1, 1),
(633, '供应商订单确认',  'supplier:order:confirm',   2, 0, NULL, NULL, NULL, 1, 1, 1, 1),
(634, '供应商质量整改',  'supplier:quality:rectify', 2, 0, NULL, NULL, NULL, 1, 1, 1, 1);

-- =====================================================
-- Part 2: 为角色分配新权限
-- =====================================================

-- SUPER_ADMIN(role_id=1) 拥有全部新权限
INSERT INTO sys_role_permission (role_id, perm_id, create_by) VALUES
(1, 600, 1), (1, 601, 1), (1, 602, 1), (1, 603, 1), (1, 604, 1),
(1, 605, 1), (1, 606, 1), (1, 607, 1), (1, 608, 1), (1, 609, 1),
(1, 610, 1), (1, 611, 1), (1, 612, 1), (1, 613, 1),
(1, 620, 1), (1, 621, 1), (1, 622, 1), (1, 623, 1), (1, 624, 1),
(1, 625, 1), (1, 626, 1), (1, 627, 1),
(1, 630, 1), (1, 631, 1), (1, 632, 1), (1, 633, 1), (1, 634, 1);

-- SUPPLIER(role_id=4) 拥有供应商侧权限
INSERT INTO sys_role_permission (role_id, perm_id, create_by) VALUES
(4, 613, 1), (4, 620, 1), (4, 621, 1), (4, 622, 1), (4, 623, 1),
(4, 624, 1), (4, 625, 1), (4, 626, 1), (4, 627, 1),
(4, 631, 1), (4, 633, 1), (4, 634, 1);

-- =====================================================
-- Part 3: 验证
-- =====================================================
SELECT '=== 新增权限码 ===' AS '';
SELECT id, perm_name, perm_code, perm_type FROM sys_permission WHERE id >= 600 ORDER BY id;

SELECT '=== SUPER_ADMIN 拥有的前端权限 ===' AS '';
SELECT p.perm_code FROM sys_role_permission rp
JOIN sys_permission p ON p.id = rp.perm_id
WHERE rp.role_id = 1 AND p.id >= 600
ORDER BY p.id;

SELECT '=== SUPPLIER 拥有的前端权限 ===' AS '';
SELECT p.perm_code FROM sys_role_permission rp
JOIN sys_permission p ON p.id = rp.perm_id
WHERE rp.role_id = 4 AND p.id >= 600
ORDER BY p.id;