<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { attachmentApi } from '@/api/attachment'
import type { AttachmentFile } from '@/types/business'

const props = withDefaults(defineProps<{
  businessType?: string
  businessId?: number | string
}>(), {
  businessType: '',
  businessId: '',
})

const files = ref<AttachmentFile[]>([])

const loadFiles = async () => {
  files.value = await attachmentApi.list({
    businessType: props.businessType || '',
    businessId: props.businessId != null ? String(props.businessId) : '',
  })
}

onMounted(loadFiles)
watch(() => [props.businessType, props.businessId], loadFiles)

const preview = async (id: number) => {
  await attachmentApi.preview(id)
}

const download = async (id: number) => {
  await attachmentApi.download(id)
}
</script>

<template>
  <el-table :data="files" border>
    <el-table-column prop="fileName" label="文件名称" min-width="220" />
    <el-table-column prop="version" label="版本" width="90" />
    <el-table-column prop="category" label="分类" width="120" />
    <el-table-column prop="size" label="大小" width="100" />
    <el-table-column prop="uploader" label="上传人" width="100" />
    <el-table-column prop="uploadedAt" label="上传时间" width="170" />
    <el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" @click="preview(row.id)">预览</el-button><el-button link type="success" @click="download(row.id)">下载</el-button></template></el-table-column>
  </el-table>
</template>
