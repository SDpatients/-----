<script setup lang="ts">
import { ref } from 'vue'
import { supplierApi } from '@/api/supplier'
import { toSupplier } from '@/api/adapters'
import type { Supplier } from '@/types/business'

const props = withDefaults(defineProps<{
  modelValue?: number | number[] | null
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
  multiple?: boolean
}>(), {
  placeholder: '搜索供应商名称/编码',
  clearable: true,
  disabled: false,
  multiple: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: number | number[] | null]
  select: [supplier: Supplier]
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
      options.value = result.records.map(toSupplier)
    } finally {
      loading.value = false
    }
  }, 300)
}

const handleChange = (val: number | number[] | undefined) => {
  if (props.multiple) {
    emit('update:modelValue', (val as number[]) ?? [])
  } else {
    emit('update:modelValue', (val as number) ?? null)
    if (val && !Array.isArray(val)) {
      const selected = options.value.find(s => s.id === val)
      if (selected) emit('select', selected)
    }
  }
}

</script>

<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
    :multiple="multiple"
    :collapse-tags="multiple"
    :collapse-tags-tooltip="multiple"
    filterable
    remote
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