# 6月2日后四模块API汇总

> 本文档汇总质量协同、质量中心、IQC检验标准、财务结算四个模块的全部后端API，供前端人员参考。
>
> **通用说明**：
> - 所有接口需认证（`@PreAuthorize("isAuthenticated()")`），请求头需携带 `Authorization: Bearer <token>`
> - 统一返回包装：`Result<T>`，结构为 `{ code: number, message: string, data: T }`
> - 分页查询统一返回 `Result<PageResult<XXXVO>>`，`PageResult` 结构为 `{ records: T[], total: number, pageNum: number, pageSize: number }`
> - 分页查询通用参数：`pageNum`（默认1）、`pageSize`（默认10，最大100）
> - 日期格式：`yyyy-MM-dd`，日期时间格式：`yyyy-MM-dd HH:mm:ss`

---

## 一、质量协同 — 统一处理检验任务、NCR、8D整改与质量申诉

### 1.1 质量检验（QualityInspection）

**基础路径**：`/v1/quality-inspections`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/quality-inspections` | 分页查询质检单 | Query: QualityInspectionQuery | `Result<PageResult<QualityInspectionVO>>` |
| 2 | GET | `/v1/quality-inspections/{id}` | 查询质检单详情 | Path: id (Long) | `Result<QualityInspectionVO>` |
| 3 | POST | `/v1/quality-inspections` | 创建质检单 | Body: QualityInspectionCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/quality-inspections/{id}/submit` | 提交质检单 | Path: id, Body: QualitySubmitDTO | `Result<Void>` |
| 5 | POST | `/v1/quality-inspections/{id}/handle` | 处理质检单 | Path: id, Body: QualityHandleDTO | `Result<Void>` |

**QualityInspectionQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| receiptId | Long | 收货记录ID |
| deliveryId | Long | 送货单ID |
| materialCode | String | 物料编码 |
| inspectResult | Integer | 检验结果 |
| inspectType | Integer | 检验类型 |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| supplierId | Long | 供应商ID |

**QualityInspectionCreateDTO 请求体**：

```json
{
  "receiptId": 1,           // Long, 必填, 收货记录ID
  "deliveryId": 1,          // Long, 送货单ID
  "supplierId": 1,          // Long, 供应商ID
  "standardId": 1,          // Long, 检验标准ID
  "materialCode": "MAT001", // String, 必填, 物料编码
  "materialName": "物料A",   // String, 必填, 物料名称
  "inspectQty": 100,        // BigDecimal, 检验数量（默认0）
  "inspectType": 1,         // Integer, 检验类型（默认1）
  "inspectRemark": "备注"    // String, 检验备注
}
```

**QualitySubmitDTO 请求体**：

```json
{
  "inspectQty": 100,       // BigDecimal, 检验数量
  "qualifiedQty": 95,      // BigDecimal, 合格数量
  "unqualifiedQty": 5,     // BigDecimal, 不合格数量
  "inspectResult": 1,      // Integer, 检验结果
  "inspectType": 1,        // Integer, 检验类型
  "inspectRemark": "合格"   // String, 检验备注
}
```

**QualityHandleDTO 请求体**：

```json
{
  "handleMethod": 1,       // Integer, 必填, 处理方式（1退货 2让步接收 3挑选使用 4报废 5扣款）
  "handleRemark": "退货处理" // String, 处理备注
}
```

**QualityInspectionVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "inspectionNo": "QI202606020001",
    "receiptId": 1,
    "deliveryId": 1,
    "supplierId": 100,
    "standardId": 1,
    "materialCode": "MAT001",
    "materialName": "物料A",
    "inspectQty": 100,
    "qualifiedQty": 95,
    "unqualifiedQty": 5,
    "inspectResult": 1,
    "inspectType": 1,
    "inspectTime": "2026-06-02 10:30:00",
    "inspectorName": "张三",
    "inspectRemark": "合格",
    "handleMethod": null,
    "handleRemark": null,
    "standard": {
      "id": 1,
      "standardNo": "IS20260001",
      "materialCode": "MAT001",
      "materialName": "物料A",
      "standardName": "物料A检验标准",
      "sampleRule": "GB/T 2828.1",
      "versionNo": "V1.0",
      "inspectionStrategy": 1,
      "sampleRate": 0.1,
      "acceptanceRate": 97.0,
      "status": 1,
      "remark": "",
      "createTime": "2026-06-01 08:00:00",
      "items": []
    }
  }
}
```

---

### 1.2 NCR质量异常（NonconformanceReport）

**基础路径**：`/v1/nonconformance-reports`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/nonconformance-reports` | 分页查询NCR | Query: NonconformanceReportQuery | `Result<PageResult<NonconformanceReportVO>>` |
| 2 | GET | `/v1/nonconformance-reports/{id}` | 查询NCR详情 | Path: id (Long) | `Result<NonconformanceReportVO>` |
| 3 | POST | `/v1/nonconformance-reports` | 创建NCR | Body: NonconformanceReportCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/nonconformance-reports/{id}/submit` | 提交NCR | Path: id, Body: NcrActionDTO | `Result<Void>` |
| 5 | POST | `/v1/nonconformance-reports/{id}/handle` | 处理NCR | Path: id, Body: NcrActionDTO | `Result<Void>` |
| 6 | POST | `/v1/nonconformance-reports/{id}/verify` | 验证NCR | Path: id, Body: NcrActionDTO | `Result<Void>` |
| 7 | POST | `/v1/nonconformance-reports/{id}/close` | 关闭NCR | Path: id, Body: NcrActionDTO | `Result<Void>` |
| 8 | POST | `/v1/nonconformance-reports/{id}/upload-attachment` | 上传NCR附件 | Path: id, Param: file (MultipartFile) | `Result<Long>` |

**NonconformanceReportQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| keyword | String | 关键词 |
| ncrStatus | Integer | NCR状态（0草稿 1已发布 2处理中 3待验证 4已关闭 5已取消） |
| supplierId | Long | 供应商ID |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |

**NonconformanceReportCreateDTO 请求体**：

```json
{
  "supplierId": 100,          // Long, 必填, 供应商ID
  "materialCode": "MAT001",   // String, 必填, 物料编码
  "materialName": "物料A",     // String, 必填, 物料名称
  "unqualifiedQty": 5,        // BigDecimal, 必填, 不合格数量
  "problemDesc": "表面划伤",    // String, 必填, 问题描述
  "severity": 2,              // Integer, 必填, 严重程度（1一般 2严重 3重大）
  "inspectionId": 1           // Long, 质检单ID
}
```

**NcrActionDTO 请求体**：

```json
{
  "remark": "处理完成",       // String, 备注
  "handleMethod": 1,         // Integer, 处理方式（1退货 2让步接收 3挑选使用 4报废 5扣款）
  "handleDetail": "已退货",   // String, 处理详情
  "handleRemark": "供应商承担运费" // String, 处理备注
}
```

**NonconformanceReportVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "ncrNo": "NCR202606020001",
    "inspectionId": 1,
    "receiptId": 1,
    "supplierId": 100,
    "materialCode": "MAT001",
    "materialName": "物料A",
    "unqualifiedQty": 5,
    "problemDesc": "表面划伤",
    "severity": 2,
    "handleMethod": null,
    "handleDetail": null,
    "handleRemark": null,
    "ncrStatus": 0,
    "submitTime": null,
    "closeTime": null,
    "closeRemark": null,
    "createTime": "2026-06-02 10:00:00"
  }
}
```

