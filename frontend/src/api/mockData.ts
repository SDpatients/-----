import type { AsnNotice, AttachmentFile, Certificate, ImportExportTask, OperationLog, PortalTodo, PurchaseOrder, QualityCase, QuoteRecord, RfqRecord, Settlement, Supplier, SupplierQualificationItem, TimelineItem, UserInfo } from '@/types/business'

export const currentUser: UserInfo = {
  id: 1,
  username: 'admin',
  realName: '系统管理员',
  roleName: '平台管理员',
  department: '供应链数字化中心',
  userType: 'internal',
  avatarColor: '#1f5eff',
}

export const supplierUser: UserInfo = {
  id: 2,
  username: 'supplier',
  realName: '周明',
  roleName: '供应商主账号',
  department: '华东精密制造有限公司',
  userType: 'supplier',
  avatarColor: '#0bb783',
}

export const permissions = ['dashboard:view', 'supplier:view', 'supplier:audit', 'order:view', 'order:confirm', 'order:change', 'asn:view', 'asn:create', 'quality:view', 'settlement:view', 'rfq:view', 'quote:view', 'inventory:view', 'integration:view', 'config:view', 'finance:view', 'message:view', 'file:upload', 'logistics:view']

export const supplierPermissions = ['supplier:dashboard:view', 'supplier:order:view', 'supplier:order:confirm', 'supplier:delivery:view', 'supplier:delivery:create', 'supplier:quality:view', 'supplier:quality:rectify', 'supplier:settlement:view', 'supplier:rfq:view', 'supplier:profile:view', 'message:view', 'file:upload']

export const suppliers: Supplier[] = [
  { id: 1, code: 'SUP-2026-001', name: '华东精密制造有限公司', category: '结构件', status: 1, contact: '周明', phone: '13800010001', admissionStage: '已准入', accountCount: 3, address: '江苏省苏州市工业园区' },
  { id: 2, code: 'SUP-2026-002', name: '星河电子科技股份有限公司', category: '电子件', status: 0, contact: '林夏', phone: '13800010002', admissionStage: '资质审核', accountCount: 2, address: '广东省深圳市南山区' },
  { id: 3, code: 'SUP-2026-003', name: '北辰包装材料有限公司', category: '包装材料', status: 2, contact: '赵青', phone: '13800010003', admissionStage: '整改中', accountCount: 1, address: '浙江省杭州市余杭区' },
  { id: 4, code: 'SUP-2026-004', name: '远航物流装备有限公司', category: '物流装备', status: 1, contact: '陈一', phone: '13800010004', admissionStage: '已准入', accountCount: 0, address: '上海市嘉定区' },
]

export const orders: PurchaseOrder[] = [
  { id: 1, orderNo: 'PO-202605-0001', supplierName: '华东精密制造有限公司', buyer: '王采购', amount: 286000, deliveryDate: '2026-05-28', status: 'confirmed', confirmStatus: '已接单', riskLevel: 'low' },
  { id: 2, orderNo: 'PO-202605-0002', supplierName: '星河电子科技股份有限公司', buyer: '李采购', amount: 458000, deliveryDate: '2026-06-02', status: 'changed', confirmStatus: '待确认', riskLevel: 'medium' },
  { id: 3, orderNo: 'PO-202605-0003', supplierName: '北辰包装材料有限公司', buyer: '王采购', amount: 76000, deliveryDate: '2026-05-25', status: 'exception', confirmStatus: '已驳回', riskLevel: 'high' },
  { id: 4, orderNo: 'PO-202605-0004', supplierName: '远航物流装备有限公司', buyer: '张采购', amount: 198000, deliveryDate: '2026-06-08', status: 'draft', confirmStatus: '未发送', riskLevel: 'none' },
]

export const asnNotices: AsnNotice[] = [
  { id: 1, asnNo: 'ASN-202605-0001', orderNo: 'PO-202605-0001', supplierName: '华东精密制造有限公司', shipDate: '2026-05-21', eta: '2026-05-23', status: 'shipped', quantity: 1200, warehouse: '华东一仓' },
  { id: 2, asnNo: 'ASN-202605-0002', orderNo: 'PO-202605-0002', supplierName: '星河电子科技股份有限公司', shipDate: '2026-05-22', eta: '2026-05-24', status: 'draft', quantity: 800, warehouse: '华南中心仓' },
  { id: 3, asnNo: 'ASN-202605-0003', orderNo: 'PO-202605-0003', supplierName: '北辰包装材料有限公司', shipDate: '2026-05-19', eta: '2026-05-21', status: 'exception', quantity: 3600, warehouse: '华东二仓' },
  { id: 4, asnNo: 'ASN-202605-0004', orderNo: 'PO-202605-0004', supplierName: '远航物流装备有限公司', shipDate: '2026-05-18', eta: '2026-05-20', status: 'received', quantity: 240, warehouse: '总装仓' },
]

export const qualityCases: QualityCase[] = [
  { id: 1, caseNo: 'QC-202605-0001', type: '来料检验', supplierName: '华东精密制造有限公司', severity: 'medium', status: 'processing', inspectResult: 0, owner: '刘质检', createdAt: '2026-05-20', description: '尺寸公差接近上限，需补充过程能力报告' },
  { id: 2, caseNo: 'NCR-202605-0002', type: 'NCR', supplierName: '北辰包装材料有限公司', severity: 'high', status: 'exception', inspectResult: 2, owner: '陈质量', createdAt: '2026-05-19', description: '包装破损率超出协议标准' },
  { id: 3, caseNo: '8D-202605-0003', type: '8D整改', supplierName: '星河电子科技股份有限公司', severity: 'medium', status: 'processing', inspectResult: 0, owner: '刘质检', createdAt: '2026-05-18', description: '批次追溯资料不完整，要求 8D 回复' },
  { id: 4, caseNo: 'AP-202605-0004', type: '质量申诉', supplierName: '远航物流装备有限公司', severity: 'low', status: 'closed', inspectResult: 3, owner: '陈质量', createdAt: '2026-05-15', description: '供应商对判定结果提交申诉，已复核关闭' },
]

