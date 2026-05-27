import { request, type ApiPage } from '@/utils/request'
import { toOperationLog } from './adapters'
import type { OperationLogItem, OperationLogQuery } from '@/types/operationLog'

export const operationLogApi = {
  list: async (query: OperationLogQuery = {}) => {
    const result = await request.get<ApiPage<unknown>, ApiPage<unknown>>('/v1/audit-logs', { params: { pageNum: 1, pageSize: 50, moduleName: query.module, businessNo: query.businessNo, username: query.operator, actionName: query.action } })
    return result.records.map(toOperationLog)
  },
  detail: async (id: number | string) => toOperationLog(await request.get<unknown, unknown>(`/v1/audit-logs/${id}`)),
}
