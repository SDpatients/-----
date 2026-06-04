import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery } from '@/types/business'

export interface SupplierAccount {
  id: number | string
  username: string
  realName: string
  phone: string
  email: string
  userType: number
  supplierId: number | string
  status: number
  lastLoginTime: string
  createTime: string
  remark: string
}

export interface SupplierAccountCreateForm {
  supplierId: number | string
  username: string
  password: string
  realName: string
  phone?: string
  email?: string
  remark?: string
}

export interface SupplierAccountQuery {
  supplierId: number | string
  keyword?: string
  pageNum: number
  pageSize: number
}

export const supplierAccountApi = {
  page: async (params: SupplierAccountQuery) => {
    const result = await request.get<ApiPage<SupplierAccount>, ApiPage<SupplierAccount>>('/v1/supplier-accounts', { params })
    return asPage<SupplierAccount>(result, params.pageNum, params.pageSize)
  },

  detail: (id: number | string) => request.get<SupplierAccount, SupplierAccount>(`/v1/supplier-accounts/${id}`),

  create: (data: SupplierAccountCreateForm) =>
    request.post<number, number>('/v1/supplier-accounts', data),

  resetPassword: (id: number | string, password: string) =>
    request.put<void, void>(`/v1/supplier-accounts/${id}/password`, { password }),

  toggleStatus: (id: number | string, status: number) =>
    request.put<void, void>(`/v1/supplier-accounts/${id}/status`, null, { params: { status } }),
}