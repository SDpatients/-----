import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, RfqRecord, QuoteRecord, RfqLineItem, QuoteLineItem, BargainRecord, ExchangeRate, RfqSummaryRecord } from '@/types/business'

/** 供应商端使用 */
export const sourcingApi = {
  /** 查询已发布的RFQ（供应商端） */
  rfqPage: async (params: PageQuery) =>
    asPage<RfqRecord>(await request.get<ApiPage<RfqRecord>, ApiPage<RfqRecord>>('/v1/rfqs', { params }), params.pageNum, params.pageSize),

  rfqDetail: (id: number | string) => request.get<RfqRecord, RfqRecord>(`/v1/rfqs/${id}`),

  /** 查询RFQ物料行 */
  rfqLines: (rfqId: number | string) => request.get<RfqLineItem[], RfqLineItem[]>(`/v1/rfqs/${rfqId}/lines`),

  /** 查询该供应商的报价列表 */
  quotePage: async (params: PageQuery) =>
    asPage<QuoteRecord>(await request.get<ApiPage<QuoteRecord>, ApiPage<QuoteRecord>>('/v1/quotes', { params }), params.pageNum, params.pageSize),

  quoteDetail: (id: number | string) => request.get<QuoteRecord, QuoteRecord>(`/v1/quotes/${id}`),

  /** 查询报价明细行 */
  quoteLines: (quoteId: number | string) => request.get<QuoteLineItem[], QuoteLineItem[]>(`/v1/quotes/${quoteId}/lines`),

  /** 创建报价（含明细行） */
  quoteCreate: (data: Record<string, unknown>) => request.post<number | string, number | string>('/v1/quotes', data),

  /** 提交报价 */
  quoteSubmit: (id: number | string) => request.post<void, void>(`/v1/quotes/${id}/submit`),

  /** 撤回报价 */
  quoteWithdraw: (id: number | string) => request.post<void, void>(`/v1/quotes/${id}/withdraw`),

  /** 修改报价（议价重提交） */
  quoteUpdate: (id: number | string, data: Record<string, unknown>) => request.put<void, void>(`/v1/quotes/${id}`, data),
}

/** 采购方RFQ管理 */
export const rfqApi = {
  page: async (params: PageQuery) =>
    asPage<RfqRecord>(await request.get<ApiPage<RfqRecord>, ApiPage<RfqRecord>>('/v1/rfqs', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<RfqRecord, RfqRecord>(`/v1/rfqs/${id}`),

  create: (data: Record<string, unknown>) => request.post<number | string, number | string>('/v1/rfqs', data),

  update: (id: number | string, data: Record<string, unknown>) => request.put<void, void>(`/v1/rfqs/${id}`, data),

  publish: (id: number | string) => request.post<void, void>(`/v1/rfqs/${id}/publish`),

  close: (id: number | string) => request.post<void, void>(`/v1/rfqs/${id}/close`),

  cancel: (id: number | string) => request.post<void, void>(`/v1/rfqs/${id}/cancel`),

  /** 查询RFQ物料行 */
  lines: (rfqId: number | string) => request.get<RfqLineItem[], RfqLineItem[]>(`/v1/rfqs/${rfqId}/lines`),

  /** 保存RFQ物料行 */
  saveLines: (rfqId: number | string, lines: RfqLineItem[]) => request.put<void, void>(`/v1/rfqs/${rfqId}/lines`, lines),

  /** 邀请供应商 */
  inviteSuppliers: (rfqId: number | string, supplierIds: (number | string)[]) =>
    request.post<void, void>(`/v1/rfqs/${rfqId}/invite`, { supplierIds }),

  /** 获取已邀请供应商 */
  invitedSuppliers: (rfqId: number | string) =>
    request.get<{ supplierId: number; supplierName: string; inviteStatus: number }[], { supplierId: number; supplierName: string; inviteStatus: number }[]>(`/v1/rfqs/${rfqId}/invited`),
}

/** 采购方报价管理 */
export const quoteApi = {
  page: async (params: PageQuery) =>
    asPage<QuoteRecord>(await request.get<ApiPage<QuoteRecord>, ApiPage<QuoteRecord>>('/v1/quotes', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<QuoteRecord, QuoteRecord>(`/v1/quotes/${id}`),

  /** 查询报价明细行 */
  lines: (quoteId: number | string) => request.get<QuoteLineItem[], QuoteLineItem[]>(`/v1/quotes/${quoteId}/lines`),

  adopt: (id: number | string) => request.post<void, void>(`/v1/quotes/${id}/accept`),

  reject: (id: number | string) => request.post<void, void>(`/v1/quotes/${id}/reject`),

  /** 定价转订单 */
  convertToOrder: (quoteId: number | string, data: { type: 'order' | 'agreement'; remark?: string }) =>
    request.post<number | string, number | string>(`/v1/quotes/${quoteId}/convert`, data),

  /** 查询有报价的询价单列表（用于报价对比页左侧） */
  listRfqWithQuotes: () =>
    request.get<RfqSummaryRecord[], RfqSummaryRecord[]>('/v1/quotes/rfqs'),
}

/** 议价管理 */
export const bargainApi = {
  /** 查询议价记录 */
  list: (quoteId: number | string) => request.get<BargainRecord[], BargainRecord[]>(`/v1/bargains`, { params: { quoteId } }),

  /** 采购方发起还价 */
  requestReprice: (quoteId: number | string, data: { message: string; targetAmount?: number }) =>
    request.post<void, void>(`/v1/bargains/${quoteId}/reprice`, data),

  /** 供应商重提交报价 */
  resubmit: (quoteId: number | string, data: Record<string, unknown>) =>
    request.post<void, void>(`/v1/bargains/${quoteId}/resubmit`, data),
}

/** 汇率管理 */
export const exchangeRateApi = {
  page: async (params: PageQuery) =>
    asPage<ExchangeRate>(await request.get<ApiPage<ExchangeRate>, ApiPage<ExchangeRate>>('/v1/exchange-rates', { params }), params.pageNum, params.pageSize),

  create: (data: Partial<ExchangeRate>) => request.post<number | string, number | string>('/v1/exchange-rates', data),

  update: (id: number | string, data: Partial<ExchangeRate>) => request.put<void, void>(`/v1/exchange-rates/${id}`, data),

  delete: (id: number | string) => request.delete<void, void>(`/v1/exchange-rates/${id}`),

  /** 获取最新汇率 */
  latest: (fromCurrency: string, toCurrency: string) =>
    request.get<number, number>(`/v1/exchange-rates/latest`, { params: { fromCurrency, toCurrency } }),
}