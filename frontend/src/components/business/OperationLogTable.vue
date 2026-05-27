<script setup lang="ts">
import { onMounted, ref } from 'vue'
import StatusTag from './StatusTag.vue'
import OperationLogDrawer from './OperationLogDrawer.vue'
import { useOperationLog } from '@/composables/useOperationLog'
import type { OperationLogItem } from '@/types/operationLog'

const { loading, logs, loadLogs } = useOperationLog()
const visible = ref(false)
const current = ref<OperationLogItem>()

const open = (row: OperationLogItem) => {
  current.value = row
  visible.value = true
}

onMounted(() => loadLogs())
</script>

<template>
  <div>
    <el-table v-loading="loading" :data="logs" border>
      <el-table-column prop="module" label="模块" width="110" />
      <el-table-column prop="businessNo" label="业务单号" width="170" />
      <el-table-column prop="action" label="动作" width="100" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column label="前状态" width="110"><template #default="{ row }"><StatusTag :value="row.beforeStatus" /></template></el-table-column>
      <el-table-column label="后状态" width="110"><template #default="{ row }"><StatusTag :value="row.afterStatus" /></template></el-table-column>
      <el-table-column prop="result" label="结果" width="90" />
      <el-table-column prop="operatedAt" label="操作时间" width="170" />
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" @click="open(row)">追踪</el-button></template></el-table-column>
    </el-table>
    <OperationLogDrawer v-model="visible" :log="current" />
  </div>
</template>
