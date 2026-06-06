<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import { useUserStore } from '@/stores/user'
import type { RiskWarning } from '@/types/dashboard'

const props = defineProps<{ risks: RiskWarning[] }>()
const router = useRouter()
const userStore = useUserStore()

// 采购方 / 供应商点击预警后的目标路径分流
const pathMap: Record<string, { purchasing: string; supplier: string }> = {
  order_pending: { purchasing: '/purchasing/orders', supplier: '/supplier/orders' },
  delivery_delay: { purchasing: '/purchasing/asn', supplier: '/supplier/deliveries' },
  delivery_approaching: { purchasing: '/purchasing/asn', supplier: '/supplier/deliveries' },
  order_overdue: { purchasing: '/purchasing/orders', supplier: '/supplier/orders' },
}

const resolvedRisks = computed(() =>
  props.risks.map((item) => {
    const mapped = pathMap[item.module]
    const targetPath = mapped
      ? userStore.user?.userType === 'supplier'
        ? mapped.supplier
        : mapped.purchasing
      : item.targetPath || '/messages'
    return { ...item, targetPath }
  }),
)

const isEmpty = computed(() => resolvedRisks.value.every((item) => !item.count))
</script>

<template>
  <div>
    <div v-if="isEmpty" class="risk-empty">
      <span>当前无业务风险，状态良好</span>
    </div>
    <div
      v-for="item in resolvedRisks"
      v-else
      :key="item.module"
      class="risk-row"
      :class="{ 'risk-row--danger': item.level === 'danger' }"
      @click="router.push(item.targetPath)"
    >
      <div class="risk-info">
        <strong>{{ item.title }}</strong>
        <span v-if="item.count !== undefined && item.count !== null" class="risk-count">{{ item.count }} 单</span>
        <span class="risk-module">{{ item.module }}</span>
      </div>
      <StatusTag :value="item.level" kind="risk" />
    </div>
  </div>
</template>

<style scoped>
.risk-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  margin-bottom: 12px;
  cursor: pointer;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
  transition: background 0.15s ease;
}
.risk-row:hover { background: #ffedd5; }
.risk-row--danger { background: #fef2f2; border-color: #fecaca; }
.risk-row--danger:hover { background: #fee2e2; }
.risk-info { display: flex; flex-direction: column; gap: 4px; }
.risk-count { font-size: 16px; font-weight: 600; color: #c2410c; }
.risk-row--danger .risk-count { color: #b91c1c; }
.risk-module { font-size: 12px; color: #92400e; }
.risk-row--danger .risk-module { color: #991b1b; }
.risk-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 12px;
  color: #16a34a;
  background: #f0fdf4;
  border: 1px dashed #bbf7d0;
  border-radius: 12px;
  font-size: 14px;
}
</style>
