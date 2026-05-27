<script setup lang="ts">
import MetricCard from '@/components/common/MetricCard.vue'
import type { DashboardMetric } from '@/types/dashboard'

defineProps<{ metrics: DashboardMetric[] }>()
</script>

<template>
  <div class="metric-grid">
    <MetricCard v-for="item in metrics" :key="item.label" :label="item.label" :value="item.value" :trend="item.trend" :tone="item.tone" :path="item.path" />
  </div>
</template>

<style scoped>
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 18px; }
</style>
