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
}
