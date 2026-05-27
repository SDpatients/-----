USE supplier_collaboration;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO sys_user (id, tenant_id, org_id, username, password, real_name, phone, email, user_type, supplier_id, status, password_update_time, last_login_time, last_login_ip, token_version, remark, create_by, update_by) VALUES
(1, 1, 100, 'admin', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '系统管理员', '13800000001', 'admin@srm.local', 1, NULL, 1, '2026-05-01 09:00:00', '2026-05-22 08:30:00', '127.0.0.1', 0, '平台初始化管理员', 1, 1),
(2, 1, 100, 'buyer01', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '张采购', '13800000002', 'buyer01@srm.local', 1, NULL, 1, '2026-05-01 09:00:00', '2026-05-22 09:10:00', '127.0.0.1', 0, '采购员账号', 1, 1),
(3, 1, 100, 'quality01', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '李质量', '13800000003', 'quality01@srm.local', 1, NULL, 1, '2026-05-01 09:00:00', '2026-05-21 17:10:00', '127.0.0.1', 0, '质量工程师账号', 1, 1),
(4, 1, 100, 'warehouse01', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '王仓储', '13800000004', 'warehouse01@srm.local', 1, NULL, 1, '2026-05-01 09:00:00', '2026-05-21 16:20:00', '127.0.0.1', 0, '仓储收货账号', 1, 1),
(5, 1, 100, 'finance01', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '赵财务', '13800000005', 'finance01@srm.local', 1, NULL, 1, '2026-05-01 09:00:00', '2026-05-21 15:50:00', '127.0.0.1', 0, '财务账号', 1, 1),
(6, 1, NULL, 'supplier001', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '陈供应', '13900000001', 'contact@huaxing.example', 2, 1, 1, '2026-05-01 09:00:00', '2026-05-22 09:30:00', '127.0.0.1', 0, '华兴供应商管理员', 1, 1),
(7, 1, NULL, 'supplier002', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', '周供应', '13900000002', 'contact@xincheng.example', 2, 2, 1, '2026-05-01 09:00:00', '2026-05-20 10:30:00', '127.0.0.1', 0, '新成供应商管理员', 1, 1),
(8, 1, NULL, 'erp_api', '$2a$12$3o0WtGCiupM4U296rmaT5eQ/ozVCYNz6taS65n9gxPu8nGwH2.via', 'ERP接口账号', NULL, 'erp@srm.local', 3, NULL, 1, '2026-05-01 09:00:00', '2026-05-22 08:00:00', '127.0.0.1', 0, 'ERP集成账号', 1, 1);

INSERT INTO sys_role (id, tenant_id, role_name, role_code, role_type, data_scope, description, sort, status, create_by, update_by) VALUES
(1, 1, '系统管理员', 'SYSTEM_ADMIN', 1, 1, '拥有系统全部管理权限', 1, 1, 1, 1),
(2, 1, '采购员', 'BUYER', 1, 2, '采购订单与寻源业务操作', 2, 1, 1, 1),
(3, 1, '质量工程师', 'QUALITY_ENGINEER', 1, 2, '质量检验与异常处理', 3, 1, 1, 1),
(4, 1, '仓储员', 'WAREHOUSE_KEEPER', 1, 2, 'ASN收货与标签管理', 4, 1, 1, 1),
(5, 1, '财务专员', 'FINANCE_SPECIALIST', 1, 2, '对账发票付款管理', 5, 1, 1, 1),
(6, 1, '供应商管理员', 'SUPPLIER_ADMIN', 2, 4, '供应商门户管理权限', 6, 1, 1, 1),
(7, 1, '接口账号', 'INTEGRATION_ACCOUNT', 3, 1, '外部系统集成调用权限', 7, 1, 1, 1);

INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, perm_type, path, component, http_method, icon, sort, visible, status, create_by, update_by) VALUES
(1, 0, '系统管理', 'system', 1, '/system', 'Layout', NULL, 'setting', 1, 1, 1, 1, 1),
(2, 1, '用户管理', 'system:user', 1, '/system/users', 'views/system/UserList.vue', NULL, 'user', 1, 1, 1, 1, 1),
(3, 1, '角色管理', 'system:role', 1, '/system/roles', 'views/system/RoleList.vue', NULL, 'lock', 2, 1, 1, 1, 1),
(4, 0, '供应商管理', 'supplier', 1, '/suppliers', 'Layout', NULL, 'office-building', 2, 1, 1, 1, 1),
(5, 4, '供应商列表', 'supplier:list', 1, '/suppliers/list', 'views/purchasing/SupplierList.vue', NULL, 'list', 1, 1, 1, 1, 1),
(6, 0, '寻源管理', 'sourcing', 1, '/sourcing', 'Layout', NULL, 'search', 3, 1, 1, 1, 1),
(7, 6, 'RFQ询价', 'sourcing:rfq', 1, '/sourcing/rfqs', 'views/purchasing/RfqList.vue', NULL, 'document', 1, 1, 1, 1, 1),
(8, 0, '订单协同', 'order', 1, '/orders', 'Layout', NULL, 'tickets', 4, 1, 1, 1, 1),
(9, 8, '采购订单', 'order:purchase', 1, '/orders/purchase-orders', 'views/purchasing/PurchaseOrderList.vue', NULL, 'shopping-cart', 1, 1, 1, 1, 1),
(10, 0, '物流交付', 'delivery', 1, '/deliveries', 'Layout', NULL, 'truck', 5, 1, 1, 1, 1),
(11, 10, 'ASN管理', 'delivery:asn', 1, '/deliveries/asn', 'views/purchasing/DeliveryNoticeList.vue', NULL, 'van', 1, 1, 1, 1, 1),
(12, 0, '质量协同', 'quality', 1, '/quality', 'Layout', NULL, 'medal', 6, 1, 1, 1, 1),
(13, 12, '质量异常', 'quality:ncr', 1, '/quality/ncrs', 'views/purchasing/NcrList.vue', NULL, 'warning', 1, 1, 1, 1, 1),
(14, 0, '财务结算', 'settlement', 1, '/settlement', 'Layout', NULL, 'money', 7, 1, 1, 1, 1),
(15, 14, '对账管理', 'settlement:reconciliation', 1, '/settlement/reconciliations', 'views/purchasing/ReconciliationList.vue', NULL, 'wallet', 1, 1, 1, 1, 1),
(16, 0, '门户工作台', 'portal', 1, '/portal', 'Layout', NULL, 'home', 8, 1, 1, 1, 1),
(17, 16, '供应商工作台', 'portal:dashboard', 1, '/portal/dashboard', 'views/supplier/Dashboard.vue', NULL, 'data-board', 1, 1, 1, 1, 1),
(18, 9, '订单确认', 'order:confirm', 2, NULL, NULL, NULL, NULL, 10, 1, 1, 1, 1),
(19, 11, 'ASN创建', 'delivery:create', 2, NULL, NULL, NULL, NULL, 10, 1, 1, 1, 1),
(20, 15, '导出对账', 'settlement:export', 2, NULL, NULL, NULL, NULL, 10, 1, 1, 1, 1);

