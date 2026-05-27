<script setup lang="ts">
import { computed } from 'vue'
import { riskMap, statusMap } from '@/constants/status'

const props = defineProps<{
  value?: string | number
  prefix?: string
  kind?: 'status' | 'risk'
}>()

const config = computed(() => {
  const map = props.kind === 'risk' ? riskMap : statusMap
  const key = props.prefix ? `${props.prefix}${props.value}` : String(props.value ?? '')
  return map[key] || { label: key || '-', type: 'info' }
})
</script>

<template>
  <el-tag :type="config.type" effect="light" round>{{ config.label }}</el-tag>
</template>
