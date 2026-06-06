export type UserType = 'internal' | 'supplier'

export interface UserInfo {
  id: number | string
  username: string
  realName: string
  roleName: string
  department: string
  userType: UserType
  avatarColor: string
}

export interface PageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface Supplier {
  id: number | string
  code: string
  name: string
  shortName?: string
  category: string
  categoryId?: number | string
  supplierType?: number
  status: number
  contact: string
  contactEmail?: string
  phone: string
  legalPerson?: string
  creditCode?: string
  province?: string
  city?: string
  district?: string
  admissionStage: string
  accountCount: number
  address: string
  bankName?: string
  bankAccount?: string
  taxNumber?: string
  remark?: string
  auditTime?: string
  auditRemark?: string
  createdAt?: string
  blacklisted?: boolean
  blacklistReason?: string
  blacklistStartTime?: string
  blacklistEndTime?: string
}

export interface PurchaseOrder {
  id: number | string
  orderNo: string
  supplierId?: number | string
  supplierName: string
  orderStatus?: number
  orderDate?: string
  buyer: string
  buyerId?: number | string
  amount: number
  currency?: string
  taxAmount?: number
  discountAmount?: number
  payAmount?: number
  deliveryDate: string
  deliveryAddress?: string
  paymentTerms?: string
  confirmTime?: string
  remark?: string
  status: string
  confirmStatus: string
  riskLevel: string
  /** 订单明细总数量 */
  totalQty?: number
  /** 已发货总数量 */
  shippedQty?: number
  /** 已收货总数量 */
  receivedQty?: number
  /** 在途总数量 */
  inTransitQty?: number
  /** 订单明细行列表 */
  details?: OrderDetailLine[]
}

/** 采购订单明细行（用于弹窗展示） */
export interface OrderDetailLine {
  id: number | string
  lineNo: number
  materialCode: string
  materialName: string
  materialSpec?: string
  unit?: string
  quantity: number
  unitPrice?: number
  amount?: number
  deliveredQty?: number
  receivedQty?: number
  deliveryDate?: string
}

export interface AsnNotice {
  id: number | string
  asnNo: string
  orderNo: string
  supplierName: string
  shipDate: string
  eta: string
  status: string
  quantity: number
  warehouse: string
}

export interface QualityCase {
  id: number | string
  caseNo: string
  type: string
  supplierName: string
  severity: string
  status: string
  inspectResult: number
  owner: string
  createdAt: string
  description: string
}

export interface Settlement {
  id: number | string
  statementNo: string
  supplierName: string
  period: string
  amount: number
  diffAmount: number
  status: string
  invoiceStatus: string
  paymentStatus: string
}

export interface TimelineItem {
  title: string
  content: string
  time: string
}

export interface PortalTodo {
  id: number | string
  title: string
  module: string
  businessType: string
  businessId: number | string
  businessNo: string
  priority: string
  dueDate: string
  createTime: string
  status: string
}

export interface AttachmentFile {
  id: number | string
  fileName: string
  version: string
  size: string
  uploader: string
  uploadedAt: string
  category: string
  businessType?: string
  businessId?: number | string
}

export interface ImportExportTask {
  id: number | string
  taskNo: string
  type: 'import' | 'export'
  module: string
  status: string
  total: number
  success: number
  failed: number
  createdAt: string
}

export interface OperationLog {
  id: number | string
  action: string
  operator: string
  result: string
  remark: string
  operatedAt: string
}

export interface Certificate {
  id: number | string
  name: string
  certNo: string
  expireDate: string
  status: string
}

// 所有新类型已在文件末尾定义，此处不再重复。
// 页面视图使用下方 Record 系列类型。

export interface RfqItem {
  id: number | string
  rfqNo: string
  rfqTitle: string
  orgId?: number | string
  currency: string
  quoteDeadline: string
  rfqStatus: number
  publishTime?: string
  closeTime?: string
  remark?: string
  createTime: string
}

