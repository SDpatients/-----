export interface OperationLogQuery {
  module?: string
  businessNo?: string
  operator?: string
  action?: string
  startTime?: string
  endTime?: string
}

export interface OperationLogItem {
  id: number
  module: string
  businessNo: string
  action: string
  operator: string
  beforeStatus: string
  afterStatus: string
  result: string
  remark: string
  ip: string
  traceId: string
  operatedAt: string
}
