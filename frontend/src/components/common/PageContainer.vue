<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <section class="page-card">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
        <div v-if="subtitle" class="page-subtitle">{{ subtitle }}</div>
      </div>
      <div>
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>
