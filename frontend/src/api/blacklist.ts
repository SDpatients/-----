import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { SupplierBlacklist } from '@/types/business'

interface BackendBlacklist {
  id: number
  supplierId: number
  supplierName: string
  creditCode?: string
  reason: string
  startTime: string
  endTime?: string
  status: number
  createTime: string
}

const toBlacklist = (item: BackendBlacklist): SupplierBlacklist => ({
  id: item.id,
  supplierId: item.supplierId,
  supplierName: item.supplierName,
  creditCode: item.creditCode,
  reason: item.reason,
  startTime: item.startTime,
  endTime: item.endTime,
  status: item.status,
  createTime: item.createTime,
})

export const blacklistApi = {
  page: async (params: Record<string, unknown>) => {
    const result = await request.get<ApiPage<BackendBlacklist>, ApiPage<BackendBlacklist>>('/v1/supplier-blacklists', { params })
    return {
      records: asPage<BackendBlacklist>(result).records.map(toBlacklist),
      total: result.total,
    }
  },
  detail: (id: number | string) =>
    request.get<BackendBlacklist, BackendBlacklist>(`/v1/supplier-blacklists/${id}`).then(toBlacklist),
  create: (data: {
    supplierId: number | string
    supplierName: string
    creditCode?: string
    reason: string
    startTime: string
    endTime?: string
  }) => request.post<number, number>('/v1/supplier-blacklists', data),
  update: (id: number | string, data: { reason?: string; endTime?: string }) =>
    request.put<void, void>(`/v1/supplier-blacklists/${id}`, data),
  remove: (id: number | string) =>
    request.post<void, void>(`/v1/supplier-blacklists/${id}/remove`),
  /** 校验统一社会信用代码是否在黑名单中 */
  checkByCreditCode: (creditCode: string) =>
    request.get<boolean, boolean>(`/v1/supplier-blacklists/check`, { params: { creditCode } }),
}