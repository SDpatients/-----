<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { importExportApi } from '@/api/importExport'
import type { ExportRequest } from '@/types/importExport'

const props = defineProps<{
  modelValue: boolean
  /** 当前业务模块，如 'ASN'、'采购订单' 等 */
  module?: string
  /** 页面查询条件，传递给后端导出接口 */
  queryParams?: Record<string, unknown>
  /** 已选中的行ID列表 */
  selectedIds?: (number | string)[]
}>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()

const form = reactive<ExportRequest>({
  module: '采购订单',
  scope: 'current',
  masked: true,
  asyncMode: true,
  queryParams: {},
  selectedIds: [],
})

watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.module) form.module = props.module
    form.queryParams = props.queryParams ?? {}
    form.selectedIds = props.selectedIds ?? []
  }
})

const submit = async () => {
  if (form.scope === 'selected' && (!form.selectedIds || form.selectedIds.length === 0)) {
    ElMessage.warning('请先选择要导出的数据')
    return
  }
  await importExportApi.createExport(form)
  ElMessage.success('导出任务已创建，请稍后在任务列表中下载')
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="数据导出" width="520px" @update:model-value="emit('update:modelValue', $event)">
    <el-form :model="form" label-width="100px">
      <el-form-item label="业务模块">
        <el-select v-model="form.module">
          <el-option label="采购订单" value="采购订单" />
          <el-option label="ASN" value="ASN" />
          <el-option label="对账单" value="对账单" />
        </el-select>
      </el-form-item>
      <el-form-item label="导出范围">
        <el-radio-group v-model="form.scope">
          <el-radio-button value="current">当前筛选</el-radio-button>
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="selected">已选择</el-radio-button>
        </el-radio-group>
        <div v-if="form.scope === 'selected'" class="text-xs text-gray-400 mt-1">
          已选择 {{ form.selectedIds?.length ?? 0 }} 条数据
        </div>
      </el-form-item>
      <el-form-item label="敏感字段"><el-switch v-model="form.masked" active-text="脱敏" /></el-form-item>
      <el-form-item label="异步导出"><el-switch v-model="form.asyncMode" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="submit">创建导出</el-button>
    </template>
  </el-dialog>
</template>
