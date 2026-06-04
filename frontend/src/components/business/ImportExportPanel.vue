<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { importExportApi } from '@/api/importExport'
import ImportDialog from './ImportDialog.vue'
import ExportDialog from './ExportDialog.vue'
import ImportExportTaskList from './ImportExportTaskList.vue'

const props = withDefaults(defineProps<{
  module?: string
}>(), {
  module: '',
})

const importVisible = ref(false)
const exportVisible = ref(false)

const downloadTemplate = async () => {
  try {
    const templates = await importExportApi.templates()
    if (templates.length > 0) {
      await importExportApi.downloadTemplate(templates[0].id)
      ElMessage.success('模板下载已开始')
    } else {
      ElMessage.warning('暂无可用模板')
    }
  } catch { /* 拦截器处理 */ }
}
</script>

<template>
  <div>
    <div class="mb-4"><el-button type="primary" @click="exportVisible = true">新建导出</el-button><el-button @click="importVisible = true">数据导入</el-button><el-button @click="downloadTemplate">下载模板</el-button></div>
    <ImportExportTaskList />
    <ImportDialog v-model="importVisible" />
    <ExportDialog v-model="exportVisible" />
  </div>
</template>
