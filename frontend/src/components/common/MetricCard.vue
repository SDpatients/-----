<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps<{
  label: string
  value: string | number
  trend: string
  tone: 'blue' | 'green' | 'orange' | 'red'
  path?: string
}>()

const router = useRouter()

const handleClick = () => {
  if (props.path) router.push(props.path)
}
</script>

<template>
  <div class="metric-card" :class="[`metric-card--${tone}`, { 'metric-card--clickable': path }]" @click="handleClick">
    <div class="metric-label">{{ label }}</div>
    <div class="metric-value">{{ value }}</div>
    <div class="metric-trend">{{ trend }}</div>
  </div>
</template>

<style scoped>
.metric-card {
  position: relative;
  overflow: hidden;
  padding: 18px;
  color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 16px 36px rgba(31, 94, 255, 0.16);
  transition: transform 0.2s, box-shadow 0.2s;
}
.metric-card--clickable {
  cursor: pointer;
}
.metric-card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 44px rgba(31, 94, 255, 0.24);
}

.metric-card::after {
  position: absolute;
  right: -28px;
  bottom: -42px;
  width: 120px;
  height: 120px;
  content: "";
  background: rgba(255, 255, 255, 0.16);
  border-radius: 50%;
}

.metric-card--blue {
  background: linear-gradient(135deg, #1f5eff, #0f2f74);
}

.metric-card--green {
  background: linear-gradient(135deg, #0bb783, #07685d);
}

.metric-card--orange {
  background: linear-gradient(135deg, #f59e0b, #92400e);
}

.metric-card--red {
  background: linear-gradient(135deg, #ef4444, #991b1b);
}

.metric-label {
  font-size: 13px;
  opacity: 0.84;
}

.metric-value {
  margin: 10px 0 6px;
  font-size: 30px;
  font-weight: 800;
}

.metric-trend {
  font-size: 12px;
  opacity: 0.86;
}
</style>