export const settlements: Settlement[] = [
  { id: 1, statementNo: 'ST-202605-0001', supplierName: '华东精密制造有限公司', period: '2026-05', amount: 286000, diffAmount: 0, status: 'reconciled', invoiceStatus: 'invoiced', paymentStatus: 'paid' },
  { id: 2, statementNo: 'ST-202605-0002', supplierName: '星河电子科技股份有限公司', period: '2026-05', amount: 458000, diffAmount: 3200, status: 'processing', invoiceStatus: 'draft', paymentStatus: 'processing' },
  { id: 3, statementNo: 'ST-202605-0003', supplierName: '北辰包装材料有限公司', period: '2026-05', amount: 76000, diffAmount: 860, status: 'exception', invoiceStatus: 'draft', paymentStatus: 'draft' },
  { id: 4, statementNo: 'ST-202605-0004', supplierName: '远航物流装备有限公司', period: '2026-04', amount: 198000, diffAmount: 0, status: 'reconciled', invoiceStatus: 'invoiced', paymentStatus: 'processing' },
]

export const timeline: TimelineItem[] = [
  { title: '创建单据', content: '系统生成业务单据并完成基础校验', time: '2026-05-18 09:20' },
  { title: '供应商确认', content: '供应商门户完成信息确认', time: '2026-05-19 14:10' },
  { title: '内部复核', content: '采购方完成业务复核并进入下一节点', time: '2026-05-20 16:35' },
]

export const portalTodos: PortalTodo[] = [
  { id: 1, title: 'PO-202605-0002 待确认交期', module: '采购订单', businessType: 'purchase_order', businessId: 1, businessNo: 'PO-202605-0002', priority: 'high', dueDate: '2026-05-22', createTime: '2026-05-20 09:00', status: 'pending' },
  { id: 2, title: 'ASN-202605-0002 待提交发货明细', module: '送货通知', businessType: 'delivery_notice', businessId: 2, businessNo: 'ASN-202605-0002', priority: 'medium', dueDate: '2026-05-23', createTime: '2026-05-21 10:00', status: 'pending' },
  { id: 3, title: '8D-202605-0003 待提交整改报告', module: '不合格报告', businessType: 'nonconformance_report', businessId: 1, businessNo: '8D-202605-0003', priority: 'high', dueDate: '2026-05-24', createTime: '2026-05-20 14:00', status: 'processing' },
  { id: 4, title: 'ST-202605-0002 对账差异待确认', module: '财务对账', businessType: 'reconciliation', businessId: 1, businessNo: 'ST-202605-0002', priority: 'medium', dueDate: '2026-05-25', createTime: '2026-05-19 16:00', status: 'processing' },
]

export const attachments: AttachmentFile[] = [
  { id: 1, fileName: '供应商准入资料.pdf', version: 'V3', size: '2.4MB', uploader: '周明', uploadedAt: '2026-05-18 10:12', category: '资质附件', businessType: 'supplier', businessId: 1 },
  { id: 2, fileName: 'PO-202605-0001确认回执.xlsx', version: 'V1', size: '860KB', uploader: '林夏', uploadedAt: '2026-05-19 15:30', category: '订单附件', businessType: 'purchase_order', businessId: 1 },
  { id: 3, fileName: '8D整改报告.docx', version: 'V2', size: '1.8MB', uploader: '周明', uploadedAt: '2026-05-20 11:04', category: '质量附件', businessType: 'quality_inspection', businessId: 1 },
  // RFQ 询价单附件
  { id: 101, fileName: 'RFQ技术规格说明书.pdf', version: 'V2', size: '3.2MB', uploader: '王采购', uploadedAt: '2026-05-21 09:15', category: 'RFQ附件', businessType: 'rfq', businessId: 2062062163412926466 },
  { id: 102, fileName: 'RFQ图纸_v2.dwg', version: 'V1', size: '5.6MB', uploader: '王采购', uploadedAt: '2026-05-21 09:18', category: 'RFQ附件', businessType: 'rfq', businessId: 2062062163412926466 },
  { id: 103, fileName: '物料BOM清单.xlsx', version: 'V3', size: '420KB', uploader: '王采购', uploadedAt: '2026-05-22 14:32', category: 'RFQ附件', businessType: 'rfq', businessId: 2062062163412926466 },
  { id: 104, fileName: '质量检验标准.pdf', version: 'V1', size: '1.2MB', uploader: '李工', uploadedAt: '2026-05-22 16:05', category: 'RFQ附件', businessType: 'rfq', businessId: 2062062163412926466 },
  { id: 105, fileName: '样品图片.zip', version: 'V1', size: '8.4MB', uploader: '林夏', uploadedAt: '2026-05-23 10:20', category: 'RFQ附件', businessType: 'rfq', businessId: 2062062163412926466 },
]

export const importExportTasks: ImportExportTask[] = [
  { id: 1, taskNo: 'EXP-202605-0001', type: 'export', module: '采购订单', status: 'closed', total: 1200, success: 1200, failed: 0, createdAt: '2026-05-20 09:20' },
  { id: 2, taskNo: 'IMP-202605-0002', type: 'import', module: 'ASN明细', status: 'exception', total: 200, success: 184, failed: 16, createdAt: '2026-05-20 13:45' },
  { id: 3, taskNo: 'EXP-202605-0003', type: 'export', module: '对账单', status: 'processing', total: 460, success: 300, failed: 0, createdAt: '2026-05-21 08:50' },
]

