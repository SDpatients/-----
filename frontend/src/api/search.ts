import { request } from '@/utils/request'

export interface SearchResultItem {
  id: number | string
  title: string
  subtitle: string
  category: 'supplier' | 'order' | 'asn' | 'quality' | 'settlement'
  path: string
}

export interface SearchResult {
  results: SearchResultItem[]
  total: number
}

export const searchApi = {
  /** 全局全文检索 */
  search: async (keyword: string): Promise<SearchResult> => {
    const result = await request.get<SearchResult, SearchResult>('/v1/search', {
      params: { keyword, limit: 12 },
    })
    return result
  },
}