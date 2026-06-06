/**
 * api/dashboard.ts 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils/request', () => ({
  request: {
    get: vi.fn(),
  },
}))

vi.mock('@/utils/apiNormalize', () => ({
  asArray: <T>(value: unknown): T[] => (Array.isArray(value) ? (value as T[]) : []),
}))

import { request } from '@/utils/request'
import { dashboardApi } from '@/api/dashboard'

describe('dashboardApi.risks', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should map backend risk items to RiskWarning shape', async () => {
    vi.mocked(request.get).mockResolvedValue([
      { riskType: 'order_pending', title: '未确认订单量', count: 3, level: 'warning' },
      { riskType: 'delivery_delay', title: '计划送货已逾期', count: 1, level: 'danger' },
    ])

    const result = await dashboardApi.risks()

    expect(result).toHaveLength(2)
    expect(result[0]).toMatchObject({
      id: 'order_pending',
      title: '未确认订单量',
      level: 'warning',
      module: 'order_pending',
      count: 3,
    })
    expect(result[1].id).toBe('delivery_delay')
  })

  it('should use riskType as id and module for stable keys', async () => {
    vi.mocked(request.get).mockResolvedValue([
      { riskType: 'delivery_approaching', title: '即将到期待发货', count: 2, level: 'warning' },
    ])

    const result = await dashboardApi.risks()

    expect(result[0].id).toBe('delivery_approaching')
    expect(result[0].module).toBe('delivery_approaching')
  })

  it('should return empty array when backend response is empty', async () => {
    vi.mocked(request.get).mockResolvedValue([])

    const result = await dashboardApi.risks()

    expect(result).toEqual([])
  })

  it('should accept risk items wrapped in object payload', async () => {
    vi.mocked(request.get).mockResolvedValue([
      { riskType: 'order_overdue', title: '订单交期已逾期', count: 0, level: 'danger' },
    ] as any)

    const result = await dashboardApi.risks()

    expect(result).toHaveLength(1)
    expect(result[0].id).toBe('order_overdue')
    expect(result[0].count).toBe(0)
  })
})

describe('dashboardApi.metrics', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should map backend metric items with index-based tone', async () => {
    vi.mocked(request.get).mockResolvedValue([
      { name: '进行中的询价单', value: 8, unit: '单', trend: '实时刷新' },
      { name: '收到的未处理报价', value: 5, unit: '单', trend: '实时刷新' },
      { name: '未送达的物流', value: 12, unit: '单', trend: '实时刷新' },
    ] as any)

    const result = await dashboardApi.metrics()

    expect(result).toHaveLength(3)
    expect(result[0]).toMatchObject({ label: '进行中的询价单', value: '8单', tone: 'blue' })
    expect(result[1].tone).toBe('green')
    expect(result[2].tone).toBe('orange')
  })

  it('should fallback to default trend text when missing', async () => {
    vi.mocked(request.get).mockResolvedValue([
      { name: 'A', value: 1, unit: '单' },
    ] as any)

    const result = await dashboardApi.metrics()

    expect(result[0].trend).toBe('来自业务聚合数据')
  })
})
