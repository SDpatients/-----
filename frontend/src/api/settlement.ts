import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, Settlement } from '@/types/business'

import type { ReconDetailLine, ThreeWayMatchData } from './mockData'

export const settlementApi = {
  page: async (params: PageQuery) => asPage<Settlement>(await request.get<ApiPage<Settlement>, ApiPage<Settlement>>('/v1/reconciliations', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) => request.get<Settlement, Settlement>(`/v1/reconciliations/${id}`),
  lines: (id: number | string) => request.get<ReconDetailLine[], ReconDetailLine[]>(`/v1/reconciliations/${id}/lines`),
  create: (data: Partial<Settlement>) => request.post<number, number>('/v1/reconciliations', data),
  send: (id: number | string) => request.post<void, void>(`/v1/reconciliations/${id}/send`),
  confirm: (id: number | string, data = {}) => request.post<void, void>(`/v1/reconciliations/${id}/confirm`, data),
  freeze: (id: number | string, data?: Record<string, unknown>) => request.post<void, void>(`/v1/reconciliations/${id}/freeze`, data),
  unfreeze: (id: number | string, data?: Record<string, unknown>) => request.post<void, void>(`/v1/reconciliations/${id}/unfreeze`, data),
  /** 三单匹配数据：订单行 vs 收货行 vs 发票行 */
  threeWayMatch: (id: number | string) => request.get<ThreeWayMatchData, ThreeWayMatchData>(`/v1/reconciliations/${id}/three-way-match`),
}