**NCR状态枚举（NcrStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| DRAFT | 0 | 草稿 |
| PUBLISHED | 1 | 已发布 |
| PROCESSING | 2 | 处理中 |
| PENDING_VERIFY | 3 | 待验证 |
| CLOSED | 4 | 已关闭 |
| CANCELED | 5 | 已取消 |

**严重程度枚举（SeverityEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| GENERAL | 1 | 一般 |
| SERIOUS | 2 | 严重 |
| MAJOR | 3 | 重大 |

**处理方式枚举（HandleMethodEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| RETURN | 1 | 退货（生成退货单，关联物流） |
| CONCESSION | 2 | 让步接收（记录让步原因和审批） |
| SORTING | 3 | 挑选使用（记录挑选结果） |
| SCRAP | 4 | 报废（记录报废数量和原因） |
| DEDUCTION | 5 | 扣款（生成扣款单） |

---

### 1.3 8D整改报告（EightDReport）

**基础路径**：`/v1/eight-d-reports`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/eight-d-reports` | 分页查询8D报告 | Query: EightDReportQuery | `Result<PageResult<EightDReportVO>>` |
| 2 | GET | `/v1/eight-d-reports/{id}` | 查询8D报告详情 | Path: id (Long) | `Result<EightDReportVO>` |
| 3 | POST | `/v1/eight-d-reports` | 创建8D报告 | Body: EightDReportCreateDTO | `Result<Long>` |
| 4 | PUT | `/v1/eight-d-reports/{id}` | 更新8D报告 | Path: id, Body: EightDReportUpdateDTO | `Result<Void>` |
| 5 | POST | `/v1/eight-d-reports/{id}/submit` | 提交8D报告 | Path: id, Body: EightDActionDTO | `Result<Void>` |
| 6 | POST | `/v1/eight-d-reports/{id}/audit` | 审核8D报告 | Path: id, Body: EightDActionDTO | `Result<Void>` |
| 7 | POST | `/v1/eight-d-reports/{id}/reject` | 退回8D报告 | Path: id, Body: EightDActionDTO | `Result<Void>` |
| 8 | POST | `/v1/eight-d-reports/{id}/close` | 关闭8D报告 | Path: id, Body: EightDActionDTO | `Result<Void>` |
| 9 | POST | `/v1/eight-d-reports/{id}/step-submit` | 提交8D阶段(D1-D8) | Path: id, Body: EightDActionDTO | `Result<Void>` |
| 10 | POST | `/v1/eight-d-reports/{id}/step-approve` | 审核8D阶段 | Path: id, Body: EightDActionDTO | `Result<Void>` |
| 11 | POST | `/v1/eight-d-reports/{id}/upload-attachment` | 上传8D报告附件 | Path: id, Param: file (MultipartFile) | `Result<Long>` |

**EightDReportQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| keyword | String | 关键词 |
| ncrId | Long | NCR ID |
| reportStatus | Integer | 报告状态（0草稿 1已提交 2审核中 3退回 4已关闭） |
| supplierId | Long | 供应商ID |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |

**EightDReportCreateDTO 请求体**：

```json
{
  "ncrId": 1,                    // Long, 必填, NCR ID
  "supplierId": 100,             // Long, 必填, 供应商ID
  "d1Team": "张三、李四、王五",    // String, D1-组建团队
  "d2Problem": "来料表面划伤",     // String, 必填, D2-问题描述
  "d3Containment": "暂停使用该批次", // String, D3-遏制措施
  "d4RootCause": "包装防护不足",    // String, D4-根因分析
  "d5CorrectiveAction": "改进包装方案", // String, D5-纠正措施
  "d6ValidateAction": "抽检3批次合格", // String, D6-验证效果
  "d7PreventAction": "更新包装SOP",   // String, D7-预防措施
  "d8CloseSummary": "问题已解决",     // String, D8-结案总结
  "dueDate": "2026-06-30",           // LocalDate, 必填, 截止日期
  "currentStep": 1                   // Integer, 当前阶段
}
```

**EightDReportUpdateDTO 请求体**：

```json
{
  "d1Team": "张三、李四、王五",
  "d2Problem": "来料表面划伤",
  "d3Containment": "暂停使用该批次",
  "d4RootCause": "包装防护不足",
  "d5CorrectiveAction": "改进包装方案",
  "d6ValidateAction": "抽检3批次合格",
  "d7PreventAction": "更新包装SOP",
  "d8CloseSummary": "问题已解决",
  "dueDate": "2026-06-30",
  "currentStep": 2,
  "stepDueDate": "2026-06-10"
}
```

**EightDActionDTO 请求体**：

```json
{
  "remark": "阶段完成",          // String, 备注
  "currentStep": 2,             // Integer, 当前阶段
  "stepDueDate": "2026-06-10",  // LocalDate, 阶段截止日期
  "stepContent": "D2问题描述内容"  // String, 阶段内容
}
```

**EightDReportVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "reportNo": "8D202606020001",
    "ncrId": 1,
    "supplierId": 100,
    "d1Team": "张三、李四、王五",
    "d2Problem": "来料表面划伤",
    "d3Containment": "暂停使用该批次",
    "d4RootCause": "包装防护不足",
    "d5CorrectiveAction": "改进包装方案",
    "d6ValidateAction": "抽检3批次合格",
    "d7PreventAction": "更新包装SOP",
    "d8CloseSummary": "问题已解决",
    "dueDate": "2026-06-30",
    "currentStep": 1,
    "stepDueDate": "2026-06-10",
    "reportStatus": 0,
    "submitTime": null,
    "auditTime": null,
    "closeTime": null,
    "createTime": "2026-06-02 10:00:00"
  }
}
```

