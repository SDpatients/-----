<script setup lang="ts">
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import type { RiskWarning } from '@/types/dashboard'

defineProps<{ risks: RiskWarning[] }>()
const router = useRouter()
</script>

<template>
  <div>
    <div v-for="item in risks" :key="item.id" class="risk-row" @click="router.push(item.targetPath)">
      <div><strong>{{ item.title }}</strong><span>{{ item.module }}</span></div>
      <StatusTag :value="item.level" kind="risk" />
    </div>
  </div>
</template>

<style scoped>
.risk-row { display: flex; align-items: center; justify-content: space-between; padding: 14px; margin-bottom: 12px; cursor: pointer; background: #fff7ed; border: 1px solid #fed7aa; border-radius: 12px; }
.risk-row span { display: block; margin-top: 4px; font-size: 12px; color: #92400e; }
</style>
