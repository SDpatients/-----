<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { importExportApi } from '@/api/importExport'

defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()
const form = reactive({ module: '采购订单', scope: 'current' as const, masked: true, asyncMode: true })

const submit = async () => {
  await importExportApi.createExport(form)
  ElMessage.success('导出任务已创建')
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="数据导出" width="520px" @update:model-value="emit('update:modelValue', $event)">
    <el-form :model="form" label-width="100px">
      <el-form-item label="业务模块"><el-select v-model="form.module"><el-option label="采购订单" value="采购订单" /><el-option label="ASN" value="ASN" /><el-option label="对账单" value="对账单" /></el-select></el-form-item>
      <el-form-item label="导出范围"><el-radio-group v-model="form.scope"><el-radio-button value="current">当前页</el-radio-button><el-radio-button value="all">全部</el-radio-button><el-radio-button value="selected">已选择</el-radio-button></el-radio-group></el-form-item>
      <el-form-item label="敏感字段"><el-switch v-model="form.masked" active-text="脱敏" /></el-form-item>
      <el-form-item label="异步导出"><el-switch v-model="form.asyncMode" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="emit('update:modelValue', false)">取消</el-button><el-button type="primary" @click="submit">创建导出</el-button></template>
  </el-dialog>
</template>
