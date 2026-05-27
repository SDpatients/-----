import type { AttachmentFile } from './business'

export interface AttachmentQuery {
  businessType: string
  businessId: string | number
}

export interface AttachmentUploadRequest extends AttachmentQuery {
  category: string
  remark?: string
}

export interface AttachmentUploadResult {
  file: AttachmentFile
  url: string
}

export interface AttachmentVersion {
  id: number | string
  fileName: string
  version: string
  uploader: string
  uploadedAt: string
  remark: string
}

export interface AttachmentPermission {
  canUpload: boolean
  canPreview: boolean
  canDownload: boolean
  canDelete: boolean
}
