export interface ImportTemplate {
  id: number
  module: string
  name: string
  format: string
  updatedAt: string
}

export interface ImportErrorRow {
  rowNo: number
  field: string
  reason: string
  rawValue: string
}

export interface ImportValidateResult {
  fileId?: number
  importType?: string
  errorFileId?: number
  total: number
  success: number
  failed: number
  errors: ImportErrorRow[]
}

export interface ExportRequest {
  module: string
  scope: 'current' | 'all' | 'selected'
  masked: boolean
  asyncMode: boolean
  /** 页面查询条件，传递给后端导出接口 */
  queryParams?: Record<string, unknown>
  /** 已选中的行ID列表 */
  selectedIds?: (number | string)[]
}
