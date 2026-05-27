import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, Supplier } from '@/types/business'

export const supplierApi = {
  page: async (params: PageQuery) => {
    const result = await request.get<ApiPage<Supplier>, ApiPage<Supplier>>('/v1/suppliers', { params })
    return asPage<Supplier>(result, params.pageNum, params.pageSize)
  },
  detail: (id: number | string) => request.get<Supplier, Supplier>(`/v1/suppliers/${id}`),
  create: (data: Record<string, unknown>, headers?: Record<string, string>) =>
    request.post<number, number>('/v1/suppliers', data, { headers }),
  update: (id: number | string, data: Record<string, unknown>, headers?: Record<string, string>) =>
    request.put<void, void>(`/v1/suppliers/${id}`, data, { headers }),
}