INSERT INTO sys_user_role (id, user_id, role_id, create_by, update_by) VALUES
(1, 1, 1, 1, 1),
(2, 2, 2, 1, 1),
(3, 3, 3, 1, 1),
(4, 4, 4, 1, 1),
(5, 5, 5, 1, 1),
(6, 6, 6, 1, 1),
(7, 7, 6, 1, 1),
(8, 8, 7, 1, 1);

INSERT INTO sys_role_permission (id, role_id, perm_id, create_by, update_by) VALUES
(1, 1, 1, 1, 1), (2, 1, 2, 1, 1), (3, 1, 3, 1, 1), (4, 1, 4, 1, 1), (5, 1, 5, 1, 1), (6, 1, 6, 1, 1), (7, 1, 7, 1, 1), (8, 1, 8, 1, 1), (9, 1, 9, 1, 1), (10, 1, 10, 1, 1), (11, 1, 11, 1, 1), (12, 1, 12, 1, 1), (13, 1, 13, 1, 1), (14, 1, 14, 1, 1), (15, 1, 15, 1, 1), (16, 1, 16, 1, 1), (17, 1, 17, 1, 1), (18, 1, 18, 1, 1), (19, 1, 19, 1, 1), (20, 1, 20, 1, 1),
(21, 2, 4, 1, 1), (22, 2, 5, 1, 1), (23, 2, 6, 1, 1), (24, 2, 7, 1, 1), (25, 2, 8, 1, 1), (26, 2, 9, 1, 1),
(27, 3, 12, 1, 1), (28, 3, 13, 1, 1),
(29, 4, 10, 1, 1), (30, 4, 11, 1, 1),
(31, 5, 14, 1, 1), (32, 5, 15, 1, 1), (33, 5, 20, 1, 1),
(34, 6, 16, 1, 1), (35, 6, 17, 1, 1), (36, 6, 18, 1, 1), (37, 6, 19, 1, 1),
(38, 7, 8, 1, 1), (39, 7, 9, 1, 1), (40, 7, 10, 1, 1);

INSERT INTO sys_dict (id, dict_name, dict_code, description, status, create_by, update_by) VALUES
(1, '采购订单状态', 'purchase_order_status', '采购订单状态字典', 1, 1, 1),
(2, 'ASN状态', 'delivery_status', 'ASN送货通知单状态字典', 1, 1, 1),
(3, '对账状态', 'recon_status', '对账单状态字典', 1, 1, 1),
(4, '供应商状态', 'supplier_status', '供应商准入状态字典', 1, 1, 1),
(5, '质量检验结果', 'inspect_result', '质量检验结果字典', 1, 1, 1);

INSERT INTO sys_dict_item (id, dict_id, item_label, item_value, sort, description, status, create_by, update_by) VALUES
(1, 1, '待下发', '0', 1, NULL, 1, 1, 1), (2, 1, '待确认', '1', 2, NULL, 1, 1, 1), (3, 1, '已确认', '2', 3, NULL, 1, 1, 1), (4, 1, '部分发货', '3', 4, NULL, 1, 1, 1), (5, 1, '已完成', '4', 5, NULL, 1, 1, 1),
(6, 2, '草稿', '0', 1, NULL, 1, 1, 1), (7, 2, '已提交', '1', 2, NULL, 1, 1, 1), (8, 2, '在途', '2', 3, NULL, 1, 1, 1), (9, 2, '已收货', '4', 4, NULL, 1, 1, 1),
(10, 3, '草稿', '0', 1, NULL, 1, 1, 1), (11, 3, '待供应商确认', '1', 2, NULL, 1, 1, 1), (12, 3, '供应商已确认', '2', 3, NULL, 1, 1, 1), (13, 3, '已开票', '4', 4, NULL, 1, 1, 1),
(14, 4, '待审核', '0', 1, NULL, 1, 1, 1), (15, 4, '合作中', '1', 2, NULL, 1, 1, 1), (16, 4, '黑名单', '3', 3, NULL, 1, 1, 1),
(17, 5, '待检', '0', 1, NULL, 1, 1, 1), (18, 5, '合格', '1', 2, NULL, 1, 1, 1), (19, 5, '不合格', '2', 3, NULL, 1, 1, 1), (20, 5, '部分合格', '3', 4, NULL, 1, 1, 1);

INSERT INTO sys_config (id, config_name, config_key, config_value, config_type, encrypted, status, remark, create_by, update_by) VALUES
(1, '系统名称', 'system.name', '供应商协同系统', 1, 0, 1, '系统展示名称', 1, 1),
(2, 'JWT访问令牌有效期', 'jwt.access-token-expire-minutes', '120', 1, 0, 1, '单位分钟', 1, 1),
(3, '导出最大条数', 'export.max.rows', '50000', 2, 0, 1, '异步导出最大行数', 1, 1),
(4, '资质到期提醒天数', 'supplier.qualification.remind-days', '30', 2, 0, 1, '资质预警配置', 1, 1);

INSERT INTO supplier_category (id, category_name, category_code, parent_id, sort, description, status, create_by, update_by) VALUES
(1, '原材料', 'RAW_MATERIAL', 0, 1, '生产原材料供应商', 1, 1, 1),
(2, '电子元器件', 'ELECTRONIC_PARTS', 1, 1, '电子类元件供应商', 1, 1, 1),
(3, '包装材料', 'PACKAGING', 0, 2, '包装辅料供应商', 1, 1, 1),
(4, '物流服务', 'LOGISTICS_SERVICE', 0, 3, '物流承运服务供应商', 1, 1, 1);

