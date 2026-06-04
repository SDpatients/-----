import { request } from '@/utils/request'

export interface DictItem {
  id: number | string
  dictId: number | string
  itemLabel: string
  itemValue: string
  sort: number
  description: string
  status: number
}

export interface SysDict {
  id: number | string
  dictName: string
  dictCode: string
  description: string
  status: number
  createTime: string
}

export const dictApi = {
  listAll: () =>
    request.get<SysDict[], SysDict[]>('/v1/dict/list'),

  createDict: (data: { dictName: string; dictCode: string; description?: string }) =>
    request.post<number, number>('/v1/dict', data),

  deleteDict: (id: number | string) =>
    request.delete<void, void>(`/v1/dict/${id}`),

  getItemsByCode: (dictCode: string) =>
    request.get<DictItem[], DictItem[]>(`/v1/dict/code/${dictCode}`),

  getItemsByDictId: (dictId: number | string) =>
    request.get<DictItem[], DictItem[]>(`/v1/dict/${dictId}/items`),

  createItem: (data: { dictId: number | string; itemLabel: string; itemValue: string; sort?: number; description?: string }) =>
    request.post<number, number>('/v1/dict/items', data),

  updateItem: (id: number | string, data: { itemLabel: string; itemValue: string; sort?: number; description?: string; status?: number }) =>
    request.put<void, void>(`/v1/dict/items/${id}`, data),

  deleteItem: (id: number | string) =>
    request.delete<void, void>(`/v1/dict/items/${id}`),
}