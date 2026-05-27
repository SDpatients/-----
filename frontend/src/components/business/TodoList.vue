<script setup lang="ts">
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import type { PortalTodo } from '@/types/business'

defineProps<{ todos: PortalTodo[] }>()
const router = useRouter()

const resolvePath = (todo: PortalTodo): string => {
  const map: Record<string, string> = {
    purchase_order: `/orders/${todo.businessId}`,
    reconciliation: `/settlement/${todo.businessId}`,
    delivery_notice: `/asn/${todo.businessId}`,
    quality_inspection: `/quality/${todo.businessId}`,
    nonconformance_report: `/quality/${todo.businessId}`,
  }
  return map[todo.businessType] || `/messages?businessType=${todo.businessType}&businessId=${todo.businessId}`
}
</script>

<template>
  <el-table :data="todos" border>
    <el-table-column prop="title" label="待办事项" min-width="260" />
    <el-table-column prop="module" label="模块" width="120" />
    <el-table-column label="优先级" width="110"><template #default="{ row }"><StatusTag :value="row.priority" kind="risk" /></template></el-table-column>
    <el-table-column prop="dueDate" label="截止日期" width="140" />
    <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
    <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="router.push(resolvePath(row))">处理</el-button></template></el-table-column>
  </el-table>
</template>