INSERT INTO supplier_info (id, tenant_id, org_id, supplier_code, supplier_name, supplier_short_name, category_id, supplier_type, credit_code, legal_person, registered_capital, establish_date, business_scope, province, city, district, address, contact_name, contact_phone, contact_email, bank_name, bank_account, tax_number, invoice_address, invoice_phone, rating, status, submit_time, audit_time, audit_by, audit_remark, remark, create_by, update_by) VALUES
(1, 1, 100, 'SUP202605220001', '深圳市华兴电子有限公司', '华兴电子', 2, 1, '91440300MA5HX001A1', '陈华', 1200.00, '2018-03-12', '电子元器件、连接器、线束生产与销售', '广东省', '深圳市', '宝安区', '航城街道工业园A栋', '陈供应', '13900000001', 'contact@huaxing.example', '招商银行深圳分行', '755900000000001', '91440300MA5HX001A1', '深圳市宝安区航城街道工业园A栋', '0755-88880001', 1, 1, '2026-05-01 09:00:00', '2026-05-02 10:00:00', 2, '资质齐全，准入通过', '核心电子料供应商', 1, 1),
(2, 1, 100, 'SUP202605220002', '苏州新成精密制造有限公司', '新成精密', 1, 1, '91320500MA5HX002B2', '周新', 3000.00, '2015-09-20', '精密结构件、机加工件生产与销售', '江苏省', '苏州市', '吴中区', '吴中经济开发区新成路88号', '周供应', '13900000002', 'contact@xincheng.example', '中国银行苏州分行', '512900000000002', '91320500MA5HX002B2', '苏州市吴中区新成路88号', '0512-88880002', 2, 1, '2026-05-03 09:00:00', '2026-05-04 11:00:00', 2, '审核通过', '结构件供应商', 1, 1),
(3, 1, 100, 'SUP202605220003', '宁波远航包装有限公司', '远航包装', 3, 2, '91330200MA5HX003C3', '林远', 500.00, '2020-01-15', '纸箱、缓冲材料、标签耗材生产与销售', '浙江省', '宁波市', '北仑区', '春晓工业区海天路18号', '林经理', '13900000003', 'contact@yuanhang.example', '宁波银行北仑支行', '574900000000003', '91330200MA5HX003C3', '宁波市北仑区海天路18号', '0574-88880003', 3, 0, '2026-05-18 13:30:00', NULL, NULL, NULL, '待准入审核供应商', 1, 1);

INSERT INTO supplier_contact (id, supplier_id, contact_name, contact_role, contact_phone, contact_email, is_primary, status, remark, create_by, update_by) VALUES
(1, 1, '陈供应', '供应商管理员', '13900000001', 'contact@huaxing.example', 1, 1, '订单与对账联系人', 1, 1),
(2, 1, '何质量', '质量联系人', '13900000011', 'quality@huaxing.example', 0, 1, 'NCR与8D联系人', 1, 1),
(3, 2, '周供应', '供应商管理员', '13900000002', 'contact@xincheng.example', 1, 1, '订单联系人', 1, 1),
(4, 3, '林经理', '业务联系人', '13900000003', 'contact@yuanhang.example', 1, 1, '准入联系人', 1, 1);

INSERT INTO supplier_bank_account (id, supplier_id, account_name, bank_name, bank_branch, bank_account, currency, is_default, status, create_by, update_by) VALUES
(1, 1, '深圳市华兴电子有限公司', '招商银行', '深圳宝安支行', '755900000000001', 'CNY', 1, 1, 1, 1),
(2, 2, '苏州新成精密制造有限公司', '中国银行', '苏州吴中支行', '512900000000002', 'CNY', 1, 1, 1, 1),
(3, 3, '宁波远航包装有限公司', '宁波银行', '北仑支行', '574900000000003', 'CNY', 1, 1, 1, 1);

INSERT INTO sys_file_attachment (id, business_type, business_id, business_no, file_name, file_ext, file_size, content_type, bucket_name, object_key, file_hash, upload_user_id, upload_time, status, create_by, update_by) VALUES
(1, 'supplier_qualification', 1, 'SUP202605220001', '华兴电子营业执照.pdf', 'pdf', 248012, 'application/pdf', 'supplier-qualification', 'qualification/2026/05/01/SUP202605220001/001.pdf', 'hash-hx-license-001', 6, '2026-05-01 09:10:00', 1, 6, 6),
(2, 'supplier_qualification', 2, 'SUP202605220002', '新成精密ISO9001证书.pdf', 'pdf', 198023, 'application/pdf', 'supplier-qualification', 'qualification/2026/05/03/SUP202605220002/001.pdf', 'hash-xc-iso-001', 7, '2026-05-03 09:20:00', 1, 7, 7),
(3, 'delivery_notice', 1, 'ASN202605220001', 'ASN202605220001送货单.pdf', 'pdf', 165210, 'application/pdf', 'supplier-delivery', 'delivery/2026/05/15/ASN202605220001/delivery.pdf', 'hash-asn-001', 6, '2026-05-15 10:00:00', 1, 6, 6),
(4, 'invoice', 1, 'INV202605220001', '发票INV202605220001.pdf', 'pdf', 302004, 'application/pdf', 'supplier-invoice', 'invoice/2026/05/20/INV202605220001/invoice.pdf', 'hash-inv-001', 6, '2026-05-20 10:00:00', 1, 6, 6),
(5, 'eight_d_report', 1, '8D202605220001', '8D整改报告.pdf', 'pdf', 408512, 'application/pdf', 'supplier-quality', 'quality/2026/05/18/NCR202605220001/8d.pdf', 'hash-8d-001', 6, '2026-05-18 15:30:00', 1, 6, 6);

INSERT INTO supplier_qualification (id, supplier_id, qual_type, qual_name, qual_no, qual_org, valid_start, valid_end, file_id, status, remind_days, remark, create_by, update_by) VALUES
(1, 1, 'business_license', '营业执照', 'BL-HX-2026', '深圳市市场监督管理局', '2026-01-01', '2036-01-01', 1, 1, 30, '长期有效', 6, 6),
(2, 1, 'iso9001', 'ISO9001质量管理体系认证', 'ISO-HX-2026', '中国质量认证中心', '2026-01-01', '2028-12-31', NULL, 1, 60, '质量体系认证', 6, 6),
(3, 2, 'iso9001', 'ISO9001质量管理体系认证', 'ISO-XC-2026', '中国质量认证中心', '2026-02-01', '2028-01-31', 2, 1, 60, '质量体系认证', 7, 7),
(4, 3, 'business_license', '营业执照', 'BL-YH-2026', '宁波市市场监督管理局', '2026-01-01', '2036-01-01', NULL, 1, 30, '待审核', 1, 1);

