/**
 * api/adapters.ts 测试
 */
import { describe, it, expect } from 'vitest'
import {
  toSupplier,
  toOrder,
  toAsn,
  toQuality,
  toSettlement,
  toAttachment,
  toExportTask,
  toOperationLog,
  toPortalTodo,
} from '@/api/adapters'

describe('toSupplier', () => {
  it('should convert backend data to Supplier', () => {
    const result = toSupplier({
      id: '1',
      supplierCode: 'SUP-001',
      supplierName: '测试供应商',
      status: 4,
      rating: 5,
      contactName: '张三',
      contactPhone: '13800001111',
    })
    expect(result.id).toBe('1')
    expect(result.code).toBe('SUP-001')
    expect(result.name).toBe('测试供应商')
    expect(result.admissionStage).toBe('已准入')
    expect(result.performanceScore).toBe(5)
    expect(result.riskLevel).toBe('low')
  })

  it('should use fallback values', () => {
    const result = toSupplier({ id: '2' })
    expect(result.code).toBe('-')
    expect(result.name).toBe('-')
    expect(result.category).toBe('-')
    expect(result.contact).toBe('-')
    expect(result.phone).toBe('-')
  })

  it('should compute risk for low-rated supplier', () => {
    const result = toSupplier({ id: '3', rating: 2 })
    expect(result.performanceScore).toBe(2)
    expect(result.riskLevel).toBe('medium')
  })

  it('should use code/name fallbacks', () => {
    const result = toSupplier({ id: '4', code: 'C001', name: 'NameX' })
    expect(result.code).toBe('C001')
    expect(result.name).toBe('NameX')
  })

  it('should map blacklist fields when blacklisted', () => {
    const result = toSupplier({
      id: '5',
      supplierCode: 'SUP-005',
      supplierName: '黑名单供应商',
      status: 6,
      blacklisted: true,
      blacklistReason: '质量多次不合格',
      blacklistStartTime: '2026-05-05T00:00:00',
      blacklistEndTime: null,
    })
    expect(result.id).toBe('5')
    expect(result.blacklisted).toBe(true)
    expect(result.blacklistReason).toBe('质量多次不合格')
    expect(result.blacklistStartTime).toBe('2026-05-05T00:00:00')
    expect(result.blacklistEndTime).toBe('')
    expect(result.admissionStage).toBe('已禁用')
  })

  it('should default blacklisted to false when not provided', () => {
    const result = toSupplier({
      id: '6',
      supplierCode: 'SUP-006',
      supplierName: '正常供应商',
      status: 4,
    })
    expect(result.blacklisted).toBe(false)
    expect(result.blacklistReason).toBe('')
    expect(result.blacklistStartTime).toBe('')
  })
})

describe('toOrder', () => {
  it('should convert order data correctly', () => {
    const result = toOrder({
      id: '100',
      orderNo: 'PO-2026-001',
      supplierName: '供应商A',
      orderStatus: 2,
      buyerName: '李四',
      totalAmount: '9999.99',
      deliveryDate: '2026-06-15',
    })
    expect(result.id).toBe('100')
    expect(result.orderNo).toBe('PO-2026-001')
    expect(result.status).toBe('已确认')
    expect(result.confirmStatus).toBe('已确认')
    expect(result.amount).toBe(9999.99)
  })

  it('should compute risk level for overdue delivery', () => {
    const result = toOrder({
      id: '101',
      orderNo: 'PO-2026-002',
      orderStatus: 1,
      deliveryDate: '2020-01-01', // 已过期
    })
    expect(result.riskLevel).toBe('high')
  })

  it('should compute risk for near due date', () => {
    const futureDate = new Date(Date.now() + 3 * 86400000) // 3天后
    const result = toOrder({
      id: '102',
      orderNo: 'PO-2026-003',
      orderStatus: 1,
      deliveryDate: futureDate.toISOString().split('T')[0],
    })
    expect(result.riskLevel).toBe('medium')
  })

  it('should fallback to defaults', () => {
    const result = toOrder({ id: '999' })
    expect(result.orderNo).toBe('-')
    expect(result.buyer).toBe('-')
    expect(result.amount).toBe(0)
    expect(result.deliveryDate).toBe('-')
  })
})

