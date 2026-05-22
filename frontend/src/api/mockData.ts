import type { AsnNotice, PurchaseOrder, QualityCase, Settlement, Supplier, TimelineItem, UserInfo } from '@/types/business'

export const currentUser: UserInfo = {
  id: 1,
  username: 'admin',
  realName: '系统管理员',
  roleName: '平台管理员',
  department: '供应链数字化中心',
  userType: 'internal',
  avatarColor: '#1f5eff',
}

export const permissions = ['dashboard:view', 'supplier:view', 'supplier:audit', 'order:view', 'order:confirm', 'asn:view', 'asn:create', 'quality:view', 'settlement:view']

export const suppliers: Supplier[] = [
  { id: 1, code: 'SUP-2026-001', name: '华东精密制造有限公司', category: '结构件', level: 'A', status: 'active', contact: '周明', phone: '13800010001', admissionStage: '已准入', performanceScore: 96, riskLevel: 'low', address: '江苏省苏州市工业园区' },
  { id: 2, code: 'SUP-2026-002', name: '星河电子科技股份有限公司', category: '电子件', level: 'B', status: 'pending', contact: '林夏', phone: '13800010002', admissionStage: '资质审核', performanceScore: 82, riskLevel: 'medium', address: '广东省深圳市南山区' },
  { id: 3, code: 'SUP-2026-003', name: '北辰包装材料有限公司', category: '包装材料', level: 'C', status: 'frozen', contact: '赵青', phone: '13800010003', admissionStage: '整改中', performanceScore: 68, riskLevel: 'high', address: '浙江省杭州市余杭区' },
  { id: 4, code: 'SUP-2026-004', name: '远航物流装备有限公司', category: '物流装备', level: 'A', status: 'active', contact: '陈一', phone: '13800010004', admissionStage: '已准入', performanceScore: 91, riskLevel: 'low', address: '上海市嘉定区' },
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
  { id: 1, caseNo: 'QC-202605-0001', type: '来料检验', supplierName: '华东精密制造有限公司', severity: 'medium', status: 'processing', owner: '刘质检', createdAt: '2026-05-20', description: '尺寸公差接近上限，需补充过程能力报告' },
  { id: 2, caseNo: 'NCR-202605-0002', type: 'NCR', supplierName: '北辰包装材料有限公司', severity: 'high', status: 'exception', owner: '陈质量', createdAt: '2026-05-19', description: '包装破损率超出协议标准' },
  { id: 3, caseNo: '8D-202605-0003', type: '8D整改', supplierName: '星河电子科技股份有限公司', severity: 'medium', status: 'processing', owner: '刘质检', createdAt: '2026-05-18', description: '批次追溯资料不完整，要求 8D 回复' },
  { id: 4, caseNo: 'AP-202605-0004', type: '质量申诉', supplierName: '远航物流装备有限公司', severity: 'low', status: 'closed', owner: '陈质量', createdAt: '2026-05-15', description: '供应商对判定结果提交申诉，已复核关闭' },
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