INSERT INTO supplier_blacklist (id, supplier_id, supplier_name, credit_code, reason, start_time, end_time, status, create_by, update_by) VALUES
(1, NULL, '虚拟失信供应商有限公司', '91330000BLACK001', '历史交付严重违约，仅用于黑名单校验演示', '2026-04-01 00:00:00', NULL, 1, 1, 1);

INSERT INTO supplier_performance (id, supplier_id, evaluate_period, quality_score, delivery_score, service_score, price_score, total_score, qualified_rate, ontime_rate, evaluate_by, evaluate_time, remark, create_by, update_by) VALUES
(1, 1, '2026-04', 96.00, 94.00, 92.00, 88.00, 93.00, 0.9820, 0.9600, 2, '2026-05-05 10:00:00', '综合表现优秀', 2, 2),
(2, 2, '2026-04', 90.00, 86.00, 88.00, 91.00, 88.50, 0.9540, 0.9100, 2, '2026-05-05 10:30:00', '交付需继续提升', 2, 2),
(3, 3, '2026-04', 82.00, 80.00, 85.00, 89.00, 84.00, 0.9200, 0.8800, 2, '2026-05-05 11:00:00', '待准入供应商试评估', 2, 2);

INSERT INTO material_info (id, material_code, material_name, material_spec, material_model, category_code, unit, status, create_by, update_by) VALUES
(1, 'MAT-EC-0001', '工业连接器', '12PIN防水', 'HX-CN-12P', 'ELECTRONIC_PARTS', 'PCS', 1, 1, 1),
(2, 'MAT-EC-0002', '控制板线束', '300mm', 'HX-WH-300', 'ELECTRONIC_PARTS', 'PCS', 1, 1, 1),
(3, 'MAT-ME-0001', '铝合金支架', '阳极氧化', 'XC-AL-01', 'RAW_MATERIAL', 'PCS', 1, 1, 1),
(4, 'MAT-PK-0001', '外箱纸箱', '450*320*280', 'YH-BOX-01', 'PACKAGING', 'PCS', 1, 1, 1);

INSERT INTO rfq (id, rfq_no, rfq_title, org_id, currency, quote_deadline, rfq_status, publish_time, close_time, remark, create_by, update_by) VALUES
(1, 'RFQ202605220001', '工业连接器年度询价', 100, 'CNY', '2026-05-28 18:00:00', 2, '2026-05-20 09:00:00', NULL, '用于2026年下半年采购定价', 2, 2),
(2, 'RFQ202605220002', '包装材料季度询价', 100, 'CNY', '2026-05-30 18:00:00', 1, '2026-05-21 10:00:00', NULL, '纸箱与标签耗材询价', 2, 2);

INSERT INTO rfq_item (id, rfq_id, line_no, material_code, material_name, material_spec, unit, quantity, target_delivery_date, remark, create_by, update_by) VALUES
(1, 1, 10, 'MAT-EC-0001', '工业连接器', '12PIN防水', 'PCS', 10000.0000, '2026-06-15', '月度分批交付', 2, 2),
(2, 1, 20, 'MAT-EC-0002', '控制板线束', '300mm', 'PCS', 8000.0000, '2026-06-20', '需附检验报告', 2, 2),
(3, 2, 10, 'MAT-PK-0001', '外箱纸箱', '450*320*280', 'PCS', 20000.0000, '2026-06-10', '按批次交付', 2, 2);

INSERT INTO rfq_supplier (id, rfq_id, supplier_id, invite_status, invite_time, response_time, create_by, update_by) VALUES
(1, 1, 1, 2, '2026-05-20 09:05:00', '2026-05-20 14:20:00', 2, 2),
(2, 1, 2, 1, '2026-05-20 09:05:00', NULL, 2, 2),
(3, 2, 3, 1, '2026-05-21 10:10:00', NULL, 2, 2);

INSERT INTO quote (id, quote_no, rfq_id, supplier_id, currency, exchange_rate, total_amount, tax_amount, quote_status, submit_time, valid_until, remark, create_by, update_by) VALUES
(1, 'QT202605220001', 1, 1, 'CNY', 1.000000, 163600.00, 18826.55, 1, '2026-05-20 14:20:00', '2026-07-31', '华兴电子报价', 6, 6),
(2, 'QT202605220002', 1, 2, 'CNY', 1.000000, 171200.00, 19702.65, 0, NULL, '2026-07-31', '新成精密草稿报价', 7, 7);

INSERT INTO quote_item (id, quote_id, rfq_item_id, material_code, quantity, price, tax_price, tax_rate, amount, tax_amount, delivery_days, remark, create_by, update_by) VALUES
(1, 1, 1, 'MAT-EC-0001', 10000.0000, 8.20, 9.27, 0.1300, 82000.00, 10660.00, 12, '价格含包装', 6, 6),
(2, 1, 2, 'MAT-EC-0002', 8000.0000, 10.20, 11.53, 0.1300, 81600.00, 10608.00, 15, '可分批交付', 6, 6),
(3, 2, 1, 'MAT-EC-0001', 10000.0000, 8.50, 9.61, 0.1300, 85000.00, 11050.00, 14, '草稿报价', 7, 7);

INSERT INTO purchase_order (id, tenant_id, org_id, order_no, erp_order_no, supplier_id, supplier_name, order_date, delivery_date, currency, exchange_rate, total_amount, tax_amount, pay_amount, order_status, submit_time, confirm_time, reject_time, close_time, cancel_time, cancel_reason, buyer_id, buyer_name, contract_no, payment_terms, delivery_address, remark, create_by, update_by) VALUES
(1, 1, 100, 'PO202605220001', 'ERP-PO-202605-001', 1, '深圳市华兴电子有限公司', '2026-05-10', '2026-05-25', 'CNY', 1.000000, 92700.00, 10660.00, 92700.00, 3, '2026-05-10 09:10:00', '2026-05-10 15:30:00', NULL, NULL, NULL, NULL, 2, '张采购', 'CT202605001', '月结30天', '深圳工厂一期仓库', '连接器采购订单', 2, 2),
(2, 1, 100, 'PO202605220002', 'ERP-PO-202605-002', 2, '苏州新成精密制造有限公司', '2026-05-12', '2026-05-28', 'CNY', 1.000000, 67800.00, 7800.00, 67800.00, 2, '2026-05-12 10:00:00', '2026-05-12 16:00:00', NULL, NULL, NULL, NULL, 2, '张采购', 'CT202605002', '月结45天', '苏州工厂成品仓', '结构件采购订单', 2, 2),
(3, 1, 100, 'PO202605220003', 'ERP-PO-202605-003', 1, '深圳市华兴电子有限公司', '2026-05-16', '2026-06-05', 'CNY', 1.000000, 46120.00, 5306.55, 46120.00, 1, '2026-05-16 09:30:00', NULL, NULL, NULL, NULL, NULL, 2, '张采购', 'CT202605003', '月结30天', '深圳工厂二期仓库', '线束补充订单', 2, 2);

