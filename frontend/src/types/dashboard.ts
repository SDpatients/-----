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
}

export interface RiskWarning {
  id: number
  title: string
  level: string
  module: string
  targetPath: string
}

export interface PerformanceMetric {
  label: string
  value: string
  color: string
}

export interface BoardRefreshConfig {
  intervalSeconds: number
  lastRefreshAt: string
}
