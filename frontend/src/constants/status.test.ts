/**
 * constants/status.ts 测试
 */
import { describe, it, expect } from 'vitest'
import { statusMap, riskMap } from '@/constants/status'

describe('statusMap', () => {
  it('should have supplier admission statuses', () => {
    expect(statusMap['0'].label).toBe('待审核')
    expect(statusMap['4'].label).toBe('审核通过')
    expect(statusMap['5'].label).toBe('审核驳回')
    expect(statusMap['6'].label).toBe('已禁用')
  })

  it('should have order statuses', () => {
    expect(statusMap['订单0'].label).toBe('草稿')
    expect(statusMap['订单1'].label).toBe('待确认')
    expect(statusMap['订单2'].label).toBe('已确认')
  })

  it('should have old compatible string values', () => {
    expect(statusMap['active'].label).toBe('合作中')
    expect(statusMap['pending'].label).toBe('待审核')
    expect(statusMap['draft'].label).toBe('草稿')
  })

  it('should have NCR statuses', () => {
    expect(statusMap['NCR0'].label).toBe('草稿')
    expect(statusMap['NCR1'].label).toBe('已发布')
    expect(statusMap['NCR4'].label).toBe('已关闭')
  })

  it('should have 8D statuses', () => {
    expect(statusMap['8D0'].label).toBe('草稿')
    expect(statusMap['8D4'].label).toBe('已关闭')
  })

  it('should have RFQ statuses', () => {
    expect(statusMap['RFQ0'].label).toBe('草稿')
    expect(statusMap['RFQ4'].label).toBe('已定价')
  })

  it('every entry should have label and type', () => {
    Object.entries(statusMap).forEach(([key, val]) => {
      expect(val).toHaveProperty('label')
      expect(val).toHaveProperty('type')
      expect(['success', 'warning', 'danger', 'info', 'primary']).toContain(
        val.type,
      )
    })
  })
})

describe('riskMap', () => {
  it('should have all risk levels', () => {
    expect(riskMap.low.label).toBe('低风险')
    expect(riskMap.medium.label).toBe('中风险')
    expect(riskMap.high.label).toBe('高风险')
  })

  it('should have valid tag types', () => {
    expect(riskMap.low.type).toBe('success')
    expect(riskMap.medium.type).toBe('warning')
    expect(riskMap.high.type).toBe('danger')
  })

  it('should support backend level aliases (warning / danger)', () => {
    expect(riskMap.warning).toBeDefined()
    expect(riskMap.danger).toBeDefined()
    expect(riskMap.warning.type).toBe('warning')
    expect(riskMap.danger.type).toBe('danger')
  })

  it('should have valid tag types for aliases', () => {
    const validTypes = ['success', 'warning', 'danger', 'info', 'primary']
    expect(validTypes).toContain(riskMap.warning.type)
    expect(validTypes).toContain(riskMap.danger.type)
  })
})