<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type UploadProps } from 'element-plus'
import { importExportApi } from '@/api/importExport'
import { useImportExport } from '@/composables/useImportExport'
import ImportErrorTable from './ImportErrorTable.vue'

defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()
const { validating, validateResult, validate: validateFile } = useImportExport()
const selectedFile = ref<File>()
const importType = ref('supplier')

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  selectedFile.value = file
  return false
}

const validate = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择导入文件')
    return
  }
  await validateFile(importType.value, selectedFile.value)
}

const submit = async () => {
  if (!validateResult.value?.fileId) return
  await importExportApi.submitImport(importType.value, validateResult.value.fileId)
  ElMessage.success('导入任务已提交')
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="数据导入" width="760px" @update:model-value="emit('update:modelValue', $event)">
    <el-steps :active="validateResult ? 2 : 1" finish-status="success" class="mb-4"><el-step title="上传文件" /><el-step title="预校验" /><el-step title="提交导入" /></el-steps>
    <el-form label-width="90px" class="mb-4"><el-form-item label="导入类型"><el-select v-model="importType"><el-option label="供应商" value="supplier" /><el-option label="订单" value="purchase_order" /><el-option label="ASN" value="delivery_notice" /></el-select></el-form-item></el-form>
    <el-upload drag action="#" :auto-upload="false" :before-upload="beforeUpload"><div>选择 Excel / CSV 文件进行预校验</div></el-upload>
    <div class="mt-4"><el-button type="primary" :loading="validating" @click="validate">开始预校验</el-button><el-button :disabled="!validateResult" @click="submit">提交导入</el-button></div>
    <template v-if="validateResult"><el-alert class="mt-4" type="warning" :closable="false" :title="`共 ${validateResult.total} 条，成功 ${validateResult.success} 条，失败 ${validateResult.failed} 条`" /><ImportErrorTable class="mt-4" :rows="validateResult.errors" /></template>
  </el-dialog>
</template>
