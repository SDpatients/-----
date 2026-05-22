import type { PageQuery, PageResult } from '@/types/business'
import { asnNotices, currentUser, orders, permissions, qualityCases, settlements, suppliers, timeline } from './mockData'

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
  login: () => wait({ token: 'mock-token-2026', user: currentUser, permissions }),
  getCurrentUser: () => wait({ user: currentUser, permissions }),
  getSupplierPage: (query: PageQuery) => wait(page(suppliers, query)),
  getOrderPage: (query: PageQuery) => wait(page(orders, query)),
  getAsnPage: (query: PageQuery) => wait(page(asnNotices, query)),
  getQualityPage: (query: PageQuery) => wait(page(qualityCases, query)),
  getSettlementPage: (query: PageQuery) => wait(page(settlements, query)),
  getSupplier: (id: number) => wait(suppliers.find((item) => item.id === id)),
  getOrder: (id: number) => wait(orders.find((item) => item.id === id)),
  getAsn: (id: number) => wait(asnNotices.find((item) => item.id === id)),
  getQuality: (id: number) => wait(qualityCases.find((item) => item.id === id)),
  getSettlement: (id: number) => wait(settlements.find((item) => item.id === id)),
  getTimeline: () => wait(timeline),
}
