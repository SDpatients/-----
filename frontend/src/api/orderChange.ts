import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery } from '@/types/business'

export interface OrderChangeItem {
  id: number | string
  orderId: number | string
  orderDetailId?: number | string
  changeType: number
  changeContent: string
  beforeValue?: string
  afterValue?: string
  changeReason: string
  approveStatus: number
  approveRemark?: string
  applyTime?: string
  approveTime?: string
}

export const orderChangeApi = {
  page: async (params: PageQuery & { orderId?: number | string }) =>
    asPage<OrderChangeItem>(await request.get<ApiPage<OrderChangeItem>, ApiPage<OrderChangeItem>>('/v1/order-changes', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) => request.get<OrderChangeItem, OrderChangeItem>(`/v1/order-changes/${id}`),
  create: (data: { orderId: number | string; orderDetailId?: number | string; changeType: number; changeContent: string; beforeValue?: string; afterValue?: string; changeReason: string }) =>
    request.post<number, number>('/v1/order-changes', data),
  approve: (id: number | string, data: { approveStatus: number; approveRemark?: string }) =>
    request.post<void, void>(`/v1/order-changes/${id}/approve`, data),
}