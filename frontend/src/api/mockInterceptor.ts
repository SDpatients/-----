/**
 * Mock 数据拦截器 —— 当 VITE_USE_MOCK=true 时，拦截 axios 请求并返回本地 mock 数据。
 * 注册方式：在 main.ts 中调用 setupMock(request)。
 */
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import {
  suppliers, orders, asnNotices, qualityCases, settlements,
  timeline, portalTodos, attachments, importExportTasks,
  operationLogs, currentUser, permissions, supplierUser, supplierPermissions,
  supplierQualifications, rfqRecords, quoteRecords, quoteLineItems,
  integrationEndpoints, integrationLogs, syncTasks, materials,
  supplierAccounts, sysDicts, dictItems,
} from './mockData'
import { idEquals } from '@/utils/id'

// ---------- 工具函数 ----------

const isMockEnabled = (): boolean => import.meta.env.VITE_USE_MOCK === 'true'

const delay = <T>(data: T, ms = 120): Promise<T> =>
  new Promise((resolve) => setTimeout(() => resolve(data), ms))

/** 构造符合 ApiResult 格式的 axios 响应 */
const mockResponse = <T>(config: InternalAxiosRequestConfig, data: T): AxiosResponse => ({
  data: { code: 200, message: 'ok', data },
  status: 200,
  statusText: 'OK',
  headers: {} as any,
  config,
})

/** 简单的分页 */
const paginate = <T extends Record<string, unknown>>(
  records: T[],
  params: Record<string, unknown> | undefined,
): { records: T[]; total: number; pageSize: number; pageNum: number; pages: number } => {
  const pageNum = Number(params?.pageNum) || 1
  const pageSize = Number(params?.pageSize) || 10
  const keyword = (params?.keyword as string)?.trim().toLowerCase()
  const status = params?.status as string | undefined
  let filtered = records
  if (keyword) {
    filtered = filtered.filter((item) =>
      Object.values(item).join(' ').toLowerCase().includes(keyword),
    )
  }
  if (status && status !== 'all') {
    filtered = filtered.filter((item) => {
      const itemStatus = item.status
      // 兼容字符串和数字类型的比较
      return itemStatus === status || String(itemStatus) === status || itemStatus === Number(status)
    })
  }
  const start = (pageNum - 1) * pageSize
  return {
    records: filtered.slice(start, start + pageSize),
    total: filtered.length,
    pageSize,
    pageNum,
    pages: Math.ceil(filtered.length / pageSize),
  }
}

/** 从 URL 中提取路径参数（如 /v1/suppliers/3 → { id: '3' }） */
const extractParams = (pattern: string, url: string): Record<string, string> | null => {
  const patternParts = pattern.split('/')
  const urlParts = url.replace(/\?.*$/, '').split('/')
  if (patternParts.length !== urlParts.length) return null
  const params: Record<string, string> = {}
  for (let i = 0; i < patternParts.length; i++) {
    if (patternParts[i].startsWith(':')) {
      params[patternParts[i].slice(1)] = urlParts[i]
    } else if (patternParts[i] !== urlParts[i]) {
      return null
    }
  }
  return params
}

// ---------- 路由匹配 & 处理器 ----------

type Handler = (config: InternalAxiosRequestConfig, params: Record<string, string>) => Promise<AxiosResponse>

const routePatterns: { method: string; pattern: string; handler: Handler }[] = []

const on = (method: string, pattern: string, handler: Handler) => {
  routePatterns.push({ method: method.toUpperCase(), pattern, handler })
}

// ===================== Auth =====================
on('POST', '/auth/login', async (config) => {
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
  const isSupplier = body.username === 'supplier'
  return mockResponse(config, {
    token: isSupplier ? 'mock-token-supplier-2026' : 'mock-token-2026',
    user: isSupplier ? supplierUser : currentUser,
    permissions: isSupplier ? supplierPermissions : permissions,
  })
})
on('POST', '/auth/logout', async (config) => mockResponse(config, null))
on('GET', '/auth/info', async (config) => mockResponse(config, { ...currentUser, permissions }))

