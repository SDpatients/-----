import { request } from '@/utils/request'
import { asArray } from '@/utils/apiNormalize'
import type { DashboardMetric, RiskWarning, TrendSeries } from '@/types/dashboard'

interface BackendDashboardMetric {
  name: string
  value: number
  unit?: string
  trend?: string
  path?: string
}

interface BackendTrendItem {
  period: string
  orderCount: number
  deliveryCount: number
}

interface BackendRiskItem {
  riskType: string
  title: string
  count: number
  level: string
}

export const dashboardApi = {
  metrics: async (): Promise<DashboardMetric[]> => {
    const result = await request.get<BackendDashboardMetric[], BackendDashboardMetric[]>('/v1/dashboard/metrics')
    return asArray<BackendDashboardMetric>(result).map((item, index) => ({
      label: item.name,
      value: `${item.value}${item.unit || ''}`,
      trend: item.trend || (index === 0 ? '来自业务聚合数据' : '实时刷新'),
      tone: ['blue', 'green', 'orange'][index % 3] as DashboardMetric['tone'],
      path: item.path || undefined,
    }))
  },
  trends: async (): Promise<TrendSeries[]> => {
    const result = await request.get<BackendTrendItem[], BackendTrendItem[]>('/v1/dashboard/trends')
    return asArray<BackendTrendItem>(result).map((item) => ({
      label: item.period,
      value: item.orderCount || 0,
      orderCount: item.orderCount,
      deliveryCount: item.deliveryCount,
    }))
  },
  risks: async (): Promise<RiskWarning[]> => {
    const result = await request.get<BackendRiskItem[], BackendRiskItem[]>('/v1/dashboard/risks')
    return asArray<BackendRiskItem>(result).map((item) => ({
      id: item.riskType,
      title: item.title,
      level: item.level,
      module: item.riskType,
      targetPath: '', // 实际跳转路径由 RiskWarningList 按用户角色解析
      count: item.count,
    }))
  },
}
