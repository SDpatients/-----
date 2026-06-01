<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import DashboardMetricGrid from '@/components/business/DashboardMetricGrid.vue'
import RiskWarningList from '@/components/business/RiskWarningList.vue'
import TrendChart from '@/components/business/TrendChart.vue'
import TodoList from '@/components/business/TodoList.vue'
import { dashboardApi } from '@/api/dashboard'
import { notificationApi } from '@/api/notification'
import { useDashboardRefresh } from '@/composables/useDashboardRefresh'
import type { DashboardMetric, RiskWarning, TrendSeries } from '@/types/dashboard'
import type { PortalTodo } from '@/types/business'

const metrics = ref<DashboardMetric[]>([])
const risks = ref<RiskWarning[]>([])
const trends = ref<TrendSeries[]>([])
const todos = ref<PortalTodo[]>([])

const loadDashboard = async () => {
  const [metricData, riskData, trendData, todoData] = await Promise.all([dashboardApi.metrics(), dashboardApi.risks(), dashboardApi.trends(), notificationApi.todos()])
  metrics.value = metricData
  risks.value = riskData
  trends.value = trendData
  todos.value = todoData
}

const { lastRefreshAt } = useDashboardRefresh(loadDashboard, 60)
</script>

<template>
  <div class="dashboard">
    <DashboardMetricGrid :metrics="metrics" />
    <div class="dashboard-grid">
      <PageContainer title="待办中心" :subtitle="`聚合采购方内部待处理事项，最后刷新：${lastRefreshAt}`">
        <TodoList :todos="todos" @refresh="loadDashboard" />
      </PageContainer>
      <PageContainer title="风险预警" subtitle="基于真实交付、质量、对账数据驱动的业务风险">
        <RiskWarningList :risks="risks" />
      </PageContainer>
    </div>
    <PageContainer title="采购协同趋势" subtitle="订单、发货、检验、对账的阶段性趋势占位">
      <TrendChart :data="trends" />
    </PageContainer>
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