export interface QuoteItem {
  id: number | string
  quoteNo: string
  rfqId: number | string
  supplierId: number | string
  currency: string
  totalAmount: number
  taxAmount: number
  quoteStatus: number
  submitTime?: string
  validUntil?: string
  remark?: string
  createTime: string
}

export interface NcrItem {
  id: number | string
  ncrNo: string
  supplierId: number | string
  materialCode?: string
  materialName?: string
  unqualifiedQty: number
  problemDesc: string
  severity: number
  ncrStatus: number
  handleMethod?: number
  submitTime?: string
  closeTime?: string
  createTime: string
}

export interface EightDItem {
  id: number | string
  reportNo: string
  ncrId: number | string
  supplierId: number | string
  d1Team?: string
  d2Problem?: string
  d3Containment?: string
  d4RootCause?: string
  d5CorrectiveAction?: string
  d6ValidateAction?: string
  d7PreventAction?: string
  d8CloseSummary?: string
  dueDate?: string
  reportStatus: number
  submitTime?: string
  auditTime?: string
  closeTime?: string
  createTime: string
}

export interface AppealItem {
  id: number | string
  appealNo: string
  ncrId?: number | string
  inspectionId?: number | string
  supplierId: number | string
  appealReason: string
  appealStatus: number
  submitTime?: string
  auditBy?: number | string
  auditTime?: string
  auditRemark?: string
  createTime: string
}

export interface InvoiceItem {
  id: number | string
  invoiceNo: string
  invoiceCode?: string
  invoiceType: number
  reconId?: number | string
  supplierId: number | string
  supplierName?: string
  taxNumber?: string
  invoiceAmount: number
  taxAmount: number
  invoiceDate?: string
  invoiceStatus: number
  receiveTime?: string
  certifyTime?: string
  remark?: string
  createTime: string
}

export interface PaymentItem {
  id: number | string
  paymentNo: string
  invoiceId?: number | string
  invoiceNo?: string
  supplierId: number | string
  supplierName?: string
  paymentAmount: number
  paymentMethod: number
  paymentStatus: number
  paymentTime?: string
  receiptNo?: string
  approveStatus: number
  createTime: string
}

export interface DeductionItem {
  id: number | string
  deductionNo: string
  supplierId: number | string
  sourceType: string
  deductionType: number
  deductionAmount: number
  deductionReason: string
  deductionStatus: number
  reconId?: number | string
  createTime: string
}

export interface VmiInventoryItem {
  id: number | string
  supplierId: number | string
  materialCode: string
  warehouseId?: number | string
  warehouseName?: string
  onhandQty: number
  availableQty: number
  safetyQty: number
  maxQty?: number
  inventoryStatus: number
  lastSyncTime?: string
  createTime: string
}

export interface ForecastDemandItem {
  id: number | string
  demandNo: string
  supplierId?: number | string
  materialCode: string
  demandDate: string
  demandQty: number
  demandType: number
  demandStatus: number
  createTime: string
}

export interface IntegrationEndpointItem {
  id: number | string
  endpointCode: string
  endpointName: string
  systemType: string
  integrationMode: string
  baseUrl?: string
  authType?: string
  timeoutMs: number
  retryLimit: number
  status: number
  remark?: string
  createTime: string
}

export interface IntegrationLogItem {
  id: number | string
  traceId?: string
  endpointCode?: string
  systemType: string
  interfaceCode: string
  direction: number
  businessType?: string
  businessId?: number | string
  requestSummary?: string
  responseSummary?: string
  resultStatus: number
  errorMessage?: string
  costMs?: number
  retryCount: number
  createTime: string
}

export interface SyncTaskItem {
  id: number | string
  taskNo: string
  systemType: string
  taskType: string
  externalNo?: string
  eventType?: string
  taskStatus: number
  retryCount: number
  nextRetryTime?: string
  errorMessage?: string
  createTime: string
}

