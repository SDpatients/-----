<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type UploadProps, type UploadUserFile } from 'element-plus'
import { attachmentApi } from '@/api/attachment'
import { useAttachment } from '@/composables/useAttachment'
import type { UploadRawFile } from 'element-plus'

const props = withDefaults(defineProps<{
  businessType?: string
  businessId?: number | string
}>(), {
  businessType: 'supplier',
  businessId: '',
})

const fileList = ref<UploadUserFile[]>([])
const progress = ref(0)
const uploading = ref(false)
const { validateFile } = useAttachment()

const beforeUpload: UploadProps['beforeUpload'] = (file) => validateFile(file)

const submit = async () => {
  const fileName = fileList.value[0]?.name
  const rawFile = fileList.value[0]?.raw as UploadRawFile | undefined
  if (!fileName) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  progress.value = 68
  if (rawFile) {
    // 19 位雪花 ID 必须按字符串透传，避免 Number() 精度丢失
    const businessId = props.businessId !== '' && props.businessId != null ? String(props.businessId) : undefined
    await attachmentApi.uploadBinary(rawFile as unknown as File, props.businessType, businessId)
  }
  progress.value = 100
  uploading.value = false
  ElMessage.success('附件上传成功')
}
</script>

<template>
  <div class="upload-card">
    <el-upload v-model:file-list="fileList" drag action="#" :auto-upload="false" :before-upload="beforeUpload" :limit="3">
      <div class="upload-title">拖拽文件到此处，或点击选择</div>
      <div class="upload-tip">支持 PDF、图片、Excel、Word，单文件不超过 20MB，最多 3 个</div>
    </el-upload>
    <el-progress v-if="uploading || progress > 0" :percentage="progress" class="mt-4" />
    <div class="mt-4"><el-button type="primary" :loading="uploading" @click="submit">上传附件</el-button><el-button>失败重试</el-button></div>
  </div>
</template>

<style scoped>
.upload-card { padding: 16px; background: #f8fbff; border: 1px solid #e4ebf3; border-radius: 14px; }
.upload-title { font-size: 16px; font-weight: 700; color: #17233d; }
.upload-tip { margin-top: 8px; color: #718096; }
</style>
