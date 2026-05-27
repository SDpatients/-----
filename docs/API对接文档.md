# 供应商协同系统 API 对接文档

## 1. 文档说明

本文档面向前端开发人员，基于当前后端已实现 Controller、DTO、VO 与统一响应规范整理。接口用于供应商协同系统的登录认证、供应商准入、采购订单、送货收货、质量检验、财务对账、附件、审计日志与导出任务等模块。

## 2. 基础约定

### 2.1 基础地址

```text
开发环境：http://localhost:8080/api
```

后端配置了统一上下文路径 `/api`，因此文档中的完整请求路径均以 `/api` 开头。

示例：

```text
GET http://localhost:8080/api/v1/purchase-orders
```

### 2.2 请求头

除登录接口外，其他接口均需要携带 JWT。

```http
Authorization: Bearer {token}
Content-Type: application/json
```

### 2.3 统一响应结构

所有接口返回统一结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2026-05-22 10:00:00",
  "traceId": "9f7c2a..."
}
```

### 2.4 分页响应结构

分页接口的 `data` 结构如下：

```json
{
  "records": [],
  "total": 100,
  "pageSize": 20,
  "pageNum": 1,
  "pages": 5
}
```

### 2.5 常用错误码

| code | 含义 |
|---:|---|
| 200 | 操作成功 |
| 40001 | 参数错误 |
| 40002 | 数据不存在 |
| 40101 | 未登录 |
| 40102 | 令牌过期 |
| 40301 | 无权限 |
| 40302 | 数据越权 |
| 40901 | 状态不允许 |
| 40902 | 重复提交 |
| 50001 | 系统异常 |
| 50002 | 外部系统异常 |

### 2.6 分页查询参数

多数分页接口支持：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10，最大 100 |

## 3. 认证管理

### 3.1 登录

```http
POST /api/auth/login
```

请求体：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

响应 data：

```json
{
  "token": "jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userInfo": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "role": "SUPER_ADMIN",
    "userType": 1,
    "supplierId": null,
    "roles": ["SUPER_ADMIN"],
    "permissions": ["system:user:query"]
  }
}
```

### 3.2 登出

```http
POST /api/auth/logout
```

### 3.3 当前用户信息

```http
GET /api/auth/info
```

响应 data 同 `userInfo`。

## 4. 供应商准入

### 4.1 分页查询供应商

```http
GET /api/v1/suppliers
```

权限：`supplier:info:query`

查询参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | number | 否 | 页码 |
| pageSize | number | 否 | 每页条数 |
| keyword | string | 否 | 供应商名称、编码、信用代码模糊查询 |
| status | number | 否 | 0 待审核，1 合作中，2 暂停合作，3 黑名单 |
| supplierType | number | 否 | 供应商类型 |

响应 records 字段：

```json
[
  {
    "id": 1,
    "supplierCode": "SUP001",
    "supplierName": "示例供应商",
    "supplierShortName": "示例",
    "supplierType": 1,
    "creditCode": "913xxx",
    "contactName": "张三",
    "contactPhone": "13800138000",
    "contactEmail": "test@example.com",
    "rating": 1,
    "status": 1,
    "auditTime": "2026-05-22 10:00:00",
    "auditRemark": "通过",
    "remark": "备注"
  }
]
```

### 4.2 供应商详情

```http
GET /api/v1/suppliers/{id}
```

权限：`supplier:info:query`

### 4.3 新增供应商

```http
POST /api/v1/suppliers
```

权限：`supplier:info:add`

请求体：

```json
{
  "supplierCode": "SUP001",
  "supplierName": "示例供应商",
  "supplierShortName": "示例",
  "categoryId": 1,
  "supplierType": 1,
  "creditCode": "913xxx",
  "legalPerson": "李四",
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactEmail": "test@example.com",
  "address": "上海市浦东新区",
  "remark": "备注"
}
```

响应 data：新增供应商 ID。

### 4.4 审核供应商

```http
POST /api/v1/suppliers/{id}/audit
```

权限：`supplier:info:audit`

请求体：

```json
{
  "status": 1,
  "auditRemark": "审核通过"
}
```

## 5. 采购订单

### 5.1 分页查询采购订单

```http
GET /api/v1/purchase-orders
```

权限：`order:purchase:query`

查询参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | number | 否 | 页码 |
| pageSize | number | 否 | 每页条数 |
| keyword | string | 否 | 订单号、采购员名称 |
| orderStatus | number | 否 | 订单状态 |
| supplierId | number | 否 | 供应商 ID，供应商用户后端强制使用当前用户绑定供应商 |
| startDate | string | 否 | 订单开始日期，格式 `yyyy-MM-dd` |
| endDate | string | 否 | 订单结束日期，格式 `yyyy-MM-dd` |

订单状态：`0 草稿`、`1 待确认`、`2 已确认`、`7 已取消`、`8 已拒单`。

响应 records 字段：

```json
[
  {
    "id": 1,
    "orderNo": "PO202605220001",
    "supplierId": 1,
    "orderDate": "2026-05-22",
    "deliveryDate": "2026-05-30",
    "currency": "CNY",
    "totalAmount": 1000.00,
    "payAmount": 1000.00,
    "orderStatus": 1,
    "confirmTime": null,
    "buyerId": 1,
    "buyerName": "采购员",
    "deliveryAddress": "上海市",
    "remark": "备注"
  }
]
```

### 5.2 采购订单详情

```http
GET /api/v1/purchase-orders/{id}
```

权限：`order:purchase:query`

### 5.3 新增采购订单

```http
POST /api/v1/purchase-orders
```

权限：`order:purchase:add`

请求体：

```json
{
  "orderNo": "PO202605220001",
  "supplierId": 1,
  "orderDate": "2026-05-22",
  "deliveryDate": "2026-05-30",
  "currency": "CNY",
  "totalAmount": 1000.00,
  "taxAmount": 130.00,
  "discountAmount": 0.00,
  "payAmount": 1000.00,
  "deliveryAddress": "上海市",
  "paymentTerms": "月结30天",
  "remark": "备注"
}
```

### 5.4 下发采购订单

```http
POST /api/v1/purchase-orders/{id}/publish
```

权限：`order:purchase:publish`

请求体：

```json
{
  "remark": "下发给供应商"
}
```

### 5.5 确认采购订单

```http
POST /api/v1/purchase-orders/{id}/confirm
```

权限：`order:purchase:confirm`

请求体：

```json
{
  "remark": "供应商确认接单"
}
```

### 5.6 拒绝采购订单

```http
POST /api/v1/purchase-orders/{id}/reject
```

权限：`order:purchase:confirm`

### 5.7 取消采购订单

```http
POST /api/v1/purchase-orders/{id}/cancel
```

权限：`order:purchase:cancel`

## 6. 采购订单明细

### 6.1 查询订单明细列表

```http
GET /api/v1/purchase-order-details?orderId=1
```

权限：`order:purchase:query`

响应 data：

```json
[
  {
    "id": 1,
    "orderId": 1,
    "lineNo": 10,
    "materialCode": "MAT001",
    "materialName": "物料A",
    "materialSpec": "规格",
    "materialModel": "型号",
    "unit": "PCS",
    "quantity": 100.0000,
    "unitPrice": 10.0000,
    "taxRate": 13.00,
    "taxAmount": 130.00,
    "amount": 1000.00,
    "deliveredQty": 0.0000,
    "receivedQty": 0.0000,
    "qualifiedQty": 0.0000,
    "deliveryDate": "2026-05-30",
    "remark": "备注"
  }
]
```

### 6.2 订单明细详情

```http
GET /api/v1/purchase-order-details/{id}
```

权限：`order:purchase:query`

### 6.3 新增订单明细

```http
POST /api/v1/purchase-order-details
```

权限：`order:purchase:add`

请求体：

```json
{
  "orderId": 1,
  "lineNo": 10,
  "materialCode": "MAT001",
  "materialName": "物料A",
  "materialSpec": "规格",
  "materialModel": "型号",
  "unit": "PCS",
  "quantity": 100.0000,
  "unitPrice": 10.0000,
  "taxRate": 13.00,
  "taxAmount": 130.00,
  "amount": 1000.00,
  "deliveryDate": "2026-05-30",
  "remark": "备注"
}
```

### 6.4 修改订单明细

```http
PUT /api/v1/purchase-order-details/{id}
```

权限：`order:purchase:edit`

请求体字段同新增，均可选。

### 6.5 删除订单明细

```http
DELETE /api/v1/purchase-order-details/{id}
```

权限：`order:purchase:delete`

## 7. 订单变更

### 7.1 查询订单变更

```http
GET /api/v1/order-changes?orderId=1
```

权限：`order:change:query`

### 7.2 订单变更详情

```http
GET /api/v1/order-changes/{id}
```

权限：`order:change:query`

### 7.3 新增订单变更

```http
POST /api/v1/order-changes
```

权限：`order:change:add`

请求体：

```json
{
  "orderId": 1,
  "orderDetailId": 1,
  "changeType": 1,
  "changeContent": "数量由100变更为120",
  "beforeValue": "100",
  "afterValue": "120",
  "changeReason": "需求增加"
}
```

变更类型：`1 数量变更`、`2 价格变更`、`3 交期变更`、`4 取消`、`5 其他`。

### 7.4 审批订单变更

```http
POST /api/v1/order-changes/{id}/approve
```

权限：`order:change:approve`

请求体：

```json
{
  "approveStatus": 1,
  "approveRemark": "同意"
}
```

审批状态：`0 待审批`、`1 已通过`、`2 已拒绝`。

## 8. 送货通知

### 8.1 分页查询送货通知

```http
GET /api/v1/delivery-notices
```

权限：`delivery:notice:query`

查询参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | number | 否 | 页码 |
| pageSize | number | 否 | 每页条数 |
| keyword | string | 否 | 通知单号、订单号 |
| supplierId | number | 否 | 供应商 ID |
| deliveryStatus | number | 否 | 送货状态 |
| startDate | string | 否 | 计划送货开始日期 |
| endDate | string | 否 | 计划送货结束日期 |

送货状态：`0 待发货`、`1 已发货`、`2 运输中`、`3 已送达`、`4 已收货`、`5 已拒收`。

### 8.2 送货通知详情

```http
GET /api/v1/delivery-notices/{id}
```

权限：`delivery:notice:query`

### 8.3 新增送货通知

```http
POST /api/v1/delivery-notices
```

权限：`delivery:notice:add`

请求体：

```json
{
  "noticeNo": "ASN202605220001",
  "orderId": 1,
  "orderNo": "PO202605220001",
  "supplierId": 1,
  "supplierName": "示例供应商",
  "planDeliveryDate": "2026-05-25",
  "deliveryMethod": "物流",
  "deliveryCompany": "顺丰",
  "deliveryNo": "SF123456",
  "driverName": "王五",
  "driverPhone": "13800138001",
  "vehicleNo": "沪A12345",
  "deliveryAddress": "上海市",
  "receiver": "仓库员",
  "receiverPhone": "13800138002",
  "remark": "备注"
}
```

### 8.4 发货

```http
POST /api/v1/delivery-notices/{id}/ship
```

权限：`delivery:notice:ship`

请求体可为空，或：

```json
{
  "remark": "已发货"
}
```

### 8.5 到达确认

```http
POST /api/v1/delivery-notices/{id}/arrive
```

权限：`delivery:notice:ship`

## 9. 送货明细

### 9.1 查询送货明细

```http
GET /api/v1/delivery-details?noticeId=1
```

权限：`delivery:notice:query`

### 9.2 新增送货明细

```http
POST /api/v1/delivery-details
```

权限：`delivery:notice:add`

请求体：

```json
{
  "noticeId": 1,
  "orderDetailId": 1,
  "materialCode": "MAT001",
  "materialName": "物料A",
  "materialSpec": "规格",
  "unit": "PCS",
  "planQty": 100.0000,
  "actualQty": 100.0000,
  "receivedQty": 0.0000,
  "qualifiedQty": 0.0000,
  "batchNo": "BATCH001",
  "productionDate": "2026-05-20",
  "remark": "备注"
}
```

### 9.3 送货明细详情 / 修改 / 删除

```http
GET /api/v1/delivery-details/{id}
PUT /api/v1/delivery-details/{id}
DELETE /api/v1/delivery-details/{id}
```

权限分别为：查询 `delivery:notice:query`，修改/删除 `delivery:notice:edit`。

## 10. 收货记录

### 10.1 分页查询收货记录

```http
GET /api/v1/receipt-records
```

权限：`delivery:receipt:query`

查询参数：`pageNum`、`pageSize`、`noticeId`、`deliveryId`、`materialCode`、`receiptStatus`、`startTime`、`endTime`。

收货状态：`0 待收货`、`1 已收货`、`2 已拒收`。

### 10.2 新增收货记录

```http
POST /api/v1/receipt-records
```

权限：`delivery:receipt:confirm`

请求体：

```json
{
  "deliveryId": 1,
  "noticeId": 1,
  "materialCode": "MAT001",
  "materialName": "物料A",
  "planQty": 100.0000,
  "receiptQty": 0.0000,
  "rejectQty": 0.0000,
  "warehouseId": 1,
  "warehouseName": "一号仓",
  "location": "A-01",
  "remark": "备注"
}
```

### 10.3 确认收货

```http
POST /api/v1/receipt-records/{id}/confirm
```

权限：`delivery:receipt:confirm`

请求体：

```json
{
  "receiptQty": 98.0000,
  "rejectQty": 2.0000,
  "warehouseId": 1,
  "warehouseName": "一号仓",
  "location": "A-01",
  "rejectReason": "外箱破损",
  "remark": "备注"
}
```

### 10.4 拒收

```http
POST /api/v1/receipt-records/{id}/reject
```

权限：`delivery:receipt:reject`

请求体同确认收货。

## 11. 质量检验

### 11.1 分页查询质检单

```http
GET /api/v1/quality-inspections
```

权限：`delivery:inspection:query`

查询参数：`pageNum`、`pageSize`、`receiptId`、`deliveryId`、`materialCode`、`inspectResult`、`inspectType`、`startTime`、`endTime`。

检验结果：`0 待检验`、`1 合格`、`2 不合格`、`3 部分合格`。

检验类型：`1 来料检验`、`2 抽检`、`3 全检`。

### 11.2 新增质检单

```http
POST /api/v1/quality-inspections
```

权限：`delivery:inspection:submit`

请求体：

```json
{
  "receiptId": 1,
  "deliveryId": 1,
  "materialCode": "MAT001",
  "materialName": "物料A",
  "inspectQty": 100.0000,
  "inspectType": 1,
  "inspectRemark": "待检验"
}
```

### 11.3 提交检验结果

```http
POST /api/v1/quality-inspections/{id}/submit
```

权限：`delivery:inspection:submit`

请求体：

```json
{
  "inspectQty": 100.0000,
  "qualifiedQty": 98.0000,
  "unqualifiedQty": 2.0000,
  "inspectResult": 3,
  "inspectType": 1,
  "inspectRemark": "部分合格"
}
```

### 11.4 不合格处理

```http
POST /api/v1/quality-inspections/{id}/handle
```

权限：`delivery:inspection:submit`

请求体：

```json
{
  "handleMethod": 1,
  "handleRemark": "退货处理"
}
```

处理方式：`1 退货`、`2 换货`、`3 特采`、`4 报废`。

## 12. 财务对账

### 12.1 分页查询对账单

```http
GET /api/v1/reconciliations
```

权限：`finance:reconciliation:query`

查询参数：`pageNum`、`pageSize`、`keyword`、`supplierId`、`reconPeriod`、`reconStatus`。

对账状态：`0 待对账`、`1 对账中`、`2 已确认`、`3 有异议`、`4 已完成`。

### 12.2 新增对账单

```http
POST /api/v1/reconciliations
```

权限：`finance:reconciliation:add`

请求体：

```json
{
  "reconNo": "REC202605220001",
  "supplierId": 1,
  "supplierName": "示例供应商",
  "reconPeriod": "2026-05",
  "startDate": "2026-05-01",
  "endDate": "2026-05-31",
  "totalAmount": 10000.00,
  "remark": "备注"
}
```

### 12.3 发送对账单

```http
POST /api/v1/reconciliations/{id}/send
```

权限：`finance:reconciliation:send`

### 12.4 确认对账单

```http
POST /api/v1/reconciliations/{id}/confirm
```

权限：`finance:reconciliation:confirm`

请求体：

```json
{
  "confirmedAmount": 9900.00,
  "diffAmount": 100.00,
  "disputed": true,
  "confirmRemark": "存在差异"
}
```

## 13. 对账明细

### 13.1 查询对账明细

```http
GET /api/v1/reconciliation-details?reconId=1
```

权限：`finance:reconciliation:query`

### 13.2 新增对账明细

```http
POST /api/v1/reconciliation-details
```

权限：`finance:reconciliation:add`

请求体：

```json
{
  "reconId": 1,
  "orderId": 1,
  "orderNo": "PO202605220001",
  "deliveryId": 1,
  "deliveryNo": "ASN202605220001",
  "materialCode": "MAT001",
  "materialName": "物料A",
  "quantity": 100.0000,
  "unitPrice": 10.0000,
  "orderAmount": 1000.00,
  "confirmedAmount": 1000.00,
  "diffAmount": 0.00,
  "diffReason": null,
  "confirmStatus": 0,
  "confirmRemark": null,
  "remark": "备注"
}
```

### 13.3 对账明细详情 / 修改 / 删除

```http
GET /api/v1/reconciliation-details/{id}
PUT /api/v1/reconciliation-details/{id}
DELETE /api/v1/reconciliation-details/{id}
```

权限分别为：查询 `finance:reconciliation:query`，新增 `finance:reconciliation:add`，修改/删除 `finance:reconciliation:edit`。

## 14. 附件管理

### 14.1 分页查询附件

```http
GET /api/v1/file-attachments
```

权限：登录即可。

查询参数：`pageNum`、`pageSize`、`businessType`、`businessId`、`businessNo`、`fileName`。

### 14.2 附件详情

```http
GET /api/v1/file-attachments/{id}
```

### 14.3 登记附件元数据

```http
POST /api/v1/file-attachments
```

请求体：

```json
{
  "businessType": "purchase_order",
  "businessId": 1,
  "businessNo": "PO202605220001",
  "fileName": "合同.pdf",
  "fileExt": "pdf",
  "fileSize": 102400,
  "contentType": "application/pdf",
  "bucketName": "supplier-contract",
  "objectKey": "order/2026/05/22/PO202605220001/uuid.pdf",
  "fileHash": "sha256..."
}
```

### 14.4 禁用附件

```http
DELETE /api/v1/file-attachments/{id}
```

## 15. 审计日志

### 15.1 分页查询审计日志

```http
GET /api/v1/audit-logs
```

权限：`system:permission:query`

查询参数：`pageNum`、`pageSize`、`traceId`、`userId`、`businessType`、`businessNo`。

### 15.2 审计日志详情

```http
GET /api/v1/audit-logs/{id}
```

权限：`system:permission:query`

## 16. 导出任务

### 16.1 分页查询导出任务

```http
GET /api/v1/export-tasks
```

权限：登录即可。

查询参数：`pageNum`、`pageSize`、`taskNo`、`taskType`、`taskStatus`。

任务状态：`0 待处理`、`1 处理中`、`2 成功`、`3 失败`。

### 16.2 导出任务详情

```http
GET /api/v1/export-tasks/{id}
```

### 16.3 创建导出任务

```http
POST /api/v1/export-tasks
```

请求体：

```json
{
  "taskType": "purchase_order_export",
  "exportParams": "{\"orderStatus\":1}",
  "totalCount": 0
}
```

响应 data：导出任务 ID。

## 17. 看板、消息与待办

### 17.1 看板指标

```http
GET /api/v1/dashboard/metrics
```

响应 data：

```json
[
  { "name": "订单总数", "value": 120, "unit": "单" },
  { "name": "待确认订单", "value": 8, "unit": "单" }
]
```

### 17.2 趋势数据

```http
GET /api/v1/dashboard/trends
```

响应 data：

```json
[
  {
    "period": "2026-05",
    "orderCount": 20,
    "deliveryCount": 15,
    "qualityIssueCount": 2,
    "reconciliationCount": 6
  }
]
```

### 17.3 风险数据

```http
GET /api/v1/dashboard/risks
```

响应 data：

```json
[
  { "riskType": "delivery_delay", "title": "计划送货已逾期", "count": 3, "level": "danger" }
]
```

### 17.4 供应商绩效

```http
GET /api/v1/dashboard/supplier-performance
```

### 17.5 消息列表

```http
GET /api/v1/messages
```

查询参数：`pageNum`、`pageSize`、`channel`、`sendStatus`、`readStatus`、`businessType`。

### 17.6 未读消息数量

```http
GET /api/v1/messages/unread-count
```

### 17.7 发送消息

```http
POST /api/v1/messages
```

请求体：

```json
{
  "receiverUserId": 1,
  "receiverSupplierId": null,
  "channel": 1,
  "title": "订单待确认",
  "content": "您有新的采购订单待确认",
  "businessType": "purchase_order",
  "businessId": 1
}
```

### 17.8 标记消息已读

```http
POST /api/v1/messages/{id}/read
POST /api/v1/messages/read-all
```

### 17.9 待办列表与数量

```http
GET /api/v1/todos
GET /api/v1/todos/unread-count
```

查询参数：`pageNum`、`pageSize`、`todoType`、`businessType`、`todoStatus`。

### 17.10 完成或忽略待办

```http
POST /api/v1/todos/{id}/finish
POST /api/v1/todos/{id}/ignore
```

## 18. 文件流、导入与导出下载

### 18.1 附件上传

```http
POST /api/v1/file-attachments/upload
Content-Type: multipart/form-data
```

表单字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | 上传文件 |
| businessType | string | 是 | 业务类型 |
| businessId | number | 否 | 业务 ID |
| businessNo | string | 否 | 业务单号 |

响应 data：附件 ID。

### 18.2 附件下载与预览

```http
GET /api/v1/file-attachments/{id}/download
GET /api/v1/file-attachments/{id}/preview
```

返回二进制文件流，下载接口带 `Content-Disposition: attachment`，预览接口直接返回文件 MIME 类型。

### 18.3 导出文件下载

```http
GET /api/v1/export-tasks/{id}/download
```

说明：仅当导出任务状态为成功且存在 `fileId` 时可下载。

### 18.4 导入模板下载

```http
GET /api/v1/imports/{importType}/template
```

返回 CSV 模板文件流。

### 18.5 导入校验

```http
POST /api/v1/imports/{importType}/check
Content-Type: multipart/form-data
```

表单字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | 待校验文件 |

响应 data：

```json
{
  "fileId": 1,
  "importType": "supplier",
  "totalCount": 100,
  "successCount": 98,
  "errorCount": 2,
  "errorFileId": 10
}
```

### 18.6 提交导入

```http
POST /api/v1/imports/{importType}/submit?fileId=1
```

响应 data：导入任务 ID。

### 18.7 错误明细下载

```http
GET /api/v1/imports/errors/{fileId}/download
```

返回错误明细文件流。

## 19. 前端对接建议

### 17.1 Axios 拦截器建议

```javascript
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(response => {
  const res = response.data
  if (res.code !== 200) {
    return Promise.reject(res)
  }
  return res.data
})

