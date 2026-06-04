import type { AsnNotice, AttachmentFile, ImportExportTask, PortalTodo, PurchaseOrder, QualityCase, ReceiptRecord, Settlement, Supplier } from '@/types/business'
import type { OperationLogItem } from '@/types/operationLog'

// 状态文本映射
// 状态文本映射（与后端 SupplierStatusEnum 对齐：0=已注册,1-3=保留,4=已准入,5=已驳回,6=已禁用）
const supplierStatusMap: Record<number, string> = { 0: '已注册', 1: '已注册', 2: '已注册', 3: '已注册', 4: '已准入', 5: '已驳回', 6: '已禁用' }
const orderStatusMap: Record<number, string> = { 0: '草稿', 1: '待确认', 2: '已确认', 3: '部分发货', 4: '已完成', 5: '已取消', 6: '已拒单' }
const deliveryStatusMap: Record<number, string> = { 0: '待发货', 1: '已发货', 2: '运输中', 3: '已送达', 4: '已收货', 5: '已拒收', 6: '质检中' }
const qualityResultMap: Record<number, string> = { 0: '待检验', 1: '合格', 2: '不合格', 3: '让步接收' }
const reconStatusMap: Record<number, string> = { 0: '草稿', 1: '已发送', 2: '已确认', 3: '有异议', 4: '已完成', 5: '已冻结', 6: '冻结中' }

const supplierTypeMap: Record<number, string> = { 1: '原材料', 2: '辅材', 3: '设备', 4: '服务', 5: '其他' }
const ratingMap: Record<number, string> = { 1: 'A级', 2: 'B级', 3: 'C级', 4: 'D级' }

const toStatus = (map: Record<number, string>, value: unknown): string => {
  if (typeof value === 'number') return map[value] || String(value)
  if (typeof value === 'string') return value
  return 'processing'
}

// 订单确认状态文本
const orderConfirmText = (orderStatus: number, confirmTime?: string): string => {
  if (orderStatus === 2) return '已确认'
  if (orderStatus === 3) return '部分发货'
  if (orderStatus === 4) return '已完成'
  if (orderStatus === 5) return '已取消'
  if (orderStatus === 6) return '已拒单'
  if (confirmTime) return '已确认'
  return '待确认'
}

// 风险等级
const riskFromDelivery = (deliveryDate?: string, orderStatus?: number): string => {
  if (orderStatus === 5 || orderStatus === 6) return 'low'
  if (!deliveryDate) return 'medium'
  const now = Date.now()
  const due = new Date(deliveryDate).getTime()
  if (due < now) return 'high'
  if (due < now + 7 * 86400000) return 'medium'
  return 'low'
}

export const toSupplier = (item: any): Supplier => ({
  id: item.id,
  code: item.supplierCode || item.code || '-',
  name: item.supplierName || item.name || '-',
  shortName: item.supplierShortName || '',
  category: supplierTypeMap[item.supplierType] || item.category || '-',
  categoryId: item.categoryId ?? undefined,
  supplierType: item.supplierType ?? undefined,
  level: ratingMap[item.rating] || item.level || '-',
  status: item.status ?? 0,
  contact: item.contactName || item.contact || '-',
  contactEmail: item.contactEmail || '',
  phone: item.contactPhone || item.phone || '-',
  legalPerson: item.legalPerson || '',
  creditCode: item.creditCode || '',
  province: item.province || '',
  city: item.city || '',
  district: item.district || '',
  admissionStage: supplierStatusMap[item.status] || '待审核',
  performanceScore: Number(item.rating || item.performanceScore || 0),
  riskLevel: Number(item.rating || 0) >= 4 ? 'low' : 'medium',
  accountCount: Number(item.accountCount ?? 0),
  address: item.address || '-',
  bankName: item.bankName || '',
  bankAccount: item.bankAccount || '',
  taxNumber: item.taxNumber || '',
  remark: item.remark || '',
  auditTime: item.auditTime || '',
  auditRemark: item.auditRemark || '',
  createdAt: item.createTime || '',
  blacklisted: item.blacklisted ?? false,
  blacklistReason: item.blacklistReason || '',
  blacklistStartTime: item.blacklistStartTime || '',
  blacklistEndTime: item.blacklistEndTime || '',
})

export const toOrder = (item: any): PurchaseOrder => ({
  id: item.id,
  orderNo: item.orderNo || '-',
  supplierId: item.supplierId,
  supplierName: item.supplierName || `供应商${item.supplierId || ''}`,
  orderStatus: item.orderStatus ?? item.status,
  orderDate: item.orderDate || '',
  buyer: item.buyerName || '-',
  buyerId: item.buyerId,
  amount: Number(item.totalAmount || item.payAmount || item.amount || 0),
  currency: item.currency || 'CNY',
  taxAmount: Number(item.taxAmount || 0),
  discountAmount: Number(item.discountAmount || 0),
  payAmount: Number(item.payAmount || 0),
  deliveryDate: item.deliveryDate || '-',
  deliveryAddress: item.deliveryAddress || '',
  paymentTerms: item.paymentTerms || '',
  confirmTime: item.confirmTime || '',
  remark: item.remark || '',
  status: toStatus(orderStatusMap, item.orderStatus ?? item.status),
  confirmStatus: orderConfirmText(item.orderStatus ?? item.status, item.confirmTime),
  riskLevel: riskFromDelivery(item.deliveryDate, item.orderStatus ?? item.status),
})

