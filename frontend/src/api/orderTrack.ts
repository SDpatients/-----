import { request } from '@/utils/request'

export interface OrderTrackVO {
  id: number
  orderId: number
  trackStatus: number
  trackTime: string
  trackRemark: string
  operator: number | null
  operatorName: string
}

export const orderTrackApi = {
  listByOrderId: (orderId: number | string) =>
    request.get<OrderTrackVO[], OrderTrackVO[]>(`/v1/order-tracks/order/${orderId}`),
}