describe('toAsn', () => {
  it('should convert ASN data', () => {
    const result = toAsn({
      id: '1',
      noticeNo: 'ASN-001',
      orderNo: 'PO-001',
      supplierName: '供应商B',
      deliveryStatus: 2,
      quantity: '100',
    })
    expect(result.asnNo).toBe('ASN-001')
    expect(result.status).toBe('已到达')
    expect(result.quantity).toBe(100)
  })
})

describe('toQuality', () => {
  it('should convert quality case', () => {
    const result = toQuality({
      id: '1',
      inspectionNo: 'QC-001',
      supplierName: '供应商C',
      inspectResult: 1,
      unqualifiedQty: 0,
      inspectorName: '王五',
    })
    expect(result.caseNo).toBe('QC-001')
    expect(result.status).toBe('合格')
    expect(result.severity).toBe('low')
  })

  it('should flag high severity for unqualified', () => {
    const result = toQuality({
      id: '2',
      inspectResult: 2,
      unqualifiedQty: 5,
    })
    expect(result.status).toBe('不合格')
    expect(result.severity).toBe('high')
  })

  it('should generate caseNo when missing', () => {
    const result = toQuality({ id: '99' })
    expect(result.caseNo).toBe('QI-99')
  })
})

describe('toSettlement', () => {
  it('should convert settlement', () => {
    const result = toSettlement({
      id: '1',
      reconNo: 'RECON-001',
      supplierName: '供应商D',
      totalAmount: '50000',
      diffAmount: '200',
      reconStatus: 2,
    })
    expect(result.statementNo).toBe('RECON-001')
    expect(result.amount).toBe(50000)
    expect(result.diffAmount).toBe(200)
    expect(result.status).toBe('已确认')
  })
})

describe('toAttachment', () => {
  it('should format file size', () => {
    const result = toAttachment({
      id: '1',
      fileName: 'document.pdf',
      fileSize: 204800,
      uploadTime: '2026-05-01',
    })
    expect(result.fileName).toBe('document.pdf')
    expect(result.size).toBe('200KB')
    expect(result.version).toBe('V1')
  })

  it('should handle missing fields', () => {
    const result = toAttachment({ id: '2' })
    expect(result.fileName).toBe('-')
    expect(result.size).toBe('-')
    expect(result.uploader).toBe('-')
  })
})

describe('toExportTask', () => {
  it('should convert export task', () => {
    const result = toExportTask({
      id: '1',
      taskNo: 'TASK-001',
      taskType: '订单导出',
      taskStatus: '1',
      totalCount: '100',
      processedCount: '80',
    })
    expect(result.taskNo).toBe('TASK-001')
    expect(result.type).toBe('export')
    expect(result.total).toBe(100)
    expect(result.success).toBe(80)
  })
})

describe('toOperationLog', () => {
  it('should convert operation log', () => {
    const result = toOperationLog({
      id: '1',
      moduleName: '订单管理',
      businessNo: 'PO-001',
      actionName: '创建订单',
      username: 'admin',
      resultStatus: 1,
      operateTime: '2026-05-27 10:00:00',
    })
    expect(result.module).toBe('订单管理')
    expect(result.action).toBe('创建订单')
    expect(result.operator).toBe('admin')
    expect(result.result).toBe('成功')
  })

  it('should handle failure result', () => {
    const result = toOperationLog({
      id: '2',
      resultStatus: 0,
      errorMessage: '操作失败原因',
    })
    expect(result.result).toBe('失败')
  })
})

describe('toPortalTodo', () => {
  it('should convert todo with high priority for overdue', () => {
    const result = toPortalTodo({
      id: '1',
      title: '待审核订单',
      businessType: 'order',
      businessId: 100,
      dueTime: '2020-01-01',
    })
    expect(result.title).toBe('待审核订单')
    expect(result.priority).toBe('high')
  })

  it('should default to low priority without due time', () => {
    const result = toPortalTodo({
      id: '2',
      title: '常规任务',
    })
    expect(result.priority).toBe('low')
  })
})