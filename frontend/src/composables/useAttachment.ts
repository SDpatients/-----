import { ElMessage, ElMessageBox } from 'element-plus'
import { attachmentApi } from '@/api/attachment'

export const useAttachment = () => {
  const validateFile = (file: File) => {
    const allowTypes = ['pdf', 'png', 'jpg', 'jpeg', 'xlsx', 'docx']
    const ext = file.name.split('.').pop()?.toLowerCase()
    if (!ext || !allowTypes.includes(ext)) {
      ElMessage.error('仅支持 PDF、图片、Excel、Word 文件')
      return false
    }
    if (file.size > 20 * 1024 * 1024) {
      ElMessage.error('单个文件不能超过 20MB')
      return false
    }
    return true
  }

  const confirmRemove = async (id: number) => {
    await ElMessageBox.confirm('确认删除该附件？', '删除确认', { type: 'warning' })
    await attachmentApi.remove(id)
    ElMessage.success('附件已删除')
  }

  return { validateFile, confirmRemove }
}
