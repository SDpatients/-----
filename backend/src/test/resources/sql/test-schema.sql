-- =============================================================
-- 测试用精简 Schema（H2 兼容）- 匹配实际实体字段名
-- =============================================================

-- 用户表 (匹配 SysUser 实体)
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    user_type INT DEFAULT 0,
    supplier_id BIGINT,
    status TINYINT DEFAULT 1,
    last_login_time DATETIME,
    last_login_ip VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

-- 角色表 (匹配 SysRole 实体)
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 物料主数据表 (匹配 MaterialInfo 实体)
CREATE TABLE IF NOT EXISTS material_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    spec VARCHAR(200),
    unit VARCHAR(20) DEFAULT 'PCS',
    category VARCHAR(50),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_material_code UNIQUE (material_code)
);

-- 权限表 (匹配 SysPermission 实体)
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_name VARCHAR(50) NOT NULL,
    perm_code VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    perm_type INT DEFAULT 0,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(100),
    sort INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 用户角色关联
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

-- 角色权限关联
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_role_perm UNIQUE (role_id, perm_id)
);

-- 供应商信息表 (匹配 SupplierInfo 实体)
CREATE TABLE IF NOT EXISTS supplier_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    supplier_short_name VARCHAR(50),
    category_id BIGINT,
    supplier_type INT,
    credit_code VARCHAR(50),
    legal_person VARCHAR(50),
    registered_capital DECIMAL(18,2),
    establish_date DATE,
    business_scope VARCHAR(500),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    address VARCHAR(255),
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_number VARCHAR(50),
    invoice_address VARCHAR(255),
    invoice_phone VARCHAR(20),
    rating INT,
    status TINYINT DEFAULT 0,
    audit_time DATETIME,
    audit_by BIGINT,
    audit_remark VARCHAR(500),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_supplier_code UNIQUE (supplier_code)
);

-- 供应商黑名单表
CREATE TABLE IF NOT EXISTS supplier_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credit_code VARCHAR(50) NOT NULL,
    company_name VARCHAR(100),
    reason VARCHAR(500),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    version INT DEFAULT 0
);

-- 采购订单表 (匹配 PurchaseOrder 实体)
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT,
    order_date DATE,
    delivery_date DATE,
    currency VARCHAR(10) DEFAULT 'CNY',
    total_amount DECIMAL(18,2) DEFAULT 0,
    tax_amount DECIMAL(18,2) DEFAULT 0,
    discount_amount DECIMAL(18,2) DEFAULT 0,
    pay_amount DECIMAL(18,2) DEFAULT 0,
    order_status INT DEFAULT 0,
    confirm_time DATETIME,
    confirm_by BIGINT,
    complete_time DATETIME,
    cancel_time DATETIME,
    cancel_reason VARCHAR(500),
    buyer_id BIGINT,
    buyer_name VARCHAR(50),
    dept_id BIGINT,
    dept_name VARCHAR(50),
    contract_no VARCHAR(50),
    payment_terms VARCHAR(200),
    delivery_address VARCHAR(255),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);

-- 采购订单明细表 (匹配 PurchaseOrderDetail 实体)
CREATE TABLE IF NOT EXISTS purchase_order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    line_no INT DEFAULT 0,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    material_spec VARCHAR(200),
    material_model VARCHAR(200),
    unit VARCHAR(20),
    quantity DECIMAL(18,4) DEFAULT 0,
    unit_price DECIMAL(18,2) DEFAULT 0,
    tax_rate DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(18,2) DEFAULT 0,
    amount DECIMAL(18,2) DEFAULT 0,
    delivered_qty DECIMAL(18,4) DEFAULT 0,
    received_qty DECIMAL(18,4) DEFAULT 0,
    qualified_qty DECIMAL(18,4) DEFAULT 0,
    delivery_date DATE,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- ASN 通知表 (匹配 DeliveryNotice 实体)
