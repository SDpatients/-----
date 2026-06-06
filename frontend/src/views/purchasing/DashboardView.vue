<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import DashboardMetricGrid from '@/components/business/DashboardMetricGrid.vue'
import RiskWarningList from '@/components/business/RiskWarningList.vue'
import TodoList from '@/components/business/TodoList.vue'
import { dashboardApi } from '@/api/dashboard'
import { notificationApi } from '@/api/notification'
import { useDashboardRefresh } from '@/composables/useDashboardRefresh'
import type { DashboardMetric, RiskWarning } from '@/types/dashboard'
import type { PortalTodo } from '@/types/business'

const metrics = ref<DashboardMetric[]>([])
const risks = ref<RiskWarning[]>([])
const todos = ref<PortalTodo[]>([])

const loadDashboard = async () => {
  const [metricData, riskData, todoData] = await Promise.all([dashboardApi.metrics(), dashboardApi.risks(), notificationApi.todos()])
  metrics.value = metricData
  risks.value = riskData
  todos.value = todoData
}

const { lastRefreshAt } = useDashboardRefresh(loadDashboard, 60)
</script>

<template>
  <div class="dashboard">
    <DashboardMetricGrid :metrics="metrics" />
    <div class="dashboard-grid">
      <PageContainer title="待办中心" :subtitle="`聚合采购方内部待处理事项，最后刷新：${lastRefreshAt}`">
        <TodoList :todos="todos" />
      </PageContainer>
      <PageContainer title="风险预警" subtitle="基于真实交付、质量、对账数据驱动的业务风险">
        <RiskWarningList :risks="risks" />
      </PageContainer>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: grid;
  gap: 18px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

</style>
