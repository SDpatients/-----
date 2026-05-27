<script setup lang="ts">
import type { ImportErrorRow } from '@/types/importExport'

defineProps<{ rows: ImportErrorRow[] }>()
</script>

<template>
  <el-table :data="rows" border max-height="260">
    <el-table-column prop="rowNo" label="行号" width="80" />
    <el-table-column prop="field" label="字段" width="120" />
    <el-table-column prop="rawValue" label="原始值" width="140" />
    <el-table-column prop="reason" label="失败原因" min-width="220" />
  </el-table>
</template>
