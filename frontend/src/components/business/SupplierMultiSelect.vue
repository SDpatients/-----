<script setup lang="ts">
import { ref } from 'vue'
import { supplierApi } from '@/api/supplier'
import type { Supplier } from '@/types/business'

const props = withDefaults(defineProps<{
  modelValue?: (number | string)[]
  placeholder?: string
  disabled?: boolean
}>(), {
  modelValue: () => [],
  placeholder: '搜索并选择供应商',
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: (number | string)[]]
}>()

const options = ref<Supplier[]>([])
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
      const result = await supplierApi.page({ pageNum: 1, pageSize: 20, keyword })
      options.value = result.records
    } finally {
      loading.value = false
    }
  }, 300)
}

const handleChange = (vals: (number | string)[]) => {
  emit('update:modelValue', vals)
}
</script>

<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :loading="loading"
    filterable
    remote
    multiple
    :remote-method="remoteSearch"
    style="width: 100%"
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.name}（${item.code}）`"
      :value="item.id"
    >
      <div class="supplier-option">
        <span class="supplier-name">{{ item.name }}</span>
        <span class="supplier-code">{{ item.code }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<style scoped>
.supplier-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.supplier-name {
  font-weight: 500;
  color: #303133;
}
.supplier-code {
  font-size: 12px;
  color: #909399;
}
</style>