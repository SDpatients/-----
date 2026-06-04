import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { Material } from '@/types/business'

interface BackendMaterial {
  id: number | string
  materialCode: string
  materialName: string
  spec: string
  unit: string
  category: string
  status: number
  createTime: string
}

const toMaterial = (item: BackendMaterial): Material => ({
  id: item.id,
  code: item.materialCode,
  name: item.materialName,
  spec: item.spec || '',
  unit: item.unit || 'PCS',
  category: item.category || '',
  status: item.status ?? 1,
  createTime: item.createTime || '',
})

export const materialApi = {
  page: (params: Record<string, unknown> = {}) =>
    request
      .get<ApiPage<BackendMaterial>, ApiPage<BackendMaterial>>('/v1/materials', { params })
      .then((r) => {
        const page = asPage<BackendMaterial>(r, Number(params.pageNum) || 1, Number(params.pageSize) || 20)
        return {
          ...page,
          records: page.records.map(toMaterial),
        }
      }),

  detail: (id: number | string) =>
    request.get<BackendMaterial, BackendMaterial>(`/v1/materials/${id}`).then(toMaterial),

  create: (data: { materialCode: string; materialName: string; spec?: string; unit?: string; category?: string; remark?: string }) =>
    request.post<number, number>('/v1/materials', data),

  update: (id: number | string, data: { materialCode: string; materialName: string; spec?: string; unit?: string; category?: string; remark?: string }) =>
    request.put<void, void>(`/v1/materials/${id}`, data),

  delete: (id: number | string) =>
    request.delete<void, void>(`/v1/materials/${id}`),

  /** 按关键词搜索物料（适合 Select 远程搜索） */
  search: (keyword: string, pageSize = 20) =>
    materialApi.page({ pageNum: 1, pageSize, keyword }),
}