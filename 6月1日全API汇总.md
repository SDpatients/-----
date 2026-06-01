# 6月1日全API汇总

> 本文档汇总供应商协同系统全部后端API接口，按模块分5个部分编写。
> 目的：帮助前端开发人员准确传参，避免参数错误和校验报错。

---

## 目录

- [第1部分：通用规范 + 认证与通用模块](#第1部分通用规范--认证与通用模块)
- [第2部分：订单模块](#第2部分订单模块)
- [第3部分：物流模块](#第3部分物流模块)
- [第4部分：质量模块](#第4部分质量模块)
- [第5部分：结算 + 门户 + 消息模块](#第5部分结算--门户--消息模块)

---

# 第1部分：通用规范 + 认证与通用模块

## 一、通用响应结构

所有API统一使用 `Result<T>` 包装响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2026-06-01T10:30:00",
  "traceId": "a1b2c3d4e5f6"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200=成功，其他=失败 |
| message | String | 提示信息 |
| data | T | 业务数据，失败时为null |
| timestamp | LocalDateTime | 响应时间 |
| traceId | String | 链路追踪ID，排查问题时需提供 |

### 判断请求是否成功

```javascript
if (response.data.code === 200) {
  // 成功
} else {
  // 失败，查看 response.data.message
}
```

## 二、分页响应结构

分页接口使用 `Result<PageResult<T>>` 包装：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [ ... ],
    "total": 100,
    "pageSize": 10,
    "pageNum": 1,
    "pages": 10
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| records | List\<T\> | 当前页数据列表 |
| total | Long | 总记录数 |
| pageSize | Long | 每页条数 |
| pageNum | Long | 当前页码 |
| pages | Long | 总页数 |

## 三、错误码一览

| 错误码 | 含义 | 前端应对 |
|--------|------|----------|
| 200 | 操作成功 | 正常处理 |
| 40001 | 参数错误 | 检查请求参数，对照本文档修正 |
| 40002 | 资源不存在 | 确认ID是否正确，可能已被删除 |
| 40101 | 未登录 | 跳转登录页 |
| 40102 | 令牌过期 | 刷新token或重新登录 |
| 40301 | 无权限 | 提示用户无操作权限 |
| 40302 | 数据越权 | 提示用户无权访问该数据 |
| 40900 | 业务异常 | 展示message内容给用户 |
| 40901 | 状态不允许 | 当前业务状态不允许此操作 |
| 40902 | 重复提交 | 提示用户勿重复提交 |
| 50001 | 系统异常 | 提示系统异常，联系管理员 |
| 50002 | 外部系统异常 | 提示外部系统异常，稍后重试 |

## 四、常见参数错误及避免方法

### 4.1 请求体JSON格式错误

**错误现象**：返回 `40001 - 请求体格式错误，请检查JSON数据格式和字段类型`

**常见原因**：
- JSON语法错误（缺少引号、逗号、括号不匹配）
- 字段类型不匹配（如将字符串传给了数字字段）
- 日期格式不正确

**避免方法**：
```javascript
// ✅ 正确：数字字段传数字
{ "supplierId": 123, "totalAmount": 1000.50 }

// ❌ 错误：数字字段传字符串
{ "supplierId": "123", "totalAmount": "1000.50" }

// ✅ 正确：日期字段使用 ISO 格式 (yyyy-MM-dd)
{ "orderDate": "2026-06-01", "deliveryDate": "2026-06-15" }

// ❌ 错误：日期格式不正确
{ "orderDate": "2026/06/01", "deliveryDate": "06-01-2026" }

// ✅ 正确：日期时间字段使用 ISO 格式 (yyyy-MM-ddTHH:mm:ss)
{ "receiptTime": "2026-06-01T10:30:00" }

// ❌ 错误：日期时间格式不正确
{ "receiptTime": "2026-06-01 10:30:00" }
```

### 4.2 必填字段缺失

**错误现象**：返回 `40001 - 不能为null` 或 `不能为空`

**避免方法**：
- 本文档中标注 **@NotNull** 的字段必须传值且不能为null
- 标注 **@NotBlank** 的字段必须传非空字符串
- 标注 **@NotEmpty** 的字段（如List）必须传非空集合
- 标注 **@Valid** 的嵌套对象内部校验也会生效

### 4.3 参数类型不匹配

**错误现象**：返回 `40001 - 参数类型错误: xxx`

**常见类型对照**：

| Java类型 | 前端JS类型 | 示例值 | 注意事项 |
|----------|-----------|--------|----------|
| Long | number | `123` | 不要传字符串 |
| Integer | number | `1` | 不要传字符串 |
| BigDecimal | number | `1000.50` | 不要传字符串，精度问题注意 |
| String | string | `"ABC"` | - |
| LocalDate | string | `"2026-06-01"` | 必须是 yyyy-MM-dd 格式 |
| LocalDateTime | string | `"2026-06-01T10:30:00"` | 必须是 ISO 格式 |
| List\<T\> | Array | `[...]` | 空数组用 `[]` |
| Boolean | boolean | `true` | 不要传字符串 |

### 4.4 分页参数越界

**错误现象**：返回 `40001 - 最小不能小于1` 等

**避免方法**：
- `pageNum` 必须 ≥ 1
- `pageSize` 必须 ≥ 1 且 ≤ 100
- 不传时默认 `pageNum=1, pageSize=10`

### 4.5 请求方法错误

**错误现象**：返回 `405 - 不支持的请求方法: GET`

**避免方法**：
- 严格按照文档中的HTTP方法调用（GET/POST/PUT/DELETE）
- 查询接口用GET，创建用POST，更新用PUT，删除用DELETE

### 4.6 Content-Type错误

**错误现象**：返回 `415 - 不支持的Content-Type，请使用application/json`

**避免方法**：
```javascript
// ✅ 正确：所有JSON请求设置Content-Type
axios.post('/api/v1/purchase-orders', data, {
  headers: { 'Content-Type': 'application/json' }
})

// 文件上传除外，使用 multipart/form-data
const formData = new FormData();
formData.append('file', fileObject);
axios.post('/api/v1/eight-d-reports/1/upload-attachment', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
```

### 4.7 幂等性防重复提交

**错误现象**：返回 `40902 - 重复提交`

**避免方法**：
1. 提交表单前先调用 `GET /common/idempotent-token` 获取幂等令牌
2. 将令牌放入提交请求的Header中（具体Header名参考后端IdempotentAspect实现）
3. 令牌5分钟内有效，使用后即失效

### 4.8 数据库约束违反

**错误现象**：返回 `40001 - 数据重复，该记录已存在` 或 `字段值超出长度限制`

**避免方法**：
- 唯一字段（如单号）不要重复提交
- 字符串字段注意长度限制
- 必填字段不要传null

## 五、认证模块 API

### 5.1 用户登录

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/auth/login` |
| **Content-Type** | application/json |
| **描述** | 通过用户名密码登录获取JWT token |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| username | String | ✅ | @NotBlank | 用户名 |
| password | String | ✅ | @NotBlank | 密码 |

**请求示例**：
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应数据** `Result<LoginResponse>`：

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | JWT令牌 |
| tokenType | String | 令牌类型（Bearer） |
| expiresIn | Long | 过期时间（秒） |
| userInfo | Object | 用户信息 |

**userInfo 字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| realName | String | 真实姓名 |
| email | String | 邮箱 |
| phone | String | 手机号 |
| role | String | 主角色 |
| userType | Integer | 用户类型 |
| supplierId | Long | 供应商ID（供应商用户有值） |
| roles | List\<String\> | 角色列表 |
| permissions | List\<String\> | 权限列表 |

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "role": "ADMIN",
      "userType": 1,
      "supplierId": null,
      "roles": ["ADMIN"],
      "permissions": ["*"]
    }
  }
}
```

**前端调用注意**：
- 登录成功后将token存储到localStorage或vuex/pinia
- 后续请求在Header中携带：`Authorization: Bearer {token}`
- token过期后需重新登录

---

### 5.2 用户登出

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/auth/logout` |
| **描述** | 退出登录清除认证信息 |

**请求参数**：无

**响应数据**：`Result<Void>`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**前端调用注意**：
- 登出后清除本地存储的token和用户信息
- 跳转到登录页

---

### 5.3 获取当前用户信息

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/auth/info` |
| **描述** | 获取当前登录用户的信息 |

**请求参数**：无

**响应数据**：`Result<UserInfo>`

响应data字段同上方 LoginResponse.userInfo。

**前端调用注意**：
- 页面刷新时调用此接口恢复用户信息
- 若返回40101，说明token已过期，需重新登录

---

## 六、通用模块 API

### 6.1 获取幂等令牌

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/common/idempotent-token` |
| **描述** | 获取一次性幂等令牌，防止重复提交 |

**请求参数**：无

**响应数据**：`Result<String>`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "a1b2c3d4e5f67890"
}
```

**前端调用注意**：
- 在提交创建类表单前调用此接口获取令牌
- 令牌5分钟内有效，使用一次后即失效
- 同一令牌不可重复使用

---

## 七、认证模块API速查表

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 1 | POST | `/auth/login` | LoginRequest | Result\<LoginResponse\> | 用户登录 |
| 2 | POST | `/auth/logout` | 无 | Result\<Void\> | 用户登出 |
| 3 | GET | `/auth/info` | 无 | Result\<UserInfo\> | 获取当前用户信息 |
| 4 | GET | `/common/idempotent-token` | 无 | Result\<String\> | 获取幂等令牌 |

---

# 第2部分：订单模块

## 一、采购订单管理 (PurchaseOrderController)

**基础路径**：`/v1/purchase-orders`
**权限**：所有接口需认证（isAuthenticated）

### 1.1 分页查询采购订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/purchase-orders` |
| **描述** | 分页查询采购订单列表 |

**Query参数** (PurchaseOrderQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| keyword | String | 否 | - | - | 关键词搜索 |
| orderStatus | Integer | 否 | - | - | 订单状态筛选 |
| supplierId | Long | 否 | - | - | 供应商ID筛选 |
| startDate | LocalDate | 否 | - | - | 起始日期(yyyy-MM-dd) |
| endDate | LocalDate | 否 | - | - | 结束日期(yyyy-MM-dd) |

**请求示例**：
```
GET /v1/purchase-orders?pageNum=1&pageSize=10&orderStatus=1&supplierId=123
```

**响应数据**：`Result<PageResult<PurchaseOrderVO>>`

---

### 1.2 查询采购订单详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/purchase-orders/{id}` |
| **描述** | 根据订单ID查询采购订单详情 |

**路径参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 订单ID |

**响应数据**：`Result<PurchaseOrderVO>`

---

### 1.3 新增采购订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders` |
| **描述** | 新增采购订单 |

**请求体** (PurchaseOrderCreateDTO, @Valid)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| orderNo | String | ✅ | - | @NotBlank | 订单号 |
| supplierId | Long | ✅ | - | @NotNull | 供应商ID |
| orderDate | LocalDate | ✅ | - | @NotNull | 订单日期 |
| deliveryDate | LocalDate | 否 | - | - | 交货日期 |
| currency | String | 否 | "CNY" | - | 币种 |
| totalAmount | BigDecimal | 否 | ZERO | - | 总金额 |
| taxAmount | BigDecimal | 否 | ZERO | - | 税额 |
| discountAmount | BigDecimal | 否 | ZERO | - | 折扣金额 |
| payAmount | BigDecimal | 否 | ZERO | - | 应付金额 |
| deliveryAddress | String | 否 | - | - | 交货地址 |
| paymentTerms | String | 否 | - | - | 付款条件 |
| remark | String | 否 | - | - | 备注 |

**请求示例**：
```json
{
  "orderNo": "PO20260601001",
  "supplierId": 100,
  "orderDate": "2026-06-01",
  "deliveryDate": "2026-06-15",
  "currency": "CNY",
  "totalAmount": 50000.00,
  "taxAmount": 6500.00,
  "discountAmount": 0,
  "payAmount": 56500.00,
  "deliveryAddress": "上海市浦东新区XX路XX号",
  "paymentTerms": "月结30天",
  "remark": ""
}
```

**响应数据**：`Result<Long>`（返回新建订单ID）

**前端注意**：
- `orderNo`、`supplierId`、`orderDate` 为必填
- 金额类字段传数字，不要传字符串
- 日期格式必须是 `yyyy-MM-dd`

---

### 1.4 下发采购订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/publish` |
| **描述** | 下发采购订单给供应商 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体** (OrderActionDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 1.5 供应商确认订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/confirm` |
| **描述** | 供应商确认接单 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体**：OrderActionDTO（同1.4）

**响应数据**：`Result<Void>`

---

### 1.6 供应商拒绝订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/reject` |
| **描述** | 供应商拒绝订单 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体**：OrderActionDTO（同1.4）

**响应数据**：`Result<Void>`

---

### 1.7 取消采购订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/cancel` |
| **描述** | 取消采购订单 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体**：OrderActionDTO（同1.4）

**响应数据**：`Result<Void>`

---

### 1.8 采购方确认供应商接单结果

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/confirm-by-buyer` |
| **描述** | 采购方确认供应商的接单结果 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体** (BuyerConfirmDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 1.9 关闭采购订单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/close` |
| **描述** | 关闭采购订单 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体** (OrderCloseDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 1.10 供应商提交交期反馈

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/{id}/delivery-feedback` |
| **描述** | 供应商提交交期反馈 |

**路径参数**：`id` (Long, 必填) — 订单ID

**请求体** (DeliveryFeedbackCreateDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| lines | List\<DeliveryFeedbackLineDTO\> | ✅ | @NotEmpty @Valid | 交期反馈明细列表 |
| remark | String | 否 | - | 备注 |

**DeliveryFeedbackLineDTO**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| orderDetailId | Long | ✅ | @NotNull | 订单明细ID |
| promisedDeliveryDate | LocalDate | ✅ | @NotNull | 承诺交期 |
| plannedQuantity | BigDecimal | ✅ | @NotNull @Positive | 计划交付数量 |
| batchNo | String | 否 | - | 批次号 |
| remark | String | 否 | - | 备注 |

**请求示例**：
```json
{
  "lines": [
    {
      "orderDetailId": 201,
      "promisedDeliveryDate": "2026-06-20",
      "plannedQuantity": 500,
      "batchNo": "B20260601",
      "remark": ""
    },
    {
      "orderDetailId": 202,
      "promisedDeliveryDate": "2026-06-25",
      "plannedQuantity": 300,
      "batchNo": "B20260602",
      "remark": ""
    }
  ],
  "remark": "部分物料需分批交付"
}
```

**响应数据**：`Result<Long>`（返回反馈记录ID）

**前端注意**：
- `lines` 不能为空数组，至少包含一条明细
- `plannedQuantity` 必须为正数
- 嵌套的 `DeliveryFeedbackLineDTO` 内部校验也会生效

---

### 1.11 查询交期反馈

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/purchase-orders/{id}/delivery-feedback` |
| **描述** | 根据订单ID查询交期反馈 |

**路径参数**：`id` (Long, 必填) — 订单ID

**响应数据**：`Result<DeliveryFeedbackVO>`

---

### 1.12 采购方确认交期反馈

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/purchase-orders/delivery-feedback/{feedbackId}/confirm` |
| **描述** | 采购方确认交期反馈 |

**路径参数**：`feedbackId` (Long, 必填) — 反馈ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

## 二、订单变更管理 (OrderChangeController)

**基础路径**：`/v1/order-changes`
**权限**：所有接口需认证

### 2.1 分页查询订单变更

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/order-changes` |
| **描述** | 分页查询订单变更列表 |

**Query参数** (OrderChangeQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| orderId | Long | 否 | - | - | 按订单ID筛选 |

**响应数据**：`Result<PageResult<OrderChangeVO>>`

---

### 2.2 查询订单变更详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/order-changes/{id}` |
| **描述** | 根据变更ID查询订单变更详情 |

**路径参数**：`id` (Long, 必填) — 变更ID

**响应数据**：`Result<OrderChangeVO>`

---

### 2.3 创建订单变更

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/order-changes` |
| **描述** | 创建订单变更申请 |

**请求体** (OrderChangeCreateDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| orderId | Long | ✅ | @NotNull | 订单ID |
| orderDetailId | Long | 否 | - | 订单明细ID |
| changeType | Integer | ✅ | @NotNull | 变更类型 |
| changeContent | String | ✅ | @NotBlank | 变更内容 |
| beforeValue | String | 否 | - | 变更前值 |
| afterValue | String | 否 | - | 变更后值 |
| changeReason | String | 否 | - | 变更原因 |

**响应数据**：`Result<Long>`（返回新建变更ID）

---

### 2.4 审批订单变更

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/order-changes/{id}/approve` |
| **描述** | 审批订单变更申请 |

**路径参数**：`id` (Long, 必填) — 变更ID

**请求体** (OrderChangeApproveDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| approveStatus | Integer | ✅ | @NotNull | 审批状态 |
| approveRemark | String | 否 | - | 审批备注 |

**响应数据**：`Result<Void>`

---

## 三、订单交付跟踪 (OrderTrackController)

**基础路径**：`/v1/order-tracks`
**权限**：所有接口需认证

### 3.1 查询订单全生命周期轨迹

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/order-tracks/order/{orderId}` |
| **描述** | 按订单ID查询全生命周期交付轨迹 |

**路径参数**：`orderId` (Long, 必填) — 订单ID

**响应数据**：`Result<List<OrderTrackVO>>`

---

## 四、辅助DTO（嵌套使用）

### PurchaseOrderDetailCreateDTO

创建采购订单明细行时使用：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| orderId | Long | ✅ | - | @NotNull | 订单ID |
| lineNo | Integer | 否 | - | - | 行号 |
| materialCode | String | ✅ | - | @NotBlank | 物料编码 |
| materialName | String | ✅ | - | @NotBlank | 物料名称 |
| materialSpec | String | 否 | - | - | 物料规格 |
| materialModel | String | 否 | - | - | 物料型号 |
| unit | String | 否 | - | - | 单位 |
| quantity | BigDecimal | 否 | ZERO | - | 数量 |
| unitPrice | BigDecimal | 否 | ZERO | - | 单价 |
| taxRate | BigDecimal | 否 | ZERO | - | 税率 |
| taxAmount | BigDecimal | 否 | ZERO | - | 税额 |
| amount | BigDecimal | 否 | ZERO | - | 金额 |
| deliveryDate | LocalDate | 否 | - | - | 交货日期 |
| remark | String | 否 | - | - | 备注 |

### PurchaseOrderDetailUpdateDTO

更新采购订单明细行时使用（所有字段可选）：

| 字段 | 类型 | 说明 |
|------|------|------|
| lineNo | Integer | 行号 |
| materialCode | String | 物料编码 |
| materialName | String | 物料名称 |
| materialSpec | String | 物料规格 |
| materialModel | String | 物料型号 |
| unit | String | 单位 |
| quantity | BigDecimal | 数量 |
| unitPrice | BigDecimal | 单价 |
| taxRate | BigDecimal | 税率 |
| taxAmount | BigDecimal | 税额 |
| amount | BigDecimal | 金额 |
| deliveryDate | LocalDate | 交货日期 |
| remark | String | 备注 |

---

## 五、订单模块API速查表

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 1 | GET | `/v1/purchase-orders` | PurchaseOrderQuery | Result\<PageResult\<PurchaseOrderVO\>\> | 分页查询采购订单 |
| 2 | GET | `/v1/purchase-orders/{id}` | - | Result\<PurchaseOrderVO\> | 查询采购订单详情 |
| 3 | POST | `/v1/purchase-orders` | PurchaseOrderCreateDTO | Result\<Long\> | 新增采购订单 |
| 4 | POST | `/v1/purchase-orders/{id}/publish` | OrderActionDTO | Result\<Void\> | 下发采购订单 |
| 5 | POST | `/v1/purchase-orders/{id}/confirm` | OrderActionDTO | Result\<Void\> | 供应商确认订单 |
| 6 | POST | `/v1/purchase-orders/{id}/reject` | OrderActionDTO | Result\<Void\> | 供应商拒绝订单 |
| 7 | POST | `/v1/purchase-orders/{id}/cancel` | OrderActionDTO | Result\<Void\> | 取消采购订单 |
| 8 | POST | `/v1/purchase-orders/{id}/confirm-by-buyer` | BuyerConfirmDTO | Result\<Void\> | 采购方确认接单结果 |
| 9 | POST | `/v1/purchase-orders/{id}/close` | OrderCloseDTO | Result\<Void\> | 关闭采购订单 |
| 10 | POST | `/v1/purchase-orders/{id}/delivery-feedback` | DeliveryFeedbackCreateDTO | Result\<Long\> | 供应商提交交期反馈 |
| 11 | GET | `/v1/purchase-orders/{id}/delivery-feedback` | - | Result\<DeliveryFeedbackVO\> | 查询交期反馈 |
| 12 | POST | `/v1/purchase-orders/delivery-feedback/{feedbackId}/confirm` | - | Result\<Void\> | 采购方确认交期反馈 |
| 13 | GET | `/v1/order-changes` | OrderChangeQuery | Result\<PageResult\<OrderChangeVO\>\> | 分页查询订单变更 |
| 14 | GET | `/v1/order-changes/{id}` | - | Result\<OrderChangeVO\> | 查询订单变更详情 |
| 15 | POST | `/v1/order-changes` | OrderChangeCreateDTO | Result\<Long\> | 创建订单变更 |
| 16 | POST | `/v1/order-changes/{id}/approve` | OrderChangeApproveDTO | Result\<Void\> | 审批订单变更 |
| 17 | GET | `/v1/order-tracks/order/{orderId}` | - | Result\<List\<OrderTrackVO\>\> | 查询订单全生命周期轨迹 |

---

# 第3部分：物流模块

## 一、送货明细管理 (DeliveryDetailController)

**基础路径**：`/v1/delivery-details`
**权限**：所有接口需认证

### 1.1 查询送货明细列表

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/delivery-details` |
| **描述** | 根据送货通知ID查询该通知下所有送货明细 |

**Query参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| noticeId | Long | ✅ | 送货通知ID |

**请求示例**：
```
GET /v1/delivery-details?noticeId=100
```

**响应数据**：`Result<List<DeliveryDetailVO>>`

---

### 1.2 查询送货明细详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/delivery-details/{id}` |
| **描述** | 根据明细ID查询单条送货明细详情 |

**路径参数**：`id` (Long, 必填) — 明细ID

**响应数据**：`Result<DeliveryDetailVO>`

---

### 1.3 创建送货明细

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/delivery-details` |
| **描述** | 创建一条送货明细记录 |

**请求体** (DeliveryDetailCreateDTO)：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| noticeId | Long | ✅ | - | 送货通知ID |
| orderDetailId | Long | 否 | - | 订单明细ID |
| materialCode | String | ✅ | - | 物料编码 |
| materialName | String | ✅ | - | 物料名称 |
| materialSpec | String | 否 | - | 物料规格 |
| unit | String | 否 | - | 单位 |
| planQty | BigDecimal | 否 | 0 | 计划数量 |
| actualQty | BigDecimal | 否 | 0 | 实际数量 |
| receivedQty | BigDecimal | 否 | 0 | 已收数量 |
| qualifiedQty | BigDecimal | 否 | 0 | 合格数量 |
| batchNo | String | 否 | - | 批次号 |
| productionDate | LocalDate | 否 | - | 生产日期 |
| expiryDate | LocalDate | 否 | - | 有效期 |
| remark | String | 否 | - | 备注 |

**响应数据**：`Result<Long>`（返回新建明细ID）

**前端注意**：
- `noticeId`、`materialCode`、`materialName` 为必填
- 数量字段不传时默认为0，不是null
- `productionDate` 和 `expiryDate` 格式为 `yyyy-MM-dd`

---

### 1.4 更新送货明细

| 项目 | 详情 |
|------|------|
| **HTTP方法** | PUT |
| **路径** | `/v1/delivery-details/{id}` |
| **描述** | 更新指定ID的送货明细 |

**路径参数**：`id` (Long, 必填) — 明细ID

**请求体** (DeliveryDetailUpdateDTO)：

所有字段均为可选，只传需要更新的字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| orderDetailId | Long | 订单明细ID |
| materialCode | String | 物料编码 |
| materialName | String | 物料名称 |
| materialSpec | String | 物料规格 |
| unit | String | 单位 |
| planQty | BigDecimal | 计划数量 |
| actualQty | BigDecimal | 实际数量 |
| receivedQty | BigDecimal | 已收数量 |
| qualifiedQty | BigDecimal | 合格数量 |
| batchNo | String | 批次号 |
| productionDate | LocalDate | 生产日期 |
| expiryDate | LocalDate | 有效期 |
| remark | String | 备注 |

**响应数据**：`Result<Void>`

---

### 1.5 删除送货明细

| 项目 | 详情 |
|------|------|
| **HTTP方法** | DELETE |
| **路径** | `/v1/delivery-details/{id}` |
| **描述** | 删除指定ID的送货明细 |

**路径参数**：`id` (Long, 必填) — 明细ID

**响应数据**：`Result<Void>`

---

## 二、送货标签/箱管理 (DeliveryLabelController)

**基础路径**：`/v1/delivery-labels`
**权限**：所有接口需认证

### 2.1 查询ASN下的箱列表

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/delivery-labels/packages/by-notice/{noticeId}` |
| **描述** | 根据送货通知ID查询其下所有箱信息 |

**路径参数**：`noticeId` (Long, 必填) — 送货通知ID

**响应数据**：`Result<List<DeliveryPackageVO>>`

---

### 2.2 箱详情（含明细、条码）

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/delivery-labels/packages/{id}` |
| **描述** | 查询箱详情，包含箱内明细和条码信息 |

**路径参数**：`id` (Long, 必填) — 箱ID

**响应数据**：`Result<DeliveryPackageVO>`

---

### 2.3 创建箱信息

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/delivery-labels/packages` |
| **描述** | 创建箱信息 |

**请求体** (DeliveryPackageCreateDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| noticeId | Long | ✅ | 送货通知ID |
| packageNo | String | ✅ | 箱号 |
| packageType | String | 否 | 箱类型 |
| weight | BigDecimal | 否 | 重量 |
| volume | BigDecimal | 否 | 体积 |
| remark | String | 否 | 备注 |
| details | List\<DeliveryPackageDetailItemDTO\> | 否 | 箱内明细列表 |

**DeliveryPackageDetailItemDTO**（嵌套）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| materialCode | String | ✅ | 物料编码 |
| materialName | String | 否 | 物料名称 |
| quantity | BigDecimal | 否 | 数量 |
| unit | String | 否 | 单位 |
| remark | String | 否 | 备注 |

**请求示例**：
```json
{
  "noticeId": 100,
  "packageNo": "BOX-20260601-001",
  "packageType": "标准箱",
  "weight": 25.5,
  "volume": 0.5,
  "remark": "",
  "details": [
    {
      "materialCode": "MAT001",
      "materialName": "螺丝M8x20",
      "quantity": 1000,
      "unit": "个",
      "remark": ""
    }
  ]
}
```

**响应数据**：`Result<Long>`（返回新建箱ID）

---

### 2.4 生成条码

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/delivery-labels/packages/{id}/barcodes` |
| **描述** | 为指定箱生成条码 |

**路径参数**：`id` (Long, 必填) — 箱ID

**请求参数**：无

**响应数据**：`Result<List<DeliveryBarcodeVO>>`

---

### 2.5 打印标签

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/delivery-labels/packages/{id}/print` |
| **描述** | 打印标签，系统会记录打印次数 |

**路径参数**：`id` (Long, 必填) — 箱ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

## 三、收货差异管理 (ReceiptDiffController)

**基础路径**：`/v1/receipt-diffs`
**权限**：所有接口需认证

### 3.1 分页查询收货差异

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/receipt-diffs` |
| **描述** | 分页查询收货差异列表 |

**Query参数** (ReceiptDiffQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| recordId | Long | 否 | - | - | 收货记录ID |
| noticeId | Long | 否 | - | - | 送货通知ID |
| status | Integer | 否 | - | - | 差异状态 |

**响应数据**：`Result<PageResult<ReceiptDiffVO>>`

---

### 3.2 查询收货差异详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/receipt-diffs/{id}` |
| **描述** | 根据差异ID查询收货差异详情 |

**路径参数**：`id` (Long, 必填) — 差异ID

**响应数据**：`Result<ReceiptDiffVO>`

---

### 3.3 创建收货差异调整

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-diffs/adjust/{recordId}` |
| **描述** | 针对指定收货记录创建差异调整 |

**路径参数**：`recordId` (Long, 必填) — 收货记录ID

**请求体** (ReceiptAdjustDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| diffQty | BigDecimal | ✅ | 差异数量 |
| diffReason | String | ✅ | 差异原因 |
| handleMethod | Integer | ✅ | 处理方式 |
| handleRemark | String | 否 | 处理备注 |
| remark | String | 否 | 备注 |

**响应数据**：`Result<Long>`（返回差异记录ID）

---

### 3.4 审批通过收货差异调整

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-diffs/{id}/approve` |
| **描述** | 审批通过指定的收货差异调整 |

**路径参数**：`id` (Long, 必填) — 差异ID

**请求体** (ReceiptDiffApproveDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approveRemark | String | 否 | 审批备注 |

**响应数据**：`Result<Void>`

---

### 3.5 驳回收货差异调整

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-diffs/{id}/reject` |
| **描述** | 驳回指定的收货差异调整 |

**路径参数**：`id` (Long, 必填) — 差异ID

**请求体**：ReceiptDiffApproveDTO（同3.4）

**响应数据**：`Result<Void>`

---

## 四、收货记录管理 (ReceiptRecordController)

**基础路径**：`/v1/receipt-records`
**权限**：所有接口需认证

### 4.1 分页查询收货记录

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/receipt-records` |
| **描述** | 分页查询收货记录列表 |

**Query参数** (ReceiptRecordQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| noticeId | Long | 否 | - | - | 送货通知ID |
| deliveryId | Long | 否 | - | - | 送货明细ID |
| materialCode | String | 否 | - | - | 物料编码 |
| receiptStatus | Integer | 否 | - | - | 收货状态 |
| startTime | LocalDateTime | 否 | - | - | 开始时间 |
| endTime | LocalDateTime | 否 | - | - | 结束时间 |
| supplierId | Long | 否 | - | - | 供应商ID |

**前端注意**：
- `startTime` 和 `endTime` 格式为 `yyyy-MM-ddTHH:mm:ss`
- 如只传日期部分，使用 `2026-06-01T00:00:00`

**响应数据**：`Result<PageResult<ReceiptRecordVO>>`

---

### 4.2 查询收货记录详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/receipt-records/{id}` |
| **描述** | 根据收货记录ID查询详情 |

**路径参数**：`id` (Long, 必填) — 收货记录ID

**响应数据**：`Result<ReceiptRecordVO>`

---

### 4.3 创建收货记录

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-records` |
| **描述** | 创建一条收货记录 |

**请求体** (ReceiptCreateDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deliveryId | Long | ✅ | 送货明细ID |
| noticeId | Long | 否 | 送货通知ID |
| materialCode | String | ✅ | 物料编码 |
| materialName | String | ✅ | 物料名称 |
| planQty | BigDecimal | 否(默认0) | 计划数量 |
| receiptQty | BigDecimal | 否(默认0) | 收货数量 |
| rejectQty | BigDecimal | 否(默认0) | 拒收数量 |
| receiptTime | LocalDateTime | 否 | 收货时间 |
| warehouseId | Long | 否 | 仓库ID |
| warehouseName | String | 否 | 仓库名称 |
| location | String | 否 | 库位 |
| remark | String | 否 | 备注 |

**响应数据**：`Result<Long>`（返回新建收货记录ID）

---

### 4.4 确认收货

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-records/{id}/confirm` |
| **描述** | 确认收货 |

**路径参数**：`id` (Long, 必填) — 收货记录ID

**请求体** (ReceiptConfirmDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiptQty | BigDecimal | 否(默认0) | 收货数量 |
| rejectQty | BigDecimal | 否(默认0) | 拒收数量 |
| warehouseId | Long | 否 | 仓库ID |
| warehouseName | String | 否 | 仓库名称 |
| location | String | 否 | 库位 |
| rejectReason | String | 否 | 拒收原因 |
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 4.5 驳回收货

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-records/{id}/reject` |
| **描述** | 驳回收货记录 |

**路径参数**：`id` (Long, 必填) — 收货记录ID

**请求体**：ReceiptConfirmDTO（同4.4）

**响应数据**：`Result<Void>`

---

### 4.6 扫码查询

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/receipt-records/scan` |
| **描述** | 通过扫描条码查询对应的收货记录 |

**Query参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| barcode | String | ✅ | 条码 |

**请求示例**：
```
GET /v1/receipt-records/scan?barcode=ASN20260601001
```

**响应数据**：`Result<ReceiptRecordVO>`

---

### 4.7 创建收货差异调整

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/receipt-records/{id}/adjust` |
| **描述** | 针对指定收货记录创建差异调整 |

**路径参数**：`id` (Long, 必填) — 收货记录ID

**请求体**：ReceiptAdjustDTO（同3.3）

**响应数据**：`Result<Long>`（返回差异记录ID）

---

## 五、VMI库存管理 (VmiInventoryController)

**基础路径**：`/v1/vmi-inventories`
**权限**：所有接口需认证

### 5.1 分页查询VMI库存

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/vmi-inventories` |
| **描述** | 分页查询VMI库存列表 |

**Query参数** (VmiInventoryQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| supplierId | Long | 否 | - | - | 供应商ID |
| materialCode | String | 否 | - | - | 物料编码 |
| inventoryStatus | Integer | 否 | - | - | 库存状态 |

**响应数据**：`Result<PageResult<VmiInventoryVO>>`

---

### 5.2 查询VMI库存详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/vmi-inventories/{id}` |
| **描述** | 根据库存ID查询VMI库存详情 |

**路径参数**：`id` (Long, 必填) — 库存ID

**响应数据**：`Result<VmiInventoryVO>`

---

### 5.3 同步VMI库存

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/vmi-inventories/sync` |
| **描述** | 同步VMI库存数据 |

**请求体** (VmiInventorySyncDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| supplierId | Long | ✅ | 供应商ID |
| materialCode | String | ✅ | 物料编码 |
| warehouseId | Long | 否 | 仓库ID |
| warehouseName | String | 否 | 仓库名称 |
| onhandQty | BigDecimal | ✅ | 现有库存 |
| availableQty | BigDecimal | ✅ | 可用库存 |
| safetyQty | BigDecimal | 否 | 安全库存 |
| maxQty | BigDecimal | 否 | 最大库存 |

**请求示例**：
```json
{
  "supplierId": 100,
  "materialCode": "MAT001",
  "warehouseId": 1,
  "warehouseName": "主仓库",
  "onhandQty": 5000,
  "availableQty": 3500,
  "safetyQty": 1000,
  "maxQty": 10000
}
```

**响应数据**：`Result<Void>`

---

## 六、物流模块响应VO字段汇总

### DeliveryDetailVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细ID |
| noticeId | Long | 送货通知ID |
| orderDetailId | Long | 订单明细ID |
| materialCode | String | 物料编码 |
| materialName | String | 物料名称 |
| materialSpec | String | 物料规格 |
| unit | String | 单位 |
| planQty | BigDecimal | 计划数量 |
| actualQty | BigDecimal | 实际数量 |
| receivedQty | BigDecimal | 已收数量 |
| qualifiedQty | BigDecimal | 合格数量 |
| batchNo | String | 批次号 |
| productionDate | LocalDate | 生产日期 |
| expiryDate | LocalDate | 有效期 |
| remark | String | 备注 |

### DeliveryPackageVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 箱ID |
| noticeId | Long | 送货通知ID |
| packageNo | String | 箱号 |
| packageType | String | 箱类型 |
| weight | BigDecimal | 重量 |
| volume | BigDecimal | 体积 |
| remark | String | 备注 |
| details | List\<DeliveryPackageDetailVO\> | 箱内明细列表 |
| barcodes | List\<DeliveryBarcodeVO\> | 条码列表 |

### DeliveryBarcodeVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 条码ID |
| noticeId | Long | 送货通知ID |
| packageId | Long | 箱ID |
| barcode | String | 条码值 |
| barcodeType | String | 条码类型 |
| printCount | Integer | 打印次数 |
| remark | String | 备注 |

### ReceiptDiffVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 差异ID |
| recordId | Long | 收货记录ID |
| noticeId | Long | 送货通知ID |
| materialCode | String | 物料编码 |
| materialName | String | 物料名称 |
| planQty | BigDecimal | 计划数量 |
| receiptQty | BigDecimal | 收货数量 |
| diffQty | BigDecimal | 差异数量 |
| diffReason | String | 差异原因 |
| handleMethod | Integer | 处理方式 |
| handleRemark | String | 处理备注 |
| status | Integer | 状态 |
| remark | String | 备注 |

### ReceiptRecordVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 收货记录ID |
| deliveryId | Long | 送货明细ID |
| noticeId | Long | 送货通知ID |
| materialCode | String | 物料编码 |
| materialName | String | 物料名称 |
| planQty | BigDecimal | 计划数量 |
| receiptQty | BigDecimal | 收货数量 |
| rejectQty | BigDecimal | 拒收数量 |
| receiptTime | LocalDateTime | 收货时间 |
| receiverName | String | 收货人 |
| warehouseName | String | 仓库名称 |
| location | String | 库位 |
| receiptStatus | Integer | 收货状态 |
| rejectReason | String | 拒收原因 |
| remark | String | 备注 |

### VmiInventoryVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 库存ID |
| supplierId | Long | 供应商ID |
| materialCode | String | 物料编码 |
| warehouseId | Long | 仓库ID |
| warehouseName | String | 仓库名称 |
| onhandQty | BigDecimal | 现有库存 |
| availableQty | BigDecimal | 可用库存 |
| safetyQty | BigDecimal | 安全库存 |
| maxQty | BigDecimal | 最大库存 |
| inventoryStatus | Integer | 库存状态 |
| lastSyncTime | LocalDateTime | 最后同步时间 |

---

## 七、物流模块API速查表

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 1 | GET | `/v1/delivery-details` | noticeId(Query) | Result\<List\<DeliveryDetailVO\>\> | 查询送货明细列表 |
| 2 | GET | `/v1/delivery-details/{id}` | - | Result\<DeliveryDetailVO\> | 查询送货明细详情 |
| 3 | POST | `/v1/delivery-details` | DeliveryDetailCreateDTO | Result\<Long\> | 创建送货明细 |
| 4 | PUT | `/v1/delivery-details/{id}` | DeliveryDetailUpdateDTO | Result\<Void\> | 更新送货明细 |
| 5 | DELETE | `/v1/delivery-details/{id}` | - | Result\<Void\> | 删除送货明细 |
| 6 | GET | `/v1/delivery-labels/packages/by-notice/{noticeId}` | - | Result\<List\<DeliveryPackageVO\>\> | 查询ASN下的箱列表 |
| 7 | GET | `/v1/delivery-labels/packages/{id}` | - | Result\<DeliveryPackageVO\> | 箱详情 |
| 8 | POST | `/v1/delivery-labels/packages` | DeliveryPackageCreateDTO | Result\<Long\> | 创建箱信息 |
| 9 | POST | `/v1/delivery-labels/packages/{id}/barcodes` | - | Result\<List\<DeliveryBarcodeVO\>\> | 生成条码 |
| 10 | POST | `/v1/delivery-labels/packages/{id}/print` | - | Result\<Void\> | 打印标签 |
| 11 | GET | `/v1/receipt-diffs` | ReceiptDiffQuery | Result\<PageResult\<ReceiptDiffVO\>\> | 分页查询收货差异 |
| 12 | GET | `/v1/receipt-diffs/{id}` | - | Result\<ReceiptDiffVO\> | 查询收货差异详情 |
| 13 | POST | `/v1/receipt-diffs/adjust/{recordId}` | ReceiptAdjustDTO | Result\<Long\> | 创建收货差异调整 |
| 14 | POST | `/v1/receipt-diffs/{id}/approve` | ReceiptDiffApproveDTO | Result\<Void\> | 审批通过收货差异 |
| 15 | POST | `/v1/receipt-diffs/{id}/reject` | ReceiptDiffApproveDTO | Result\<Void\> | 驳回收货差异 |
| 16 | GET | `/v1/receipt-records` | ReceiptRecordQuery | Result\<PageResult\<ReceiptRecordVO\>\> | 分页查询收货记录 |
| 17 | GET | `/v1/receipt-records/{id}` | - | Result\<ReceiptRecordVO\> | 查询收货记录详情 |
| 18 | POST | `/v1/receipt-records` | ReceiptCreateDTO | Result\<Long\> | 创建收货记录 |
| 19 | POST | `/v1/receipt-records/{id}/confirm` | ReceiptConfirmDTO | Result\<Void\> | 确认收货 |
| 20 | POST | `/v1/receipt-records/{id}/reject` | ReceiptConfirmDTO | Result\<Void\> | 驳回收货 |
| 21 | GET | `/v1/receipt-records/scan` | barcode(Query) | Result\<ReceiptRecordVO\> | 扫码查询 |
| 22 | POST | `/v1/receipt-records/{id}/adjust` | ReceiptAdjustDTO | Result\<Long\> | 创建收货差异调整 |
| 23 | GET | `/v1/vmi-inventories` | VmiInventoryQuery | Result\<PageResult\<VmiInventoryVO\>\> | 分页查询VMI库存 |
| 24 | GET | `/v1/vmi-inventories/{id}` | - | Result\<VmiInventoryVO\> | 查询VMI库存详情 |
| 25 | POST | `/v1/vmi-inventories/sync` | VmiInventorySyncDTO | Result\<Void\> | 同步VMI库存 |

---

# 第4部分：质量模块

## 一、8D整改报告管理 (EightDReportController)

**基础路径**：`/v1/eight-d-reports`
**权限**：所有接口需认证

### 1.1 分页查询8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/eight-d-reports` |
| **描述** | 分页查询8D整改报告 |

**Query参数** (EightDReportQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| keyword | String | 否 | - | - | 关键词搜索 |
| ncrId | Long | 否 | - | - | 不符合报告ID |
| reportStatus | Integer | 否 | - | - | 报告状态 |
| supplierId | Long | 否 | - | - | 供应商ID |
| startDate | LocalDate | 否 | - | - | 起始日期 |
| endDate | LocalDate | 否 | - | - | 截止日期 |

**响应数据**：`Result<PageResult<EightDReportVO>>`

---

### 1.2 查询8D整改报告详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/eight-d-reports/{id}` |
| **描述** | 查询8D整改报告详情 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**响应数据**：`Result<EightDReportVO>`

---

### 1.3 创建8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports` |
| **描述** | 创建8D整改报告 |

**请求体** (EightDReportCreateDTO)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| ncrId | Long | ✅ | @NotNull | NCR ID |
| supplierId | Long | ✅ | @NotNull | 供应商ID |
| d1Team | String | 否 | - | D1-团队 |
| d2Problem | String | 否 | - | D2-问题描述 |
| d3Containment | String | 否 | - | D3-临时遏制措施 |
| d4RootCause | String | 否 | - | D4-根本原因分析 |
| d5CorrectiveAction | String | 否 | - | D5-纠正措施 |
| d6ValidateAction | String | 否 | - | D6-验证措施 |
| d7PreventAction | String | 否 | - | D7-预防措施 |
| d8CloseSummary | String | 否 | - | D8-结案总结 |
| dueDate | LocalDate | 否 | - | 截止日期 |
| currentStep | Integer | 否 | - | 当前阶段 |

**请求示例**：
```json
{
  "ncrId": 50,
  "supplierId": 100,
  "d1Team": "张三、李四、王五",
  "d2Problem": "来料检验发现尺寸超差",
  "d3Containment": "暂停使用该批次物料，隔离库存",
  "dueDate": "2026-06-30",
  "currentStep": 1
}
```

**响应数据**：`Result<Long>`（返回新建报告ID）

**前端注意**：
- `ncrId` 和 `supplierId` 为必填
- D1-D8步骤内容可以分阶段填写，不必一次全部填完
- `currentStep` 表示当前进度（1-8对应D1-D8）

---

### 1.4 更新8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | PUT |
| **路径** | `/v1/eight-d-reports/{id}` |
| **描述** | 更新8D整改报告 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体** (EightDReportUpdateDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| d1Team | String | 否 | D1-团队 |
| d2Problem | String | 否 | D2-问题描述 |
| d3Containment | String | 否 | D3-临时遏制措施 |
| d4RootCause | String | 否 | D4-根本原因分析 |
| d5CorrectiveAction | String | 否 | D5-纠正措施 |
| d6ValidateAction | String | 否 | D6-验证措施 |
| d7PreventAction | String | 否 | D7-预防措施 |
| d8CloseSummary | String | 否 | D8-结案总结 |
| dueDate | LocalDate | 否 | 截止日期 |
| currentStep | Integer | 否 | 当前阶段 |
| stepDueDate | LocalDate | 否 | 阶段截止日期 |

**响应数据**：`Result<Void>`

---

### 1.5 提交8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/submit` |
| **描述** | 提交8D整改报告 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体** (EightDActionDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |
| currentStep | Integer | 否 | 当前阶段 |
| stepDueDate | LocalDate | 否 | 阶段截止日期 |
| stepContent | String | 否 | 阶段内容 |

**响应数据**：`Result<Void>`

---

### 1.6 审核通过8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/audit` |
| **描述** | 审核8D整改报告 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体**：EightDActionDTO（同1.5）

**响应数据**：`Result<Void>`

---

### 1.7 退回8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/reject` |
| **描述** | 退回8D整改报告 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体**：EightDActionDTO（同1.5）

**响应数据**：`Result<Void>`

---

### 1.8 关闭8D整改报告

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/close` |
| **描述** | 关闭8D整改报告 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体**：EightDActionDTO（同1.5）

**响应数据**：`Result<Void>`

---

### 1.9 提交8D阶段(D1-D8)

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/step-submit` |
| **描述** | 提交8D阶段(D1-D8) |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体**：EightDActionDTO（同1.5）

**前端注意**：
- `stepContent` 字段填写当前阶段的具体内容
- `currentStep` 标识当前提交的是哪个阶段

**响应数据**：`Result<Void>`

---

### 1.10 审核通过8D阶段(推进到下一阶段)

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/step-approve` |
| **描述** | 审核通过8D阶段，推进到下一阶段 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求体**：EightDActionDTO（同1.5）

**响应数据**：`Result<Void>`

---

### 1.11 上传8D报告附件

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/eight-d-reports/{id}/upload-attachment` |
| **Content-Type** | multipart/form-data |
| **描述** | 上传8D报告附件 |

**路径参数**：`id` (Long, 必填) — 8D报告ID

**请求参数** (form-data)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | ✅ | 上传的附件文件 |

**前端调用示例**：
```javascript
const formData = new FormData();
formData.append('file', fileObject);

axios.post(`/v1/eight-d-reports/${id}/upload-attachment`, formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
```

**响应数据**：`Result<Long>`（返回附件ID）

**前端注意**：
- 此接口使用 `multipart/form-data`，不是 `application/json`
- 不要设置 `Content-Type: application/json`

---

## 二、质量申诉管理 (QualityAppealController)

**基础路径**：`/v1/quality-appeals`
**权限**：所有接口需认证

### 2.1 分页查询质量申诉

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/quality-appeals` |
| **描述** | 分页查询质量申诉 |

**Query参数** (QualityAppealQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| keyword | String | 否 | - | - | 关键词搜索 |
| ncrId | Long | 否 | - | - | 不符合报告ID |
| appealStatus | Integer | 否 | - | - | 申诉状态 |
| supplierId | Long | 否 | - | - | 供应商ID |
| startDate | LocalDate | 否 | - | - | 起始日期 |
| endDate | LocalDate | 否 | - | - | 截止日期 |

**响应数据**：`Result<PageResult<QualityAppealVO>>`

---

### 2.2 查询质量申诉详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/quality-appeals/{id}` |
| **描述** | 查询质量申诉详情 |

**路径参数**：`id` (Long, 必填) — 申诉ID

**响应数据**：`Result<QualityAppealVO>`

---

### 2.3 创建质量申诉

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/quality-appeals` |
| **描述** | 创建质量申诉 |

**请求体** (QualityAppealCreateDTO)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| ncrId | Long | 否 | - | 不符合报告ID |
| inspectionId | Long | 否 | - | 检验ID |
| supplierId | Long | ✅ | @NotNull | 供应商ID |
| appealReason | String | ✅ | @NotBlank | 申诉原因 |

**请求示例**：
```json
{
  "ncrId": 50,
  "inspectionId": 30,
  "supplierId": 100,
  "appealReason": "该批次物料经第三方复检合格，认为判定有误"
}
```

**响应数据**：`Result<Long>`（返回新建申诉ID）

**前端注意**：
- `supplierId` 和 `appealReason` 为必填
- `ncrId` 和 `inspectionId` 至少传一个，用于关联原始质量记录

---

### 2.4 提交质量申诉

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/quality-appeals/{id}/submit` |
| **描述** | 提交质量申诉 |

**路径参数**：`id` (Long, 必填) — 申诉ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

### 2.5 通过质量申诉

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/quality-appeals/{id}/approve` |
| **描述** | 通过质量申诉 |

**路径参数**：`id` (Long, 必填) — 申诉ID

**请求体** (QualityAppealAuditDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| auditRemark | String | ✅ | @NotBlank | 审核意见 |

**响应数据**：`Result<Void>`

---

### 2.6 驳回质量申诉

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/quality-appeals/{id}/reject` |
| **描述** | 驳回质量申诉 |

**路径参数**：`id` (Long, 必填) — 申诉ID

**请求体**：QualityAppealAuditDTO（同2.5，`auditRemark` 必填）

**响应数据**：`Result<Void>`

---

## 三、质量模块响应VO字段汇总

### EightDReportVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 报告ID |
| reportNo | String | 报告编号 |
| ncrId | Long | 不符合报告ID |
| supplierId | Long | 供应商ID |
| d1Team | String | D1-团队 |
| d2Problem | String | D2-问题描述 |
| d3Containment | String | D3-临时遏制措施 |
| d4RootCause | String | D4-根本原因分析 |
| d5CorrectiveAction | String | D5-纠正措施 |
| d6ValidateAction | String | D6-验证措施 |
| d7PreventAction | String | D7-预防措施 |
| d8CloseSummary | String | D8-结案总结 |
| dueDate | LocalDate | 截止日期 |
| currentStep | Integer | 当前阶段 |
| stepDueDate | LocalDate | 阶段截止日期 |
| reportStatus | Integer | 报告状态 |
| submitTime | LocalDateTime | 提交时间 |
| auditTime | LocalDateTime | 审核时间 |
| closeTime | LocalDateTime | 关闭时间 |
| createTime | LocalDateTime | 创建时间 |

### QualityAppealVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 申诉ID |
| appealNo | String | 申诉编号 |
| ncrId | Long | 不符合报告ID |
| inspectionId | Long | 检验ID |
| supplierId | Long | 供应商ID |
| appealReason | String | 申诉原因 |
| appealStatus | Integer | 申诉状态 |
| submitTime | LocalDateTime | 提交时间 |
| auditBy | Long | 审核人ID |
| auditTime | LocalDateTime | 审核时间 |
| auditRemark | String | 审核意见 |
| createTime | LocalDateTime | 创建时间 |

---

## 四、质量模块API速查表

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 1 | GET | `/v1/eight-d-reports` | EightDReportQuery | Result\<PageResult\<EightDReportVO\>\> | 分页查询8D整改报告 |
| 2 | GET | `/v1/eight-d-reports/{id}` | - | Result\<EightDReportVO\> | 查询8D整改报告详情 |
| 3 | POST | `/v1/eight-d-reports` | EightDReportCreateDTO | Result\<Long\> | 创建8D整改报告 |
| 4 | PUT | `/v1/eight-d-reports/{id}` | EightDReportUpdateDTO | Result\<Void\> | 更新8D整改报告 |
| 5 | POST | `/v1/eight-d-reports/{id}/submit` | EightDActionDTO | Result\<Void\> | 提交8D整改报告 |
| 6 | POST | `/v1/eight-d-reports/{id}/audit` | EightDActionDTO | Result\<Void\> | 审核通过8D整改报告 |
| 7 | POST | `/v1/eight-d-reports/{id}/reject` | EightDActionDTO | Result\<Void\> | 退回8D整改报告 |
| 8 | POST | `/v1/eight-d-reports/{id}/close` | EightDActionDTO | Result\<Void\> | 关闭8D整改报告 |
| 9 | POST | `/v1/eight-d-reports/{id}/step-submit` | EightDActionDTO | Result\<Void\> | 提交8D阶段(D1-D8) |
| 10 | POST | `/v1/eight-d-reports/{id}/step-approve` | EightDActionDTO | Result\<Void\> | 审核通过8D阶段 |
| 11 | POST | `/v1/eight-d-reports/{id}/upload-attachment` | MultipartFile | Result\<Long\> | 上传8D报告附件 |
| 12 | GET | `/v1/quality-appeals` | QualityAppealQuery | Result\<PageResult\<QualityAppealVO\>\> | 分页查询质量申诉 |
| 13 | GET | `/v1/quality-appeals/{id}` | - | Result\<QualityAppealVO\> | 查询质量申诉详情 |
| 14 | POST | `/v1/quality-appeals` | QualityAppealCreateDTO | Result\<Long\> | 创建质量申诉 |
| 15 | POST | `/v1/quality-appeals/{id}/submit` | - | Result\<Void\> | 提交质量申诉 |
| 16 | POST | `/v1/quality-appeals/{id}/approve` | QualityAppealAuditDTO | Result\<Void\> | 通过质量申诉 |
| 17 | POST | `/v1/quality-appeals/{id}/reject` | QualityAppealAuditDTO | Result\<Void\> | 驳回质量申诉 |

---

# 第5部分：结算 + 门户 + 消息模块

## 一、扣款管理 (DeductionController)

**基础路径**：`/v1/deductions`
**权限**：所有接口需认证

### 1.1 分页查询扣款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/deductions` |
| **描述** | 分页查询扣款单列表 |

**Query参数** (DeductionQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| supplierId | Long | 否 | - | - | 供应商ID |
| deductionStatus | Integer | 否 | - | - | 扣款状态 |
| keyword | String | 否 | - | - | 关键字搜索 |

**响应数据**：`Result<PageResult<DeductionVO>>`

---

### 1.2 查询扣款单详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/deductions/{id}` |
| **描述** | 查询扣款单详情 |

**路径参数**：`id` (Long, 必填) — 扣款单ID

**响应数据**：`Result<DeductionVO>`

---

### 1.3 新增扣款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/deductions` |
| **描述** | 新增扣款单 |

**请求体** (DeductionCreateDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| deductionNo | String | ✅ | @NotBlank | 扣款单号 |
| supplierId | Long | ✅ | @NotNull | 供应商ID |
| sourceType | String | ✅ | @NotBlank | 来源类型 |
| sourceId | Long | 否 | - | 来源ID |
| deductionType | Integer | ✅ | @NotNull | 扣款类型 |
| deductionAmount | BigDecimal | ✅ | @NotNull | 扣款金额 |
| deductionReason | String | ✅ | @NotBlank | 扣款原因 |
| reconId | Long | 否 | - | 对账单ID |

**请求示例**：
```json
{
  "deductionNo": "DK20260601001",
  "supplierId": 100,
  "sourceType": "QUALITY",
  "sourceId": 50,
  "deductionType": 1,
  "deductionAmount": 5000.00,
  "deductionReason": "来料质量不合格扣款",
  "reconId": 20
}
```

**响应数据**：`Result<Long>`（返回新建扣款单ID）

**前端注意**：
- `deductionAmount` 必须传数字类型，不能传字符串
- `sourceType` 为枚举字符串，如 "QUALITY"、"DELIVERY" 等

---

### 1.4 提交扣款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/deductions/{id}/submit` |
| **描述** | 提交扣款单 |

**路径参数**：`id` (Long, 必填) — 扣款单ID

**请求体** (DeductionActionDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 1.5 确认扣款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/deductions/{id}/confirm` |
| **描述** | 确认扣款单 |

**路径参数**：`id` (Long, 必填) — 扣款单ID

**请求体**：DeductionActionDTO（同1.4）

**响应数据**：`Result<Void>`

---

### 1.6 扣款单异议

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/deductions/{id}/dispute` |
| **描述** | 供应商对扣款单提出异议 |

**路径参数**：`id` (Long, 必填) — 扣款单ID

**请求体**：DeductionActionDTO（同1.4）

**响应数据**：`Result<Void>`

---

### 1.7 扣款单入账

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/deductions/{id}/book` |
| **描述** | 扣款单入账 |

**路径参数**：`id` (Long, 必填) — 扣款单ID

**请求体**：DeductionActionDTO（同1.4）

**响应数据**：`Result<Void>`

---

## 二、发票管理 (InvoiceController)

**基础路径**：`/v1/invoices`
**权限**：所有接口需认证

### 2.1 分页查询发票

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/invoices` |
| **描述** | 分页查询发票列表 |

**Query参数** (InvoiceQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| supplierId | Long | 否 | - | - | 供应商ID |
| invoiceStatus | Integer | 否 | - | - | 发票状态 |
| keyword | String | 否 | - | - | 关键字搜索 |

**响应数据**：`Result<PageResult<InvoiceVO>>`

---

### 2.2 查询发票详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/invoices/{id}` |
| **描述** | 查询发票详情 |

**路径参数**：`id` (Long, 必填) — 发票ID

**响应数据**：`Result<InvoiceVO>`

---

### 2.3 新增发票

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/invoices` |
| **描述** | 新增发票 |

**请求体** (InvoiceCreateDTO, @Valid)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| invoiceNo | String | ✅ | - | @NotBlank | 发票号码 |
| invoiceCode | String | 否 | - | - | 发票编码 |
| invoiceType | Integer | 否 | 1 | - | 发票类型 |
| reconId | Long | 否 | - | - | 对账单ID |
| supplierId | Long | ✅ | - | @NotNull | 供应商ID |
| supplierName | String | 否 | - | - | 供应商名称 |
| taxNumber | String | 否 | - | - | 税号 |
| invoiceAmount | BigDecimal | ✅ | - | @NotNull | 发票金额 |
| taxAmount | BigDecimal | 否 | - | - | 税额 |
| taxRate | BigDecimal | 否 | - | - | 税率 |
| invoiceDate | LocalDate | 否 | - | - | 开票日期 |
| remark | String | 否 | - | - | 备注 |

**请求示例**：
```json
{
  "invoiceNo": "FP20260601001",
  "invoiceCode": "1234567890",
  "invoiceType": 1,
  "reconId": 20,
  "supplierId": 100,
  "supplierName": "XX供应商有限公司",
  "taxNumber": "91310000MA1FL8XX3K",
  "invoiceAmount": 50000.00,
  "taxAmount": 6500.00,
  "taxRate": 0.13,
  "invoiceDate": "2026-06-01",
  "remark": ""
}
```

**响应数据**：`Result<Long>`（返回新建发票ID）

**前端注意**：
- `invoiceNo`、`supplierId`、`invoiceAmount` 为必填
- `invoiceType` 不传时默认为1（增值税专用发票）
- 金额和税率字段传数字类型

---

### 2.4 上传发票

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/invoices/{id}/upload` |
| **描述** | 上传发票文件 |

**路径参数**：`id` (Long, 必填) — 发票ID

**请求体** (InvoiceUploadDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| fileId | Long | ✅ | @NotNull | 附件ID（先上传文件获取ID） |

**响应数据**：`Result<Void>`

**前端注意**：
- 此接口不是直接上传文件，而是关联已上传的文件ID
- 需先调用文件上传接口获取fileId，再调用此接口关联

---

### 2.5 发票验真

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/invoices/{id}/verify` |
| **描述** | 发票验真 |

**路径参数**：`id` (Long, 必填) — 发票ID

**请求体** (InvoiceActionDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 2.6 发票认证

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/invoices/{id}/certify` |
| **描述** | 发票认证 |

**路径参数**：`id` (Long, 必填) — 发票ID

**请求体**：InvoiceActionDTO（同2.5）

**响应数据**：`Result<Void>`

---

### 2.7 发票作废

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/invoices/{id}/void` |
| **描述** | 发票作废 |

**路径参数**：`id` (Long, 必填) — 发票ID

**请求体**：InvoiceActionDTO（同2.5）

**响应数据**：`Result<Void>`

---

## 三、付款管理 (PaymentController)

**基础路径**：`/v1/payments`
**权限**：所有接口需认证

### 3.1 分页查询付款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/payments` |
| **描述** | 分页查询付款单列表 |

**Query参数** (PaymentQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| supplierId | Long | 否 | - | - | 供应商ID |
| reconId | Long | 否 | - | - | 对账单ID |
| invoiceId | Long | 否 | - | - | 发票ID |
| paymentStatus | Integer | 否 | - | - | 付款状态 |
| approveStatus | Integer | 否 | - | - | 审批状态 |
| scheduleDateStart | LocalDate | 否 | - | - | 排期开始日期 |
| scheduleDateEnd | LocalDate | 否 | - | - | 排期结束日期 |
| keyword | String | 否 | - | - | 关键字搜索 |

**响应数据**：`Result<PageResult<PaymentVO>>`

---

### 3.2 查询付款单详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/payments/{id}` |
| **描述** | 查询付款单详情 |

**路径参数**：`id` (Long, 必填) — 付款单ID

**响应数据**：`Result<PaymentVO>`

---

### 3.3 新增付款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/payments` |
| **描述** | 新增付款单 |

**请求体** (PaymentCreateDTO, @Valid)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| paymentNo | String | ✅ | - | @NotBlank | 付款单号 |
| invoiceId | Long | 否 | - | - | 发票ID |
| invoiceNo | String | 否 | - | - | 发票号码 |
| reconId | Long | 否 | - | - | 对账单ID |
| supplierId | Long | ✅ | - | @NotNull | 供应商ID |
| supplierName | String | 否 | - | - | 供应商名称 |
| paymentAmount | BigDecimal | ✅ | - | @NotNull | 付款金额 |
| paymentMethod | Integer | 否 | 1 | - | 付款方式 |
| paymentAccount | String | 否 | - | - | 付款账号 |
| paymentBank | String | 否 | - | - | 付款银行 |
| receiveAccount | String | 否 | - | - | 收款账号 |
| receiveBank | String | 否 | - | - | 收款银行 |
| scheduleDate | LocalDate | 否 | - | - | 计划付款日期 |
| paymentTerms | String | 否 | - | - | 付款条件 |
| remark | String | 否 | - | - | 备注 |

**请求示例**：
```json
{
  "paymentNo": "FK20260601001",
  "invoiceId": 30,
  "invoiceNo": "FP20260601001",
  "reconId": 20,
  "supplierId": 100,
  "supplierName": "XX供应商有限公司",
  "paymentAmount": 56500.00,
  "paymentMethod": 1,
  "paymentAccount": "6225880123456789",
  "paymentBank": "中国工商银行",
  "receiveAccount": "6225880987654321",
  "receiveBank": "中国建设银行",
  "scheduleDate": "2026-06-15",
  "paymentTerms": "月结30天",
  "remark": ""
}
```

**响应数据**：`Result<Long>`（返回新建付款单ID）

**前端注意**：
- `paymentNo`、`supplierId`、`paymentAmount` 为必填
- `paymentMethod` 不传时默认为1
- 金额字段传数字类型

---

### 3.4 提交审批

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/payments/{id}/submit-approval` |
| **描述** | 提交付款审批 |

**路径参数**：`id` (Long, 必填) — 付款单ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

### 3.5 付款排期

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/payments/{id}/schedule` |
| **描述** | 付款排期 |

**路径参数**：`id` (Long, 必填) — 付款单ID

**请求体** (PaymentScheduleDTO, @Valid)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| scheduleDate | LocalDate | ✅ | @NotNull | 计划付款日期 |
| paymentTerms | String | 否 | - | 付款条件 |

**响应数据**：`Result<Void>`

**前端注意**：
- `scheduleDate` 为必填，格式 `yyyy-MM-dd`

---

### 3.6 付款

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/payments/{id}/pay` |
| **描述** | 确认付款 |

**路径参数**：`id` (Long, 必填) — 付款单ID

**请求体** (PaymentActionDTO)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 备注 |

**响应数据**：`Result<Void>`

---

### 3.7 拒绝付款

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/payments/{id}/reject` |
| **描述** | 拒绝付款 |

**路径参数**：`id` (Long, 必填) — 付款单ID

**请求体**：PaymentActionDTO（同3.6）

**响应数据**：`Result<Void>`

---

### 3.8 取消付款单

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/payments/{id}/cancel` |
| **描述** | 取消付款单 |

**路径参数**：`id` (Long, 必填) — 付款单ID

**请求体**：PaymentActionDTO（同3.6）

**响应数据**：`Result<Void>`

---

## 四、仪表盘 (DashboardController)

**基础路径**：`/v1/dashboard`
**权限**：所有接口需认证

### 4.1 获取指标概览

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/dashboard/metrics` |
| **描述** | 获取仪表盘指标概览数据 |

**请求参数**：无

**响应数据**：`Result<List<DashboardMetricVO>>`

**DashboardMetricVO**：

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 指标名称 |
| value | Long | 指标值 |
| unit | String | 单位 |

---

### 4.2 获取趋势数据

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/dashboard/trends` |
| **描述** | 获取仪表盘趋势数据 |

**请求参数**：无

**响应数据**：`Result<List<DashboardTrendVO>>`

**DashboardTrendVO**：

| 字段 | 类型 | 说明 |
|------|------|------|
| period | String | 时间段 |
| orderCount | Long | 订单数量 |
| deliveryCount | Long | 交货数量 |
| qualityIssueCount | Long | 质量问题数量 |
| reconciliationCount | Long | 对账数量 |

---

### 4.3 获取风险预警

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/dashboard/risks` |
| **描述** | 获取风险预警数据 |

**请求参数**：无

**响应数据**：`Result<List<DashboardRiskVO>>`

**DashboardRiskVO**：

| 字段 | 类型 | 说明 |
|------|------|------|
| riskType | String | 风险类型 |
| title | String | 风险标题 |
| count | Long | 风险数量 |
| level | String | 风险等级 |

---

### 4.4 获取供应商绩效

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/dashboard/supplier-performance` |
| **描述** | 获取供应商绩效数据 |

**请求参数**：无

**响应数据**：`Result<List<SupplierPerformanceVO>>`

**SupplierPerformanceVO**：

| 字段 | 类型 | 说明 |
|------|------|------|
| supplierId | Long | 供应商ID |
| supplierName | String | 供应商名称 |
| deliveryRate | BigDecimal | 交货率 |
| qualityRate | BigDecimal | 质量合格率 |
| responseRate | BigDecimal | 响应率 |
| score | Integer | 综合评分 |

---

## 五、待办事项管理 (PortalTodoController)

**基础路径**：`/v1/todos`
**权限**：所有接口需认证

### 5.1 分页查询待办

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/todos` |
| **描述** | 分页查询待办事项列表 |

**Query参数** (PortalTodoQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| todoType | String | 否 | - | - | 待办类型 |
| businessType | String | 否 | - | - | 业务类型 |
| todoStatus | Integer | 否 | - | - | 待办状态 |

**响应数据**：`Result<PageResult<PortalTodoVO>>`

---

### 5.2 查询未读待办数

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/todos/unread-count` |
| **描述** | 查询未读待办数量 |

**请求参数**：无

**响应数据**：`Result<Long>`

---

### 5.3 完成待办

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/todos/{id}/finish` |
| **描述** | 标记待办为已完成 |

**路径参数**：`id` (Long, 必填) — 待办ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

### 5.4 忽略待办

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/todos/{id}/ignore` |
| **描述** | 忽略待办事项 |

**路径参数**：`id` (Long, 必填) — 待办ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

### 5.5 新增待办

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/todos` |
| **描述** | 新增待办事项 |

**请求体** (PortalTodoCreateDTO)：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| supplierId | Long | 否 | - | 供应商ID |
| todoType | String | ✅ | @NotBlank | 待办类型 |
| businessType | String | ✅ | @NotBlank | 业务类型 |
| businessId | Long | ✅ | @NotNull | 业务ID |
| businessNo | String | 否 | - | 业务编号 |
| title | String | ✅ | @NotBlank | 标题 |
| dueTime | LocalDateTime | 否 | - | 截止时间 |

**响应数据**：`Result<Long>`（返回新建待办ID）

**前端注意**：
- `todoType`、`businessType`、`businessId`、`title` 为必填
- `dueTime` 格式为 `yyyy-MM-ddTHH:mm:ss`

---

## 六、消息通知管理 (MessageNoticeController)

**基础路径**：`/v1/messages`
**权限**：所有接口需认证

### 6.1 分页查询消息

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/messages` |
| **描述** | 分页查询消息通知列表 |

**Query参数** (MessageNoticeQuery)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| pageNum | Long | 否 | 1 | @Min(1) | 页码 |
| pageSize | Long | 否 | 10 | @Min(1) @Max(100) | 每页条数 |
| channel | Integer | 否 | - | - | 消息渠道 |
| sendStatus | Integer | 否 | - | - | 发送状态 |
| readStatus | Integer | 否 | - | - | 已读状态 |
| businessType | String | 否 | - | - | 业务类型 |

**响应数据**：`Result<PageResult<MessageNoticeVO>>`

---

### 6.2 查询消息详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/messages/{id}` |
| **描述** | 查询消息详情 |

**路径参数**：`id` (Long, 必填) — 消息ID

**响应数据**：`Result<MessageNoticeVO>`

---

### 6.3 查询未读消息数

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/messages/unread-count` |
| **描述** | 查询未读消息数量 |

**请求参数**：无

**响应数据**：`Result<Long>`

---

### 6.4 新增消息

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/messages` |
| **描述** | 新增消息通知 |

**请求体** (MessageNoticeCreateDTO)：

| 字段 | 类型 | 必填 | 默认值 | 校验 | 说明 |
|------|------|------|--------|------|------|
| receiverUserId | Long | 否 | - | - | 接收用户ID |
| receiverSupplierId | Long | 否 | - | - | 接收供应商ID |
| channel | Integer | 否 | 1 | - | 消息渠道 |
| title | String | ✅ | - | @NotBlank | 标题 |
| content | String | ✅ | - | @NotBlank | 内容 |
| businessType | String | 否 | - | - | 业务类型 |
| businessId | Long | 否 | - | - | 业务ID |

**响应数据**：`Result<Long>`（返回新建消息ID）

**前端注意**：
- `title` 和 `content` 为必填
- `channel` 不传时默认为1
- `receiverUserId` 和 `receiverSupplierId` 至少传一个

---

### 6.5 标记已读

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/messages/{id}/read` |
| **描述** | 标记指定消息为已读 |

**路径参数**：`id` (Long, 必填) — 消息ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

### 6.6 全部标记已读

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/messages/read-all` |
| **描述** | 将当前用户所有消息标记为已读 |

**请求参数**：无

**响应数据**：`Result<Void>`

---

## 七、消息模板管理 (MessageTemplateController)

**基础路径**：`/v1/message-templates`
**权限**：查询类需认证，增删改类需 `config:manage` 权限

### 7.1 分页查询消息模板

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/message-templates` |
| **描述** | 分页查询消息模板 |
| **权限** | isAuthenticated |

**Query参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| keyword | String | 否 | - | 关键词搜索 |
| channel | Integer | 否 | - | 消息渠道 |

**响应数据**：`Result<PageResult<MessageTemplate>>`

---

### 7.2 获取模板详情

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/message-templates/{id}` |
| **描述** | 获取消息模板详情 |
| **权限** | isAuthenticated |

**路径参数**：`id` (Long, 必填) — 模板ID

**响应数据**：`Result<MessageTemplate>`

---

### 7.3 新增消息模板

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/message-templates` |
| **描述** | 新增消息模板 |
| **权限** | config:manage |

**请求体** (MessageTemplate)：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| templateCode | String | ✅ | 模板编码 |
| templateName | String | ✅ | 模板名称 |
| channel | Integer | ✅ | 消息渠道 |
| titleTemplate | String | ✅ | 标题模板 |
| contentTemplate | String | ✅ | 内容模板 |
| variables | String | 否 | 变量定义 |
| businessType | String | 否 | 业务类型 |
| status | Integer | 否 | 状态 |
| remark | String | 否 | 备注 |

**响应数据**：`Result<Long>`（返回新建模板ID）

**前端注意**：
- 此接口需要 `config:manage` 权限，普通用户无权调用
- `id`、`createTime`、`createBy`、`updateTime`、`updateBy`、`deleted`、`version` 字段由后端自动填充，前端不需要传

---

### 7.4 更新消息模板

| 项目 | 详情 |
|------|------|
| **HTTP方法** | PUT |
| **路径** | `/v1/message-templates/{id}` |
| **描述** | 更新消息模板 |
| **权限** | config:manage |

**路径参数**：`id` (Long, 必填) — 模板ID

**请求体**：MessageTemplate（同7.3）

**响应数据**：`Result<Void>`

---

### 7.5 删除消息模板

| 项目 | 详情 |
|------|------|
| **HTTP方法** | DELETE |
| **路径** | `/v1/message-templates/{id}` |
| **描述** | 删除消息模板 |
| **权限** | config:manage |

**路径参数**：`id` (Long, 必填) — 模板ID

**响应数据**：`Result<Void>`

---

### 7.6 启用/停用模板

| 项目 | 详情 |
|------|------|
| **HTTP方法** | POST |
| **路径** | `/v1/message-templates/{id}/toggle` |
| **描述** | 切换模板启用/停用状态 |
| **权限** | config:manage |

**路径参数**：`id` (Long, 必填) — 模板ID

**请求参数**：无

**响应数据**：`Result<Void>`

---

### 7.7 按业务类型查询启用的模板列表

| 项目 | 详情 |
|------|------|
| **HTTP方法** | GET |
| **路径** | `/v1/message-templates/list` |
| **描述** | 按业务类型查询启用的模板列表 |
| **权限** | isAuthenticated |

**Query参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| businessType | String | 否 | 业务类型 |
| channel | Integer | 否 | 消息渠道 |

**响应数据**：`Result<List<MessageTemplate>>`

---

## 八、结算+门户+消息模块响应VO字段汇总

### DeductionVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| deductionNo | String | 扣款单号 |
| supplierId | Long | 供应商ID |
| sourceType | String | 来源类型 |
| sourceId | Long | 来源ID |
| deductionType | Integer | 扣款类型 |
| deductionAmount | BigDecimal | 扣款金额 |
| deductionReason | String | 扣款原因 |
| deductionStatus | Integer | 扣款状态 |
| reconId | Long | 对账单ID |
| createTime | LocalDateTime | 创建时间 |

### InvoiceVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| invoiceNo | String | 发票号码 |
| invoiceCode | String | 发票编码 |
| invoiceType | Integer | 发票类型 |
| reconId | Long | 对账单ID |
| supplierId | Long | 供应商ID |
| supplierName | String | 供应商名称 |
| taxNumber | String | 税号 |
| invoiceAmount | BigDecimal | 发票金额 |
| taxAmount | BigDecimal | 税额 |
| taxRate | BigDecimal | 税率 |
| invoiceDate | LocalDate | 开票日期 |
| invoiceStatus | Integer | 发票状态 |
| receiveTime | LocalDateTime | 收票时间 |
| certifyTime | LocalDateTime | 认证时间 |
| voidTime | LocalDateTime | 作废时间 |
| voidReason | String | 作废原因 |
| fileId | Long | 附件ID |
| ocrStatus | Integer | OCR识别状态 |
| remark | String | 备注 |
| createTime | LocalDateTime | 创建时间 |

### PaymentVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| paymentNo | String | 付款单号 |
| invoiceId | Long | 发票ID |
| invoiceNo | String | 发票号码 |
| reconId | Long | 对账单ID |
| supplierId | Long | 供应商ID |
| supplierName | String | 供应商名称 |
| paymentAmount | BigDecimal | 付款金额 |
| paymentMethod | Integer | 付款方式 |
| paymentAccount | String | 付款账号 |
| paymentBank | String | 付款银行 |
| receiveAccount | String | 收款账号 |
| receiveBank | String | 收款银行 |
| scheduleDate | LocalDate | 计划付款日期 |
| paymentTerms | String | 付款条件 |
| paymentTime | LocalDateTime | 实际付款时间 |
| paymentStatus | Integer | 付款状态 |
| receiptNo | String | 回单号 |
| approveStatus | Integer | 审批状态 |
| voucherNo | String | 凭证号 |
| remark | String | 备注 |
| createTime | LocalDateTime | 创建时间 |

### PortalTodoVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 用户ID |
| supplierId | Long | 供应商ID |
| todoType | String | 待办类型 |
| businessType | String | 业务类型 |
| businessId | Long | 业务ID |
| businessNo | String | 业务编号 |
| title | String | 标题 |
| todoStatus | Integer | 待办状态 |
| dueTime | LocalDateTime | 截止时间 |
| finishTime | LocalDateTime | 完成时间 |
| createTime | LocalDateTime | 创建时间 |

### MessageNoticeVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| noticeNo | String | 通知编号 |
| receiverUserId | Long | 接收用户ID |
| receiverSupplierId | Long | 接收供应商ID |
| channel | Integer | 消息渠道 |
| title | String | 标题 |
| content | String | 内容 |
| businessType | String | 业务类型 |
| businessId | Long | 业务ID |
| sendStatus | Integer | 发送状态 |
| readStatus | Integer | 已读状态 |
| sendTime | LocalDateTime | 发送时间 |
| readTime | LocalDateTime | 已读时间 |
| retryCount | Integer | 重试次数 |
| errorMessage | String | 错误信息 |

---

## 九、结算+门户+消息模块API速查表

### 扣款管理

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 1 | GET | `/v1/deductions` | DeductionQuery | Result\<PageResult\<DeductionVO\>\> | 分页查询扣款单 |
| 2 | GET | `/v1/deductions/{id}` | - | Result\<DeductionVO\> | 查询扣款单详情 |
| 3 | POST | `/v1/deductions` | DeductionCreateDTO | Result\<Long\> | 新增扣款单 |
| 4 | POST | `/v1/deductions/{id}/submit` | DeductionActionDTO | Result\<Void\> | 提交扣款单 |
| 5 | POST | `/v1/deductions/{id}/confirm` | DeductionActionDTO | Result\<Void\> | 确认扣款单 |
| 6 | POST | `/v1/deductions/{id}/dispute` | DeductionActionDTO | Result\<Void\> | 扣款单异议 |
| 7 | POST | `/v1/deductions/{id}/book` | DeductionActionDTO | Result\<Void\> | 扣款单入账 |

### 发票管理

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 8 | GET | `/v1/invoices` | InvoiceQuery | Result\<PageResult\<InvoiceVO\>\> | 分页查询发票 |
| 9 | GET | `/v1/invoices/{id}` | - | Result\<InvoiceVO\> | 查询发票详情 |
| 10 | POST | `/v1/invoices` | InvoiceCreateDTO | Result\<Long\> | 新增发票 |
| 11 | POST | `/v1/invoices/{id}/upload` | InvoiceUploadDTO | Result\<Void\> | 上传发票 |
| 12 | POST | `/v1/invoices/{id}/verify` | InvoiceActionDTO | Result\<Void\> | 发票验真 |
| 13 | POST | `/v1/invoices/{id}/certify` | InvoiceActionDTO | Result\<Void\> | 发票认证 |
| 14 | POST | `/v1/invoices/{id}/void` | InvoiceActionDTO | Result\<Void\> | 发票作废 |

### 付款管理

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 15 | GET | `/v1/payments` | PaymentQuery | Result\<PageResult\<PaymentVO\>\> | 分页查询付款单 |
| 16 | GET | `/v1/payments/{id}` | - | Result\<PaymentVO\> | 查询付款单详情 |
| 17 | POST | `/v1/payments` | PaymentCreateDTO | Result\<Long\> | 新增付款单 |
| 18 | POST | `/v1/payments/{id}/submit-approval` | - | Result\<Void\> | 提交审批 |
| 19 | POST | `/v1/payments/{id}/schedule` | PaymentScheduleDTO | Result\<Void\> | 付款排期 |
| 20 | POST | `/v1/payments/{id}/pay` | PaymentActionDTO | Result\<Void\> | 付款 |
| 21 | POST | `/v1/payments/{id}/reject` | PaymentActionDTO | Result\<Void\> | 拒绝付款 |
| 22 | POST | `/v1/payments/{id}/cancel` | PaymentActionDTO | Result\<Void\> | 取消付款单 |

### 仪表盘

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 23 | GET | `/v1/dashboard/metrics` | - | Result\<List\<DashboardMetricVO\>\> | 获取指标概览 |
| 24 | GET | `/v1/dashboard/trends` | - | Result\<List\<DashboardTrendVO\>\> | 获取趋势数据 |
| 25 | GET | `/v1/dashboard/risks` | - | Result\<List\<DashboardRiskVO\>\> | 获取风险预警 |
| 26 | GET | `/v1/dashboard/supplier-performance` | - | Result\<List\<SupplierPerformanceVO\>\> | 获取供应商绩效 |

### 待办事项

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 27 | GET | `/v1/todos` | PortalTodoQuery | Result\<PageResult\<PortalTodoVO\>\> | 分页查询待办 |
| 28 | GET | `/v1/todos/unread-count` | - | Result\<Long\> | 查询未读待办数 |
| 29 | POST | `/v1/todos/{id}/finish` | - | Result\<Void\> | 完成待办 |
| 30 | POST | `/v1/todos/{id}/ignore` | - | Result\<Void\> | 忽略待办 |
| 31 | POST | `/v1/todos` | PortalTodoCreateDTO | Result\<Long\> | 新增待办 |

### 消息通知

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 |
|---|------|------|--------|----------|------|
| 32 | GET | `/v1/messages` | MessageNoticeQuery | Result\<PageResult\<MessageNoticeVO\>\> | 分页查询消息 |
| 33 | GET | `/v1/messages/{id}` | - | Result\<MessageNoticeVO\> | 查询消息详情 |
| 34 | GET | `/v1/messages/unread-count` | - | Result\<Long\> | 查询未读消息数 |
| 35 | POST | `/v1/messages` | MessageNoticeCreateDTO | Result\<Long\> | 新增消息 |
| 36 | POST | `/v1/messages/{id}/read` | - | Result\<Void\> | 标记已读 |
| 37 | POST | `/v1/messages/read-all` | - | Result\<Void\> | 全部标记已读 |

### 消息模板

| # | 方法 | 路径 | 请求体 | 响应类型 | 描述 | 权限 |
|---|------|------|--------|----------|------|------|
| 38 | GET | `/v1/message-templates` | Query | Result\<PageResult\<MessageTemplate\>\> | 分页查询模板 | 认证 |
| 39 | GET | `/v1/message-templates/{id}` | - | Result\<MessageTemplate\> | 获取模板详情 | 认证 |
| 40 | POST | `/v1/message-templates` | MessageTemplate | Result\<Long\> | 新增模板 | config:manage |
| 41 | PUT | `/v1/message-templates/{id}` | MessageTemplate | Result\<Void\> | 更新模板 | config:manage |
| 42 | DELETE | `/v1/message-templates/{id}` | - | Result\<Void\> | 删除模板 | config:manage |
| 43 | POST | `/v1/message-templates/{id}/toggle` | - | Result\<Void\> | 启用/停用模板 | config:manage |
| 44 | GET | `/v1/message-templates/list` | Query | Result\<List\<MessageTemplate\>\> | 按业务类型查询模板 | 认证 |

---

# 全局统计

| 模块 | 控制器 | 端点数量 |
|------|--------|---------|
| 认证 | AuthController | 3 |
| 通用 | IdempotentController | 1 |
| 订单 | PurchaseOrderController | 12 |
| 订单 | OrderChangeController | 4 |
| 订单 | OrderTrackController | 1 |
| 物流 | DeliveryDetailController | 5 |
| 物流 | DeliveryLabelController | 5 |
| 物流 | ReceiptDiffController | 5 |
| 物流 | ReceiptRecordController | 7 |
| 物流 | VmiInventoryController | 3 |
| 质量 | EightDReportController | 11 |
| 质量 | QualityAppealController | 6 |
| 结算 | DeductionController | 7 |
| 结算 | InvoiceController | 7 |
| 结算 | PaymentController | 8 |
| 门户 | DashboardController | 4 |
| 门户 | PortalTodoController | 5 |
| 消息 | MessageNoticeController | 6 |
| 消息 | MessageTemplateController | 7 |
| **合计** | **19个控制器** | **107个API端点** |
