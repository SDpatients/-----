import { request } from '@/utils/request'
import { asArray } from '@/utils/apiNormalize'

export interface OrderDetailLineItem {
  id?: number | string
  orderId: number | string
  lineNo: number
  materialCode: string
  materialName: string
  materialSpec?: string
  materialModel?: string
  unit?: string
  quantity: number
  unitPrice: number
  taxRate?: number
  taxAmount?: number
  amount: number
  deliveredQty?: number
  receivedQty?: number
  qualifiedQty?: number
  deliveryDate?: string
  remark?: string
}

export const orderDetailApi = {
  list: async (orderId: number | string): Promise<OrderDetailLineItem[]> => {
    const result = await request.get<OrderDetailLineItem[], OrderDetailLineItem[]>('/v1/purchase-order-details', { params: { orderId } })
    return asArray<OrderDetailLineItem>(result)
  },
  detail: (id: number | string) => request.get<OrderDetailLineItem, OrderDetailLineItem>(`/v1/purchase-order-details/${id}`),
  create: (data: Omit<OrderDetailLineItem, 'id'>) => request.post<number, number>('/v1/purchase-order-details', data),
  update: (id: number | string, data: Partial<OrderDetailLineItem>) => request.put<void, void>(`/v1/purchase-order-details/${id}`, data),
  delete: (id: number | string) => request.delete<void, void>(`/v1/purchase-order-details/${id}`),
}