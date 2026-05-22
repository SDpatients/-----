# 供应商协同系统数据库设计文档

## 1. 概述

### 1.1 文档说明
本文档描述供应商协同系统的数据库设计,包括ER图说明、表结构设计、索引设计等内容。

### 1.2 数据库信息
- 数据库类型: MySQL 8.0+
- 字符集: utf8mb4
- 排序规则: utf8mb4_general_ci
- 存储引擎: InnoDB

### 1.3 设计原则
- 所有表使用InnoDB引擎,支持事务和外键
- 主键使用BIGINT类型,采用雪花算法或自增
- 状态字段使用TINYINT类型
- 金额字段使用DECIMAL(18,2)类型
- 时间字段使用DATETIME类型
- 所有表包含公共字段:创建时间、更新时间、创建人、更新人、逻辑删除标志、乐观锁版本号

---

## 2. ER图说明

### 2.1 系统管理模块ER图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  sys_user   │       │  sys_role   │       │sys_permission│
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ username    │       │ role_name   │       │ perm_name   │
│ password    │       │ role_code   │       │ perm_code   │
│ real_name   │       │ status      │       │ perm_type   │
│ phone       │       │ ...         │       │ parent_id   │
│ email       │       └──────┬──────┘       │ path        │
│ status      │              │              │ ...         │
│ ...         │              │              └──────┬──────┘
└──────┬──────┘              │                     │
       │                     │                     │
       │ M:N                 │ M:N                 │
       ▼                     ▼                     │
┌─────────────────┐   ┌─────────────────┐         │
│ sys_user_role   │   │sys_role_perm    │◄────────┘
├─────────────────┤   ├─────────────────┤
│ user_id (FK)    │   │ role_id (FK)    │
│ role_id (FK)    │   │ perm_id (FK)    │
└─────────────────┘   └─────────────────┘

┌─────────────┐       ┌─────────────────┐
│  sys_dict   │       │ sys_dict_item   │
├─────────────┤       ├─────────────────┤
│ id (PK)     │◄──────│ id (PK)         │
│ dict_name   │       │ dict_id (FK)    │
│ dict_code   │       │ item_label      │
│ status      │       │ item_value      │
│ ...         │       │ sort            │
└─────────────┘       │ ...             │
                      └─────────────────┘
```

### 2.2 供应商管理模块ER图

```
┌───────────────────┐
│ supplier_category │
├───────────────────┤
│ id (PK)           │
│ category_name     │
│ category_code     │
│ parent_id         │
│ ...               │
└────────┬──────────┘
         │
         │ 1:N
         ▼
┌───────────────────┐       ┌───────────────────────┐
│  supplier_info    │       │supplier_qualification │
├───────────────────┤       ├───────────────────────┤
│ id (PK)           │◄──────│ id (PK)               │
│ supplier_code     │       │ supplier_id (FK)      │
│ supplier_name     │       │ qual_type             │
│ category_id (FK)  │       │ qual_name             │
│ supplier_type     │       │ qual_no               │
│ contact_name      │       │ valid_start           │
│ contact_phone     │       │ valid_end             │
│ status            │       │ status                │
│ ...               │       │ ...                   │
└────────┬──────────┘       └───────────────────────┘
         │
         │ 1:N
         ▼
┌───────────────────────┐
│ supplier_performance  │
├───────────────────────┤
│ id (PK)               │
│ supplier_id (FK)      │
│ evaluate_period       │
│ quality_score         │
│ delivery_score        │
│ service_score         │
│ total_score           │
│ ...                   │
└───────────────────────┘
```

### 2.3 订单协同模块ER图

```
┌───────────────────┐
│  purchase_order   │
├───────────────────┤
│ id (PK)           │
│ order_no          │
│ supplier_id (FK)  │
│ order_date        │
│ total_amount      │
│ order_status      │
│ ...               │
└────────┬──────────┘
         │
         │ 1:N
         ▼
┌─────────────────────────┐
│ purchase_order_detail   │
├─────────────────────────┤
│ id (PK)                 │
│ order_id (FK)           │
│ material_code           │
│ material_name           │
│ quantity                │
│ unit_price              │
│ amount                  │
│ ...                     │
└─────────────────────────┘

