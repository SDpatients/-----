export interface DashboardMetric {
  label: string
  value: string | number
  trend: string
  tone: 'blue' | 'green' | 'orange' | 'red'
  path?: string
}

export interface TrendSeries {
  label: string
  value: number
  orderCount?: number
  deliveryCount?: number
}

export interface RiskWarning {
  id: string
  title: string
  level: string
  module: string
  targetPath: string
  count?: number
}

export interface BoardRefreshConfig {
  intervalSeconds: number
  lastRefreshAt: string
}
