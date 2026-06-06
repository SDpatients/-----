import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'

/** 对账概览数据 */
export interface ReconciliationOverview {
  totalAmount: number
  reconciledAmount: number
  pendingAmount: number
  diffAmount: number
  totalCount: number
  reconciledCount: number
  pendingCount: number
  diffCount: number
}

/** 趋势数据项 */
export interface ReconciliationTrendItem {
  period: string
  totalAmount: number
  reconciledAmount: number
  diffAmount: number
  count: number
}

/** 供应商分布数据项 */
export interface SupplierDistributionItem {
  supplierName: string
  amount: number
  count: number
}

/** 付款状态分布数据项 */
export interface PaymentStatusItem {
  status: string
  label: string
  count: number
  amount: number
}

/** 对账状态分布数据项 */
export interface ReconciliationStatusItem {
  status: string
  label: string
  count: number
  amount: number
}

/** 图表聚合数据 */
export interface ReconciliationChartData {
  trends: ReconciliationTrendItem[]
  supplierDistribution: SupplierDistributionItem[]
  paymentStatus: PaymentStatusItem[]
  reconciliationStatus: ReconciliationStatusItem[]
}

/** 对账明细查询参数 */
export interface ReconciliationQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  reconciliationStatus?: string
  paymentStatus?: string
  supplierName?: string
  startDate?: string
  endDate?: string
}

/** 对账明细记录 */
export interface ReconciliationRecord {
  id: number | string
  asnNo: string
  orderNo: string
  supplierName: string
  totalAmount: number
  quantity: number
  currency: string
  reconciliationStatus: number
  reconciliationStatusLabel: string
  paymentStatus: number
  paymentStatusLabel: string
  diffAmount: number
  period: string
  createDate: string
}

/** 对账明细行 - 单条物料 */
export interface ReconciliationLine {
  detailId: number
  orderDetailId: number
  materialCode: string
  materialName: string
  materialSpec: string
  unit: string
  orderQty: number
  unitPrice: number
  orderAmount: number
  deliveryQty: number
  receivedQty: number
  reconcileQty: number
  reconcileAmount: number
  diffQty: number
  diffAmount: number
}

/** 对账明细详情 */
export interface ReconciliationDetail {
  id: number
  asnNo: string
  orderNo: string
  supplierName: string
  totalAmount: number
  currency: string
  reconciliationStatus: number
  paymentStatus: number
  period: string
  lines: ReconciliationLine[]
  totalOrderAmount: number
  totalReconcileAmount: number
  totalDiffAmount: number
}

/** 对账确认提交项 */
export interface LineReconcileItem {
  detailId: number
  reconcileQty: number
}

/** 对账确认提交数据 */
export interface ReconciliationConfirmData {
  reconciliationStatus: number
  diffReason?: string
  lines: LineReconcileItem[]
}

export const financialReconciliationApi = {
  /** 获取对账概览 */
  overview: (params?: { startDate?: string; endDate?: string; supplierName?: string }) =>
    request.get<ReconciliationOverview, ReconciliationOverview>('/v1/financial-reconciliation/overview', { params }),

  /** 获取图表数据 */
  chartData: (params?: { startDate?: string; endDate?: string; supplierName?: string }) =>
    request.get<ReconciliationChartData, ReconciliationChartData>('/v1/financial-reconciliation/chart-data', { params }),

  /** 获取对账列表 */
  page: async (params: ReconciliationQuery) =>
    asPage<ReconciliationRecord>(await request.get<ApiPage<ReconciliationRecord>, ApiPage<ReconciliationRecord>>('/v1/financial-reconciliation/list', { params }), params.pageNum, params.pageSize),

  /** 更新对账状态（含差异金额） */
  updateReconciliationStatus: (id: number | string, data: { reconciliationStatus: number; diffAmount?: number; diffReason?: string }) =>
    request.put<void, void>(`/v1/financial-reconciliation/${id}/reconciliation-status`, data),

  /** 更新付款状态 */
  updatePaymentStatus: (id: number | string, data: { paymentStatus: number }) =>
    request.put<void, void>(`/v1/financial-reconciliation/${id}/payment-status`, data),

  /** 获取对账明细详情（含物料行） */
  getDetail: (id: number | string) =>
    request.get<ReconciliationDetail, ReconciliationDetail>(`/v1/financial-reconciliation/${id}/detail`),

  /** 提交对账确认（含各行对账数量，自动计算差异金额） */
  confirmReconciliation: (id: number | string, data: ReconciliationConfirmData) =>
    request.put<void, void>(`/v1/financial-reconciliation/${id}/confirm-reconciliation`, data),
}
