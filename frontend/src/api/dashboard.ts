import { request } from '@/utils/request'
import { asArray } from '@/utils/apiNormalize'
import type { DashboardMetric, PerformanceMetric, RiskWarning, TrendSeries } from '@/types/dashboard'

interface BackendDashboardMetric {
  name: string
  value: number
  unit?: string
}

interface BackendTrendItem {
  period: string
  orderCount: number
  deliveryCount: number
  qualityIssueCount: number
  reconciliationCount: number
}

interface BackendRiskItem {
  riskType: string
  title: string
  count: number
  level: string
}

interface BackendSupplierPerformance {
  supplierId: number
  supplierName: string | null
  deliveryRate: number
  qualityRate: number
  responseRate: number
  score: number
}

export const dashboardApi = {
  metrics: async (): Promise<DashboardMetric[]> => {
    const result = await request.get<BackendDashboardMetric[], BackendDashboardMetric[]>('/v1/dashboard/metrics')
    return asArray<BackendDashboardMetric>(result).map((item, index) => ({
      label: item.name,
      value: `${item.value}${item.unit || ''}`,
      trend: index === 0 ? '来自业务聚合数据' : '实时刷新',
      tone: ['blue', 'green', 'orange', 'red'][index % 4] as DashboardMetric['tone'],
    }))
  },
  trends: async (): Promise<TrendSeries[]> => {
    const result = await request.get<BackendTrendItem[], BackendTrendItem[]>('/v1/dashboard/trends')
    return asArray<BackendTrendItem>(result).map((item) => ({ label: item.period, value: Math.max(item.orderCount, item.deliveryCount, item.qualityIssueCount, item.reconciliationCount) }))
  },
  risks: async (): Promise<RiskWarning[]> => {
    const result = await request.get<BackendRiskItem[], BackendRiskItem[]>('/v1/dashboard/risks')
    const riskPathMap: Record<string, string> = {
      order_pending: '/orders',
      delivery_delay: '/asn',
      quality_issue: '/quality',
      recon_dispute: '/settlement',
    }
    return asArray<BackendRiskItem>(result).map((item, index) => ({
      id: index + 1,
      title: item.title,
      level: item.level,
      module: item.riskType,
      targetPath: riskPathMap[item.riskType] || '/messages',
    }))
  },
  performance: async (): Promise<PerformanceMetric[]> => {
    const result = await request.get<BackendSupplierPerformance[], BackendSupplierPerformance[]>('/v1/dashboard/supplier-performance')
    const list = asArray<BackendSupplierPerformance>(result)
    if (list.length === 0) return []
    const p = list[0]
    const toPercent = (v: number | undefined | null) => (v != null && !isNaN(v) ? `${Number(v)}%` : '-')
    return [
      { label: '准时交货率', value: toPercent(p.deliveryRate), color: '#0bb783' },
      { label: '质量合格率', value: toPercent(p.qualityRate), color: '#4a90d9' },
      { label: '响应及时率', value: toPercent(p.responseRate), color: '#f5a623' },
      { label: '综合评分', value: p.score != null && !isNaN(p.score) ? `${p.score}分` : '-', color: '#d0021b' },
    ]
  },
}
