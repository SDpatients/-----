import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'

export const endpointApi = {
  page: (params: Record<string, unknown>) =>
    request.get('/v1/integration-endpoints', { params }).then(r => asPage(r as any, Number(params.pageNum), Number(params.pageSize))),
  detail: (id: number | string) => request.get(`/v1/integration-endpoints/${id}`),
  create: (data: Record<string, unknown>) => request.post('/v1/integration-endpoints', data),
  update: (id: number | string, data: Record<string, unknown>) => request.put(`/v1/integration-endpoints/${id}`, data),
  delete: (id: number | string) => request.delete(`/v1/integration-endpoints/${id}`),
  enable: (id: number | string) => request.post(`/v1/integration-endpoints/${id}/enable`),
  disable: (id: number | string) => request.post(`/v1/integration-endpoints/${id}/disable`),
}

export const integrationLogApi = {
  page: (params: Record<string, unknown>) =>
    request.get('/v1/integration-logs', { params }).then(r => asPage(r as any, Number(params.pageNum), Number(params.pageSize))),
  detail: (id: number | string) => request.get(`/v1/integration-logs/${id}`),
}

export const syncTaskApi = {
  page: (params: Record<string, unknown>) =>
    request.get('/v1/integration-sync-tasks', { params }).then(r => asPage(r as any, Number(params.pageNum), Number(params.pageSize))),
  detail: (id: number | string) => request.get(`/v1/integration-sync-tasks/${id}`),
  retry: (id: number | string) => request.post(`/v1/integration-sync-tasks/${id}/retry`),
}