CREATE TABLE IF NOT EXISTS delivery_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_no VARCHAR(50) NOT NULL,
    order_id BIGINT,
    order_no VARCHAR(50),
    supplier_id BIGINT,
    supplier_name VARCHAR(100),
    plan_delivery_date DATE,
    actual_delivery_date DATE,
    delivery_status INT DEFAULT 0,
    delivery_method VARCHAR(50),
    delivery_company VARCHAR(100),
    delivery_no VARCHAR(50),
    driver_name VARCHAR(50),
    driver_phone VARCHAR(20),
    vehicle_no VARCHAR(50),
    delivery_address VARCHAR(255),
    receiver VARCHAR(50),
    receiver_phone VARCHAR(20),
    send_time DATETIME,
    arrive_time DATETIME,
    batch_no VARCHAR(50),
    production_date DATE,
    expiry_date DATE,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_notice_no UNIQUE (notice_no)
);

-- ASN 明细表 (匹配 DeliveryDetail 实体)
CREATE TABLE IF NOT EXISTS delivery_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    order_detail_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    material_spec VARCHAR(200),
    unit VARCHAR(20),
    plan_qty DECIMAL(18,4) DEFAULT 0,
    actual_qty DECIMAL(18,4) DEFAULT 0,
    received_qty DECIMAL(18,4) DEFAULT 0,
    qualified_qty DECIMAL(18,4) DEFAULT 0,
    batch_no VARCHAR(50),
    production_date DATE,
    expiry_date DATE,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 收货记录表 (匹配 ReceiptRecord 实体)
CREATE TABLE IF NOT EXISTS receipt_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT,
    notice_id BIGINT,
    supplier_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    plan_qty DECIMAL(18,4) DEFAULT 0,
    receipt_qty DECIMAL(18,4) DEFAULT 0,
    reject_qty DECIMAL(18,4) DEFAULT 0,
    receipt_time DATETIME,
    receiver BIGINT,
    receiver_name VARCHAR(50),
    warehouse_id BIGINT,
    warehouse_name VARCHAR(100),
    location VARCHAR(100),
    receipt_status INT DEFAULT 0,
    reject_reason VARCHAR(500),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 质量检验表 (匹配 QualityInspection 实体)
CREATE TABLE IF NOT EXISTS quality_inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inspection_no VARCHAR(50) NOT NULL,
    receipt_id BIGINT,
    delivery_id BIGINT,
    supplier_id BIGINT,
    standard_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    inspect_qty DECIMAL(18,4) DEFAULT 0,
    qualified_qty DECIMAL(18,4) DEFAULT 0,
    unqualified_qty DECIMAL(18,4) DEFAULT 0,
    inspect_result INT,
    inspect_type INT,
    inspect_time DATETIME,
    inspector BIGINT,
    inspector_name VARCHAR(50),
    inspect_remark VARCHAR(500),
    handle_method INT,
    handle_remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_inspection_no UNIQUE (inspection_no)
);

-- 对账单表 (匹配 Reconciliation 实体)
CREATE TABLE IF NOT EXISTS reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recon_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT,
    supplier_name VARCHAR(100),
    recon_period VARCHAR(50),
    start_date DATE,
    end_date DATE,
    total_amount DECIMAL(18,2) DEFAULT 0,
    confirmed_amount DECIMAL(18,2) DEFAULT 0,
    diff_amount DECIMAL(18,2) DEFAULT 0,
    recon_status INT DEFAULT 0,
    send_time DATETIME,
    confirm_time DATETIME,
    confirm_by BIGINT,
    confirm_remark VARCHAR(500),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_recon_no UNIQUE (recon_no)
);

-- 幂等记录表
CREATE TABLE IF NOT EXISTS sys_idempotent_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotent_key VARCHAR(200) NOT NULL,
    business_type VARCHAR(50),
    business_id BIGINT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    CONSTRAINT uk_idempotent_key UNIQUE (idempotent_key)
);

