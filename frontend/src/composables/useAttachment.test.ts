/**
 * composables/useAttachment.ts 测试
 */
import { describe, it, expect, vi } from 'vitest'
import { useAttachment } from '@/composables/useAttachment'
import { attachmentApi } from '@/api/attachment'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn(),
  },
}))

// Mock attachment API
vi.mock('@/api/attachment', () => ({
  attachmentApi: {
    remove: vi.fn(),
  },
}))

import { ElMessage, ElMessageBox } from 'element-plus'

describe('useAttachment', () => {
  const { validateFile, confirmRemove } = useAttachment()

  describe('validateFile', () => {
    it('should accept PDF files', () => {
      const file = new File([''], 'document.pdf', { type: 'application/pdf' })
      expect(validateFile(file)).toBe(true)
    })

    it('should accept image files', () => {
      expect(validateFile(new File([''], 'photo.png', { type: 'image/png' }))).toBe(true)
      expect(validateFile(new File([''], 'photo.jpg', { type: 'image/jpeg' }))).toBe(true)
    })

    it('should accept Excel and Word files', () => {
      expect(
        validateFile(
          new File([''], 'sheet.xlsx', {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          }),
        ),
      ).toBe(true)
      expect(
        validateFile(
          new File([''], 'doc.docx', {
            type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
          }),
        ),
      ).toBe(true)
    })

    it('should reject unsupported file types', () => {
      expect(validateFile(new File([''], 'script.exe'))).toBe(false)
      expect(validateFile(new File([''], 'archive.zip'))).toBe(false)
      expect(ElMessage.error).toHaveBeenCalledWith(
        '仅支持 PDF、图片、Excel、Word 文件',
      )
    })

    it('should reject files without extension', () => {
      expect(validateFile(new File([''], 'noextension'))).toBe(false)
    })

    it('should reject files larger than 20MB', () => {
      const largeFile = new File([new ArrayBuffer(21 * 1024 * 1024)], 'large.pdf')
      expect(validateFile(largeFile)).toBe(false)
      expect(ElMessage.error).toHaveBeenCalledWith('单个文件不能超过 20MB')
    })

    it('should accept files exactly at 20MB boundary', () => {
      // 对于边界测试我们用 mock 的方式
      const file = new File([''], 'boundary.pdf')
      Object.defineProperty(file, 'size', { value: 20 * 1024 * 1024 })
      expect(validateFile(file)).toBe(true)
    })
  })

  describe('confirmRemove', () => {
    it('should call confirm and remove on success', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm' as any)
      vi.mocked(ElMessage.success).mockReturnValue(undefined)
      vi.mocked(attachmentApi.remove).mockResolvedValue(undefined)

      await confirmRemove(1)

      expect(ElMessageBox.confirm).toHaveBeenCalledWith(
        '确认删除该附件？',
        '删除确认',
        { type: 'warning' },
      )
      expect(attachmentApi.remove).toHaveBeenCalledWith(1)
      expect(ElMessage.success).toHaveBeenCalledWith('附件已删除')
    })
  })
})