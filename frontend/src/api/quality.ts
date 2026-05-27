import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, QualityCase, InspectionStandard } from '@/types/business'

import type { InspectionDetailLine } from './mockData'

export const qualityApi = {
  page: async (params: PageQuery) => asPage<QualityCase>(await request.get<ApiPage<QualityCase>, ApiPage<QualityCase>>('/v1/quality-inspections', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) => request.get<QualityCase, QualityCase>(`/v1/quality-inspections/${id}`),
  lines: (id: number | string) => request.get<InspectionDetailLine[], InspectionDetailLine[]>(`/v1/quality-inspections/${id}/lines`),
  create: (data: Partial<QualityCase>) => request.post<number, number>('/v1/quality-inspections', data),
  /** 从收货记录创建质检任务 */
  createFromReceipt: (receiptId: number | string, data: Record<string, unknown>) => request.post<number, number>(`/v1/quality-inspections/from-receipt/${receiptId}`, data),
  submit: (id: number | string, data = {}) => request.post<void, void>(`/v1/quality-inspections/${id}/submit`, data),
  handle: (id: number | string, data: Record<string, unknown>) => request.post<void, void>(`/v1/quality-inspections/${id}/handle`, data),
}

/** IQC检验标准 */
export const inspectionStandardApi = {
  page: async (params: PageQuery) => asPage<InspectionStandard>(await request.get<ApiPage<InspectionStandard>, ApiPage<InspectionStandard>>('/v1/inspection-standards', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) => request.get<InspectionStandard, InspectionStandard>(`/v1/inspection-standards/${id}`),
  create: (data: Partial<InspectionStandard>) => request.post<number, number>('/v1/inspection-standards', data),
  update: (id: number | string, data: Partial<InspectionStandard>) => request.put<void, void>(`/v1/inspection-standards/${id}`, data),
  delete: (id: number | string) => request.delete<void, void>(`/v1/inspection-standards/${id}`),
  toggleStatus: (id: number | string) => request.post<void, void>(`/v1/inspection-standards/${id}/toggle`),
}
