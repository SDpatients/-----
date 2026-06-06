<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { attachmentApi } from '@/api/attachment'
import { useAttachment } from '@/composables/useAttachment'
import type { AttachmentFile } from '@/types/business'
import type { UploadRawFile } from 'element-plus'

const props = withDefaults(defineProps<{
  businessType?: string
  businessId?: number | string
  editable?: boolean
}>(), {
  businessType: '',
  businessId: '',
  editable: false,
})

const emit = defineEmits<{
  (e: 'change'): void
}>()

const files = ref<AttachmentFile[]>([])
const loading = ref(false)
const uploading = ref(false)
const renameDialogVisible = ref(false)
const renameTarget = ref<AttachmentFile | null>(null)
const renameFileName = ref('')
const renameFileExt = ref('')

const { validateFile } = useAttachment()

const loadFiles = async () => {
  loading.value = true
  try {
    files.value = await attachmentApi.list({
      businessType: props.businessType || '',
      businessId: props.businessId != null ? String(props.businessId) : '',
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadFiles)
watch(() => [props.businessType, props.businessId], loadFiles)

const preview = async (row: AttachmentFile) => {
  await attachmentApi.preview(row.id as number, row.fileName)
}

const download = async (row: AttachmentFile) => {
  await attachmentApi.download(row.id as number, row.fileName)
}

const handleDelete = async (row: AttachmentFile) => {
  try {
    await ElMessageBox.confirm(`确认删除附件「${row.fileName}」？`, '删除确认', { type: 'warning' })
    await attachmentApi.remove(row.id as number)
    ElMessage.success('附件已删除')
    loadFiles()
    emit('change')
  } catch { /* cancelled */ }
}

const openRename = (row: AttachmentFile) => {
  renameTarget.value = row
  const lastDotIndex = row.fileName.lastIndexOf('.')
  if (lastDotIndex > 0) {
    renameFileName.value = row.fileName.substring(0, lastDotIndex)
    renameFileExt.value = row.fileName.substring(lastDotIndex) // 包含点号
  } else {
    renameFileName.value = row.fileName
    renameFileExt.value = ''
  }
  renameDialogVisible.value = true
}

const submitRename = async () => {
  if (!renameTarget.value || !renameFileName.value.trim()) {
    ElMessage.warning('文件名不能为空')
    return
  }
  const newFileName = renameFileName.value.trim() + renameFileExt.value
  if (newFileName === renameTarget.value.fileName) {
    renameDialogVisible.value = false
    return
  }
  try {
    await attachmentApi.rename(renameTarget.value.id as number, newFileName)
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    loadFiles()
    emit('change')
  } catch { /* interceptor handles */ }
}

const handleUploadChange = (uploadFile: any, uploadFiles: any) => {
  fileList.value = uploadFiles
}

const fileList = ref<any[]>([])

const submitUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    for (const f of fileList.value) {
      const rawFile = f.raw as File
      if (!validateFile(rawFile)) {
        uploading.value = false
        return
      }
      // 19 位雪花 ID 必须按字符串透传，避免 Number() 精度丢失
      const businessId = props.businessId !== '' && props.businessId != null ? String(props.businessId) : undefined
      await attachmentApi.uploadBinary(rawFile, props.businessType, businessId)
    }
    ElMessage.success('附件上传成功')
    fileList.value = []
    loadFiles()
    emit('change')
  } catch {
    ElMessage.error('附件上传失败')
  } finally {
    uploading.value = false
  }
}

const formatTime = (val: string) => {
  if (!val || val === '-') return '-'
  if (val.includes('T')) {
    return val.replace('T', ' ').substring(0, 16)
  }
  return val.length > 16 ? val.substring(0, 16) : val
}
</script>

<template>
  <div class="attachment-panel">
    <div v-if="editable" class="attachment-toolbar">
      <el-upload
        v-model:file-list="fileList"
        action="#"
        :auto-upload="false"
        :show-file-list="true"
        :on-change="handleUploadChange"
        multiple
        class="upload-inline"
      >
        <el-button type="primary" size="small">选择文件</el-button>
      </el-upload>
      <el-button type="success" size="small" :loading="uploading" :disabled="fileList.length === 0" @click="submitUpload">
        {{ uploading ? '上传中...' : '上传附件' }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="files" border size="small" class="attachment-table">
      <el-table-column prop="fileName" label="文件名称" min-width="220" show-overflow-tooltip />
      <el-table-column prop="size" label="大小" width="100" />
      <el-table-column prop="uploader" label="上传人" width="100" />
      <el-table-column label="上传时间" width="170">
        <template #default="{ row }">{{ formatTime(row.uploadedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" :width="editable ? 250 : 150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="preview(row)">预览</el-button>
          <el-button link type="success" size="small" @click="download(row)">下载</el-button>
          <template v-if="editable">
            <el-button link type="warning" size="small" @click="openRename(row)">重命名</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && files.length === 0" description="暂无附件" :image-size="60" />

    <el-dialog v-model="renameDialogVisible" title="重命名附件" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="原文件名">
          <span class="rename-old-name">{{ renameTarget?.fileName }}</span>
        </el-form-item>
        <el-form-item label="新文件名">
          <div class="rename-input-group">
            <el-input v-model="renameFileName" placeholder="请输入新文件名" @keyup.enter="submitRename" />
            <span v-if="renameFileExt" class="rename-ext-suffix">{{ renameFileExt }}</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRename">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.attachment-panel {
  width: 100%;
}
.attachment-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.upload-inline :deep(.el-upload-list) {
  max-height: 120px;
  overflow-y: auto;
}
.attachment-table {
  width: 100%;
}
.rename-old-name {
  color: #909399;
  font-size: 13px;
}
.rename-input-group {
  display: flex;
  align-items: center;
  flex: 1;
}
.rename-input-group .el-input {
  flex: 1;
}
.rename-ext-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
  user-select: none;
}
</style>
