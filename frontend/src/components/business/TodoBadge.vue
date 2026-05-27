<script setup lang="ts">
defineProps<{ count: number }>()
</script>

<template>
  <el-badge :value="count" :hidden="count === 0" type="danger" />
</template>
