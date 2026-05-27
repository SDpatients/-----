<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import type { PerformanceMetric } from '@/types/dashboard'

const props = withDefaults(defineProps<{
  scores?: { quality: number; delivery: number; service: number; price: number }
  history?: { period: string; quality: number; delivery: number; service: number; price: number }[]
}>(), {
  scores: () => ({ quality: 85, delivery: 78, service: 90, price: 72 }),
  history: () => [
    { period: 'Q1', quality: 80, delivery: 75, service: 88, price: 70 },
    { period: 'Q2', quality: 82, delivery: 76, service: 89, price: 71 },
    { period: 'Q3', quality: 84, delivery: 77, service: 89, price: 72 },
    { period: 'Q4', quality: 85, delivery: 78, service: 90, price: 72 },
  ],
})

const metrics = ref<PerformanceMetric[]>([])

onMounted(async () => {
  try {
    metrics.value = await dashboardApi.performance()
  } catch { /* */ }
})

// 雷达图配置
const radarSize = 220
const radarCenter = radarSize / 2
const radarRadius = 80
const radarLevels = 5
const radarLabels = ['质量', '交付', '服务', '价格']
const radarAngles = [Math.PI / 2, 0, -Math.PI / 2, Math.PI] // top, right, bottom, left

const radarPoints = computed(() => {
  const values = [props.scores.quality, props.scores.delivery, props.scores.service, props.scores.price]
  return values.map((v, i) => {
    const angle = radarAngles[i]
    const r = (v / 100) * radarRadius
    return { x: radarCenter + r * Math.cos(angle), y: radarCenter - r * Math.sin(angle) }
  })
})

const generateGridPath = (level: number) => {
  const r = (level / radarLevels) * radarRadius
  const pts = radarAngles.map(a => ({
    x: radarCenter + r * Math.cos(a),
    y: radarCenter - r * Math.sin(a),
  }))
  return pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ') + 'Z'
}

// 综合分
const totalScore = computed(() => {
  const { quality, delivery, service, price } = props.scores
  return Math.round((quality + delivery + service + price) / 4)
})

// 趋势图配置
const trendWidth = 400
const trendHeight = 160
const trendPadding = { top: 20, right: 20, bottom: 30, left: 40 }
const trendChartW = trendWidth - trendPadding.left - trendPadding.right
const trendChartH = trendHeight - trendPadding.top - trendPadding.bottom

const trendPoints = computed(() => {
  const periods = props.history
  const allValues = periods.flatMap(p => [p.quality, p.delivery, p.service, p.price])
  const maxVal = Math.max(...allValues, 100)
  const minVal = Math.min(...allValues, 0)
  const range = maxVal - minVal || 1

  return {
    quality: periods.map((p, i) => ({
      x: trendPadding.left + (i / (periods.length - 1)) * trendChartW,
      y: trendPadding.top + trendChartH - ((p.quality - minVal) / range) * trendChartH,
    })),
    delivery: periods.map((p, i) => ({
      x: trendPadding.left + (i / (periods.length - 1)) * trendChartW,
      y: trendPadding.top + trendChartH - ((p.delivery - minVal) / range) * trendChartH,
    })),
    service: periods.map((p, i) => ({
      x: trendPadding.left + (i / (periods.length - 1)) * trendChartW,
      y: trendPadding.top + trendChartH - ((p.service - minVal) / range) * trendChartH,
    })),
    price: periods.map((p, i) => ({
      x: trendPadding.left + (i / (periods.length - 1)) * trendChartW,
      y: trendPadding.top + trendChartH - ((p.price - minVal) / range) * trendChartH,
    })),
  }
})

const linePath = (pts: { x: number; y: number }[]) => {
  if (pts.length === 0) return ''
  return pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
}

const trendColors = { quality: '#409eff', delivery: '#67c23a', service: '#e6a23c', price: '#f56c6c' }
</script>

