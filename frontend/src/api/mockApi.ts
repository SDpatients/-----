import type { PageQuery, PageResult } from '@/types/business'
import { asnNotices, attachments, currentUser, importExportTasks, operationLogs, orders, permissions, portalTodos, qualityCases, settlements, supplierPermissions, suppliers, supplierUser, timeline } from './mockData'
import { logisticsApi } from './logistics'
import { orderApi } from './order'
import { qualityApi } from './quality'
import { qualificationApi } from './qualification'
import { settlementApi } from './settlement'
import { supplierApi } from './supplier'
import { toAsn, toOrder, toQuality, toSettlement, toSupplier } from './adapters'
import { asPage } from '@/utils/apiNormalize'

const wait = <T>(data: T) => new Promise<T>((resolve) => window.setTimeout(() => resolve(data), 180))

const page = <T extends object & { status?: string }>(records: T[], query: PageQuery): PageResult<T> => {
  const keyword = query.keyword?.trim().toLowerCase()
  const filtered = records.filter((item) => {
    const text = Object.values(item as Record<string, unknown>).join(' ').toLowerCase()
    const matchKeyword = !keyword || text.includes(keyword)
    const matchStatus = !query.status || item.status === query.status
    return matchKeyword && matchStatus
  })
  const start = (query.pageNum - 1) * query.pageSize
  return { records: filtered.slice(start, start + query.pageSize), total: filtered.length, pageNum: query.pageNum, pageSize: query.pageSize }
}

export const mockApi = {
  login: (username = 'admin') => wait(username === 'supplier' ? { token: 'mock-token-supplier-2026', user: supplierUser, permissions: supplierPermissions } : { token: 'mock-token-2026', user: currentUser, permissions }),
  getCurrentUser: (token?: string) => wait(token?.includes('supplier') ? { user: supplierUser, permissions: supplierPermissions } : { user: currentUser, permissions }),
  getSupplierPage: async (query: PageQuery) => {
    const result = await supplierApi.page(query)
    const normalized = asPage<unknown>(result, query.pageNum, query.pageSize)
    return { ...normalized, records: normalized.records.map(toSupplier) }
  },
  getOrderPage: async (query: PageQuery) => {
    const result = await orderApi.page(query)
    const normalized = asPage<unknown>(result, query.pageNum, query.pageSize)
    return { ...normalized, records: normalized.records.map(toOrder) }
  },
  getAsnPage: async (query: PageQuery) => {
    const result = await logisticsApi.deliveryPage(query)
    const normalized = asPage<unknown>(result, query.pageNum, query.pageSize)
    return { ...normalized, records: normalized.records.map(toAsn) }
  },
  getQualityPage: async (query: PageQuery) => {
    const result = await qualityApi.page(query)
    const normalized = asPage<unknown>(result, query.pageNum, query.pageSize)
    return { ...normalized, records: normalized.records.map(toQuality) }
  },
  getSettlementPage: async (query: PageQuery) => {
    const result = await settlementApi.page(query)
    const normalized = asPage<unknown>(result, query.pageNum, query.pageSize)
    return { ...normalized, records: normalized.records.map(toSettlement) }
  },
  getSupplier: async (id: number | string) => toSupplier(await supplierApi.detail(id)),
  getOrder: async (id: number | string) => toOrder(await orderApi.detail(id)),
  getAsn: async (id: number | string) => toAsn(await logisticsApi.deliveryDetail(id)),
  getQuality: async (id: number | string) => toQuality(await qualityApi.detail(id)),
  getSettlement: async (id: number | string) => toSettlement(await settlementApi.detail(id)),
  getTimeline: () => wait(timeline),
  getPortalTodos: () => wait(portalTodos),
  getAttachments: () => wait(attachments),
  getImportExportTasks: () => wait(importExportTasks),
  getOperationLogs: () => wait(operationLogs),
  getCertificates: async (supplierId: number) => {
    try {
      const result = await qualificationApi.page({ supplierId, pageNum: 1, pageSize: 100 } as any)
      return result.records.map(r => ({
        id: r.id,
        name: r.qualName,
        certNo: r.qualNo || '',
        expireDate: r.validEnd || '',
        status: r.status === 1 ? 'active' : r.status === 0 ? 'pending' : 'expired',
      }))
    } catch {
      return [] // 后端不可用时返回空数组
    }
  },
  getOrderDetails: async (orderId: number) => {
    try {
      return await orderApi.lines(orderId)
    } catch {
      return [] // 后端不可用时返回空数组
    }
  },
  getDeliveryDetails: async (deliveryId: number) => {
    try {
      return await logisticsApi.deliveryLines(deliveryId)
    } catch {
      return []
    }
  },
  getInspectionDetails: async (inspectionId: number) => {
    try {
      return await qualityApi.lines(inspectionId)
    } catch {
      return []
    }
  },
  getReconDetails: async (reconId: number) => {
    try {
      return await settlementApi.lines(reconId)
    } catch {
      return []
    }
  },
}