┌───────────────────┐       ┌───────────────────┐
│   order_change    │       │   order_track     │
├───────────────────┤       ├───────────────────┤
│ id (PK)           │       │ id (PK)           │
│ order_id (FK)     │       │ order_id (FK)     │
│ change_type       │       │ track_status      │
│ change_content    │       │ track_time        │
│ before_value      │       │ track_remark      │
│ after_value       │       │ operator          │
│ ...               │       │ ...               │
└───────────────────┘       └───────────────────┘
```

### 2.4 送货收货模块ER图

```
┌───────────────────┐
│  delivery_notice  │
├───────────────────┤
│ id (PK)           │
│ notice_no         │
│ order_id (FK)     │
│ supplier_id (FK)  │
│ plan_delivery_date│
│ delivery_status   │
│ ...               │
└────────┬──────────┘
         │
         │ 1:N
         ▼
┌───────────────────┐       ┌───────────────────┐
│  delivery_detail  │       │  receipt_record   │
├───────────────────┤       ├───────────────────┤
│ id (PK)           │       │ id (PK)           │
│ notice_id (FK)    │◄──────│ delivery_id (FK)  │
│ order_detail_id   │       │ receipt_qty       │
│ material_code     │       │ receipt_time      │
│ plan_qty          │       │ receiver          │
│ actual_qty        │       │ ...               │
│ ...               │       └────────┬──────────┘
└───────────────────┘                │
                                     │ 1:N
                                     ▼
                         ┌───────────────────────┐
                         │ quality_inspection    │
                         ├───────────────────────┤
                         │ id (PK)               │
                         │ receipt_id (FK)       │
                         │ inspect_qty           │
                         │ qualified_qty         │
                         │ unqualified_qty       │
                         │ inspect_result        │
                         │ ...                   │
                         └───────────────────────┘
```

### 2.5 财务对账模块ER图

```
┌───────────────────┐
│  reconciliation   │
├───────────────────┤
│ id (PK)           │
│ recon_no          │
│ supplier_id (FK)  │
│ recon_period      │
│ total_amount      │
│ recon_status      │
│ ...               │
└────────┬──────────┘
         │
         │ 1:N
         ▼
┌─────────────────────────┐
│ reconciliation_detail   │
├─────────────────────────┤
│ id (PK)                 │
│ recon_id (FK)           │
│ order_id (FK)           │
│ order_no                │
│ order_amount            │
│ confirmed_amount        │
│ ...                     │
└─────────────────────────┘

