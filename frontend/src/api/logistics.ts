import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { AsnNotice, PageQuery, DeliveryLineItem, ReceiveDiscrepancy, WriteOffRecord, ReceiptRecord } from '@/types/business'

import type { DeliveryDetailLine } from './mockData'

export const logisticsApi = {
  deliveryPage: async (params: PageQuery) => asPage<AsnNotice>(await request.get<ApiPage<AsnNotice>, ApiPage<AsnNotice>>('/v1/delivery-notices', { params }), params.pageNum, params.pageSize),
  deliveryDetail: (id: number | string) => request.get<AsnNotice, AsnNotice>(`/v1/delivery-notices/${id}`),
  deliveryLines: (id: number | string) => request.get<DeliveryDetailLine[], DeliveryDetailLine[]>(`/v1/delivery-notices/${id}/lines`),
  createDelivery: (data: Partial<AsnNotice> & { lines?: DeliveryLineItem[] }, headers?: Record<string, string>) =>
    request.post<number, number>('/v1/delivery-notices', data, { headers }),
  ship: (id: number | string, remark?: string) => request.post<void, void>(`/v1/delivery-notices/${id}/ship`, { remark }),
  shipWithLines: (id: number | string, data: { remark?: string; lines?: DeliveryLineItem[] }) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/ship`, data),
  arrive: (id: number | string, remark?: string, discrepancy?: ReceiveDiscrepancy) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/arrive`, { remark, discrepancy }),
  // 收货差异
  getDiscrepancy: (id: number | string) => request.get<ReceiveDiscrepancy, ReceiveDiscrepancy>(`/v1/delivery-notices/${id}/discrepancy`),
  saveDiscrepancy: (id: number | string, data: ReceiveDiscrepancy) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/discrepancy`, data),
  // 触发质检
  triggerQuality: (id: number | string) => request.post<void, void>(`/v1/delivery-notices/${id}/trigger-quality`),
  // 确认入库
  confirmWarehousing: (id: number | string) => request.post<void, void>(`/v1/delivery-notices/${id}/warehousing`),
  // 扫码收货
  scanReceive: (barcode: string) => request.get<AsnNotice, AsnNotice>(`/v1/delivery-notices/scan/${barcode}`),
  confirmScanReceive: (id: number | string, data: { receivedQty: number; location?: string; remark?: string }) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/scan-receive`, data),
  // 冲销/调整单
  writeOffPage: async (params: PageQuery) => asPage<WriteOffRecord>(await request.get<ApiPage<WriteOffRecord>, ApiPage<WriteOffRecord>>('/v1/write-offs', { params }), params.pageNum, params.pageSize),
  createWriteOff: (data: Partial<WriteOffRecord>) => request.post<number, number>('/v1/write-offs', data),
  // 条形码
  generateBarcode: (id: number | string) => request.get<{ barcode: string; labelUrl: string }, { barcode: string; labelUrl: string }>(`/v1/delivery-notices/${id}/barcode`),
  generateBatchBarcodes: (id: number | string) => request.get<{ barcodes: Array<{ lineNo: number; barcode: string; materialName: string }> }, { barcodes: Array<{ lineNo: number; barcode: string; materialName: string }> }>(`/v1/delivery-notices/${id}/barcodes`),
  // 收货记录
  receiptPage: async (params: PageQuery) => asPage<ReceiptRecord>(await request.get<ApiPage<ReceiptRecord>, ApiPage<ReceiptRecord>>('/v1/receipt-records', { params }), params.pageNum, params.pageSize),
  receiptDetail: (id: number | string) => request.get<ReceiptRecord, ReceiptRecord>(`/v1/receipt-records/${id}`),
}