export const toAsn = (item: any): AsnNotice => ({
  id: item.id,
  asnNo: item.noticeNo || item.asnNo || '-',
  orderNo: item.orderNo || '-',
  supplierName: item.supplierName || `供应商${item.supplierId || ''}`,
  shipDate: item.actualDeliveryDate || item.planDeliveryDate || '-',
  eta: item.planDeliveryDate || '-',
  status: toStatus(deliveryStatusMap, item.deliveryStatus ?? item.status),
  quantity: Number(item.quantity || 0),
  warehouse: item.deliveryAddress || item.warehouse || '-',
})

export const toReceiptRecord = (item: any): ReceiptRecord => ({
  id: item.id,
  receiptNo: item.receiptNo || item.noticeNo || '-',
  asnId: item.asnId || item.noticeId || 0,
  asnNo: item.asnNo || item.noticeNo || '-',
  supplierName: item.supplierName || '-',
  materialCode: item.materialCode || '-',
  materialName: item.materialName || '-',
  orderQty: Number(item.orderQty || item.planQty || 0),
  receivedQty: Number(item.receivedQty || item.actualQty || 0),
  diffQty: Number(item.diffQty || 0),
  diffReason: item.diffReason || '',
  receiptTime: item.receiptTime || item.createTime || '-',
  status: item.status ?? 0,
  createTime: item.createTime || '-',
})

export const toQuality = (item: any): QualityCase => ({
  id: item.id,
  caseNo: item.inspectionNo || `QI-${item.id}`,
  type: item.inspectType ? `检验类型${item.inspectType}` : item.type || '质量检验',
  supplierName: item.supplierName || '-',
  severity: Number(item.unqualifiedQty || 0) > 0 ? 'high' : 'low',
  status: toStatus(qualityResultMap, item.inspectResult ?? item.status),
  inspectResult: item.inspectResult ?? 0,
  owner: item.inspectorName || '-',
  createdAt: item.inspectTime || '-',
  description: item.inspectRemark || item.materialName || '-',
})

export const toSettlement = (item: any): Settlement => ({
  id: item.id,
  statementNo: item.reconNo || item.statementNo || '-',
  supplierName: item.supplierName || `供应商${item.supplierId || ''}`,
  period: item.reconPeriod || item.period || '-',
  amount: Number(item.totalAmount || item.amount || 0),
  diffAmount: Number(item.diffAmount || 0),
  status: toStatus(reconStatusMap, item.reconStatus ?? item.status),
  invoiceStatus: item.invoiceStatus != null ? `发票${item.invoiceStatus}` : 'processing',
  paymentStatus: item.paymentStatus != null ? `付款${item.paymentStatus}` : 'processing',
})

export const toAttachment = (item: any): AttachmentFile => ({
  id: item.id,
  fileName: item.fileName || '-',
  version: item.version || 'V1',
  size: item.fileSize ? `${Math.ceil(item.fileSize / 1024)}KB` : '-',
  uploader: item.uploadUserName || (item.uploadUserId ? `用户${item.uploadUserId}` : '-'),
  uploadedAt: item.uploadTime || '-',
  category: item.businessType || '-',
  businessType: item.businessType,
  businessId: item.businessId,
})

export const toExportTask = (item: any): ImportExportTask => ({
  id: item.id,
  taskNo: item.taskNo || '-',
  type: 'export',
  module: item.taskType || '-',
  status: toStatus({ '0': '0', '1': '1', '2': '2', '3': '3' } as any, item.taskStatus ?? item.status),
  total: Number(item.totalCount || 0),
  success: Number(item.processedCount || 0),
  failed: item.errorMessage ? 1 : 0,
  createdAt: item.startTime || item.finishTime || '-',
})

export const toOperationLog = (item: any): OperationLogItem => ({
  id: item.id,
  module: item.moduleName || item.businessType || '-',
  businessNo: item.businessNo || '-',
  action: item.actionName || '-',
  operator: item.username || '-',
  beforeStatus: String(item.beforeStatus ?? ''),
  afterStatus: String(item.afterStatus ?? ''),
  result: item.resultStatus === 1 ? '成功' : item.errorMessage ? '失败' : '-',
  remark: item.errorMessage || '-',
  ip: item.clientIp || '-',
  traceId: item.traceId || '-',
  operatedAt: item.operateTime || '-',
})

export const toPortalTodo = (item: any): PortalTodo => ({
  id: item.id,
  title: item.title || '-',
  module: item.businessType || item.todoType || '-',
  businessType: item.businessType || '',
  businessId: item.businessId || 0,
  priority: priorityFromDueTime(item.dueTime),
  dueDate: item.dueTime || '-',
  createTime: item.createTime || '-',
  status: toStatus({ '0': '0', '1': '1', '2': '2' } as any, item.todoStatus ?? item.status),
})

const priorityFromDueTime = (dueTime?: string): string => {
  if (!dueTime) return 'low'
  const now = Date.now()
  const due = new Date(dueTime).getTime()
  if (due < now) return 'high'
  if (due < now + 3 * 86400000) return 'medium'
  return 'low'
}