import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { VmiInventoryItem, ForecastDemandItem, PageQuery } from '@/types/business'

export interface VmiInventoryQuery extends PageQuery {
  supplierId?: number | string
  materialCode?: string
  inventoryStatus?: number
}

export interface VmiInventorySyncDTO {
  supplierId: number | string
  materialCode: string
  warehouseId?: number
  warehouseName?: string
  onhandQty: number
  availableQty: number
  safetyQty?: number
  maxQty?: number
}

export const vmiApi = {
  page: async (params: VmiInventoryQuery) =>
    asPage<VmiInventoryItem>(await request.get<ApiPage<VmiInventoryItem>, ApiPage<VmiInventoryItem>>('/v1/vmi-inventories', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) =>
    request.get<VmiInventoryItem, VmiInventoryItem>(`/v1/vmi-inventories/${id}`),
  sync: (data: VmiInventorySyncDTO) =>
    request.post<void, void>('/v1/vmi-inventories/sync', data),
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
