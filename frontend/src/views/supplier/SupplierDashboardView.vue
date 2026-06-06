<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import { notificationApi } from '@/api/notification'
import MetricCard from '@/components/common/MetricCard.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PerformanceBoard from '@/components/business/PerformanceBoard.vue'
import TodoList from '@/components/business/TodoList.vue'
import type { DashboardMetric } from '@/types/dashboard'
import type { PortalTodo } from '@/types/business'

const metrics = ref<DashboardMetric[]>([])
const todos = ref<PortalTodo[]>([])

onMounted(async () => {
  const [m, t] = await Promise.all([
    dashboardApi.metrics(),
    notificationApi.todos(),
  ])
  metrics.value = m
  todos.value = t
})
</script>

<template>
  <div class="supplier-dashboard">
    <div class="metrics">
      <MetricCard v-for="m in metrics" :key="m.label" :label="m.label" :value="m.value" :trend="m.trend" :tone="m.tone" :path="m.path" />
    </div>
    <PageContainer title="供应商待办" subtitle="集中处理接单、发货、整改、对账任务">
      <TodoList :todos="todos" />
    </PageContainer>
    <PageContainer title="协同表现看板" subtitle="供应商侧交付、质量、对账与附件完整率">
      <PerformanceBoard />
    </PageContainer>
  </div>
</template>

<style scoped>
.supplier-dashboard { display: grid; gap: 18px; }
.metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
</style>