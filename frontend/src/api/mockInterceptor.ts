/**
 * Mock 数据拦截器 —— 当 VITE_USE_MOCK=true 时，拦截 axios 请求并返回本地 mock 数据。
 * 注册方式：在 main.ts 中调用 setupMock(request)。
 */
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import {
  suppliers, orders, asnNotices, qualityCases, settlements,
  timeline, portalTodos, attachments, importExportTasks,
  operationLogs, currentUser, permissions, supplierUser, supplierPermissions,
  supplierQualifications, rfqRecords, quoteRecords,
  integrationEndpoints, integrationLogs, syncTasks, materials,
} from './mockData'

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
  const supplier = suppliers.find((s) => s.id === Number(params.id))
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
  const order = orders.find((o) => o.id === Number(params.id))
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
  const asn = asnNotices.find((a) => a.id === Number(params.id))
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
  const qc = qualityCases.find((q) => q.id === Number(params.id))
  return mockResponse(config, qc || qualityCases[0])
})
on('POST', '/v1/quality-inspections', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/quality-inspections/:id/submit', async (config) => mockResponse(config, null))
on('POST', '/v1/quality-inspections/:id/handle', async (config) => mockResponse(config, null))

// ===================== Reconciliations (Settlement) =====================
on('GET', '/v1/reconciliations', async (config) => {
  const result = paginate(settlements as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/reconciliations/:id', async (config, params) => {
  const st = settlements.find((s) => s.id === Number(params.id))
  return mockResponse(config, st || settlements[0])
})
on('POST', '/v1/reconciliations', async (config) => mockResponse(config, Date.now()))
on('POST', '/v1/reconciliations/:id/send', async (config) => mockResponse(config, null))
on('POST', '/v1/reconciliations/:id/confirm', async (config) => mockResponse(config, null))

// ===================== Timeline / Todos / Attachments / Certificates =====================
on('GET', '/v1/business-timeline', async (config) => mockResponse(config, timeline))
on('GET', '/v1/todos', async (config) => {
  const result = paginate(portalTodos as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/todos/unread-count', async (config) => mockResponse(config, 3))
on('GET', '/v1/file-attachments', async (config) => mockResponse(config, { records: attachments, total: attachments.length, pageSize: 100, pageNum: 1, pages: 1 }))
on('POST', '/v1/file-attachments', async (config) => mockResponse(config, Date.now()))
on('GET', '/v1/file-attachments/:id/preview', async (config) => mockResponse(config, null))
on('GET', '/v1/file-attachments/:id/download', async (config) => mockResponse(config, null))
on('DELETE', '/v1/file-attachments/:id', async (config) => mockResponse(config, null))
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
  const log = operationLogs.find((l) => l.id === Number(params.id))
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
on('POST', '/v1/invoices/:id/upload', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/verify', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/certify', async (config) => mockResponse(config, null))
on('POST', '/v1/invoices/:id/cancel', async (config) => mockResponse(config, null))

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
on('POST', '/v1/payments/:id/pay', async (config) => mockResponse(config, null))
on('POST', '/v1/payments/:id/reject', async (config) => mockResponse(config, null))

on('GET', '/v1/performances', async (config) =>
  mockResponse(config, { records: [], total: 0, pageSize: 10, pageNum: 1, pages: 0 }),
)
on('GET', '/v1/performances/:id', async (config) => mockResponse(config, null))
on('POST', '/v1/performances', async (config) => mockResponse(config, Date.now()))
on('PUT', '/v1/performances/:id', async (config) => mockResponse(config, null))
on('DELETE', '/v1/performances/:id', async (config) => mockResponse(config, null))

// ===================== Supplier Qualifications =====================
on('GET', '/v1/supplier-qualifications', async (config) => {
  const result = paginate(supplierQualifications as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/supplier-qualifications/:id', async (config, params) => {
  const qual = supplierQualifications.find((q) => q.id === Number(params.id))
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
  const rfq = rfqRecords.find((r) => r.id === Number(params.id))
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
  const quote = quoteRecords.find((q) => q.id === Number(params.id))
  return mockResponse(config, quote || quoteRecords[0])
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
  const ep = integrationEndpoints.find((e) => e.id === Number(params.id))
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
  const log = integrationLogs.find((l) => l.id === Number(params.id))
  return mockResponse(config, log || integrationLogs[0])
})

on('GET', '/v1/integration-sync-tasks', async (config) => {
  const result = paginate(syncTasks as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/integration-sync-tasks/:id', async (config, params) => {
  const task = syncTasks.find((t) => t.id === Number(params.id))
  return mockResponse(config, task || syncTasks[0])
})
on('POST', '/v1/integration-sync-tasks/:id/retry', async (config) => mockResponse(config, null))

// ===================== Materials =====================
on('GET', '/v1/materials', async (config) => {
  const result = paginate(materials as any, config.params)
  return mockResponse(config, result)
})
on('GET', '/v1/materials/:id', async (config, params) => {
  const material = materials.find((m) => m.id === Number(params.id))
  return mockResponse(config, material || materials[0])
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
    console.debug('[Mock] Mock 模式未启用（VITE_USE_MOCK ≠ true），所有请求走真实网络')
    return
  }

  console.debug('[Mock] Mock 模式已启用，拦截生效中')

  instance.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
    const method = (config.method || 'GET').toUpperCase()
    const url = config.url || ''

    for (const route of routePatterns) {
      if (route.method !== method) continue
      const params = extractParams(route.pattern, url)
      if (params === null) continue

      // 匹配成功，用 mock adapter 接管请求
      console.debug(`[Mock] ✓ ${method} ${url} → 命中 mock 处理器`)
      const adapter = async () => {
        const response = await route.handler(config, params)
        return delay(response)
      }
      return { ...config, adapter } as InternalAxiosRequestConfig
    }

    // 未匹配：兜底返回空数据，禁止穿透到后端
    console.debug(`[Mock] ○ ${method} ${url} → 未命中，兜底返回空数据`)
    const fallbackAdapter = async () => {
      const emptyData = method === 'GET' ? [] : null
      return delay(mockResponse(config, emptyData))
    }
    return { ...config, adapter: fallbackAdapter } as InternalAxiosRequestConfig
  })
}