// ===================== Dashboard =====================
on('GET', '/v1/dashboard/metrics', async (config) =>
  mockResponse(config, [
    { name: '供应商总数', value: 4, unit: '家' },
    { name: '待处理订单', value: 12, unit: '笔' },
    { name: '本月质检批次', value: 36, unit: '批' },
    { name: '待对账金额', value: 286000, unit: '元' },
  ]),
)
on('GET', '/v1/dashboard/trends', async (config) =>
  mockResponse(config, [
    { period: 'W1', orderCount: 18, deliveryCount: 14, qualityIssueCount: 2, reconciliationCount: 5 },
    { period: 'W2', orderCount: 22, deliveryCount: 18, qualityIssueCount: 1, reconciliationCount: 7 },
    { period: 'W3', orderCount: 20, deliveryCount: 16, qualityIssueCount: 3, reconciliationCount: 6 },
    { period: 'W4', orderCount: 25, deliveryCount: 20, qualityIssueCount: 0, reconciliationCount: 8 },
  ]),
)
on('GET', '/v1/dashboard/risks', async (config) =>
  mockResponse(config, [
    { riskType: 'order_pending', title: '2 笔订单超 3 天未确认', count: 2, level: 'warning' },
    { riskType: 'delivery_delay', title: '1 笔 ASN 预计延迟到货', count: 1, level: 'danger' },
    { riskType: 'quality_issue', title: '3 条质检异常待处理', count: 3, level: 'warning' },
    { riskType: 'recon_dispute', title: '1 笔对账差异未解决', count: 1, level: 'info' },
  ]),
)
on('GET', '/v1/dashboard/supplier-performance', async (config) =>
  mockResponse(config, [
    { supplierId: 1, supplierName: '华东精密', deliveryRate: 96, qualityRate: 98, responseRate: 92, score: 96 },
  ]),
)