export const operationLogs: OperationLog[] = [
  { id: 1, action: '提交', operator: '周明', result: '成功', remark: '供应商提交确认信息', operatedAt: '2026-05-18 09:20' },
  { id: 2, action: '复核', operator: '王采购', result: '通过', remark: '采购方复核通过', operatedAt: '2026-05-19 14:12' },
  { id: 3, action: '归档', operator: '系统', result: '完成', remark: '生成业务追溯记录', operatedAt: '2026-05-20 16:35' },
]

export const certificates: Certificate[] = [
  { id: 1, name: 'ISO9001 质量管理体系认证', certNo: 'ISO-2026-001', expireDate: '2026-10-31', status: 'active' },
  { id: 2, name: '环境管理体系认证', certNo: 'EMS-2026-013', expireDate: '2026-07-15', status: 'pending' },
  { id: 3, name: '营业执照', certNo: 'LIC-9132XXXX', expireDate: '长期有效', status: 'active' },
]

/** 供应商资质证书 mock 数据 */
export const supplierQualifications: SupplierQualificationItem[] = [
  { id: 1, supplierId: 1, qualType: 'iso9001', qualName: 'ISO9001 质量管理体系认证', qualNo: 'ISO-2026-001', qualOrg: 'SGS认证中心', validStart: '2025-03-15', validEnd: '2028-03-14', status: 1, remindDays: 30, remark: '', createTime: '2025-03-15' },
  { id: 2, supplierId: 1, qualType: 'business_license', qualName: '企业营业执照', qualNo: '91320500MA1XXXXXX', qualOrg: '苏州市市场监督管理局', validStart: '2023-01-01', validEnd: '长期有效', status: 1, remindDays: 30, remark: '', createTime: '2023-01-01' },
  { id: 3, supplierId: 2, qualType: 'iatf16949', qualName: 'IATF16949 汽车行业质量管理体系', qualNo: 'IATF-2025-088', qualOrg: 'TUV认证中心', validStart: '2025-06-01', validEnd: '2028-05-31', status: 1, remindDays: 60, remark: '汽车零部件专用', createTime: '2025-06-01' },
  { id: 4, supplierId: 2, qualType: 'iso14001', qualName: 'ISO14001 环境管理体系认证', qualNo: 'EMS-2025-102', qualOrg: 'BSI认证中心', validStart: '2025-01-10', validEnd: '2026-07-15', status: 2, remindDays: 30, remark: '注意续期', createTime: '2025-01-10' },
  { id: 5, supplierId: 3, qualType: 'production_license', qualName: '包装材料生产许可证', qualNo: 'XK16-204-00123', qualOrg: '浙江省市场监督管理局', validStart: '2024-08-01', validEnd: '2027-07-31', status: 1, remindDays: 45, remark: '', createTime: '2024-08-01' },
]

/** 订单明细行 */
export interface OrderDetailLine {
  id: number
  lineNo: number
  materialCode: string
  materialName: string
  materialSpec: string
  unit: string
  quantity: number
  unitPrice: number
  amount: number
  deliveryDate: string
  receivedQty: number
  remark: string
}

export const orderDetails: OrderDetailLine[] = [
  { id: 1, lineNo: 10, materialCode: 'MAT-001', materialName: '精密铸件A型', materialSpec: '300x200x50mm', unit: '件', quantity: 500, unitPrice: 320, amount: 160000, deliveryDate: '2026-05-28', receivedQty: 0, remark: '' },
  { id: 2, lineNo: 20, materialCode: 'MAT-002', materialName: '不锈钢法兰', materialSpec: 'DN50 PN16', unit: '套', quantity: 200, unitPrice: 180, amount: 36000, deliveryDate: '2026-05-30', receivedQty: 50, remark: '急单' },
  { id: 3, lineNo: 30, materialCode: 'MAT-003', materialName: '密封垫片', materialSpec: 'Φ100x3mm', unit: '片', quantity: 1000, unitPrice: 45, amount: 45000, deliveryDate: '2026-06-05', receivedQty: 0, remark: '' },
  { id: 4, lineNo: 40, materialCode: 'MAT-004', materialName: '紧固螺栓M16', materialSpec: 'M16x80 8.8级', unit: '套', quantity: 2000, unitPrice: 22.5, amount: 45000, deliveryDate: '2026-06-02', receivedQty: 500, remark: '' },
]

/** 送货明细行 */
export interface DeliveryDetailLine {
  id: number
  lineNo: number
  materialCode: string
  materialName: string
  orderLineNo: number
  unit: string
  orderQty: number
  shipQty: number
  batchNo: string
  remark: string
}

export const deliveryDetails: DeliveryDetailLine[] = [
  { id: 1, lineNo: 1, materialCode: 'MAT-001', materialName: '精密铸件A型', orderLineNo: 10, unit: '件', orderQty: 500, shipQty: 200, batchNo: 'B20260518-01', remark: '第一批' },
  { id: 2, lineNo: 2, materialCode: 'MAT-001', materialName: '精密铸件A型', orderLineNo: 10, unit: '件', orderQty: 500, shipQty: 300, batchNo: 'B20260520-01', remark: '第二批' },
  { id: 3, lineNo: 3, materialCode: 'MAT-004', materialName: '紧固螺栓M16', orderLineNo: 40, unit: '套', orderQty: 2000, shipQty: 500, batchNo: 'B20260518-02', remark: '' },
]

/** 质量检验明细 */
export interface InspectionDetailLine {
  id: number
  lineNo: number
  checkItem: string
  standard: string
  measuredValue: string
  result: string
  inspector: string
  inspectDate: string
}