INSERT INTO purchase_order_detail (id, order_id, order_no, line_no, material_code, material_name, material_spec, material_model, unit, quantity, price, tax_price, tax_rate, amount, tax_amount, delivered_qty, received_qty, qualified_qty, delivery_date, promise_date, line_status, remark, create_by, update_by) VALUES
(1, 1, 'PO202605220001', 10, 'MAT-EC-0001', '工业连接器', '12PIN防水', 'HX-CN-12P', 'PCS', 10000.0000, 8.20, 9.27, 0.1300, 82000.00, 10660.00, 6000.0000, 5800.0000, 5750.0000, '2026-05-25', '2026-05-24', 2, '首批交付6000', 2, 2),
(2, 2, 'PO202605220002', 10, 'MAT-ME-0001', '铝合金支架', '阳极氧化', 'XC-AL-01', 'PCS', 3000.0000, 20.00, 22.60, 0.1300, 60000.00, 7800.00, 0.0000, 0.0000, 0.0000, '2026-05-28', '2026-05-28', 1, '待发货', 2, 2),
(3, 3, 'PO202605220003', 10, 'MAT-EC-0002', '控制板线束', '300mm', 'HX-WH-300', 'PCS', 4000.0000, 10.20, 11.53, 0.1300, 40800.00, 5306.55, 0.0000, 0.0000, 0.0000, '2026-06-05', NULL, 0, '待供应商确认', 2, 2);

INSERT INTO delivery_plan (id, order_id, order_detail_id, supplier_id, plan_date, plan_qty, promise_date, plan_status, remark, create_by, update_by) VALUES
(1, 1, 1, 1, '2026-05-24', 6000.0000, '2026-05-24', 2, '第一批已发货', 6, 6),
(2, 1, 1, 1, '2026-06-02', 4000.0000, '2026-06-02', 1, '第二批待发货', 6, 6),
(3, 2, 2, 2, '2026-05-28', 3000.0000, '2026-05-28', 1, '按期交付', 7, 7);

INSERT INTO order_change (id, change_no, order_id, order_detail_id, change_type, change_content, before_value, after_value, change_reason, approve_status, approve_by, approve_time, approve_remark, create_by, update_by) VALUES
(1, 'PCO202605220001', 1, 1, 3, '首批交期提前一天', '2026-05-25', '2026-05-24', '供应商产能提前释放', 1, 2, '2026-05-13 11:00:00', '同意提前交付', 6, 2);

INSERT INTO order_status_log (id, order_id, order_no, action_name, before_status, after_status, operator_id, operator_name, operate_time, remark, create_by, update_by) VALUES
(1, 1, 'PO202605220001', '下发', 0, 1, 2, '张采购', '2026-05-10 09:10:00', 'ERP订单同步后下发', 2, 2),
(2, 1, 'PO202605220001', '接单', 1, 2, 6, '陈供应', '2026-05-10 15:30:00', '供应商确认接单', 6, 6),
(3, 1, 'PO202605220001', '创建ASN', 2, 3, 6, '陈供应', '2026-05-15 09:30:00', '首批发货', 6, 6),
(4, 2, 'PO202605220002', '接单', 1, 2, 7, '周供应', '2026-05-12 16:00:00', '供应商确认接单', 7, 7);

INSERT INTO order_track (id, order_id, track_status, track_time, track_remark, operator, operator_name, create_by, update_by) VALUES
(1, 1, 1, '2026-05-10 09:00:00', '订单已创建', 2, '张采购', 2, 2),
(2, 1, 3, '2026-05-10 15:30:00', '供应商已确认', 6, '陈供应', 6, 6),
(3, 1, 5, '2026-05-15 09:30:00', '首批ASN已提交', 6, '陈供应', 6, 6),
(4, 2, 3, '2026-05-12 16:00:00', '供应商已确认', 7, '周供应', 7, 7);

INSERT INTO delivery_notice (id, notice_no, order_id, order_no, supplier_id, supplier_name, plan_delivery_date, actual_delivery_date, delivery_status, delivery_method, delivery_company, delivery_no, driver_name, driver_phone, vehicle_no, delivery_address, receiver, receiver_phone, submit_time, send_time, arrive_time, close_time, remark, create_by, update_by) VALUES
(1, 'ASN202605220001', 1, 'PO202605220001', 1, '深圳市华兴电子有限公司', '2026-05-15', '2026-05-15', 4, '汽运', '顺丰快运', 'SF202605150001', '刘司机', '13700000001', '粤B12345', '深圳工厂一期仓库', '王仓储', '13800000004', '2026-05-15 09:30:00', '2026-05-15 10:00:00', '2026-05-15 15:30:00', '2026-05-16 11:00:00', '首批连接器送货', 6, 6),
(2, 'ASN202605220002', 2, 'PO202605220002', 2, '苏州新成精密制造有限公司', '2026-05-28', NULL, 1, '汽运', '德邦物流', 'DB202605280001', '吴司机', '13700000002', '苏E67890', '苏州工厂成品仓', '王仓储', '13800000004', '2026-05-22 09:00:00', NULL, NULL, NULL, '结构件待发货ASN', 7, 7);

INSERT INTO delivery_detail (id, notice_id, order_detail_id, material_code, material_name, material_spec, unit, plan_qty, actual_qty, received_qty, qualified_qty, batch_no, production_date, box_count, remark, create_by, update_by) VALUES
(1, 1, 1, 'MAT-EC-0001', '工业连接器', '12PIN防水', 'PCS', 6000.0000, 6000.0000, 5800.0000, 5750.0000, 'BATCH-HX-20260515', '2026-05-12', 60, '每箱100PCS', 6, 6),
(2, 2, 2, 'MAT-ME-0001', '铝合金支架', '阳极氧化', 'PCS', 3000.0000, 3000.0000, 0.0000, 0.0000, 'BATCH-XC-20260522', '2026-05-20', 30, '待发货', 7, 7);

