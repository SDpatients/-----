<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const goDashboard = () => {
  const userType = userStore.user?.userType
  if (userType === 'supplier') {
    router.push('/supplier/dashboard')
  } else if (userType === 'internal') {
    router.push('/purchasing/dashboard')
  } else if (!userStore.token) {
    router.push('/login')
  } else {
    router.push('/login')
  }
}
</script>

<template>
  <div class="error-page"><h1>403</h1><p>当前账号暂无访问权限</p><el-button type="primary" @click="goDashboard">返回工作台</el-button></div>
</template>

<style scoped>
.error-page { display: grid; height: 100vh; place-content: center; text-align: center; background: #eef3f8; }
.error-page h1 { margin: 0; font-size: 92px; color: #1f5eff; }
.error-page p { margin: 8px 0 24px; color: #718096; }
</style>