┌───────────────────┐       ┌───────────────────┐
│     invoice       │       │     payment       │
├───────────────────┤       ├───────────────────┤
│ id (PK)           │       │ id (PK)           │
│ invoice_no        │       │ payment_no        │
│ recon_id (FK)     │       │ invoice_id (FK)   │
│ supplier_id (FK)  │       │ payment_amount    │
│ invoice_amount    │       │ payment_method    │
│ invoice_date      │       │ payment_time      │
│ invoice_status    │       │ payment_status    │
│ ...               │       │ ...               │
└───────────────────┘       └───────────────────┘
```

---

## 3. 表关系说明

### 3.1 系统管理模块

| 表名 | 说明 | 关联关系 |
|------|------|----------|
| sys_user | 用户表 | 与sys_role多对多关联 |
| sys_role | 角色表 | 与sys_user、sys_permission多对多关联 |
| sys_permission | 权限表 | 自关联(树形结构),与sys_role多对多关联 |
| sys_user_role | 用户角色关联表 | 中间表,关联sys_user和sys_role |
| sys_role_permission | 角色权限关联表 | 中间表,关联sys_role和sys_permission |
| sys_dict | 数据字典表 | 与sys_dict_item一对多关联 |
| sys_dict_item | 字典项表 | 从属于sys_dict |

### 3.2 供应商管理模块

| 表名 | 说明 | 关联关系 |
|------|------|----------|
| supplier_category | 供应商分类表 | 自关联(树形结构) |
| supplier_info | 供应商基本信息表 | 关联supplier_category |
| supplier_qualification | 供应商资质表 | 从属于supplier_info |
| supplier_performance | 供应商绩效表 | 从属于supplier_info |

### 3.3 订单协同模块

| 表名 | 说明 | 关联关系 |
|------|------|----------|
| purchase_order | 采购订单主表 | 关联supplier_info |
| purchase_order_detail | 采购订单明细表 | 从属于purchase_order |
| order_change | 订单变更记录表 | 从属于purchase_order |
| order_track | 订单跟踪记录表 | 从属于purchase_order |

### 3.4 送货收货模块

| 表名 | 说明 | 关联关系 |
|------|------|----------|
| delivery_notice | 送货通知单表 | 关联purchase_order、supplier_info |
| delivery_detail | 送货明细表 | 从属于delivery_notice |
| receipt_record | 收货记录表 | 关联delivery_detail |
| quality_inspection | 质量检验表 | 从属于receipt_record |

### 3.5 财务对账模块

| 表名 | 说明 | 关联关系 |
|------|------|----------|
| reconciliation | 对账单表 | 关联supplier_info |
| reconciliation_detail | 对账明细表 | 从属于reconciliation,关联purchase_order |
| invoice | 发票表 | 关联reconciliation、supplier_info |
| payment | 付款记录表 | 关联invoice |

---

## 4. 索引设计

### 4.1 索引设计原则
1. 主键自动创建聚簇索引
2. 外键字段创建普通索引
3. 经常作为查询条件的字段创建索引
4. 经常用于排序的字段创建索引
5. 组合查询的字段创建组合索引
6. 遵循最左前缀原则

### 4.2 系统管理模块索引

| 表名 | 索引名 | 索引字段 | 索引类型 | 说明 |
|------|--------|----------|----------|------|
| sys_user | uk_username | username | 唯一索引 | 用户名唯一 |
| sys_user | idx_phone | phone | 普通索引 | 手机号查询 |
| sys_user | idx_status | status | 普通索引 | 状态筛选 |
| sys_role | uk_role_code | role_code | 唯一索引 | 角色编码唯一 |
| sys_permission | idx_parent_id | parent_id | 普通索引 | 父级查询 |
| sys_permission | idx_perm_code | perm_code | 普通索引 | 权限编码查询 |
| sys_user_role | idx_user_id | user_id | 普通索引 | 用户查询 |
| sys_user_role | idx_role_id | role_id | 普通索引 | 角色查询 |
| sys_role_permission | idx_role_id | role_id | 普通索引 | 角色查询 |
| sys_role_permission | idx_perm_id | perm_id | 普通索引 | 权限查询 |
| sys_dict | uk_dict_code | dict_code | 唯一索引 | 字典编码唯一 |
| sys_dict_item | idx_dict_id | dict_id | 普通索引 | 字典查询 |

### 4.3 供应商管理模块索引

| 表名 | 索引名 | 索引字段 | 索引类型 | 说明 |
|------|--------|----------|----------|------|
| supplier_category | uk_category_code | category_code | 唯一索引 | 分类编码唯一 |
| supplier_category | idx_parent_id | parent_id | 普通索引 | 父级查询 |
| supplier_info | uk_supplier_code | supplier_code | 唯一索引 | 供应商编码唯一 |
| supplier_info | idx_category_id | category_id | 普通索引 | 分类查询 |
| supplier_info | idx_status | status | 普通索引 | 状态筛选 |
| supplier_qualification | idx_supplier_id | supplier_id | 普通索引 | 供应商查询 |
| supplier_qualification | idx_qual_type | qual_type | 普通索引 | 资质类型查询 |
| supplier_performance | idx_supplier_id | supplier_id | 普通索引 | 供应商查询 |
| supplier_performance | idx_evaluate_period | evaluate_period | 普通索引 | 评估周期查询 |

### 4.4 订单协同模块索引

| 表名 | 索引名 | 索引字段 | 索引类型 | 说明 |
|------|--------|----------|----------|------|
| purchase_order | uk_order_no | order_no | 唯一索引 | 订单号唯一 |
| purchase_order | idx_supplier_id | supplier_id | 普通索引 | 供应商查询 |
| purchase_order | idx_order_status | order_status | 普通索引 | 订单状态查询 |
| purchase_order | idx_order_date | order_date | 普通索引 | 订单日期查询 |
| purchase_order_detail | idx_order_id | order_id | 普通索引 | 订单查询 |
| order_change | idx_order_id | order_id | 普通索引 | 订单查询 |
| order_change | idx_change_type | change_type | 普通索引 | 变更类型查询 |
| order_track | idx_order_id | order_id | 普通索引 | 订单查询 |
| order_track | idx_track_status | track_status | 普通索引 | 跟踪状态查询 |

### 4.5 送货收货模块索引

| 表名 | 索引名 | 索引字段 | 索引类型 | 说明 |
|------|--------|----------|----------|------|
| delivery_notice | uk_notice_no | notice_no | 唯一索引 | 通知单号唯一 |
| delivery_notice | idx_order_id | order_id | 普通索引 | 订单查询 |
| delivery_notice | idx_supplier_id | supplier_id | 普通索引 | 供应商查询 |
| delivery_notice | idx_delivery_status | delivery_status | 普通索引 | 送货状态查询 |
| delivery_detail | idx_notice_id | notice_id | 普通索引 | 通知单查询 |
| receipt_record | idx_delivery_id | delivery_id | 普通索引 | 送货明细查询 |
| receipt_record | idx_receipt_time | receipt_time | 普通索引 | 收货时间查询 |
| quality_inspection | idx_receipt_id | receipt_id | 普通索引 | 收货记录查询 |
| quality_inspection | idx_inspect_result | inspect_result | 普通索引 | 检验结果查询 |

### 4.6 财务对账模块索引

| 表名 | 索引名 | 索引字段 | 索引类型 | 说明 |
|------|--------|----------|----------|------|
| reconciliation | uk_recon_no | recon_no | 唯一索引 | 对账单号唯一 |
| reconciliation | idx_supplier_id | supplier_id | 普通索引 | 供应商查询 |
| reconciliation | idx_recon_status | recon_status | 普通索引 | 对账状态查询 |
| reconciliation_detail | idx_recon_id | recon_id | 普通索引 | 对账单查询 |
| reconciliation_detail | idx_order_id | order_id | 普通索引 | 订单查询 |
| invoice | uk_invoice_no | invoice_no | 唯一索引 | 发票号唯一 |
| invoice | idx_recon_id | recon_id | 普通索引 | 对账单查询 |
| invoice | idx_supplier_id | supplier_id | 普通索引 | 供应商查询 |
| invoice | idx_invoice_status | invoice_status | 普通索引 | 发票状态查询 |
| payment | uk_payment_no | payment_no | 唯一索引 | 付款单号唯一 |
| payment | idx_invoice_id | invoice_id | 普通索引 | 发票查询 |
| payment | idx_payment_status | payment_status | 普通索引 | 付款状态查询 |

---

## 5. 公共字段说明

所有业务表都包含以下公共字段:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键,雪花算法生成 |
| create_time | DATETIME | 创建时间 |
| create_by | BIGINT | 创建人ID |
| update_time | DATETIME | 更新时间 |
| update_by | BIGINT | 更新人ID |
| deleted | TINYINT | 逻辑删除标志(0-未删除,1-已删除) |
| version | INT | 乐观锁版本号 |

---

## 6. 数据字典说明

### 6.1 订单状态(order_status)
| 值 | 说明 |
|----|------|
| 0 | 草稿 |
| 1 | 待确认 |
| 2 | 已确认 |
| 3 | 生产中 |
| 4 | 已发货 |
| 5 | 已收货 |
| 6 | 已完成 |
| 7 | 已取消 |

### 6.2 供应商类型(supplier_type)
| 值 | 说明 |
|----|------|
| 1 | 原材料供应商 |
| 2 | 辅材供应商 |
| 3 | 设备供应商 |
| 4 | 服务供应商 |
| 5 | 其他 |

### 6.3 供应商状态(supplier_status)
| 值 | 说明 |
|----|------|
| 0 | 待审核 |
| 1 | 合作中 |
| 2 | 暂停合作 |
| 3 | 黑名单 |

### 6.4 付款方式(payment_method)
| 值 | 说明 |
|----|------|
| 1 | 银行转账 |
| 2 | 承兑汇票 |
| 3 | 现金 |
| 4 | 支票 |
| 5 | 其他 |

### 6.5 送货状态(delivery_status)
| 值 | 说明 |
|----|------|
| 0 | 待发货 |
| 1 | 已发货 |
| 2 | 运输中 |
| 3 | 已送达 |
| 4 | 已收货 |
| 5 | 已拒收 |

### 6.6 检验结果(inspect_result)
| 值 | 说明 |
|----|------|
| 0 | 待检验 |
| 1 | 合格 |
| 2 | 不合格 |
| 3 | 部分合格 |

### 6.7 对账状态(recon_status)
| 值 | 说明 |
|----|------|
| 0 | 待对账 |
| 1 | 对账中 |
| 2 | 已确认 |
| 3 | 有异议 |
| 4 | 已完成 |

### 6.8 发票状态(invoice_status)
| 值 | 说明 |
|----|------|
| 0 | 待开票 |
| 1 | 已开票 |
| 2 | 已收到 |
| 3 | 已认证 |
| 4 | 已作废 |

### 6.9 付款状态(payment_status)
| 值 | 说明 |
|----|------|
| 0 | 待付款 |
| 1 | 部分付款 |
| 2 | 已付款 |
| 3 | 已拒绝 |

---

## 7. 版本记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0.0 | 2026-05-21 | 系统 | 初始版本 |