INSERT INTO delivery_label (id, label_no, notice_id, delivery_detail_id, material_code, batch_no, package_no, pallet_no, package_qty, qr_content, print_count, last_print_time, create_by, update_by) VALUES
(1, 'LBL202605220001', 1, 1, 'MAT-EC-0001', 'BATCH-HX-20260515', 'PKG-HX-001', 'PLT-HX-001', 100.0000, 'ASN202605220001|MAT-EC-0001|PKG-HX-001', 1, '2026-05-15 09:40:00', 6, 6),
(2, 'LBL202605220002', 1, 1, 'MAT-EC-0001', 'BATCH-HX-20260515', 'PKG-HX-002', 'PLT-HX-001', 100.0000, 'ASN202605220001|MAT-EC-0001|PKG-HX-002', 1, '2026-05-15 09:41:00', 6, 6),
(3, 'LBL202605220003', 2, 2, 'MAT-ME-0001', 'BATCH-XC-20260522', 'PKG-XC-001', 'PLT-XC-001', 100.0000, 'ASN202605220002|MAT-ME-0001|PKG-XC-001', 1, '2026-05-22 09:05:00', 7, 7);

INSERT INTO receipt_record (id, receipt_no, delivery_id, notice_id, supplier_id, material_code, material_name, plan_qty, receipt_qty, reject_qty, receipt_time, receiver, receiver_name, warehouse_id, warehouse_name, location, receipt_status, diff_reason, reject_reason, remark, create_by, update_by) VALUES
(1, 'GR202605220001', 1, 1, 1, 'MAT-EC-0001', '工业连接器', 6000.0000, 5800.0000, 200.0000, '2026-05-16 10:00:00', 4, '王仓储', 101, '深圳一期原料仓', 'A01-01', 3, '运输外箱破损导致短收', '外观损坏拒收', '已触发质检', 4, 4);

INSERT INTO vmi_inventory (id, supplier_id, material_code, warehouse_id, warehouse_name, onhand_qty, available_qty, safety_qty, max_qty, inventory_status, last_sync_time, create_by, update_by) VALUES
(1, 1, 'MAT-EC-0001', 101, '深圳一期原料仓', 5750.0000, 5700.0000, 1000.0000, 12000.0000, 1, '2026-05-22 08:00:00', 8, 8),
(2, 2, 'MAT-ME-0001', 102, '苏州成品仓', 0.0000, 0.0000, 500.0000, 5000.0000, 3, '2026-05-22 08:00:00', 8, 8);

INSERT INTO forecast_demand (id, demand_no, supplier_id, material_code, demand_date, demand_qty, demand_type, demand_status, create_by, update_by) VALUES
(1, 'FD202605220001', 1, 'MAT-EC-0001', '2026-06-10', 4000.0000, 1, 1, 2, 2),
(2, 'FD202605220002', 2, 'MAT-ME-0001', '2026-06-15', 2500.0000, 3, 0, 2, 2);

INSERT INTO inspection_standard (id, standard_no, material_code, material_name, standard_name, sample_rule, version_no, status, remark, create_by, update_by) VALUES
(1, 'IQC-STD-EC-0001', 'MAT-EC-0001', '工业连接器', '工业连接器来料检验标准', 'GB/T 2828.1 II级 AQL1.0', 'V1.0', 1, '外观、电气性能、尺寸检验', 3, 3),
(2, 'IQC-STD-ME-0001', 'MAT-ME-0001', '铝合金支架', '铝合金支架来料检验标准', 'GB/T 2828.1 II级 AQL1.5', 'V1.0', 1, '尺寸、外观、表面处理检验', 3, 3);

INSERT INTO inspection_standard_item (id, standard_id, item_name, item_type, standard_value, upper_limit, lower_limit, unit, required, sort, create_by, update_by) VALUES
(1, 1, '外观检查', 1, '无破损、无氧化、针脚无变形', NULL, NULL, NULL, 1, 1, 3, 3),
(2, 1, '绝缘电阻', 2, '≥100', NULL, 100.0000, 'MΩ', 1, 2, 3, 3),
(3, 2, '长度尺寸', 2, '100±0.2', 100.2000, 99.8000, 'mm', 1, 1, 3, 3),
(4, 2, '表面处理', 1, '阳极氧化均匀无划伤', NULL, NULL, NULL, 1, 2, 3, 3);

INSERT INTO quality_inspection (id, inspection_no, receipt_id, delivery_id, supplier_id, material_code, material_name, inspect_qty, qualified_qty, unqualified_qty, inspect_result, inspect_type, inspect_time, inspector, inspector_name, inspect_remark, handle_method, handle_remark, create_by, update_by) VALUES
(1, 'QI202605220001', 1, 1, 1, 'MAT-EC-0001', '工业连接器', 5800.0000, 5750.0000, 50.0000, 3, 1, '2026-05-16 14:30:00', 3, '李质量', '抽检发现50PCS针脚轻微变形', 5, '不合格品隔离并要求供应商提交8D', 3, 3);

INSERT INTO nonconformance_report (id, ncr_no, inspection_id, receipt_id, supplier_id, material_code, material_name, unqualified_qty, problem_desc, severity, handle_method, ncr_status, submit_time, close_time, close_remark, create_by, update_by) VALUES
(1, 'NCR202605220001', 1, 1, 1, 'MAT-EC-0001', '工业连接器', 50.0000, '来料工业连接器针脚轻微变形，影响装配稳定性。', 2, 5, 2, '2026-05-16 16:00:00', NULL, NULL, 3, 3);

INSERT INTO eight_d_report (id, report_no, ncr_id, supplier_id, d1_team, d2_problem, d3_containment, d4_root_cause, d5_corrective_action, d6_validate_action, d7_prevent_action, d8_close_summary, due_date, report_status, submit_time, audit_time, close_time, create_by, update_by) VALUES
(1, '8D202605220001', 1, 1, '供应商质量、制程、仓储、采购组成临时小组', '连接器针脚在运输后出现轻微变形', '库存批次全检，异常品隔离，后续出货增加泡棉防护', '周转箱内部隔板间隙过大，长途运输产生挤压', '更换定制隔板并增加出货前跌落测试', '试运行三批次未再发现针脚变形', '包装规范纳入SOP并培训仓储人员', NULL, '2026-05-25', 2, '2026-05-18 15:30:00', '2026-05-19 10:00:00', NULL, 6, 6);

