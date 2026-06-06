import { downloadBlob, request, API_BASE_URL } from '@/utils/request'
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
  preview: async (id: number, fileName?: string) => {
    // 支持的在线预览文件类型
    const previewableExts = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg', 'txt', 'mp4', 'webm']
    const ext = fileName ? fileName.split('.').pop()?.toLowerCase() || '' : ''
    const isPreviewable = previewableExts.includes(ext)

    if (isPreviewable) {
      // 支持的类型：构建带 token 的预览 URL，在新窗口打开
      const token = localStorage.getItem('token') || ''
      const url = `${API_BASE_URL}/v1/file-attachments/${id}/preview?token=${encodeURIComponent(token)}`
      window.open(url, '_blank')
    } else {
      // 不支持预览的类型：直接下载
      await downloadBlob({ url: `/v1/file-attachments/${id}/download`, method: 'GET' }, fileName || `attachment-${id}`)
    }
  },
  download: async (id: number, fileName?: string) => downloadBlob({ url: `/v1/file-attachments/${id}/download`, method: 'GET' }, fileName || `attachment-${id}`),
  remove: (id: number) => request.delete<void, void>(`/v1/file-attachments/${id}`),
  rename: (id: number, fileName: string) => request.put<void, void>(`/v1/file-attachments/${id}/rename`, { fileName }),
  versions: async (query: { businessType?: string; businessId: string | number }): Promise<AttachmentVersion[]> => {
    const files = await attachmentApi.list({ businessType: query.businessType || '', businessId: query.businessId })
    return files.map((item) => ({ id: item.id, fileName: item.fileName, version: item.version, uploader: item.uploader, uploadedAt: item.uploadedAt, remark: `${item.category}版本记录` }))
  },
  uploadBinary: async (file: File, businessType: string, businessId?: number | string, businessNo?: string) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('businessType', businessType)
    // 注意：19 位雪花 ID 不能走 JS Number，必须按原字符串透传，否则会丢精度
    if (businessId !== undefined && businessId !== null && businessId !== '') {
      formData.append('businessId', typeof businessId === 'string' ? businessId : String(businessId))
    }
    if (businessNo) formData.append('businessNo', businessNo)
    return request.post<number, number>('/v1/file-attachments/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}
