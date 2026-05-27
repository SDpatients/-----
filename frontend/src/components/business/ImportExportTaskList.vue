<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { importExportApi } from '@/api/importExport'
import StatusTag from './StatusTag.vue'
import type { ImportExportTask } from '@/types/business'

const tasks = ref<ImportExportTask[]>([])

const loadData = async () => {
  tasks.value = await importExportApi.tasks()
}

const download = async (id: number) => {
  await importExportApi.downloadExport(id)
}

onMounted(loadData)
</script>

<template>
  <el-table :data="tasks" border>
    <el-table-column prop="taskNo" label="任务号" width="160" />
    <el-table-column prop="module" label="模块" width="120" />
    <el-table-column label="类型" width="90"><template #default="{ row }">{{ row.type === 'import' ? '导入' : '导出' }}</template></el-table-column>
    <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
    <el-table-column prop="total" label="总数" width="90" />
    <el-table-column prop="success" label="成功" width="90" />
    <el-table-column prop="failed" label="失败" width="90" />
    <el-table-column prop="createdAt" label="创建时间" min-width="170" />
    <el-table-column label="操作" width="180"><template #default="{ row }"><el-button link type="primary" @click="loadData">刷新</el-button><el-button link type="success" @click="download(row.id)">下载结果</el-button><el-button v-if="row.failed" link type="danger">错误明细</el-button></template></el-table-column>
  </el-table>
</template>