INSERT INTO quality_appeal (id, appeal_no, ncr_id, inspection_id, supplier_id, appeal_reason, appeal_status, submit_time, audit_by, audit_time, audit_remark, create_by, update_by) VALUES
(1, 'QA202605220001', 1, 1, 1, '供应商申请复核不合格数量，认为其中10PCS可返工使用。', 2, '2026-05-18 10:00:00', 3, '2026-05-19 09:30:00', '待复检确认', 6, 3);

INSERT INTO reconciliation (id, recon_no, supplier_id, supplier_name, recon_period, start_date, end_date, total_amount, confirmed_amount, diff_amount, recon_status, send_time, confirm_time, confirm_by, close_time, remark, create_by, update_by) VALUES
(1, 'REC202605220001', 1, '深圳市华兴电子有限公司', '2026-05', '2026-05-01', '2026-05-31', 53766.00, 53306.00, 460.00, 4, '2026-05-19 09:00:00', '2026-05-19 15:30:00', 6, NULL, '5月首批收货对账，含质量扣款', 5, 5),
(2, 'REC202605220002', 2, '苏州新成精密制造有限公司', '2026-05', '2026-05-01', '2026-05-31', 0.00, 0.00, 0.00, 0, NULL, NULL, NULL, NULL, '待收货后生成明细', 5, 5);

INSERT INTO reconciliation_detail (id, recon_id, order_id, order_no, receipt_id, receipt_no, material_code, material_name, quantity, unit_price, order_amount, deduction_amount, confirmed_amount, diff_amount, diff_reason, confirm_status, confirm_time, remark, create_by, update_by) VALUES
(1, 1, 1, 'PO202605220001', 1, 'GR202605220001', 'MAT-EC-0001', '工业连接器', 5800.0000, 9.27, 53766.00, 460.00, 53306.00, 460.00, '50PCS质量扣款', 1, '2026-05-19 15:30:00', '供应商已确认', 5, 5);

INSERT INTO deduction (id, deduction_no, supplier_id, source_type, source_id, deduction_type, deduction_amount, deduction_reason, deduction_status, recon_id, create_by, update_by) VALUES
(1, 'DED202605220001', 1, 'NCR', 1, 1, 460.00, '针脚变形质量异常扣款', 2, 1, 3, 3);

INSERT INTO invoice (id, invoice_no, invoice_code, invoice_type, recon_id, supplier_id, supplier_name, tax_number, invoice_amount, tax_amount, tax_rate, invoice_date, invoice_status, receive_time, certify_time, void_time, void_reason, file_id, ocr_status, remark, create_by, update_by) VALUES
(1, 'INV202605220001', '044002600111', 1, 1, 1, '深圳市华兴电子有限公司', '91440300MA5HX001A1', 53306.00, 6130.78, 0.1300, '2026-05-20', 3, '2026-05-20 10:00:00', '2026-05-20 15:00:00', NULL, NULL, 4, 2, '对账确认后开票', 6, 5);

INSERT INTO payment (id, payment_no, invoice_id, invoice_no, supplier_id, supplier_name, payment_amount, payment_method, payment_account, payment_bank, receive_account, receive_bank, payment_time, payment_status, receipt_no, approve_status, voucher_no, remark, create_by, update_by) VALUES
(1, 'PAY202605220001', 1, 'INV202605220001', 1, '深圳市华兴电子有限公司', 30000.00, 1, '1000000000000001', '招商银行深圳分行', '755900000000001', '招商银行深圳宝安支行', '2026-05-21 16:00:00', 1, 'FIN-RCPT-202605210001', 1, 'VCH202605210001', '部分付款', 5, 5);

INSERT INTO sys_idempotent_record (id, idempotent_key, business_type, business_id, request_hash, result_code, result_message, expire_time, status, create_by, update_by) VALUES
(1, 'ERP_ORDER_ERP-PO-202605-001_order.created', 'purchase_order', 1, 'sha256-order-001', '200', '订单同步成功', '2026-06-22 00:00:00', 1, 8, 8),
(2, 'PAYMENT_FIN-RCPT-202605210001', 'payment', 1, 'sha256-payment-001', '200', '付款回传成功', '2026-06-22 00:00:00', 1, 8, 8);

INSERT INTO sys_export_task (id, task_no, task_type, file_id, export_params, total_count, processed_count, task_status, error_message, start_time, finish_time, create_by, update_by) VALUES
(1, 'EXP202605220001', 'reconciliation_export', NULL, '{"reconPeriod":"2026-05","supplierId":1}', 1, 1, 2, NULL, '2026-05-22 09:00:00', '2026-05-22 09:00:05', 5, 5);

INSERT INTO sys_audit_log (id, trace_id, user_id, username, module_name, business_type, business_id, business_no, action_name, before_status, after_status, request_method, request_path, client_ip, result_status, error_message, operate_time, create_by, update_by) VALUES
(1, 'trace-po-001', 2, 'buyer01', '订单协同', 'purchase_order', 1, 'PO202605220001', '下发订单', 0, 1, 'POST', '/api/v1/purchase-orders/1/publish', '127.0.0.1', 1, NULL, '2026-05-10 09:10:00', 2, 2),
(2, 'trace-asn-001', 6, 'supplier001', '物流交付', 'delivery_notice', 1, 'ASN202605220001', '提交ASN', 0, 1, 'POST', '/api/v1/delivery-notices/1/submit', '127.0.0.1', 1, NULL, '2026-05-15 09:30:00', 6, 6),
(3, 'trace-ncr-001', 3, 'quality01', '质量协同', 'nonconformance_report', 1, 'NCR202605220001', '发布NCR', 0, 1, 'POST', '/api/v1/ncrs/1/submit', '127.0.0.1', 1, NULL, '2026-05-16 16:00:00', 3, 3);