export interface SupplierQualificationItem {
  id: number | string
  supplierId: number | string
  qualType: string
  qualName: string
  qualNo?: string
  qualOrg?: string
  validStart?: string
  validEnd?: string
  fileId?: number | string
  status: number
  remindDays: number
  remark?: string
  createTime: string
}

/** 采购方发布的RFQ询价单 */
export interface RfqRecord {
  id: number | string
  rfqNo: string
  rfqTitle: string
  currency: string
  quoteDeadline: string
  rfqStatus: number
  publishTime: string
  remark?: string
}

/** 供应商报价记录 */
export interface QuoteRecord {
  id: number | string
  quoteNo: string
  rfqId: number | string
  rfqNo?: string
  rfqTitle?: string
  supplierId?: number | string
  supplierName: string
  currency: string
  exchangeRate?: number
  totalAmount: number
  taxAmount: number
  quoteStatus: number
  negotiationRound?: number
  paymentTerms?: string
  validUntil?: string
  remark?: string
  submitTime?: string
  createTime?: string
}

/** 询价单概要（用于报价对比页左侧列表） */
export interface RfqSummaryRecord {
  id: number | string
  rfqNo: string
  rfqTitle: string
  currency: string
  quoteDeadline?: string
  rfqStatus: number
  publishTime?: string
  closeTime?: string
  remark?: string
  createTime?: string
  /** 该询价单下的报价数量 */
  quoteCount: number
  /** 最后一次报价时间 */
  latestQuoteTime?: string
}

/** NCR质量异常 */
export interface NcrRecord {
  id: number | string
  ncrNo: string
  materialCode?: string
  materialName: string
  unqualifiedQty: number
  severity: string
  ncrStatus: number
  handleMethod?: number
  problemDesc: string
  createTime: string
  submitTime?: string
  closeTime?: string
}

/** 8D整改报告 */
export interface EightDReport {
  id: number | string
  reportNo: string
  ncrId: number | string
  ncrNo?: string
  supplierId?: number | string
  d1Team?: string
  d2Problem?: string
  d3Containment?: string
  d4RootCause?: string
  d5CorrectiveAction?: string
  d6ValidateAction?: string
  d7PreventAction?: string
  d8CloseSummary?: string
  dueDate: string
  currentStep?: number
  stepDueDate?: string
  reportStatus: number
  submitTime?: string
  auditTime?: string
  closeTime?: string
  createTime: string
}

/** 质量申诉记录 */
export interface QualityAppeal {
  id: number | string
  appealNo: string
  ncrId?: number | string
  inspectionId?: number | string
  supplierId?: number | string
  appealReason: string
  appealStatus: number
  submitTime?: string
  auditBy?: number | string
  auditTime?: string
  auditRemark?: string
  createTime: string
}

/** 发票记录 */
export interface InvoiceRecord {
  id: number | string
  invoiceNo: string
  invoiceAmount: number
  taxAmount: number
  invoiceDate: string
  invoiceStatus: number
}

/** 扣款记录 */
export interface DeductionRecord {
  id: number | string
  deductionNo: string
  deductionType: string
  deductionAmount: number
  deductionReason: string
  deductionStatus: number
}

/** 付款记录 */
export interface PaymentRecord {
  id: number | string
  paymentNo: string
  invoiceNo: string
  paymentAmount: number
  paymentStatus: number
  paymentTime: string
}

/** IQC 检验标准 */
export interface InspectionStandard {
  id: number | string
  standardNo: string
  standardName: string
  materialCode: string
  materialName: string
  checkItem: string
  checkMethod: string
  sampleRule: string
  sampleQty: number
  acValue: number
  reValue: number
  lowerLimit: string
  upperLimit: string
  unit: string
  severity: string
  status: number
  createTime: string
  updateTime: string
}

/** 8D节点时间线记录 */
export interface EightDTimeline {
  stage: string
  stageName: string
  content: string
  completedTime: string
  dueDate: string
  status: 'completed' | 'in_progress' | 'pending' | 'overdue'
  operator: string
}

