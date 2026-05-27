import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'

export const vmiApi = {
  page: (params: Record<string, unknown>) =>
    request.get('/v1/vmi-inventories', { params }).then(r => asPage(r as any, Number(params.pageNum), Number(params.pageSize))),
  detail: (id: number | string) => request.get(`/v1/vmi-inventories/${id}`),
  sync: (data: Record<string, unknown>) => request.post('/v1/vmi-inventories/sync', data),
}

export const forecastApi = {
  page: (params: Record<string, unknown>) =>
    request.get('/v1/forecast-demands', { params }).then(r => asPage(r as any, Number(params.pageNum), Number(params.pageSize))),
  detail: (id: number | string) => request.get(`/v1/forecast-demands/${id}`),
  create: (data: Record<string, unknown>) => request.post('/v1/forecast-demands', data),
  publish: (id: number | string) => request.post(`/v1/forecast-demands/${id}/publish`),
  respond: (id: number | string, data?: Record<string, unknown>) => request.post(`/v1/forecast-demands/${id}/respond`, data || {}),
  close: (id: number | string) => request.post(`/v1/forecast-demands/${id}/close`),
}