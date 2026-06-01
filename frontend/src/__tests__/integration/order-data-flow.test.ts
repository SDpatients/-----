/**
 * 集成测试: 订单数据流 — API → adapter → toOrder 转换
 */
import { describe, it, expect } from 'vitest'
import { toOrder } from '@/api/adapters'

describe('集成测试 - 订单数据转换链路', () => {
  const rawOrder = {
    id: '9999',
    orderNo: 'PO-2026-999',
    supplierCode: 'SUP-042',
    supplierName: '精密五金(深圳)有限公司',
    orderStatus: 2,
    buyerId: '10',
    buyerName: '采购员张三',
    totalAmount: '128000.00',
    deliveryDate: '2026-07-15',
    confirmedAt: '2026-05-27T10:00:00Z',
  }

  it('toOrder 应正确转换所有字段', () => {
    const result = toOrder(rawOrder)

    expect(result.id).toBe('9999')
    expect(result.orderNo).toBe('PO-2026-999')
    expect(result.supplierName).toBe('精密五金(深圳)有限公司')
    expect(result.status).toBe('已确认')
    expect(result.buyer).toBe('采购员张三')
    expect(result.amount).toBe(128000)
    expect(result.deliveryDate).toBe('2026-07-15')
    expect(result.riskLevel).toBe('low')
  })

  it('toOrder 应正确处理逾期订单风险', () => {
    const overdueOrder = {
      ...rawOrder,
      deliveryDate: '2020-01-01',
      orderStatus: 1,
    }
    const result = toOrder(overdueOrder)
    expect(result.riskLevel).toBe('high')
  })

  it('toOrder 应正确处理近期待交付订单风险', () => {
    const nearDueDate = new Date(Date.now() + 3 * 86400000)
      .toISOString()
      .split('T')[0]

    const nearDueOrder = {
      ...rawOrder,
      deliveryDate: nearDueDate,
      orderStatus: 1,
    }
    const result = toOrder(nearDueOrder)
    expect(result.riskLevel).toBe('medium')
  })

  it('toOrder 应对缺失字段使用默认值', () => {
    const minimalOrder = { id: '1' }
    const result = toOrder(minimalOrder)

    expect(result.orderNo).toBe('-')
    expect(result.supplierName).toBe('供应商')
    expect(result.buyer).toBe('-')
    expect(result.status).toBe('processing')
    expect(result.amount).toBe(0)
    expect(result.deliveryDate).toBe('-')
    expect(result.riskLevel).toBe('medium')
  })
})