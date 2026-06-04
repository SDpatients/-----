<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { attachmentApi } from '@/api/attachment'
import type { AttachmentVersion } from '@/types/attachment'

const props = withDefaults(defineProps<{
  businessType?: string
  businessId?: number | string
}>(), {
  businessType: '',
  businessId: '',
})

defineOptions({ inheritAttrs: false })

const versions = ref<AttachmentVersion[]>([])
const loading = ref(false)

const loadVersions = async () => {
  if (!props.businessId) {
    versions.value = []
    return
  }
  loading.value = true
  try {
    versions.value = await attachmentApi.versions({
      businessType: props.businessType,
      businessId: props.businessId,
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadVersions)
watch(() => [props.businessType, props.businessId], loadVersions)
</script>

<template>
  <el-table v-loading="loading" :data="versions" border>
    <el-table-column prop="fileName" label="文件名称" min-width="220" />
    <el-table-column prop="version" label="版本" width="90" />
    <el-table-column prop="uploader" label="上传人" width="100" />
    <el-table-column prop="uploadedAt" label="上传时间" width="170" />
    <el-table-column prop="remark" label="版本备注" min-width="180" />
  </el-table>
  <el-empty v-if="!loading && versions.length === 0" description="暂无附件版本" :image-size="60" />
</template>
