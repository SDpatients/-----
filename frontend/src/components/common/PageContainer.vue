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
        <div v-if="subtitle || $slots.subtitle" class="page-subtitle">
          <slot name="subtitle">{{ subtitle }}</slot>
        </div>
      </div>
      <div>
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>