export const inspectionDetails: InspectionDetailLine[] = [
  { id: 1, lineNo: 1, checkItem: '外观检查', standard: '无裂纹、气孔、夹渣', measuredValue: '表面光洁，无可见缺陷', result: '合格', inspector: '刘质检', inspectDate: '2026-05-20' },
  { id: 2, lineNo: 2, checkItem: '尺寸公差', standard: '300±0.5mm', measuredValue: '300.82mm', result: '不合格（超上差）', inspector: '刘质检', inspectDate: '2026-05-20' },
  { id: 3, lineNo: 3, checkItem: '硬度测试', standard: 'HB 180-220', measuredValue: 'HB 205', result: '合格', inspector: '陈质量', inspectDate: '2026-05-20' },
  { id: 4, lineNo: 4, checkItem: '化学成分', standard: 'C≤0.25%, Mn≤0.9%', measuredValue: 'C 0.22%, Mn 0.75%', result: '合格', inspector: '陈质量', inspectDate: '2026-05-21' },
]

/** 对账明细行 */
export interface ReconDetailLine {
  id: number
  lineNo: number
  businessType: string
  businessNo: string
  occurDate: string
  amount: number
  confirmedAmount: number
  diffAmount: number
  diffReason: string
}

/** 三单匹配数据：订单行 vs 收货行 vs 发票行 */
export interface ThreeWayMatchItem {
  id: number
  lineNo: number
  materialCode: string
  materialName: string
  unit: string
  orderQty: number
  orderAmount: number
  receivedQty: number
  receivedAmount: number
  invoicedQty: number
  invoicedAmount: number
  matchStatus: 'matched' | 'partial' | 'unmatched'
  diffDescription: string
}

export interface ThreeWayMatchData {
  reconId: number | string
  items: ThreeWayMatchItem[]
  summary: {
    totalOrderAmount: number
    totalReceivedAmount: number
    totalInvoicedAmount: number
    matchedCount: number
    partialCount: number
    unmatchedCount: number
  }
}

/** OCR 发票识别结果 */
export interface OcrResult {
  invoiceNo: string
  invoiceCode: string
  invoiceDate: string
  invoiceAmount: number
  taxAmount: number
  sellerName: string
  buyerName: string
  confidence: number
  rawText: string
}

/** 付款回传状态日志 */
export interface PaymentCallbackLog {
  id: number
  paymentId: number
  callbackStatus: number // 0 待回传, 1 回传中, 2 回传成功, 3 回传失败
  callbackTime: string
  fundSystem: string
  fundPaymentNo: string
  responseMessage: string
  retryCount: number
  createTime: string
}

/** 三单匹配 mock 数据 */
export const threeWayMatchData: ThreeWayMatchData = {
  reconId: 1,
  items: [
    { id: 1, lineNo: 10, materialCode: 'MAT-001', materialName: '精密铸件A型', unit: '件', orderQty: 500, orderAmount: 160000, receivedQty: 500, receivedAmount: 160000, invoicedQty: 500, invoicedAmount: 160000, matchStatus: 'matched', diffDescription: '' },
    { id: 2, lineNo: 20, materialCode: 'MAT-002', materialName: '不锈钢法兰', unit: '套', orderQty: 200, orderAmount: 36000, receivedQty: 150, receivedAmount: 27000, invoicedQty: 150, invoicedAmount: 27000, matchStatus: 'partial', diffDescription: '收货短少50套，待补货' },
    { id: 3, lineNo: 30, materialCode: 'MAT-003', materialName: '密封垫片', unit: '片', orderQty: 1000, orderAmount: 45000, receivedQty: 1000, receivedAmount: 45000, invoicedQty: 1000, invoicedAmount: 45000, matchStatus: 'matched', diffDescription: '' },
    { id: 4, lineNo: 40, materialCode: 'MAT-004', materialName: '紧固螺栓M16', unit: '套', orderQty: 2000, orderAmount: 45000, receivedQty: 2000, receivedAmount: 45000, invoicedQty: 1800, invoicedAmount: 40500, matchStatus: 'partial', diffDescription: '发票少开200套' },
    { id: 5, lineNo: 50, materialCode: 'MAT-005', materialName: '液压阀组件', unit: '台', orderQty: 100, orderAmount: 88000, receivedQty: 0, receivedAmount: 0, invoicedQty: 0, invoicedAmount: 0, matchStatus: 'unmatched', diffDescription: '未发货也未开票' },
  ],
  summary: {
    totalOrderAmount: 374000,
    totalReceivedAmount: 277000,
    totalInvoicedAmount: 272500,
    matchedCount: 2,
    partialCount: 2,
    unmatchedCount: 1,
  },
}

/** OCR mock 结果 */
export const mockOcrResult: OcrResult = {
  invoiceNo: '12345678',
  invoiceCode: '044001900111',
  invoiceDate: '2026-05-15',
  invoiceAmount: 160000,
  taxAmount: 20800,
  sellerName: '华东精密制造有限公司',
  buyerName: 'XX供应链科技有限公司',
  confidence: 0.97,
  rawText: '发票号码: 12345678\n发票代码: 044001900111\n开票日期: 2026-05-15\n金额: 160000.00\n税额: 20800.00\n销方: 华东精密制造有限公司\n购方: XX供应链科技有限公司',
}

/** 付款回传日志 mock 数据 */
export const mockCallbackLogs: PaymentCallbackLog[] = [
  { id: 1, paymentId: 1, callbackStatus: 3, callbackTime: '2026-05-20 10:30:00', fundSystem: 'ERP资金系统', fundPaymentNo: 'FPM20260520001', responseMessage: '付款成功，资金已划拨', retryCount: 0, createTime: '2026-05-20 10:30:00' },
  { id: 2, paymentId: 1, callbackStatus: 2, callbackTime: '2026-05-20 10:25:00', fundSystem: 'ERP资金系统', fundPaymentNo: '', responseMessage: '等待资金系统处理中', retryCount: 1, createTime: '2026-05-20 10:25:00' },
  { id: 3, paymentId: 1, callbackStatus: 1, callbackTime: '2026-05-20 10:15:00', fundSystem: 'ERP资金系统', fundPaymentNo: '', responseMessage: '已提交付款请求至资金系统', retryCount: 0, createTime: '2026-05-20 10:15:00' },
]

