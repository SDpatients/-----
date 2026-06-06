<script setup lang="ts">
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import type { PortalTodo } from '@/types/business'

defineProps<{ todos: PortalTodo[] }>()
const router = useRouter()

const resolvePath = (todo: PortalTodo): string => {
  const map: Record<string, string> = {
    purchase_order: `/purchasing/orders/${todo.businessId}`,
    delivery_notice: `/purchasing/asn/${todo.businessId}`,
    rfq: `/purchasing/rfq/${todo.businessId}`,
    quote: `/purchasing/quotes`,
    reconciliation: `/purchasing/financial-reconciliation`,
    quality_inspection: `/purchasing/asn/${todo.businessId}`,
    nonconformance_report: `/purchasing/asn/${todo.businessId}`,
    order_pending: `/purchasing/orders`,
    delivery_delay: `/purchasing/asn`,
    delivery_approaching: `/purchasing/asn`,
    order_overdue: `/purchasing/orders`,
    rfq_deadline: `/purchasing/rfq/${todo.businessId}`,
  }
  return map[todo.businessType] || `/messages?businessType=${todo.businessType}&businessId=${todo.businessId}`
}

const handleNavigate = (todo: PortalTodo) => {
  router.push(resolvePath(todo))
}
</script>

<template>
  <div v-if="todos.length === 0" class="empty-todo">暂无待办事项</div>
  <el-table v-else :data="todos" border>
    <el-table-column prop="title" label="待办事项" min-width="260">
      <template #default="{ row }">
        <el-link type="primary" :underline="false" @click="handleNavigate(row)">{{ row.title }}</el-link>
      </template>
    </el-table-column>
    <el-table-column prop="module" label="模块" width="120" />
    <el-table-column prop="businessNo" label="单据号" width="160">
      <template #default="{ row }"><span v-if="row.businessNo">{{ row.businessNo }}</span><span v-else class="text-muted">-</span></template>
    </el-table-column>
    <el-table-column label="优先级" width="100">
      <template #default="{ row }"><StatusTag :value="row.priority" kind="risk" /></template>
    </el-table-column>
    <el-table-column prop="dueDate" label="截止日期" width="140" />
    <el-table-column label="操作" width="100">
      <template #default="{ row }">
        <el-button link type="primary" @click="handleNavigate(row)">去处理</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.empty-todo {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}
.text-muted {
  color: #c0c4cc;
}
</style>