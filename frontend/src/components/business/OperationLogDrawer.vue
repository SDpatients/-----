<script setup lang="ts">
import type { OperationLogItem } from '@/types/operationLog'

defineProps<{ modelValue: boolean; log?: OperationLogItem }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()
</script>

<template>
  <el-drawer :model-value="modelValue" title="日志追踪详情" size="420px" @update:model-value="emit('update:modelValue', $event)">
    <el-descriptions v-if="log" :column="1" border>
      <el-descriptions-item label="模块">{{ log.module }}</el-descriptions-item>
      <el-descriptions-item label="业务单号">{{ log.businessNo }}</el-descriptions-item>
      <el-descriptions-item label="动作">{{ log.action }}</el-descriptions-item>
      <el-descriptions-item label="操作人">{{ log.operator }}</el-descriptions-item>
      <el-descriptions-item label="状态变化">{{ log.beforeStatus }} → {{ log.afterStatus }}</el-descriptions-item>
      <el-descriptions-item label="TraceId">{{ log.traceId }}</el-descriptions-item>
      <el-descriptions-item label="IP">{{ log.ip }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ log.remark }}</el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>
