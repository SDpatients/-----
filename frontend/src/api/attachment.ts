import { downloadBlob, request } from '@/utils/request'
import { toAttachment } from './adapters'
import { asPage } from '@/utils/apiNormalize'
import type { AttachmentQuery, AttachmentUploadRequest, AttachmentUploadResult, AttachmentVersion } from '@/types/attachment'
import type { ApiPage } from '@/utils/request'

export const attachmentApi = {
  list: async (query: AttachmentQuery) => {
    const result = await request.get<ApiPage<unknown>, ApiPage<unknown>>('/v1/file-attachments', { params: { ...query, pageNum: 1, pageSize: 100 } })
    return asPage<unknown>(result, 1, 100).records.map(toAttachment)
  },
  upload: async (data: AttachmentUploadRequest, fileName: string): Promise<AttachmentUploadResult> => {
    const id = await request.post<number, number>('/v1/file-attachments', { businessType: data.businessType, businessId: data.businessId, fileName, fileExt: fileName.split('.').pop() || '', fileSize: 1, contentType: 'application/octet-stream', bucketName: 'metadata-only', objectKey: `metadata/${fileName}`, fileHash: `${Date.now()}` })
    return { file: { id, fileName, category: data.category, version: 'V1', size: '1KB', uploader: '当前用户', uploadedAt: new Date().toLocaleString() }, url: '' }
  },
  preview: async (id: number) => downloadBlob({ url: `/v1/file-attachments/${id}/preview`, method: 'GET' }, `attachment-${id}`),
  download: async (id: number) => downloadBlob({ url: `/v1/file-attachments/${id}/download`, method: 'GET' }, `attachment-${id}`),
  remove: (id: number) => request.delete<void, void>(`/v1/file-attachments/${id}`),
  versions: async (id: number): Promise<AttachmentVersion[]> => {
    const files = await attachmentApi.list({ businessType: '', businessId: id })
    return files.map((item) => ({ id: item.id, fileName: item.fileName, version: item.version, uploader: item.uploader, uploadedAt: item.uploadedAt, remark: `${item.category}版本记录` }))
  },
  uploadBinary: async (file: File, businessType: string, businessId?: number, businessNo?: string) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('businessType', businessType)
    if (businessId !== undefined && businessId !== null) formData.append('businessId', String(businessId))
    if (businessNo) formData.append('businessNo', businessNo)
    return request.post<number, number>('/v1/file-attachments/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}
