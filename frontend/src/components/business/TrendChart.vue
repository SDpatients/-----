<script setup lang="ts">
import type { TrendSeries } from '@/types/dashboard'

defineProps<{ data: TrendSeries[] }>()
</script>

<template>
  <div class="trend-chart">
    <div v-for="item in data" :key="item.label" class="trend-item">
      <div class="trend-bar" :style="{ height: `${item.value}px` }" />
      <span>{{ item.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.trend-chart { display: flex; gap: 18px; align-items: flex-end; height: 180px; padding: 20px; background: linear-gradient(180deg, #f8fbff, #edf4fb); border-radius: 14px; }
.trend-item { display: grid; flex: 1; gap: 8px; justify-items: center; color: #718096; }
.trend-bar { width: 100%; min-height: 20px; background: linear-gradient(180deg, #1f5eff, #0bb783); border-radius: 10px 10px 2px 2px; }
</style>
