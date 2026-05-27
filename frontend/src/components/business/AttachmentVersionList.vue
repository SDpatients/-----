<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { attachmentApi } from '@/api/attachment'
import type { AttachmentVersion } from '@/types/attachment'

const versions = ref<AttachmentVersion[]>([])

onMounted(async () => {
  versions.value = await attachmentApi.versions(1)
})
</script>

<template>
  <el-table :data="versions" border>
    <el-table-column prop="fileName" label="文件名称" min-width="220" />
    <el-table-column prop="version" label="版本" width="90" />
    <el-table-column prop="uploader" label="上传人" width="100" />
    <el-table-column prop="uploadedAt" label="上传时间" width="170" />
    <el-table-column prop="remark" label="版本备注" min-width="180" />
  </el-table>
</template>