/** RFQ 询价单 mock 数据 */
export const rfqRecords: RfqRecord[] = [
  { id: 1, rfqNo: 'RFQ-202605-0001', rfqTitle: '精密铸件A型年度询价', currency: 'CNY', quoteDeadline: '2026-05-30 18:00', rfqStatus: 2, publishTime: '2026-05-20 09:00' },
  { id: 2, rfqNo: 'RFQ-202605-0002', rfqTitle: '不锈钢法兰DN50询价', currency: 'CNY', quoteDeadline: '2026-06-05 12:00', rfqStatus: 1, publishTime: '2026-05-22 14:30' },
  { id: 3, rfqNo: 'RFQ-202605-0003', rfqTitle: '密封垫片批量采购', currency: 'USD', quoteDeadline: '2026-05-25 17:00', rfqStatus: 3, publishTime: '2026-05-15 10:00' },
  { id: 4, rfqNo: 'RFQ-202605-0004', rfqTitle: '包装材料年度框架', currency: 'CNY', quoteDeadline: '2026-06-10 16:00', rfqStatus: 0, publishTime: '' },
]

/** 报价单 mock 数据 */
export const quoteRecords: QuoteRecord[] = [
  { id: 1, quoteNo: 'QT-202605-0001', rfqId: 1, rfqNo: 'RFQ-202605-0001', supplierName: '华东精密制造有限公司', currency: 'CNY', totalAmount: 156000, taxAmount: 20280, quoteStatus: 1, validUntil: '2026-06-30', remark: '含税含运费', submitTime: '2026-05-21 15:30' },
  { id: 2, quoteNo: 'QT-202605-0002', rfqId: 1, rfqNo: 'RFQ-202605-0001', supplierName: '远航物流装备有限公司', currency: 'CNY', totalAmount: 162000, taxAmount: 21060, quoteStatus: 2, validUntil: '2026-06-28', remark: '', submitTime: '2026-05-22 09:15' },
  { id: 3, quoteNo: 'QT-202605-0003', rfqId: 2, rfqNo: 'RFQ-202605-0002', supplierName: '星河电子科技股份有限公司', currency: 'CNY', totalAmount: 38000, taxAmount: 4940, quoteStatus: 1, validUntil: '2026-06-15', remark: '30天账期', submitTime: '2026-05-23 11:00' },
  { id: 4, quoteNo: 'QT-202605-0004', rfqId: 3, rfqNo: 'RFQ-202605-0003', supplierName: '北辰包装材料有限公司', currency: 'USD', totalAmount: 8500, taxAmount: 0, quoteStatus: 3, validUntil: '2026-05-31', remark: '', submitTime: '2026-05-18 08:45' },
]

/** 报价明细行 mock 数据 */
export const quoteLineItems = [
  { id: 1, quoteId: 1, rfqLineId: 1, materialCode: 'MAT-001', materialName: '精密铸件A型', spec: '300x200x50mm', unit: 'PCS', quantity: 500, unitPrice: 312, totalPrice: 156000, deliveryDate: '2026-06-15', paymentTerms: '30天账期' },
  { id: 2, quoteId: 2, rfqLineId: 1, materialCode: 'MAT-001', materialName: '精密铸件A型', spec: '300x200x50mm', unit: 'PCS', quantity: 500, unitPrice: 324, totalPrice: 162000, deliveryDate: '2026-06-18', paymentTerms: '款到发货' },
  { id: 3, quoteId: 3, rfqLineId: 1, materialCode: 'MAT-002', materialName: '不锈钢法兰', spec: 'DN50 PN16', unit: 'SET', quantity: 200, unitPrice: 190, totalPrice: 38000, deliveryDate: '2026-06-10', paymentTerms: '月结30天' },
]

/** 集成端点 mock 数据 */
export const integrationEndpoints = [
  { id: 1, endpointCode: 'ERP_INV', endpointName: 'ERP库存同步', systemType: 'ERP', integrationMode: 'REST', baseUrl: 'http://erp.internal/api/v2/inventory', timeoutMs: 30000, retryLimit: 3, status: 1 },
  { id: 2, endpointCode: 'MES_QUALITY', endpointName: 'MES质检结果', systemType: 'MES', integrationMode: 'SOAP', baseUrl: 'http://mes.internal/ws/quality', timeoutMs: 45000, retryLimit: 2, status: 1 },
  { id: 3, endpointCode: 'WMS_ASN', endpointName: 'WMS入库通知', systemType: 'WMS', integrationMode: 'REST', baseUrl: 'http://wms.internal/api/asn', timeoutMs: 20000, retryLimit: 5, status: 0 },
]

/** 集成调用日志 mock 数据 */
export const integrationLogs = [
  { id: 1, interfaceCode: 'ERP_INV_SYNC', systemType: 'ERP', direction: 1, resultStatus: 1, costMs: 234, errorMessage: '', createTime: '2026-05-24 08:30:15' },
  { id: 2, interfaceCode: 'MES_QUALITY_PUSH', systemType: 'MES', direction: 2, resultStatus: 1, costMs: 567, errorMessage: '', createTime: '2026-05-24 09:00:22' },
  { id: 3, interfaceCode: 'WMS_ASN_SYNC', systemType: 'WMS', direction: 1, resultStatus: 0, costMs: 3021, errorMessage: 'Connection timeout after 3000ms', createTime: '2026-05-24 10:15:08' },
]