-- 订单跟踪表 (匹配 OrderTrack 实体)
CREATE TABLE IF NOT EXISTS order_track (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    track_status INT DEFAULT 0,
    track_time DATETIME,
    track_remark VARCHAR(500),
    operator BIGINT,
    operator_name VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 业务状态跟踪表 (匹配 BizStatusTrack 实体)
CREATE TABLE IF NOT EXISTS biz_status_track (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_type VARCHAR(50),
    business_id BIGINT,
    before_status INT,
    after_status INT,
    track_remark VARCHAR(500),
    operator BIGINT,
    operator_name VARCHAR(50),
    operate_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_id VARCHAR(50),
    user_id BIGINT,
    username VARCHAR(50),
    business_type VARCHAR(50),
    business_id BIGINT,
    business_no VARCHAR(50),
    action_name VARCHAR(100),
    before_status TINYINT,
    after_status TINYINT,
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    operate_ip VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 门户待办表 (匹配 PortalTodo 实体)
CREATE TABLE IF NOT EXISTS portal_todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    supplier_id BIGINT,
    todo_type VARCHAR(50),
    business_type VARCHAR(50),
    business_id BIGINT,
    business_no VARCHAR(50),
    title VARCHAR(200),
    todo_status INT DEFAULT 0,
    due_time DATETIME,
    finish_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 不合格报告表 (匹配 NonconformanceReport 实体)
CREATE TABLE IF NOT EXISTS nonconformance_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ncr_no VARCHAR(50),
    inspection_id BIGINT,
    receipt_id BIGINT,
    supplier_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    unqualified_qty DECIMAL(18,4) DEFAULT 0,
    problem_desc VARCHAR(500),
    severity INT,
    handle_method INT,
    handle_detail VARCHAR(500),
    handle_remark VARCHAR(500),
    ncr_status INT DEFAULT 0,
    submit_time DATETIME,
    close_time DATETIME,
    close_remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 8D整改报告表
CREATE TABLE IF NOT EXISTS eight_d_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_no VARCHAR(50),
    ncr_id BIGINT,
    supplier_id BIGINT,
    d1_team VARCHAR(500),
    d2_problem VARCHAR(500),
    d3_containment VARCHAR(500),
    d4_root_cause VARCHAR(500),
    d5_corrective_action VARCHAR(500),
    d6_validate_action VARCHAR(500),
    d7_prevent_action VARCHAR(500),
    d8_close_summary VARCHAR(500),
    due_date DATE,
    current_step INT DEFAULT 1,
    step_due_date DATE,
    report_status INT DEFAULT 0,
    submit_time DATETIME,
    audit_time DATETIME,
    close_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 质量申诉表
CREATE TABLE IF NOT EXISTS quality_appeal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appeal_no VARCHAR(50),
    ncr_id BIGINT,
    inspection_id BIGINT,
    supplier_id BIGINT,
    appeal_reason VARCHAR(500),
    appeal_status INT DEFAULT 0,
    submit_time DATETIME,
    audit_by BIGINT,
    audit_time DATETIME,
    audit_remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 扣款单表
CREATE TABLE IF NOT EXISTS deduction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deduction_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT,
    source_type VARCHAR(50),
    source_id BIGINT,
    deduction_type INT,
    deduction_amount DECIMAL(18,2) DEFAULT 0,
    deduction_reason VARCHAR(500),
    deduction_status INT DEFAULT 0,
    recon_id BIGINT,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_deduction_no UNIQUE (deduction_no)
);

-- 发票表
CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(50) NOT NULL,
    invoice_code VARCHAR(50),
    invoice_type INT DEFAULT 1,
    recon_id BIGINT,
    supplier_id BIGINT,
    supplier_name VARCHAR(100),
    tax_number VARCHAR(50),
    invoice_amount DECIMAL(18,2) DEFAULT 0,
    tax_amount DECIMAL(18,2) DEFAULT 0,
    tax_rate DECIMAL(5,2) DEFAULT 0,
    invoice_date DATE,
    invoice_status INT DEFAULT 0,
    receive_time DATETIME,
    certify_time DATETIME,
    void_time DATETIME,
    void_reason VARCHAR(500),
    file_id BIGINT,
    ocr_status INT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_invoice_no UNIQUE (invoice_no)
);

