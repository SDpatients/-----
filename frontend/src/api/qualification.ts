import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { SupplierQualificationItem } from '@/types/business'

interface BackendQualification {
  id: number | string
  supplierId: number
  qualType: string
  qualName: string
  qualNo?: string
  qualOrg?: string
  validStart?: string
  validEnd?: string
  fileId?: number
  status: number
  remindDays: number
  remark?: string
  createTime?: string
}

const toQualification = (item: BackendQualification): SupplierQualificationItem => ({
  id: item.id,
  supplierId: item.supplierId,
  qualType: item.qualType,
  qualName: item.qualName,
  qualNo: item.qualNo,
  qualOrg: item.qualOrg,
  validStart: item.validStart,
  validEnd: item.validEnd,
  fileId: item.fileId,
  status: item.status,
  remindDays: item.remindDays,
  remark: item.remark,
  createTime: item.createTime || '',
})

export const qualificationApi = {
  page: async (params: Record<string, unknown>) => {
    const result = await request.get<ApiPage<BackendQualification>, ApiPage<BackendQualification>>('/v1/supplier-qualifications', { params })
    return {
      records: asPage<BackendQualification>(result).records.map(toQualification),
      total: result.total,
    }
  },
  detail: (id: number | string) =>
    request.get<BackendQualification, BackendQualification>(`/v1/supplier-qualifications/${id}`).then(toQualification),
  create: (data: Partial<SupplierQualificationItem>) =>
    request.post<number, number>('/v1/supplier-qualifications', data),
  update: (id: number | string, data: Partial<SupplierQualificationItem>) =>
    request.put<void, void>(`/v1/supplier-qualifications/${id}`, data),
  delete: (id: number | string) =>
    request.delete<void, void>(`/v1/supplier-qualifications/${id}`),
}