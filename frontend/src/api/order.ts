import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, PurchaseOrder } from '@/types/business'

export interface DeliveryFeedbackDTO {
  promisedDeliveryDate: string
  batchPlan?: string
  remark?: string
}

export const orderApi = {
  page: async (params: PageQuery) => asPage<PurchaseOrder>(await request.get<ApiPage<PurchaseOrder>, ApiPage<PurchaseOrder>>('/v1/purchase-orders', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) => request.get<PurchaseOrder, PurchaseOrder>(`/v1/purchase-orders/${id}`),
  lines: (id: number | string) => request.get<any[], any[]>(`/v1/purchase-orders/${id}/lines`),
  create: (data: Partial<PurchaseOrder>) => request.post<number, number>('/v1/purchase-orders', data),
  publish: (id: number | string, remark?: string) => request.post<void, void>(`/v1/purchase-orders/${id}/publish`, { remark }),
  confirm: (id: number | string, remark?: string) => request.post<void, void>(`/v1/purchase-orders/${id}/confirm`, { remark }),
  reject: (id: number | string, remark?: string) => request.post<void, void>(`/v1/purchase-orders/${id}/reject`, { remark }),
  cancel: (id: number | string, remark?: string) => request.post<void, void>(`/v1/purchase-orders/${id}/cancel`, { remark }),
  close: (id: number | string, remark?: string) => request.post<void, void>(`/v1/purchase-orders/${id}/close`, { remark }),
  feedbackDelivery: (id: number | string, data: DeliveryFeedbackDTO) => request.post<void, void>(`/v1/purchase-orders/${id}/delivery-feedback`, data),
}