**8D报告状态枚举（ReportStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| DRAFT | 0 | 草稿 |
| SUBMITTED | 1 | 已提交 |
| AUDITING | 2 | 审核中 |
| REJECTED | 3 | 退回 |
| CLOSED | 4 | 已关闭 |

**8D阶段枚举（EightDStepEnum）**：

| 枚举 | code | 说明 | 默认天数 |
|------|------|------|---------|
| D1 | 1 | D1-组建团队 | 3 |
| D2 | 2 | D2-问题描述 | 3 |
| D3 | 3 | D3-遏制措施 | 3 |
| D4 | 4 | D4-根因分析 | 5 |
| D5 | 5 | D5-纠正措施 | 5 |
| D6 | 6 | D6-验证效果 | 5 |
| D7 | 7 | D7-预防措施 | 5 |
| D8 | 8 | D8-结案 | 3 |

---

### 1.4 质量申诉（QualityAppeal）

**基础路径**：`/v1/quality-appeals`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/quality-appeals` | 分页查询质量申诉 | Query: QualityAppealQuery | `Result<PageResult<QualityAppealVO>>` |
| 2 | GET | `/v1/quality-appeals/{id}` | 查询质量申诉详情 | Path: id (Long) | `Result<QualityAppealVO>` |
| 3 | POST | `/v1/quality-appeals` | 创建质量申诉 | Body: QualityAppealCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/quality-appeals/{id}/submit` | 提交质量申诉 | Path: id | `Result<Void>` |
| 5 | POST | `/v1/quality-appeals/{id}/approve` | 通过质量申诉 | Path: id, Body: QualityAppealAuditDTO | `Result<Void>` |
| 6 | POST | `/v1/quality-appeals/{id}/reject` | 驳回质量申诉 | Path: id, Body: QualityAppealAuditDTO | `Result<Void>` |

**QualityAppealQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| keyword | String | 关键词 |
| ncrId | Long | NCR ID |
| appealStatus | Integer | 申诉状态（0草稿 1已提交 2审核中 3通过 4驳回） |
| supplierId | Long | 供应商ID |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |

**QualityAppealCreateDTO 请求体**：

```json
{
  "ncrId": 1,              // Long, 必填, NCR ID
  "inspectionId": 1,       // Long, 质检单ID
  "supplierId": 100,       // Long, 必填, 供应商ID
  "appealReason": "判定标准有争议" // String, 必填, 申诉原因
}
```

**QualityAppealAuditDTO 请求体**：

```json
{
  "auditRemark": "同意申诉" // String, 必填, 审核意见
}
```

**QualityAppealVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "appealNo": "QA202606020001",
    "ncrId": 1,
    "inspectionId": 1,
    "supplierId": 100,
    "appealReason": "判定标准有争议",
    "appealStatus": 0,
    "submitTime": null,
    "auditBy": null,
    "auditTime": null,
    "auditRemark": null,
    "createTime": "2026-06-02 10:00:00"
  }
}
```

**申诉状态枚举（AppealStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| DRAFT | 0 | 草稿 |
| SUBMITTED | 1 | 已提交 |
| AUDITING | 2 | 审核中 |
| APPROVED | 3 | 通过 |
| REJECTED | 4 | 驳回 |

---

## 二、质量中心 — NCR质量异常、8D整改报告与质量申诉管理

> 质量中心与质量协同共享同一套后端API，区别在于前端展示视角不同。质量中心侧重于管理视角的汇总与审批操作。

### 2.1 NCR质量异常管理

同 **1.2 NCR质量异常**，管理视角重点关注：
- NCR列表查询（按状态/供应商/日期筛选）
- NCR处理方式决策（退货/让步接收/挑选使用/报废/扣款）
- NCR验证与关闭

### 2.2 8D整改报告管理

同 **1.3 8D整改报告**，管理视角重点关注：
- 8D报告审核（audit / reject）
- 8D阶段推进审批（step-approve）
- 8D报告关闭

### 2.3 质量申诉管理

同 **1.4 质量申诉**，管理视角重点关注：
- 申诉审批（approve / reject）
- 申诉状态跟踪

### 2.4 申诉联动扣款（Appeal — 内部服务，暂无独立Controller）

> 以下DTO/VO已定义，但后端暂未暴露独立API端点，供前端了解数据结构。

**AppealCreateDTO（联动扣款申诉创建）**：

```json
{
  "ncrId": 1,                    // Long, 必填, NCR ID
  "inspectionId": 1,             // Long, 质检单ID
  "deductionId": 1,              // Long, 扣款单ID
  "supplierId": 100,             // Long, 必填, 供应商ID
  "materialCode": "MAT001",      // String, 物料编码
  "materialName": "物料A",        // String, 物料名称
  "appealReason": "扣款金额不合理", // String, 必填, 申诉原因
  "appealDesc": "详细说明...",     // String, 申诉说明
  "adjustAmount": 500.00         // BigDecimal, 申诉要求调整的金额
}
```

**AppealReviewDTO（申诉审核）**：

```json
{
  "appealStatus": 2,          // Integer, 必填, 审核结果（2通过 3驳回）
  "reviewOpinion": "同意调整"   // String, 审核意见
}
```

**AppealVO 返回结构**：

```json
{
  "id": 1,
  "appealNo": "AP202606020001",
  "ncrId": 1,
  "inspectionId": 1,
  "deductionId": 1,
  "supplierId": 100,
  "materialCode": "MAT001",
  "materialName": "物料A",
  "appealReason": "扣款金额不合理",
  "appealDesc": "详细说明...",
  "adjustAmount": 500.00,
  "appealStatus": 1,
  "appealStatusDesc": "已提交",
  "submitTime": "2026-06-02 10:00:00",
  "reviewerName": "审核人A",
  "reviewTime": null,
  "reviewOpinion": null,
  "remark": "",
  "createTime": "2026-06-02 09:00:00"
}
```

---

## 三、IQC检验标准 — 维护来料检验的检验项目、抽样规则与判定标准

### 3.1 检验标准（InspectionStandard）

**基础路径**：`/v1/inspection-standards`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/inspection-standards` | 分页查询检验标准 | Query: InspectionStandardQuery | `Result<PageResult<InspectionStandardVO>>` |
| 2 | GET | `/v1/inspection-standards/{id}` | 查询检验标准详情 | Path: id (Long) | `Result<InspectionStandardVO>` |
| 3 | GET | `/v1/inspection-standards/by-material/{materialCode}` | 按物料编码查询标准 | Path: materialCode (String) | `Result<InspectionStandardVO>` |
| 4 | POST | `/v1/inspection-standards` | 创建检验标准 | Body: InspectionStandardCreateDTO | `Result<Long>` |
| 5 | PUT | `/v1/inspection-standards` | 更新检验标准 | Body: InspectionStandardUpdateDTO | `Result<Void>` |
| 6 | POST | `/v1/inspection-standards/{id}/status` | 更新标准状态 | Path: id, Param: status (Integer) | `Result<Void>` |
| 7 | DELETE | `/v1/inspection-standards/{id}` | 删除检验标准 | Path: id (Long) | `Result<Void>` |

**InspectionStandardQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| keyword | String | 关键词 |
| materialCode | String | 物料编码 |
| inspectionStrategy | Integer | 检验策略（0免检 1抽检 2全检） |
| status | Integer | 状态 |

**InspectionStandardCreateDTO 请求体**：

```json
{
  "materialCode": "MAT001",        // String, 必填, 物料编码
  "materialName": "物料A",          // String, 必填, 物料名称
  "standardName": "物料A检验标准",   // String, 必填, 标准名称
  "sampleRule": "GB/T 2828.1",     // String, 抽样规则
  "versionNo": "V1.0",             // String, 版本号
  "inspectionStrategy": 1,         // Integer, 检验策略（0免检 1抽检 2全检）
  "sampleRate": 0.1,               // BigDecimal, 抽检比例（如0.1表示10%）
  "acceptanceRate": 97.0,          // BigDecimal, 合格标准（如97.0表示97%）
  "remark": ""                     // String, 备注
}
```

**InspectionStandardUpdateDTO 请求体**：

```json
{
  "id": 1,                         // Long, 必填, ID
  "materialCode": "MAT001",        // String, 物料编码
  "materialName": "物料A",          // String, 物料名称
  "standardName": "物料A检验标准V2", // String, 标准名称
  "sampleRule": "GB/T 2828.1",     // String, 抽样规则
  "versionNo": "V2.0",             // String, 版本号
  "status": 1,                     // Integer, 状态
  "remark": ""                     // String, 备注
}
```

**InspectionStandardVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "standardNo": "IS20260001",
    "materialCode": "MAT001",
    "materialName": "物料A",
    "standardName": "物料A检验标准",
    "sampleRule": "GB/T 2828.1",
    "versionNo": "V1.0",
    "inspectionStrategy": 1,
    "sampleRate": 0.1,
    "acceptanceRate": 97.0,
    "status": 1,
    "remark": "",
    "createTime": "2026-06-01 08:00:00",
    "items": [
      {
        "id": 1,
        "standardId": 1,
        "itemName": "外观检查",
        "itemType": 1,
        "standardValue": "无划痕",
        "upperLimit": null,
        "lowerLimit": null,
        "unit": null,
        "required": 1,
        "sort": 1
      },
      {
        "id": 2,
        "standardId": 1,
        "itemName": "尺寸检测",
        "itemType": 2,
        "standardValue": "100mm",
        "upperLimit": 100.5,
        "lowerLimit": 99.5,
        "unit": "mm",
        "required": 1,
        "sort": 2
      }
    ]
  }
}
```

---

### 3.2 检验项目（InspectionStandardItem）

**基础路径**：`/v1/inspection-standards/{standardId}/items`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/inspection-standards/{standardId}/items` | 查询检验项目列表 | Path: standardId (Long) | `Result<List<InspectionStandardItemVO>>` |
| 2 | POST | `/v1/inspection-standards/{standardId}/items` | 添加检验项目 | Path: standardId, Body: InspectionStandardItemDTO | `Result<InspectionStandardItemVO>` |
| 3 | PUT | `/v1/inspection-standards/{standardId}/items` | 更新检验项目 | Body: InspectionStandardItemDTO | `Result<Void>` |
| 4 | DELETE | `/v1/inspection-standards/{standardId}/items/{itemId}` | 删除检验项目 | Path: standardId, itemId (Long) | `Result<Void>` |

**InspectionStandardItemDTO 请求体**：

```json
{
  "itemName": "外观检查",       // String, 必填, 检验项目名称
  "itemType": 1,              // Integer, 项目类型（默认1）
  "standardValue": "无划痕",    // String, 标准值
  "upperLimit": 100.5,        // BigDecimal, 上限
  "lowerLimit": 99.5,         // BigDecimal, 下限
  "unit": "mm",               // String, 单位
  "required": 1,              // Integer, 是否必检（默认1）
  "sort": 1,                  // Integer, 排序
  "standardId": 1             // Long, 必填, 标准ID
}
```

**InspectionStandardItemVO 返回示例**：

```json
{
  "id": 1,
  "standardId": 1,
  "itemName": "外观检查",
  "itemType": 1,
  "standardValue": "无划痕",
  "upperLimit": null,
  "lowerLimit": null,
  "unit": null,
  "required": 1,
  "sort": 1
}
```

---

## 四、财务结算 — 覆盖对账确认、发票管理与付款进度跟踪

### 4.1 对账管理（Reconciliation）

**基础路径**：`/v1/reconciliations`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/reconciliations` | 分页查询对账单 | Query: ReconciliationQuery | `Result<PageResult<ReconciliationVO>>` |
| 2 | GET | `/v1/reconciliations/{id}` | 查询对账单详情 | Path: id (Long) | `Result<ReconciliationVO>` |
| 3 | POST | `/v1/reconciliations` | 新增对账单 | Body: ReconciliationCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/reconciliations/{id}/send` | 发送对账单 | Path: id (Long) | `Result<Void>` |
| 5 | POST | `/v1/reconciliations/{id}/confirm` | 确认对账单 | Path: id, Body: ReconciliationConfirmDTO | `Result<Void>` |

**ReconciliationQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| keyword | String | 关键词 |
| supplierId | Long | 供应商ID |
| reconPeriod | String | 对账周期 |
| reconStatus | Integer | 对账状态（0草稿 1已发送 2已确认 3有争议 4已关闭） |

**ReconciliationCreateDTO 请求体**：

```json
{
  "reconNo": "REC202606020001",    // String, 必填, 对账单号
  "supplierId": 100,               // Long, 必填, 供应商ID
  "supplierName": "供应商A",        // String, 供应商名称
  "reconPeriod": "2026-05",        // String, 必填, 对账周期
  "startDate": "2026-05-01",       // LocalDate, 必填, 对账开始日期
  "endDate": "2026-05-31",         // LocalDate, 必填, 对账结束日期
  "totalAmount": 0,                // BigDecimal, 总金额（默认0）
  "remark": ""                     // String, 备注
}
```