export default request
```

### 19.2 状态展示建议

前端不要在多个页面重复写死状态，建议统一维护枚举：

```javascript
export const orderStatusMap = {
  0: '草稿',
  1: '待确认',
  2: '已确认',
  7: '已取消',
  8: '已拒单'
}
```

### 19.3 权限按钮建议

登录后从 `/api/auth/info` 获取 `permissions`，用于控制菜单与按钮展示。按钮隐藏只做体验控制，后端仍会校验权限。

```javascript
function hasPerm(code) {
  return userInfo.permissions?.includes(code)
}
```

## 20. 当前接口清单

| 模块 | 接口数量 | 说明 |
|---|---:|---|
| 认证管理 | 3 | 登录、登出、当前用户 |
| 供应商准入 | 4 | 查询、新增、审核 |
| 采购订单 | 7 | 查询、新增、下发、确认、拒单、取消 |
| 采购订单明细 | 5 | 查询、新增、修改、删除 |
| 订单变更 | 4 | 查询、新增、审批 |
| 送货通知 | 5 | 查询、新增、发货、到达 |
| 送货明细 | 5 | 查询、新增、修改、删除 |
| 收货记录 | 5 | 查询、新增、确认、拒收 |
| 质量检验 | 5 | 查询、新增、提交、处理 |
| 对账单 | 5 | 查询、新增、发送、确认 |
| 对账明细 | 5 | 查询、新增、修改、删除 |
| 看板 | 4 | 指标、趋势、风险、绩效 |
| 消息通知 | 6 | 列表、详情、未读数量、发送、已读 |
| 待办 | 4 | 列表、数量、完成、忽略 |
| 附件管理 | 7 | 查询、登记、上传、下载、预览、禁用 |
| 审计日志 | 2 | 查询、详情 |
| 导出任务 | 4 | 查询、新建、下载 |
| 导入任务 | 4 | 模板、校验、提交、错误下载 |
