-- =====================================================
-- 供应商协同系统数据库建表脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS supplier_collaboration 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

USE supplier_collaboration;

-- =====================================================
-- 系统管理模块
-- =====================================================

-- -----------------------------------------------------
-- 表 sys_user: 用户表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    username        VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    password        VARCHAR(100)    NOT NULL                 COMMENT '密码(加密存储)',
    real_name       VARCHAR(50)     DEFAULT NULL             COMMENT '真实姓名',
    phone           VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    email           VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    avatar          VARCHAR(255)    DEFAULT NULL             COMMENT '头像URL',
    user_type       TINYINT         DEFAULT 1                COMMENT '用户类型(1-系统用户,2-供应商用户)',
    supplier_id     BIGINT          DEFAULT NULL             COMMENT '供应商ID(供应商用户关联)',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-禁用,1-启用)',
    last_login_time DATETIME        DEFAULT NULL             COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50)     DEFAULT NULL             COMMENT '最后登录IP',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_status (status),
    KEY idx_supplier_id (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------
-- 表 sys_role: 角色表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    role_name       VARCHAR(50)     NOT NULL                 COMMENT '角色名称',
    role_code       VARCHAR(50)     NOT NULL                 COMMENT '角色编码',
    description     VARCHAR(255)    DEFAULT NULL             COMMENT '角色描述',
    sort            INT             DEFAULT 0                COMMENT '排序号',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-禁用,1-启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- -----------------------------------------------------
-- 表 sys_permission: 权限表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    perm_name       VARCHAR(50)     NOT NULL                 COMMENT '权限名称',
    perm_code       VARCHAR(100)    NOT NULL                 COMMENT '权限编码',
    perm_type       TINYINT         NOT NULL                 COMMENT '权限类型(1-菜单,2-按钮,3-接口)',
    parent_id       BIGINT          DEFAULT 0                COMMENT '父级ID',
    path            VARCHAR(255)    DEFAULT NULL             COMMENT '菜单路径',
    component       VARCHAR(255)    DEFAULT NULL             COMMENT '组件路径',
    icon            VARCHAR(100)    DEFAULT NULL             COMMENT '图标',
    sort            INT             DEFAULT 0                COMMENT '排序号',
    visible         TINYINT         DEFAULT 1                COMMENT '是否可见(0-隐藏,1-显示)',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-禁用,1-启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- -----------------------------------------------------
-- 表 sys_user_role: 用户角色关联表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    user_id         BIGINT          NOT NULL                 COMMENT '用户ID',
    role_id         BIGINT          NOT NULL                 COMMENT '角色ID',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- -----------------------------------------------------
-- 表 sys_role_permission: 角色权限关联表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    role_id         BIGINT          NOT NULL                 COMMENT '角色ID',
    perm_id         BIGINT          NOT NULL                 COMMENT '权限ID',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id),
    KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- -----------------------------------------------------
-- 表 sys_dict: 数据字典表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    dict_name       VARCHAR(50)     NOT NULL                 COMMENT '字典名称',
    dict_code       VARCHAR(50)     NOT NULL                 COMMENT '字典编码',
    description     VARCHAR(255)    DEFAULT NULL             COMMENT '字典描述',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-禁用,1-启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- -----------------------------------------------------
-- 表 sys_dict_item: 字典项表
-- -----------------------------------------------------
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    dict_id         BIGINT          NOT NULL                 COMMENT '字典ID',
    item_label      VARCHAR(50)     NOT NULL                 COMMENT '字典项标签',
    item_value      VARCHAR(50)     NOT NULL                 COMMENT '字典项值',
    sort            INT             DEFAULT 0                COMMENT '排序号',
    description     VARCHAR(255)    DEFAULT NULL             COMMENT '描述',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-禁用,1-启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

-- =====================================================
-- 供应商管理模块
-- =====================================================