-- 付款单表
CREATE TABLE IF NOT EXISTS payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(50) NOT NULL,
    invoice_id BIGINT,
    invoice_no VARCHAR(50),
    recon_id BIGINT,
    supplier_id BIGINT,
    supplier_name VARCHAR(100),
    payment_amount DECIMAL(18,2) DEFAULT 0,
    payment_method INT DEFAULT 1,
    payment_account VARCHAR(50),
    payment_bank VARCHAR(100),
    receive_account VARCHAR(50),
    receive_bank VARCHAR(100),
    schedule_date DATE,
    payment_terms VARCHAR(200),
    payment_time DATETIME,
    payment_status INT DEFAULT 0,
    receipt_no VARCHAR(50),
    approve_status INT DEFAULT 0,
    voucher_no VARCHAR(50),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0,
    CONSTRAINT uk_payment_no UNIQUE (payment_no)
);

-- 收货差异表
CREATE TABLE IF NOT EXISTS receipt_diff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT,
    notice_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    plan_qty DECIMAL(18,4) DEFAULT 0,
    receipt_qty DECIMAL(18,4) DEFAULT 0,
    diff_qty DECIMAL(18,4) DEFAULT 0,
    diff_reason VARCHAR(500),
    handle_method INT,
    handle_remark VARCHAR(500),
    status INT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 送货箱表
CREATE TABLE IF NOT EXISTS delivery_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT,
    package_no VARCHAR(50),
    package_type VARCHAR(50),
    weight DECIMAL(18,2),
    volume DECIMAL(18,2),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 送货箱明细表
CREATE TABLE IF NOT EXISTS delivery_package_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    quantity DECIMAL(18,4) DEFAULT 0,
    unit VARCHAR(20),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 送货条码表
CREATE TABLE IF NOT EXISTS delivery_barcode (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT,
    package_id BIGINT,
    barcode VARCHAR(100),
    barcode_type VARCHAR(50),
    print_count INT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 送货模板表
CREATE TABLE IF NOT EXISTS delivery_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100),
    template_type INT,
    supplier_id BIGINT,
    content VARCHAR(2000),
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- VMI库存表
CREATE TABLE IF NOT EXISTS vmi_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT,
    material_code VARCHAR(50),
    warehouse_id BIGINT,
    warehouse_name VARCHAR(100),
    onhand_qty DECIMAL(18,4) DEFAULT 0,
    available_qty DECIMAL(18,4) DEFAULT 0,
    safety_qty DECIMAL(18,4) DEFAULT 0,
    max_qty DECIMAL(18,4) DEFAULT 0,
    inventory_status INT DEFAULT 0,
    last_sync_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 消息通知表
CREATE TABLE IF NOT EXISTS message_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_no VARCHAR(50),
    receiver_user_id BIGINT,
    receiver_supplier_id BIGINT,
    channel INT DEFAULT 1,
    title VARCHAR(200),
    content VARCHAR(2000),
    business_type VARCHAR(50),
    business_id BIGINT,
    send_status INT DEFAULT 0,
    read_status INT DEFAULT 0,
    send_time DATETIME,
    read_time DATETIME,
    retry_count INT DEFAULT 0,
    error_message VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 消息模板表
CREATE TABLE IF NOT EXISTS message_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(50),
    template_name VARCHAR(100),
    channel INT,
    title_template VARCHAR(200),
    content_template VARCHAR(2000),
    variables VARCHAR(1000),
    business_type VARCHAR(50),
    status INT DEFAULT 1,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 供应商绩效表
CREATE TABLE IF NOT EXISTS supplier_performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT,
    evaluate_period VARCHAR(50),
    ontime_rate DECIMAL(5,2),
    qualified_rate DECIMAL(5,2),
    delivery_score INT,
    total_score INT,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 付款审批表
CREATE TABLE IF NOT EXISTS payment_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT,
    approval_no VARCHAR(50),
    approval_status INT DEFAULT 0,
    approval_remark VARCHAR(500),
    approver_id BIGINT,
    approval_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 对账明细表