// ===================== Suppliers =====================
on('GET', '/v1/suppliers', async (config) => {
  const result = paginate(suppliers as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/suppliers/:id', async (config, params) => {
  const supplier = suppliers.find((s) => idEquals(s.id, params.id))
  return mockResponse(config, supplier || suppliers[0])
})
on('POST', '/v1/suppliers', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/suppliers/:id/audit', async (config) => mockResponse(config, null))

// ===================== Purchase Orders =====================
on('GET', '/v1/purchase-orders', async (config) => {
  const result = paginate(orders as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/purchase-orders/:id', async (config, params) => {
  const order = orders.find((o) => idEquals(o.id, params.id))
  return mockResponse(config, order || orders[0])
})
on('POST', '/v1/purchase-orders', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/purchase-orders/:id/publish', async (config) => mockResponse(config, null))
on('POST', '/v1/purchase-orders/:id/confirm', async (config) => mockResponse(config, null))
on('POST', '/v1/purchase-orders/:id/reject', async (config) => mockResponse(config, null))
on('POST', '/v1/purchase-orders/:id/cancel', async (config) => mockResponse(config, null))

// ===================== Delivery Notices (ASN) =====================
on('GET', '/v1/delivery-notices', async (config) => {
  const result = paginate(asnNotices as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/delivery-notices/:id', async (config, params) => {
  const asn = asnNotices.find((a) => idEquals(a.id, params.id))
  return mockResponse(config, asn || asnNotices[0])
})
on('POST', '/v1/delivery-notices', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/delivery-notices/:id/ship', async (config) => mockResponse(config, null))
on('POST', '/v1/delivery-notices/:id/arrive', async (config) => mockResponse(config, null))

// ===================== Quality =====================
on('GET', '/v1/quality-inspections', async (config) => {
  const result = paginate(qualityCases as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/quality-inspections/:id', async (config, params) => {
  const qc = qualityCases.find((q) => idEquals(q.id, params.id))
  return mockResponse(config, qc || qualityCases[0])
})
on('GET', '/v1/quality-inspections/:id/lines', async (config) =>
  mockResponse(config, []),
)
on('POST', '/v1/quality-inspections', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/quality-inspections/from-receipt/:receiptId', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/quality-inspections/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/quality-inspections/:id/handle', async (config) => mockResponse(config, null))

// ===================== Inspection Standards =====================
on('GET', '/v1/inspection-standards', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/inspection-standards/by-material/:materialCode', async (config) => mockResponse(config, null))
on('GET', '/v1/inspection-standards/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/inspection-standards', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/inspection-standards', async (config) => mockResponse(config, null))
on('POST', '/v1/inspection-standards/:id/status', async (config) => mockResponse(config, null))
on('DELETE', '/v1/inspection-standards/:id', async (config) => mockResponse(config, null))

// ===================== NCR =====================
on('GET', '/v1/nonconformance-reports', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/nonconformance-reports/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/nonconformance-reports', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/nonconformance-reports/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/nonconformance-reports/:id/handle', async (config) => mockResponse(config, null))
on('POST', '/v1/nonconformance-reports/:id/verify', async (config) => mockResponse(config, null))
on('POST', '/v1/nonconformance-reports/:id/close', async (config) => mockResponse(config, null))
on('POST', '/v1/nonconformance-reports/:id/upload-attachment', async (config) => mockResponse(config, Date.now()))

// ===================== 8D Reports =====================
on('GET', '/v1/eight-d-reports', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/eight-d-reports/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/eight-d-reports/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/audit', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/reject', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/close', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/step-submit', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/step-approve', async (config) => mockResponse(config, null))
on('POST', '/v1/eight-d-reports/:id/upload-attachment', async (config) => mockResponse(config, Date.now()))

// ===================== Quality Appeals =====================
on('GET', '/v1/quality-appeals', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/quality-appeals/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/quality-appeals', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/quality-appeals/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/quality-appeals/:id/approve', async (config) => mockResponse(config, null))
on('POST', '/v1/quality-appeals/:id/reject', async (config) => mockResponse(config, null))

// ===================== Reconciliations (Settlement) =====================
on('GET', '/v1/reconciliations', async (config) => {
  const result = paginate(settlements as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/reconciliations/:id', async (config, params) => {
  const st = settlements.find((s) => idEquals(s.id, params.id))
  return mockResponse(config, st || settlements[0])
})
on('POST', '/v1/reconciliations', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/reconciliations/:id/send', async (config) => mockResponse(config, null))
on('POST', '/v1/reconciliations/:id/confirm', async (config) => mockResponse(config, null))
on('GET', '/v1/reconciliations/:id/lines', async (config) =>
  mockResponse(config, []),
)
on('POST', '/v1/reconciliations/:id/freeze', async (config) => mockResponse(config, null))
on('POST', '/v1/reconciliations/:id/unfreeze', async (config) => mockResponse(config, null))
on('GET', '/v1/reconciliations/:id/three-way-match', async (config) =>
  mockResponse(config, { records: [], total: 0 }),
)
on('GET', '/v1/reconciliations/:reconId/invoicable-amount', async (config) =>
  mockResponse(config, { invoicableAmount: 0, totalAmount: 0, invoicedAmount: 0 }),
)

// ===================== Timeline / Todos / Attachments / Certificates =====================
on('GET', '/v1/business-timeline', async (config) => mockResponse(config, timeline))
on('GET', '/v1/todos', async (config) => {
  const result = paginate(portalTodos as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/todos/unread-count', async (config) => mockResponse(config, 3))
on('GET', '/v1/file-attachments', async (config) => {
  const businessType = (config.params?.businessType as string) || ''
  const businessId = config.params?.businessId
  let filtered = attachments
  if (businessType) {
    filtered = filtered.filter((a) => a.businessType === businessType)
  }
  if (businessId !== undefined && businessId !== null && businessId !== '') {
    filtered = filtered.filter((a) => String(a.businessId) === String(businessId))
  }
  return mockResponse(config, { records: filtered, total: filtered.length, pageSize: 100, pageNum: 1, pages: 1 })
})
on('POST', '/v1/file-attachments', async (config) => mockResponse(config, Date.now()))
on('GET', '/v1/file-attachments/:id/preview', async (config) => mockResponse(config, null))
on('GET', '/v1/file-attachments/:id/download', async (config) => mockResponse(config, null))
on('DELETE', '/v1/file-attachments/:id', async (config) => mockResponse(config, null))
on('PUT', '/v1/file-attachments/:id/rename', async (config) => mockResponse(config, null))
on('POST', '/v1/file-attachments/upload', async (config) => mockResponse(config, Date.now()))

// ===================== Export Tasks =====================
on('GET', '/v1/export-tasks', async (config) => {
  const result = paginate(importExportTasks as any, config.params)
  return mockResponse(config, result)
})
on('POST', '/v1/export-tasks', async (config) => mockResponse(config, Date.now()))
on('GET', '/v1/export-tasks/:id/download', async (config) => mockResponse(config, null))

// ===================== Import =====================
on('GET', '/v1/imports/:id/template', async (config) => mockResponse(config, null))
on('POST', '/v1/imports/:type/check', async (config) =>
  mockResponse(config, { fileId: Date.now(), importType: '', totalCount: 0, successCount: 0, errorCount: 0, errorFileId: 0 }),
)
on('POST', '/v1/imports/:type/submit', async (config) => mockResponse(config, Date.now()))
on('GET', '/v1/imports/errors/:id/download', async (config) => mockResponse(config, null))

// ===================== Audit Logs (Operation Log) =====================
on('GET', '/v1/audit-logs', async (config) => {
  const result = paginate(operationLogs as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/audit-logs/:id', async (config, params) => {
  const log = operationLogs.find((l) => idEquals(l.id, params.id))
  return mockResponse(config, log || operationLogs[0])
})

// ===================== Messages =====================
on('GET', '/v1/messages', async (config) => mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }))
on('GET', '/v1/messages/unread-count', async (config) => mockResponse(config, 2))
on('POST', '/v1/messages', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/messages/:id/read', async (config) => mockResponse(config, null))
on('POST', '/v1/messages/read-all', async (config) => mockResponse(config, null))

// ===================== Common =====================
on('GET', '/common/idempotent-token', async (config) =>
  mockResponse(config, `mock-idempotent-${Date.now()}`),
)

// ===================== Sys Config =====================
on('GET', '/v1/sys-config/:key', async (config) => mockResponse(config, 'true'))
on('POST', '/v1/sys-config', async (config) => mockResponse(config, null))

// ===================== Message Templates =====================
on('GET', '/v1/message-templates', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/message-templates/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/message-templates', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/message-templates/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/message-templates/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/message-templates/:id/toggle', async (config) => mockResponse(config, null))

// ===================== Third Party PO Config =====================
on('GET', '/v1/third-party-po-config', async (config) => mockResponse(config, []))
on('GET', '/v1/third-party-po-config/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/third-party-po-config', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/third-party-po-config/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/third-party-po-config/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/third-party-po-config/:id/enable', async (config) => mockResponse(config, null))
on('POST', '/v1/third-party-po-config/:id/disable', async (config) => mockResponse(config, null))
on('POST', '/v1/third-party-po/:configId/push/:orderId', async (config) => mockResponse(config, 'ok'))
on('POST', '/v1/third-party-po/:configId/fetch-latest', async (config) => mockResponse(config, 'ok'))

// ===================== VMI / Forecast =====================
on('GET', '/v1/vmi-inventories', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/vmi-inventories/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/vmi-inventories/sync', async (config) => mockResponse(config, null))
on('GET', '/v1/forecast-demands', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/forecast-demands/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/forecast-demands', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/forecast-demands/:id/publish', async (config) => mockResponse(config, null))
on('POST', '/v1/forecast-demands/:id/respond', async (config) => mockResponse(config, null))
on('POST', '/v1/forecast-demands/:id/close', async (config) => mockResponse(config, null))

// ===================== Finance (Invoices / Deductions / Payments / Performance) =====================
on('GET', '/v1/invoices', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/invoices/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/invoices/ocr', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/upload', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/verify', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/certify', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/void', async (config) => mockResponse(config, null))
on('GET', '/v1/invoices/:id/payments', async (config) =>
  mockResponse(config, []),
)

on('GET', '/v1/deductions', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/deductions/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/deductions', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/deductions/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/deductions/:id/confirm', async (config) => mockResponse(config, null))
on('POST', '/v1/deductions/:id/dispute', async (config) => mockResponse(config, null))
on('POST', '/v1/deductions/:id/book', async (config) => mockResponse(config, null))

on('GET', '/v1/payments', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/payments/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/payments', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/payments/:id/submit-approval', async (config) => mockResponse(config, null))
on('POST', '/v1/payments/:id/schedule', async (config) => mockResponse(config, null))
on('POST', '/v1/payments/:id/pay', async (config) => mockResponse(config, null))
on('POST', '/v1/payments/:id/reject', async (config) => mockResponse(config, null))
on('POST', '/v1/payments/:id/cancel', async (config) => mockResponse(config, null))
on('GET', '/v1/payments/:id/callback-logs', async (config) =>
  mockResponse(config, []),
)
on('GET', '/v1/payments/:id/invoices', async (config) =>
  mockResponse(config, []),
)

on('GET', '/v1/supplier-performances', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/supplier-performances/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/supplier-performances', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/supplier-performances/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/supplier-performances/:id', async (config) => mockResponse(config, null))

// ===================== Payment Approvals =====================
on('GET', '/v1/payment-approvals', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/payment-approvals/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/payment-approvals/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/payment-approvals/:id/approve', async (config) => mockResponse(config, null))
on('POST', '/v1/payment-approvals/:id/reject', async (config) => mockResponse(config, null))

// ===================== Reconciliation Details =====================
on('GET', '/v1/reconciliation-details', async (config) =>
  mockResponse(config, []),
)
on('GET', '/v1/reconciliation-details/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/reconciliation-details', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/reconciliation-details/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/reconciliation-details/:id', async (config) => mockResponse(config, null))

// ===================== Supplier Qualifications =====================
on('GET', '/v1/supplier-qualifications', async (config) => {
  const result = paginate(supplierQualifications as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/supplier-qualifications/:id', async (config, params) => {
  const qual = supplierQualifications.find((q) => idEquals(q.id, params.id))
  return mockResponse(config, qual || supplierQualifications[0])
})
on('POST', '/v1/supplier-qualifications', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/supplier-qualifications/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/supplier-qualifications/:id', async (config) => mockResponse(config, null))

// ===================== RFQ =====================
on('GET', '/v1/rfqs', async (config) => {
  const result = paginate(rfqRecords as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/rfqs/:id', async (config, params) => {
  const rfq = rfqRecords.find((r) => idEquals(r.id, params.id))
  return mockResponse(config, rfq || rfqRecords[0])
})
on('POST', '/v1/rfqs', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/rfqs/:id/publish', async (config) => mockResponse(config, null))
on('POST', '/v1/rfqs/:id/close', async (config) => mockResponse(config, null))
on('POST', '/v1/rfqs/:id/cancel', async (config) => mockResponse(config, null))

// ===================== Quotes =====================
on('GET', '/v1/quotes', async (config) => {
  const result = paginate(quoteRecords as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/quotes/:id', async (config, params) => {
  const quote = quoteRecords.find((q) => idEquals(q.id, params.id))
  return mockResponse(config, quote || quoteRecords[0])
})
on('GET', '/v1/quotes/:id/lines', async (config, params) => {
  const lines = quoteLineItems.filter((item) => idEquals(item.quoteId, params.id))
  return mockResponse(config, lines)
})
on('POST', '/v1/quotes', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/quotes/:id/accept', async (config) => mockResponse(config, null))
on('POST', '/v1/quotes/:id/reject', async (config) => mockResponse(config, null))
on('POST', '/v1/quotes/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/quotes/:id/withdraw', async (config) => mockResponse(config, null))

// ===================== Integration =====================
on('GET', '/v1/integration-endpoints', async (config) => {
  const result = paginate(integrationEndpoints as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/integration-endpoints/:id', async (config, params) => {
  const ep = integrationEndpoints.find((e) => idEquals(e.id, params.id))
  return mockResponse(config, ep || integrationEndpoints[0])
})
on('POST', '/v1/integration-endpoints', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/integration-endpoints/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/integration-endpoints/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/integration-endpoints/:id/enable', async (config) => mockResponse(config, null))
on('POST', '/v1/integration-endpoints/:id/disable', async (config) => mockResponse(config, null))

on('GET', '/v1/integration-logs', async (config) => {
  const result = paginate(integrationLogs as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/integration-logs/:id', async (config, params) => {
  const log = integrationLogs.find((l) => idEquals(l.id, params.id))
  return mockResponse(config, log || integrationLogs[0])
})

on('GET', '/v1/integration-sync-tasks', async (config) => {
  const result = paginate(syncTasks as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/integration-sync-tasks/:id', async (config, params) => {
  const task = syncTasks.find((t) => idEquals(t.id, params.id))
  return mockResponse(config, task || syncTasks[0])
})
on('POST', '/v1/integration-sync-tasks/:id/retry', async (config) => mockResponse(config, null))

// ===================== Materials =====================
on('GET', '/v1/materials', async (config) => {
  let filtered = [...(materials as any[])]
  const category = config.params?.category as string
  if (category) {
    filtered = filtered.filter((m) => m.category === category)
  }
  const result = paginate(filtered, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/materials/:id', async (config, params) => {
  const material = materials.find((m) => idEquals(m.id, params.id))
  return mockResponse(config, material || materials[0])
})
on('POST', '/v1/materials', async (config) => {
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
  const newId = Math.max(...(materials as any[]).map((m: any) => m.id), 10) + 1
  const newMaterial = {
    id: newId,
    materialCode: body.materialCode,
    materialName: body.materialName,
    spec: body.spec || '',
    unit: body.unit || 'PCS',
    category: body.category || '',
    status: 1,
    createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
  }
  ;(materials as any[]).push(newMaterial)
  return mockResponse(config, newId)
})
on('PUT', '/v1/materials/:id', async (config, params) => {
  const material = materials.find((m) => idEquals(m.id, params.id))
  if (material) {
    const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
    if (body.materialCode) material.materialCode = body.materialCode
    if (body.materialName) material.materialName = body.materialName
    if (body.spec !== undefined) material.spec = body.spec
    if (body.unit !== undefined) material.unit = body.unit
    if (body.category !== undefined) material.category = body.category
  }
  return mockResponse(config, null)
})
on('DELETE', '/v1/materials/:id', async (config, params) => {
  const idx = materials.findIndex((m) => idEquals(m.id, params.id))
  if (idx >= 0) materials.splice(idx, 1)
  return mockResponse(config, null)
})

// ===================== Dict =====================
on('GET', '/v1/dict/list', async (config) => mockResponse(config, [...sysDicts]))

on('POST', '/v1/dict', async (config) => {
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
  const newId = Math.max(...sysDicts.map(d => d.id), 0) + 1
  const newDict = { id: newId, dictName: body.dictName, dictCode: body.dictCode, description: body.description || '', status: 1, createTime: new Date().toISOString().replace('T', ' ').slice(0, 19) }
  sysDicts.push(newDict)
  dictItems[body.dictCode] = []
  return mockResponse(config, newId)
})

on('DELETE', '/v1/dict/:id', async (config, params) => {
  const idx = sysDicts.findIndex(d => idEquals(d.id, params.id))
  if (idx > -1) {
    const code = sysDicts[idx].dictCode
    sysDicts.splice(idx, 1)
    delete dictItems[code]
  }
  return mockResponse(config, null)
})

on('GET', '/v1/dict/code/:dictCode', async (config, params) => {
  const items = dictItems[params.dictCode] || []
  return mockResponse(config, items.filter((i: any) => i.status === 1))
})

on('GET', '/v1/dict/:dictId/items', async (config, params) => {
  const dict = sysDicts.find(d => d.id === Number(params.dictId))
  if (!dict) return mockResponse(config, [])
  return mockResponse(config, dictItems[dict.dictCode] || [])
})

on('POST', '/v1/dict/items', async (config) => {
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
  const dict = sysDicts.find(d => d.id === Number(body.dictId))
  if (!dict) return mockResponse(config, null)
  if (!dictItems[dict.dictCode]) dictItems[dict.dictCode] = []
  const allItems = dictItems[dict.dictCode]
  const newId = Math.max(...allItems.map((i: any) => i.id), ...Object.values(dictItems).flat().map((i: any) => i.id), 0) + 1
  const newItem = { id: newId, dictId: Number(body.dictId), itemLabel: body.itemLabel, itemValue: body.itemValue, sort: body.sort || 0, description: body.description || '', status: 1 }
  allItems.push(newItem)
  return mockResponse(config, newId)
})

on('PUT', '/v1/dict/items/:id', async (config, params) => {
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
  for (const code of Object.keys(dictItems)) {
    const item = dictItems[code].find((i: any) => idEquals(i.id, params.id))
    if (item) {
      if (body.itemLabel) (item as any).itemLabel = body.itemLabel
      if (body.itemValue) (item as any).itemValue = body.itemValue
      if (body.sort !== undefined) (item as any).sort = body.sort
      if (body.description !== undefined) (item as any).description = body.description
      if (body.status !== undefined) (item as any).status = body.status
      break
    }
  }
  return mockResponse(config, null)
})

on('DELETE', '/v1/dict/items/:id', async (config, params) => {
  for (const code of Object.keys(dictItems)) {
    const idx = dictItems[code].findIndex((i: any) => idEquals(i.id, params.id))
    if (idx > -1) { dictItems[code].splice(idx, 1); break }
  }
  return mockResponse(config, null)
})

// ===================== Supplier Accounts =====================
on('GET', '/v1/supplier-accounts', async (config) => {
  const supplierId = config.params?.supplierId as string | number | undefined
  let filtered = supplierAccounts
  if (supplierId) {
    filtered = filtered.filter((a) => idEquals(a.supplierId, supplierId))
  }
  const keyword = config.params?.keyword as string
  if (keyword) {
    const kw = keyword.toLowerCase()
    filtered = filtered.filter((a) =>
      a.username.includes(kw) || a.realName.includes(kw) || a.phone.includes(kw),
    )
  }
  const result = paginate(filtered as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/supplier-accounts/:id', async (config, params) => {
  const account = supplierAccounts.find((a) => idEquals(a.id, params.id))
  return mockResponse(config, account || supplierAccounts[0])
})
on('POST', '/v1/supplier-accounts', async (config) => {
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data || {})
  const newId = Math.max(...supplierAccounts.map((a) => a.id), 300) + 1
  const newAccount = {
    id: newId,
    username: body.username,
    realName: body.realName,
    phone: body.phone || '',
    email: body.email || '',
    userType: 2,
    supplierId: body.supplierId,
    status: 1,
    lastLoginTime: '',
    createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    remark: body.remark || '',
  }
  supplierAccounts.push(newAccount)
  return mockResponse(config, newId)
})
on('PUT', '/v1/supplier-accounts/:id/password', async (config) => mockResponse(config, null))
on('PUT', '/v1/supplier-accounts/:id/status', async (config, params) => {
  const account = supplierAccounts.find((a) => idEquals(a.id, params.id))
  if (account) {
    const status = config.params?.status ? Number(config.params.status) : 0
    account.status = status
  }
  return mockResponse(config, null)
})

// ---------- 拦截器注册 ----------

/**
 * 在 axios 实例上注册 mock 请求拦截器。
 * 当 VITE_USE_MOCK=true 时：
 *   - 匹配预定义路由 → 返回 mock 数据
 *   - 未匹配的路由 → 兜底返回空数据，绝不穿透到后端（避免后端不可达/401 导致跳登录）
 */
export const setupMock = (instance: AxiosInstance): void => {
  if (!isMockEnabled()) {
    return
  }

  instance.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
    const method = (config.method || 'GET').toUpperCase()
    const url = config.url || ''

    for (const route of routePatterns) {
      if (route.method !== method) continue
      const params = extractParams(route.pattern, url)
      if (params === null) continue

      // 匹配成功，用 mock adapter 接管请求
      const adapter = async () => {
        const response = await route.handler(config, params)
        return delay(response)
      }
      return { ...config, adapter } as InternalAxiosRequestConfig
    }

    // 未匹配：兜底返回空数据，禁止穿透到后端
    const fallbackAdapter = async () => {
      const emptyData = method === 'GET' ? [] : null
      return delay(mockResponse(config, emptyData))
    }
    return { ...config, adapter: fallbackAdapter } as InternalAxiosRequestConfig
  })
}