/** 收货记录 */
export interface ReceiptRecord {
  id: number | string
  receiptNo: string
  asnId: number | string
  asnNo: string
  supplierName: string
  materialCode: string
  materialName: string
  orderQty: number
  receivedQty: number
  diffQty: number
  diffReason: string
  receiptTime: string
  status: number
  createTime: string
}

/** ASN 送货明细行 */
export interface DeliveryLineItem {
  id?: number | string
  lineNo: number
  materialCode: string
  materialName: string
  orderLineNo: number
  orderDetailId?: number | string | null
  unit: string
  orderQty: number
  shippedQty: number
  shipQty: number
  batchNo: string
  caseNo: string
  qtyPerCase: number
  barcode: string
  remark: string
}

/** 收货差异记录 */
export interface ReceiveDiscrepancy {
  asnId: number | string
  asnNo: string
  receivedQty: number
  discrepantQty: number
  discrepancyReason: string
  discrepancyType: string
  handler: string
  remark: string
}

/** 冲销/调整单 */
export interface WriteOffRecord {
  id: number | string
  writeOffNo: string
  asnNo: string
  orderNo: string
  supplierName: string
  writeOffType: string
  amount: number
  reason: string
  status: string
  createTime: string
}

// ========== RFQ 物料明细 ==========
export interface RfqLineItem {
  id?: number | string
  rfqId?: number | string
  lineNo: number
  materialCode: string
  materialName: string
  spec: string
  unit: string
  quantity: number
  deliveryDate: string
  remark?: string
}

// ========== RFQ 供应商邀请 ==========
export interface RfqSupplierInvite {
  rfqId: number | string
  supplierId: number | string
  supplierName: string
  inviteStatus: number // 0 待确认, 1 已确认, 2 已拒绝
}

// ========== 报价明细行 ==========
export interface QuoteLineItem {
  id?: number | string
  quoteId?: number | string
  rfqLineId: number | string
  materialCode: string
  materialName: string
  spec: string
  unit: string
  quantity: number
  unitPrice: number
  totalPrice: number
  deliveryDate: string
  paymentTerms?: string
  remark?: string
}

// ========== 议价记录 ==========
export interface BargainRecord {
  id: number | string
  quoteId: number | string
  rfqId: number | string
  fromUserType: 'buyer' | 'supplier'
  fromUserName: string
  action: string // 'request_reprice' | 'resubmit' | 'accept' | 'reject'
  message: string
  targetPrice?: number  // 采购方目标价
  supplierPrice?: number  // 供应商报价
  createTime: string
}

// ========== 汇率 ==========
export interface ExchangeRate {
  id: number | string
  fromCurrency: string
  toCurrency: string
  rate: number
  effectiveDate: string
  updateTime: string
}

// ========== 报价附件 ==========
export interface QuoteAttachment {
  id: number | string
  quoteId: number | string
  fileName: string
  version: number
  size: string
  uploader: string
  uploadedAt: string
  category: string
  fileId: number | string
}

// ========== 供应商黑名单 ==========
export interface SupplierBlacklist {
  id: number | string
  supplierId: number | string
  supplierName: string
  creditCode?: string
  reason: string
  startTime: string
  endTime?: string
  status: number // 0=已解除, 1=生效中
  createTime: string
}

// ========== 供应商注册表单 ==========
export interface SupplierRegisterForm {
  supplierName: string
  creditCode: string
  contactName: string
  contactPhone: string
  contactEmail?: string
  address?: string
  legalPerson?: string
  supplierType?: number
  remark?: string
}

// ========== 账户创建与角色分配 ==========
export interface SupplierAccountCreateForm {
  supplierId: number | string
  username: string
  password: string
  realName: string
  roleIds: number[]
}

// ========== 物料主数据 ==========
export interface Material {
  id: number | string
  code: string
  name: string
  spec: string
  unit: string
  category: string
  status: number
  createTime: string
}