CREATE TABLE IF NOT EXISTS reconciliation_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recon_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    quantity DECIMAL(18,4) DEFAULT 0,
    order_amount DECIMAL(18,2) DEFAULT 0,
    confirmed_amount DECIMAL(18,2) DEFAULT 0,
    diff_reason VARCHAR(500),
    confirm_status INT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 三单匹配表
CREATE TABLE IF NOT EXISTS three_way_match (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    delivery_id BIGINT,
    invoice_id BIGINT,
    match_status INT DEFAULT 0,
    match_result VARCHAR(500),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 订单变更表
CREATE TABLE IF NOT EXISTS order_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    order_detail_id BIGINT,
    change_type INT,
    change_content VARCHAR(500),
    before_value VARCHAR(500),
    after_value VARCHAR(500),
    change_reason VARCHAR(500),
    apply_by BIGINT,
    apply_time DATETIME,
    approve_status INT DEFAULT 0,
    approve_by BIGINT,
    approve_time DATETIME,
    approve_remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 交期反馈表
CREATE TABLE IF NOT EXISTS delivery_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    remark VARCHAR(500),
    confirm_status INT DEFAULT 0,
    confirm_by BIGINT,
    confirm_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 交期反馈明细表
CREATE TABLE IF NOT EXISTS delivery_feedback_line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT,
    order_detail_id BIGINT,
    promised_delivery_date DATE,
    planned_quantity DECIMAL(18,4) DEFAULT 0,
    batch_no VARCHAR(50),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 申诉表
CREATE TABLE IF NOT EXISTS appeal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appeal_no VARCHAR(50),
    ncr_id BIGINT,
    inspection_id BIGINT,
    deduction_id BIGINT,
    supplier_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    appeal_reason VARCHAR(500),
    appeal_desc VARCHAR(1000),
    adjust_amount DECIMAL(18,2),
    appeal_status INT DEFAULT 0,
    submit_time DATETIME,
    reviewer BIGINT,
    reviewer_name VARCHAR(50),
    review_time DATETIME,
    review_opinion VARCHAR(500),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 检验标准表
CREATE TABLE IF NOT EXISTS inspection_standard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    standard_code VARCHAR(50),
    standard_name VARCHAR(100),
    material_code VARCHAR(50),
    standard_type INT,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 检验标准项表
CREATE TABLE IF NOT EXISTS inspection_standard_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    standard_id BIGINT,
    item_name VARCHAR(100),
    item_code VARCHAR(50),
    test_method VARCHAR(200),
    standard_value VARCHAR(200),
    unit VARCHAR(20),
    sort INT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 集成端点表
CREATE TABLE IF NOT EXISTS integration_endpoint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    endpoint_name VARCHAR(100),
    endpoint_code VARCHAR(50),
    system_type INT,
    integration_mode INT,
    url VARCHAR(500),
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 集成日志表
CREATE TABLE IF NOT EXISTS integration_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    endpoint_id BIGINT,
    direction INT,
    request_data VARCHAR(4000),
    response_data VARCHAR(4000),
    status INT,
    error_message VARCHAR(500),
    duration BIGINT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 集成同步任务表
CREATE TABLE IF NOT EXISTS integration_sync_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type VARCHAR(50),
    task_status INT DEFAULT 0,
    last_sync_time DATETIME,
    next_sync_time DATETIME,
    sync_config VARCHAR(2000),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 预测需求表
CREATE TABLE IF NOT EXISTS forecast_demand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    demand_date DATE,
    demand_qty DECIMAL(18,4) DEFAULT 0,
    demand_type INT,
    status INT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);

-- 送货计划表
CREATE TABLE IF NOT EXISTS delivery_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT,
    plan_date DATE,
    plan_qty DECIMAL(18,4) DEFAULT 0,
    actual_qty DECIMAL(18,4) DEFAULT 0,
    status INT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    version INT DEFAULT 0
);