/** 同步任务 mock 数据 */
export const syncTasks = [
  { id: 1, taskNo: 'SYNC-202605-0001', systemType: 'ERP', taskType: '库存同步', taskStatus: 2, retryCount: 0, errorMessage: '', nextRetryTime: '' },
  { id: 2, taskNo: 'SYNC-202605-0002', systemType: 'WMS', taskType: 'ASN入库', taskStatus: 4, retryCount: 3, errorMessage: '目标系统返回500', nextRetryTime: '2026-05-25 12:00:00' },
]

/** 财务对账 mock 数据 */
export const financialReconciliationRecords = [
  { id: 1, asnNo: 'ASN-202605-0001', orderNo: 'PO-202605-0001', supplierName: '华东精密制造有限公司', totalAmount: 286000, quantity: 1200, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-05', createDate: '2026-05-21' },
  { id: 2, asnNo: 'ASN-202605-0002', orderNo: 'PO-202605-0002', supplierName: '星河电子科技股份有限公司', totalAmount: 458000, quantity: 800, currency: 'CNY', reconciliationStatus: 1, reconciliationStatusLabel: '对账中', paymentStatus: 1, paymentStatusLabel: '部分付款', diffAmount: 3200, period: '2026-05', createDate: '2026-05-22' },
  { id: 3, asnNo: 'ASN-202605-0003', orderNo: 'PO-202605-0003', supplierName: '北辰包装材料有限公司', totalAmount: 76000, quantity: 3600, currency: 'CNY', reconciliationStatus: 3, reconciliationStatusLabel: '有差异', paymentStatus: 0, paymentStatusLabel: '未付款', diffAmount: 860, period: '2026-05', createDate: '2026-05-19' },
  { id: 4, asnNo: 'ASN-202605-0004', orderNo: 'PO-202605-0004', supplierName: '远航物流装备有限公司', totalAmount: 198000, quantity: 240, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 1, paymentStatusLabel: '部分付款', diffAmount: 0, period: '2026-04', createDate: '2026-05-18' },
  { id: 5, asnNo: 'ASN-202604-0005', orderNo: 'PO-202604-0005', supplierName: '华东精密制造有限公司', totalAmount: 195000, quantity: 960, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-04', createDate: '2026-04-15' },
  { id: 6, asnNo: 'ASN-202604-0006', orderNo: 'PO-202604-0006', supplierName: '星河电子科技股份有限公司', totalAmount: 342000, quantity: 1200, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-04', createDate: '2026-04-18' },
  { id: 7, asnNo: 'ASN-202604-0007', orderNo: 'PO-202604-0007', supplierName: '北辰包装材料有限公司', totalAmount: 58000, quantity: 2800, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-04', createDate: '2026-04-20' },
  { id: 8, asnNo: 'ASN-202603-0008', orderNo: 'PO-202603-0008', supplierName: '华东精密制造有限公司', totalAmount: 312000, quantity: 1500, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-03', createDate: '2026-03-10' },
  { id: 9, asnNo: 'ASN-202603-0009', orderNo: 'PO-202603-0009', supplierName: '远航物流装备有限公司', totalAmount: 165000, quantity: 300, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-03', createDate: '2026-03-15' },
  { id: 10, asnNo: 'ASN-202603-0010', orderNo: 'PO-202603-0010', supplierName: '星河电子科技股份有限公司', totalAmount: 228000, quantity: 640, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-03', createDate: '2026-03-20' },
  { id: 11, asnNo: 'ASN-202602-0011', orderNo: 'PO-202602-0011', supplierName: '华东精密制造有限公司', totalAmount: 256000, quantity: 1100, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-02', createDate: '2026-02-12' },
  { id: 12, asnNo: 'ASN-202602-0012', orderNo: 'PO-202602-0012', supplierName: '北辰包装材料有限公司', totalAmount: 92000, quantity: 4200, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 1200, period: '2026-02', createDate: '2026-02-18' },
  { id: 13, asnNo: 'ASN-202601-0013', orderNo: 'PO-202601-0013', supplierName: '华东精密制造有限公司', totalAmount: 378000, quantity: 1800, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-01', createDate: '2026-01-08' },
  { id: 14, asnNo: 'ASN-202601-0014', orderNo: 'PO-202601-0014', supplierName: '远航物流装备有限公司', totalAmount: 210000, quantity: 450, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-01', createDate: '2026-01-15' },
  { id: 15, asnNo: 'ASN-202601-0015', orderNo: 'PO-202601-0015', supplierName: '星河电子科技股份有限公司', totalAmount: 186000, quantity: 520, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2026-01', createDate: '2026-01-22' },
  { id: 16, asnNo: 'ASN-202512-0016', orderNo: 'PO-202512-0016', supplierName: '华东精密制造有限公司', totalAmount: 298000, quantity: 1350, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2025-12', createDate: '2025-12-05' },
  { id: 17, asnNo: 'ASN-202512-0017', orderNo: 'PO-202512-0017', supplierName: '北辰包装材料有限公司', totalAmount: 64000, quantity: 3000, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2025-12', createDate: '2025-12-12' },
  { id: 18, asnNo: 'ASN-202512-0018', orderNo: 'PO-202512-0018', supplierName: '星河电子科技股份有限公司', totalAmount: 415000, quantity: 960, currency: 'CNY', reconciliationStatus: 2, reconciliationStatusLabel: '已对账', paymentStatus: 2, paymentStatusLabel: '已付款', diffAmount: 0, period: '2025-12', createDate: '2025-12-18' },
]

export const financialReconciliationOverview = {
  totalAmount: 4183000,
  reconciledAmount: 3724000,
  pendingAmount: 458000,
  diffAmount: 5260,
  totalCount: 18,
  reconciledCount: 15,
  pendingCount: 2,
  diffCount: 1,
}

export const financialReconciliationChartData = {
  trends: [
    { period: '2025-12', totalAmount: 777000, reconciledAmount: 777000, diffAmount: 0, count: 3 },
    { period: '2026-01', totalAmount: 774000, reconciledAmount: 774000, diffAmount: 0, count: 3 },
    { period: '2026-02', totalAmount: 348000, reconciledAmount: 348000, diffAmount: 1200, count: 2 },
    { period: '2026-03', totalAmount: 705000, reconciledAmount: 705000, diffAmount: 0, count: 3 },
    { period: '2026-04', totalAmount: 793000, reconciledAmount: 793000, diffAmount: 0, count: 3 },
    { period: '2026-05', totalAmount: 1018000, reconciledAmount: 484000, diffAmount: 4060, count: 4 },
  ],
  supplierDistribution: [
    { supplierName: '华东精密制造有限公司', amount: 1725000, count: 6 },
    { supplierName: '星河电子科技股份有限公司', amount: 1629000, count: 5 },
    { supplierName: '远航物流装备有限公司', amount: 573000, count: 3 },
    { supplierName: '北辰包装材料有限公司', amount: 290000, count: 4 },
  ],
  paymentStatus: [
    { status: 'paid', label: '已付款', count: 13, amount: 3321000 },
    { status: 'partial', label: '部分付款', count: 2, amount: 656000 },
    { status: 'unpaid', label: '未付款', count: 3, amount: 206000 },
  ],
  reconciliationStatus: [
    { status: 'reconciled', label: '已对账', count: 15, amount: 3724000 },
    { status: 'processing', label: '对账中', count: 2, amount: 458000 },
    { status: 'diff', label: '有差异', count: 1, amount: 76000 },
  ],
}

export const reconDetails: ReconDetailLine[] = [
  { id: 1, lineNo: 1, businessType: '采购订单', businessNo: 'PO-202605-0001', occurDate: '2026-05-10', amount: 160000, confirmedAmount: 160000, diffAmount: 0, diffReason: '' },
  { id: 2, lineNo: 2, businessType: '采购订单', businessNo: 'PO-202605-0001', occurDate: '2026-05-12', amount: 36000, confirmedAmount: 36000, diffAmount: 0, diffReason: '' },
  { id: 3, lineNo: 3, businessType: '采购订单', businessNo: 'PO-202605-0001', occurDate: '2026-05-15', amount: 45000, confirmedAmount: 45000, diffAmount: 0, diffReason: '' },
  { id: 4, lineNo: 4, businessType: '采购订单', businessNo: 'PO-202605-0001', occurDate: '2026-05-18', amount: 45000, confirmedAmount: 41800, diffAmount: 3200, diffReason: '单价差异，待双方确认' },
]

/** 物料主数据 mock 数据 */
export const materials = [
  { id: 1, materialCode: 'MAT-001', materialName: '精密铸件A型', spec: '300x200x50mm', unit: 'PCS', category: '结构件', status: 1, createTime: '2026-04-01 00:00:00' },
  { id: 2, materialCode: 'MAT-002', materialName: '不锈钢法兰', spec: 'DN50 PN16', unit: 'SET', category: '管件', status: 1, createTime: '2026-04-01 00:00:00' },
  { id: 3, materialCode: 'MAT-003', materialName: '密封垫片', spec: 'Φ100x3mm', unit: 'PCS', category: '密封件', status: 1, createTime: '2026-04-02 00:00:00' },
  { id: 4, materialCode: 'MAT-004', materialName: '紧固螺栓M16', spec: 'M16x80 8.8级', unit: 'SET', category: '紧固件', status: 1, createTime: '2026-04-02 00:00:00' },
  { id: 5, materialCode: 'MAT-005', materialName: '铝合金垫板', spec: '500x300x10mm', unit: 'PCS', category: '结构件', status: 1, createTime: '2026-04-03 00:00:00' },
  { id: 6, materialCode: 'MAT-006', materialName: '高压油管', spec: 'Φ25xL2000mm', unit: 'M', category: '液压件', status: 1, createTime: '2026-04-03 00:00:00' },
  { id: 7, materialCode: 'MAT-007', materialName: '电路板PCB-A', spec: '100x80mm 四层板', unit: 'PCS', category: '电子件', status: 1, createTime: '2026-04-04 00:00:00' },
  { id: 8, materialCode: 'MAT-008', materialName: '轴承6205', spec: '6205-2RS', unit: 'SET', category: '传动件', status: 1, createTime: '2026-04-04 00:00:00' },
  { id: 9, materialCode: 'MAT-009', materialName: '包装纸箱A型', spec: '600x400x300mm', unit: 'PCS', category: '包装材料', status: 1, createTime: '2026-04-05 00:00:00' },
  { id: 10, materialCode: 'MAT-010', materialName: '润滑脂EP2', spec: 'EP2 15kg/桶', unit: 'KG', category: '辅料', status: 1, createTime: '2026-04-05 00:00:00' },
]

export const supplierAccounts = [
  { id: 101, username: 'zhouming', realName: '周明', phone: '13800010001', email: 'zhouming@huadong.com', userType: 2, supplierId: 1, status: 1, lastLoginTime: '2026-05-30 14:22:00', createTime: '2026-01-10 09:00:00', remark: '主账号' },
  { id: 102, username: 'wangfang', realName: '王芳', phone: '13900020002', email: 'wangfang@huadong.com', userType: 2, supplierId: 1, status: 1, lastLoginTime: '2026-05-28 10:15:00', createTime: '2026-02-15 09:00:00', remark: '' },
  { id: 103, username: 'liwei', realName: '李伟', phone: '13700030003', email: 'liwei@huadong.com', userType: 2, supplierId: 1, status: 0, lastLoginTime: '2026-04-01 08:00:00', createTime: '2026-03-20 09:00:00', remark: '已离职' },
  { id: 201, username: 'linxia', realName: '林夏', phone: '13800010002', email: 'linxia@xinghe.com', userType: 2, supplierId: 2, status: 1, lastLoginTime: '2026-05-31 09:30:00', createTime: '2026-02-01 09:00:00', remark: '主账号' },
  { id: 202, username: 'chenxu', realName: '陈旭', phone: '13600040004', email: 'chenxu@xinghe.com', userType: 2, supplierId: 2, status: 1, lastLoginTime: '2026-05-29 16:45:00', createTime: '2026-03-10 09:00:00', remark: '' },
  { id: 301, username: 'zhaoqing', realName: '赵青', phone: '13800010003', email: 'zhaoqing@beichen.com', userType: 2, supplierId: 3, status: 1, lastLoginTime: '2026-05-25 11:00:00', createTime: '2026-04-05 09:00:00', remark: '主账号' },
]

export const sysDicts = [
  { id: 1, dictName: '订单状态', dictCode: 'order_status', description: '采购订单状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 2, dictName: '供应商类型', dictCode: 'supplier_type', description: '供应商分类', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 3, dictName: '供应商状态', dictCode: 'supplier_status', description: '供应商状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 4, dictName: '付款方式', dictCode: 'payment_method', description: '付款方式', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 5, dictName: '送货状态', dictCode: 'delivery_status', description: '送货状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 6, dictName: '检验结果', dictCode: 'inspect_result', description: '检验结果', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 7, dictName: '对账状态', dictCode: 'recon_status', description: '对账状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 8, dictName: '发票状态', dictCode: 'invoice_status', description: '发票状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 9, dictName: '付款状态', dictCode: 'payment_status', description: '付款状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 10, dictName: '发票类型', dictCode: 'invoice_type', description: '发票类型', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 11, dictName: '检验类型', dictCode: 'inspect_type', description: '检验类型', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 12, dictName: '变更类型', dictCode: 'change_type', description: '变更类型', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 13, dictName: '处理方式', dictCode: 'handle_method', description: '处理方式', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 14, dictName: '用户类型', dictCode: 'user_type', description: '用户类型', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 15, dictName: '审批状态', dictCode: 'approve_status', description: '审批状态', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 16, dictName: '供应商评级', dictCode: 'supplier_rating', description: '供应商评级', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 17, dictName: '资质类型', dictCode: 'qual_type', description: '供应商资质类型', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 18, dictName: '币种', dictCode: 'currency', description: '货币类型', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 19, dictName: '物料分类', dictCode: 'material_category', description: '物料主数据分类', status: 1, createTime: '2026-01-01 00:00:00' },
]

export const dictItems: Record<string, { id: number; dictId: number; itemLabel: string; itemValue: string; sort: number; description: string; status: number }[]> = {
  material_category: [
    { id: 1, dictId: 19, itemLabel: '结构件', itemValue: '结构件', sort: 1, description: '', status: 1 },
    { id: 2, dictId: 19, itemLabel: '液压件', itemValue: '液压件', sort: 2, description: '', status: 1 },
    { id: 3, dictId: 19, itemLabel: '电子件', itemValue: '电子件', sort: 3, description: '', status: 1 },
    { id: 4, dictId: 19, itemLabel: '传动件', itemValue: '传动件', sort: 4, description: '', status: 1 },
    { id: 5, dictId: 19, itemLabel: '包装材料', itemValue: '包装材料', sort: 5, description: '', status: 1 },
    { id: 6, dictId: 19, itemLabel: '辅料', itemValue: '辅料', sort: 6, description: '', status: 1 },
    { id: 7, dictId: 19, itemLabel: '紧固件', itemValue: '紧固件', sort: 7, description: '', status: 1 },
    { id: 8, dictId: 19, itemLabel: '密封件', itemValue: '密封件', sort: 8, description: '', status: 1 },
  ],
  order_status: [
    { id: 101, dictId: 1, itemLabel: '待确认', itemValue: 'pending', sort: 1, description: '', status: 1 },
    { id: 102, dictId: 1, itemLabel: '已确认', itemValue: 'confirmed', sort: 2, description: '', status: 1 },
    { id: 103, dictId: 1, itemLabel: '已发货', itemValue: 'shipped', sort: 3, description: '', status: 1 },
    { id: 104, dictId: 1, itemLabel: '已完成', itemValue: 'completed', sort: 4, description: '', status: 1 },
    { id: 105, dictId: 1, itemLabel: '已取消', itemValue: 'cancelled', sort: 5, description: '', status: 1 },
  ],
  supplier_type: [
    { id: 201, dictId: 2, itemLabel: '生产型', itemValue: 'production', sort: 1, description: '', status: 1 },
    { id: 202, dictId: 2, itemLabel: '贸易型', itemValue: 'trade', sort: 2, description: '', status: 1 },
  ],
  supplier_status: [
    { id: 301, dictId: 3, itemLabel: '待审核', itemValue: 'pending', sort: 1, description: '', status: 1 },
    { id: 302, dictId: 3, itemLabel: '已通过', itemValue: 'approved', sort: 2, description: '', status: 1 },
    { id: 303, dictId: 3, itemLabel: '已拒绝', itemValue: 'rejected', sort: 3, description: '', status: 1 },
    { id: 304, dictId: 3, itemLabel: '已冻结', itemValue: 'frozen', sort: 4, description: '', status: 1 },
  ],
  currency: [
    { id: 401, dictId: 18, itemLabel: '人民币', itemValue: 'CNY', sort: 1, description: '', status: 1 },
    { id: 402, dictId: 18, itemLabel: '美元', itemValue: 'USD', sort: 2, description: '', status: 1 },
    { id: 403, dictId: 18, itemLabel: '欧元', itemValue: 'EUR', sort: 3, description: '', status: 1 },
  ],
}