INSERT INTO sys_login_log (id, username, user_id, login_type, client_ip, user_agent, login_status, fail_reason, login_time, create_by, update_by) VALUES
(1, 'admin', 1, 1, '127.0.0.1', 'Mozilla/5.0 Mock Browser', 1, NULL, '2026-05-22 08:30:00', 1, 1),
(2, 'supplier001', 6, 1, '127.0.0.1', 'Mozilla/5.0 Mock Browser', 1, NULL, '2026-05-22 09:30:00', 6, 6),
(3, 'unknown', NULL, 1, '127.0.0.1', 'Mozilla/5.0 Mock Browser', 0, '用户不存在', '2026-05-22 09:35:00', NULL, NULL);

INSERT INTO portal_todo (id, user_id, supplier_id, todo_type, business_type, business_id, business_no, title, todo_status, due_time, finish_time, create_by, update_by) VALUES
(1, 6, 1, 'order_confirm', 'purchase_order', 3, 'PO202605220003', '待确认采购订单PO202605220003', 0, '2026-05-23 18:00:00', NULL, 2, 2),
(2, 6, 1, 'eight_d_submit', 'nonconformance_report', 1, 'NCR202605220001', '待完善8D整改报告', 0, '2026-05-25 18:00:00', NULL, 3, 3),
(3, 5, NULL, 'payment_approve', 'payment', 1, 'PAY202605220001', '待跟进付款单部分付款', 1, '2026-05-22 18:00:00', '2026-05-21 16:10:00', 5, 5);

INSERT INTO message_notice (id, notice_no, receiver_user_id, receiver_supplier_id, channel, title, content, business_type, business_id, send_status, read_status, send_time, read_time, retry_count, error_message, create_by, update_by) VALUES
(1, 'MSG202605220001', 6, 1, 1, '新采购订单待确认', '采购订单PO202605220003已下发，请及时确认。', 'purchase_order', 3, 1, 0, '2026-05-16 09:35:00', NULL, 0, NULL, 2, 2),
(2, 'MSG202605220002', 6, 1, 1, '质量异常待处理', 'NCR202605220001已发布，请提交整改措施。', 'nonconformance_report', 1, 1, 1, '2026-05-16 16:05:00', '2026-05-18 09:00:00', 0, NULL, 3, 3),
(3, 'MSG202605220003', 4, NULL, 1, 'ASN待收货', 'ASN202605220002已提交，请关注到货。', 'delivery_notice', 2, 1, 0, '2026-05-22 09:05:00', NULL, 0, NULL, 7, 7);

INSERT INTO message_event_log (id, event_id, event_type, source, trace_id, business_type, business_id, business_no, payload, publish_status, consume_status, retry_count, error_message, occurred_at, create_by, update_by) VALUES
(1, 'evt-202605220001', 'order.purchase.created', 'supplier-collaboration', 'trace-po-001', 'purchase_order', 1, 'PO202605220001', '{"businessId":1,"businessNo":"PO202605220001"}', 1, 1, 0, NULL, '2026-05-10 09:10:00', 2, 2),
(2, 'evt-202605220002', 'delivery.asn.created', 'supplier-collaboration', 'trace-asn-001', 'delivery_notice', 1, 'ASN202605220001', '{"businessId":1,"businessNo":"ASN202605220001"}', 1, 1, 0, NULL, '2026-05-15 09:30:00', 6, 6),
(3, 'evt-202605220003', 'quality.ncr.created', 'supplier-collaboration', 'trace-ncr-001', 'nonconformance_report', 1, 'NCR202605220001', '{"businessId":1,"businessNo":"NCR202605220001"}', 1, 1, 0, NULL, '2026-05-16 16:00:00', 3, 3);

INSERT INTO integration_endpoint (id, endpoint_code, endpoint_name, system_type, integration_mode, base_url, auth_type, timeout_ms, retry_limit, status, remark, create_by, update_by) VALUES
(1, 'ERP_ORDER_API', 'ERP采购订单接口', 'ERP', 'REST', 'http://erp.mock.local/api/orders', 'AK_SK', 30000, 3, 1, '接收ERP采购订单', 1, 1),
(2, 'WMS_RECEIPT_API', 'WMS收货回传接口', 'WMS', 'REST', 'http://wms.mock.local/api/receipts', 'TOKEN', 30000, 3, 1, '同步收货结果', 1, 1),
(3, 'FIN_PAYMENT_API', '财务付款回传接口', 'FINANCE', 'REST', 'http://finance.mock.local/api/payments', 'TOKEN', 30000, 3, 1, '付款状态同步', 1, 1);

INSERT INTO integration_log (id, trace_id, endpoint_code, system_type, interface_code, direction, business_type, business_id, request_summary, response_summary, result_status, error_message, cost_ms, retry_count, create_by, update_by) VALUES
(1, 'trace-po-001', 'ERP_ORDER_API', 'ERP', 'syncPurchaseOrder', 1, 'purchase_order', 1, '接收ERP采购订单ERP-PO-202605-001', '创建平台订单PO202605220001成功', 1, NULL, 138, 0, 8, 8),
(2, 'trace-gr-001', 'WMS_RECEIPT_API', 'WMS', 'syncReceipt', 2, 'receipt_record', 1, '推送收货单GR202605220001', 'WMS接收成功', 1, NULL, 221, 0, 8, 8),
(3, 'trace-pay-001', 'FIN_PAYMENT_API', 'FINANCE', 'syncPayment', 1, 'payment', 1, '接收付款回执FIN-RCPT-202605210001', '付款状态更新成功', 1, NULL, 96, 0, 8, 8);

INSERT INTO integration_sync_task (id, task_no, system_type, task_type, external_no, event_type, payload, task_status, retry_count, next_retry_time, error_message, create_by, update_by) VALUES
(1, 'SYNC202605220001', 'ERP', 'purchase_order_import', 'ERP-PO-202605-001', 'order.purchase.created', '{"erpOrderNo":"ERP-PO-202605-001","orderNo":"PO202605220001"}', 2, 0, NULL, NULL, 8, 8),
(2, 'SYNC202605220002', 'WMS', 'receipt_push', 'GR202605220001', 'delivery.received', '{"receiptNo":"GR202605220001","noticeNo":"ASN202605220001"}', 2, 0, NULL, NULL, 8, 8),
(3, 'SYNC202605220003', 'FINANCE', 'payment_callback', 'FIN-RCPT-202605210001', 'settlement.payment.synced', '{"paymentNo":"PAY202605220001","receiptNo":"FIN-RCPT-202605210001"}', 2, 0, NULL, NULL, 8, 8);

SET FOREIGN_KEY_CHECKS = 1;
