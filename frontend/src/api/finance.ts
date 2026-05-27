import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { PageQuery, InvoiceRecord, DeductionRecord, PaymentRecord } from '@/types/business'
import type { OcrResult, PaymentCallbackLog } from './mockData'

export const invoiceApi = {
  page: async (params: PageQuery) =>
    asPage<InvoiceRecord>(await request.get<ApiPage<InvoiceRecord>, ApiPage<InvoiceRecord>>('/v1/invoices', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<InvoiceRecord, InvoiceRecord>(`/v1/invoices/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/invoices', data),

  upload: (id: number | string, data: Record<string, unknown>) =>
    request.post<void, void>(`/v1/invoices/${id}/upload`, data),

  verify: (id: number | string) => request.post<void, void>(`/v1/invoices/${id}/verify`),

  certify: (id: number | string) => request.post<void, void>(`/v1/invoices/${id}/certify`),

  cancel: (id: number | string, data?: Record<string, unknown>) => request.post<void, void>(`/v1/invoices/${id}/void`, data),

  /** OCR 发票识别 */
  ocrRecognize: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request.post<OcrResult, OcrResult>('/v1/invoices/ocr', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },

  /** 查询可开票金额 */
  invoicableAmount: (reconId: number | string) => request.get<{ invoicableAmount: number; totalAmount: number; invoicedAmount: number }, { invoicableAmount: number; totalAmount: number; invoicedAmount: number }>(`/v1/reconciliations/${reconId}/invoicable-amount`),

  /** 获取发票关联的付款记录 */
  linkedPayments: (id: number | string) => request.get<PaymentRecord[], PaymentRecord[]>(`/v1/invoices/${id}/payments`),
}

export const deductionApi = {
  page: async (params: PageQuery) =>
    asPage<DeductionRecord>(await request.get<ApiPage<DeductionRecord>, ApiPage<DeductionRecord>>('/v1/deductions', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<DeductionRecord, DeductionRecord>(`/v1/deductions/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/deductions', data),

  submit: (id: number | string) => request.post<void, void>(`/v1/deductions/${id}/submit`),

  confirm: (id: number | string) => request.post<void, void>(`/v1/deductions/${id}/confirm`),

  dispute: (id: number | string, data: { disputeReason: string }) =>
    request.post<void, void>(`/v1/deductions/${id}/dispute`, data),

  book: (id: number | string) => request.post<void, void>(`/v1/deductions/${id}/book`),
}

export const paymentApi = {
  page: async (params: PageQuery) =>
    asPage<PaymentRecord>(await request.get<ApiPage<PaymentRecord>, ApiPage<PaymentRecord>>('/v1/payments', { params }), params.pageNum, params.pageSize),

  detail: (id: number | string) => request.get<PaymentRecord, PaymentRecord>(`/v1/payments/${id}`),

  create: (data: Record<string, unknown>) => request.post<number, number>('/v1/payments', data),

  pay: (id: number | string) => request.post<void, void>(`/v1/payments/${id}/pay`),

  reject: (id: number | string) => request.post<void, void>(`/v1/payments/${id}/reject`),

  /** 获取付款回传状态日志 */
  callbackLogs: (id: number | string) => request.get<PaymentCallbackLog[], PaymentCallbackLog[]>(`/v1/payments/${id}/callback-logs`),

  /** 获取付款关联的发票记录 */
  linkedInvoices: (id: number | string) => request.get<InvoiceRecord[], InvoiceRecord[]>(`/v1/payments/${id}/invoices`),
}

export const performanceApi = {
  page: async (params: Record<string, unknown>) =>
    request.get('/v1/supplier-performances', { params }).then(r => asPage(r as any, Number(params.pageNum), Number(params.pageSize))),

  detail: (id: number | string) => request.get(`/v1/supplier-performances/${id}`),

  create: (data: Record<string, unknown>) => request.post('/v1/supplier-performances', data),

  update: (id: number | string, data: Record<string, unknown>) => request.put(`/v1/supplier-performances/${id}`, data),

  delete: (id: number | string) => request.delete(`/v1/supplier-performances/${id}`),
}