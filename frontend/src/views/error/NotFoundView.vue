<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="error-page"><h1>404</h1><p>页面不存在或已被移动</p><el-button type="primary" @click="router.push('/purchasing/dashboard')">返回工作台</el-button></div>
</template>

<style scoped>
.error-page { display: grid; height: 100vh; place-content: center; text-align: center; background: #eef3f8; }
.error-page h1 { margin: 0; font-size: 92px; color: #10233f; }
.error-page p { margin: 8px 0 24px; color: #718096; }
</style>
