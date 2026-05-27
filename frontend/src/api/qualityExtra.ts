import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, NcrRecord, EightDReport, QualityAppeal } from '@/types/business'

export const ncrApi = {
  page: async (params: PageQuery) =>
    asPage<NcrRecord>(await request.get<ApiPage<NcrRecord>, ApiPage<NcrRecord>>('/v1/nonconformance-reports', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<NcrRecord, NcrRecord>(`/v1/nonconformance-reports/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/nonconformance-reports', data),

  submit: (id: number | string) => request.post<void, void>(`/v1/nonconformance-reports/${id}/submit`),

  /** 处理NCR */
  handle: (id: number | string, data: Record<string, unknown>) =>
    request.post<void, void>(`/v1/nonconformance-reports/${id}/handle`, data),

  verify: (id: number | string) => request.post<void, void>(`/v1/nonconformance-reports/${id}/verify`),

  close: (id: number | string) => request.post<void, void>(`/v1/nonconformance-reports/${id}/close`),
}

export const eightDApi = {
  page: async (params: PageQuery) =>
    asPage<EightDReport>(await request.get<ApiPage<EightDReport>, ApiPage<EightDReport>>('/v1/eight-d-reports', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<EightDReport, EightDReport>(`/v1/eight-d-reports/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/eight-d-reports', data),

  update: (id: number | string, data: Record<string, unknown>) => request.put<void, void>(`/v1/eight-d-reports/${id}`, data),

  submit: (id: number | string) => request.post<void, void>(`/v1/eight-d-reports/${id}/submit`),

  review: (id: number | string) => request.post<void, void>(`/v1/eight-d-reports/${id}/audit`),

  return: (id: number | string, data?: Record<string, unknown>) => request.post<void, void>(`/v1/eight-d-reports/${id}/reject`, data || {}),

  close: (id: number | string) => request.post<void, void>(`/v1/eight-d-reports/${id}/close`),
}

export const qualityAppealApi = {
  page: async (params: PageQuery) =>
    asPage<QualityAppeal>(await request.get<ApiPage<QualityAppeal>, ApiPage<QualityAppeal>>('/v1/quality-appeals', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<QualityAppeal, QualityAppeal>(`/v1/quality-appeals/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/quality-appeals', data),

  submit: (id: number | string) => request.post<void, void>(`/v1/quality-appeals/${id}/submit`),

  approve: (id: number | string) => request.post<void, void>(`/v1/quality-appeals/${id}/approve`),

  reject: (id: number | string, data?: Record<string, unknown>) => request.post<void, void>(`/v1/quality-appeals/${id}/reject`, data || {}),
}