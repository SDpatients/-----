<script setup lang="ts">
const metrics = [
  { label: '交付达成率', value: '96.8%', color: '#0bb783' },
  { label: '质量关闭率', value: '92.1%', color: '#1f5eff' },
  { label: '平均对账周期', value: '4.6天', color: '#f59e0b' },
  { label: '附件完整率', value: '98.2%', color: '#6366f1' },
]
</script>

<template>
  <div class="board-grid">
    <div v-for="item in metrics" :key="item.label" class="board-card">
      <div class="board-ring" :style="{ borderColor: item.color }">{{ item.value }}</div>
      <div class="board-label">{{ item.label }}</div>
    </div>
  </div>
</template>

<style scoped>
.board-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.board-card { display: grid; gap: 12px; justify-items: center; padding: 18px; background: #f8fbff; border: 1px solid #e4ebf3; border-radius: 14px; }
.board-ring { display: grid; width: 96px; height: 96px; font-size: 20px; font-weight: 800; place-items: center; border: 8px solid; border-radius: 50%; }
.board-label { color: #607089; }
</style>
