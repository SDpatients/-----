import { downloadBlob, request, type ApiPage } from '@/utils/request'
import { toExportTask } from './adapters'
import { asPage } from '@/utils/apiNormalize'
import type { ImportExportTask } from '@/types/business'
import type { ExportRequest, ImportTemplate, ImportValidateResult } from '@/types/importExport'

interface BackendImportCheckResult {
  fileId: number
  importType: string
  totalCount: number
  successCount: number
  errorCount: number
  errorFileId?: number
}

/** 模块名到后端导出接口路径的映射 */
const MODULE_EXPORT_PATH: Record<string, string> = {
  'ASN': '/v1/delivery-notices/export',
  '采购订单': '/v1/purchase-orders/export',
  '对账单': '/v1/reconciliation/export',
}

export const importExportApi = {
  /** 获取导入模板列表（后端暂无模板列表接口，暂时返回默认模板；后续对接 GET /v1/imports/templates） */
  templates: async (): Promise<ImportTemplate[]> => {
    try {
      const result = await request.get<ImportTemplate[], ImportTemplate[]>('/v1/imports/templates')
      return result || []
    } catch {
      return [
        { id: 1, module: '订单', name: '订单导入模板', format: 'xlsx', updatedAt: '2026-05-21' },
        { id: 2, module: 'ASN', name: 'ASN明细模板', format: 'xlsx', updatedAt: '2026-05-21' },
      ]
    }
  },
  downloadTemplate: async (id: number | string) => downloadBlob({ url: `/v1/imports/${id}/template`, method: 'GET' }, `template-${id}.csv`),
  validateImport: async (importType: string, file: File): Promise<ImportValidateResult> => {
    const formData = new FormData()
    formData.append('file', file)
    const result = await request.post<BackendImportCheckResult, BackendImportCheckResult>(`/v1/imports/${importType}/check`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    return { fileId: result.fileId, importType: result.importType, errorFileId: result.errorFileId, total: result.totalCount, success: result.successCount, failed: result.errorCount, errors: [] }
  },
  submitImport: async (importType: string, fileId: number) => {
    const result = await request.post<number, number>(`/v1/imports/${importType}/submit`, null, { params: { fileId } })
    return result
  },
  createExport: async (data: ExportRequest) => {
    const exportPath = MODULE_EXPORT_PATH[data.module]
    if (exportPath) {
      // 调用模块专用导出接口（同步执行，返回 taskId 且文件已生成）
      const taskId = await request.post<number, number>(exportPath, {
        scope: data.scope,
        selectedIds: data.selectedIds,
        keyword: data.queryParams?.keyword,
        supplierId: data.queryParams?.supplierId,
        deliveryStatus: data.queryParams?.deliveryStatus,
        startDate: data.queryParams?.startDate,
        endDate: data.queryParams?.endDate,
      })
      // 模块专用导出是同步完成的，创建成功后立即下载
      if (taskId) {
        await importExportApi.downloadExport(taskId)
      }
      return importExportApi.tasks()
    }
    // 兜底：使用通用导出任务接口
    await request.post<number, number>('/v1/export-tasks', { taskType: data.module, exportParams: JSON.stringify(data), totalCount: 0 })
    return importExportApi.tasks()
  },
  tasks: async (): Promise<ImportExportTask[]> => {
    const result = await request.get<ApiPage<unknown>, ApiPage<unknown>>('/v1/export-tasks', { params: { pageNum: 1, pageSize: 20 } })
    return asPage<unknown>(result, 1, 20).records.map(toExportTask)
  },
  downloadError: async (fileId: number) => downloadBlob({ url: `/v1/imports/errors/${fileId}/download`, method: 'GET' }, `import-errors-${fileId}.csv`),
  downloadExport: async (id: number | string) => downloadBlob({ url: `/v1/export-tasks/${id}/download`, method: 'GET' }, `export-task-${id}.xlsx`),
}