-- -----------------------------------------------------
-- 表 supplier_category: 供应商分类表
-- -----------------------------------------------------
DROP TABLE IF EXISTS supplier_category;
CREATE TABLE supplier_category (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    category_name   VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
    category_code   VARCHAR(50)     NOT NULL                 COMMENT '分类编码',
    parent_id       BIGINT          DEFAULT 0                COMMENT '父级ID',
    sort            INT             DEFAULT 0                COMMENT '排序号',
    description     VARCHAR(255)    DEFAULT NULL             COMMENT '分类描述',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-禁用,1-启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商分类表';

-- -----------------------------------------------------
-- 表 supplier_info: 供应商基本信息表
-- -----------------------------------------------------
DROP TABLE IF EXISTS supplier_info;
CREATE TABLE supplier_info (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    supplier_code       VARCHAR(50)     NOT NULL                 COMMENT '供应商编码',
    supplier_name       VARCHAR(100)    NOT NULL                 COMMENT '供应商名称',
    supplier_short_name VARCHAR(50)     DEFAULT NULL             COMMENT '供应商简称',
    category_id         BIGINT          DEFAULT NULL             COMMENT '分类ID',
    supplier_type       TINYINT         DEFAULT 1                COMMENT '供应商类型(1-原材料,2-辅材,3-设备,4-服务,5-其他)',
    credit_code         VARCHAR(50)     DEFAULT NULL             COMMENT '统一社会信用代码',
    legal_person        VARCHAR(50)     DEFAULT NULL             COMMENT '法人代表',
    registered_capital  DECIMAL(18,2)   DEFAULT NULL             COMMENT '注册资本(万元)',
    establish_date      DATE            DEFAULT NULL             COMMENT '成立日期',
    business_scope      TEXT            DEFAULT NULL             COMMENT '经营范围',
    province            VARCHAR(50)     DEFAULT NULL             COMMENT '省份',
    city                VARCHAR(50)     DEFAULT NULL             COMMENT '城市',
    district            VARCHAR(50)     DEFAULT NULL             COMMENT '区县',
    address             VARCHAR(255)    DEFAULT NULL             COMMENT '详细地址',
    contact_name        VARCHAR(50)     DEFAULT NULL             COMMENT '联系人',
    contact_phone       VARCHAR(20)     DEFAULT NULL             COMMENT '联系电话',
    contact_email       VARCHAR(100)    DEFAULT NULL             COMMENT '联系邮箱',
    bank_name           VARCHAR(100)    DEFAULT NULL             COMMENT '开户银行',
    bank_account        VARCHAR(50)     DEFAULT NULL             COMMENT '银行账号',
    tax_number          VARCHAR(50)     DEFAULT NULL             COMMENT '税号',
    invoice_address     VARCHAR(255)    DEFAULT NULL             COMMENT '开票地址',
    invoice_phone       VARCHAR(20)     DEFAULT NULL             COMMENT '开票电话',
    rating              TINYINT         DEFAULT 0                COMMENT '供应商评级(1-A级,2-B级,3-C级,4-D级)',
    status              TINYINT         DEFAULT 0                COMMENT '状态(0-待审核,1-合作中,2-暂停合作,3-黑名单)',
    audit_time          DATETIME        DEFAULT NULL             COMMENT '审核时间',
    audit_by            BIGINT          DEFAULT NULL             COMMENT '审核人',
    audit_remark        VARCHAR(500)    DEFAULT NULL             COMMENT '审核备注',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_code (supplier_code),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_credit_code (credit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商基本信息表';

-- -----------------------------------------------------
-- 表 supplier_qualification: 供应商资质表
-- -----------------------------------------------------
DROP TABLE IF EXISTS supplier_qualification;
CREATE TABLE supplier_qualification (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    supplier_id     BIGINT          NOT NULL                 COMMENT '供应商ID',
    qual_type       VARCHAR(50)     NOT NULL                 COMMENT '资质类型',
    qual_name       VARCHAR(100)    NOT NULL                 COMMENT '资质名称',
    qual_no         VARCHAR(100)    DEFAULT NULL             COMMENT '资质编号',
    qual_org        VARCHAR(100)    DEFAULT NULL             COMMENT '发证机构',
    valid_start     DATE            DEFAULT NULL             COMMENT '有效期开始',
    valid_end       DATE            DEFAULT NULL             COMMENT '有效期结束',
    qual_file       VARCHAR(255)    DEFAULT NULL             COMMENT '资质文件URL',
    status          TINYINT         DEFAULT 1                COMMENT '状态(0-无效,1-有效,2-即将过期)',
    remind_days     INT             DEFAULT 30               COMMENT '提前提醒天数',
    remark          VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted         TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version         INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_supplier_id (supplier_id),
    KEY idx_qual_type (qual_type),
    KEY idx_valid_end (valid_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质表';

-- -----------------------------------------------------
-- 表 supplier_performance: 供应商绩效表
-- -----------------------------------------------------
DROP TABLE IF EXISTS supplier_performance;
CREATE TABLE supplier_performance (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    supplier_id         BIGINT          NOT NULL                 COMMENT '供应商ID',
    evaluate_period     VARCHAR(20)     NOT NULL                 COMMENT '评估周期(格式:YYYY-MM)',
    quality_score       DECIMAL(5,2)    DEFAULT 0.00             COMMENT '质量评分(满分100)',
    delivery_score      DECIMAL(5,2)    DEFAULT 0.00             COMMENT '交付评分(满分100)',
    service_score       DECIMAL(5,2)    DEFAULT 0.00             COMMENT '服务评分(满分100)',
    price_score         DECIMAL(5,2)    DEFAULT 0.00             COMMENT '价格评分(满分100)',
    total_score         DECIMAL(5,2)    DEFAULT 0.00             COMMENT '综合评分(满分100)',
    quality_weight      DECIMAL(5,2)    DEFAULT 30.00            COMMENT '质量权重(%)',
    delivery_weight     DECIMAL(5,2)    DEFAULT 30.00            COMMENT '交付权重(%)',
    service_weight      DECIMAL(5,2)    DEFAULT 20.00            COMMENT '服务权重(%)',
    price_weight        DECIMAL(5,2)    DEFAULT 20.00            COMMENT '价格权重(%)',
    delivery_count      INT             DEFAULT 0                COMMENT '送货次数',
    ontime_count        INT             DEFAULT 0                COMMENT '准时送货次数',
    quality_count       INT             DEFAULT 0                COMMENT '质量合格次数',
    reject_count        INT             DEFAULT 0                COMMENT '退货次数',
    evaluate_by         BIGINT          DEFAULT NULL             COMMENT '评估人',
    evaluate_time       DATETIME        DEFAULT NULL             COMMENT '评估时间',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_supplier_id (supplier_id),
    KEY idx_evaluate_period (evaluate_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商绩效表';

-- =====================================================
-- 订单协同模块
-- =====================================================

-- -----------------------------------------------------
-- 表 purchase_order: 采购订单主表
-- -----------------------------------------------------
DROP TABLE IF EXISTS purchase_order;
CREATE TABLE purchase_order (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    order_no            VARCHAR(50)     NOT NULL                 COMMENT '订单编号',
    supplier_id         BIGINT          NOT NULL                 COMMENT '供应商ID',
    order_date          DATE            NOT NULL                 COMMENT '订单日期',
    delivery_date       DATE            DEFAULT NULL             COMMENT '要求交货日期',
    currency            VARCHAR(10)     DEFAULT 'CNY'            COMMENT '币种',
    total_amount        DECIMAL(18,2)   DEFAULT 0.00             COMMENT '订单总金额',
    tax_amount          DECIMAL(18,2)   DEFAULT 0.00             COMMENT '税额',
    discount_amount     DECIMAL(18,2)   DEFAULT 0.00             COMMENT '优惠金额',
    pay_amount          DECIMAL(18,2)   DEFAULT 0.00             COMMENT '应付金额',
    order_status        TINYINT         DEFAULT 0                COMMENT '订单状态(0-草稿,1-待确认,2-已确认,3-生产中,4-已发货,5-已收货,6-已完成,7-已取消)',
    confirm_time        DATETIME        DEFAULT NULL             COMMENT '确认时间',
    confirm_by          BIGINT          DEFAULT NULL             COMMENT '确认人',
    complete_time       DATETIME        DEFAULT NULL             COMMENT '完成时间',
    cancel_time         DATETIME        DEFAULT NULL             COMMENT '取消时间',
    cancel_reason       VARCHAR(500)    DEFAULT NULL             COMMENT '取消原因',
    buyer_id            BIGINT          DEFAULT NULL             COMMENT '采购员ID',
    buyer_name          VARCHAR(50)     DEFAULT NULL             COMMENT '采购员姓名',
    dept_id             BIGINT          DEFAULT NULL             COMMENT '部门ID',
    dept_name           VARCHAR(100)    DEFAULT NULL             COMMENT '部门名称',
    contract_no         VARCHAR(50)     DEFAULT NULL             COMMENT '合同编号',
    payment_terms       VARCHAR(255)    DEFAULT NULL             COMMENT '付款条款',
    delivery_address    VARCHAR(255)    DEFAULT NULL             COMMENT '送货地址',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_supplier_id (supplier_id),
    KEY idx_order_status (order_status),
    KEY idx_order_date (order_date),
    KEY idx_delivery_date (delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

-- -----------------------------------------------------
-- 表 purchase_order_detail: 采购订单明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS purchase_order_detail;
CREATE TABLE purchase_order_detail (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    order_id            BIGINT          NOT NULL                 COMMENT '订单ID',
    line_no             INT             DEFAULT 0                COMMENT '行号',
    material_code       VARCHAR(50)     NOT NULL                 COMMENT '物料编码',
    material_name       VARCHAR(100)    NOT NULL                 COMMENT '物料名称',
    material_spec       VARCHAR(100)    DEFAULT NULL             COMMENT '物料规格',
    material_model      VARCHAR(100)    DEFAULT NULL             COMMENT '物料型号',
    unit                VARCHAR(20)     DEFAULT NULL             COMMENT '单位',
    quantity            DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '数量',
    unit_price          DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '单价',
    tax_rate            DECIMAL(5,2)    DEFAULT 0.00             COMMENT '税率(%)',
    tax_amount          DECIMAL(18,2)   DEFAULT 0.00             COMMENT '税额',
    amount              DECIMAL(18,2)   DEFAULT 0.00             COMMENT '金额',
    delivered_qty       DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '已送货数量',
    received_qty        DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '已收货数量',
    qualified_qty       DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '合格数量',
    delivery_date       DATE            DEFAULT NULL             COMMENT '要求交货日期',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_material_code (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- -----------------------------------------------------
-- 表 order_change: 订单变更记录表
-- -----------------------------------------------------
DROP TABLE IF EXISTS order_change;
CREATE TABLE order_change (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    order_id            BIGINT          NOT NULL                 COMMENT '订单ID',
    order_detail_id     BIGINT          DEFAULT NULL             COMMENT '订单明细ID',
    change_type         TINYINT         NOT NULL                 COMMENT '变更类型(1-数量变更,2-价格变更,3-交期变更,4-取消,5-其他)',
    change_content      VARCHAR(500)    DEFAULT NULL             COMMENT '变更内容',
    before_value        VARCHAR(255)    DEFAULT NULL             COMMENT '变更前值',
    after_value         VARCHAR(255)    DEFAULT NULL             COMMENT '变更后值',
    change_reason       VARCHAR(500)    DEFAULT NULL             COMMENT '变更原因',
    apply_by            BIGINT          DEFAULT NULL             COMMENT '申请人',
    apply_time          DATETIME        DEFAULT NULL             COMMENT '申请时间',
    approve_by          BIGINT          DEFAULT NULL             COMMENT '审批人',
    approve_time        DATETIME        DEFAULT NULL             COMMENT '审批时间',
    approve_status      TINYINT         DEFAULT 0                COMMENT '审批状态(0-待审批,1-已通过,2-已拒绝)',
    approve_remark      VARCHAR(500)    DEFAULT NULL             COMMENT '审批备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_change_type (change_type),
    KEY idx_approve_status (approve_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单变更记录表';

-- -----------------------------------------------------
-- 表 order_track: 订单跟踪记录表
-- -----------------------------------------------------
DROP TABLE IF EXISTS order_track;
CREATE TABLE order_track (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    order_id            BIGINT          NOT NULL                 COMMENT '订单ID',
    track_status        TINYINT         NOT NULL                 COMMENT '跟踪状态(1-已创建,2-已发送,3-已确认,4-生产中,5-已发货,6-运输中,7-已送达,8-已收货,9-已完成,10-已取消)',
    track_time          DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '跟踪时间',
    track_remark        VARCHAR(500)    DEFAULT NULL             COMMENT '跟踪说明',
    operator            BIGINT          DEFAULT NULL             COMMENT '操作人',
    operator_name       VARCHAR(50)     DEFAULT NULL             COMMENT '操作人姓名',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_track_status (track_status),
    KEY idx_track_time (track_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单跟踪记录表';

-- =====================================================
-- 送货收货模块
-- =====================================================

-- -----------------------------------------------------
-- 表 delivery_notice: 送货通知单表
-- -----------------------------------------------------
DROP TABLE IF EXISTS delivery_notice;
CREATE TABLE delivery_notice (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    notice_no           VARCHAR(50)     NOT NULL                 COMMENT '通知单号',
    order_id            BIGINT          NOT NULL                 COMMENT '订单ID',
    order_no            VARCHAR(50)     DEFAULT NULL             COMMENT '订单编号',
    supplier_id         BIGINT          NOT NULL                 COMMENT '供应商ID',
    supplier_name       VARCHAR(100)    DEFAULT NULL             COMMENT '供应商名称',
    plan_delivery_date  DATE            DEFAULT NULL             COMMENT '计划送货日期',
    actual_delivery_date DATE           DEFAULT NULL             COMMENT '实际送货日期',
    delivery_status     TINYINT         DEFAULT 0                COMMENT '送货状态(0-待发货,1-已发货,2-运输中,3-已送达,4-已收货,5-已拒收)',
    delivery_method     VARCHAR(50)     DEFAULT NULL             COMMENT '送货方式',
    delivery_company    VARCHAR(100)    DEFAULT NULL             COMMENT '物流公司',
    delivery_no         VARCHAR(50)     DEFAULT NULL             COMMENT '物流单号',
    driver_name         VARCHAR(50)     DEFAULT NULL             COMMENT '司机姓名',
    driver_phone        VARCHAR(20)     DEFAULT NULL             COMMENT '司机电话',
    vehicle_no          VARCHAR(20)     DEFAULT NULL             COMMENT '车牌号',
    delivery_address    VARCHAR(255)    DEFAULT NULL             COMMENT '送货地址',
    receiver            VARCHAR(50)     DEFAULT NULL             COMMENT '收货人',
    receiver_phone      VARCHAR(20)     DEFAULT NULL             COMMENT '收货人电话',
    send_time           DATETIME        DEFAULT NULL             COMMENT '发货时间',
    arrive_time         DATETIME        DEFAULT NULL             COMMENT '到达时间',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    total_amount        DECIMAL(18,2)   DEFAULT NULL             COMMENT '发货总金额',
    currency            VARCHAR(10)     DEFAULT 'CNY'            COMMENT '币种',
    tax_amount          DECIMAL(18,2)   DEFAULT NULL             COMMENT '税额',
    net_amount          DECIMAL(18,2)   DEFAULT NULL             COMMENT '净额',
    payment_status      TINYINT         DEFAULT 0                COMMENT '付款状态(0-未付款,1-部分付款,2-已付款)',
    reconciliation_status TINYINT       DEFAULT 0                COMMENT '对账状态(0-待对账,1-对账中,2-已对账,3-有差异)',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notice_no (notice_no),
    KEY idx_order_id (order_id),
    KEY idx_supplier_id (supplier_id),
    KEY idx_delivery_status (delivery_status),
    KEY idx_plan_delivery_date (plan_delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='送货通知单表';

-- -----------------------------------------------------
-- 表 delivery_detail: 送货明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS delivery_detail;
CREATE TABLE delivery_detail (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    notice_id           BIGINT          NOT NULL                 COMMENT '通知单ID',
    order_detail_id     BIGINT          DEFAULT NULL             COMMENT '订单明细ID',
    material_code       VARCHAR(50)     NOT NULL                 COMMENT '物料编码',
    material_name       VARCHAR(100)    NOT NULL                 COMMENT '物料名称',
    material_spec       VARCHAR(100)    DEFAULT NULL             COMMENT '物料规格',
    unit                VARCHAR(20)     DEFAULT NULL             COMMENT '单位',
    plan_qty            DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '计划送货数量',
    actual_qty          DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '实际送货数量',
    received_qty        DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '已收货数量',
    qualified_qty       DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '合格数量',
    batch_no            VARCHAR(50)     DEFAULT NULL             COMMENT '批次号',
    production_date     DATE            DEFAULT NULL             COMMENT '生产日期',
    expiry_date         DATE            DEFAULT NULL             COMMENT '过期日期/有效期至',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_notice_id (notice_id),
    KEY idx_order_detail_id (order_detail_id),
    KEY idx_material_code (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='送货明细表';

-- -----------------------------------------------------
-- 表 receipt_record: 收货记录表
-- -----------------------------------------------------
DROP TABLE IF EXISTS receipt_record;
CREATE TABLE receipt_record (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    delivery_id         BIGINT          NOT NULL                 COMMENT '送货明细ID',
    notice_id           BIGINT          DEFAULT NULL             COMMENT '通知单ID',
    material_code       VARCHAR(50)     NOT NULL                 COMMENT '物料编码',
    material_name       VARCHAR(100)    NOT NULL                 COMMENT '物料名称',
    plan_qty            DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '计划收货数量',
    receipt_qty         DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '实际收货数量',
    reject_qty          DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '拒收数量',
    receipt_time        DATETIME        DEFAULT NULL             COMMENT '收货时间',
    receiver            BIGINT          DEFAULT NULL             COMMENT '收货人',
    receiver_name       VARCHAR(50)     DEFAULT NULL             COMMENT '收货人姓名',
    warehouse_id        BIGINT          DEFAULT NULL             COMMENT '仓库ID',
    warehouse_name      VARCHAR(100)    DEFAULT NULL             COMMENT '仓库名称',
    location            VARCHAR(100)    DEFAULT NULL             COMMENT '库位',
    receipt_status      TINYINT         DEFAULT 0                COMMENT '收货状态(0-待收货,1-已收货,2-已拒收)',
    reject_reason       VARCHAR(500)    DEFAULT NULL             COMMENT '拒收原因',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_delivery_id (delivery_id),
    KEY idx_notice_id (notice_id),
    KEY idx_receipt_time (receipt_time),
    KEY idx_receipt_status (receipt_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货记录表';

-- -----------------------------------------------------
-- 表 quality_inspection: 质量检验表
-- -----------------------------------------------------
DROP TABLE IF EXISTS quality_inspection;
CREATE TABLE quality_inspection (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    receipt_id          BIGINT          NOT NULL                 COMMENT '收货记录ID',
    delivery_id         BIGINT          DEFAULT NULL             COMMENT '送货明细ID',
    material_code       VARCHAR(50)     NOT NULL                 COMMENT '物料编码',
    material_name       VARCHAR(100)    NOT NULL                 COMMENT '物料名称',
    inspect_qty         DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '检验数量',
    qualified_qty       DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '合格数量',
    unqualified_qty     DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '不合格数量',
    inspect_result      TINYINT         DEFAULT 0                COMMENT '检验结果(0-待检验,1-合格,2-不合格,3-部分合格)',
    inspect_type        TINYINT         DEFAULT 1                COMMENT '检验类型(1-来料检验,2-抽检,3-全检)',
    inspect_time        DATETIME        DEFAULT NULL             COMMENT '检验时间',
    inspector           BIGINT          DEFAULT NULL             COMMENT '检验员',
    inspector_name      VARCHAR(50)     DEFAULT NULL             COMMENT '检验员姓名',
    inspect_remark      VARCHAR(500)    DEFAULT NULL             COMMENT '检验说明',
    handle_method       TINYINT         DEFAULT NULL             COMMENT '处理方式(1-退货,2-换货,3-特采,4-报废)',
    handle_remark       VARCHAR(500)    DEFAULT NULL             COMMENT '处理说明',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_receipt_id (receipt_id),
    KEY idx_delivery_id (delivery_id),
    KEY idx_inspect_result (inspect_result),
    KEY idx_inspect_time (inspect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检验表';

-- =====================================================
-- 财务对账模块
-- =====================================================

-- -----------------------------------------------------
-- 表 reconciliation: 对账单表
-- -----------------------------------------------------
DROP TABLE IF EXISTS reconciliation;
CREATE TABLE reconciliation (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    recon_no            VARCHAR(50)     NOT NULL                 COMMENT '对账单号',
    supplier_id         BIGINT          NOT NULL                 COMMENT '供应商ID',
    supplier_name       VARCHAR(100)    DEFAULT NULL             COMMENT '供应商名称',
    recon_period        VARCHAR(20)     NOT NULL                 COMMENT '对账周期(格式:YYYY-MM)',
    start_date          DATE            DEFAULT NULL             COMMENT '开始日期',
    end_date            DATE            DEFAULT NULL             COMMENT '结束日期',
    total_amount        DECIMAL(18,2)   DEFAULT 0.00             COMMENT '对账总金额',
    confirmed_amount    DECIMAL(18,2)   DEFAULT 0.00             COMMENT '确认金额',
    diff_amount         DECIMAL(18,2)   DEFAULT 0.00             COMMENT '差异金额',
    recon_status        TINYINT         DEFAULT 0                COMMENT '对账状态(0-待对账,1-对账中,2-已确认,3-有异议,4-已完成)',
    send_time           DATETIME        DEFAULT NULL             COMMENT '发送时间',
    confirm_time        DATETIME        DEFAULT NULL             COMMENT '确认时间',
    confirm_by          BIGINT          DEFAULT NULL             COMMENT '确认人',
    confirm_remark      VARCHAR(500)    DEFAULT NULL             COMMENT '确认备注',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recon_no (recon_no),
    KEY idx_supplier_id (supplier_id),
    KEY idx_recon_status (recon_status),
    KEY idx_recon_period (recon_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账单表';

-- -----------------------------------------------------
-- 表 reconciliation_detail: 对账明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS reconciliation_detail;
CREATE TABLE reconciliation_detail (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    recon_id            BIGINT          NOT NULL                 COMMENT '对账单ID',
    order_id            BIGINT          DEFAULT NULL             COMMENT '订单ID',
    order_no            VARCHAR(50)     DEFAULT NULL             COMMENT '订单编号',
    delivery_id         BIGINT          DEFAULT NULL             COMMENT '送货单ID',
    delivery_no         VARCHAR(50)     DEFAULT NULL             COMMENT '送货单号',
    material_code       VARCHAR(50)     DEFAULT NULL             COMMENT '物料编码',
    material_name       VARCHAR(100)    DEFAULT NULL             COMMENT '物料名称',
    quantity            DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '数量',
    unit_price          DECIMAL(18,4)   DEFAULT 0.0000           COMMENT '单价',
    order_amount        DECIMAL(18,2)   DEFAULT 0.00             COMMENT '订单金额',
    confirmed_amount    DECIMAL(18,2)   DEFAULT 0.00             COMMENT '确认金额',
    diff_amount         DECIMAL(18,2)   DEFAULT 0.00             COMMENT '差异金额',
    diff_reason         VARCHAR(500)    DEFAULT NULL             COMMENT '差异原因',
    confirm_status      TINYINT         DEFAULT 0                COMMENT '确认状态(0-待确认,1-已确认,2-有异议)',
    confirm_time        DATETIME        DEFAULT NULL             COMMENT '确认时间',
    confirm_remark      VARCHAR(500)    DEFAULT NULL             COMMENT '确认备注',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_recon_id (recon_id),
    KEY idx_order_id (order_id),
    KEY idx_delivery_id (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账明细表';

-- -----------------------------------------------------
-- 表 invoice: 发票表
-- -----------------------------------------------------
DROP TABLE IF EXISTS invoice;
CREATE TABLE invoice (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    invoice_no          VARCHAR(50)     NOT NULL                 COMMENT '发票号码',
    invoice_type        TINYINT         DEFAULT 1                COMMENT '发票类型(1-增值税专用发票,2-增值税普通发票,3-电子发票)',
    recon_id            BIGINT          DEFAULT NULL             COMMENT '对账单ID',
    supplier_id         BIGINT          NOT NULL                 COMMENT '供应商ID',
    supplier_name       VARCHAR(100)    DEFAULT NULL             COMMENT '供应商名称',
    invoice_amount      DECIMAL(18,2)   DEFAULT 0.00             COMMENT '发票金额',
    tax_amount          DECIMAL(18,2)   DEFAULT 0.00             COMMENT '税额',
    tax_rate            DECIMAL(5,2)    DEFAULT 0.00             COMMENT '税率(%)',
    invoice_date        DATE            DEFAULT NULL             COMMENT '开票日期',
    invoice_status      TINYINT         DEFAULT 0                COMMENT '发票状态(0-待开票,1-已开票,2-已收到,3-已认证,4-已作废)',
    receive_time        DATETIME        DEFAULT NULL             COMMENT '收到时间',
    receive_by          BIGINT          DEFAULT NULL             COMMENT '收票人',
    certify_time        DATETIME        DEFAULT NULL             COMMENT '认证时间',
    certify_by          BIGINT          DEFAULT NULL             COMMENT '认证人',
    void_time           DATETIME        DEFAULT NULL             COMMENT '作废时间',
    void_reason         VARCHAR(500)    DEFAULT NULL             COMMENT '作废原因',
    invoice_file        VARCHAR(255)    DEFAULT NULL             COMMENT '发票文件URL',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_no (invoice_no),
    KEY idx_recon_id (recon_id),
    KEY idx_supplier_id (supplier_id),
    KEY idx_invoice_status (invoice_status),
    KEY idx_invoice_date (invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- -----------------------------------------------------
-- 表 payment: 付款记录表
-- -----------------------------------------------------
DROP TABLE IF EXISTS payment;
CREATE TABLE payment (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    payment_no          VARCHAR(50)     NOT NULL                 COMMENT '付款单号',
    invoice_id          BIGINT          NOT NULL                 COMMENT '发票ID',
    invoice_no          VARCHAR(50)     DEFAULT NULL             COMMENT '发票号码',
    supplier_id         BIGINT          DEFAULT NULL             COMMENT '供应商ID',
    supplier_name       VARCHAR(100)    DEFAULT NULL             COMMENT '供应商名称',
    payment_amount      DECIMAL(18,2)   DEFAULT 0.00             COMMENT '付款金额',
    payment_method      TINYINT         DEFAULT 1                COMMENT '付款方式(1-银行转账,2-承兑汇票,3-现金,4-支票,5-其他)',
    payment_account     VARCHAR(50)     DEFAULT NULL             COMMENT '付款账号',
    payment_bank        VARCHAR(100)    DEFAULT NULL             COMMENT '付款银行',
    receive_account     VARCHAR(50)     DEFAULT NULL             COMMENT '收款账号',
    receive_bank        VARCHAR(100)    DEFAULT NULL             COMMENT '收款银行',
    payment_time        DATETIME        DEFAULT NULL             COMMENT '付款时间',
    payment_status      TINYINT         DEFAULT 0                COMMENT '付款状态(0-待付款,1-部分付款,2-已付款,3-已拒绝)',
    apply_by            BIGINT          DEFAULT NULL             COMMENT '申请人',
    apply_time          DATETIME        DEFAULT NULL             COMMENT '申请时间',
    approve_by          BIGINT          DEFAULT NULL             COMMENT '审批人',
    approve_time        DATETIME        DEFAULT NULL             COMMENT '审批时间',
    approve_status      TINYINT         DEFAULT 0                COMMENT '审批状态(0-待审批,1-已通过,2-已拒绝)',
    approve_remark      VARCHAR(500)    DEFAULT NULL             COMMENT '审批备注',
    voucher_no          VARCHAR(50)     DEFAULT NULL             COMMENT '凭证号',
    remark              VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL             COMMENT '创建人',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by           BIGINT          DEFAULT NULL             COMMENT '更新人',
    deleted             TINYINT         DEFAULT 0                COMMENT '逻辑删除(0-未删除,1-已删除)',
    version             INT             DEFAULT 0                COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_invoice_id (invoice_id),
    KEY idx_supplier_id (supplier_id),
    KEY idx_payment_status (payment_status),
    KEY idx_payment_time (payment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款记录表';

-- =====================================================
-- 外键约束(可选,根据实际情况启用)
-- =====================================================

-- 系统管理模块外键
ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id);
ALTER TABLE sys_role_permission ADD CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role(id);
ALTER TABLE sys_role_permission ADD CONSTRAINT fk_role_perm_perm FOREIGN KEY (perm_id) REFERENCES sys_permission(id);
ALTER TABLE sys_dict_item ADD CONSTRAINT fk_dict_item_dict FOREIGN KEY (dict_id) REFERENCES sys_dict(id);

-- 供应商管理模块外键
ALTER TABLE supplier_info ADD CONSTRAINT fk_supplier_category FOREIGN KEY (category_id) REFERENCES supplier_category(id);
ALTER TABLE supplier_qualification ADD CONSTRAINT fk_qual_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id);
ALTER TABLE supplier_performance ADD CONSTRAINT fk_perf_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id);

-- 订单协同模块外键
ALTER TABLE purchase_order ADD CONSTRAINT fk_order_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id);
ALTER TABLE purchase_order_detail ADD CONSTRAINT fk_detail_order FOREIGN KEY (order_id) REFERENCES purchase_order(id);
ALTER TABLE order_change ADD CONSTRAINT fk_change_order FOREIGN KEY (order_id) REFERENCES purchase_order(id);
ALTER TABLE order_track ADD CONSTRAINT fk_track_order FOREIGN KEY (order_id) REFERENCES purchase_order(id);

-- 送货收货模块外键
ALTER TABLE delivery_notice ADD CONSTRAINT fk_notice_order FOREIGN KEY (order_id) REFERENCES purchase_order(id);
ALTER TABLE delivery_notice ADD CONSTRAINT fk_notice_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id);
ALTER TABLE delivery_detail ADD CONSTRAINT fk_detail_notice FOREIGN KEY (notice_id) REFERENCES delivery_notice(id);
ALTER TABLE receipt_record ADD CONSTRAINT fk_receipt_detail FOREIGN KEY (delivery_id) REFERENCES delivery_detail(id);
ALTER TABLE quality_inspection ADD CONSTRAINT fk_inspect_receipt FOREIGN KEY (receipt_id) REFERENCES receipt_record(id);

-- 财务对账模块外键
ALTER TABLE reconciliation ADD CONSTRAINT fk_recon_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id);
ALTER TABLE reconciliation_detail ADD CONSTRAINT fk_detail_recon FOREIGN KEY (recon_id) REFERENCES reconciliation(id);
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_recon FOREIGN KEY (recon_id) REFERENCES reconciliation(id);
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_supplier FOREIGN KEY (supplier_id) REFERENCES supplier_info(id);
ALTER TABLE payment ADD CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id);