<template>
  <div class="performance-dashboard">
    <!-- 评分环形图 -->
    <div class="performance-grid">
      <div v-for="item in metrics" :key="item.label" class="performance-card">
        <div class="performance-ring" :style="{ borderColor: item.color }">{{ item.value }}</div>
        <div>{{ item.label }}</div>
      </div>
    </div>

    <!-- 6.2.8 雷达图 + 趋势图 -->
    <div class="charts-row">
      <!-- 雷达图 -->
      <div class="chart-container">
        <h4 class="chart-title">四维能力雷达图</h4>
        <svg :width="radarSize" :height="radarSize" :viewBox="`0 0 ${radarSize} ${radarSize}`" class="radar-svg">
          <!-- 背景网格 -->
          <polygon
            v-for="level in radarLevels"
            :key="'grid-' + level"
            :points="generateGridPath(level).replace(/[MLZ]/g, '').trim().replace(/\s+/g, ' ')"
            fill="none"
            stroke="#e4e7ed"
            stroke-width="1"
          />
          <!-- 轴线 -->
          <line
            v-for="(angle, i) in radarAngles"
            :key="'axis-' + i"
            :x1="radarCenter"
            :y1="radarCenter"
            :x2="(radarCenter + radarRadius * Math.cos(angle)).toFixed(1)"
            :y2="(radarCenter - radarRadius * Math.sin(angle)).toFixed(1)"
            stroke="#e4e7ed"
            stroke-width="1"
          />
          <!-- 数据多边形 -->
          <polygon :points="radarPoints.map(p => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')" fill="rgba(64,158,255,0.15)" stroke="#409eff" stroke-width="2" />
          <!-- 数据点 -->
          <circle
            v-for="(p, i) in radarPoints"
            :key="'dot-' + i"
            :cx="p.x.toFixed(1)"
            :cy="p.y.toFixed(1)"
            r="4"
            fill="#409eff"
          />
          <!-- 标签 -->
          <text
            v-for="(label, i) in radarLabels"
            :key="'label-' + i"
            :x="(radarCenter + (radarRadius + 25) * Math.cos(radarAngles[i])).toFixed(1)"
            :y="(radarCenter - (radarRadius + 25) * Math.sin(radarAngles[i]) + 5).toFixed(1)"
            text-anchor="middle"
            font-size="12"
            fill="#606266"
          >{{ label }}: {{ [props.scores.quality, props.scores.delivery, props.scores.service, props.scores.price][i] }}</text>
        </svg>
        <div class="chart-total">综合评分: <strong>{{ totalScore }}</strong></div>
      </div>

      <!-- 趋势图 -->
      <div class="chart-container trend-container">
        <h4 class="chart-title">绩效趋势（近4个季度）</h4>
        <svg :width="trendWidth" :height="trendHeight" :viewBox="`0 0 ${trendWidth} ${trendHeight}`" class="trend-svg">
          <!-- Y轴刻度线 -->
          <line :x1="trendPadding.left" :y1="trendPadding.top" :x2="trendPadding.left" :y2="trendPadding.top + trendChartH" stroke="#dcdfe6" stroke-width="1" />
          <!-- X轴刻度线 -->
          <line :x1="trendPadding.left" :y1="trendPadding.top + trendChartH" :x2="trendPadding.left + trendChartW" :y2="trendPadding.top + trendChartH" stroke="#dcdfe6" stroke-width="1" />
          <!-- 网格线 -->
          <line
            v-for="i in 4"
            :key="'hline-' + i"
            :x1="trendPadding.left"
            :y1="(trendPadding.top + (trendChartH * (i - 1)) / 3).toFixed(1)"
            :x2="(trendPadding.left + trendChartW).toFixed(1)"
            :y2="(trendPadding.top + (trendChartH * (i - 1)) / 3).toFixed(1)"
            stroke="#f0f0f0"
            stroke-width="1"
          />
          <!-- 数据线 -->
          <path
            v-for="(label, key) in { quality: '质量', delivery: '交付', service: '服务', price: '价格' }"
            :key="'line-' + key"
            :d="linePath(trendPoints[key as keyof typeof trendPoints])"
            fill="none"
            :stroke="trendColors[key as keyof typeof trendColors]"
            stroke-width="2"
          />
          <!-- 数据点 -->
          <template v-for="(label, key) in { quality: '质量', delivery: '交付', service: '服务', price: '价格' }" :key="'dots-' + key">
            <circle
              v-for="(p, i) in trendPoints[key as keyof typeof trendPoints]"
              :key="'dot-' + i"
              :cx="p.x.toFixed(1)"
              :cy="p.y.toFixed(1)"
              r="3"
              :fill="trendColors[key as keyof typeof trendColors]"
            />
          </template>
          <!-- X轴标签 -->
          <text
            v-for="(p, i) in props.history"
            :key="'xlabel-' + i"
            :x="(trendPadding.left + (i / (props.history.length - 1)) * trendChartW).toFixed(1)"
            :y="trendHeight - 4"
            text-anchor="middle"
            font-size="10"
            fill="#909399"
          >{{ p.period }}</text>
        </svg>
        <!-- 图例 -->
        <div class="trend-legend">
          <span v-for="(color, key) in trendColors" :key="key" class="legend-item">
            <span class="legend-dot" :style="{ background: color }"></span>
            {{ { quality: '质量', delivery: '交付', service: '服务', price: '价格' }[key] }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.performance-dashboard { display: grid; gap: 20px; }
.performance-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.performance-card { display: grid; gap: 12px; justify-items: center; padding: 18px; background: #f8fbff; border: 1px solid #e4ebf3; border-radius: 14px; }
.performance-ring { display: grid; width: 96px; height: 96px; font-size: 20px; font-weight: 800; place-items: center; border: 8px solid; border-radius: 50%; }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.chart-container { background: #ffffff; border: 1px solid #e4ebf3; border-radius: 14px; padding: 20px; display: flex; flex-direction: column; align-items: center; }
.chart-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 16px; align-self: flex-start; padding-left: 8px; border-left: 3px solid #409eff; }
.chart-total { margin-top: 12px; font-size: 14px; color: #606266; }
.chart-total strong { color: #409eff; font-size: 20px; }
.radar-svg { display: block; }
.trend-container { align-items: flex-start; }
.trend-svg { display: block; }
.trend-legend { display: flex; gap: 16px; margin-top: 8px; font-size: 12px; color: #606266; }
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
</style>