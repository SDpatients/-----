<script setup lang="ts">
import { ref } from 'vue'
import { materialApi } from '@/api/material'
import type { Material } from '@/types/business'

const props = withDefaults(defineProps<{
  modelValue?: number | null
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
}>(), {
  placeholder: '搜索物料编码/名称',
  clearable: true,
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  select: [material: Material]
}>()

const options = ref<Material[]>([])
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
      const result = await materialApi.search(keyword)
      options.value = result.records
    } finally {
      loading.value = false
    }
  }, 300)
}

const handleChange = (val: number | undefined) => {
  emit('update:modelValue', val ?? null)
  if (val) {
    const selected = options.value.find(m => m.id === val)
    if (selected) emit('select', selected)
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
    filterable
    remote
    :remote-method="remoteSearch"
    style="width: 100%"
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.code} - ${item.name}`"
      :value="item.id"
    >
      <div class="material-option">
        <span class="material-code">{{ item.code }}</span>
        <span class="material-name">{{ item.name }}</span>
        <span class="material-spec">{{ item.spec }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<style scoped>
.material-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.material-code {
  font-weight: 600;
  color: #303133;
  flex-shrink: 0;
}
.material-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.material-spec {
  font-size: 11px;
  color: #909399;
  flex-shrink: 0;
}
</style>