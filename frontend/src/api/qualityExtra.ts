import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, NcrRecord, EightDReport, QualityAppeal } from '@/types/business'

export interface NcrActionDTO {
  remark?: string
  handleMethod?: number
  handleDetail?: string
  handleRemark?: string
}

export interface EightDActionDTO {
  remark?: string
  currentStep?: number
  stepDueDate?: string
  stepContent?: string
}

export interface QualityAppealAuditDTO {
  auditRemark: string
}

export const ncrApi = {
  page: async (params: PageQuery) =>
    asPage<NcrRecord>(await request.get<ApiPage<NcrRecord>, ApiPage<NcrRecord>>('/v1/nonconformance-reports', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<NcrRecord, NcrRecord>(`/v1/nonconformance-reports/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/nonconformance-reports', data),

  submit: (id: number | string, data?: NcrActionDTO) => request.post<void, void>(`/v1/nonconformance-reports/${id}/submit`, data || {}),

  handle: (id: number | string, data: NcrActionDTO) =>
    request.post<void, void>(`/v1/nonconformance-reports/${id}/handle`, data),

  verify: (id: number | string, data?: NcrActionDTO) => request.post<void, void>(`/v1/nonconformance-reports/${id}/verify`, data || {}),

  close: (id: number | string, data?: NcrActionDTO) => request.post<void, void>(`/v1/nonconformance-reports/${id}/close`, data || {}),
}

export const eightDApi = {
  page: async (params: PageQuery) =>
    asPage<EightDReport>(await request.get<ApiPage<EightDReport>, ApiPage<EightDReport>>('/v1/eight-d-reports', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<EightDReport, EightDReport>(`/v1/eight-d-reports/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/eight-d-reports', data),

  update: (id: number | string, data: Record<string, unknown>) => request.put<void, void>(`/v1/eight-d-reports/${id}`, data),

  submit: (id: number | string, data?: EightDActionDTO) => request.post<void, void>(`/v1/eight-d-reports/${id}/submit`, data || {}),

  review: (id: number | string, data?: EightDActionDTO) => request.post<void, void>(`/v1/eight-d-reports/${id}/audit`, data || {}),

  return: (id: number | string, data?: EightDActionDTO) => request.post<void, void>(`/v1/eight-d-reports/${id}/reject`, data || {}),

  close: (id: number | string, data?: EightDActionDTO) => request.post<void, void>(`/v1/eight-d-reports/${id}/close`, data || {}),

  stepSubmit: (id: number | string, data: EightDActionDTO) => request.post<void, void>(`/v1/eight-d-reports/${id}/step-submit`, data),

  stepApprove: (id: number | string, data?: EightDActionDTO) => request.post<void, void>(`/v1/eight-d-reports/${id}/step-approve`, data || {}),

  uploadAttachment: (id: number | string, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<number, number>(`/v1/eight-d-reports/${id}/upload-attachment`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

export const qualityAppealApi = {
  page: async (params: PageQuery) =>
    asPage<QualityAppeal>(await request.get<ApiPage<QualityAppeal>, ApiPage<QualityAppeal>>('/v1/quality-appeals', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<QualityAppeal, QualityAppeal>(`/v1/quality-appeals/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/quality-appeals', data),

  submit: (id: number | string) => request.post<void, void>(`/v1/quality-appeals/${id}/submit`),

  approve: (id: number | string, data: QualityAppealAuditDTO) => request.post<void, void>(`/v1/quality-appeals/${id}/approve`, data),

  reject: (id: number | string, data: QualityAppealAuditDTO) => request.post<void, void>(`/v1/quality-appeals/${id}/reject`, data),
}
