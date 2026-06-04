<script setup lang="ts">
import { ref } from 'vue'
import { orderApi } from '@/api/order'
import { toOrder } from '@/api/adapters'
import type { PurchaseOrder } from '@/types/business'

const props = withDefaults(defineProps<{
  modelValue?: number | string | null
  placeholder?: string
  clearable?: boolean
}>(), {
  placeholder: '搜索订单号选择订单',
  clearable: true,
})

const emit = defineEmits<{
  'update:modelValue': [value: number | string | null]
  select: [order: PurchaseOrder]
}>()

const options = ref<PurchaseOrder[]>([])
const loading = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

const remoteSearch = (keyword: string) => {
  if (timer) clearTimeout(timer)
  if (!keyword) {
    options.value = []
    return
  }
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const result = await orderApi.page({ pageNum: 1, pageSize: 20, keyword })
      options.value = result.records.map(toOrder)
    } finally {
      loading.value = false
    }
  }, 300)
}

const handleChange = (val: number | string | undefined) => {
  emit('update:modelValue', val ?? null)
  if (val) {
    const selected = options.value.find(o => o.id === val)
    if (selected) emit('select', selected)
  }
}
</script>

<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :loading="loading"
    filterable
    remote
    :remote-method="remoteSearch"
    style="width: 100%"
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.orderNo} - ${item.supplierName}`"
      :value="item.id"
    >
      <div class="order-option">
        <span class="order-no">{{ item.orderNo }}</span>
        <span class="order-supplier">{{ item.supplierName }}</span>
        <span class="order-amount">{{ item.amount }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<style scoped>
.order-option {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.order-no {
  font-weight: 500;
  color: #303133;
}
.order-supplier {
  flex: 1;
  font-size: 13px;
  color: #606266;
}
.order-amount {
  font-size: 12px;
  color: #909399;
}
</style>