<script setup lang="ts">
defineProps<{
  modelValue: boolean
  fileName?: string
  previewUrl?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()
</script>

<template>
  <el-dialog :model-value="modelValue" title="附件预览" width="720px" @update:model-value="emit('update:modelValue', $event)">
    <div class="preview-box">
      <div class="preview-title">{{ fileName || '未选择附件' }}</div>
      <div class="preview-url">受控预览地址：{{ previewUrl || '-' }}</div>
      <el-empty description="当前为虚拟预览，真实环境通过受控接口获取临时预览地址" />
    </div>
  </el-dialog>
</template>

<style scoped>
.preview-box { padding: 20px; background: #f8fbff; border-radius: 14px; }
.preview-title { margin-bottom: 8px; font-size: 18px; font-weight: 800; }
.preview-url { margin-bottom: 16px; color: #718096; }
</style>
