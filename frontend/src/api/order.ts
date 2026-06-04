import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'

export interface PurchaseOrderQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  orderStatus?: number
  supplierId?: number | string
  startDate?: string
  endDate?: string
}

export interface DeliveryFeedbackLineDTO {
  orderDetailId: number | string
  promisedDeliveryDate: string
  plannedQuantity: number
  batchNo?: string
  remark?: string
}

export interface DeliveryFeedbackCreateDTO {
  lines: DeliveryFeedbackLineDTO[]
  remark?: string
}

export interface DeliveryFeedbackLineVO {
  id: number
  feedbackId: number
  orderDetailId: number
  materialCode: string
  materialName: string
  promisedDeliveryDate: string
  plannedQuantity: number
  batchNo: string
  remark: string
}

export interface DeliveryFeedbackVO {
  id: number
  orderId: number
  orderNo: string
  supplierId: number
  feedbackStatus: number
  remark: string
  buyerConfirmBy: number | null
  buyerConfirmTime: string | null
  lines: DeliveryFeedbackLineVO[]
}

export interface OrderActionDTO {
  remark?: string
}

export interface BuyerConfirmDTO {
  remark?: string
}

export const orderApi = {
  page: async (params: PurchaseOrderQuery) =>
    asPage<any>(await request.get<ApiPage<any>, ApiPage<any>>('/v1/purchase-orders', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<any, any>(`/v1/purchase-orders/${id}`),

  create: (data: any) => request.post<number, number>('/v1/purchase-orders', data),

  publish: (id: number | string, data?: OrderActionDTO) =>
    request.post<void, void>(`/v1/purchase-orders/${id}/publish`, data || {}),

  confirm: (id: number | string, data?: OrderActionDTO) =>
    request.post<void, void>(`/v1/purchase-orders/${id}/confirm`, data || {}),

  reject: (id: number | string, data?: OrderActionDTO) =>
    request.post<void, void>(`/v1/purchase-orders/${id}/reject`, data || {}),

  cancel: (id: number | string, data?: OrderActionDTO) =>
    request.post<void, void>(`/v1/purchase-orders/${id}/cancel`, data || {}),

  confirmByBuyer: (id: number | string, data?: BuyerConfirmDTO) =>
    request.post<void, void>(`/v1/purchase-orders/${id}/confirm-by-buyer`, data || {}),

  close: (id: number | string, data?: OrderActionDTO) =>
    request.post<void, void>(`/v1/purchase-orders/${id}/close`, data || {}),

  submitDeliveryFeedback: (id: number | string, data: DeliveryFeedbackCreateDTO) =>
    request.post<number, number>(`/v1/purchase-orders/${id}/delivery-feedback`, data),

  getDeliveryFeedback: (id: number | string) =>
    request.get<DeliveryFeedbackVO, DeliveryFeedbackVO>(`/v1/purchase-orders/${id}/delivery-feedback`),

  confirmDeliveryFeedback: (feedbackId: number | string) =>
    request.post<void, void>(`/v1/purchase-orders/delivery-feedback/${feedbackId}/confirm`),
}
