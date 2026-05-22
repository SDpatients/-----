<script setup lang="ts">
import { computed } from 'vue'
import { riskMap, statusMap } from '@/constants/status'

const props = defineProps<{
  value?: string
  kind?: 'status' | 'risk'
}>()

const config = computed(() => {
  const map = props.kind === 'risk' ? riskMap : statusMap
  return map[props.value || ''] || { label: props.value || '-', type: 'info' }
})
</script>

<template>
  <el-tag :type="config.type" effect="light" round>{{ config.label }}</el-tag>
</template>
