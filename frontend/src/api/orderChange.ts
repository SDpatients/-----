import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'

export interface OrderChangeQuery {
  pageNum: number
  pageSize: number
  orderId?: number | string | null
}

export interface OrderChangeItem {
  id: number
  orderId: number
  orderDetailId?: number
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

export interface OrderChangeCreateDTO {
  orderId: number | string
  orderDetailId?: number
  changeType: number
  changeContent: string
  beforeValue?: string
  afterValue?: string
  changeReason: string
}

export interface OrderChangeApproveDTO {
  approveStatus: number
  approveRemark?: string
}

export const orderChangeApi = {
  page: async (params: OrderChangeQuery) =>
    asPage<OrderChangeItem>(await request.get<ApiPage<OrderChangeItem>, ApiPage<OrderChangeItem>>('/v1/order-changes', { params }), params.pageNum, params.pageSize),
  detail: (id: number | string) => request.get<OrderChangeItem, OrderChangeItem>(`/v1/order-changes/${id}`),
  create: (data: OrderChangeCreateDTO) =>
    request.post<number, number>('/v1/order-changes', data),
  approve: (id: number | string, data: OrderChangeApproveDTO) =>
    request.post<void, void>(`/v1/order-changes/${id}/approve`, data),
}
