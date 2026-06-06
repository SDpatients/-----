import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'
import type { AsnNotice, PageQuery, DeliveryLineItem, WriteOffRecord, ReceiptRecord, ReceiptDiff, DeliveryDetail, DeliveryPackage, DeliveryBarcode } from '@/types/business'

import type { DeliveryDetailLine } from './mockData'

export const logisticsApi = {
  // ==================== ASN 送货通知（DeliveryNotice） ====================
  deliveryPage: async (params: PageQuery) => asPage<AsnNotice>(await request.get<ApiPage<AsnNotice>, ApiPage<AsnNotice>>('/v1/delivery-notices', { params }), params.pageNum, params.pageSize),
  deliveryDetail: (id: number | string) => request.get<AsnNotice, AsnNotice>(`/v1/delivery-notices/${id}`),
  deliveryLines: (id: number | string) => request.get<DeliveryDetailLine[], DeliveryDetailLine[]>(`/v1/delivery-notices/${id}/lines`),
  createDelivery: (data: Partial<AsnNotice> & { lines?: DeliveryLineItem[] }, headers?: Record<string, string>) =>
    request.post<number, number>('/v1/delivery-notices', data, { headers }),
  ship: (id: number | string, remark?: string) => request.post<void, void>(`/v1/delivery-notices/${id}/ship`, { remark }),
  shipWithLines: (id: number | string, data: { remark?: string; lines?: DeliveryLineItem[] }) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/ship`, data),
  arrive: (id: number | string, remark?: string, discrepancy?: ReceiptDiff) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/arrive`, { remark, discrepancy }),
  confirmWarehousing: (id: number | string) => request.post<void, void>(`/v1/delivery-notices/${id}/warehousing`),
  confirmScanReceive: (id: number | string, data: { receivedQty: number; location?: string; remark?: string }) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/scan-receive`, data),
  confirmReceive: (id: number | string, data?: { remark?: string; warehouse?: string }) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/confirm-receive`, data),
  rejectReceive: (id: number | string, data?: { remark?: string }) =>
    request.post<void, void>(`/v1/delivery-notices/${id}/reject-receive`, data),
  writeOffPage: async (params: PageQuery) => asPage<WriteOffRecord>(await request.get<ApiPage<WriteOffRecord>, ApiPage<WriteOffRecord>>('/v1/write-offs', { params }), params.pageNum, params.pageSize),
  createWriteOff: (data: Partial<WriteOffRecord>) => request.post<number, number>('/v1/write-offs', data),

  // ==================== 送货明细管理（DeliveryDetailController） ====================
  deliveryDetailList: (noticeId: number | string) =>
    request.get<DeliveryDetail[], DeliveryDetail[]>('/v1/delivery-details', { params: { noticeId } }),
  deliveryDetailGet: (id: number | string) =>
    request.get<DeliveryDetail, DeliveryDetail>(`/v1/delivery-details/${id}`),
  deliveryDetailCreate: (data: Partial<DeliveryDetail>) =>
    request.post<number, number>('/v1/delivery-details', data),
  deliveryDetailUpdate: (id: number | string, data: Partial<DeliveryDetail>) =>
    request.put<void, void>(`/v1/delivery-details/${id}`, data),
  deliveryDetailDelete: (id: number | string) =>
    request.delete<void, void>(`/v1/delivery-details/${id}`),

  // ==================== 送货标签/箱管理（DeliveryLabelController） ====================
  packageListByNotice: (noticeId: number | string) =>
    request.get<DeliveryPackage[], DeliveryPackage[]>(`/v1/delivery-labels/packages/by-notice/${noticeId}`),
  packageDetail: (id: number | string) =>
    request.get<DeliveryPackage, DeliveryPackage>(`/v1/delivery-labels/packages/${id}`),
  packageCreate: (data: Partial<DeliveryPackage>) =>
    request.post<number, number>('/v1/delivery-labels/packages', data),
  packageGenerateBarcodes: (id: number | string) =>
    request.post<DeliveryBarcode[], DeliveryBarcode[]>(`/v1/delivery-labels/packages/${id}/barcodes`),
  packagePrint: (id: number | string) =>
    request.post<void, void>(`/v1/delivery-labels/packages/${id}/print`),

  // ==================== 收货差异管理（ReceiptDiffController） ====================
  receiptDiffPage: async (params: PageQuery & { recordId?: number; noticeId?: number; status?: number }) =>
    asPage<ReceiptDiff>(await request.get<ApiPage<ReceiptDiff>, ApiPage<ReceiptDiff>>('/v1/receipt-diffs', { params }), params.pageNum, params.pageSize),
  receiptDiffDetail: (id: number | string) =>
    request.get<ReceiptDiff, ReceiptDiff>(`/v1/receipt-diffs/${id}`),
  receiptDiffAdjust: (recordId: number | string, data: { diffQty: number; diffReason: string; handleMethod: number; handleRemark?: string; remark?: string }) =>
    request.post<number, number>(`/v1/receipt-diffs/adjust/${recordId}`, data),
  receiptDiffApprove: (id: number | string, data?: { approveRemark?: string }) =>
    request.post<void, void>(`/v1/receipt-diffs/${id}/approve`, data),
  receiptDiffReject: (id: number | string, data?: { approveRemark?: string }) =>
    request.post<void, void>(`/v1/receipt-diffs/${id}/reject`, data),

  // ==================== 收货记录管理（ReceiptRecordController） ====================
  receiptPage: async (params: PageQuery & { noticeId?: number; deliveryId?: number; materialCode?: string; receiptStatus?: number; startTime?: string; endTime?: string; supplierId?: number }) =>
    asPage<ReceiptRecord>(await request.get<ApiPage<ReceiptRecord>, ApiPage<ReceiptRecord>>('/v1/receipt-records', { params }), params.pageNum, params.pageSize),
  receiptDetail: (id: number | string) =>
    request.get<ReceiptRecord, ReceiptRecord>(`/v1/receipt-records/${id}`),
  receiptCreate: (data: { deliveryId: number; noticeId?: number; materialCode: string; materialName: string; planQty?: number; receiptQty?: number; rejectQty?: number; receiptTime?: string; warehouseId?: number; warehouseName?: string; location?: string; remark?: string }) =>
    request.post<number, number>('/v1/receipt-records', data),
  receiptConfirm: (id: number | string, data?: { receiptQty?: number; rejectQty?: number; warehouseId?: number; warehouseName?: string; location?: string; rejectReason?: string; remark?: string }) =>
    request.post<void, void>(`/v1/receipt-records/${id}/confirm`, data),
  receiptReject: (id: number | string, data?: { receiptQty?: number; rejectQty?: number; warehouseId?: number; warehouseName?: string; location?: string; rejectReason?: string; remark?: string }) =>
    request.post<void, void>(`/v1/receipt-records/${id}/reject`, data),
  receiptScan: (barcode: string) =>
    request.get<ReceiptRecord, ReceiptRecord>('/v1/receipt-records/scan', { params: { barcode } }),
  receiptAdjust: (id: number | string, data: { diffQty: number; diffReason: string; handleMethod: number; handleRemark?: string; remark?: string }) =>
    request.post<number, number>(`/v1/receipt-records/${id}/adjust`, data),
}