**ReconciliationConfirmDTO 请求体**：

```json
{
  "confirmedAmount": 50000.00,     // BigDecimal, 确认金额（默认0）
  "diffAmount": 0,                 // BigDecimal, 差异金额（默认0）
  "disputed": false,               // Boolean, 是否有争议（默认false）
  "confirmRemark": "确认无误"       // String, 确认备注
}
```

**ReconciliationVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "reconNo": "REC202606020001",
    "supplierId": 100,
    "supplierName": "供应商A",
    "reconPeriod": "2026-05",
    "startDate": "2026-05-01",
    "endDate": "2026-05-31",
    "totalAmount": 50000.00,
    "confirmedAmount": 0,
    "diffAmount": 0,
    "reconStatus": 0,
    "sendTime": null,
    "confirmTime": null,
    "confirmRemark": null,
    "remark": "",
    "createTime": "2026-06-02 08:00:00"
  }
}
```

**对账状态枚举（ReconStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| DRAFT | 0 | 草稿 |
| SENT | 1 | 已发送 |
| CONFIRMED | 2 | 已确认 |
| DISPUTED | 3 | 有争议 |
| CLOSED | 4 | 已关闭 |

---

### 4.2 对账明细（ReconciliationDetail）

**基础路径**：`/v1/reconciliation-details`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/reconciliation-details` | 列表查询对账明细 | Query: ReconciliationDetailQuery | `Result<List<ReconciliationDetailVO>>` |
| 2 | GET | `/v1/reconciliation-details/{id}` | 查询明细详情 | Path: id (Long) | `Result<ReconciliationDetailVO>` |
| 3 | POST | `/v1/reconciliation-details` | 新增对账明细 | Body: ReconciliationDetailCreateDTO | `Result<Long>` |
| 4 | PUT | `/v1/reconciliation-details/{id}` | 更新对账明细 | Path: id, Body: ReconciliationDetailUpdateDTO | `Result<Void>` |
| 5 | DELETE | `/v1/reconciliation-details/{id}` | 删除对账明细 | Path: id (Long) | `Result<Void>` |

**ReconciliationDetailQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| reconId | Long | 必填, 对账单ID |

**ReconciliationDetailCreateDTO 请求体**：

```json
{
  "reconId": 1,                    // Long, 必填, 对账单ID
  "orderId": 1,                    // Long, 订单ID
  "orderNo": "PO202605010001",     // String, 订单号
  "deliveryId": 1,                 // Long, 收货ID
  "deliveryNo": "DN202605010001",  // String, 收货单号
  "materialCode": "MAT001",        // String, 物料编码
  "materialName": "物料A",          // String, 物料名称
  "quantity": 100,                 // BigDecimal, 数量（默认0）
  "unitPrice": 50.00,              // BigDecimal, 单价（默认0）
  "orderAmount": 5000.00,          // BigDecimal, 订单金额（默认0）
  "confirmedAmount": 5000.00,      // BigDecimal, 确认金额（默认0）
  "diffAmount": 0,                 // BigDecimal, 差异金额（默认0）
  "diffReason": "",                // String, 差异原因
  "confirmStatus": 0,              // Integer, 确认状态（默认0）
  "confirmRemark": "",             // String, 确认备注
  "remark": ""                     // String, 备注
}
```

**ReconciliationDetailUpdateDTO 请求体**：

```json
{
  "orderId": 1,
  "orderNo": "PO202605010001",
  "deliveryId": 1,
  "deliveryNo": "DN202605010001",
  "materialCode": "MAT001",
  "materialName": "物料A",
  "quantity": 100,
  "unitPrice": 50.00,
  "orderAmount": 5000.00,
  "confirmedAmount": 4800.00,
  "diffAmount": 200.00,
  "diffReason": "部分不合格",
  "confirmStatus": 1,
  "confirmRemark": "已确认差异",
  "remark": ""
}
```

**ReconciliationDetailVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "reconId": 1,
    "orderId": 1,
    "orderNo": "PO202605010001",
    "deliveryId": 1,
    "deliveryNo": "DN202605010001",
    "materialCode": "MAT001",
    "materialName": "物料A",
    "quantity": 100,
    "unitPrice": 50.00,
    "orderAmount": 5000.00,
    "confirmedAmount": 5000.00,
    "diffAmount": 0,
    "diffReason": null,
    "confirmStatus": 0,
    "confirmTime": null,
    "confirmRemark": null,
    "remark": ""
  }
}
```

---

### 4.3 对账汇总（ReconciliationSummary — 内部服务，暂无独立Controller）

> 以下DTO/VO已定义，后端暂未暴露独立API端点，供前端了解数据结构。

**ReconciliationSummaryDTO 请求体**：

```json
{
  "supplierId": 100,               // Long, 必填, 供应商ID
  "reconPeriod": "2026-05",        // String, 必填, 对账周期
  "startDate": "2026-05-01",       // LocalDate, 必填, 开始日期
  "endDate": "2026-05-31",         // LocalDate, 必填, 结束日期
  "includeDeduction": true,        // Boolean, 是否包含扣款（默认true）
  "remark": ""                     // String, 备注
}
```

**ReconciliationSummaryVO 返回结构**：

```json
{
  "reconId": 1,
  "reconNo": "REC202606020001",
  "supplierId": 100,
  "supplierName": "供应商A",
  "reconPeriod": "2026-05",
  "startDate": "2026-05-01",
  "endDate": "2026-05-31",
  "totalReceiptAmount": 60000.00,
  "totalReturnAmount": 5000.00,
  "totalDeductionAmount": 1000.00,
  "payableAmount": 54000.00,
  "receiptCount": 12,
  "returnCount": 2,
  "deductionCount": 1,
  "details": [
    {
      "sourceType": "receipt",
      "sourceNo": "DN202605010001",
      "materialCode": "MAT001",
      "materialName": "物料A",
      "quantity": 100,
      "unitPrice": 50.00,
      "amount": 5000.00,
      "diffAmount": 0,
      "diffReason": null,
      "remark": ""
    }
  ]
}
```

---

### 4.4 三单匹配（ThreeWayMatch — 内部服务，暂无独立Controller）

> 以下DTO/VO已定义，后端暂未暴露独立API端点，供前端了解数据结构。

**ThreeWayMatchDTO 请求体**：

```json
{
  "supplierId": 100,               // Long, 必填, 供应商ID
  "orderNo": "",                   // String, 订单号（为空则匹配所有）
  "startDate": "2026-05-01",       // LocalDate, 匹配日期范围开始
  "endDate": "2026-05-31",         // LocalDate, 匹配日期范围结束
  "tolerance": 0                   // BigDecimal, 金额容差（默认0）
}
```

**ThreeWayMatchVO 返回结构**：

```json
{
  "id": 1,
  "orderId": 1,
  "orderNo": "PO202605010001",
  "orderAmount": 5000.00,
  "receiptId": 1,
  "receiptAmount": 5000.00,
  "invoiceId": 1,
  "invoiceNo": "INV202605010001",
  "invoiceAmount": 5000.00,
  "supplierId": 100,
  "matchResult": 0,
  "matchResultDesc": "完全匹配",
  "diffAmount": 0,
  "diffReason": null,
  "matchTime": "2026-06-02 10:00:00",
  "remark": ""
}
```

> matchResult: 0=完全匹配, 1=部分匹配, 2=不匹配

---

### 4.5 发票管理（Invoice）

**基础路径**：`/v1/invoices`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/invoices` | 分页查询发票 | Query: InvoiceQuery | `Result<PageResult<InvoiceVO>>` |
| 2 | GET | `/v1/invoices/{id}` | 查询发票详情 | Path: id (Long) | `Result<InvoiceVO>` |
| 3 | POST | `/v1/invoices` | 新增发票 | Body: InvoiceCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/invoices/{id}/upload` | 上传发票 | Path: id, Body: InvoiceUploadDTO | `Result<Void>` |
| 5 | POST | `/v1/invoices/{id}/verify` | 发票验真 | Path: id, Body: InvoiceActionDTO | `Result<Void>` |
| 6 | POST | `/v1/invoices/{id}/certify` | 发票认证 | Path: id, Body: InvoiceActionDTO | `Result<Void>` |
| 7 | POST | `/v1/invoices/{id}/void` | 发票作废 | Path: id, Body: InvoiceActionDTO | `Result<Void>` |

**InvoiceQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| supplierId | Long | 供应商ID |
| invoiceStatus | Integer | 发票状态（0待开票 1已上传 2已验真 3已认证 4已作废） |
| keyword | String | 关键词 |

**InvoiceCreateDTO 请求体**：

```json
{
  "invoiceNo": "INV202606020001",   // String, 必填, 发票号码
  "invoiceCode": "1234567890",      // String, 发票代码
  "invoiceType": 1,                 // Integer, 发票类型（默认1）
  "reconId": 1,                     // Long, 对账单ID
  "supplierId": 100,                // Long, 必填, 供应商ID
  "supplierName": "供应商A",         // String, 供应商名称
  "taxNumber": "91110000XXXXXXXX",  // String, 税号
  "invoiceAmount": 50000.00,        // BigDecimal, 必填, 发票金额
  "taxAmount": 6500.00,             // BigDecimal, 税额
  "taxRate": 13,                    // BigDecimal, 必填, 税率
  "invoiceDate": "2026-06-02",      // LocalDate, 必填, 开票日期
  "remark": ""                      // String, 备注
}
```

**InvoiceUploadDTO 请求体**：

```json
{
  "fileId": 1  // Long, 必填, 附件ID
}
```

**InvoiceActionDTO 请求体**：

```json
{
  "remark": "验真通过"  // String, 备注
}
```

**InvoiceVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "invoiceNo": "INV202606020001",
    "invoiceCode": "1234567890",
    "invoiceType": 1,
    "reconId": 1,
    "supplierId": 100,
    "supplierName": "供应商A",
    "taxNumber": "91110000XXXXXXXX",
    "invoiceAmount": 50000.00,
    "taxAmount": 6500.00,
    "taxRate": 13,
    "invoiceDate": "2026-06-02",
    "invoiceStatus": 0,
    "receiveTime": null,
    "certifyTime": null,
    "voidTime": null,
    "voidReason": null,
    "fileId": null,
    "ocrStatus": null,
    "remark": "",
    "createTime": "2026-06-02 08:00:00"
  }
}
```

**发票状态枚举（InvoiceStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| PENDING | 0 | 待开票 |
| UPLOADED | 1 | 已上传 |
| VERIFIED | 2 | 已验真 |
| CERTIFIED | 3 | 已认证 |
| VOIDED | 4 | 已作废 |

---

### 4.6 扣款管理（Deduction）

**基础路径**：`/v1/deductions`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/deductions` | 分页查询扣款单 | Query: DeductionQuery | `Result<PageResult<DeductionVO>>` |
| 2 | GET | `/v1/deductions/{id}` | 查询扣款单详情 | Path: id (Long) | `Result<DeductionVO>` |
| 3 | POST | `/v1/deductions` | 新增扣款单 | Body: DeductionCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/deductions/{id}/submit` | 提交扣款单 | Path: id, Body: DeductionActionDTO | `Result<Void>` |
| 5 | POST | `/v1/deductions/{id}/confirm` | 确认扣款单 | Path: id, Body: DeductionActionDTO | `Result<Void>` |
| 6 | POST | `/v1/deductions/{id}/dispute` | 扣款单异议 | Path: id, Body: DeductionActionDTO | `Result<Void>` |
| 7 | POST | `/v1/deductions/{id}/book` | 扣款单入账 | Path: id, Body: DeductionActionDTO | `Result<Void>` |

**DeductionQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| supplierId | Long | 供应商ID |
| deductionStatus | Integer | 扣款状态（0草稿 1已提交 2已确认 3有异议 4已入账） |
| keyword | String | 关键词 |

**DeductionCreateDTO 请求体**：

```json
{
  "deductionNo": "DED202606020001",  // String, 必填, 扣款单号
  "supplierId": 100,                 // Long, 必填, 供应商ID
  "sourceType": "NCR",               // String, 必填, 来源类型
  "sourceId": 1,                     // Long, 来源ID
  "deductionType": 1,                // Integer, 必填, 扣款类型（1质量 2延期 3短交 4其他）
  "deductionAmount": 2000.00,        // BigDecimal, 必填, 扣款金额
  "deductionReason": "来料不合格",     // String, 必填, 扣款原因
  "reconId": 1                       // Long, 对账单ID
}
```

**DeductionActionDTO 请求体**：

```json
{
  "remark": "确认扣款"  // String, 备注
}
```

**DeductionVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "deductionNo": "DED202606020001",
    "supplierId": 100,
    "sourceType": "NCR",
    "sourceId": 1,
    "deductionType": 1,
    "deductionAmount": 2000.00,
    "deductionReason": "来料不合格",
    "deductionStatus": 0,
    "reconId": 1,
    "createTime": "2026-06-02 08:00:00"
  }
}
```

**扣款状态枚举（DeductionStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| DRAFT | 0 | 草稿 |
| SUBMITTED | 1 | 已提交 |
| CONFIRMED | 2 | 已确认 |
| DISPUTED | 3 | 有异议 |
| BOOKED | 4 | 已入账 |

**扣款类型枚举（DeductionTypeEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| QUALITY | 1 | 质量扣款 |
| DELAY | 2 | 延期扣款 |
| SHORTAGE | 3 | 短交扣款 |
| OTHER | 4 | 其他扣款 |

---

### 4.7 付款管理（Payment）

**基础路径**：`/v1/payments`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/payments` | 分页查询付款单 | Query: PaymentQuery | `Result<PageResult<PaymentVO>>` |
| 2 | GET | `/v1/payments/{id}` | 查询付款单详情 | Path: id (Long) | `Result<PaymentVO>` |
| 3 | POST | `/v1/payments` | 新增付款单 | Body: PaymentCreateDTO | `Result<Long>` |
| 4 | POST | `/v1/payments/{id}/submit-approval` | 提交审批 | Path: id (Long) | `Result<Void>` |
| 5 | POST | `/v1/payments/{id}/schedule` | 付款排期 | Path: id, Body: PaymentScheduleDTO | `Result<Void>` |
| 6 | POST | `/v1/payments/{id}/pay` | 付款 | Path: id, Body: PaymentActionDTO | `Result<Void>` |
| 7 | POST | `/v1/payments/{id}/reject` | 拒绝付款 | Path: id, Body: PaymentActionDTO | `Result<Void>` |
| 8 | POST | `/v1/payments/{id}/cancel` | 取消付款单 | Path: id, Body: PaymentActionDTO | `Result<Void>` |

**PaymentQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| supplierId | Long | 供应商ID |
| reconId | Long | 对账单ID |
| invoiceId | Long | 发票ID |
| paymentStatus | Integer | 付款状态（0待付款 1审批中 2已审批 3已排期 4已付款 5已拒绝 6已取消） |
| approveStatus | Integer | 审批状态（0未提交 1审批中 2已通过 3已驳回） |
| scheduleDateStart | LocalDate | 计划付款日期起 |
| scheduleDateEnd | LocalDate | 计划付款日期止 |
| keyword | String | 关键词 |

**PaymentCreateDTO 请求体**：

```json
{
  "paymentNo": "PAY202606020001",    // String, 必填, 付款单号
  "invoiceId": 1,                    // Long, 发票ID
  "invoiceNo": "INV202606020001",    // String, 发票号码
  "reconId": 1,                      // Long, 对账单ID
  "supplierId": 100,                 // Long, 必填, 供应商ID
  "supplierName": "供应商A",          // String, 供应商名称
  "paymentAmount": 50000.00,         // BigDecimal, 必填, 付款金额
  "paymentMethod": 1,                // Integer, 付款方式（默认1）
  "paymentAccount": "6222000000001", // String, 付款账号
  "paymentBank": "中国银行",          // String, 付款银行
  "receiveAccount": "6222000000002", // String, 收款账号
  "receiveBank": "工商银行",          // String, 收款银行
  "scheduleDate": "2026-06-15",      // LocalDate, 必填, 计划付款日期
  "paymentTerms": "Net30",           // String, 付款条件
  "remark": ""                       // String, 备注
}
```

**PaymentScheduleDTO 请求体**：

```json
{
  "scheduleDate": "2026-06-20",  // LocalDate, 必填, 计划付款日期
  "paymentTerms": "Net45"        // String, 付款条件
}
```

**PaymentActionDTO 请求体**：

```json
{
  "remark": "付款完成"  // String, 备注
}
```

**PaymentVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY202606020001",
    "invoiceId": 1,
    "invoiceNo": "INV202606020001",
    "reconId": 1,
    "supplierId": 100,
    "supplierName": "供应商A",
    "paymentAmount": 50000.00,
    "paymentMethod": 1,
    "paymentAccount": "6222000000001",
    "paymentBank": "中国银行",
    "receiveAccount": "6222000000002",
    "receiveBank": "工商银行",
    "scheduleDate": "2026-06-15",
    "paymentTerms": "Net30",
    "paymentTime": null,
    "paymentStatus": 0,
    "receiptNo": null,
    "approveStatus": 0,
    "voucherNo": null,
    "remark": "",
    "createTime": "2026-06-02 08:00:00"
  }
}
```

**付款状态枚举（PaymentStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| PENDING | 0 | 待付款 |
| APPROVING | 1 | 审批中 |
| APPROVED | 2 | 已审批 |
| SCHEDULED | 3 | 已排期 |
| PAID | 4 | 已付款 |
| REJECTED | 5 | 已拒绝 |
| CANCELLED | 6 | 已取消 |

**付款审批状态枚举（ApproveStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| NOT_SUBMITTED | 0 | 未提交审批 |
| PENDING | 1 | 审批中 |
| APPROVED | 2 | 已通过 |
| REJECTED | 3 | 已驳回 |

---

### 4.8 付款审批管理（PaymentApproval）

**基础路径**：`/v1/payment-approvals`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/payment-approvals` | 分页查询审批记录 | Query: PaymentApprovalQuery | `Result<PageResult<PaymentApprovalVO>>` |
| 2 | GET | `/v1/payment-approvals/{id}` | 查询审批记录详情 | Path: id (Long) | `Result<PaymentApprovalVO>` |
| 3 | POST | `/v1/payment-approvals/submit` | 提交付款审批 | Body: PaymentApprovalSubmitDTO | `Result<Void>` |
| 4 | POST | `/v1/payment-approvals/{id}/approve` | 审批通过 | Path: id, Body: PaymentApprovalActionDTO | `Result<Void>` |
| 5 | POST | `/v1/payment-approvals/{id}/reject` | 审批驳回 | Path: id, Body: PaymentApprovalActionDTO | `Result<Void>` |

**PaymentApprovalQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| paymentId | Long | 付款单ID |
| supplierId | Long | 供应商ID |
| approvalStatus | Integer | 审批状态（0待审批 1已通过 2已驳回） |
| approvalLevel | Integer | 审批层级 |

**PaymentApprovalSubmitDTO 请求体**：

```json
{
  "paymentId": 1  // Long, 必填, 付款单ID
}
```

**PaymentApprovalActionDTO 请求体**：

```json
{
  "approvalStatus": 1,          // Integer, 必填, 审批结果（1通过 2驳回）
  "approveRemark": "同意付款"    // String, 审批备注
}
```

**PaymentApprovalVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentId": 1,
    "paymentNo": "PAY202606020001",
    "supplierId": 100,
    "supplierName": "供应商A",
    "paymentAmount": 50000.00,
    "approvalLevel": 1,
    "approvalStatus": 0,
    "approverId": null,
    "approverName": null,
    "approveRemark": null,
    "approveTime": null,
    "createTime": "2026-06-02 08:00:00"
  }
}
```

**审批状态枚举（ApprovalStatusEnum）**：

| 枚举 | code | 说明 |
|------|------|------|
| PENDING | 0 | 待审批 |
| APPROVED | 1 | 已通过 |
| REJECTED | 2 | 已驳回 |

---

### 4.9 供应商绩效（SupplierPerformance）

**基础路径**：`/v1/supplier-performances`

| # | 方法 | 路径 | 说明 | 请求参数 | 返回值 |
|---|------|------|------|----------|--------|
| 1 | GET | `/v1/supplier-performances` | 分页查询供应商绩效 | Query: SupplierPerformanceQuery | `Result<PageResult<SupplierPerformanceVO>>` |
| 2 | GET | `/v1/supplier-performances/{id}` | 查询绩效详情 | Path: id (Long) | `Result<SupplierPerformanceVO>` |
| 3 | POST | `/v1/supplier-performances` | 新增供应商绩效 | Body: SupplierPerformanceCreateDTO | `Result<Long>` |
| 4 | PUT | `/v1/supplier-performances/{id}` | 更新供应商绩效 | Path: id, Body: SupplierPerformanceUpdateDTO | `Result<Void>` |
| 5 | DELETE | `/v1/supplier-performances/{id}` | 删除供应商绩效 | Path: id (Long) | `Result<Void>` |

**SupplierPerformanceQuery 查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | Long | 页码（默认1） |
| pageSize | Long | 每页条数（默认10） |
| supplierId | Long | 供应商ID |
| evaluatePeriod | String | 考评周期 |
| keyword | String | 关键词 |

**SupplierPerformanceCreateDTO 请求体**：

```json
{
  "supplierId": 100,               // Long, 必填, 供应商ID
  "evaluatePeriod": "2026-Q2",     // String, 必填, 考评周期
  "qualityScore": 90,              // BigDecimal, 必填, 质量评分
  "deliveryScore": 85,             // BigDecimal, 必填, 交付评分
  "serviceScore": 88,              // BigDecimal, 必填, 服务评分
  "priceScore": 92,                // BigDecimal, 必填, 价格评分
  "qualifiedRate": 97.5,           // BigDecimal, 合格率
  "ontimeRate": 95.0,              // BigDecimal, 准时率
  "remark": ""                     // String, 备注
}
```

**SupplierPerformanceUpdateDTO 请求体**：

```json
{
  "evaluatePeriod": "2026-Q2",     // String, 必填, 考评周期
  "qualityScore": 92,              // BigDecimal, 质量评分
  "deliveryScore": 87,             // BigDecimal, 交付评分
  "serviceScore": 90,              // BigDecimal, 服务评分
  "priceScore": 93,                // BigDecimal, 价格评分
  "qualifiedRate": 98.0,           // BigDecimal, 合格率
  "ontimeRate": 96.0,              // BigDecimal, 准时率
  "remark": ""                     // String, 备注
}
```

**SupplierPerformanceVO 返回示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "supplierId": 100,
    "evaluatePeriod": "2026-Q2",
    "qualityScore": 90,
    "deliveryScore": 85,
    "serviceScore": 88,
    "priceScore": 92,
    "totalScore": 88.75,
    "qualifiedRate": 97.5,
    "ontimeRate": 95.0,
    "evaluateBy": 1,
    "evaluateTime": "2026-06-02 08:00:00",
    "remark": "",
    "createTime": "2026-06-02 08:00:00"
  }
}
```

---

## 五、前端已定义但后端暂未实现的API

> 以下API在前端代码中已有调用，但后端Controller暂未暴露对应端点，需后续补充实现。

| # | 方法 | 路径 | 说明 | 来源 |
|---|------|------|------|------|
| 1 | GET | `/v1/quality-inspections/{id}/lines` | 质检明细行 | quality.ts |
| 2 | POST | `/v1/quality-inspections/from-receipt/{receiptId}` | 从收货记录创建质检任务 | quality.ts |
| 3 | POST | `/v1/inspection-standards/{id}/toggle` | 切换检验标准状态 | quality.ts |
| 4 | GET | `/v1/reconciliations/{id}/lines` | 对账明细行 | settlement.ts |
| 5 | POST | `/v1/reconciliations/{id}/freeze` | 冻结对账单 | settlement.ts |
| 6 | POST | `/v1/reconciliations/{id}/unfreeze` | 解冻对账单 | settlement.ts |
| 7 | GET | `/v1/reconciliations/{id}/three-way-match` | 三单匹配数据 | settlement.ts |
| 8 | POST | `/v1/invoices/ocr` | OCR发票识别 | finance.ts |
| 9 | GET | `/v1/reconciliations/{reconId}/invoicable-amount` | 查询可开票金额 | finance.ts |
| 10 | GET | `/v1/invoices/{id}/payments` | 发票关联的付款记录 | finance.ts |
| 11 | GET | `/v1/payments/{id}/callback-logs` | 付款回传状态日志 | finance.ts |
| 12 | GET | `/v1/payments/{id}/invoices` | 付款关联的发票记录 | finance.ts |

---

## 六、API统计总览

| 模块 | 子模块 | 基础路径 | API数量 |
|------|--------|----------|---------|
| 质量协同 | 质量检验 | /v1/quality-inspections | 5 |
| 质量协同 | NCR质量异常 | /v1/nonconformance-reports | 8 |
| 质量协同 | 8D整改报告 | /v1/eight-d-reports | 11 |
| 质量协同 | 质量申诉 | /v1/quality-appeals | 6 |
| IQC检验标准 | 检验标准 | /v1/inspection-standards | 7 |
| IQC检验标准 | 检验项目 | /v1/inspection-standards/{standardId}/items | 4 |
| 财务结算 | 对账管理 | /v1/reconciliations | 5 |
| 财务结算 | 对账明细 | /v1/reconciliation-details | 5 |
| 财务结算 | 发票管理 | /v1/invoices | 7 |
| 财务结算 | 扣款管理 | /v1/deductions | 7 |
| 财务结算 | 付款管理 | /v1/payments | 8 |
| 财务结算 | 付款审批 | /v1/payment-approvals | 5 |
| 财务结算 | 供应商绩效 | /v1/supplier-performances | 5 |
| **合计** | **13个子模块** | - | **83** |
