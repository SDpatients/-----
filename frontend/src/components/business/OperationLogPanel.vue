<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { mockApi } from '@/api/mockApi'
import type { OperationLog } from '@/types/business'

const logs = ref<OperationLog[]>([])

onMounted(async () => {
  logs.value = await mockApi.getOperationLogs()
})
</script>

<template>
  <el-timeline>
    <el-timeline-item v-for="item in logs" :key="item.id" :timestamp="item.operatedAt" type="primary">
      <strong>{{ item.action }} · {{ item.result }}</strong>
      <div>{{ item.operator }}：{{ item.remark }}</div>
    </el-timeline-item>
  </el-timeline>
</template>
